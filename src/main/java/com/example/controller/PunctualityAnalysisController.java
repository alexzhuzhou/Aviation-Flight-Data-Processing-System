package com.example.controller;

import com.example.service.PunctualityAnalysisService;
import com.example.model.PunctualityAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ArrayList;

/**
 * REST controller for punctuality analysis operations
 * 
 * This controller provides endpoints for:
 * - Arrival Punctuality Analysis (ICAO KPI14)
 * - Comparing predicted vs actual flight times
 * - Getting analysis statistics and health checks
 */
@RestController
@RequestMapping("/api/punctuality-analysis")
@CrossOrigin(origins = "*") // Allow cross-origin requests
public class PunctualityAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(PunctualityAnalysisController.class);
    
    @Autowired
    private PunctualityAnalysisService punctualityAnalysisService;
    
    /**
     * Arrival Punctuality Analysis (ICAO KPI14)
     * Compares predicted en-route time with executed flight time
     * 
     * This endpoint analyzes the accuracy of flight time predictions by:
     * 1. Matching predicted flights (instanceId) with real flights (planId)
     * 2. Calculating predicted en-route time from route predictor data
     * 3. Calculating executed flight time from actual flight data
     * 4. Comparing times and categorizing by delay tolerance windows (±3, ±5, ±15 minutes)
     * 5. Providing KPI percentages for punctuality assessment
     */
    @GetMapping("/run")
    public ResponseEntity<PunctualityAnalysisResult> performPunctualityAnalysis() {
        try {
            logger.info("Starting arrival punctuality analysis (ICAO KPI14) via REST endpoint");
            
            PunctualityAnalysisResult result = punctualityAnalysisService.performPunctualityAnalysis();
            
            if (result.getTotalAnalyzedFlights() > 0) {
                logger.info("Punctuality analysis completed successfully: {} flights analyzed", 
                    result.getTotalAnalyzedFlights());
                return ResponseEntity.ok(result);
            } else {
                logger.warn("Punctuality analysis completed but no flights could be analyzed");
                return ResponseEntity.ok(result); // Return the result even if no flights analyzed
            }
            
        } catch (Exception e) {
            logger.error("Error performing punctuality analysis", e);
            PunctualityAnalysisResult errorResult = new PunctualityAnalysisResult(
                0, 0, new ArrayList<>(), 
                java.time.LocalDateTime.now().toString(), 
                "Error during punctuality analysis: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * Get punctuality analysis statistics summary
     * Returns information about available data for analysis
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAnalysisStatistics() {
        try {
            Map<String, Object> stats = punctualityAnalysisService.getAnalysisStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting punctuality analysis statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check endpoint for punctuality analysis service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Punctuality Analysis Service is running");
    }
}