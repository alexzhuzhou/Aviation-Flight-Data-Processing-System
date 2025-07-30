package com.example.controller;

import com.example.service.PredictedFlightService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}