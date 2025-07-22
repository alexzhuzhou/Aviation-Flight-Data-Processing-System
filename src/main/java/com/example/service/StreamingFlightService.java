package com.example.service;

import com.example.model.*;
import com.example.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

/**
 * Service for streaming flight data processing
 * Handles real-time insertion and updates of flight tracking data
 */
@Service
public class StreamingFlightService {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamingFlightService.class);
    
    @Autowired
    private FlightRepository flightRepository;
    
    /**
     * Process a ReplayPath packet and update database
     * This is the main entry point for streaming data
     */
    public ProcessingResult processReplayPath(ReplayPath replayPath) {
        if (replayPath == null) {
            return new ProcessingResult(0, 0, "Invalid replay path");
        }
        
        logger.info("Processing ReplayPath with {} flight intentions and {} real path points, packet timestamp: {}",
                replayPath.getListFlightIntention() != null ? replayPath.getListFlightIntention().size() : 0,
                replayPath.getListRealPath() != null ? replayPath.getListRealPath().size() : 0,
                replayPath.getPacketStoredTimestamp());
        
        int newFlights = 0;
        int updatedFlights = 0;
        String packetTimestamp = replayPath.getPacketStoredTimestamp();
        
        // Process flight intentions first (these define the flights)
        if (replayPath.getListFlightIntention() != null) {
            for (FlightIntention intention : replayPath.getListFlightIntention()) {
                if (processFlightIntention(intention, packetTimestamp)) {
                    newFlights++;
                }
            }
        }
        
        // Process real path points (tracking data) with packet timestamp
        if (replayPath.getListRealPath() != null) {
            updatedFlights += processRealPathPoints(replayPath.getListRealPath(), packetTimestamp);
        }
        
        String message = String.format("Processed %d new flights, updated %d flights with tracking data", 
                newFlights, updatedFlights);
        logger.info(message);
        
        return new ProcessingResult(newFlights, updatedFlights, message);
    }
    
    /**
     * Process a single flight intention
     * Creates new flight if it doesn't exist
     */
    private boolean processFlightIntention(FlightIntention intention, String packetTimestamp) {
        if (intention == null || intention.getPlanId() == 0) {
            logger.debug("Skipping invalid flight intention: {}", intention);
            return false;
        }
        
        // Check if flight already exists (by planId for uniqueness)
        var existingFlightOpt = flightRepository.findByPlanId(intention.getPlanId());
        
        if (existingFlightOpt.isEmpty()) {
            // Create new flight from intention
            JoinedFlightData newFlight = new JoinedFlightData(intention);
            newFlight.setLastPacketTimestamp(packetTimestamp);
            flightRepository.save(newFlight);
            
            logger.info("Created new flight: planId={}, indicative={}, timestamp={}", 
                    intention.getPlanId(), intention.getIndicative(), packetTimestamp);
            return true;
        } else {
            // Flight already exists, optionally update timestamp
            JoinedFlightData existingFlight = existingFlightOpt.get();
            if (packetTimestamp != null) {
                existingFlight.setLastPacketTimestamp(packetTimestamp);
                flightRepository.save(existingFlight);
            }
            logger.debug("Flight already exists: planId={}, indicative={}", 
                    intention.getPlanId(), intention.getIndicative());
        }
        
        return false; // Didn't create a new flight
    }
    
    /**
     * Process real path points and append to existing flights
     * Links via indicative ↔ indicativeSafe matching
     */
    private int processRealPathPoints(List<RealPathPoint> realPathPoints, String packetTimestamp) {
        // Group tracking points by indicativeSafe (for linking to flights)
        Map<String, List<RealPathPoint>> pointsByIndicative = realPathPoints.stream()
                .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        rp -> rp.getIndicativeSafe().trim()
                ));
        
        int updatedCount = 0;
        
        // Process indicative-based linking: indicativeSafe → indicative
        for (Map.Entry<String, List<RealPathPoint>> entry : pointsByIndicative.entrySet()) {
            String indicativeSafe = entry.getKey();
            List<RealPathPoint> newPoints = entry.getValue();
            
            // Find existing flight by matching indicative
            var existingFlightOpt = flightRepository.findByIndicative(indicativeSafe);
            
            if (existingFlightOpt.isPresent()) {
                updatedCount += updateFlightWithTrackingPoints(existingFlightOpt.get(), newPoints, "indicative=" + indicativeSafe, packetTimestamp);
            } else {
                logger.warn("Received tracking data for unknown flight indicative: {}", indicativeSafe);
                // Optionally, you could create a new flight here with only tracking data
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Helper method to update flight with new tracking points
     */
    private int updateFlightWithTrackingPoints(JoinedFlightData existingFlight, List<RealPathPoint> newPoints, String identifier, String packetTimestamp) {
        // Convert new RealPathPoints to TrackingPoints
        List<TrackingPoint> newTrackingPoints = newPoints.stream()
                .map(point -> new TrackingPoint(point, packetTimestamp))
                .collect(Collectors.toList());
        
        // Get existing tracking points
        List<TrackingPoint> allTrackingPoints = existingFlight.getTrackingPoints();
        if (allTrackingPoints == null) {
            allTrackingPoints = new ArrayList<>();
        }
        
        // Create a set of existing coordinate+indicativeSafe combinations for deduplication
        Set<String> existingCoordinateKeys = allTrackingPoints.stream()
                .map(point -> createCoordinateKey(point.getLatitude(), point.getLongitude(), point.getIndicativeSafe()))
                .collect(Collectors.toSet());
        
        // Filter out tracking points that already exist (based on lat+lon+indicativeSafe)
        List<TrackingPoint> uniqueNewPoints = newTrackingPoints.stream()
                .filter(point -> !existingCoordinateKeys.contains(
                    createCoordinateKey(point.getLatitude(), point.getLongitude(), point.getIndicativeSafe())
                ))
                .collect(Collectors.toList());
        
        if (uniqueNewPoints.isEmpty()) {
            logger.debug("No new tracking points to add for flight {}", identifier);
            return 0; // No update needed
        }
        
        // Append only unique tracking points
        allTrackingPoints.addAll(uniqueNewPoints);
        
        // Update flight with new tracking points
        existingFlight.setTrackingPoints(allTrackingPoints);
        existingFlight.setHasTrackingData(true);
        existingFlight.setTotalTrackingPoints(allTrackingPoints.size());
        
        // Update the flight with the packet timestamp
        if (packetTimestamp != null) {
            existingFlight.setLastPacketTimestamp(packetTimestamp);
        }
        
        flightRepository.save(existingFlight);
        
        logger.info("Updated flight {} with {} new tracking points (total: {})", 
                identifier, uniqueNewPoints.size(), allTrackingPoints.size());
        
        return 1; // One flight updated
    }

    /**
     * Create a unique key for coordinate+indicativeSafe combination
     */
    private String createCoordinateKey(double latitude, double longitude, String indicativeSafe) {
        // Round coordinates to 6 decimal places (~1 meter precision) to handle floating point precision issues
        double roundedLat = Math.round(latitude * 1000000.0) / 1000000.0;
        double roundedLon = Math.round(longitude * 1000000.0) / 1000000.0;
        return String.format("%.6f,%.6f,%s", roundedLat, roundedLon, 
                indicativeSafe != null ? indicativeSafe : "");
    }

    /**
     * Clean up duplicate tracking points from all flights
     * This method removes duplicate tracking points based on lat+lon+indicativeSafe
     */
    public int cleanupDuplicateTrackingPoints() {
        logger.info("Starting cleanup of duplicate tracking points...");
        
        List<JoinedFlightData> allFlights = flightRepository.findAll();
        int totalCleaned = 0;
        int flightsUpdated = 0;
        
        for (JoinedFlightData flight : allFlights) {
            List<TrackingPoint> trackingPoints = flight.getTrackingPoints();
            if (trackingPoints == null || trackingPoints.isEmpty()) {
                continue;
            }
            
            int originalSize = trackingPoints.size();
            
            // Remove duplicates based on lat+lon+indicativeSafe, keeping the first occurrence
            Map<String, TrackingPoint> uniquePoints = new LinkedHashMap<>();
            for (TrackingPoint point : trackingPoints) {
                String key = createCoordinateKey(point.getLatitude(), point.getLongitude(), point.getIndicativeSafe());
                uniquePoints.putIfAbsent(key, point);
            }
            
            List<TrackingPoint> cleanedPoints = new ArrayList<>(uniquePoints.values());
            
            if (cleanedPoints.size() < originalSize) {
                flight.setTrackingPoints(cleanedPoints);
                flight.setTotalTrackingPoints(cleanedPoints.size());
                flight.setHasTrackingData(!cleanedPoints.isEmpty());
                
                flightRepository.save(flight);
                
                int removed = originalSize - cleanedPoints.size();
                totalCleaned += removed;
                flightsUpdated++;
                
                logger.info("Cleaned flight {}: removed {} duplicate tracking points ({} -> {})", 
                        flight.getIndicative(), removed, originalSize, cleanedPoints.size());
            }
        }
        
        logger.info("Cleanup completed: {} flights updated, {} total duplicate tracking points removed", 
                flightsUpdated, totalCleaned);
        
        return totalCleaned;
    }

    /**
     * Get flight statistics
     */
    public FlightStats getStats() {
        long totalFlights = flightRepository.count();
        List<JoinedFlightData> allFlights = flightRepository.findAll();
        
        long flightsWithTracking = allFlights.stream()
                .filter(JoinedFlightData::isHasTrackingData)
                .count();
        
        int totalTrackingPoints = allFlights.stream()
                .mapToInt(JoinedFlightData::getTotalTrackingPoints)
                .sum();
        
        return new FlightStats(totalFlights, flightsWithTracking, totalTrackingPoints);
    }
    
    /**
     * Result of processing operation
     */
    public static class ProcessingResult {
        private final int newFlights;
        private final int updatedFlights;
        private final String message;
        
        public ProcessingResult(int newFlights, int updatedFlights, String message) {
            this.newFlights = newFlights;
            this.updatedFlights = updatedFlights;
            this.message = message;
        }
        
        public int getNewFlights() { return newFlights; }
        public int getUpdatedFlights() { return updatedFlights; }
        public String getMessage() { return message; }
    }
    
    /**
     * Flight statistics
     */
    public static class FlightStats {
        private final long totalFlights;
        private final long flightsWithTracking;
        private final int totalTrackingPoints;
        
        public FlightStats(long totalFlights, long flightsWithTracking, int totalTrackingPoints) {
            this.totalFlights = totalFlights;
            this.flightsWithTracking = flightsWithTracking;
            this.totalTrackingPoints = totalTrackingPoints;
        }
        
        public long getTotalFlights() { return totalFlights; }
        public long getFlightsWithTracking() { return flightsWithTracking; }
        public int getTotalTrackingPoints() { return totalTrackingPoints; }
    }
} 