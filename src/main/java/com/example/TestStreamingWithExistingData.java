package com.example;

import com.example.model.ReplayData;
import com.example.service.ReplayDataService;
import com.example.service.StreamingFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Test class to demonstrate streaming functionality with existing JSON data
 * This shows how the system works before integrating with the external codebase
 */
@Component
public class TestStreamingWithExistingData implements CommandLineRunner {
    
    @Autowired
    private StreamingFlightService streamingService;
    
    @Autowired
    private ReplayDataService replayDataService;
    
    @Override
    public void run(String... args) throws Exception {
        // Only run if "test-streaming" argument is provided
        if (args.length > 0 && "test-streaming".equals(args[0])) {
            System.out.println("🧪 Testing streaming functionality with existing data...");
            testStreamingWithExistingJson();
        }
    }
    
    private void testStreamingWithExistingJson() {
        try {
            // Try to load existing JSON file
            String[] testFiles = {"replay2.json", "replay.json"};
            
            for (String filename : testFiles) {
                try {
                    System.out.println("📁 Loading test data from: " + filename);
                    ReplayData replayData = replayDataService.loadReplayData(filename);
                    
                    // Create ReplayPath from ReplayData for streaming simulation
                    com.example.model.ReplayPath replayPath = new com.example.model.ReplayPath(
                        replayData.getListRealPath(),
                        replayData.getListFlightIntention(),
                        replayData.getTime()
                    );
                    
                    // Process as if it came from streaming
                    System.out.println("⚡ Processing data through streaming service...");
                    StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
                    
                    System.out.println("✅ Processing completed:");
                    System.out.println("   📥 New flights: " + result.getNewFlights());
                    System.out.println("   🔄 Updated flights: " + result.getUpdatedFlights());
                    System.out.println("   💬 Message: " + result.getMessage());
                    
                    // Get statistics
                    StreamingFlightService.FlightStats stats = streamingService.getStats();
                    System.out.println("📊 Database statistics:");
                    System.out.println("   🛫 Total flights: " + stats.getTotalFlights());
                    System.out.println("   📍 Flights with tracking: " + stats.getFlightsWithTracking());
                    System.out.println("   📋 Total tracking points: " + stats.getTotalTrackingPoints());
                    
                    break; // Success, exit loop
                    
                } catch (Exception e) {
                    System.out.println("❌ Could not load " + filename + ": " + e.getMessage());
                    if (filename.equals(testFiles[testFiles.length - 1])) {
                        System.out.println("💡 To test streaming functionality:");
                        System.out.println("   1. Make sure you have replay.json or replay2.json in the project root");
                        System.out.println("   2. Start the application with: mvn spring-boot:run -Dspring-boot.run.arguments=test-streaming");
                        System.out.println("   3. Or test using the REST API endpoints");
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during streaming test: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 