package com.example.controller;

import com.example.service.PredictedFlightService;
import com.example.model.PredictedFlightData;
import com.example.model.BatchProcessingResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for predicted flight data processing
 */
@RestController
@RequestMapping("/api/predicted-flights")
@CrossOrigin(origins = "*") // Allow cross-origin requests
public class PredictedFlightController {
    
    private static final Logger logger = LoggerFactory.getLogger(PredictedFlightController.class);
    
    @Autowired
    private PredictedFlightService predictedFlightService;
    
    /**
     * Main endpoint for processing predicted flight data
     * This endpoint receives predicted flight information and stores it in the database
     * The data will later be used for comparison with actual flight tracking data
     */
    @PostMapping("/process")
    public ResponseEntity<PredictedFlightService.ProcessingResult> processPredictedFlight(
            @RequestBody JsonNode predictedFlightData) {
        
        try {
            logger.info("Received predicted flight data for processing");
            
            PredictedFlightService.ProcessingResult result = 
                predictedFlightService.processPredictedFlight(predictedFlightData);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            logger.error("Error processing predicted flight data", e);
            PredictedFlightService.ProcessingResult errorResult = 
                new PredictedFlightService.ProcessingResult(false, "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * Get predicted flight statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<PredictedFlightService.PredictedFlightStats> getStats() {
        try {
            PredictedFlightService.PredictedFlightStats stats = predictedFlightService.getStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting predicted flight stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check endpoint for predicted flights service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Predicted Flight Service is running");
    }
    
    /**
     * Batch processing endpoint for predicted flight data
     * Processes multiple predicted flights efficiently with optimal batch size handling
     * 
     * Expected payload: Array of PredictedFlightData objects
     * Features:
     * - Skip existing records (based on planId)
     * - Save what we can, report failures
     * - Optimal batch size handling (500 records per DB batch)
     * - Comprehensive error handling and reporting
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchProcessingResult> processPredictedFlightsBatch(
            @RequestBody List<PredictedFlightData> predictedFlights) {
        
        try {
            logger.info("Received batch of {} predicted flights for processing", 
                predictedFlights != null ? predictedFlights.size() : 0);
            long startTime = System.currentTimeMillis();
            
            // Process batch with optimal size handling
            BatchProcessingResult result = predictedFlightService.processBatch(predictedFlights);
            
            long totalTime = System.currentTimeMillis() - startTime;
            result.setProcessingTimeMs(totalTime);
            
            logger.info("Batch processing completed: {} received, {} processed, {} skipped, {} failed in {}ms",
                    result.getTotalReceived(), result.getTotalProcessed(), 
                    result.getTotalSkipped(), result.getTotalFailed(), totalTime);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing predicted flights batch", e);
            BatchProcessingResult errorResult = new BatchProcessingResult(
                    predictedFlights != null ? predictedFlights.size() : 0, 
                    0, 0, 0, 0L, "Error: " + e.getMessage(), null
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}