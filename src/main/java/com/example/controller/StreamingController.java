package com.example.controller;

import com.example.model.ReplayPath;
import com.example.service.StreamingFlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    /**
     * Main endpoint for processing ReplayPath packets
     * This is what the external system will call
     */
    @PostMapping("/process-packet")
    public ResponseEntity<StreamingFlightService.ProcessingResult> processPacket(
            @RequestBody ReplayPath replayPath) {
        
        try {
            logger.info("Received ReplayPath packet for processing");
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
} 