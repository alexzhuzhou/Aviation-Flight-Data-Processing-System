package com.example.service;

import com.example.model.PredictedFlightData;
import com.example.model.PunctualityAnalysisResult;
import com.example.model.JoinedFlightData;
import com.example.repository.PredictedFlightRepository;
import com.example.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
 * - Comparing predicted en-route time with executed flight time
 * - Matching predicted flights with real flights via instanceId/planId
 * - Calculating KPI percentages for different delay tolerance windows
 * - Providing comprehensive punctuality analysis results
 */
@Service
public class PunctualityAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(PunctualityAnalysisService.class);
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    /**
     * Perform arrival punctuality analysis (ICAO KPI14)
     * Compares predicted en-route time with executed flight time
     */
    public PunctualityAnalysisResult performPunctualityAnalysis() {
        try {
            logger.info("Starting arrival punctuality analysis (ICAO KPI14)");
            
            // Get all predicted flights
            List<PredictedFlightData> allPredictedFlights = predictedFlightRepository.findAll();
            logger.info("Found {} predicted flights for analysis", allPredictedFlights.size());
            
            if (allPredictedFlights.isEmpty()) {
                return new PunctualityAnalysisResult(0, 0, new ArrayList<>(), 
                    LocalDateTime.now().toString(), "No predicted flights found for analysis");
            }
            
            // Track analysis results
            int totalMatched = 0;
            int totalAnalyzed = 0;
            
            // Delay tolerance windows (in minutes)
            int[] toleranceWindows = {3, 5, 15};
            Map<Integer, Integer> flightsWithinTolerance = new HashMap<>();
            for (int tolerance : toleranceWindows) {
                flightsWithinTolerance.put(tolerance, 0);
            }
            
            // Analyze each predicted flight
            for (PredictedFlightData predictedFlight : allPredictedFlights) {
                try {
                    // Find matching real flight by planId = instanceId
                    Optional<JoinedFlightData> realFlightOpt = flightRepository.findByPlanId(predictedFlight.getInstanceId());
                    
                    if (!realFlightOpt.isPresent()) {
                        logger.debug("No matching real flight found for instanceId: {}", predictedFlight.getInstanceId());
                        continue;
                    }
                    
                    totalMatched++;
                    JoinedFlightData realFlight = realFlightOpt.get();
                    
                    // Calculate predicted en-route time
                    Long predictedEnRouteMinutes = parsePredictedEnRouteTime(predictedFlight.getTime());
                    if (predictedEnRouteMinutes == null) {
                        logger.warn("Could not parse predicted time for instanceId {}: {}", 
                            predictedFlight.getInstanceId(), predictedFlight.getTime());
                        continue;
                    }
                    
                    // Calculate executed flight time
                    Long executedFlightMinutes = calculateExecutedFlightTime(realFlight);
                    if (executedFlightMinutes == null) {
                        logger.warn("Could not calculate executed flight time for planId {}", realFlight.getPlanId());
                        continue;
                    }
                    
                    totalAnalyzed++;
                    
                    // Calculate difference in minutes
                    long differenceMinutes = Math.abs(predictedEnRouteMinutes - executedFlightMinutes);
                    
                    // Check which tolerance windows this flight falls within
                    boolean withinAnyTolerance = false;
                    for (int tolerance : toleranceWindows) {
                        if (differenceMinutes <= tolerance) {
                            flightsWithinTolerance.put(tolerance, flightsWithinTolerance.get(tolerance) + 1);
                            withinAnyTolerance = true;
                        }
                    }
                    
                    // Log flights that don't fall within any tolerance window
                    if (!withinAnyTolerance) {
                        logger.info("Flight OUTSIDE all tolerance windows: {} (planId: {}) - predicted: {}min, executed: {}min, difference: {}min", 
                            realFlight.getIndicative(), realFlight.getPlanId(), predictedEnRouteMinutes, executedFlightMinutes, differenceMinutes);
                    }
                    
                    logger.debug("Analyzed flight {}: predicted={}min, executed={}min, difference={}min", 
                        realFlight.getIndicative(), predictedEnRouteMinutes, executedFlightMinutes, differenceMinutes);
                    
                } catch (Exception e) {
                    logger.error("Error analyzing predicted flight instanceId: {}", predictedFlight.getInstanceId(), e);
                }
            }
            
            // Build result with delay tolerance windows
            List<PunctualityAnalysisResult.DelayToleranceWindow> windows = new ArrayList<>();
            
            for (int tolerance : toleranceWindows) {
                int count = flightsWithinTolerance.get(tolerance);
                double percentage = totalAnalyzed > 0 ? (count * 100.0 / totalAnalyzed) : 0.0;
                
                String windowDesc = "± " + tolerance + " minutes";
                String kpiOutput = String.format("%.1f%% (%d/%d flights) where predicted time was within ± %d minutes of actual time", 
                    percentage, count, totalAnalyzed, tolerance);
                
                windows.add(new PunctualityAnalysisResult.DelayToleranceWindow(
                    windowDesc, tolerance, count, percentage, kpiOutput));
                
                logger.info("Tolerance {}: {}/{} flights ({}%)", windowDesc, count, totalAnalyzed, String.format("%.1f", percentage));
            }
            
            String message = String.format("Analysis completed: %d predicted flights, %d matched with real flights, %d analyzed successfully", 
                allPredictedFlights.size(), totalMatched, totalAnalyzed);
            
            logger.info(message);
            
            return new PunctualityAnalysisResult(totalMatched, totalAnalyzed, windows, 
                LocalDateTime.now().toString(), message);
                
        } catch (Exception e) {
            logger.error("Error performing punctuality analysis", e);
            return new PunctualityAnalysisResult(0, 0, new ArrayList<>(), 
                LocalDateTime.now().toString(), "Error during analysis: " + e.getMessage());
        }
    }
    
    /**
     * Parse predicted flight time string to calculate en-route time in minutes
     * Input format: "[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]"
     * Output: en-route time = end time - start time (in minutes)
     */
    private Long parsePredictedEnRouteTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Remove brackets and split by comma
            String cleanTime = timeString.replace("[", "").replace("]", "").trim();
            String[] timeParts = cleanTime.split(",");
            
            if (timeParts.length != 2) {
                logger.warn("Invalid time format - expected 2 parts separated by comma: {}", timeString);
                return null;
            }
            
            String startTimeStr = timeParts[0].trim();
            String endTimeStr = timeParts[1].trim();
            
            // Parse both timestamps
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            Date startTime = dateFormat.parse(startTimeStr);
            Date endTime = dateFormat.parse(endTimeStr);
            
            // Calculate difference in minutes
            long diffMillis = endTime.getTime() - startTime.getTime();
            long diffMinutes = diffMillis / (1000 * 60);
            
            logger.debug("Parsed predicted time: {} -> {} = {} minutes", startTimeStr, endTimeStr, diffMinutes);
            
            return diffMinutes;
            
        } catch (Exception e) {
            logger.error("Error parsing predicted time string: {}", timeString, e);
            return null;
        }
    }
    
    /**
     * Calculate executed flight time from real flight data
     * executed flight time = currentTimeOfArrival - flightPlanDate (in minutes)
     */
    private Long calculateExecutedFlightTime(JoinedFlightData realFlight) {
        try {
            String flightPlanDate = realFlight.getFlightPlanDate();
            String currentTimeOfArrival = realFlight.getCurrentDateTimeOfArrival();
            
            if (flightPlanDate == null || currentTimeOfArrival == null) {
                logger.warn("Missing time data for planId {}: flightPlanDate={}, currentTimeOfArrival={}", 
                    realFlight.getPlanId(), flightPlanDate, currentTimeOfArrival);
                return null;
            }
            
            // Parse timestamps - try different date formats commonly used
            Date planDate = null;
            Date arrivalDate = null;
            
            // Common date formats to try
            String[] dateFormats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",  // 2025-07-11T19:00:00.000+0000
                "yyyy-MM-dd'T'HH:mm:ssXXX",      // 2025-07-11T19:00:00+0000
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // 2025-07-11T19:00:00.000Z
                "yyyy-MM-dd'T'HH:mm:ss'Z'",      // 2025-07-11T19:00:00Z
                "yyyy-MM-dd'T'HH:mm:ss",         // 2025-07-11T19:00:00
                "yyyy-MM-dd HH:mm:ss",           // 2025-07-11 19:00:00
                "EEE MMM dd HH:mm:ss zzz yyyy"   // Thu Jul 10 22:25:00 UTC 2025
            };
            
            for (String format : dateFormats) {
                try {
                    SimpleDateFormat df = new SimpleDateFormat(format);
                    if (format.contains("Z") || format.contains("zzz")) {
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }
                    
                    if (planDate == null) {
                        planDate = df.parse(flightPlanDate);
                    }
                    if (arrivalDate == null) {
                        arrivalDate = df.parse(currentTimeOfArrival);
                    }
                    
                    if (planDate != null && arrivalDate != null) {
                        break;
                    }
                } catch (Exception e) {
                    // Try next format
                }
            }
            
            if (planDate == null || arrivalDate == null) {
                logger.warn("Could not parse dates for planId {}: flightPlanDate='{}', currentTimeOfArrival='{}'", 
                    realFlight.getPlanId(), flightPlanDate, currentTimeOfArrival);
                return null;
            }
            
            // Calculate difference in minutes
            long diffMillis = arrivalDate.getTime() - planDate.getTime();
            long diffMinutes = diffMillis / (1000 * 60);
            
            logger.debug("Calculated executed time for planId {}: {} -> {} = {} minutes", 
                realFlight.getPlanId(), flightPlanDate, currentTimeOfArrival, diffMinutes);
            
            return diffMinutes;
            
        } catch (Exception e) {
            logger.error("Error calculating executed flight time for planId: {}", realFlight.getPlanId(), e);
            return null;
        }
    }
    
    /**
     * Get punctuality analysis statistics summary
     */
    public Map<String, Object> getAnalysisStatistics() {
        try {
            long totalPredictedFlights = predictedFlightRepository.count();
            long totalRealFlights = flightRepository.count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPredictedFlights", totalPredictedFlights);
            stats.put("totalRealFlights", totalRealFlights);
            stats.put("analysisCapability", totalPredictedFlights > 0 && totalRealFlights > 0);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting analysis statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Could not retrieve statistics: " + e.getMessage());
            return errorStats;
        }
    }
}