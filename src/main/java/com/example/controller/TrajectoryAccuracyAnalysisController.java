package com.example.controller;

import com.example.model.TrajectoryAccuracyResult;
import com.example.service.TrajectoryAccuracyAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for trajectory accuracy analysis endpoints.
 * 
 * Provides APIs for analyzing the accuracy of predicted flight trajectories
 * compared to actual flight tracking points using MSE and RMSE metrics.
 */
@RestController
@RequestMapping("/api/trajectory-accuracy")
@CrossOrigin(origins = "*")
public class TrajectoryAccuracyAnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrajectoryAccuracyAnalysisController.class);
    
    @Autowired
    private TrajectoryAccuracyAnalysisService trajectoryAccuracyService;
    
    /**
     * Run complete trajectory accuracy analysis
     * 
     * Analyzes the accuracy of predicted flight trajectories by comparing them
     * point-by-point with actual flight tracking data using MSE and RMSE metrics.
     * 
     * Process:
     * 1. Filters flights for SBSP ↔ SBRJ routes
     * 2. Matches predicted and real flights by planId
     * 3. Only processes flights with equal point counts (after densification)
     * 4. Calculates horizontal and vertical MSE/RMSE for each flight
     * 5. Provides both per-flight and aggregate statistics
     * 
     * @return TrajectoryAccuracyResult with detailed accuracy metrics
     */
    @GetMapping("/run")
    public ResponseEntity<TrajectoryAccuracyResult> runTrajectoryAccuracyAnalysis() {
        logger.info("Received request for trajectory accuracy analysis");
        
        try {
            TrajectoryAccuracyResult result = trajectoryAccuracyService.runTrajectoryAccuracyAnalysis();
            
            logger.info("Trajectory accuracy analysis completed: {} flights analyzed, {} skipped", 
                       result.getTotalAnalyzedFlights(), result.getTotalSkippedFlights());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error during trajectory accuracy analysis", e);
            
            TrajectoryAccuracyResult errorResult = new TrajectoryAccuracyResult();
            errorResult.setMessage("Analysis failed: " + e.getMessage());
            errorResult.setTotalAnalyzedFlights(0);
            errorResult.setTotalSkippedFlights(0);
            
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * Get statistics about available data for trajectory accuracy analysis
     * 
     * Provides information about:
     * - Total predicted and real flights in database
     * - Number of qualified flights (SBSP ↔ SBRJ routes)
     * - Potential matches between predicted and real flights
     * - Analysis capability status
     * 
     * @return Map containing statistics and capability information
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTrajectoryAccuracyStats() {
        logger.info("Received request for trajectory accuracy statistics");
        
        try {
            Map<String, Object> stats = trajectoryAccuracyService.getTrajectoryAccuracyStats();
            
            logger.info("Retrieved trajectory accuracy stats: {} potential matches", 
                       stats.get("potentialMatches"));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting trajectory accuracy stats", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Health check endpoint for trajectory accuracy analysis service
     * 
     * @return Simple health status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Health check requested for trajectory accuracy analysis service");
        return ResponseEntity.ok("Trajectory Accuracy Analysis Service is running");
    }
    
    /**
     * Get detailed information about the trajectory accuracy analysis process
     * 
     * @return Information about the analysis methodology and requirements
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAnalysisInfo() {
        logger.info("Received request for trajectory accuracy analysis information");
        
        Map<String, Object> info = Map.of(
            "analysisType", "Point-by-Point Trajectory Accuracy Analysis",
            "metrics", Map.of(
                "horizontal", "MSE and RMSE for latitude/longitude coordinates",
                "vertical", "MSE and RMSE for altitude/flight level"
            ),
            "requirements", Map.of(
                "routeFilter", "SBSP ↔ SBRJ routes only",
                "matching", "planId-based matching between predicted and real flights",
                "pointCounts", "Equal number of route elements and tracking points required",
                "densification", "Trajectory densification should be completed beforehand"
            ),
            "unitConversions", Map.of(
                "coordinates", "Predicted (degrees) → Real (radians)",
                "altitude", "Predicted (meters/levelmeters) → Real (flight levels × 30.48m)"
            ),
            "matchingStrategy", "Sequential order (1st predicted point vs 1st real point, etc.)",
            "altitudeHandling", Map.of(
                "interpolated_true", "Use levelmeters field",
                "interpolated_false", "Use altitude field"
            )
        );
        
        return ResponseEntity.ok(info);
    }
}
