package com.example.service;

import com.example.model.FlightIntention;
import com.example.model.RealPathPoint;
import com.example.model.ReplayData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loading and accessing replay data from files
 * 
 * Primary responsibilities:
 * - Load ReplayData from JSON files
 * - Provide basic data access methods
 * - Format timestamps for display
 * 
 * This service focuses on data loading and basic access.
 * For analysis, use DataAnalysisService.
 * For joining, use FlightDataJoinService.
 */
@Service
public class ReplayDataService {
    
    private final ObjectMapper objectMapper;
    
    public ReplayDataService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Load replay data from JSON file
     */
    public ReplayData loadReplayData(String filePath) throws IOException {
        File file = new File(filePath);
        return objectMapper.readValue(file, ReplayData.class);
    }
    
    /**
     * Get basic statistics about the replay data
     */
    public void printDataSummary(ReplayData data) {
        System.out.println("=== REPLAY DATA SUMMARY ===");
        System.out.println("Timestamp: " + formatTimestamp(data.getTime()));
        System.out.println("Real Path Points: " + (data.getListRealPath() != null ? data.getListRealPath().size() : 0));
        System.out.println("Flight Intentions: " + (data.getListFlightIntention() != null ? data.getListFlightIntention().size() : 0));
        System.out.println();
    }
    
    /**
     * Find flights by call sign in the data
     */
    public List<FlightIntention> findFlightsByCallSign(ReplayData data, String callSign) {
        if (data.getListFlightIntention() == null) {
            return new ArrayList<>();
        }
        
        return data.getListFlightIntention().stream()
                .filter(fi -> fi.getIndicative() != null && 
                             fi.getIndicative().toLowerCase().contains(callSign.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get tracking points for a specific flight plan
     */
    public List<RealPathPoint> getTrackingPointsForPlan(ReplayData data, int planId) {
        if (data.getListRealPath() == null) {
            return new ArrayList<>();
        }
        
        return data.getListRealPath().stream()
                .filter(rp -> rp.getPlanId() == planId)
                .sorted(Comparator.comparingInt(RealPathPoint::getSeqNum))
                .collect(Collectors.toList());
    }
    
    /**
     * Format timestamp for display
     */
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "N/A";
        }
        
        try {
            // Try to parse as long (Unix timestamp)
            long timestampLong = Long.parseLong(timestamp);
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestampLong), 
                ZoneId.systemDefault()
            );
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (NumberFormatException e) {
            // If not a number, return as is
            return timestamp;
        }
    }
} 