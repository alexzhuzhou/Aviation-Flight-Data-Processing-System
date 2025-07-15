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
 * Service class for processing aviation replay data
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
     * Analyze real path tracking data
     */
    public void analyzeRealPathData(ReplayData data) {
        if (data.getListRealPath() == null || data.getListRealPath().isEmpty()) {
            System.out.println("No real path data available.");
            return;
        }
        
        List<RealPathPoint> realPaths = data.getListRealPath();
        
        System.out.println("=== REAL PATH ANALYSIS ===");
        System.out.println("Total tracking points: " + realPaths.size());
        
        // Count simulated vs real points
        long simulatedCount = realPaths.stream().filter(RealPathPoint::isSimulating).count();
        long realCount = realPaths.size() - simulatedCount;
        System.out.println("Simulated points: " + simulatedCount);
        System.out.println("Real points: " + realCount);
        
        // Flight level statistics
        OptionalDouble avgFlightLevel = realPaths.stream()
                .mapToInt(RealPathPoint::getFlightLevel)
                .filter(fl -> fl > 0)
                .average();
        if (avgFlightLevel.isPresent()) {
            System.out.println("Average flight level: " + String.format("%.1f", avgFlightLevel.getAsDouble()));
        }
        
        // Speed statistics
        OptionalDouble avgSpeed = realPaths.stream()
                .mapToInt(RealPathPoint::getTrackSpeed)
                .filter(speed -> speed > 0)
                .average();
        if (avgSpeed.isPresent()) {
            System.out.println("Average speed: " + String.format("%.1f", avgSpeed.getAsDouble()) + " knots");
        }
        
        // Detector sources
        Map<String, Long> detectorSources = realPaths.stream()
                .filter(rp -> rp.getKinematic() != null)
                .collect(Collectors.groupingBy(
                        rp -> rp.getKinematic().getDetectorSource() != null ? 
                              rp.getKinematic().getDetectorSource() : "UNKNOWN",
                        Collectors.counting()
                ));
        
        System.out.println("\nDetector Sources:");
        detectorSources.forEach((source, count) -> 
            System.out.println("  " + source + ": " + count + " points"));
        
        // ACC Types (control sectors)
        Map<String, Long> accTypes = realPaths.stream()
                .filter(rp -> rp.getAccTypes() != null && !rp.getAccTypes().isEmpty())
                .flatMap(rp -> rp.getAccTypes().stream())
                .collect(Collectors.groupingBy(
                        accType -> accType,
                        Collectors.counting()
                ));
        
        System.out.println("\nControl Sectors (ACC Types):");
        accTypes.forEach((sector, count) -> 
            System.out.println("  " + sector + ": " + count + " points"));
        
        System.out.println();
    }
    
    /**
     * Analyze flight intention data
     */
    public void analyzeFlightIntentions(ReplayData data) {
        if (data.getListFlightIntention() == null || data.getListFlightIntention().isEmpty()) {
            System.out.println("No flight intention data available.");
            return;
        }
        
        List<FlightIntention> intentions = data.getListFlightIntention();
        
        System.out.println("=== FLIGHT INTENTIONS ANALYSIS ===");
        System.out.println("Total flight plans: " + intentions.size());
        
        // Count finished vs active flights
        long finishedCount = intentions.stream().filter(FlightIntention::isFinished).count();
        long activeCount = intentions.size() - finishedCount;
        System.out.println("Finished flights: " + finishedCount);
        System.out.println("Active flights: " + activeCount);
        
        // Aircraft types
        Map<String, Long> aircraftTypes = intentions.stream()
                .filter(fi -> fi.getAircraftType() != null && !fi.getAircraftType().isEmpty())
                .collect(Collectors.groupingBy(
                        FlightIntention::getAircraftType,
                        Collectors.counting()
                ));
        
        System.out.println("\nTop Aircraft Types:");
        aircraftTypes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> 
                    System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " flights"));
        
        // Airlines
        Map<String, Long> airlines = intentions.stream()
                .filter(fi -> fi.getAirline() != null && !fi.getAirline().isEmpty())
                .collect(Collectors.groupingBy(
                        FlightIntention::getAirline,
                        Collectors.counting()
                ));
        
        System.out.println("\nTop Airlines:");
        airlines.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> 
                    System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " flights"));
        
        // RVSM capability
        long rvsmCount = intentions.stream().filter(FlightIntention::isRvsm).count();
        System.out.println("\nRVSM capable flights: " + rvsmCount + " (" + 
                           String.format("%.1f", (double)rvsmCount / intentions.size() * 100) + "%)");
        
        // Correlation with tracking data
        long correlatedCount = intentions.stream().filter(FlightIntention::isCorrelated).count();
        System.out.println("Correlated with tracking: " + correlatedCount + " (" + 
                           String.format("%.1f", (double)correlatedCount / intentions.size() * 100) + "%)");
        
        System.out.println();
    }
    
    /**
     * Find correlations between flight intentions and real path data
     */
    public void analyzeCorrelations(ReplayData data) {
        if (data.getListRealPath() == null || data.getListFlightIntention() == null) {
            System.out.println("Insufficient data for correlation analysis.");
            return;
        }
        
        System.out.println("=== CORRELATION ANALYSIS ===");
        
        // Group real path points by planId
        Map<Integer, List<RealPathPoint>> realPathsByPlan = data.getListRealPath().stream()
                .collect(Collectors.groupingBy(RealPathPoint::getPlanId));
        
        // Group flight intentions by planId (handle duplicates by keeping the first one)
        Map<Integer, FlightIntention> intentionsByPlan = data.getListFlightIntention().stream()
                .collect(Collectors.toMap(
                    FlightIntention::getPlanId, 
                    fi -> fi,
                    (existing, replacement) -> existing // Keep the first one if duplicate
                ));
        
        System.out.println("Plans with both intention and tracking data: " + 
                          realPathsByPlan.keySet().stream()
                                  .filter(intentionsByPlan::containsKey)
                                  .count());
        
        // Show some examples of correlated data
        System.out.println("\nSample Correlated Flights:");
        realPathsByPlan.keySet().stream()
                .filter(intentionsByPlan::containsKey)
                .limit(5)
                .forEach(planId -> {
                    FlightIntention intention = intentionsByPlan.get(planId);
                    List<RealPathPoint> points = realPathsByPlan.get(planId);
                    System.out.println("  Plan " + planId + ": " + intention.getIndicative() + 
                                     " (" + intention.getAircraftType() + ") - " + 
                                     points.size() + " tracking points");
                });
        
        System.out.println();
    }
    
    /**
     * Display sample data for inspection
     */
    public void showSampleData(ReplayData data) {
        System.out.println("=== SAMPLE DATA ===");
        
        // Show first few real path points
        if (data.getListRealPath() != null && !data.getListRealPath().isEmpty()) {
            System.out.println("Sample Real Path Points:");
            data.getListRealPath().stream()
                    .limit(3)
                    .forEach(rp -> System.out.println("  " + rp));
        }
        
        // Show first few flight intentions
        if (data.getListFlightIntention() != null && !data.getListFlightIntention().isEmpty()) {
            System.out.println("\nSample Flight Intentions:");
            data.getListFlightIntention().stream()
                    .limit(3)
                    .forEach(fi -> System.out.println("  " + fi));
        }
        
        System.out.println();
    }
    
    /**
     * Format timestamp for display
     */
    private String formatTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Find flights by call sign
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
} 