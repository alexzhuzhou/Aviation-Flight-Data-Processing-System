package com.example;

import com.example.model.JoinedFlightData;
import com.example.model.ReplayData;
import com.example.service.FlightDataJoinService;
import com.example.service.ReplayDataService;

import java.util.List;

/**
 * Simple utility to process replay2.json and generate joined flights JSON
 * Uses traditional in-memory join logic (not streaming)
 */
public class SimpleJsonProcessor {
    
    public static void main(String[] args) {
        String inputFile = "replay2.json";  // Input file
        String outputFile = "joined_flights_output.json"; // Output file
        
        try {
            System.out.println("🔄 Processing " + inputFile + "...");
            
            // 1. Load the replay data
            ReplayDataService dataService = new ReplayDataService();
            ReplayData replayData = dataService.loadReplayData(inputFile);
            
            System.out.println("✅ Loaded replay data:");
            System.out.println("   📋 Flight Intentions: " + 
                (replayData.getListFlightIntention() != null ? replayData.getListFlightIntention().size() : 0));
            System.out.println("   📍 Real Path Points: " + 
                (replayData.getListRealPath() != null ? replayData.getListRealPath().size() : 0));
            
            // 2. Join the flight data using traditional logic
            FlightDataJoinService joinService = new FlightDataJoinService();
            List<JoinedFlightData> joinedFlights = joinService.joinFlightData(replayData);
            
            // 3. Show join results
            joinService.analyzeJoinResults(joinedFlights);
            
            // 4. Export to JSON
            joinService.exportToJson(joinedFlights, outputFile);
            
            System.out.println("🎉 Successfully generated: " + outputFile);
            System.out.println("📊 Total joined flights: " + joinedFlights.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 