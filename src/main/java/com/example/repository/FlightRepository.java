package com.example.repository;

import com.example.model.JoinedFlightData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for flight data operations
 * Uses planId as the primary unique identifier
 */
@Repository
public interface FlightRepository extends MongoRepository<JoinedFlightData, String> {
    
    /**
     * Find flight by planId (primary unique identifier)
     */
    Optional<JoinedFlightData> findByPlanId(long planId);
    
    /**
     * Check if flight exists by planId
     */
    boolean existsByPlanId(long planId);
    
    /**
     * Find flight by indicative (call sign) - may not be unique
     * WARNING: This returns only the first match!
     */
    Optional<JoinedFlightData> findByIndicative(String indicative);
    
    /**
     * Find ALL flights by indicative (call sign) - handles multiple matches
     * Use this for proper disambiguation when multiple flights have same indicative
     */
    List<JoinedFlightData> findAllByIndicative(String indicative);
} 