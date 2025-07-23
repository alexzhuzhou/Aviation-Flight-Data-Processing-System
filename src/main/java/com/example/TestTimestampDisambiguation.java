package com.example;

import com.example.model.*;
import com.example.service.StreamingFlightService;
import com.example.repository.FlightRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;

/**
 * Test class to demonstrate timestamp-based disambiguation
 * Shows how tracking points are assigned to correct flights based on timestamps
 */
@SpringBootApplication
public class TestTimestampDisambiguation {
    
    public static void main(String[] args) {
        System.out.println("🕒 Testing Timestamp-Based Disambiguation");
        System.out.println("=========================================");
        
        ConfigurableApplicationContext context = SpringApplication.run(TestTimestampDisambiguation.class, args);
        
        try {
            FlightRepository flightRepository = context.getBean(FlightRepository.class);
            StreamingFlightService streamingService = context.getBean(StreamingFlightService.class);
            
            // Clear any existing data
            flightRepository.deleteAll();
            System.out.println("🧹 Cleared existing flight data");
            
            // Create two flights with the same indicative but different time windows
            createTestFlights(flightRepository);
            
            // Create tracking points with different timestamps
            testTrackingPointAssignment(streamingService);
            
            // Analyze results
            analyzeResults(flightRepository);
            
        } catch (Exception e) {
            System.err.println("❌ Error during test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
    
    private static void createTestFlights(FlightRepository flightRepository) {
        System.out.println("\n📋 Creating test flights with same indicative 'TAM3886'...");
        
        // Flight 1: Earlier time window
        FlightIntention flight1Intention = new FlightIntention();
        flight1Intention.setPlanId(1001);
        flight1Intention.setIndicative("TAM3886");
        flight1Intention.setFlightPlanDate("2025-07-10T20:00:00.000+0000"); // 8 PM
        flight1Intention.setCurrentDateTimeOfArrival("2025-07-10T22:00:00.000+0000"); // 10 PM
        flight1Intention.setAircraftType("A320");
        
        JoinedFlightData flight1 = new JoinedFlightData(flight1Intention);
        flight1.setLastPacketTimestamp("1752185280000"); // Some packet timestamp
        flightRepository.save(flight1);
        System.out.println("  ✅ Flight 1: planId=1001, TAM3886, 20:00-22:00");
        
        // Flight 2: Later time window  
        FlightIntention flight2Intention = new FlightIntention();
        flight2Intention.setPlanId(1002);
        flight2Intention.setIndicative("TAM3886");
        flight2Intention.setFlightPlanDate("2025-07-11T22:00:00.000+0000"); // 10 PM next day
        flight2Intention.setCurrentDateTimeOfArrival("2025-07-12T00:30:00.000+0000"); // 12:30 AM day after
        flight2Intention.setAircraftType("A321");
        
        JoinedFlightData flight2 = new JoinedFlightData(flight2Intention);
        flight2.setLastPacketTimestamp("1752278400000"); // Different packet timestamp
        flightRepository.save(flight2);
        System.out.println("  ✅ Flight 2: planId=1002, TAM3886, 22:00-00:30+1");
    }
    
    private static void testTrackingPointAssignment(StreamingFlightService streamingService) {
        System.out.println("\n📍 Testing tracking point assignment...");
        
        // Create replay data with tracking points for different time periods
        ReplayData testData = new ReplayData();
        
        // Tracking point for Flight 1 time window (around 21:00) - July 10, 2025
        RealPathPoint point1 = createTestTrackingPoint("TAM3886", 1752188400000L); // ~21:00
        
        // Tracking point for Flight 2 time window (around 23:00 next day) - July 11, 2025
        RealPathPoint point2 = createTestTrackingPoint("TAM3886", 1752278400000L + 3600000L); // ~23:00
        
        // Tracking point outside both windows (should use tolerance) - before Flight 1
        RealPathPoint point3 = createTestTrackingPoint("TAM3886", 1752185280000L - 1800000L); // 30 min before flight 1
        
        testData.setListRealPath(Arrays.asList(point1, point2, point3));
        
        // Process the tracking points using different packet timestamps
        // This simulates receiving different packets at different times
        System.out.println("  📊 Processing point 1 with packet timestamp: 1752188400000");
        processPointsWithTimestamp(streamingService, Arrays.asList(point1), "1752188400000");
        
        System.out.println("  📊 Processing point 2 with packet timestamp: " + (1752278400000L + 3600000L));
        processPointsWithTimestamp(streamingService, Arrays.asList(point2), String.valueOf(1752278400000L + 3600000L));
        
        System.out.println("  📊 Processing point 3 with packet timestamp: " + (1752185280000L - 1800000L));
        processPointsWithTimestamp(streamingService, Arrays.asList(point3), String.valueOf(1752185280000L - 1800000L));
        
        System.out.println("  ✅ Processed all tracking points with respective timestamps");
    }
    
    private static void processPointsWithTimestamp(StreamingFlightService streamingService, List<RealPathPoint> points, String packetTimestamp) {
        ReplayData testData = new ReplayData();
        testData.setListRealPath(points);
        
        // Convert ReplayData to ReplayPath for processing
        ReplayPath replayPath = new ReplayPath(
            testData.getListRealPath(),
            testData.getListFlightIntention(),
            testData.getTime(),
            packetTimestamp
        );
        
        streamingService.processReplayPath(replayPath);
    }
    
    private static RealPathPoint createTestTrackingPoint(String indicative, long timestamp) {
        RealPathPoint point = new RealPathPoint();
        point.setIndicativeSafe(indicative);
        point.setFlightLevel(350);
        // Note: RealPathPoint doesn't have setSpeed method, speed is in kinematic
        
        // Create kinematic data with position
        Kinematic kinematic = new Kinematic();
        Kinematic.Position position = new Kinematic.Position();
        position.setLatitude(-23.5505);
        position.setLongitude(-46.6333);
        kinematic.setPosition(position);
        kinematic.setSpeed(450);
        kinematic.setDetectorSource("SIMULADO");
        point.setKinematic(kinematic);
        
        return point;
    }
    
    private static void analyzeResults(FlightRepository flightRepository) {
        System.out.println("\n📈 Analysis Results:");
        System.out.println("==================");
        
        List<JoinedFlightData> allFlights = flightRepository.findAll();
        
        for (JoinedFlightData flight : allFlights) {
            System.out.println(String.format("Flight planId=%d, indicative=%s:", 
                flight.getPlanId(), flight.getIndicative()));
            System.out.println(String.format("  Time window: %s to %s", 
                flight.getFlightPlanDate(), flight.getCurrentDateTimeOfArrival()));
            System.out.println(String.format("  Tracking points: %d", 
                flight.getTotalTrackingPoints()));
            
            if (flight.getTrackingPoints() != null) {
                for (TrackingPoint tp : flight.getTrackingPoints()) {
                    System.out.println(String.format("    - Timestamp: %d (%.2f,%.2f)", 
                        tp.getTimestamp(), tp.getLatitude(), tp.getLongitude()));
                }
            }
            System.out.println();
        }
        
        System.out.println("🎯 Disambiguation Summary:");
        System.out.println("- Tracking points should be assigned to flights with matching time windows");
        System.out.println("- Points near flight 1 window (21:00) → Flight 1001"); 
        System.out.println("- Points near flight 2 window (23:00) → Flight 1002");
        System.out.println("- Points with tolerance should be assigned to closest flight");
    }
} 