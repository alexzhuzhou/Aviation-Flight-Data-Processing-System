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
 * Service for processing flight streaming data with timestamp-based disambiguation
 * 
 * Key features:
 * - Flight matching by planId (primary unique identifier)
 * - Cross-packet tracking point assignment using indicative ↔ indicativeSafe
 * - Timestamp-based disambiguation for multiple flights with same indicative
 * - Deduplication using timestamp + coordinates + indicativeSafe for uniqueness
 * 
 * Disambiguation Strategy:
 * 1. Match tracking point timestamp to flight time window (flightPlanDate to currentDateTimeOfArrival)
 * 2. Find closest time window with 30-minute tolerance
 * 3. If no temporal match found, discard tracking points to prevent data contamination
 * 
 * This approach ensures tracking points are assigned to the correct flight instance
 * only when there is clear temporal evidence, preventing cross-flight data mixing.
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
        
        // Process real path points - allow cross-packet updates for flight progress
        if (replayPath.getListRealPath() != null) {
            updatedFlights += processRealPathPoints(replayPath.getListRealPath(), packetTimestamp);
        }
        
        String message = String.format("Processed %d new flights, updated %d flights with tracking data (cross-packet progress tracking)", 
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
     * Allows cross-packet tracking for flight progress while maintaining data integrity
     */
    private int processRealPathPoints(List<RealPathPoint> realPathPoints, String packetTimestamp) {
        // Group tracking points by indicativeSafe (for linking to flights)
        Map<String, List<RealPathPoint>> pointsByIndicative = realPathPoints.stream()
                .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        rp -> rp.getIndicativeSafe().trim()
                ));
        
        int updatedCount = 0;
        
        // Process indicative-based linking: Find flights across all packets (cross-packet progress)
        for (Map.Entry<String, List<RealPathPoint>> entry : pointsByIndicative.entrySet()) {
            String indicativeSafe = entry.getKey();
            List<RealPathPoint> newPoints = entry.getValue();
            
            // Find ALL flights with matching indicative (handles multiple matches)
            List<JoinedFlightData> candidateFlights = flightRepository.findAllByIndicative(indicativeSafe);
            
            if (candidateFlights.isEmpty()) {
                logger.debug("Received tracking data for unknown flight indicative: {} - no existing flight found", indicativeSafe);
                continue;
            }
            
            if (candidateFlights.size() == 1) {
                // Simple case: only one flight with this indicative
                JoinedFlightData targetFlight = candidateFlights.get(0);
                updatedCount += updateFlightWithTrackingPoints(targetFlight, newPoints, "single-match=" + indicativeSafe, packetTimestamp);
                logger.info("Added {} tracking points to flight {} (single match)", newPoints.size(), indicativeSafe);
            } else {
                // Complex case: multiple flights with same indicative - need disambiguation
                JoinedFlightData bestMatch = disambiguateFlights(candidateFlights, newPoints, packetTimestamp, indicativeSafe);
                if (bestMatch != null) {
                    updatedCount += updateFlightWithTrackingPoints(bestMatch, newPoints, "disambiguated=" + indicativeSafe, packetTimestamp);
                    logger.info("Added {} tracking points to flight {} (disambiguated from {} candidates)", 
                        newPoints.size(), indicativeSafe, candidateFlights.size());
                } else {
                    logger.warn("Could not disambiguate between {} flights with indicative {}", 
                        candidateFlights.size(), indicativeSafe);
                }
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Disambiguate between multiple flights with the same indicative
     * Uses timestamp-based matching to assign tracking points to the correct flight
     */
    private JoinedFlightData disambiguateFlights(List<JoinedFlightData> candidateFlights, 
                                               List<RealPathPoint> newPoints, 
                                               String packetTimestamp,
                                               String indicative) {
        
        logger.debug("Disambiguating between {} flights with indicative {}", candidateFlights.size(), indicative);
        
        // Strategy 1: Find flight with time window that matches packet timestamp
        // All tracking points in the same packet have the same timestamp (packet timestamp)
        if (packetTimestamp != null) {
            try {
                long trackingTimestamp = parseTimestamp(packetTimestamp);
                
                for (JoinedFlightData flight : candidateFlights) {
                    if (isTrackingPointWithinFlightTimeWindow(flight, trackingTimestamp)) {
                        logger.debug("Selected flight {} - packet timestamp {} is within flight time window", 
                            flight.getId(), trackingTimestamp);
                        return flight;
                    }
                }
                
                // Strategy 2: Find flight with closest time window (with tolerance)
                JoinedFlightData closestFlight = findFlightWithClosestTimeWindow(candidateFlights, trackingTimestamp);
                if (closestFlight != null) {
                    logger.debug("Selected flight {} - closest time window to packet timestamp {}", 
                        closestFlight.getId(), trackingTimestamp);
                    return closestFlight;
                }
            } catch (Exception e) {
                logger.debug("Could not parse packet timestamp {}: {}", packetTimestamp, e.getMessage());
            }
        }
        
        // No suitable flight found - cannot disambiguate based on timing
        logger.warn("Could not disambiguate between {} flights with indicative {} - no flight matches timestamp criteria. Tracking points will be discarded to prevent data contamination.", 
                   candidateFlights.size(), indicative);
        return null;
    }
    
    /**
     * Check if a tracking point timestamp falls within a flight's time window
     */
    private boolean isTrackingPointWithinFlightTimeWindow(JoinedFlightData flight, long trackingTimestamp) {
        try {
            // Parse flight plan date (departure time)
            long flightPlanTime = parseTimestamp(flight.getFlightPlanDate());
            
            // Parse current date time of arrival
            long arrivalTime = parseTimestamp(flight.getCurrentDateTimeOfArrival());
            
            // Check if tracking timestamp is within the flight window
            return trackingTimestamp >= flightPlanTime && trackingTimestamp <= arrivalTime;
            
        } catch (Exception e) {
            logger.debug("Could not parse flight times for flight {}: {}", flight.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Find the flight with the time window closest to the tracking point timestamp
     * Uses tolerance to allow tracking points slightly outside the flight window
     */
    private JoinedFlightData findFlightWithClosestTimeWindow(List<JoinedFlightData> candidateFlights, long trackingTimestamp) {
        final long TOLERANCE_MS = 30 * 60 * 1000; // 30 minutes tolerance
        
        JoinedFlightData closestFlight = null;
        long smallestDistance = Long.MAX_VALUE;
        
        for (JoinedFlightData flight : candidateFlights) {
            try {
                long flightPlanTime = parseTimestamp(flight.getFlightPlanDate());
                long arrivalTime = parseTimestamp(flight.getCurrentDateTimeOfArrival());
                
                long distance;
                if (trackingTimestamp < flightPlanTime) {
                    // Before flight window
                    distance = flightPlanTime - trackingTimestamp;
                } else if (trackingTimestamp > arrivalTime) {
                    // After flight window
                    distance = trackingTimestamp - arrivalTime;
                } else {
                    // Within flight window
                    distance = 0;
                }
                
                // Only consider flights within tolerance
                if (distance <= TOLERANCE_MS && distance < smallestDistance) {
                    smallestDistance = distance;
                    closestFlight = flight;
                }
                
            } catch (Exception e) {
                logger.debug("Could not parse flight times for flight {}: {}", flight.getId(), e.getMessage());
            }
        }
        
        return closestFlight;
    }
    
    /**
     * Parse timestamp string to milliseconds
     * Handles various timestamp formats used in the flight data
     * FIXED: Always parse timestamps in UTC to avoid timezone issues
     */
    private long parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp string is null or empty");
        }
        
        try {
            // Try parsing as ISO format first (e.g., "2025-07-11T00:00:00.000+0000")
            // FIXED: Ensure UTC parsing by properly handling timezone indicators
            String normalized = timestampStr.replace("+0000", "Z");
            return java.time.Instant.parse(normalized).toEpochMilli();
        } catch (Exception e) {
            try {
                // Try parsing as long value (Unix timestamp - already in UTC)
                return Long.parseLong(timestampStr);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Could not parse timestamp: " + timestampStr, nfe);
            }
        }
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
        
        // Enhanced deduplication: Use timestamp + coordinates + indicativeSafe for better uniqueness
        Set<String> existingTrackingKeys = allTrackingPoints.stream()
                .map(point -> createTimestampCoordinateKey(point.getTimestamp(), point.getLatitude(), point.getLongitude(), point.getIndicativeSafe()))
                .collect(Collectors.toSet());
        
        // Filter out tracking points that already exist (based on timestamp+coordinates+indicativeSafe)
        List<TrackingPoint> uniqueNewPoints = newTrackingPoints.stream()
                .filter(point -> !existingTrackingKeys.contains(
                    createTimestampCoordinateKey(point.getTimestamp(), point.getLatitude(), point.getLongitude(), point.getIndicativeSafe())
                ))
                .collect(Collectors.toList());
        
        if (uniqueNewPoints.isEmpty()) {
            logger.debug("No new tracking points to add for flight {} (all timestamp+coordinate combinations already exist)", identifier);
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
     * Create a unique key for timestamp + coordinates + indicativeSafe combination
     * This prevents duplicate tracking points based on time and location
     */
    private String createTimestampCoordinateKey(long timestamp, double latitude, double longitude, String indicativeSafe) {
        // Round coordinates to 6 decimal places (~1 meter precision) to handle floating point precision issues
        double roundedLat = Math.round(latitude * 1000000.0) / 1000000.0;
        double roundedLon = Math.round(longitude * 1000000.0) / 1000000.0;
        return String.format("%d,%.6f,%.6f,%s", timestamp, roundedLat, roundedLon, 
                indicativeSafe != null ? indicativeSafe : "");
    }

    /**
     * Create a unique key for coordinate+indicativeSafe combination (legacy method)
     * Keeping this for the cleanup method
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
     * Analyze duplicate indicatives in the database
     * This helps identify data quality issues with multiple flights having same call signs
     */
    public DuplicateIndicativeAnalysis analyzeDuplicateIndicatives() {
        List<JoinedFlightData> allFlights = flightRepository.findAll();
        
        // Group flights by indicative
        Map<String, List<JoinedFlightData>> flightsByIndicative = allFlights.stream()
                .filter(f -> f.getIndicative() != null && !f.getIndicative().trim().isEmpty())
                .collect(Collectors.groupingBy(f -> f.getIndicative().trim()));
        
        // Find duplicates
        Map<String, List<JoinedFlightData>> duplicates = flightsByIndicative.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        int totalDuplicateIndicatives = duplicates.size();
        int totalAffectedFlights = duplicates.values().stream()
                .mapToInt(List::size)
                .sum();
        
        logger.info("=== DUPLICATE INDICATIVE ANALYSIS ===");
        logger.info("Total unique indicatives: {}", flightsByIndicative.size());
        logger.info("Duplicate indicatives found: {}", totalDuplicateIndicatives);
        logger.info("Total flights affected: {}", totalAffectedFlights);
        
        if (!duplicates.isEmpty()) {
            logger.warn("🚨 DUPLICATE INDICATIVES DETECTED! This could cause tracking point contamination:");
            duplicates.entrySet().stream()
                    .limit(10) // Show first 10 duplicates
                    .forEach(entry -> {
                        String indicative = entry.getKey();
                        List<JoinedFlightData> flights = entry.getValue();
                        logger.warn("  - {}: {} flights (IDs: {})", 
                                indicative, 
                                flights.size(),
                                flights.stream().map(JoinedFlightData::getId).collect(Collectors.joining(", ")));
                    });
        }
        
        return new DuplicateIndicativeAnalysis(totalDuplicateIndicatives, totalAffectedFlights, duplicates);
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
    
    /**
     * Analysis of duplicate indicatives in the database
     */
    public static class DuplicateIndicativeAnalysis {
        private final int duplicateIndicatives;
        private final int affectedFlights;
        private final Map<String, List<JoinedFlightData>> duplicateDetails;
        
        public DuplicateIndicativeAnalysis(int duplicateIndicatives, int affectedFlights, 
                                         Map<String, List<JoinedFlightData>> duplicateDetails) {
            this.duplicateIndicatives = duplicateIndicatives;
            this.affectedFlights = affectedFlights;
            this.duplicateDetails = duplicateDetails;
        }
        
        public int getDuplicateIndicatives() { return duplicateIndicatives; }
        public int getAffectedFlights() { return affectedFlights; }
        public Map<String, List<JoinedFlightData>> getDuplicateDetails() { return duplicateDetails; }
        
        public boolean hasDuplicates() { return duplicateIndicatives > 0; }
    }
} 