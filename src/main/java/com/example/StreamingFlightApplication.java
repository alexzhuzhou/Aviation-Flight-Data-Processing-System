package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main Spring Boot application for Streaming Flight Data Processing
 */
@SpringBootApplication
@EnableMongoRepositories
public class StreamingFlightApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StreamingFlightApplication.class, args);
        System.out.println(" Streaming Flight Service is running!");
        System.out.println(" Main API endpoints:");
        System.out.println("   POST /api/flights/process-packet       - Process single ReplayPath packet");
        System.out.println("   GET  /api/flights/plan-ids             - Get all planIds for prediction scripts");
        System.out.println("   GET  /api/flights/stats                 - Get flight statistics");
        System.out.println("   GET  /api/flights/health                - Health check");
        System.out.println("   GET  /api/flights/analyze-duplicates    - Analyze duplicate indicatives");
        System.out.println("   POST /api/flights/cleanup-duplicates    - Clean up duplicate tracking points");
        System.out.println("   POST /api/predicted-flights/process     - Process single predicted flight");
        System.out.println("   POST /api/predicted-flights/batch       - Batch process predicted flights");
        System.out.println("   GET  /api/predicted-flights/stats       - Get predicted flight statistics");
        System.out.println("   GET  /api/predicted-flights/health      - Health check for predicted flights");
    }
} 