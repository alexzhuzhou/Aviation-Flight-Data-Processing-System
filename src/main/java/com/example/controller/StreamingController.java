package com.example.controller;

import com.example.model.ReplayPath;
import com.example.model.JoinedFlightData;
import com.example.model.OracleProcessingResult;
import com.example.service.StreamingFlightService;
import com.example.service.OracleDataExtractionService;
import com.example.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * REST controller for streaming flight data processing
 */
@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*") // Allow cross-origin requests
public class StreamingController {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamingController.class);
    
    @Autowired
    private StreamingFlightService streamingService;
    
    @Autowired
    private OracleDataExtractionService oracleExtractionService;
    
    @Autowired
    private FlightRepository flightRepository;
    
    /**
     * NEW: Main endpoint for processing flight data directly from Oracle database
     * 
     * This endpoint replaces the HTTP POST approach with direct Oracle database access.
     * It extracts flight data from the Sigma Oracle database and processes it through
     * the existing StreamingFlightService, providing the same functionality as
     * PathVoGeneratorTest but as a REST endpoint.
     * 
     * Now supports optional date and time range parameters for flexible data extraction.
     * 
     * @param date Optional date parameter (format: YYYY-MM-DD). If not provided, uses hardcoded date (2025-07-11)
     * @param startTime Optional start time parameter (format: HH:mm). If provided, endTime must also be provided
     * @param endTime Optional end time parameter (format: HH:mm). If provided, startTime must also be provided
     */
    @PostMapping("/process-packet")
    public ResponseEntity<OracleProcessingResult> processPacketFromOracle(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        try {
            logger.info("Starting Oracle-based flight data processing...");
            
            // Validate time parameters
            if ((startTime != null && endTime == null) || (startTime == null && endTime != null)) {
                OracleProcessingResult errorResult = new OracleProcessingResult(
                    0, 0, 0, 0, 0, 
                    "Parameter Validation Failed", 
                    date != null ? date : "2025-07-11", 
                    "Both startTime and endTime must be provided together, or both should be omitted for full day processing"
                );
                return ResponseEntity.badRequest().body(errorResult);
            }
            
            // Test database connection first
            if (!oracleExtractionService.testDatabaseConnection()) {
                OracleProcessingResult errorResult = new OracleProcessingResult(
                    0, 0, 0, 0, 0, 
                    "Sigma Oracle Database (Connection Failed)", 
                    date != null ? date : "2025-07-11", 
                    "Failed to connect to Oracle database"
                );
                return ResponseEntity.status(503).body(errorResult);
            }
            
            // Extract and process data from Oracle with optional parameters
            OracleProcessingResult result;
            if (date != null || (startTime != null && endTime != null)) {
                // Use parameterized extraction
                result = oracleExtractionService.extractAndProcessFlightData(date, startTime, endTime);
            } else {
                // Use default extraction (existing behavior)
                result = oracleExtractionService.extractAndProcessFlightData();
            }
            
            // Return appropriate HTTP status based on results
            if (result.getPacketsWithErrors() > 0 && result.getTotalPacketsProcessed() == 0) {
                // All packets failed
                return ResponseEntity.status(500).body(result);
            } else if (result.getPacketsWithErrors() > 0) {
                // Some packets failed, but some succeeded
                return ResponseEntity.status(207).body(result); // 207 Multi-Status
            } else {
                // All successful
                return ResponseEntity.ok(result);
            }
            
        } catch (Exception e) {
            logger.error("Error during Oracle-based packet processing", e);
            OracleProcessingResult errorResult = new OracleProcessingResult(
                0, 0, 0, 0, 0,
                "Sigma Oracle Database (Error)", 
                "2025-07-11", 
                "Unexpected error: " + e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * LEGACY: Original endpoint for processing ReplayPath packets via HTTP POST
     * 
     * This endpoint is kept for backward compatibility and testing purposes.
     * External systems can still send ReplayPath data directly via HTTP.
     */
    @PostMapping("/process-packet-legacy")
    public ResponseEntity<StreamingFlightService.ProcessingResult> processPacketLegacy(
            @RequestBody ReplayPath replayPath) {
        
        try {
            logger.info("Received ReplayPath packet for legacy processing with timestamp: {}", 
                replayPath.getPacketStoredTimestamp());
            StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing ReplayPath packet via legacy endpoint", e);
            StreamingFlightService.ProcessingResult errorResult = 
                new StreamingFlightService.ProcessingResult(0, 0, "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * Test Oracle database connectivity
     */
    @GetMapping("/test-oracle-connection")
    public ResponseEntity<Map<String, Object>> testOracleConnection() {
        try {
            logger.info("Testing Oracle database connection via REST endpoint...");
            
            long startTime = System.currentTimeMillis();
            boolean connected = oracleExtractionService.testDatabaseConnection();
            long responseTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("connected", connected);
            response.put("responseTimeMs", responseTime);
            response.put("database", "Sigma Oracle Database");
            response.put("message", connected ? "Connection successful" : "Connection failed");
            
            return connected ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
            
        } catch (Exception e) {
            logger.error("Error testing Oracle connection", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("connected", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Connection test failed");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    

    
    /**
     * Get current flight statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<StreamingFlightService.FlightStats> getStats() {
        try {
            StreamingFlightService.FlightStats stats = streamingService.getStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting flight stats", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Analyze duplicate indicatives in the database
     * This helps identify potential data contamination issues
     */
    @GetMapping("/analyze-duplicates")
    public ResponseEntity<StreamingFlightService.DuplicateIndicativeAnalysis> analyzeDuplicateIndicatives() {
        try {
            StreamingFlightService.DuplicateIndicativeAnalysis analysis = streamingService.analyzeDuplicateIndicatives();
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error analyzing duplicate indicatives", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Streaming Flight Service is running");
    }
    

    
    /**
     * Clean up duplicate tracking points from all flights
     * This endpoint removes duplicate tracking points based on seqNum
     */
    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<String> cleanupDuplicates() {
        try {
            logger.info("Starting duplicate tracking points cleanup...");
            int removedCount = streamingService.cleanupDuplicateTrackingPoints();
            
            String message = String.format("Cleanup completed: %d duplicate tracking points removed", removedCount);
            logger.info(message);
            
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get all planIds from flights collection
     * Returns planIds with metadata for feeding into prediction scripts
     */
    @GetMapping("/plan-ids")
    public ResponseEntity<Map<String, Object>> getAllPlanIds() {
        try {
            logger.info("Retrieving all planIds from flights collection...");
            long startTime = System.currentTimeMillis();
            
            // Use projection query to efficiently get only planIds
            List<JoinedFlightData> projectionResults = flightRepository.findAllPlanIdsProjection();
            List<Long> planIds = projectionResults.stream()
                    .map(JoinedFlightData::getPlanId)
                    .collect(Collectors.toList());
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", planIds.size());
            response.put("planIds", planIds);
            response.put("processingTimeMs", processingTime);
            response.put("message", String.format("Retrieved %d planIds successfully", planIds.size()));
            
            logger.info("Retrieved {} planIds in {}ms", planIds.size(), processingTime);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving planIds", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve planIds: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    

} 