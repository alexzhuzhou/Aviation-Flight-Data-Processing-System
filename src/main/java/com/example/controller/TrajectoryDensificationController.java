package com.example.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.TrajectoryDensificationResult;
import com.example.service.TrajectoryDensificationService;
import com.example.repository.FlightRepository;

/**
 * REST controller for trajectory densification operations.
 * 
 * Provides endpoints to densify predicted flight trajectories to match
 * the tracking point density of corresponding real flights.
 */
@RestController
@RequestMapping("/api/trajectory-densification")
@CrossOrigin(origins = "*")
public class TrajectoryDensificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrajectoryDensificationController.class);
    
    @Autowired
    private TrajectoryDensificationService densificationService;
    
    @Autowired
    private FlightRepository flightRepository;
    
    /**
     * Densifies a single predicted flight trajectory.
     * 
     * @param planId The planId to process
     * @return TrajectoryDensificationResult with processing details
     */
    @PostMapping("/densify/{planId}")
    public ResponseEntity<TrajectoryDensificationResult> densifyTrajectory(@PathVariable Long planId) {
        logger.info("Received request to densify trajectory for planId: {}", planId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            TrajectoryDensificationResult result = densificationService.densifyPredictedTrajectory(planId);
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            logger.info("Trajectory densification completed for planId: {} in {}ms", 
                       planId, result.getProcessingTimeMs());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error densifying trajectory for planId: {}", planId, e);
            
            TrajectoryDensificationResult errorResult = TrajectoryDensificationResult.error(planId, e.getMessage());
            errorResult.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * Batch densification for multiple flights.
     * 
     * @param request Request containing list of planIds
     * @return Batch processing results
     */
    @PostMapping("/densify/batch")
    public ResponseEntity<Map<String, Object>> densifyMultipleTrajectories(@RequestBody Map<String, List<Long>> request) {
        List<Long> planIds = request.get("planIds");
        
        if (planIds == null || planIds.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "planIds list is required and cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        logger.info("Received batch densification request for {} planIds", planIds.size());
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<TrajectoryDensificationResult> results = densificationService.densifyMultipleTrajectories(planIds);
            
            // Calculate summary statistics
            long totalProcessingTime = System.currentTimeMillis() - startTime;
            int successCount = (int) results.stream().filter(TrajectoryDensificationResult::isSuccess).count();
            int errorCount = results.size() - successCount;
            
            // Calculate Sigma success rate statistics
            long highSigmaSuccessCount = results.stream()
                .filter(TrajectoryDensificationResult::isSuccess)
                .filter(r -> r.getSigmaSuccessRate() >= 90.0)
                .count();
            
            double highSigmaSuccessPercentage = successCount > 0 ? 
                (double) highSigmaSuccessCount / successCount * 100.0 : 0.0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalRequested", planIds.size());
            response.put("totalProcessed", results.size());
            response.put("successCount", successCount);
            response.put("errorCount", errorCount);
            response.put("highSigmaSuccessCount", highSigmaSuccessCount);
            response.put("highSigmaSuccessPercentage", Math.round(highSigmaSuccessPercentage * 10.0) / 10.0);
            response.put("results", results);
            response.put("processingTimeMs", totalProcessingTime);
            response.put("message", String.format("Batch densification completed: %d successful, %d errors out of %d requested. %d flights (%.1f%%) achieved ≥90%% Sigma success rate", 
                                                successCount, errorCount, planIds.size(), highSigmaSuccessCount, highSigmaSuccessPercentage));
            
            logger.info("Batch trajectory densification completed: {} successful, {} errors, {}ms total", 
                       successCount, errorCount, totalProcessingTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in batch trajectory densification", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Batch processing failed: " + e.getMessage());
            errorResponse.put("processingTimeMs", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Automatic synchronization: Densify all flights that have real flight data.
     * 
     * This endpoint:
     * 1. Gets all planIds from real flights in the database
     * 2. Automatically runs batch densification on all of them
     * 3. Returns comprehensive sync results
     * 
     * @return Auto-sync densification results
     */
    @PostMapping("/auto-sync")
    public ResponseEntity<Map<String, Object>> autoSyncDensification() {
        logger.info("Starting automatic trajectory densification sync for all flights");
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Get all planIds from real flights using existing repository
            List<Long> allPlanIds = flightRepository.findAllPlanIdsProjection()
                .stream()
                .map(flight -> flight.getPlanId())
                .collect(java.util.stream.Collectors.toList());
            
            if (allPlanIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("totalRequested", 0);
                response.put("totalProcessed", 0);
                response.put("totalErrors", 0);
                response.put("message", "No real flights found in database");
                response.put("processingTimeMs", System.currentTimeMillis() - startTime);
                return ResponseEntity.ok(response);
            }
            
            logger.info("Found {} real flights, starting batch densification", allPlanIds.size());
            
            // Step 2: Run batch densification using existing service
            List<TrajectoryDensificationResult> results = densificationService.densifyMultipleTrajectories(allPlanIds);
            
            // Step 3: Analyze results
            long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
            long errorCount = results.size() - successCount;
            long totalProcessingTime = System.currentTimeMillis() - startTime;
            
            // Calculate Sigma success rate statistics
            long highSigmaSuccessCount = results.stream()
                .filter(TrajectoryDensificationResult::isSuccess)
                .filter(r -> r.getSigmaSuccessRate() >= 90.0)
                .count();
            
            double highSigmaSuccessPercentage = successCount > 0 ? 
                (double) highSigmaSuccessCount / successCount * 100.0 : 0.0;
            
            // Step 4: Build comprehensive response
            Map<String, Object> response = new HashMap<>();
            response.put("totalRequested", allPlanIds.size());
            response.put("totalProcessed", successCount);
            response.put("totalErrors", errorCount);
            response.put("highSigmaSuccessCount", highSigmaSuccessCount);
            response.put("highSigmaSuccessPercentage", Math.round(highSigmaSuccessPercentage * 10.0) / 10.0);
            response.put("processingTimeMs", totalProcessingTime);
            response.put("results", results);
            
            // Add summary statistics
            Map<String, Object> summary = new HashMap<>();
            summary.put("successRate", String.format("%.1f%%", (successCount * 100.0) / allPlanIds.size()));
            summary.put("averageProcessingTimePerFlight", totalProcessingTime / allPlanIds.size() + "ms");
            summary.put("totalDensifiedElements", results.stream()
                .filter(TrajectoryDensificationResult::isSuccess)
                .mapToLong(TrajectoryDensificationResult::getFinalRouteElementCount)
                .sum());
            summary.put("highSigmaSuccessFlights", highSigmaSuccessCount + " out of " + successCount + " successful flights");
            response.put("summary", summary);
            
            response.put("message", String.format(
                "Auto-sync densification completed: %d successful, %d errors out of %d flights in %dms. %d flights (%.1f%%) achieved ≥90%% Sigma success rate", 
                successCount, errorCount, allPlanIds.size(), totalProcessingTime, highSigmaSuccessCount, highSigmaSuccessPercentage));
            
            logger.info("Auto-sync trajectory densification completed: {} successful, {} errors, {}ms total", 
                       successCount, errorCount, totalProcessingTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in auto-sync trajectory densification", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Auto-sync densification failed: " + e.getMessage());
            errorResponse.put("processingTimeMs", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Gets densification statistics for analysis.
     * 
     * @return Statistics about densification operations
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDensificationStats() {
        logger.info("Received request for densification statistics");
        
        try {
            // This would typically query a statistics service or database
            // For now, returning basic information
            Map<String, Object> stats = new HashMap<>();
            stats.put("serviceStatus", "ACTIVE");
            stats.put("simulationEngineAvailable", true);
            stats.put("supportedOperations", List.of("single-densification", "batch-densification"));
            stats.put("message", "Trajectory Densification Service is operational");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving densification statistics", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve statistics: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint.
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Trajectory Densification Service is running");
    }
    
    /**
     * Gets information about the densification process.
     * 
     * @return Information about how densification works
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "Trajectory Densification Service");
        info.put("description", "Densifies predicted flight trajectories using Sigma simulation engine");
        info.put("purpose", "Matches predicted flight route element density to real flight tracking point density for accurate trajectory analysis");
        info.put("algorithm", "Uses SimTrackSimulator for linear interpolation along route segments");
        info.put("inputData", List.of("Real flight tracking points", "Predicted flight route elements"));
        info.put("outputData", "Densified route elements matching real flight density");
        info.put("endpoints", Map.of(
            "POST /densify/{planId}", "Densify single flight trajectory",
            "POST /densify/batch", "Batch densify multiple flight trajectories",
            "POST /auto-sync", "Automatically densify all flights with real flight data",
            "GET /stats", "Get service statistics",
            "GET /health", "Health check",
            "GET /info", "Service information"
        ));
        
        return ResponseEntity.ok(info);
    }
}
