package com.example.service;

import com.example.model.PredictedFlightData;
import com.example.model.PunctualityAnalysisResult;
import com.example.model.JoinedFlightData;
import com.example.model.RouteElement;
import com.example.model.TrackingPoint;
import com.example.repository.PredictedFlightRepository;
import com.example.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Service for performing arrival punctuality analysis (ICAO KPI14)
 * 
 * Key responsibilities:
 * - Finding flights that meet specific route conditions (SBSP ↔ SBRJ)
 * - Extracting airport coordinates from route elements
 * - Comparing predicted en-route time with executed flight time
 * - Matching predicted flights with real flights via instanceId/planId
 * - Calculating KPI percentages for different delay tolerance windows
 * - Providing comprehensive punctuality analysis results
 */
@Service
public class PunctualityAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(PunctualityAnalysisService.class);
    
    // Target airports for analysis
    private static final String SBSP = "SBSP"; // São Paulo Congonhas
    private static final String SBRJ = "SBRJ"; // Rio de Janeiro Santos Dumont
    private static final String AERODROME_TYPE = "AERODROME";
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    /**
     * Step 1: Find predicted flights that meet the specific conditions
     * - First route element must be AERODROME
     * - Last route element must be AERODROME  
     * - Origin/destination must be SBSP ↔ SBRJ (either direction)
     */
    public List<PredictedFlightData> findQualifyingFlights() {
        try {
            logger.info("Step 1: Finding predicted flights with SBSP ↔ SBRJ routes and AERODROME endpoints");
            
            // Get all predicted flights
            List<PredictedFlightData> allPredictedFlights = predictedFlightRepository.findAll();
            logger.info("Total predicted flights found: {}", allPredictedFlights.size());
            
            List<PredictedFlightData> qualifyingFlights = new ArrayList<>();
            
            for (PredictedFlightData flight : allPredictedFlights) {
                if (meetsQualifyingConditions(flight)) {
                    qualifyingFlights.add(flight);
                }
            }
            
            logger.info("Step 1 completed: Found {} qualifying flights out of {} total", 
                qualifyingFlights.size(), allPredictedFlights.size());
            
            return qualifyingFlights;
            
        } catch (Exception e) {
            logger.error("Error in Step 1: Finding qualifying flights", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Extract airport coordinates from qualifying flights
     * Returns a list of flights with their departure and arrival airport coordinates
     */
    public List<Map<String, Object>> extractAirportCoordinates() {
        try {
            logger.info("Extracting airport coordinates from qualifying flights");
            
            List<PredictedFlightData> qualifyingFlights = findQualifyingFlights();
            List<Map<String, Object>> flightsWithCoordinates = new ArrayList<>();
            
            for (PredictedFlightData flight : qualifyingFlights) {
                Map<String, Object> flightData = new HashMap<>();
                
                // Basic flight info
                flightData.put("planId", flight.getInstanceId());
                flightData.put("indicative", flight.getIndicative());
                flightData.put("startPointIndicative", flight.getStartPointIndicative());
                flightData.put("endPointIndicative", flight.getEndPointIndicative());
                
                // Extract coordinates from first and last route elements
                List<RouteElement> routeElements = flight.getRouteElements();
                if (routeElements != null && routeElements.size() >= 2) {
                    RouteElement firstElement = routeElements.get(0);
                    RouteElement lastElement = routeElements.get(routeElements.size() - 1);
                    
                    // Departure airport coordinates (first element)
                    Map<String, Object> departureAirport = new HashMap<>();
                    departureAirport.put("indicative", firstElement.getIndicative());
                    departureAirport.put("latitude", firstElement.getLatitude());
                    departureAirport.put("longitude", firstElement.getLongitude());
                    departureAirport.put("elementType", firstElement.getElementType());
                    departureAirport.put("coordinateText", firstElement.getCoordinateText());
                    flightData.put("departureAirport", departureAirport);
                    
                    // Arrival airport coordinates (last element)
                    Map<String, Object> arrivalAirport = new HashMap<>();
                    arrivalAirport.put("indicative", lastElement.getIndicative());
                    arrivalAirport.put("latitude", lastElement.getLatitude());
                    arrivalAirport.put("longitude", lastElement.getLongitude());
                    arrivalAirport.put("elementType", lastElement.getElementType());
                    arrivalAirport.put("coordinateText", lastElement.getCoordinateText());
                    flightData.put("arrivalAirport", arrivalAirport);
                    
                    // Calculate distance between airports (approximate)
                    double distanceKm = calculateDistance(
                        firstElement.getLatitude(), firstElement.getLongitude(),
                        lastElement.getLatitude(), lastElement.getLongitude()
                    );
                    flightData.put("distanceKm", Math.round(distanceKm * 100.0) / 100.0);
                    
                    logger.debug("Flight {}: {} ({}, {}) → {} ({}, {}) - Distance: {} km", 
                        flight.getIndicative(),
                        firstElement.getIndicative(), firstElement.getLatitude(), firstElement.getLongitude(),
                        lastElement.getIndicative(), lastElement.getLatitude(), lastElement.getLongitude(),
                        Math.round(distanceKm * 100.0) / 100.0);
                }
                
                flightsWithCoordinates.add(flightData);
            }
            
            logger.info("Extracted coordinates for {} flights", flightsWithCoordinates.size());
            return flightsWithCoordinates;
            
        } catch (Exception e) {
            logger.error("Error extracting airport coordinates", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Filter matched flights based on geographic validation
     * Only include flights where:
     * - First tracking point is within 2 NM of predicted departing aerodrome
     * - Last tracking point is within 2 NM of predicted landing aerodrome
     * - Both first and last tracking points have flight level ≤ 4
     */
    public List<Map<String, Object>> filterFlightsByGeographicValidation() {
        try {
            logger.info("Applying geographic validation filter (2 NM threshold + flight level ≤ 37)");
            
            List<Map<String, Object>> matchedFlights = matchPredictedWithRealFlights();
            List<Map<String, Object>> geographicallyValidFlights = new ArrayList<>();
            
            int totalValidated = 0;
            int totalRejected = 0;
            int rejectedByDistance = 0;
            int rejectedByFlightLevel = 0;
            
            // Flight level threshold (4 = 400 feet, 3.7 = 370 feet)
            final int FLIGHT_LEVEL_THRESHOLD = 4;
            
            for (Map<String, Object> flight : matchedFlights) {
                PredictedFlightData predictedFlight = (PredictedFlightData) flight.get("predictedFlight");
                JoinedFlightData realFlight = (JoinedFlightData) flight.get("realFlight");
                
                if (predictedFlight == null || realFlight == null || 
                    predictedFlight.getRouteElements() == null || predictedFlight.getRouteElements().isEmpty() ||
                    realFlight.getTrackingPoints() == null || realFlight.getTrackingPoints().isEmpty()) {
                    totalRejected++;
                    continue;
                }
                
                // Get predicted aerodrome coordinates
                RouteElement firstRouteElement = predictedFlight.getRouteElements().get(0);
                RouteElement lastRouteElement = predictedFlight.getRouteElements().get(predictedFlight.getRouteElements().size() - 1);
                
                // Get real flight tracking points
                List<TrackingPoint> trackingPoints = realFlight.getTrackingPoints();
                TrackingPoint firstTrackingPoint = trackingPoints.get(0);
                TrackingPoint lastTrackingPoint = trackingPoints.get(trackingPoints.size() - 1);
                
                // Check flight level requirements (both must be ≤ 4)
                int firstFlightLevel = firstTrackingPoint.getFlightLevel();
                int lastFlightLevel = lastTrackingPoint.getFlightLevel();
                
                boolean flightLevelValid = firstFlightLevel <= FLIGHT_LEVEL_THRESHOLD && lastFlightLevel <= FLIGHT_LEVEL_THRESHOLD;
                
                if (!flightLevelValid) {
                    totalRejected++;
                    rejectedByFlightLevel++;
                    logger.debug("Flight {} rejected by flight level: departure={} ({}ft), arrival={} ({}ft)", 
                        predictedFlight.getIndicative(), 
                        firstFlightLevel, firstFlightLevel * 100,
                        lastFlightLevel, lastFlightLevel * 100);
                    continue;
                }
                
                // Convert tracking point coordinates from radians to degrees
                double firstTrackingLatRad = firstTrackingPoint.getLatitude();
                double firstTrackingLonRad = firstTrackingPoint.getLongitude();
                double lastTrackingLatRad = lastTrackingPoint.getLatitude();
                double lastTrackingLonRad = lastTrackingPoint.getLongitude();
                
                // Convert radians to degrees
                double firstTrackingLatDeg = Math.toDegrees(firstTrackingLatRad);
                double firstTrackingLonDeg = Math.toDegrees(firstTrackingLonRad);
                double lastTrackingLatDeg = Math.toDegrees(lastTrackingLatRad);
                double lastTrackingLonDeg = Math.toDegrees(lastTrackingLonRad);
                
                // Debug logging for coordinate conversion
                logger.debug("Flight {} coordinate conversion:", predictedFlight.getIndicative());
                logger.debug("  Departure - Predicted: ({}, {}), Real (rad): ({}, {}), Real (deg): ({}, {})", 
                    firstRouteElement.getLatitude(), firstRouteElement.getLongitude(),
                    firstTrackingLatRad, firstTrackingLonRad, firstTrackingLatDeg, firstTrackingLonDeg);
                logger.debug("  Arrival - Predicted: ({}, {}), Real (rad): ({}, {}), Real (deg): ({}, {})", 
                    lastRouteElement.getLatitude(), lastRouteElement.getLongitude(),
                    lastTrackingLatRad, lastTrackingLonRad, lastTrackingLatDeg, lastTrackingLonDeg);
                
                // Calculate distances (both coordinates now in degrees)
                double departureDistance = calculateDistance(
                    firstRouteElement.getLatitude(), firstRouteElement.getLongitude(),
                    firstTrackingLatDeg, firstTrackingLonDeg
                );
                
                double arrivalDistance = calculateDistance(
                    lastRouteElement.getLatitude(), lastRouteElement.getLongitude(),
                    lastTrackingLatDeg, lastTrackingLonDeg
                );
                
                // Check if within 2 NM threshold (1 NM = 1.852 km)
                double thresholdNM = 2.0;
                double thresholdKm = thresholdNM * 1.852;
                
                boolean distanceValid = departureDistance <= thresholdKm && arrivalDistance <= thresholdKm;
                
                if (distanceValid) {
                    // Add distance and flight level information to the flight data
                    Map<String, Object> validatedFlight = new HashMap<>(flight);
                    validatedFlight.put("departureDistanceNM", departureDistance / 1.852);
                    validatedFlight.put("arrivalDistanceNM", arrivalDistance / 1.852);
                    validatedFlight.put("departureDistanceKm", departureDistance);
                    validatedFlight.put("arrivalDistanceKm", arrivalDistance);
                    validatedFlight.put("departureFlightLevel", firstFlightLevel);
                    validatedFlight.put("arrivalFlightLevel", lastFlightLevel);
                    validatedFlight.put("departureFlightLevelFeet", firstFlightLevel * 100);
                    validatedFlight.put("arrivalFlightLevelFeet", lastFlightLevel * 100);
                    
                    geographicallyValidFlights.add(validatedFlight);
                    totalValidated++;
                    
                    logger.debug("Flight {} validated: departure={:.2f}NM (FL{}), arrival={:.2f}NM (FL{})", 
                        predictedFlight.getIndicative(), 
                        departureDistance/1.852, firstFlightLevel,
                        arrivalDistance/1.852, lastFlightLevel);
                } else {
                    totalRejected++;
                    rejectedByDistance++;
                    logger.debug("Flight {} rejected by distance: departure={:.2f}NM, arrival={:.2f}NM", 
                        predictedFlight.getIndicative(), departureDistance/1.852, arrivalDistance/1.852);
                }
            }
            
            logger.info("Geographic validation completed: {} validated, {} rejected out of {} matched flights", 
                totalValidated, totalRejected, matchedFlights.size());
            logger.info("Rejection breakdown: {} by distance, {} by flight level", 
                rejectedByDistance, rejectedByFlightLevel);
            
            return geographicallyValidFlights;
            
        } catch (Exception e) {
            logger.error("Error during geographic validation: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Check if a predicted flight meets the qualifying conditions:
     * 1. Has route elements
     * 2. First route element is AERODROME
     * 3. Last route element is AERODROME
     * 4. Origin/destination is SBSP ↔ SBRJ (either direction)
     */
    private boolean meetsQualifyingConditions(PredictedFlightData flight) {
        try {
            List<RouteElement> routeElements = flight.getRouteElements();
            
            // Check if route elements exist and have at least 2 elements
            if (routeElements == null || routeElements.size() < 2) {
                return false;
            }
            
            // Get first and last route elements
            RouteElement firstElement = routeElements.get(0);
            RouteElement lastElement = routeElements.get(routeElements.size() - 1);
            
            // Check if first and last elements are AERODROME
            if (!AERODROME_TYPE.equals(firstElement.getElementType()) || 
                !AERODROME_TYPE.equals(lastElement.getElementType())) {
                return false;
            }
            
            // Get airport codes
            String firstAirport = firstElement.getIndicative();
            String lastAirport = lastElement.getIndicative();
            
            // Check if route is SBSP ↔ SBRJ (either direction)
            boolean isSbspToSbrj = SBSP.equals(firstAirport) && SBRJ.equals(lastAirport);
            boolean isSbrjToSbsp = SBRJ.equals(firstAirport) && SBSP.equals(lastAirport);
            
            if (isSbspToSbrj || isSbrjToSbsp) {
                logger.debug("Found qualifying flight: {} ({} → {})", 
                    flight.getIndicative(), firstAirport, lastAirport);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking qualifying conditions for flight {}: {}", 
                flight.getIndicative(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Get statistics about qualifying flights
     */
    public Map<String, Object> getQualifyingFlightsStatistics() {
        try {
            List<PredictedFlightData> qualifyingFlights = findQualifyingFlights();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalQualifyingFlights", qualifyingFlights.size());
            
            // Count by direction
            int sbspToSbrj = 0;
            int sbrjToSbsp = 0;
            
            for (PredictedFlightData flight : qualifyingFlights) {
                List<RouteElement> routeElements = flight.getRouteElements();
                String firstAirport = routeElements.get(0).getIndicative();
                String lastAirport = routeElements.get(routeElements.size() - 1).getIndicative();
                
                if (SBSP.equals(firstAirport) && SBRJ.equals(lastAirport)) {
                    sbspToSbrj++;
                } else if (SBRJ.equals(firstAirport) && SBSP.equals(lastAirport)) {
                    sbrjToSbsp++;
                }
            }
            
            stats.put("sbspToSbrj", sbspToSbrj);
            stats.put("sbrjToSbsp", sbrjToSbsp);
            stats.put("analysisCapability", qualifyingFlights.size() > 0);
            
            logger.info("Qualifying flights statistics: {} total ({} SBSP→SBRJ, {} SBRJ→SBSP)", 
                qualifyingFlights.size(), sbspToSbrj, sbrjToSbsp);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting qualifying flights statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Could not retrieve statistics: " + e.getMessage());
            return errorStats;
        }
    }
    
    /**
     * Perform arrival punctuality analysis (ICAO KPI14) - TO BE IMPLEMENTED
     * This will be the next step after finding qualifying flights
     */
    public PunctualityAnalysisResult performPunctualityAnalysis() {
        try {
            logger.info("Starting arrival punctuality analysis (ICAO KPI14)");
            
            // Step 1: Find qualifying flights
            List<PredictedFlightData> qualifyingFlights = findQualifyingFlights();
            
            if (qualifyingFlights.isEmpty()) {
                return new PunctualityAnalysisResult(0, 0, new ArrayList<>(), 
                    LocalDateTime.now().toString(), "No qualifying flights found for analysis");
            }
            
            // Step 2: Match with real flights
            logger.info("Step 2: Matching predicted flights with real flights");
            List<Map<String, Object>> matchedFlights = matchPredictedWithRealFlights();
            
            // Filter for flights that have both predicted and real data
            List<Map<String, Object>> analyzableFlights = matchedFlights.stream()
                .filter(flight -> (Boolean) flight.get("hasRealFlight"))
                .collect(Collectors.toList());
            
            if (analyzableFlights.isEmpty()) {
                logger.warn("No flights could be matched between predicted and real data");
                return new PunctualityAnalysisResult(qualifyingFlights.size(), 0, new ArrayList<>(), 
                    LocalDateTime.now().toString(), 
                    String.format("Found %d qualifying flights but none could be matched with real flight data", 
                                qualifyingFlights.size()));
            }
            
            logger.info("Step 2 completed: {} flights can be analyzed (have both predicted and real data)", 
                       analyzableFlights.size());
            
            // Step 3: Calculate punctuality KPIs using existing method
            logger.info("Step 3: Calculating punctuality KPIs");
            Map<String, Object> kpiResults = calculatePunctualityKPIs();
            
            // Extract tolerance windows from KPI results
            List<PunctualityAnalysisResult.DelayToleranceWindow> toleranceWindows = new ArrayList<>();
            
            if (kpiResults.containsKey("toleranceWindows")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> windows = (List<Map<String, Object>>) kpiResults.get("toleranceWindows");
                
                for (Map<String, Object> window : windows) {
                    toleranceWindows.add(new PunctualityAnalysisResult.DelayToleranceWindow(
                        (String) window.get("windowDescription"),
                        (Integer) window.get("toleranceMinutes"),
                        (Integer) window.get("flightsWithinTolerance"),
                        (Double) window.get("percentageWithinTolerance"),
                        (String) window.get("kpiOutput")
                    ));
                }
            }
            
            // Get total analyzed flights from KPI results
            int totalAnalyzedFromKPI = kpiResults.containsKey("totalAnalyzed") ? 
                (Integer) kpiResults.get("totalAnalyzed") : analyzableFlights.size();
            
            logger.info("Punctuality analysis completed successfully: {} flights analyzed", totalAnalyzedFromKPI);
            
            return new PunctualityAnalysisResult(
                qualifyingFlights.size(), 
                totalAnalyzedFromKPI, 
                toleranceWindows, 
                LocalDateTime.now().toString(), 
                String.format("Analysis completed: %d predicted flights, %d matched with real flights, %d analyzed successfully", 
                            qualifyingFlights.size(), matchedFlights.size(), totalAnalyzedFromKPI)
            );
                
        } catch (Exception e) {
            logger.error("Error performing punctuality analysis", e);
            return new PunctualityAnalysisResult(0, 0, new ArrayList<>(), 
                LocalDateTime.now().toString(), "Error during analysis: " + e.getMessage());
        }
    }
    
    /**
     * Get punctuality analysis statistics summary
     */
    public Map<String, Object> getAnalysisStatistics() {
        return getQualifyingFlightsStatistics();
    }

    /**
     * Match qualifying predicted flights with their corresponding real flights
     * Uses instanceId (predicted) ↔ planId (real) matching
     * Returns flights with both predicted and real data
     */
    public List<Map<String, Object>> matchPredictedWithRealFlights() {
        try {
            logger.info("Matching qualifying predicted flights with real flights");
            
            List<PredictedFlightData> qualifyingFlights = findQualifyingFlights();
            List<Map<String, Object>> matchedFlights = new ArrayList<>();
            
            int totalMatched = 0;
            int totalNotFound = 0;
            
            for (PredictedFlightData predictedFlight : qualifyingFlights) {
                Map<String, Object> flightMatch = new HashMap<>();
                
                // Predicted flight data
                flightMatch.put("predictedFlight", predictedFlight);
                flightMatch.put("instanceId", predictedFlight.getInstanceId());
                flightMatch.put("predictedIndicative", predictedFlight.getIndicative());
                
                // Try to find matching real flight
                Optional<JoinedFlightData> realFlightOpt = flightRepository.findByPlanId(predictedFlight.getInstanceId());
                
                if (realFlightOpt.isPresent()) {
                    JoinedFlightData realFlight = realFlightOpt.get();
                    flightMatch.put("realFlight", realFlight);
                    flightMatch.put("planId", realFlight.getPlanId());
                    flightMatch.put("realIndicative", realFlight.getIndicative());
                    flightMatch.put("hasRealFlight", true);
                    flightMatch.put("trackingPointsCount", realFlight.getTrackingPoints() != null ? realFlight.getTrackingPoints().size() : 0);
                    
                    totalMatched++;
                    
                    logger.debug("Matched flight: {} (instanceId: {}) ↔ {} (planId: {})", 
                        predictedFlight.getIndicative(), predictedFlight.getInstanceId(),
                        realFlight.getIndicative(), realFlight.getPlanId());
                        
                } else {
                    flightMatch.put("hasRealFlight", false);
                    flightMatch.put("realFlight", null);
                    flightMatch.put("planId", null);
                    flightMatch.put("realIndicative", null);
                    flightMatch.put("trackingPointsCount", 0);
                    
                    totalNotFound++;
                    
                    logger.debug("No real flight found for predicted flight: {} (instanceId: {})", 
                        predictedFlight.getIndicative(), predictedFlight.getInstanceId());
                }
                
                matchedFlights.add(flightMatch);
            }
            
            logger.info("Flight matching completed: {} matched, {} not found out of {} qualifying flights", 
                totalMatched, totalNotFound, qualifyingFlights.size());
            
            return matchedFlights;
            
        } catch (Exception e) {
            logger.error("Error matching predicted with real flights", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get statistics about flight matching
     */
    public Map<String, Object> getFlightMatchingStatistics() {
        try {
            List<Map<String, Object>> matchedFlights = matchPredictedWithRealFlights();
            
            int totalFlights = matchedFlights.size();
            int matchedCount = 0;
            int notMatchedCount = 0;
            
            for (Map<String, Object> flightMatch : matchedFlights) {
                Boolean hasRealFlight = (Boolean) flightMatch.get("hasRealFlight");
                if (hasRealFlight != null && hasRealFlight) {
                    matchedCount++;
                } else {
                    notMatchedCount++;
                }
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalQualifyingFlights", totalFlights);
            stats.put("matchedFlights", matchedCount);
            stats.put("unmatchedFlights", notMatchedCount);
            stats.put("matchRate", totalFlights > 0 ? (matchedCount * 100.0 / totalFlights) : 0.0);
            stats.put("analysisCapability", matchedCount > 0);
            
            logger.info("Flight matching statistics: {} total, {} matched ({}%), {} unmatched", 
                totalFlights, matchedCount, String.format("%.1f", stats.get("matchRate")), notMatchedCount);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting flight matching statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Could not retrieve matching statistics: " + e.getMessage());
            return errorStats;
        }
    }
    
    /**
     * Get geographic validation statistics
     */
    public Map<String, Object> getGeographicValidationStatistics() {
        try {
            List<Map<String, Object>> matchedFlights = matchPredictedWithRealFlights();
            List<Map<String, Object>> validatedFlights = filterFlightsByGeographicValidation();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalMatchedFlights", matchedFlights.size());
            stats.put("totalValidatedFlights", validatedFlights.size());
            stats.put("totalRejectedFlights", matchedFlights.size() - validatedFlights.size());
            
            if (matchedFlights.size() > 0) {
                double validationRate = (double) validatedFlights.size() / matchedFlights.size() * 100;
                stats.put("validationRate", String.format("%.1f%%", validationRate));
            } else {
                stats.put("validationRate", "0.0%");
            }
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting geographic validation statistics: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * Calculate punctuality KPIs (ICAO KPI14)
     * Compares predicted vs actual flight times and calculates percentages within tolerance windows
     * Tolerance windows: ±3 minutes, ±5 minutes, ±15 minutes
     */
    public Map<String, Object> calculatePunctualityKPIs() {
        try {
            logger.info("Calculating punctuality KPIs (ICAO KPI14)");
            
            List<Map<String, Object>> geographicallyValidFlights = filterFlightsByGeographicValidation();
            List<Map<String, Object>> punctualityResults = new ArrayList<>();
            
            int totalAnalyzed = 0;
            int within3Min = 0;
            int within5Min = 0;
            int within15Min = 0;
            int totalErrors = 0;
            
            // Tolerance windows in milliseconds
            final long TOLERANCE_3_MIN = 3 * 60 * 1000L;  // 180,000 ms
            final long TOLERANCE_5_MIN = 5 * 60 * 1000L;  // 300,000 ms
            final long TOLERANCE_15_MIN = 15 * 60 * 1000L; // 900,000 ms
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            for (Map<String, Object> flight : geographicallyValidFlights) {
                try {
                    PredictedFlightData predictedFlight = (PredictedFlightData) flight.get("predictedFlight");
                    JoinedFlightData realFlight = (JoinedFlightData) flight.get("realFlight");
                    
                    if (predictedFlight == null || realFlight == null || 
                        realFlight.getTrackingPoints() == null || realFlight.getTrackingPoints().isEmpty()) {
                        totalErrors++;
                        continue;
                    }
                    
                    // Calculate actual flight duration from tracking points
                    List<TrackingPoint> trackingPoints = realFlight.getTrackingPoints();
                    TrackingPoint firstTrackingPoint = trackingPoints.get(0);
                    TrackingPoint lastTrackingPoint = trackingPoints.get(trackingPoints.size() - 1);
                    
                    long actualDepartureTime = firstTrackingPoint.getTimestamp();
                    long actualArrivalTime = lastTrackingPoint.getTimestamp();
                    long actualDuration = actualArrivalTime - actualDepartureTime;
                    
                    // Calculate predicted flight duration from time string
                    String timeString = predictedFlight.getTime();
                    long predictedDuration = parsePredictedFlightDuration(timeString, dateFormat);
                    
                    if (predictedDuration == -1) {
                        totalErrors++;
                        continue;
                    }
                    
                    // Calculate time difference
                    long timeDifference = Math.abs(actualDuration - predictedDuration);
                    
                    // Check tolerance windows
                    boolean within3MinWindow = timeDifference <= TOLERANCE_3_MIN;
                    boolean within5MinWindow = timeDifference <= TOLERANCE_5_MIN;
                    boolean within15MinWindow = timeDifference <= TOLERANCE_15_MIN;
                    
                    if (within3MinWindow) within3Min++;
                    if (within5MinWindow) within5Min++;
                    if (within15MinWindow) within15Min++;
                    
                    totalAnalyzed++;
                    
                    // Create detailed result for this flight
                    Map<String, Object> flightResult = new HashMap<>();
                    flightResult.put("planId", realFlight.getPlanId()); // Add planId as unique identifier
                    flightResult.put("flightIndicative", predictedFlight.getIndicative());
                    flightResult.put("actualDurationMs", actualDuration);
                    flightResult.put("predictedDurationMs", predictedDuration);
                    flightResult.put("timeDifferenceMs", timeDifference);
                    flightResult.put("timeDifferenceMinutes", timeDifference / 60000.0);
                    flightResult.put("within3Min", within3MinWindow);
                    flightResult.put("within5Min", within5MinWindow);
                    flightResult.put("within15Min", within15MinWindow);
                    flightResult.put("actualDepartureTime", actualDepartureTime);
                    flightResult.put("actualArrivalTime", actualArrivalTime);
                    flightResult.put("predictedTimeString", timeString);
                    
                    punctualityResults.add(flightResult);
                    
                    logger.debug("Flight {} (planId: {}): Actual={}ms, Predicted={}ms, Diff={}ms ({}min), Within3Min={}, Within5Min={}, Within15Min={}", 
                        predictedFlight.getIndicative(), realFlight.getPlanId(), actualDuration, predictedDuration, timeDifference, 
                        timeDifference / 60000.0, within3MinWindow, within5MinWindow, within15MinWindow);
                        
                } catch (Exception e) {
                    logger.error("Error analyzing flight punctuality: {}", e.getMessage());
                    totalErrors++;
                }
            }
            
            // Calculate KPI percentages
            Map<String, Object> kpiResults = new HashMap<>();
            kpiResults.put("totalAnalyzed", totalAnalyzed);
            kpiResults.put("totalErrors", totalErrors);
            
            if (totalAnalyzed > 0) {
                double percentage3Min = (double) within3Min / totalAnalyzed * 100;
                double percentage5Min = (double) within5Min / totalAnalyzed * 100;
                double percentage15Min = (double) within15Min / totalAnalyzed * 100;
                
                kpiResults.put("within3MinCount", within3Min);
                kpiResults.put("within3MinPercentage", String.format("%.1f%%", percentage3Min));
                kpiResults.put("within5MinCount", within5Min);
                kpiResults.put("within5MinPercentage", String.format("%.1f%%", percentage5Min));
                kpiResults.put("within15MinCount", within15Min);
                kpiResults.put("within15MinPercentage", String.format("%.1f%%", percentage15Min));
                
                logger.info("Punctuality KPIs calculated: {} flights analyzed", totalAnalyzed);
                logger.info("±3min: {}/{} ({:.1f}%)", within3Min, totalAnalyzed, percentage3Min);
                logger.info("±5min: {}/{} ({:.1f}%)", within5Min, totalAnalyzed, percentage5Min);
                logger.info("±15min: {}/{} ({:.1f}%)", within15Min, totalAnalyzed, percentage15Min);
            } else {
                kpiResults.put("within3MinCount", 0);
                kpiResults.put("within3MinPercentage", "0.0%");
                kpiResults.put("within5MinCount", 0);
                kpiResults.put("within5MinPercentage", "0.0%");
                kpiResults.put("within15MinCount", 0);
                kpiResults.put("within15MinPercentage", "0.0%");
            }
            
            kpiResults.put("detailedResults", punctualityResults);
            
            return kpiResults;
            
        } catch (Exception e) {
            logger.error("Error calculating punctuality KPIs: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Could not calculate KPIs: " + e.getMessage());
            return errorResult;
        }
    }
   
    /**
     * Parse predicted flight duration from time string
     * Format: '[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]'
     * Returns duration in milliseconds, or -1 if parsing fails
     */
    private long parsePredictedFlightDuration(String timeString, SimpleDateFormat dateFormat) {
        try {
            if (timeString == null || timeString.isEmpty()) {
                return -1;
            }
            
            // Remove brackets and split by comma
            String cleanTimeString = timeString.replaceAll("[\\[\\]]", "");
            String[] timeParts = cleanTimeString.split(",");
            
            if (timeParts.length != 2) {
                logger.error("Invalid time string format: {}", timeString);
                return -1;
            }
            
            // Parse departure and arrival times
            Date departureTime = dateFormat.parse(timeParts[0].trim());
            Date arrivalTime = dateFormat.parse(timeParts[1].trim());
            
            // Calculate duration in milliseconds
            long duration = arrivalTime.getTime() - departureTime.getTime();
            
            if (duration < 0) {
                logger.error("Negative duration calculated for time string: {}", timeString);
                return -1;
            }
            
            return duration;
            
        } catch (Exception e) {
            logger.error("Error parsing predicted flight duration from '{}': {}", timeString, e.getMessage());
            return -1;
        }
    }
}