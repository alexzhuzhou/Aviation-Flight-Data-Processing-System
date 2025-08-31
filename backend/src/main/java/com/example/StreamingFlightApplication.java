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
    org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration.class,
    org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration.class,
    // Additional JPA-related exclusions to prevent jpaContext bean creation
    org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration.class
})
@EnableMongoRepositories
@ComponentScan(basePackages = {"com.example", "br.atech.pista.dam"})
public class StreamingFlightApplication {
    
    public static void main(String[] args) {
        // FIXED: Force UTC timezone to avoid timezone-related timestamp issues
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("user.timezone", "UTC");
        
        try {
            System.out.println(" Starting Aviation Flight Data Processing System...");
            SpringApplication app = new SpringApplication(StreamingFlightApplication.class);
            
            // Add shutdown hook to debug what's causing the shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("  Application is shutting down...");
                System.out.println("  Check logs above for any errors that might have caused this shutdown");
            }));
            
            var context = app.run(args);
            
            System.out.println(" Application context started successfully!");
            System.out.println(" Context active: " + context.isActive());
            System.out.println(" Context running: " + context.isRunning());
            
        } catch (Exception e) {
            System.err.println(" Application failed to start:");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(" Aviation Flight Data Processing System is running!");
        System.out.println(" System timezone set to: " + TimeZone.getDefault().getID());
        System.out.println("");
        System.out.println(" Main API endpoints:");
        System.out.println("     Oracle Integration:");
        System.out.println("   POST /api/flights/process-packet              - Process data directly from Oracle DB");
        System.out.println("   GET  /api/flights/test-oracle-connection      - Test Oracle database connectivity");
        System.out.println("   GET  /api/flights/plan-ids                    - Get all planIds for predictions");
        System.out.println("");
        System.out.println("    Predicted Flights (Oracle-based):");
        System.out.println("   POST /api/predicted-flights/process           - Process single planId from Oracle");
        System.out.println("   POST /api/predicted-flights/batch             - Batch process multiple planIds");
        System.out.println("   GET  /api/predicted-flights/stats             - Get predicted flight statistics");
        System.out.println("");
        System.out.println("    Punctuality Analysis (ICAO KPI14):");
        System.out.println("   GET  /api/punctuality-analysis/match-flights  - Match predicted with real flights");
        System.out.println("   GET  /api/punctuality-analysis/punctuality-kpis - Calculate punctuality KPIs");
        System.out.println("   GET  /api/punctuality-analysis/stats          - Get analysis statistics");
        System.out.println("");
        System.out.println("    Trajectory Accuracy Analysis (MSE/RMSE):");
        System.out.println("   GET  /api/trajectory-accuracy/run             - Run trajectory accuracy analysis");
        System.out.println("   GET  /api/trajectory-accuracy/stats           - Get trajectory accuracy statistics");
        System.out.println("   GET  /api/trajectory-accuracy/info            - Get analysis methodology info");
        System.out.println("");
        System.out.println("    Statistics & Health:");
        System.out.println("   GET  /api/flights/stats                       - Get flight statistics");
        System.out.println("   GET  /api/flights/health                      - Health check");
        System.out.println("   GET  /api/predicted-flights/health            - Predicted flights health");
        System.out.println("   GET  /api/punctuality-analysis/health         - Analysis health check");
        System.out.println("   GET  /api/trajectory-accuracy/health          - Trajectory accuracy health");
        System.out.println("");
        System.out.println("    Legacy & Utilities:");
        System.out.println("   POST /api/flights/process-packet-legacy       - Legacy JSON packet processing");
        System.out.println("   GET  /api/flights/analyze-duplicates          - Analyze duplicate indicatives");
        System.out.println("   POST /api/flights/cleanup-duplicates          - Clean up duplicate tracking points");
        System.out.println("");
        System.out.println(" Oracle Integration: Direct connection to Sigma production database");
        System.out.println(" Processing Date: 2025-07-11");
        System.out.println(" Complete API Guide: See API_USAGE_GUIDE.md for detailed examples");
    }
}