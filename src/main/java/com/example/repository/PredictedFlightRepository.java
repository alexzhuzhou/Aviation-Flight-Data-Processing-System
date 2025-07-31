package com.example.repository;

import com.example.model.PredictedFlightData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for predicted flight data operations
 * Uses instanceId as the primary identifier for matching with actual flights (via planId)
 */
@Repository
public interface PredictedFlightRepository extends MongoRepository<PredictedFlightData, String> {
    
    /**
     * Find predicted flight by instanceId (for matching with actual flight data planId)
     */
    Optional<PredictedFlightData> findByInstanceId(long instanceId);
    
    /**
     * Check if predicted flight exists by instanceId
     */
    boolean existsByInstanceId(long instanceId);
    
    /**
     * Find predicted flight by indicative (call sign)
     */
    Optional<PredictedFlightData> findByIndicative(String indicative);
    
    /**
     * Find ALL predicted flights by indicative (call sign)
     */
    List<PredictedFlightData> findAllByIndicative(String indicative);
    
    /**
     * Find predicted flights by route ID
     */
    List<PredictedFlightData> findByRouteId(long routeId);
    
    /**
     * Find predicted flights by start and end points
     */
    List<PredictedFlightData> findByStartPointIndicativeAndEndPointIndicative(
            String startPointIndicative, String endPointIndicative);
}