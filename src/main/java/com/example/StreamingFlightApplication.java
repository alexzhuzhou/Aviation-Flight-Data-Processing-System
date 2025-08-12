package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import java.util.TimeZone;

/**
 * Main Spring Boot application for Streaming Flight Data Processing
 * 
 * Now includes Oracle database integration for direct data extraction
 * from the Sigma production database.
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration.class,
    org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration.class
})
@EnableMongoRepositories
@ComponentScan(basePackages = {"com.example", "br.atech.pista.dam"})
public class StreamingFlightApplication {
    
    public static void main(String[] args) {
        // FIXED: Force UTC timezone to avoid timezone-related timestamp issues
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("user.timezone", "UTC");
        
        SpringApplication.run(StreamingFlightApplication.class, args);
        System.out.println(" Streaming Flight Service is running!");
        System.out.println(" System timezone set to: " + TimeZone.getDefault().getID());
        System.out.println(" Main API endpoints:");
        System.out.println("   POST /api/flights/process-packet           - NEW: Process data directly from Oracle DB");
        System.out.println("   POST /api/flights/process-packet-legacy     - Legacy: Process single ReplayPath packet via HTTP");
        System.out.println("   GET  /api/flights/test-oracle-connection    - NEW: Test Oracle database connectivity");
        System.out.println("   GET  /api/flights/plan-ids                  - Get all planIds for prediction scripts");
        System.out.println("   GET  /api/flights/stats                     - Get flight statistics");
        System.out.println("   GET  /api/flights/health                    - Health check");
        System.out.println("   GET  /api/flights/analyze-duplicates        - Analyze duplicate indicatives");
        System.out.println("   POST /api/flights/cleanup-duplicates        - Clean up duplicate tracking points");
        System.out.println("   POST /api/predicted-flights/process         - Process single predicted flight");
        System.out.println("   POST /api/predicted-flights/batch           - Batch process predicted flights");
        System.out.println("   GET  /api/predicted-flights/stats           - Get predicted flight statistics");
        System.out.println("   GET  /api/predicted-flights/health          - Health check for predicted flights");
        System.out.println(" Oracle Integration: Direct connection to Sigma production database");
        System.out.println(" Processing Date: 2025-07-11 ");
    }
}