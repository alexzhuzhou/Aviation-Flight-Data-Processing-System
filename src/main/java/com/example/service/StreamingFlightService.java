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
import java.util.stream.Collectors;

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
        
        logger.info("Processing ReplayPath with {} flight intentions and {} real path points",
                replayPath.getListFlightIntention() != null ? replayPath.getListFlightIntention().size() : 0,
                replayPath.getListRealPath() != null ? replayPath.getListRealPath().size() : 0);
        
        int newFlights = 0;
        int updatedFlights = 0;
        
        // Process flight intentions first (these define the flights)
        if (replayPath.getListFlightIntention() != null) {
            for (FlightIntention intention : replayPath.getListFlightIntention()) {
                if (processFlightIntention(intention)) {
                    newFlights++;
                }
            }
        }
        
        // Process real path points (tracking data)
        if (replayPath.getListRealPath() != null) {
            updatedFlights += processRealPathPoints(replayPath.getListRealPath());
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
    private boolean processFlightIntention(FlightIntention intention) {
        if (intention.getPlanId() == 0) {
            logger.warn("Skipping flight intention with planId = 0");
            return false;
        }
        
        long planId = intention.getPlanId();
        
        // Check if flight already exists by planId
        if (flightRepository.existsByPlanId(planId)) {
            logger.debug("Flight with planId {} already exists, skipping", planId);
            return false;
        }
        
        // Create new flight record
        JoinedFlightData newFlight = new JoinedFlightData(intention);
        newFlight.setTrackingPoints(new ArrayList<>()); // Initialize empty tracking points
        
        flightRepository.save(newFlight);
        logger.info("Created new flight: planId={}, indicative={}", planId, intention.getIndicative());
        return true;
    }
    
    /**
     * Process real path points and append to existing flights
     * Links via indicative ↔ indicativeSafe matching
     */
    private int processRealPathPoints(List<RealPathPoint> realPathPoints) {
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
                updatedCount += updateFlightWithTrackingPoints(existingFlightOpt.get(), newPoints, "indicative=" + indicativeSafe);
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
    private int updateFlightWithTrackingPoints(JoinedFlightData existingFlight, List<RealPathPoint> newPoints, String identifier) {
        // Convert new RealPathPoints to TrackingPoints
        List<TrackingPoint> newTrackingPoints = newPoints.stream()
                .map(TrackingPoint::new)
                .collect(Collectors.toList());
        
        // Append to existing tracking points
        List<TrackingPoint> allTrackingPoints = existingFlight.getTrackingPoints();
        if (allTrackingPoints == null) {
            allTrackingPoints = new ArrayList<>();
        }
        allTrackingPoints.addAll(newTrackingPoints);
        
        // Update flight with new tracking points
        existingFlight.setTrackingPoints(allTrackingPoints);
        flightRepository.save(existingFlight);
        
        logger.info("Updated flight {} with {} new tracking points (total: {})", 
                identifier, newTrackingPoints.size(), allTrackingPoints.size());
        
        return 1; // One flight updated
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