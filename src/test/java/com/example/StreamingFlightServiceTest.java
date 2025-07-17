package com.example;

import com.example.model.*;
import com.example.service.StreamingFlightService;
import com.example.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class StreamingFlightServiceTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private StreamingFlightService streamingService;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @BeforeEach
    void setUp() {
        flightRepository.deleteAll();
    }
    
    @Test
    void testProcessNewFlightIntention() {
        // Create test flight intention
        FlightIntention intention = new FlightIntention();
        intention.setPlanId(12345);
        intention.setIndicative("TEST123");
        intention.setAircraftType("B737");
        intention.setAirline("TEST");
        
        // Create ReplayPath with flight intention
        ReplayPath replayPath = new ReplayPath();
        replayPath.setListFlightIntention(List.of(intention));
        replayPath.setTime(String.valueOf(System.currentTimeMillis()));
        
        // Process the packet
        StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
        
        // Verify results
        assertEquals(1, result.getNewFlights());
        assertEquals(0, result.getUpdatedFlights());
        
        // Verify flight was saved to database using planId
        Optional<JoinedFlightData> savedFlight = flightRepository.findByPlanId(12345);
        assertTrue(savedFlight.isPresent());
        assertEquals("B737", savedFlight.get().getAircraftType());
        assertEquals("TEST", savedFlight.get().getAirline());
        assertEquals("TEST123", savedFlight.get().getIndicative());
    }
    
    @Test
    void testProcessTrackingDataForExistingFlight() {
        // First, create a flight
        FlightIntention intention = new FlightIntention();
        intention.setPlanId(12345);
        intention.setIndicative("TEST123");
        intention.setAircraftType("B737");
        
        JoinedFlightData flight = new JoinedFlightData(intention);
        flightRepository.save(flight);
        
        // Create tracking data that links via indicativeSafe → indicative
        RealPathPoint trackingPoint = new RealPathPoint();
        trackingPoint.setPlanId(99999); // planId doesn't matter for linking
        trackingPoint.setIndicativeSafe("TEST123"); // This matches flight.indicative
        trackingPoint.setFlightLevel(350);
        trackingPoint.setSeqNum(1);
        
        // Create ReplayPath with tracking data
        ReplayPath replayPath = new ReplayPath();
        replayPath.setListRealPath(List.of(trackingPoint));
        replayPath.setTime(String.valueOf(System.currentTimeMillis()));
        
        // Process the packet
        StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
        
        // Verify results
        assertEquals(0, result.getNewFlights());
        assertEquals(1, result.getUpdatedFlights());
        
        // Verify tracking data was added via indicative matching
        Optional<JoinedFlightData> updatedFlight = flightRepository.findByPlanId(12345);
        assertTrue(updatedFlight.isPresent());
        assertEquals(1, updatedFlight.get().getTotalTrackingPoints());
        assertTrue(updatedFlight.get().isHasTrackingData());
    }
    
    @Test
    void testProcessTrackingDataWithoutMatchingFlight() {
        // Create tracking data for non-existent flight
        RealPathPoint trackingPoint = new RealPathPoint();
        trackingPoint.setIndicativeSafe("NONEXISTENT");
        trackingPoint.setFlightLevel(350);
        trackingPoint.setSeqNum(1);
        
        // Create ReplayPath with tracking data
        ReplayPath replayPath = new ReplayPath();
        replayPath.setListRealPath(List.of(trackingPoint));
        replayPath.setTime(String.valueOf(System.currentTimeMillis()));
        
        // Process the packet
        StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
        
        // Verify results - no flights should be updated
        assertEquals(0, result.getNewFlights());
        assertEquals(0, result.getUpdatedFlights());
    }
    

    
    @Test
    void testGetStats() {
        // Create some test data
        FlightIntention intention1 = new FlightIntention();
        intention1.setIndicative("FLIGHT1");
        JoinedFlightData flight1 = new JoinedFlightData(intention1);
        
        FlightIntention intention2 = new FlightIntention();
        intention2.setIndicative("FLIGHT2");
        JoinedFlightData flight2 = new JoinedFlightData(intention2);
        
        // Add tracking data to one flight
        TrackingPoint trackingPoint = new TrackingPoint();
        flight2.setTrackingPoints(List.of(trackingPoint));
        
        flightRepository.saveAll(List.of(flight1, flight2));
        
        // Get stats
        StreamingFlightService.FlightStats stats = streamingService.getStats();
        
        // Verify stats
        assertEquals(2, stats.getTotalFlights());
        assertEquals(1, stats.getFlightsWithTracking());
        assertEquals(1, stats.getTotalTrackingPoints());
    }
} 