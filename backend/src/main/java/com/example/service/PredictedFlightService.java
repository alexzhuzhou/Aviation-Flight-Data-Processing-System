package com.example.service;

import com.example.model.PredictedFlightData;
import com.example.model.BatchProcessingResult;
import com.example.repository.PredictedFlightRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for processing predicted flight data
 * 
 * Key responsibilities:
 * - Processing incoming predicted flight JSON data
 * - Mapping JSON 'id' field to model 'planId' field for future comparison
 * - Storing predicted flight data in MongoDB
 * - Providing query methods for predicted flight data
 * - Supporting future comparison with actual flight data
 */
@Service
public class PredictedFlightService {
    
    private static final Logger logger = LoggerFactory.getLogger(PredictedFlightService.class);
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Process predicted flight data from JSON and store in database
     * Uses instanceId for future matching with actual flights' planId
     */
    public ProcessingResult processPredictedFlight(JsonNode jsonData) {
        try {
            logger.info("Processing predicted flight data for indicative: {}", 
                jsonData.get("indicative").asText());
            
            // Convert JSON to PredictedFlightData object
            PredictedFlightData predictedFlight = objectMapper.treeToValue(jsonData, PredictedFlightData.class);
            
            return processPredictedFlightInternal(predictedFlight);
            
        } catch (Exception e) {
            logger.error("Error processing predicted flight data from JSON", e);
            return new ProcessingResult(false, "Error processing predicted flight: " + e.getMessage());
        }
    }
    
    /**
     * Process predicted flight data from Map (extracted from Oracle) and store in database
     * Uses instanceId for future matching with actual flights' planId
     */
    public ProcessingResult processPredictedFlightFromMap(Map<String, Object> flightData) {
        try {
            logger.info("Processing predicted flight data from Oracle for indicative: {}", 
                flightData.get("indicative"));
            
            // Convert Map to PredictedFlightData object
            PredictedFlightData predictedFlight = objectMapper.convertValue(flightData, PredictedFlightData.class);
            
            return processPredictedFlightInternal(predictedFlight);
            
        } catch (Exception e) {
            logger.error("Error processing predicted flight data from Map", e);
            return new ProcessingResult(false, "Error processing predicted flight: " + e.getMessage());
        }
    }
    
    /**
     * Internal method to process PredictedFlightData object
     */
    private ProcessingResult processPredictedFlightInternal(PredictedFlightData predictedFlight) {
        try {
            // Validate that instanceId exists
            if (predictedFlight.getInstanceId() == 0) {
                return new ProcessingResult(false, "Missing instanceId for predicted flight: " + predictedFlight.getIndicative());
            }
            
            // Check if predicted flight already exists
            if (predictedFlightRepository.existsByInstanceId(predictedFlight.getInstanceId())) {
                logger.warn("Predicted flight with instanceId {} already exists, updating...", 
                    predictedFlight.getInstanceId());
                
                // Update existing record
                Optional<PredictedFlightData> existingOpt = 
                    predictedFlightRepository.findByInstanceId(predictedFlight.getInstanceId());
                
                if (existingOpt.isPresent()) {
                    PredictedFlightData existing = existingOpt.get();
                    predictedFlight.setId(existing.getId()); // Keep the same MongoDB ID
                }
            }
            
            // Save to database
            PredictedFlightData saved = predictedFlightRepository.save(predictedFlight);
            
            logger.info("Successfully stored predicted flight: instanceId={}, indicative={}, routeElements={}, routeSegments={}", 
                saved.getInstanceId(), 
                saved.getIndicative(),
                saved.getRouteElements() != null ? saved.getRouteElements().size() : 0,
                saved.getRouteSegments() != null ? saved.getRouteSegments().size() : 0);
            
            return new ProcessingResult(true, 
                "Successfully processed predicted flight: " + saved.getIndicative() + 
                " (instanceId: " + saved.getInstanceId() + ")");
            
        } catch (Exception e) {
            logger.error("Error processing predicted flight data internally", e);
            return new ProcessingResult(false, "Error processing predicted flight: " + e.getMessage());
        }
    }
    
    /**
     * Get predicted flight statistics
     */
    public PredictedFlightStats getStats() {
        try {
            long totalCount = predictedFlightRepository.count();
            logger.debug("Retrieved predicted flight stats: total count = {}", totalCount);
            
            return new PredictedFlightStats(totalCount);
            
        } catch (Exception e) {
            logger.error("Error getting predicted flight stats", e);
            throw new RuntimeException("Error retrieving predicted flight statistics", e);
        }
    }
    
    /**
     * Find predicted flight by instanceId (for comparison with actual flights via planId)
     */
    public Optional<PredictedFlightData> findByInstanceId(long instanceId) {
        return predictedFlightRepository.findByInstanceId(instanceId);
    }
    
    /**
     * Check if predicted flight exists for given instanceId
     */
    public boolean existsByInstanceId(long instanceId) {
        return predictedFlightRepository.existsByInstanceId(instanceId);
    }
    
    /**
     * Process multiple predicted flights in batch with optimal performance
     * Handles large batches efficiently with proper error handling and skip logic
     */
    @Transactional
    public BatchProcessingResult processBatch(List<PredictedFlightData> predictedFlights) {
        if (predictedFlights == null || predictedFlights.isEmpty()) {
            return new BatchProcessingResult(0, 0, 0, 0, 
                "No predicted flights to process");
        }
        
        int totalReceived = predictedFlights.size();
        int processed = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        List<String> skippedDetails = new ArrayList<>();
        Map<String, Integer> skipReasons = new HashMap<>();
        
        logger.info("Starting batch processing of {} predicted flights", totalReceived);
        
        // Determine optimal batch size for database operations
        final int OPTIMAL_BATCH_SIZE = 500;
        List<PredictedFlightData> toSave = new ArrayList<>();
        
        for (PredictedFlightData predictedFlight : predictedFlights) {
            try {
                // Validate required fields
                if (predictedFlight.getInstanceId() == 0) {
                    failed++;
                    String error = "PredictedFlight missing instanceId: " + predictedFlight.getIndicative();
                    errors.add(error);
                    continue;
                }
                
                // Check if already exists (skip logic as per your requirement)
                if (predictedFlightRepository.existsByInstanceId(predictedFlight.getInstanceId())) {
                    skipped++;
                    String skipDetail = String.format("Skipped instanceId %d (indicative: %s) - already exists in database", 
                        predictedFlight.getInstanceId(), predictedFlight.getIndicative());
                    skippedDetails.add(skipDetail);
                    
                    // Update skip reasons summary
                    String reason = "Duplicate instanceId";
                    skipReasons.put(reason, skipReasons.getOrDefault(reason, 0) + 1);
                    
                    logger.debug("Skipping existing predicted flight with instanceId: {}", predictedFlight.getInstanceId());
                    continue;
                }
                
                // Add to batch for saving
                toSave.add(predictedFlight);
                
                // Process batch when it reaches optimal size
                if (toSave.size() >= OPTIMAL_BATCH_SIZE) {
                    processed += saveBatch(toSave);
                    toSave.clear();
                }
                
            } catch (Exception e) {
                failed++;
                String error = String.format("Error processing instanceId %d: %s", 
                    predictedFlight.getInstanceId(), e.getMessage());
                errors.add(error);
                logger.error("Error processing predicted flight instanceId: {}", predictedFlight.getInstanceId(), e);
            }
        }
        
        // Process remaining batch
        if (!toSave.isEmpty()) {
            processed += saveBatch(toSave);
        }
        
        String message = String.format(
            "Batch processing completed: %d received, %d processed, %d skipped, %d failed", 
            totalReceived, processed, skipped, failed);
        
        logger.info(message);
        
        return new BatchProcessingResult(totalReceived, processed, skipped, failed, 0L, message, 
            errors.isEmpty() ? null : errors, 
            skippedDetails.isEmpty() ? null : skippedDetails,
            skipReasons.isEmpty() ? null : skipReasons);
    }
    
    /**
     * Helper method to save a batch of predicted flights efficiently
     */
    private int saveBatch(List<PredictedFlightData> batch) {
        try {
            List<PredictedFlightData> saved = predictedFlightRepository.saveAll(batch);
            logger.debug("Successfully saved batch of {} predicted flights", saved.size());
            return saved.size();
        } catch (Exception e) {
            logger.error("Error saving batch of {} predicted flights", batch.size(), e);
            // Try to save individually to identify problematic records
            int savedCount = 0;
            for (PredictedFlightData flight : batch) {
                try {
                    predictedFlightRepository.save(flight);
                    savedCount++;
                } catch (Exception individualError) {
                    logger.error("Failed to save individual predicted flight instanceId: {}", 
                        flight.getInstanceId(), individualError);
                }
            }
            return savedCount;
        }
    }
    
    /**
     * Result class for processing operations
     */
    public static class ProcessingResult {
        private final boolean success;
        private final String message;
        
        public ProcessingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    /**
     * Statistics class for predicted flights
     */
    public static class PredictedFlightStats {
        private final long totalPredictedFlights;
        
        public PredictedFlightStats(long totalPredictedFlights) {
            this.totalPredictedFlights = totalPredictedFlights;
        }
        
        public long getTotalPredictedFlights() { return totalPredictedFlights; }
    }
}