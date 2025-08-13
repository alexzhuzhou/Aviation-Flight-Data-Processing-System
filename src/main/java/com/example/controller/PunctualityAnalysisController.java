package com.example.controller;

import com.example.service.PunctualityAnalysisService;
import com.example.model.PunctualityAnalysisResult;
import com.example.model.PredictedFlightData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * REST controller for punctuality analysis operations
 * 
 * This controller provides endpoints for:
 * - Arrival Punctuality Analysis (ICAO KPI14)
 * - Finding qualifying flights (SBSP ↔ SBRJ routes)
 * - Extracting airport coordinates from route elements
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
     * Match predicted flights with real flights
     * Uses instanceId (predicted) ↔ planId (real) matching
     */
    @GetMapping("/match-flights")
    public ResponseEntity<Map<String, Object>> matchPredictedWithRealFlights() {
        try {
            logger.info("Matching predicted flights with real flights");
            
            List<Map<String, Object>> matchedFlights = punctualityAnalysisService.matchPredictedWithRealFlights();
            Map<String, Object> stats = punctualityAnalysisService.getFlightMatchingStatistics();
            
            // Add the matched flights to the response
            stats.put("matchedFlights", matchedFlights);
            
            logger.info("Flight matching completed with {} matched flights", matchedFlights.size());
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error matching predicted with real flights", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error matching flights: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Extract airport coordinates from qualifying flights
     * Returns flights with departure and arrival airport coordinates
     */
    @GetMapping("/airport-coordinates")
    public ResponseEntity<Map<String, Object>> extractAirportCoordinates() {
        try {
            logger.info("Extracting airport coordinates from qualifying flights");
            
            List<Map<String, Object>> flightsWithCoordinates = punctualityAnalysisService.extractAirportCoordinates();
            Map<String, Object> stats = punctualityAnalysisService.getQualifyingFlightsStatistics();
            
            // Add the flights with coordinates to the response
            stats.put("flightsWithCoordinates", flightsWithCoordinates);
            
            logger.info("Extracted coordinates for {} flights", flightsWithCoordinates.size());
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error extracting airport coordinates", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error extracting airport coordinates: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Step 1: Find qualifying flights (SBSP ↔ SBRJ with AERODROME endpoints)
     * Returns predicted flights that meet the specific route conditions
     */
    @GetMapping("/qualifying-flights")
    public ResponseEntity<Map<String, Object>> findQualifyingFlights() {
        try {
            logger.info("Finding qualifying flights (SBSP ↔ SBRJ routes)");
            
            List<PredictedFlightData> qualifyingFlights = punctualityAnalysisService.findQualifyingFlights();
            Map<String, Object> stats = punctualityAnalysisService.getQualifyingFlightsStatistics();
            
            // Add the qualifying flights list to the response
            stats.put("qualifyingFlights", qualifyingFlights);
            
            logger.info("Found {} qualifying flights", qualifyingFlights.size());
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error finding qualifying flights", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error finding qualifying flights: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
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
            return ResponseEntity.status(500).body(errorResult);
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
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Health check endpoint for punctuality analysis service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Punctuality Analysis Service is running");
    }

    /**
     * Apply geographic validation filter (2 NM threshold)
     * Filters flights where tracking points are within 2 NM of predicted aerodromes
     */
    @GetMapping("/geographic-validation")
    public ResponseEntity<Map<String, Object>> applyGeographicValidation() {
        try {
            logger.info("Applying geographic validation filter");
            
            List<Map<String, Object>> validatedFlights = punctualityAnalysisService.filterFlightsByGeographicValidation();
            Map<String, Object> stats = punctualityAnalysisService.getGeographicValidationStatistics();
            
            // Add sample validated flights to the response (limit to 5 for readability)
            List<Map<String, Object>> sampleFlights = validatedFlights.size() > 5 ? 
                validatedFlights.subList(0, 5) : validatedFlights;
            
            Map<String, Object> response = new HashMap<>(stats);
            response.put("sampleValidatedFlights", sampleFlights);
            
            logger.info("Geographic validation completed with {} validated flights", validatedFlights.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during geographic validation: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Geographic validation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Calculate punctuality KPIs (ICAO KPI14)
     * Compares predicted vs actual flight times and calculates percentages within tolerance windows
     * Tolerance windows: ±3 minutes, ±5 minutes, ±15 minutes
     */
    @GetMapping("/punctuality-kpis")
    public ResponseEntity<Map<String, Object>> calculatePunctualityKPIs() {
        try {
            logger.info("Calculating punctuality KPIs (ICAO KPI14)");
            
            Map<String, Object> kpiResults = punctualityAnalysisService.calculatePunctualityKPIs();
            
            if (kpiResults.containsKey("error")) {
                logger.error("Error calculating punctuality KPIs: {}", kpiResults.get("error"));
                return ResponseEntity.status(500).body(kpiResults);
            }
            
            // Add sample detailed results to the response (limit to 3 for readability)
            List<Map<String, Object>> detailedResults = (List<Map<String, Object>>) kpiResults.get("detailedResults");
            if (detailedResults != null && detailedResults.size() > 3) {
                kpiResults.put("sampleDetailedResults", detailedResults.subList(0, 3));
            } else {
                kpiResults.put("sampleDetailedResults", detailedResults);
            }
            
            logger.info("Punctuality KPIs calculated successfully: {} flights analyzed", kpiResults.get("totalAnalyzed"));
            
            return ResponseEntity.ok(kpiResults);
            
        } catch (Exception e) {
            logger.error("Error calculating punctuality KPIs: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error calculating punctuality KPIs: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
   
}