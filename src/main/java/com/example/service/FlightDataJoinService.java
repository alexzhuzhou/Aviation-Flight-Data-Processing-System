package com.example.service;

import com.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for joining flight intentions with tracking points
 */
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
     * Analyze the join results
     */
    public void analyzeJoinResults(List<JoinedFlightData> joinedFlights) {
        if (joinedFlights == null || joinedFlights.isEmpty()) {
            System.out.println("No joined flight data to analyze");
            return;
        }
        
        System.out.println("=== JOIN RESULTS ANALYSIS ===");
        System.out.println("Total flights: " + joinedFlights.size());
        
        // Count flights with/without tracking data
        long flightsWithTracking = joinedFlights.stream()
                .filter(JoinedFlightData::isHasTrackingData)
                .count();
        
        System.out.println("Flights with tracking data: " + flightsWithTracking);
        System.out.println("Flights without tracking data: " + (joinedFlights.size() - flightsWithTracking));
        
        // Total tracking points
        int totalTrackingPoints = joinedFlights.stream()
                .mapToInt(JoinedFlightData::getTotalTrackingPoints)
                .sum();
        
        System.out.println("Total tracking points: " + totalTrackingPoints);
        
        // Average tracking points per flight (for flights with tracking)
        double avgTrackingPoints = joinedFlights.stream()
                .filter(JoinedFlightData::isHasTrackingData)
                .mapToInt(JoinedFlightData::getTotalTrackingPoints)
                .average()
                .orElse(0.0);
        
        System.out.println("Average tracking points per flight (with tracking): " + String.format("%.1f", avgTrackingPoints));
        
        // Top flights by tracking points
        System.out.println("\nTop flights by tracking points:");
        joinedFlights.stream()
                .filter(JoinedFlightData::isHasTrackingData)
                .sorted(Comparator.comparingInt(JoinedFlightData::getTotalTrackingPoints).reversed())
                .limit(10)
                .forEach(flight -> System.out.println("  " + flight.getIndicative() + 
                        " (" + flight.getAircraftType() + "): " + flight.getTotalTrackingPoints() + " points"));
        
        // Airlines with most tracking data
        Map<String, Long> airlineTrackingPoints = joinedFlights.stream()
                .filter(JoinedFlightData::isHasTrackingData)
                .filter(flight -> flight.getAirline() != null)
                .collect(Collectors.groupingBy(
                        JoinedFlightData::getAirline,
                        Collectors.summingLong(JoinedFlightData::getTotalTrackingPoints)
                ));
        
        System.out.println("\nAirlines by total tracking points:");
        airlineTrackingPoints.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " points"));
        
        System.out.println();
    }
    
    /**
     * Show sample joined data
     */
    public void showSampleJoinedData(List<JoinedFlightData> joinedFlights) {
        if (joinedFlights == null || joinedFlights.isEmpty()) {
            System.out.println("No joined flight data to show");
            return;
        }
        
        System.out.println("=== SAMPLE JOINED DATA ===");
        
        // Show flights with tracking data
        System.out.println("Flights with tracking data:");
        joinedFlights.stream()
                .filter(JoinedFlightData::isHasTrackingData)
                .limit(3)
                .forEach(flight -> {
                    System.out.println("  " + flight.getIndicative() + " (" + flight.getAircraftType() + 
                            ", " + flight.getAirline() + ") - " + flight.getTotalTrackingPoints() + " tracking points");
                    
                    // Show first few tracking points
                    if (flight.getTrackingPoints() != null && !flight.getTrackingPoints().isEmpty()) {
                        flight.getTrackingPoints().stream()
                                .limit(2)
                                .forEach(tp -> System.out.println("    " + tp));
                    }
                });
        
        // Show flights without tracking data
        System.out.println("\nFlights without tracking data:");
        joinedFlights.stream()
                .filter(flight -> !flight.isHasTrackingData())
                .limit(3)
                .forEach(flight -> System.out.println("  " + flight.getIndicative() + 
                        " (" + flight.getAircraftType() + ", " + flight.getAirline() + ") - No tracking data"));
        
        System.out.println();
    }
    
    /**
     * Export joined data to JSON file (ready for MongoDB import)
     */
    public void exportToJson(List<JoinedFlightData> joinedFlights, String filename) throws IOException {
        if (joinedFlights == null || joinedFlights.isEmpty()) {
            System.out.println("No data to export");
            return;
        }
        
        File outputFile = new File(filename);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, joinedFlights);
        
        System.out.println("Exported " + joinedFlights.size() + " joined flights to: " + filename);
        System.out.println("File size: " + String.format("%.2f MB", outputFile.length() / (1024.0 * 1024.0)));
        System.out.println("Ready for MongoDB import using: mongoimport --collection flights --file " + filename);
    }
    
    /**
     * Get unmatched tracking points (tracking points without flight intention)
     */
    public List<TrackingPoint> getUnmatchedTrackingPoints(ReplayData replayData) {
        if (replayData == null || replayData.getListRealPath() == null || replayData.getListFlightIntention() == null) {
            return new ArrayList<>();
        }
        
        // Get all flight intention indicatives
        Set<String> flightIndicatives = replayData.getListFlightIntention().stream()
                .map(FlightIntention::getIndicative)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());
        
        // Find tracking points without matching flight intention
        List<TrackingPoint> unmatchedPoints = replayData.getListRealPath().stream()
                .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().trim().isEmpty())
                .filter(rp -> !flightIndicatives.contains(rp.getIndicativeSafe().trim()))
                .map(TrackingPoint::new)
                .collect(Collectors.toList());
        
        System.out.println("Unmatched tracking points: " + unmatchedPoints.size());
        
        return unmatchedPoints;
    }
    
    /**
     * Search joined flights by various criteria
     */
    public List<JoinedFlightData> searchFlights(List<JoinedFlightData> joinedFlights, String searchTerm) {
        if (joinedFlights == null || searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String search = searchTerm.trim().toLowerCase();
        
        return joinedFlights.stream()
                .filter(flight -> 
                    (flight.getIndicative() != null && flight.getIndicative().toLowerCase().contains(search)) ||
                    (flight.getAirline() != null && flight.getAirline().toLowerCase().contains(search)) ||
                    (flight.getAircraftType() != null && flight.getAircraftType().toLowerCase().contains(search))
                )
                .collect(Collectors.toList());
    }
} 