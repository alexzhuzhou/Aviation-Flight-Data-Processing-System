package com.example.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.OracleExtractionResponse;
import com.example.model.PlanIdRequest;
import com.example.service.OracleFlightDataService;
import com.example.service.PredictedFlightService;

/**
 * REST controller for predicted flight data processing with Oracle integration
 * 
 * This controller now extracts flight data directly from Oracle database
 * based on provided planIds, eliminating the need for external JSON input.
 */
@RestController
@RequestMapping("/api/predicted-flights")
@CrossOrigin(origins = "*") // Allow cross-origin requests
public class PredictedFlightController {
    
    private static final Logger logger = LoggerFactory.getLogger(PredictedFlightController.class);
    
    @Autowired
    private OracleFlightDataService oracleFlightDataService;
    
    @Autowired
    private PredictedFlightService predictedFlightService;
    
    /**
     * Process predicted flight data from Oracle database using planId
     * 
     * Request format:
     * {
     *   "planId": 17879345
     * }
     * 
     * This endpoint:
     * 1. Extracts flight data from Oracle database using the planId
     * 2. Processes it through the existing predicted flight service
     * 3. Returns detailed results with Option A error handling
     */
    @PostMapping("/process")
    public ResponseEntity<OracleExtractionResponse> processPredictedFlightFromOracle(
            @RequestBody PlanIdRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Processing predicted flight from Oracle for request: {}", request);
            
            // Validate request
            if (!request.isValid()) {
                OracleExtractionResponse errorResponse = new OracleExtractionResponse();
                errorResponse.setTotalRequested(0);
                errorResponse.setTotalProcessed(0);
                errorResponse.setTotalNotFound(0);
                errorResponse.setTotalErrors(1);
                errorResponse.setMessage("Invalid request: must provide either 'planId' or 'planIds'");
                errorResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Handle single planId request
            if (request.isSinglePlanId()) {
                return processSinglePlanId(request.getPlanId(), startTime);
            } else {
                // This shouldn't happen for /process endpoint, but handle gracefully
                return processBatchPlanIds(request.getPlanIds(), startTime);
            }
            
        } catch (Exception e) {
            logger.error("Error processing predicted flight from Oracle", e);
            
            OracleExtractionResponse errorResponse = new OracleExtractionResponse();
            errorResponse.setTotalRequested(request.getTotalPlanIds());
            errorResponse.setTotalProcessed(0);
            errorResponse.setTotalNotFound(0);
            errorResponse.setTotalErrors(request.getTotalPlanIds());
            errorResponse.setMessage("Processing error: " + e.getMessage());
            errorResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Batch process predicted flight data from Oracle database using planIds
     * 
     * Request format:
     * {
     *   "planIds": [17879345, 17879346, 17879347]
     * }
     * 
     * This endpoint:
     * 1. Extracts flight data from Oracle database for all planIds
     * 2. Processes them through the existing predicted flight service
     * 3. Returns detailed results with Option A error handling (skip missing, report all)
     */
    @PostMapping("/batch")
    public ResponseEntity<OracleExtractionResponse> processPredictedFlightsBatchFromOracle(
            @RequestBody PlanIdRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Processing predicted flights batch from Oracle for request: {}", request);
            
            // Validate request
            if (!request.isValid()) {
                OracleExtractionResponse errorResponse = new OracleExtractionResponse();
                errorResponse.setTotalRequested(0);
                errorResponse.setTotalProcessed(0);
                errorResponse.setTotalNotFound(0);
                errorResponse.setTotalErrors(1);
                errorResponse.setMessage("Invalid request: must provide either 'planId' or 'planIds'");
                errorResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Handle batch planIds request
            if (request.isBatchPlanIds()) {
                return processBatchPlanIds(request.getPlanIds(), startTime);
            } else {
                // Handle single planId as batch of 1
                return processBatchPlanIds(Arrays.asList(request.getPlanId()), startTime);
            }
            
        } catch (Exception e) {
            logger.error("Error processing predicted flights batch from Oracle", e);
            
            OracleExtractionResponse errorResponse = new OracleExtractionResponse();
            errorResponse.setTotalRequested(request.getTotalPlanIds());
            errorResponse.setTotalProcessed(0);
            errorResponse.setTotalNotFound(0);
            errorResponse.setTotalErrors(request.getTotalPlanIds());
            errorResponse.setMessage("Batch processing error: " + e.getMessage());
            errorResponse.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(500).body(errorResponse);
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
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Health check endpoint for predicted flights service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Predicted Flight Service with Oracle Integration is running");
    }
    
    /**
     * Process a single planId
     */
    private ResponseEntity<OracleExtractionResponse> processSinglePlanId(Long planId, long startTime) {
        long extractionStart = System.currentTimeMillis();
        
        // Extract flight data from Oracle
        Map<String, Object> flightData = oracleFlightDataService.extractFlightData(planId);
        long extractionTime = System.currentTimeMillis() - extractionStart;
        
        OracleExtractionResponse response = new OracleExtractionResponse();
        response.setTotalRequested(1);
        response.setExtractionTimeMs(extractionTime);
        
        if (flightData != null) {
            // Process the extracted flight data
            try {
                PredictedFlightService.ProcessingResult result = 
                    predictedFlightService.processPredictedFlightFromMap(flightData);
                
                if (result.isSuccess()) {
                    response.setTotalProcessed(1);
                    response.setTotalNotFound(0);
                    response.setTotalErrors(0);
                    response.setProcessedPlanIds(Arrays.asList(planId));
                    response.setNotFoundPlanIds(new ArrayList<>());
                    response.setErrorPlanIds(new ArrayList<>());
                } else {
                    response.setTotalProcessed(0);
                    response.setTotalNotFound(0);
                    response.setTotalErrors(1);
                    response.setProcessedPlanIds(new ArrayList<>());
                    response.setNotFoundPlanIds(new ArrayList<>());
                    response.setErrorPlanIds(Arrays.asList(planId));
                }
                
            } catch (Exception e) {
                logger.error("Error processing extracted flight data for planId {}: {}", planId, e.getMessage());
                response.setTotalProcessed(0);
                response.setTotalNotFound(0);
                response.setTotalErrors(1);
                response.setProcessedPlanIds(new ArrayList<>());
                response.setNotFoundPlanIds(new ArrayList<>());
                response.setErrorPlanIds(Arrays.asList(planId));
            }
        } else {
            // Flight not found in Oracle database
            response.setTotalProcessed(0);
            response.setTotalNotFound(1);
            response.setTotalErrors(0);
            response.setProcessedPlanIds(new ArrayList<>());
            response.setNotFoundPlanIds(Arrays.asList(planId));
            response.setErrorPlanIds(new ArrayList<>());
        }
        
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        response.generateMessage();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Process multiple planIds
     */
    private ResponseEntity<OracleExtractionResponse> processBatchPlanIds(List<Long> planIds, long startTime) {
        long extractionStart = System.currentTimeMillis();
        
        // Extract flight data from Oracle for all planIds
        OracleFlightDataService.OracleExtractionResult extractionResult = 
            oracleFlightDataService.extractFlightDataBatch(planIds);
        long extractionTime = System.currentTimeMillis() - extractionStart;
        
        OracleExtractionResponse response = new OracleExtractionResponse();
        response.setTotalRequested(planIds.size());
        response.setExtractionTimeMs(extractionTime);
        response.setNotFoundPlanIds(extractionResult.getNotFoundPlanIds());
        response.setTotalNotFound(extractionResult.getTotalNotFound());
        
        List<Long> processedPlanIds = new ArrayList<>();
        List<Long> errorPlanIds = new ArrayList<>(extractionResult.getErrorPlanIds());
        
        // Process each extracted flight
        for (Map<String, Object> flightData : extractionResult.getExtractedFlights()) {
            try {
                Long planId = (Long) flightData.get("instanceId");
                PredictedFlightService.ProcessingResult result = 
                    predictedFlightService.processPredictedFlightFromMap(flightData);
                
                if (result.isSuccess()) {
                    processedPlanIds.add(planId);
                } else {
                    errorPlanIds.add(planId);
                    logger.warn("Failed to process extracted flight data for planId {}: {}", planId, result.getMessage());
                }
                
            } catch (Exception e) {
                Long planId = (Long) flightData.get("instanceId");
                errorPlanIds.add(planId);
                logger.error("Error processing extracted flight data for planId {}: {}", planId, e.getMessage());
            }
        }
        
        response.setTotalProcessed(processedPlanIds.size());
        response.setTotalErrors(errorPlanIds.size());
        response.setProcessedPlanIds(processedPlanIds);
        response.setErrorPlanIds(errorPlanIds);
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        response.generateMessage();
        
        logger.info("Batch processing complete: {}/{} processed, {} not found, {} errors", 
                   processedPlanIds.size(), planIds.size(), 
                   extractionResult.getTotalNotFound(), errorPlanIds.size());
        
        return ResponseEntity.ok(response);
    }
}