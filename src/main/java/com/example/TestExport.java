package com.example;

import com.example.model.JoinedFlightData;
import com.example.model.ReplayData;
import com.example.service.FlightDataJoinService;
import com.example.service.ReplayDataService;

import java.util.List;

/**
 * Test class to demonstrate the export functionality
 */
public class TestExport {
    
    public static void main(String[] args) {
        try {
            // Load the replay data
            ReplayDataService dataService = new ReplayDataService();
            ReplayData replayData = dataService.loadReplayData("replay2.json");
            
            // Join the flight data
            FlightDataJoinService joinService = new FlightDataJoinService();
            List<JoinedFlightData> joinedFlights = joinService.joinFlightData(replayData);
            
            // Export to JSON
            String filename = "joined_flights_replay2_mongodb.json";
            joinService.exportToJson(joinedFlights, filename);
            
            System.out.println("Export completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 