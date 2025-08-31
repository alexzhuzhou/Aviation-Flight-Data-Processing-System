package com.example.service;

import com.example.model.*;
import com.example.repository.FlightRepository;
import com.example.repository.PredictedFlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing trajectory accuracy by comparing predicted flight routes
 * with actual flight tracking points using MSE and RMSE metrics.
 * 
 * Reuses complete filtering pipeline from PunctualityAnalysisService for consistency:
 * - SBSP ↔ SBRJ route filtering (findQualifyingFlights)
 * - planId matching between predicted and real flights (matchPredictedWithRealFlights)
 * - 2 NM threshold + flight level ≤ 4 geographic validation (filterFlightsByGeographicValidation)
 * 
 * This ensures both analyses use exactly the same qualified flight dataset.
 */
@Service
public class TrajectoryAccuracyAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrajectoryAccuracyAnalysisService.class);
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    @Autowired
    private PunctualityAnalysisService punctualityAnalysisService;
    
    // Constants for unit conversions
    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    // FIXED: Flight level conversion - standard aviation flight levels (FL350 = 35,000 feet)
    private static final double FLIGHT_LEVEL_TO_FEET = 100.0; // FL350 = 35,000 feet
    private static final double FEET_TO_METERS = 0.3048; // 1 foot = 0.3048 meters
    private static final double FLIGHT_LEVEL_TO_METERS = FLIGHT_LEVEL_TO_FEET * FEET_TO_METERS; // FL to meters directly
    
    // NEW: Constants for converting radians to meters (approximate for small angles)
    private static final double EARTH_RADIUS_METERS = 6371000.0; // Earth's radius in meters
    private static final double RADIANS_TO_METERS = EARTH_RADIUS_METERS; // For small angles: distance ≈ radius × angle_in_radians
    
    // Geographic constants (reused from PunctualityAnalysisService)
    private static final double SBSP_LAT = -23.6266; // São Paulo (Congonhas)
    private static final double SBSP_LON = -46.6556;
    private static final double SBRJ_LAT = -22.8099; // Rio de Janeiro (Santos Dumont)
    private static final double SBRJ_LON = -43.1635;
    private static final double THRESHOLD_NM = 2.0;
    private static final double NM_TO_DEGREES = 1.0 / 60.0; // Approximate conversion
    
    /**
     * Run complete trajectory accuracy analysis
     */
    public TrajectoryAccuracyResult runTrajectoryAccuracyAnalysis() {
        long startTime = System.currentTimeMillis();
        logger.info("Starting trajectory accuracy analysis...");
        
        TrajectoryAccuracyResult result = new TrajectoryAccuracyResult();
        
        try {
            // FIXED: Reuse complete punctuality analysis filtering pipeline for consistency
            logger.info("Step 1: Using punctuality analysis filtering pipeline for consistent flight qualification");
            
            // Use the complete filtering pipeline from punctuality analysis:
            // 1. findQualifyingFlights() - SBSP ↔ SBRJ route filtering
            // 2. matchPredictedWithRealFlights() - Match with real flights
            // 3. filterFlightsByGeographicValidation() - Geographic validation (2 NM + flight level ≤ 4)
            List<Map<String, Object>> qualifiedFlights = punctualityAnalysisService.filterFlightsByGeographicValidation();
            
            logger.info("Qualified flights after complete punctuality filtering: {}", qualifiedFlights.size());
            
            if (qualifiedFlights.isEmpty()) {
                result.setTotalQualifiedFlights(0);
                result.setTotalAnalyzedFlights(0);
                result.setTotalSkippedFlights(0);
                result.setMessage("No flights found with both predicted and real data for trajectory accuracy analysis");
                result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }
            
            // Step 2: Filter by equal point counts and calculate accuracy
            List<TrajectoryAccuracyResult.FlightAccuracyMetrics> flightResults = new ArrayList<>();
            int totalSkipped = 0;
            
            for (Map<String, Object> flightData : qualifiedFlights) {
                PredictedFlightData predictedFlight = (PredictedFlightData) flightData.get("predictedFlight");
                JoinedFlightData realFlight = (JoinedFlightData) flightData.get("realFlight");
                
                if (predictedFlight == null || realFlight == null) {
                    totalSkipped++;
                    continue;
                }
                
                int predictedPointCount = predictedFlight.getRouteElements() != null ? 
                    predictedFlight.getRouteElements().size() : 0;
                int realPointCount = realFlight.getTrackingPoints() != null ? 
                    realFlight.getTrackingPoints().size() : 0;
                
                // Only process flights with equal point counts
                if (predictedPointCount == realPointCount && predictedPointCount > 0) {
                    TrajectoryAccuracyResult.FlightAccuracyMetrics flightMetrics = 
                        calculateFlightAccuracy(predictedFlight, realFlight);
                    flightResults.add(flightMetrics);
                    
                    logger.debug("Analyzed flight planId={}, points={}, horizontalRMSE={:.6f} rad ({:.2f} m), verticalRMSE={:.2f} m",
                               predictedFlight.getInstanceId(), predictedPointCount, 
                               flightMetrics.getHorizontalRMSE(), flightMetrics.getHorizontalRMSEMeters(), 
                               flightMetrics.getVerticalRMSE());
                } else {
                    totalSkipped++;
                    logger.debug("Skipped flight planId={} - point count mismatch: predicted={}, real={}",
                               predictedFlight.getInstanceId(), predictedPointCount, realPointCount);
                }
            }
            
            // Step 3: Calculate aggregate metrics
            TrajectoryAccuracyResult.AggregateAccuracyMetrics aggregateMetrics = 
                calculateAggregateMetrics(flightResults);
            
            // Step 4: Set results
            result.setTotalQualifiedFlights(qualifiedFlights.size());
            result.setTotalAnalyzedFlights(flightResults.size());
            result.setTotalSkippedFlights(totalSkipped);
            result.setAggregateMetrics(aggregateMetrics);
            result.setFlightResults(flightResults);
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            result.setMessage(String.format(
                "Analysis completed: %d qualified flights matched, %d analyzed successfully, %d skipped due to point count mismatch",
                qualifiedFlights.size(), flightResults.size(), totalSkipped));
            
            logger.info("Trajectory accuracy analysis completed in {}ms. Analyzed {} flights, skipped {} flights",
                       result.getProcessingTimeMs(), flightResults.size(), totalSkipped);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error during trajectory accuracy analysis", e);
            result.setMessage("Analysis failed: " + e.getMessage());
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }
    }
    
    /**
     * Get statistics about available data for trajectory accuracy analysis
     */
    public Map<String, Object> getTrajectoryAccuracyStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalPredictedFlights = predictedFlightRepository.count();
            long totalRealFlights = flightRepository.count();
            
            // FIXED: Use punctuality analysis service for consistent qualification
            List<Map<String, Object>> matchedFlights = punctualityAnalysisService.matchPredictedWithRealFlights();
            
            List<Map<String, Object>> qualifiedFlights = matchedFlights.stream()
                .filter(flight -> (Boolean) flight.get("hasRealFlight"))
                .collect(Collectors.toList());
            
            stats.put("totalPredictedFlights", totalPredictedFlights);
            stats.put("totalRealFlights", totalRealFlights);
            stats.put("qualifiedPredictedFlights", matchedFlights.size());
            stats.put("qualifiedRealFlights", qualifiedFlights.size());
            stats.put("potentialMatches", qualifiedFlights.size());
            stats.put("analysisCapability", qualifiedFlights.size() > 0);
            
        } catch (Exception e) {
            logger.error("Error getting trajectory accuracy stats", e);
            stats.put("error", e.getMessage());
            stats.put("analysisCapability", false);
        }
        
        return stats;
    }
    
    /**
     * Calculate accuracy metrics for a single flight
     */
    private TrajectoryAccuracyResult.FlightAccuracyMetrics calculateFlightAccuracy(
            PredictedFlightData predictedFlight, JoinedFlightData realFlight) {
        
        List<RouteElement> predictedPoints = predictedFlight.getRouteElements();
        List<TrackingPoint> realPoints = realFlight.getTrackingPoints();
        
        int pointCount = Math.min(predictedPoints.size(), realPoints.size());
        
        double horizontalMSE = 0.0;
        double verticalMSE = 0.0;
        double totalHorizontalError = 0.0;
        double totalVerticalError = 0.0;
        double maxHorizontalError = 0.0;
        double maxVerticalError = 0.0;
        
        for (int i = 0; i < pointCount; i++) {
            RouteElement predictedPoint = predictedPoints.get(i);
            TrackingPoint realPoint = realPoints.get(i);
            
            // Convert coordinates and calculate horizontal error
            double predictedLatRad = predictedPoint.getLatitude() * DEGREES_TO_RADIANS;
            double predictedLonRad = predictedPoint.getLongitude() * DEGREES_TO_RADIANS;
            double realLatRad = realPoint.getLatitude(); // Already in radians
            double realLonRad = realPoint.getLongitude(); // Already in radians
            
            double latError = realLatRad - predictedLatRad;
            double lonError = realLonRad - predictedLonRad;
            double horizontalError = latError * latError + lonError * lonError;
            
            horizontalMSE += horizontalError;
            totalHorizontalError += Math.sqrt(horizontalError);
            maxHorizontalError = Math.max(maxHorizontalError, Math.sqrt(horizontalError));
            
            // Calculate vertical error using consistent meter units
            double predictedAltitudeMeters = getPredictedAltitudeInMeters(predictedPoint); // levelMeters field (already in meters)
            // FIXED: Convert real flight level to meters properly
            // Real flight level appears to be in standard aviation flight levels (hundreds of feet)
            double realAltitudeMeters = realPoint.getFlightLevel() * 100.0 * 0.3048; // FL to feet to meters
            
            // Debug logging for first few points to verify altitude handling
            if (i < 3) {
                logger.debug("Point {}: predicted={} m, realFL={} (={} m), elementType={}", 
                           i, predictedAltitudeMeters, realPoint.getFlightLevel(), 
                           realAltitudeMeters, predictedPoint.getElementType());
            }
            
            double verticalError = realAltitudeMeters - predictedAltitudeMeters;
            double verticalErrorSquared = verticalError * verticalError;
            
            // ADDED: Log significant altitude differences for investigation
            if (Math.abs(verticalError) > 5000) { // More than 5km difference
                logger.warn("Large altitude difference at point {}: predicted={} m, real={} m, diff={} m", 
                           i, predictedAltitudeMeters, realAltitudeMeters, verticalError);
            }
            
            verticalMSE += verticalErrorSquared;
            totalVerticalError += Math.abs(verticalError);
            maxVerticalError = Math.max(maxVerticalError, Math.abs(verticalError));
        }
        
        // Calculate final metrics in radians (original)
        horizontalMSE /= pointCount;
        verticalMSE /= pointCount;
        double horizontalRMSE = Math.sqrt(horizontalMSE);
        double verticalRMSE = Math.sqrt(verticalMSE);
        double averageHorizontalError = totalHorizontalError / pointCount;
        double averageVerticalError = totalVerticalError / pointCount;
        
        // NEW: Convert horizontal metrics from radians to meters for more intuitive results
        double horizontalMSEMeters = horizontalMSE * RADIANS_TO_METERS * RADIANS_TO_METERS; // radians² to meters²
        double horizontalRMSEMeters = horizontalRMSE * RADIANS_TO_METERS; // radians to meters
        double averageHorizontalErrorMeters = averageHorizontalError * RADIANS_TO_METERS; // radians to meters
        double maxHorizontalErrorMeters = maxHorizontalError * RADIANS_TO_METERS; // radians to meters
        
        // Create and populate result with both radians and meters
        TrajectoryAccuracyResult.FlightAccuracyMetrics metrics = 
            new TrajectoryAccuracyResult.FlightAccuracyMetrics(
                predictedFlight.getInstanceId(),
                predictedFlight.getIndicative(),
                realFlight.getIndicative(),
                pointCount,
                horizontalMSE,    // radians²
                horizontalRMSE,   // radians
                verticalMSE,      // meters²
                verticalRMSE      // meters
            );
        
        // Set additional horizontal metrics (radians)
        metrics.setMaxHorizontalError(maxHorizontalError);           // radians
        metrics.setAverageHorizontalError(averageHorizontalError);   // radians
        
        // Set horizontal metrics in meters
        metrics.setHorizontalMSEMeters(horizontalMSEMeters);         // meters²
        metrics.setHorizontalRMSEMeters(horizontalRMSEMeters);       // meters
        metrics.setMaxHorizontalErrorMeters(maxHorizontalErrorMeters); // meters
        metrics.setAverageHorizontalErrorMeters(averageHorizontalErrorMeters); // meters
        
        // Set vertical metrics (already in meters)
        metrics.setMaxVerticalError(maxVerticalError);               // meters
        metrics.setAverageVerticalError(averageVerticalError);       // meters
        
        return metrics;
    }
    
    /**
     * Get predicted altitude in meters using levelMeters field consistently.
     * 
     * UPDATED: Since trajectory densification now populates levelMeters for all route elements,
     * we can use levelMeters consistently for all predicted flight altitude data.
     * This eliminates the need for different conversion logic based on element type.
     */
    private double getPredictedAltitudeInMeters(RouteElement routeElement) {
        // Use levelMeters field for all predicted flight altitude data (already in meters)
        return routeElement.getLevelMeters();
    }
    
    /**
     * Calculate aggregate metrics across all analyzed flights with both radians and meters
     */
    private TrajectoryAccuracyResult.AggregateAccuracyMetrics calculateAggregateMetrics(
            List<TrajectoryAccuracyResult.FlightAccuracyMetrics> flightResults) {
        
        if (flightResults.isEmpty()) {
            return new TrajectoryAccuracyResult.AggregateAccuracyMetrics(0, 0, 0, 0, 0, 0);
        }
        
        double totalHorizontalMSE = 0.0;
        double totalVerticalMSE = 0.0;
        int totalPoints = 0;
        
        double minHorizontalRMSE = Double.MAX_VALUE;
        double maxHorizontalRMSE = Double.MIN_VALUE;
        double minVerticalRMSE = Double.MAX_VALUE;
        double maxVerticalRMSE = Double.MIN_VALUE;
        
        for (TrajectoryAccuracyResult.FlightAccuracyMetrics flight : flightResults) {
            totalHorizontalMSE += flight.getHorizontalMSE() * flight.getPointCount();
            totalVerticalMSE += flight.getVerticalMSE() * flight.getPointCount();
            totalPoints += flight.getPointCount();
            
            minHorizontalRMSE = Math.min(minHorizontalRMSE, flight.getHorizontalRMSE());
            maxHorizontalRMSE = Math.max(maxHorizontalRMSE, flight.getHorizontalRMSE());
            minVerticalRMSE = Math.min(minVerticalRMSE, flight.getVerticalRMSE());
            maxVerticalRMSE = Math.max(maxVerticalRMSE, flight.getVerticalRMSE());
        }
        
        // Calculate aggregate metrics in radians (original)
        double aggregateHorizontalMSE = totalHorizontalMSE / totalPoints;
        double aggregateVerticalMSE = totalVerticalMSE / totalPoints;
        double aggregateHorizontalRMSE = Math.sqrt(aggregateHorizontalMSE);
        double aggregateVerticalRMSE = Math.sqrt(aggregateVerticalMSE);
        double averagePointsPerFlight = (double) totalPoints / flightResults.size();
        
        // NEW: Convert horizontal aggregate metrics to meters
        double aggregateHorizontalMSEMeters = aggregateHorizontalMSE * RADIANS_TO_METERS * RADIANS_TO_METERS;
        double aggregateHorizontalRMSEMeters = aggregateHorizontalRMSE * RADIANS_TO_METERS;
        double minHorizontalRMSEMeters = minHorizontalRMSE * RADIANS_TO_METERS;
        double maxHorizontalRMSEMeters = maxHorizontalRMSE * RADIANS_TO_METERS;
        
        TrajectoryAccuracyResult.AggregateAccuracyMetrics aggregate = 
            new TrajectoryAccuracyResult.AggregateAccuracyMetrics(
                aggregateHorizontalMSE,   // radians²
                aggregateHorizontalRMSE,  // radians
                aggregateVerticalMSE,     // meters²
                aggregateVerticalRMSE,    // meters
                averagePointsPerFlight,
                totalPoints
            );
        
        // Set horizontal statistics (radians)
        aggregate.setMinHorizontalRMSE(minHorizontalRMSE);   // radians
        aggregate.setMaxHorizontalRMSE(maxHorizontalRMSE);   // radians
        
        // Set horizontal metrics and statistics (meters)
        aggregate.setHorizontalMSEMeters(aggregateHorizontalMSEMeters);     // meters²
        aggregate.setHorizontalRMSEMeters(aggregateHorizontalRMSEMeters);   // meters
        aggregate.setMinHorizontalRMSEMeters(minHorizontalRMSEMeters);      // meters
        aggregate.setMaxHorizontalRMSEMeters(maxHorizontalRMSEMeters);      // meters
        
        // Set vertical statistics (already in meters)
        aggregate.setMinVerticalRMSE(minVerticalRMSE);       // meters
        aggregate.setMaxVerticalRMSE(maxVerticalRMSE);       // meters
        
        return aggregate;
    }
}
