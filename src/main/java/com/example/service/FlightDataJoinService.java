package com.example.service;

import com.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for joining flight intentions with tracking points and data export
 * 
 * Primary responsibilities:
 * - Join flight intentions with tracking points using indicative matching
 * - Export joined data to JSON format
 * - Provide basic search functionality for joined data
 * 
 * This service focuses on data joining and export operations.
 * For analysis, use DataAnalysisService.
 * For file loading, use ReplayDataService.
 */
@Service
public class FlightDataJoinService {
    
    private final ObjectMapper objectMapper;
    
    public FlightDataJoinService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Join flight intentions with tracking points based on indicative matching
     */
    public List<JoinedFlightData> joinFlightData(ReplayData replayData) {
        if (replayData == null) {
            return new ArrayList<>();
        }
        
        List<FlightIntention> flightIntentions = replayData.getListFlightIntention();
        List<RealPathPoint> realPathPoints = replayData.getListRealPath();
        
        if (flightIntentions == null || flightIntentions.isEmpty()) {
            System.out.println("No flight intentions to join");
            return new ArrayList<>();
        }
        
        // Group tracking points by indicativeSafe
        Map<String, List<RealPathPoint>> trackingPointsByIndicative = new HashMap<>();
        if (realPathPoints != null) {
            trackingPointsByIndicative = realPathPoints.stream()
                    .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            rp -> rp.getIndicativeSafe().trim(),
                            Collectors.toList()
                    ));
        }
        
        System.out.println("=== JOIN PROCESS ===");
        System.out.println("Flight intentions to process: " + flightIntentions.size());
        System.out.println("Unique indicatives with tracking data: " + trackingPointsByIndicative.size());
        
        List<JoinedFlightData> joinedFlights = new ArrayList<>();
        int matchedCount = 0;
        int unmatchedCount = 0;
        
        // Process each flight intention
        for (FlightIntention flightIntention : flightIntentions) {
            JoinedFlightData joinedFlight = new JoinedFlightData(flightIntention);
            
            String indicative = flightIntention.getIndicative();
            if (indicative != null && !indicative.trim().isEmpty()) {
                indicative = indicative.trim();
                
                // Find matching tracking points
                List<RealPathPoint> matchingPoints = trackingPointsByIndicative.get(indicative);
                
                if (matchingPoints != null && !matchingPoints.isEmpty()) {
                    // Convert to TrackingPoint objects and sort by sequence number
                    List<TrackingPoint> trackingPoints = matchingPoints.stream()
                            .map(TrackingPoint::new)
                            .sorted(Comparator.comparingInt(TrackingPoint::getSeqNum))
                            .collect(Collectors.toList());
                    
                    joinedFlight.setTrackingPoints(trackingPoints);
                    matchedCount++;
                } else {
                    // No tracking data found
                    joinedFlight.setTrackingPoints(new ArrayList<>());
                    unmatchedCount++;
                }
            } else {
                // No indicative to match
                joinedFlight.setTrackingPoints(new ArrayList<>());
                unmatchedCount++;
            }
            
            joinedFlights.add(joinedFlight);
        }
        
        System.out.println("Flights with tracking data: " + matchedCount);
        System.out.println("Flights without tracking data: " + unmatchedCount);
        
        return joinedFlights;
    }
    
    /**
     * Export joined flight data to JSON file
     */
    public void exportToJson(List<JoinedFlightData> joinedFlights, String filename) throws IOException {
        File outputFile = new File(filename);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, joinedFlights);
        System.out.println("Exported " + joinedFlights.size() + " joined flights to: " + filename);
    }
    
    /**
     * Get tracking points that couldn't be matched to any flight
     */
    public List<TrackingPoint> getUnmatchedTrackingPoints(ReplayData replayData) {
        if (replayData == null || replayData.getListRealPath() == null) {
            return new ArrayList<>();
        }
        
        // Get all indicatives from flight intentions
        final Set<String> flightIndicatives = replayData.getListFlightIntention() != null ?
                replayData.getListFlightIntention().stream()
                        .map(FlightIntention::getIndicative)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .collect(Collectors.toSet()) :
                new HashSet<>();
        
        // Find tracking points with indicatives not in flight intentions
        return replayData.getListRealPath().stream()
                .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().trim().isEmpty())
                .filter(rp -> !flightIndicatives.contains(rp.getIndicativeSafe().trim()))
                .map(TrackingPoint::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Search joined flights by various criteria
     */
    public List<JoinedFlightData> searchFlights(List<JoinedFlightData> joinedFlights, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return joinedFlights;
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        
        return joinedFlights.stream()
                .filter(flight -> 
                    (flight.getIndicative() != null && flight.getIndicative().toLowerCase().contains(lowerSearchTerm)) ||
                    (flight.getAircraftType() != null && flight.getAircraftType().toLowerCase().contains(lowerSearchTerm)) ||
                    (flight.getAirline() != null && flight.getAirline().toLowerCase().contains(lowerSearchTerm)) ||
                    String.valueOf(flight.getPlanId()).contains(lowerSearchTerm)
                )
                .collect(Collectors.toList());
    }
} 