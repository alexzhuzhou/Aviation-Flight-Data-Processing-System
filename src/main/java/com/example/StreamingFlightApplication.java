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
        System.out.println(" API endpoints:");
        System.out.println("   POST /api/flights/process-packet - Process streaming data");
        System.out.println("   POST /api/flights/process-batch  - Process batch data (for testing)");
        System.out.println("   GET  /api/flights/stats         - Get flight statistics");
        System.out.println("   GET  /api/flights/health        - Health check");
    }
} 