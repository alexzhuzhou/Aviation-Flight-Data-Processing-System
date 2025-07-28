package com.example.service;

import com.example.model.FlightIntention;
import com.example.model.RealPathPoint;
import com.example.model.ReplayData;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for comprehensive data analysis and pattern recognition
 * 
 * Primary responsibilities:
 * - Analyze data patterns and distributions
 * - Provide insights for join strategies
 * - Generate statistical reports
 * - Support data quality assessment
 * 
 * This service consolidates all analysis functionality that was previously
 * scattered across multiple services.
 */
@Service
public class DataAnalysisService {
    
    /**
     * Analyze planId distribution in both lists
     */
    public void analyzePlanIds(ReplayData data) {
        System.out.println("=== PLAN ID ANALYSIS ===");
        
        if (data.getListRealPath() != null) {
            Map<Integer, Long> realPathPlanIds = data.getListRealPath().stream()
                    .collect(Collectors.groupingBy(RealPathPoint::getPlanId, Collectors.counting()));
            
            System.out.println("Real Path Plan IDs:");
            System.out.println("  Total unique plan IDs: " + realPathPlanIds.size());
            System.out.println("  Plan ID 0 count: " + realPathPlanIds.getOrDefault(0, 0L));
            System.out.println("  Non-zero plan IDs: " + realPathPlanIds.entrySet().stream()
                    .filter(entry -> entry.getKey() != 0)
                    .count());
            
            // Show top plan IDs
            System.out.println("  Top plan IDs by frequency:");
            realPathPlanIds.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> System.out.println("    Plan ID " + entry.getKey() + ": " + entry.getValue() + " points"));
        }
        
        if (data.getListFlightIntention() != null) {
            Map<Integer, Long> flightIntentionPlanIds = data.getListFlightIntention().stream()
                    .collect(Collectors.groupingBy(FlightIntention::getPlanId, Collectors.counting()));
            
            System.out.println("\nFlight Intention Plan IDs:");
            System.out.println("  Total unique plan IDs: " + flightIntentionPlanIds.size());
            System.out.println("  Plan ID 0 count: " + flightIntentionPlanIds.getOrDefault(0, 0L));
            System.out.println("  Non-zero plan IDs: " + flightIntentionPlanIds.entrySet().stream()
                    .filter(entry -> entry.getKey() != 0)
                    .count());
            
            // Show all plan IDs for flight intentions
            System.out.println("  All flight intention plan IDs:");
            flightIntentionPlanIds.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.println("    Plan ID " + entry.getKey() + ": " + entry.getValue() + " intention(s)"));
        }
        
        System.out.println();
    }
    
    /**
     * Analyze indicative values for join strategy
     */
    public void analyzeIndicatives(ReplayData data) {
        System.out.println("=== INDICATIVE ANALYSIS ===");
        
        Set<String> flightIndicatives = new HashSet<>();
        Set<String> realPathIndicatives = new HashSet<>();
        
        if (data.getListFlightIntention() != null) {
            flightIndicatives = data.getListFlightIntention().stream()
                    .map(FlightIntention::getIndicative)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            System.out.println("Flight Intention Indicatives:");
            System.out.println("  Total unique indicatives: " + flightIndicatives.size());
            System.out.println("  Sample indicatives: " + flightIndicatives.stream().limit(10).collect(Collectors.toList()));
        }
        
        if (data.getListRealPath() != null) {
            realPathIndicatives = data.getListRealPath().stream()
                    .map(RealPathPoint::getIndicativeSafe)
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            
            System.out.println("\nReal Path Indicative Safe:");
            System.out.println("  Total unique indicatives: " + realPathIndicatives.size());
            System.out.println("  Sample indicatives: " + realPathIndicatives.stream().limit(10).collect(Collectors.toList()));
            
            // Count non-empty indicativeSafe
            long nonEmptyIndicatives = data.getListRealPath().stream()
                    .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().isEmpty())
                    .count();
            System.out.println("  Non-empty indicativeSafe count: " + nonEmptyIndicatives + " out of " + data.getListRealPath().size());
        }
        
        // Find common indicatives
        Set<String> commonIndicatives = new HashSet<>(flightIndicatives);
        commonIndicatives.retainAll(realPathIndicatives);
        
        System.out.println("\nCommon Indicatives (potential matches):");
        System.out.println("  Count: " + commonIndicatives.size());
        System.out.println("  Sample: " + commonIndicatives.stream().limit(10).collect(Collectors.toList()));
        
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
     * Recommend the best join strategy based on data analysis
     */
    public JoinStrategy recommendJoinStrategy(ReplayData data) {
        analyzePlanIds(data);
        analyzeIndicatives(data);
        
        // Analyze planId matching potential
        long realPathNonZeroPlanIds = data.getListRealPath() != null ? 
            data.getListRealPath().stream().filter(rp -> rp.getPlanId() != 0).count() : 0;
        
        long realPathWithIndicatives = data.getListRealPath() != null ? 
            data.getListRealPath().stream()
                .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().isEmpty())
                .count() : 0;
        
        System.out.println("=== JOIN STRATEGY RECOMMENDATION ===");
        System.out.println("  PlanId approach: " + realPathNonZeroPlanIds + " real path points with non-zero planId");
        System.out.println("  Indicative approach: " + realPathWithIndicatives + " real path points with indicatives");
        
        if (realPathNonZeroPlanIds > realPathWithIndicatives * 0.8) {
            System.out.println("  RECOMMENDATION: Use PLAN_ID strategy (high planId coverage)");
            return JoinStrategy.PLAN_ID;
        } else if (realPathWithIndicatives > 0) {
            System.out.println("  RECOMMENDATION: Use INDICATIVE strategy (good indicative coverage)");
            return JoinStrategy.INDICATIVE;
        } else {
            System.out.println("  RECOMMENDATION: No viable join strategy found");
            return JoinStrategy.NO_JOIN;
        }
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
     * Available join strategies
     */
    public enum JoinStrategy {
        PLAN_ID,
        INDICATIVE,
        HYBRID,
        NO_JOIN
    }
} 