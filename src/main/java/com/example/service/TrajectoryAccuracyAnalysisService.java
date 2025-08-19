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
 * Reuses flight qualification logic from PunctualityAnalysisService for:
 * - SBSP ↔ SBRJ route filtering
 * - planId matching between predicted and real flights
 * - 2 NM threshold geographic validation
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
    // FIXED: Flight level is in hundreds of feet (e.g., FL16 = 1,600 feet = 487.68 meters)
    private static final double FLIGHT_LEVEL_TO_METERS = 30.48; // 100 feet = 30.48 meters
    private static final double FEET_TO_METERS = 0.3048; // 1 foot = 0.3048 meters
    
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
            // FIXED: Reuse punctuality analysis methods for consistent flight qualification
            logger.info("Step 1: Finding qualifying flights using punctuality analysis methods");
            
            // Get matched flights from punctuality analysis (this includes the qualification logic)
            List<Map<String, Object>> matchedFlights = punctualityAnalysisService.matchPredictedWithRealFlights();
            
            logger.info("Found {} matched flights from punctuality analysis", matchedFlights.size());
            
            // Filter for flights that have both predicted and real data
            List<Map<String, Object>> qualifiedFlights = matchedFlights.stream()
                .filter(flight -> (Boolean) flight.get("hasRealFlight"))
                .collect(Collectors.toList());
            
            logger.info("Qualified flights: {} (have both predicted and real data)", qualifiedFlights.size());
            
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
                    
                    logger.debug("Analyzed flight planId={}, points={}, horizontalRMSE={:.4f}, verticalRMSE={:.4f}",
                               predictedFlight.getInstanceId(), predictedPointCount, 
                               flightMetrics.getHorizontalRMSE(), flightMetrics.getVerticalRMSE());
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
            // Convert real flight level to meters (flightLevel is in hundreds of feet, e.g., FL16 = 1,600 feet)
            double realAltitudeMeters = realPoint.getFlightLevel() * FLIGHT_LEVEL_TO_METERS;
            
            // Debug logging for first few points to verify altitude handling
            if (i < 3) {
                logger.debug("Point {}: elementType={}, levelMeters={} m (predicted), realFL={}, real={} m", 
                           i, predictedPoint.getElementType(), 
                           predictedPoint.getLevelMeters(),
                           realPoint.getFlightLevel(), realAltitudeMeters);
            }
            
            double verticalError = realAltitudeMeters - predictedAltitudeMeters;
            double verticalErrorSquared = verticalError * verticalError;
            
            verticalMSE += verticalErrorSquared;
            totalVerticalError += Math.abs(verticalError);
            maxVerticalError = Math.max(maxVerticalError, Math.abs(verticalError));
        }
        
        // Calculate final metrics
        horizontalMSE /= pointCount;
        verticalMSE /= pointCount;
        double horizontalRMSE = Math.sqrt(horizontalMSE);
        double verticalRMSE = Math.sqrt(verticalMSE);
        double averageHorizontalError = totalHorizontalError / pointCount;
        double averageVerticalError = totalVerticalError / pointCount;
        
        // Create and populate result
        TrajectoryAccuracyResult.FlightAccuracyMetrics metrics = 
            new TrajectoryAccuracyResult.FlightAccuracyMetrics(
                predictedFlight.getInstanceId(),
                predictedFlight.getIndicative(),
                realFlight.getIndicative(),
                pointCount,
                horizontalMSE,
                horizontalRMSE,
                verticalMSE,
                verticalRMSE
            );
        
        metrics.setMaxHorizontalError(maxHorizontalError);
        metrics.setMaxVerticalError(maxVerticalError);
        metrics.setAverageHorizontalError(averageHorizontalError);
        metrics.setAverageVerticalError(averageVerticalError);
        
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
     * Calculate aggregate metrics across all analyzed flights
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
        
        double aggregateHorizontalMSE = totalHorizontalMSE / totalPoints;
        double aggregateVerticalMSE = totalVerticalMSE / totalPoints;
        double aggregateHorizontalRMSE = Math.sqrt(aggregateHorizontalMSE);
        double aggregateVerticalRMSE = Math.sqrt(aggregateVerticalMSE);
        double averagePointsPerFlight = (double) totalPoints / flightResults.size();
        
        TrajectoryAccuracyResult.AggregateAccuracyMetrics aggregate = 
            new TrajectoryAccuracyResult.AggregateAccuracyMetrics(
                aggregateHorizontalMSE,
                aggregateHorizontalRMSE,
                aggregateVerticalMSE,
                aggregateVerticalRMSE,
                averagePointsPerFlight,
                totalPoints
            );
        
        aggregate.setMinHorizontalRMSE(minHorizontalRMSE);
        aggregate.setMaxHorizontalRMSE(maxHorizontalRMSE);
        aggregate.setMinVerticalRMSE(minVerticalRMSE);
        aggregate.setMaxVerticalRMSE(maxVerticalRMSE);
        
        return aggregate;
    }
}
