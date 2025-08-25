package com.example.service;

import com.example.model.JoinedFlightData;
import com.example.model.PredictedFlightData;
import com.example.repository.FlightRepository;
import com.example.repository.PredictedFlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for flight search and management operations
 */
@Service
public class FlightSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightSearchService.class);
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Search real flights by planId with partial matching
     */
    public List<JoinedFlightData> searchRealFlightsByPlanId(String query) {
        try {
            // If query is numeric, search by exact or partial planId
            if (query.matches("\\d+")) {
                Long planId = Long.parseLong(query);
                // First try exact match
                Optional<JoinedFlightData> exactMatch = flightRepository.findByPlanId(planId);
                if (exactMatch.isPresent()) {
                    return List.of(exactMatch.get());
                }
            }
            
            // Partial matching using regex
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("planId").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, JoinedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching real flights by planId: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search predicted flights by instanceId with partial matching
     */
    public List<PredictedFlightData> searchPredictedFlightsByInstanceId(String query) {
        try {
            // If query is numeric, search by exact or partial instanceId
            if (query.matches("\\d+")) {
                Long instanceId = Long.parseLong(query);
                // First try exact match
                Optional<PredictedFlightData> exactMatch = predictedFlightRepository.findByInstanceId(instanceId);
                if (exactMatch.isPresent()) {
                    return List.of(exactMatch.get());
                }
            }
            
            // Partial matching using regex
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("instanceId").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, PredictedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching predicted flights by instanceId: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search real flights by indicative with partial matching
     */
    public List<JoinedFlightData> searchRealFlightsByIndicative(String query) {
        try {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("indicative").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, JoinedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching real flights by indicative: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search predicted flights by indicative with partial matching
     */
    public List<PredictedFlightData> searchPredictedFlightsByIndicative(String query) {
        try {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("indicative").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, PredictedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching predicted flights by indicative: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search real flights by origin airport with partial matching
     */
    public List<JoinedFlightData> searchRealFlightsByOrigin(String query) {
        try {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("startPointIndicative").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, JoinedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching real flights by origin: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search predicted flights by origin airport with partial matching
     */
    public List<PredictedFlightData> searchPredictedFlightsByOrigin(String query) {
        try {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("startPointIndicative").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, PredictedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching predicted flights by origin: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search real flights by destination airport with partial matching
     */
    public List<JoinedFlightData> searchRealFlightsByDestination(String query) {
        try {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("endPointIndicative").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, JoinedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching real flights by destination: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Search predicted flights by destination airport with partial matching
     */
    public List<PredictedFlightData> searchPredictedFlightsByDestination(String query) {
        try {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            Query mongoQuery = new Query(Criteria.where("endPointIndicative").regex(pattern));
            mongoQuery.limit(50); // Limit results for performance
            
            return mongoTemplate.find(mongoQuery, PredictedFlightData.class);
            
        } catch (Exception e) {
            logger.error("Error searching predicted flights by destination: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get real flight by exact planId
     */
    public JoinedFlightData getRealFlightByPlanId(Long planId) {
        return flightRepository.findByPlanId(planId).orElse(null);
    }
    
    /**
     * Get predicted flight by exact instanceId
     */
    public PredictedFlightData getPredictedFlightByInstanceId(Long instanceId) {
        return predictedFlightRepository.findByInstanceId(instanceId).orElse(null);
    }
    
    /**
     * Delete real flight by planId
     */
    public boolean deleteRealFlight(Long planId) {
        try {
            Optional<JoinedFlightData> flight = flightRepository.findByPlanId(planId);
            if (flight.isPresent()) {
                flightRepository.delete(flight.get());
                logger.info("Deleted real flight with planId: {}", planId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting real flight with planId {}: {}", planId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Delete predicted flight by instanceId
     */
    public boolean deletePredictedFlight(Long instanceId) {
        try {
            Optional<PredictedFlightData> flight = predictedFlightRepository.findByInstanceId(instanceId);
            if (flight.isPresent()) {
                predictedFlightRepository.delete(flight.get());
                logger.info("Deleted predicted flight with instanceId: {}", instanceId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting predicted flight with instanceId {}: {}", instanceId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Bulk delete flights
     */
    public Map<String, Object> bulkDeleteFlights(List<Long> realFlightIds, List<Long> predictedFlightIds, boolean deleteMatching) {
        Map<String, Object> result = new HashMap<>();
        int realDeleted = 0;
        int predictedDeleted = 0;
        int realErrors = 0;
        int predictedErrors = 0;
        
        // Delete real flights
        if (realFlightIds != null) {
            for (Long planId : realFlightIds) {
                try {
                    boolean deleted = deleteRealFlight(planId);
                    if (deleted) {
                        realDeleted++;
                        // Also delete matching predicted flight if requested
                        if (deleteMatching) {
                            deletePredictedFlight(planId);
                        }
                    }
                } catch (Exception e) {
                    realErrors++;
                    logger.error("Error deleting real flight {}: {}", planId, e.getMessage());
                }
            }
        }
        
        // Delete predicted flights
        if (predictedFlightIds != null) {
            for (Long instanceId : predictedFlightIds) {
                try {
                    boolean deleted = deletePredictedFlight(instanceId);
                    if (deleted) {
                        predictedDeleted++;
                        // Also delete matching real flight if requested
                        if (deleteMatching) {
                            deleteRealFlight(instanceId);
                        }
                    }
                } catch (Exception e) {
                    predictedErrors++;
                    logger.error("Error deleting predicted flight {}: {}", instanceId, e.getMessage());
                }
            }
        }
        
        result.put("realFlightsDeleted", realDeleted);
        result.put("predictedFlightsDeleted", predictedDeleted);
        result.put("realFlightErrors", realErrors);
        result.put("predictedFlightErrors", predictedErrors);
        result.put("totalDeleted", realDeleted + predictedDeleted);
        result.put("totalErrors", realErrors + predictedErrors);
        result.put("message", String.format("Deleted %d real flights and %d predicted flights. %d errors occurred.", 
            realDeleted, predictedDeleted, realErrors + predictedErrors));
        
        return result;
    }
    
    /**
     * Get search statistics
     */
    public Map<String, Object> getSearchStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalRealFlights = flightRepository.count();
            long totalPredictedFlights = predictedFlightRepository.count();
            
            // Get unique indicatives count
            Query realIndicativesQuery = new Query();
            realIndicativesQuery.fields().include("indicative");
            List<JoinedFlightData> realFlights = mongoTemplate.find(realIndicativesQuery, JoinedFlightData.class);
            long uniqueRealIndicatives = realFlights.stream().map(JoinedFlightData::getIndicative).distinct().count();
            
            Query predictedIndicativesQuery = new Query();
            predictedIndicativesQuery.fields().include("indicative");
            List<PredictedFlightData> predictedFlights = mongoTemplate.find(predictedIndicativesQuery, PredictedFlightData.class);
            long uniquePredictedIndicatives = predictedFlights.stream().map(PredictedFlightData::getIndicative).distinct().count();
            
            stats.put("totalRealFlights", totalRealFlights);
            stats.put("totalPredictedFlights", totalPredictedFlights);
            stats.put("uniqueRealIndicatives", uniqueRealIndicatives);
            stats.put("uniquePredictedIndicatives", uniquePredictedIndicatives);
            stats.put("matchingRate", totalRealFlights > 0 ? (double) totalPredictedFlights / totalRealFlights * 100 : 0);
            
        } catch (Exception e) {
            logger.error("Error getting search stats: {}", e.getMessage(), e);
            stats.put("error", "Failed to calculate statistics");
        }
        
        return stats;
    }
}
