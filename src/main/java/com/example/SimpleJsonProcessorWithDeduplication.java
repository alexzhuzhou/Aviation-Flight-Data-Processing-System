package com.example;

import com.example.model.*;
import com.example.service.FlightDataJoinService;
import com.example.service.ReplayDataService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON processor with consistent deduplication logic
 * Follows same approach as StreamingFlightService:
 * - FlightIntentions: deduplicate by planId only (skip planId = 0)
 * - RealPathPoints: no deduplication (deferred to future work)  
 * - Matching: indicative ↔ indicativeSafe
 */
public class SimpleJsonProcessorWithDeduplication {
    
    public static void main(String[] args) {
        String inputFile = "replay2.json";
        String outputFile = "joined_flights_consistent.json";
        
        try {
            System.out.println("🔄 Processing " + inputFile + " with consistent deduplication...");
            
            // 1. Load the replay data
            ReplayDataService dataService = new ReplayDataService();
            ReplayData replayData = dataService.loadReplayData(inputFile);
            
            System.out.println("✅ Loaded replay data:");
            System.out.println("   📋 Flight Intentions: " + 
                (replayData.getListFlightIntention() != null ? replayData.getListFlightIntention().size() : 0));
            System.out.println("   📍 Real Path Points: " + 
                (replayData.getListRealPath() != null ? replayData.getListRealPath().size() : 0));
            
            // 2. Apply consistent deduplication (same logic as StreamingFlightService)
            ReplayData processedData = deduplicateReplayData(replayData);
            
            System.out.println("🧹 After processing:");
            System.out.println("   📋 Flight Intentions: " + 
                (processedData.getListFlightIntention() != null ? processedData.getListFlightIntention().size() : 0));
            System.out.println("   📍 Real Path Points: " + 
                (processedData.getListRealPath() != null ? processedData.getListRealPath().size() : 0));
            
            // 3. Join the processed data (indicative ↔ indicativeSafe matching)
            FlightDataJoinService joinService = new FlightDataJoinService();
            List<JoinedFlightData> joinedFlights = joinService.joinFlightData(processedData);
            
            // 4. Show join results
            joinService.analyzeJoinResults(joinedFlights);
            
            // 5. Export to JSON
            joinService.exportToJson(joinedFlights, outputFile);
            
            System.out.println("🎉 Successfully generated: " + outputFile);
            System.out.println("📊 Total joined flights: " + joinedFlights.size());
            System.out.println("✅ Uses same deduplication logic as StreamingFlightService");
            
        } catch (Exception e) {
            System.err.println("❌ Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Deduplicate ReplayData following same logic as StreamingFlightService:
     * - FlightIntentions: deduplicate by planId only (skip planId = 0)  
     * - RealPathPoints: no deduplication (defer to future work)
     * - Matching: indicative ↔ indicativeSafe (handled by FlightDataJoinService)
     */
    private static ReplayData deduplicateReplayData(ReplayData originalData) {
        System.out.println("🧹 Deduplicating data (consistent with streaming service)...");
        
        // 1. Deduplicate FlightIntentions by planId ONLY (same as StreamingFlightService)
        List<FlightIntention> uniqueFlightIntentions = new ArrayList<>();
        Set<Long> seenPlanIds = new HashSet<>();
        
        if (originalData.getListFlightIntention() != null) {
            for (FlightIntention intention : originalData.getListFlightIntention()) {
                long planId = intention.getPlanId();
                
                // Skip planId = 0 (same as streaming service)
                if (planId == 0) {
                    System.out.println("   Skipping flight intention with planId = 0 (indicative: " + intention.getIndicative() + ")");
                    continue;
                }
                
                // Deduplicate by planId only
                if (!seenPlanIds.contains(planId)) {
                    seenPlanIds.add(planId);
                    uniqueFlightIntentions.add(intention);
                }
            }
            System.out.println("   FlightIntentions: " + originalData.getListFlightIntention().size() + 
                             " → " + uniqueFlightIntentions.size() + " (removed " + 
                             (originalData.getListFlightIntention().size() - uniqueFlightIntentions.size()) + " duplicates/planId=0)");
        }
        
        // 2. RealPathPoints: NO deduplication (same as streaming service approach)
        // Note: Streaming service appends all tracking points, so we preserve all here too
        List<RealPathPoint> allRealPathPoints = originalData.getListRealPath() != null ? 
            new ArrayList<>(originalData.getListRealPath()) : new ArrayList<>();
        
        System.out.println("   RealPathPoints: " + allRealPathPoints.size() + " preserved (no deduplication - deferred to future work)");
        
        return new ReplayData(allRealPathPoints, uniqueFlightIntentions, originalData.getTime());
    }
} 