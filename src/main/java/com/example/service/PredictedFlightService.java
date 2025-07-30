package com.example.service;

import com.example.model.PredictedFlightData;
import com.example.repository.PredictedFlightRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * Maps the JSON 'id' field to 'planId' for future matching with actual flights
     */
    public ProcessingResult processPredictedFlight(JsonNode jsonData) {
        try {
            logger.info("Processing predicted flight data for indicative: {}", 
                jsonData.get("indicative").asText());
            
            // Convert JSON to PredictedFlightData object
            PredictedFlightData predictedFlight = objectMapper.treeToValue(jsonData, PredictedFlightData.class);
            
            // Map the JSON 'id' field to 'planId' for matching with actual flights
            if (jsonData.has("id")) {
                long jsonId = jsonData.get("id").asLong();
                predictedFlight.setPlanId(jsonId);
                logger.debug("Mapped JSON id {} to planId for predicted flight {}", 
                    jsonId, predictedFlight.getIndicative());
            }
            
            // Check if predicted flight already exists
            if (predictedFlightRepository.existsByPlanId(predictedFlight.getPlanId())) {
                logger.warn("Predicted flight with planId {} already exists, updating...", 
                    predictedFlight.getPlanId());
                
                // Update existing record
                Optional<PredictedFlightData> existingOpt = 
                    predictedFlightRepository.findByPlanId(predictedFlight.getPlanId());
                
                if (existingOpt.isPresent()) {
                    PredictedFlightData existing = existingOpt.get();
                    predictedFlight.setId(existing.getId()); // Keep the same MongoDB ID
                }
            }
            
            // Save to database
            PredictedFlightData saved = predictedFlightRepository.save(predictedFlight);
            
            logger.info("Successfully stored predicted flight: planId={}, indicative={}, routeElements={}, routeSegments={}", 
                saved.getPlanId(), 
                saved.getIndicative(),
                saved.getRouteElements() != null ? saved.getRouteElements().size() : 0,
                saved.getRouteSegments() != null ? saved.getRouteSegments().size() : 0);
            
            return new ProcessingResult(true, 
                "Successfully processed predicted flight: " + saved.getIndicative() + 
                " (planId: " + saved.getPlanId() + ")");
            
        } catch (Exception e) {
            logger.error("Error processing predicted flight data", e);
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
     * Find predicted flight by planId (for comparison with actual flights)
     */
    public Optional<PredictedFlightData> findByPlanId(long planId) {
        return predictedFlightRepository.findByPlanId(planId);
    }
    
    /**
     * Check if predicted flight exists for given planId
     */
    public boolean existsByPlanId(long planId) {
        return predictedFlightRepository.existsByPlanId(planId);
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