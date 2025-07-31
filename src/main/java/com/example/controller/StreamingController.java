package com.example.controller;

import com.example.model.ReplayPath;
import com.example.model.JoinedFlightData;
import com.example.service.StreamingFlightService;
import com.example.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private FlightRepository flightRepository;
    
    /**
     * Main endpoint for processing ReplayPath packets
     * This is what the external system will call
     */
    @PostMapping("/process-packet")
    public ResponseEntity<StreamingFlightService.ProcessingResult> processPacket(
            @RequestBody ReplayPath replayPath) {
        
        try {
            logger.info("Received ReplayPath packet for processing with timestamp: {}", 
                replayPath.getPacketStoredTimestamp());
            StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing ReplayPath packet", e);
            StreamingFlightService.ProcessingResult errorResult = 
                new StreamingFlightService.ProcessingResult(0, 0, "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
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
            return ResponseEntity.internalServerError().build();
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
            return ResponseEntity.internalServerError().build();
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
     * Batch processing endpoint for testing with existing JSON files
     */
    @PostMapping("/process-batch")
    public ResponseEntity<StreamingFlightService.ProcessingResult> processBatch(
            @RequestBody com.example.model.ReplayData replayData) {
        
        try {
            logger.info("Received batch ReplayData for processing");
            
            // Convert ReplayData to ReplayPath for processing
            ReplayPath replayPath = new ReplayPath(
                replayData.getListRealPath(),
                replayData.getListFlightIntention(), 
                replayData.getTime()
            );
            
            StreamingFlightService.ProcessingResult result = streamingService.processReplayPath(replayPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing batch data", e);
            StreamingFlightService.ProcessingResult errorResult = 
                new StreamingFlightService.ProcessingResult(0, 0, "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
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
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
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
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    

} 