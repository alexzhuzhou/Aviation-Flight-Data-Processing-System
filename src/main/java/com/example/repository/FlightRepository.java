package com.example.repository;

import com.example.model.JoinedFlightData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

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
     */
    Optional<JoinedFlightData> findByIndicative(String indicative);
} 