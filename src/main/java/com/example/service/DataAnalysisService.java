package com.example.service;

import com.example.model.FlightIntention;
import com.example.model.RealPathPoint;
import com.example.model.ReplayData;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing data patterns and join strategies
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
        
        // Find matches
        Set<String> matches = new HashSet<>(flightIndicatives);
        matches.retainAll(realPathIndicatives);
        
        System.out.println("\nMatching Indicatives:");
        System.out.println("  Matches found: " + matches.size());
        System.out.println("  Matching indicatives: " + matches);
        
        System.out.println();
    }
    
    /**
     * Recommend join strategy based on data analysis
     */
    public JoinStrategy recommendJoinStrategy(ReplayData data) {
        System.out.println("=== JOIN STRATEGY RECOMMENDATION ===");
        
        // Analyze both potential join keys
        analyzePlanIds(data);
        analyzeIndicatives(data);
        
        // Make recommendation
        if (data.getListRealPath() == null || data.getListFlightIntention() == null) {
            System.out.println("Recommendation: Cannot join - missing data");
            return JoinStrategy.NO_JOIN;
        }
        
        // Check planId effectiveness
        long realPathNonZeroPlanIds = data.getListRealPath().stream()
                .filter(rp -> rp.getPlanId() != 0)
                .count();
        
        long flightIntentionNonZeroPlanIds = data.getListFlightIntention().stream()
                .filter(fi -> fi.getPlanId() != 0)
                .count();
        
        // Check indicative effectiveness
        long realPathWithIndicatives = data.getListRealPath().stream()
                .filter(rp -> rp.getIndicativeSafe() != null && !rp.getIndicativeSafe().isEmpty())
                .count();
        
        Set<String> flightIndicatives = data.getListFlightIntention().stream()
                .map(FlightIntention::getIndicative)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<String> realPathIndicatives = data.getListRealPath().stream()
                .map(RealPathPoint::getIndicativeSafe)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        
        Set<String> indicativeMatches = new HashSet<>(flightIndicatives);
        indicativeMatches.retainAll(realPathIndicatives);
        
        System.out.println("Join Strategy Analysis:");
        System.out.println("  PlanId approach: " + realPathNonZeroPlanIds + " real path points with non-zero planId");
        System.out.println("  Indicative approach: " + realPathWithIndicatives + " real path points with indicatives");
        System.out.println("  Indicative matches: " + indicativeMatches.size() + " common indicatives");
        
        if (indicativeMatches.size() > 0 && realPathWithIndicatives > realPathNonZeroPlanIds) {
            System.out.println("Recommendation: Use INDICATIVE join strategy");
            return JoinStrategy.INDICATIVE;
        } else if (realPathNonZeroPlanIds > 0) {
            System.out.println("Recommendation: Use PLAN_ID join strategy");
            return JoinStrategy.PLAN_ID;
        } else {
            System.out.println("Recommendation: Use HYBRID join strategy (try both)");
            return JoinStrategy.HYBRID;
        }
    }
    
    public enum JoinStrategy {
        PLAN_ID,
        INDICATIVE,
        HYBRID,
        NO_JOIN
    }
} 