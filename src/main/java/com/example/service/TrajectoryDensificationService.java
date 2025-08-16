package com.example.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.JoinedFlightData;
import com.example.model.PredictedFlightData;
import com.example.model.RouteElement;
import com.example.model.TrackingPoint;
import com.example.model.TrajectoryDensificationResult;
import com.example.repository.FlightRepository;
import com.example.repository.PredictedFlightRepository;

import br.atech.sigma.gpv.domain.sharedobjects.simulation.SimTrackSimulator;
import br.atech.sigma.gpv.domain.sharedobjects.simulation.FlightIntentionManipulatorAdapter;
import br.atech.sigma.gpv.domain.sharedobjects.simulation.AuxVarsVO;
import br.atech.sigma.gpv.domain.sharedobjects.vo.FlightIntentionVO;
import br.atech.sigma.gpv.domain.sharedobjects.vo.PathVO;
import br.atech.sigma.gpv.domain.sharedobjects.vo.SegmentVO;
import br.atech.sigma.gpv.domain.sharedobjects.vo.CoordinateVO;
import br.atech.sigma.gpv.domain.sharedobjects.vo.ExtractionRouteVO;
import br.atech.sigma.gpv.domain.sharedobjects.vo.RouteElementVO;

/**
 * Service for densifying predicted flight trajectories to match real flight tracking point density.
 * 
 * This service uses the Sigma simulation engine to generate interpolated route elements
 * for predicted flights, ensuring trajectory analysis can be performed accurately by
 * matching the density of tracking points between real and predicted flights.
 */
@Service
public class TrajectoryDensificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrajectoryDensificationService.class);
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    /**
     * Densifies a predicted flight trajectory to match the tracking point density of its corresponding real flight.
     * 
     * @param planId The planId to match real and predicted flights
     * @return TrajectoryDensificationResult with densified route elements
     */
    public TrajectoryDensificationResult densifyPredictedTrajectory(Long planId) {
        logger.info("Starting trajectory densification for planId: {}", planId);
        
        // 1. Find the real flight and predicted flight
        Optional<JoinedFlightData> realFlightOpt = flightRepository.findByPlanId(planId);
        Optional<PredictedFlightData> predictedFlightOpt = predictedFlightRepository.findByInstanceId(planId);
        
        if (!realFlightOpt.isPresent() || !predictedFlightOpt.isPresent()) {
            logger.warn("Could not find matching real and predicted flights for planId: {}", planId);
            return TrajectoryDensificationResult.notFound(planId);
        }
        
        JoinedFlightData realFlight = realFlightOpt.get();
        PredictedFlightData predictedFlight = predictedFlightOpt.get();
        
        // 2. Get target density from real flight
        int targetPointCount = realFlight.getTrackingPoints().size();
        int originalRouteElementCount = predictedFlight.getRouteElements().size();
        
        logger.info("Real flight has {} tracking points, predicted flight has {} route elements", 
                   targetPointCount, originalRouteElementCount);
        
        if (targetPointCount <= originalRouteElementCount) {
            logger.info("Predicted flight already has sufficient density, no densification needed");
            return TrajectoryDensificationResult.noActionNeeded(planId, targetPointCount, originalRouteElementCount);
        }
        
        // 3. Generate densified trajectory using simulation engine
        DensificationResult densificationResult = generateDensifiedTrajectoryWithStats(
            predictedFlight, targetPointCount, realFlight);
        
        List<RouteElement> densifiedRouteElements = densificationResult.elements;
        
        // 4. Validate densification results
        if (densifiedRouteElements.isEmpty()) {
            logger.warn("Densification failed - no route elements generated for planId: {}", planId);
            return TrajectoryDensificationResult.error(planId, 
                "Densification failed: Sigma simulation engine generated no route elements. Original route preserved.");
        }
        
        if (densifiedRouteElements.size() < originalRouteElementCount) {
            logger.warn("Densification generated fewer elements ({}) than original ({}) for planId: {}", 
                       densifiedRouteElements.size(), originalRouteElementCount, planId);
            return TrajectoryDensificationResult.error(planId, 
                String.format("Densification failed: Generated %d elements, less than original %d. Original route preserved.", 
                             densifiedRouteElements.size(), originalRouteElementCount));
        }
        
        // 5. Update predicted flight with densified route elements (only if successful)
        predictedFlight.setRouteElements(densifiedRouteElements);
        predictedFlightRepository.save(predictedFlight);
        
        logger.info("Successfully densified trajectory for planId: {} from {} to {} route elements", 
                   planId, originalRouteElementCount, densifiedRouteElements.size());
        
        return TrajectoryDensificationResult.success(planId, originalRouteElementCount, 
                                                   densifiedRouteElements.size(), targetPointCount,
                                                   densificationResult.sigmaPoints, densificationResult.linearPoints);
    }
    
    /**
     * Inner class to hold densification results with statistics.
     */
    private static class DensificationResult {
        List<RouteElement> elements;
        int sigmaPoints;
        int linearPoints;
        
        DensificationResult(List<RouteElement> elements, int sigmaPoints, int linearPoints) {
            this.elements = elements;
            this.sigmaPoints = sigmaPoints;
            this.linearPoints = linearPoints;
        }
    }
    
    /**
     * Generates densified trajectory using simulation engine with interpolation method tracking.
     */
    private DensificationResult generateDensifiedTrajectoryWithStats(PredictedFlightData predictedFlight, 
                                                          int targetPointCount, 
                                                          JoinedFlightData realFlight) {
        
        // The main method now returns DensificationResult with statistics
        return generateDensifiedTrajectory(predictedFlight, targetPointCount, realFlight);
    }
    
    /**
     * Generates densified trajectory using the Sigma simulation engine.
     * 
     * @param predictedFlight Original predicted flight data
     * @param targetPointCount Target number of route elements to generate
     * @param realFlight Real flight for timing reference
     * @return DensificationResult containing elements and statistics
     */
    private DensificationResult generateDensifiedTrajectory(PredictedFlightData predictedFlight, 
                                                          int targetPointCount, 
                                                          JoinedFlightData realFlight) {
        
        logger.debug("Generating densified trajectory with {} target points", targetPointCount);
        
        // 1. Convert predicted flight to FlightIntentionVO format for simulation
        FlightIntentionVO flightIntention = convertToFlightIntention(predictedFlight, realFlight);
        
        // Debug logging
        logger.debug("FlightIntentionVO setup: planId={}, indicative={}, segments={}, flightPlanDate={}", 
                   flightIntention.getPlanId(), flightIntention.getIndicative(),
                   flightIntention.getExtractionRoute() != null ? flightIntention.getExtractionRoute().getSegments().size() : 0,
                   flightIntention.getFlightPlanDate() != null ? flightIntention.getFlightPlanDate().getTime() : "null");
        
        // 2. Calculate time intervals based on FLIGHT PLAN duration, not real flight duration
        // Get the total flight plan duration from first to last segment
        List<SegmentVO> segments = flightIntention.getExtractionRoute().getSegments();
        if (segments.isEmpty()) {
            logger.warn("No segments available for simulation");
            return new DensificationResult(new ArrayList<>(), 0, 0);
        }
        
        int flightPlanStartSeconds = segments.get(0).getFirst().getAetSeconds();
        int flightPlanEndSeconds = segments.get(segments.size() - 1).getSecond().getAetSeconds();
        int flightPlanDurationSeconds = flightPlanEndSeconds - flightPlanStartSeconds;
        
        long timeIntervalSeconds = flightPlanDurationSeconds / (targetPointCount - 1);
        
        logger.debug("Flight plan: start={}s, end={}s, duration={}s ({}min), interval={}s", 
                   flightPlanStartSeconds, flightPlanEndSeconds, flightPlanDurationSeconds, 
                   flightPlanDurationSeconds/60, timeIntervalSeconds);
        
        // 3. Use simulation engine to generate intermediate points
        Calendar startTime = getFlightStartTime(realFlight);
        SimTrackSimulator simulator = createSimulator(startTime);
        
        List<RouteElement> densifiedElements = new ArrayList<>();
        int sigmaSuccessCount = 0;
        int linearInterpolationCount = 0;
        
        for (int i = 0; i < targetPointCount; i++) {
            // CRITICAL FIX: Calculate simulation time based on flight plan progression
            int simulationTimeSeconds = flightPlanStartSeconds + (int) (i * timeIntervalSeconds);
            
            // Convert to Calendar time - FIXED: Add AET offset to actual flight start time
            Calendar currentTime = (Calendar) startTime.clone();
            currentTime.add(Calendar.SECOND, simulationTimeSeconds);
            
            simulator.setCurrentTime(currentTime);
            
            List<PathVO> simulatedTracks = new ArrayList<>();
            RouteElement densifiedElement = null;
            
            try {
                // ENHANCED DEBUG for first few attempts
                if (i < 3) {
                    logger.debug("Simulation attempt {}: simulationTimeSeconds={}, currentTime={}, flightPlanRange={}s-{}s", 
                               i, simulationTimeSeconds, currentTime.getTime(), flightPlanStartSeconds, flightPlanEndSeconds);
                    logger.debug("FlightIntention details: planId={}, segments={}, extractionRoute={}", 
                               flightIntention.getPlanId(), 
                               flightIntention.getExtractionRoute() != null ? flightIntention.getExtractionRoute().getSegments().size() : "null",
                               flightIntention.getExtractionRoute() != null);
                }
                
                // Clear previous results
                simulatedTracks.clear();
                
                // Enhanced debugging: Check which segment this time falls into
                String segmentInfo = "NONE";
                for (int segIdx = 0; segIdx < segments.size(); segIdx++) {
                    SegmentVO seg = segments.get(segIdx);
                    if (simulationTimeSeconds >= seg.getFirst().getAetSeconds() && 
                        simulationTimeSeconds <= seg.getSecond().getAetSeconds()) {
                        segmentInfo = String.format("Segment[%d]: %ds-%ds", segIdx, 
                                                  seg.getFirst().getAetSeconds(), 
                                                  seg.getSecond().getAetSeconds());
                        break;
                    }
                }
                
                simulator.verifyAndCreateSimulatedTrack(simulatedTracks, flightIntention, new AuxVarsVO());
                
                if (i < 10 || simulatedTracks.isEmpty()) { // Log first 10 attempts or all failures
                    logger.debug("Simulation attempt {}: time={}s, segment={}, result={} tracks", 
                               i, simulationTimeSeconds, segmentInfo, simulatedTracks.size());
                    
                    if (!simulatedTracks.isEmpty()) {
                        PathVO track = simulatedTracks.get(0);
                        logger.debug("SUCCESS - Track details: kinematic={}, flightLevel={}", 
                                   track.getKinematic() != null, track.getFlightLevel());
                        if (track.getKinematic() != null && track.getKinematic().getPosition() != null) {
                            logger.debug("Position: lat={}, lon={}", 
                                       track.getKinematic().getPosition().getLatitude(),
                                       track.getKinematic().getPosition().getLongitude());
                        }
                    } else {
                        logger.debug("FAILURE - No tracks generated for time={}s, segment={}", 
                                   simulationTimeSeconds, segmentInfo);
                        
                        // Additional failure analysis
                        if (segmentInfo.equals("NONE")) {
                            logger.debug("FAILURE REASON: Time {}s falls outside all segment ranges", simulationTimeSeconds);
                        }
                    }
                }
                
                if (!simulatedTracks.isEmpty()) {
                    // SUCCESS: Use Sigma's physics-based simulation
                    PathVO simulatedTrack = simulatedTracks.get(0);
                    densifiedElement = convertToRouteElement(simulatedTrack, i);
                    sigmaSuccessCount++;
                    
                    if (i < 3) { // Log first few successful generations
                        logger.debug("SUCCESS: Generated point {} using Sigma simulation: lat={}, lon={}", i, 
                                   densifiedElement.getLatitude(), densifiedElement.getLongitude());
                    }
                } else {
                    // FALLBACK: Use linear interpolation between waypoints
                    densifiedElement = createRouteElementByLinearInterpolation(segments, simulationTimeSeconds, i);
                    if (densifiedElement != null) {
                        linearInterpolationCount++;
                        if (i < 3) {
                            logger.debug("FALLBACK: Generated point {} using linear interpolation: lat={}, lon={}", 
                                       i, densifiedElement.getLatitude(), densifiedElement.getLongitude());
                        }
                    } else {
                        if (i < 3) { // Log first few failures
                            logger.debug("FAILED: Could not generate point {} at simulationTime={}s", i, simulationTimeSeconds);
                        }
                    }
                }
            } catch (Exception e) {
                // FALLBACK: Use linear interpolation on exception
                densifiedElement = createRouteElementByLinearInterpolation(segments, simulationTimeSeconds, i);
                if (densifiedElement != null) {
                    linearInterpolationCount++;
                    if (i < 3) {
                        logger.debug("EXCEPTION FALLBACK: Generated point {} using linear interpolation after error: {}", 
                                   i, e.getMessage());
                    }
                } else {
                    logger.error("Error generating point {} at simulationTime={}s: {}", 
                               i, simulationTimeSeconds, e.getMessage());
                    if (i < 3) { // Only log stack trace for first few errors
                        logger.debug("Simulation error details", e);
                    }
                }
            }
            
            if (densifiedElement != null) {
                densifiedElements.add(densifiedElement);
            }
        }
        
        logger.info("Densification completed: {} Sigma points, {} linear interpolation points, {:.1f}% Sigma success rate", 
                   sigmaSuccessCount, linearInterpolationCount, 
                   (sigmaSuccessCount + linearInterpolationCount > 0) ? 
                   (sigmaSuccessCount * 100.0) / (sigmaSuccessCount + linearInterpolationCount) : 0.0);
        
        logger.debug("Generated {} densified route elements", densifiedElements.size());
        return new DensificationResult(densifiedElements, sigmaSuccessCount, linearInterpolationCount);
    }
    
    /**
     * Converts PredictedFlightData to FlightIntentionVO for use with simulation engine.
     * Uses only available fields in the Sigma FlightIntentionVO.
     */
    private FlightIntentionVO convertToFlightIntention(PredictedFlightData predictedFlight, JoinedFlightData realFlight) {
        FlightIntentionVO flightIntention = new FlightIntentionVO();
        
        // Set basic flight information
        flightIntention.setPlanId(predictedFlight.getInstanceId());
        flightIntention.setIndicative(predictedFlight.getIndicative());
        
        // Set aircraft type (use default since PredictedFlightData doesn't have this field)
        flightIntention.setAircraftType("B738"); // Default Boeing 737-800
        
        // Set SSR code
        flightIntention.setSsrCode("1000"); // Default SSR code
        
        // Convert route elements to segments for simulation
        List<SegmentVO> segments = convertRouteElementsToSegments(predictedFlight.getRouteElements(), realFlight);
        
        // Create and set extraction route with segments
        ExtractionRouteVO extractionRoute = new ExtractionRouteVO();
        extractionRoute.getSegments().addAll(segments);
        flightIntention.setExtractionRoute(extractionRoute);
        
        // Set timing information
        Calendar startTime = getFlightStartTime(realFlight);
        if (startTime != null) {
            LocalDateTime previewDeparture = LocalDateTime.ofInstant(
                startTime.toInstant(), 
                java.time.ZoneId.of("UTC")
            );
            flightIntention.setPreviewDeparture(previewDeparture);
            flightIntention.setFlightPlanDate(startTime);
        } else {
            // Fallback to current time
            Calendar currentTime = Calendar.getInstance();
            flightIntention.setFlightPlanDate(currentTime);
            LocalDateTime previewDeparture = LocalDateTime.ofInstant(
                currentTime.toInstant(), 
                java.time.ZoneId.of("UTC")
            );
            flightIntention.setPreviewDeparture(previewDeparture);
        }
        
        logger.debug("FlightIntentionVO created: planId={}, indicative={}, segments={}", 
                   flightIntention.getPlanId(), flightIntention.getIndicative(), segments.size());
        
        // ENHANCED VALIDATION: Check if FlightIntentionVO is properly configured
        logger.debug("FlightIntentionVO validation: planId={}, indicative={}, aircraftType={}, ssrCode={}", 
                   flightIntention.getPlanId(), flightIntention.getIndicative(), 
                   flightIntention.getAircraftType(), flightIntention.getSsrCode());
        logger.debug("FlightIntentionVO timing: flightPlanDate={}, previewDeparture={}", 
                   flightIntention.getFlightPlanDate() != null ? flightIntention.getFlightPlanDate().getTime() : "null",
                   flightIntention.getPreviewDeparture());
        logger.debug("FlightIntentionVO route: extractionRoute={}, segments={}", 
                   flightIntention.getExtractionRoute() != null,
                   flightIntention.getExtractionRoute() != null ? flightIntention.getExtractionRoute().getSegments().size() : 0);
        
        // Validate critical fields
        if (flightIntention.getExtractionRoute() == null || flightIntention.getExtractionRoute().getSegments().isEmpty()) {
            logger.warn("FlightIntentionVO has no extraction route or segments - this will cause simulation failure");
        }
        if (flightIntention.getFlightPlanDate() == null) {
            logger.warn("FlightIntentionVO has no flight plan date - this may cause simulation failure");
        }
        
        return flightIntention;
    }
    
    /**
     * Converts route elements to segments for simulation engine.
     * Fixed to align AET values with real flight timing.
     */
    private List<SegmentVO> convertRouteElementsToSegments(List<RouteElement> routeElements, JoinedFlightData realFlight) {
        List<SegmentVO> segments = new ArrayList<>();
        
        if (routeElements.size() < 2) {
            logger.warn("Insufficient route elements for segment creation: {}", routeElements.size());
            return segments;
        }
        
        // CRITICAL FIX: Normalize flight plan to start at AET=0 instead of absolute time offset
        // The Sigma simulation engine expects flight plans to start at AET=0
        
        // Calculate actual flight duration from real flight data
        long actualFlightDurationMs = calculateFlightDuration(realFlight);
        double actualFlightDurationMinutes = actualFlightDurationMs / 60000.0;
        
        // Find max EET in this specific flight for scaling
        double maxOriginalEET = routeElements.stream()
            .mapToDouble(RouteElement::getEetMinutes)
            .max()
            .orElse(1.0);
        
        // Calculate scaling factor to match actual flight duration
        double scalingFactor = actualFlightDurationMinutes / maxOriginalEET;
        
        logger.debug("EET normalization: actualDuration={}min, maxOriginalEET={}min, scalingFactor={}", 
                   actualFlightDurationMinutes, maxOriginalEET, scalingFactor);
        
        for (int i = 0; i < routeElements.size() - 1; i++) {
            RouteElement current = routeElements.get(i);
            RouteElement next = routeElements.get(i + 1);
            
            // Validate coordinates (primitive doubles, check for 0.0 as invalid)
            if (current.getLatitude() == 0.0 && current.getLongitude() == 0.0 ||
                next.getLatitude() == 0.0 && next.getLongitude() == 0.0) {
                logger.warn("Skipping segment {} due to invalid coordinates", i);
                continue;
            }
            
            SegmentVO segment = new SegmentVO();
            
            // Create first RouteElementVO
            RouteElementVO firstElement = new RouteElementVO();
            firstElement.setId((long) i);
            firstElement.setIndicative(current.getIndicative() != null ? current.getIndicative() : "WPT" + i);
            firstElement.setOriginal(true);
            
            // CRITICAL FIX: Use normalized AET starting from 0
            double currentEet = (current.getEetMinutes() != 0.0) ? current.getEetMinutes() : (i * 5.0);
            double scaledCurrentEet = currentEet * scalingFactor;
            int normalizedAetSeconds = (int) (scaledCurrentEet * 60);
            firstElement.setAetSeconds(normalizedAetSeconds);
            
            // Set speed (primitive double, use 0.0 check)
            double speedKnots = 450.0; // Default cruise speed
            if (current.getSpeedMeterPerSecond() != 0.0) {
                speedKnots = current.getSpeedMeterPerSecond() * 1.94384;
            }
            firstElement.setSpeed((float) speedKnots);
            
            // Set altitude (primitive double, use 0.0 check)
            double altitudeFeet = 35000.0; // Default FL350
            if (current.getLevelMeters() != 0.0) {
                altitudeFeet = current.getLevelMeters() * 3.28084;
            } else if (current.getAltitude() != 0.0) {
                altitudeFeet = current.getAltitude();
            }
            firstElement.setLevel((float) altitudeFeet);
            firstElement.setDesiredLevel((float) altitudeFeet);
            
            // Set coordinates
            CoordinateVO firstCoord = new CoordinateVO();
            firstCoord.setLatitude(current.getLatitude());
            firstCoord.setLongitude(current.getLongitude());
            firstElement.setCoordinate(firstCoord);
            
            // Create second RouteElementVO
            RouteElementVO secondElement = new RouteElementVO();
            secondElement.setId((long) (i + 1));
            secondElement.setIndicative(next.getIndicative() != null ? next.getIndicative() : "WPT" + (i + 1));
            secondElement.setOriginal(true);
            
            // CRITICAL FIX: Use normalized AET for second element
            double nextEet = (next.getEetMinutes() != 0.0) ? next.getEetMinutes() : ((i + 1) * 5.0);
            if (nextEet <= currentEet) {
                nextEet = currentEet + 5.0; // Ensure progressive timing
            }
            double scaledNextEet = nextEet * scalingFactor;
            int normalizedNextAetSeconds = (int) (scaledNextEet * 60);
            secondElement.setAetSeconds(normalizedNextAetSeconds);
            
            // Set speed for second element (primitive double, use 0.0 check)
            double nextSpeedKnots = 450.0;
            if (next.getSpeedMeterPerSecond() != 0.0) {
                nextSpeedKnots = next.getSpeedMeterPerSecond() * 1.94384;
            }
            secondElement.setSpeed((float) nextSpeedKnots);
            
            // Set altitude for second element (primitive double, use 0.0 check)
            double nextAltitudeFeet = 35000.0;
            if (next.getLevelMeters() != 0.0) {
                nextAltitudeFeet = next.getLevelMeters() * 3.28084;
            } else if (next.getAltitude() != 0.0) {
                nextAltitudeFeet = next.getAltitude();
            }
            secondElement.setLevel((float) nextAltitudeFeet);
            secondElement.setDesiredLevel((float) nextAltitudeFeet);
            
            // Set coordinates for second element
            CoordinateVO secondCoord = new CoordinateVO();
            secondCoord.setLatitude(next.getLatitude());
            secondCoord.setLongitude(next.getLongitude());
            secondElement.setCoordinate(secondCoord);
            
            // Set the RouteElementVO objects on the segment
            segment.setFirst(firstElement);
            segment.setSecond(secondElement);
            
            // Debug logging for first few segments
            if (i < 3) {
                logger.debug("NORMALIZED Segment {}: first AET={}s ({}h:{}m), second AET={}s ({}h:{}m), coords=({},{}) to ({},{})",
                           i, normalizedAetSeconds, normalizedAetSeconds/3600, (normalizedAetSeconds%3600)/60,
                           normalizedNextAetSeconds, normalizedNextAetSeconds/3600, (normalizedNextAetSeconds%3600)/60,
                           firstElement.getCoordinate().getLatitude(), firstElement.getCoordinate().getLongitude(),
                           secondElement.getCoordinate().getLatitude(), secondElement.getCoordinate().getLongitude());
            }
            
            segments.add(segment);
        }
        
        logger.debug("Created {} valid segments with normalized AET starting from 0s", segments.size());
        return segments;
    }
    
    /**
     * Creates and configures a SimTrackSimulator instance.
     * Fixed to use proper initialization with actual flight date.
     */
    private SimTrackSimulator createSimulator(Calendar flightStartTime) {
        SimTrackSimulator simulator = new SimTrackSimulator();
        
        // Set flight plan manipulator (required for simulation)
        FlightIntentionManipulatorAdapter manipulator = new FlightIntentionManipulatorAdapter();
        simulator.setFlightPlanManipulator(manipulator);
        
        // Set start date session to match the actual flight date (required for time calculations)
        Calendar startDateSession = (Calendar) flightStartTime.clone();
        // Reset to start of day for session
        startDateSession.set(Calendar.HOUR_OF_DAY, 0);
        startDateSession.set(Calendar.MINUTE, 0);
        startDateSession.set(Calendar.SECOND, 0);
        startDateSession.set(Calendar.MILLISECOND, 0);
        simulator.setStartDateSession(startDateSession);
        
        // Initialize segment auxiliaries map (for performance optimization)
        simulator.setSegmentAuxsByFlightIntentions(new HashMap<>());
        
        logger.debug("SimTrackSimulator created with startDateSession: {} (flight starts at: {})", 
                   startDateSession.getTime(), flightStartTime.getTime());
        
        return simulator;
    }
    
    /**
     * Converts a simulated PathVO to a RouteElement.
     */
    private RouteElement convertToRouteElement(PathVO simulatedTrack, int sequenceNumber) {
        RouteElement element = new RouteElement();
        
        if (simulatedTrack.getKinematic() != null && simulatedTrack.getKinematic().getPosition() != null) {
            CoordinateVO position = simulatedTrack.getKinematic().getPosition();
            element.setLatitude(position.getLatitude());
            element.setLongitude(position.getLongitude());
            element.setAltitude(simulatedTrack.getFlightLevel() * 100.0); // Convert flight level to feet
            
            if (simulatedTrack.getKinematic().getSpeed() > 0) {
                element.setSpeed(simulatedTrack.getKinematic().getSpeed());
            }
        }
        
        element.setSequenceNumber(sequenceNumber);
        element.setElementType("INTERPOLATED"); // Mark as generated point
        
        return element;
    }
    
    /**
     * Calculates flight duration from real flight tracking points.
     */
    private long calculateFlightDuration(JoinedFlightData realFlight) {
        List<TrackingPoint> trackingPoints = realFlight.getTrackingPoints();
        if (trackingPoints.size() < 2) {
            return 3600000; // Default 1 hour if insufficient data
        }
        
        TrackingPoint first = trackingPoints.get(0);
        TrackingPoint last = trackingPoints.get(trackingPoints.size() - 1);
        
        // Assuming TrackingPoint has a timestamp field
        return last.getTimestamp() - first.getTimestamp();
    }
    
    /**
     * Creates a route element using linear interpolation between waypoints when Sigma simulation fails.
     */
    private RouteElement createRouteElementByLinearInterpolation(List<SegmentVO> segments, int simulationTimeSeconds, int pointIndex) {
        // Find which segment this time falls into
        for (SegmentVO segment : segments) {
            int segmentStartTime = segment.getFirst().getAetSeconds();
            int segmentEndTime = segment.getSecond().getAetSeconds();
            
            if (simulationTimeSeconds >= segmentStartTime && simulationTimeSeconds <= segmentEndTime) {
                // Calculate interpolation ratio
                double ratio = 0.0;
                if (segmentEndTime > segmentStartTime) {
                    ratio = (double)(simulationTimeSeconds - segmentStartTime) / (segmentEndTime - segmentStartTime);
                }
                
                // Linear interpolation between segment endpoints
                RouteElementVO first = segment.getFirst();
                RouteElementVO second = segment.getSecond();
                
                double lat = first.getCoordinate().getLatitude() + 
                           ratio * (second.getCoordinate().getLatitude() - first.getCoordinate().getLatitude());
                double lon = first.getCoordinate().getLongitude() + 
                           ratio * (second.getCoordinate().getLongitude() - first.getCoordinate().getLongitude());
                double alt = first.getLevel() + ratio * (second.getLevel() - first.getLevel());
                
                // Create interpolated route element
                RouteElement element = new RouteElement();
                element.setLatitude(lat);
                element.setLongitude(lon);
                element.setLevelMeters(alt * 0.3048); // Convert feet to meters
                element.setEetMinutes(simulationTimeSeconds / 60.0);
                element.setElementType("INTERPOLATED_LINEAR");
                element.setIndicative("INTERP_" + pointIndex);
                element.setInterpolated(true);
                
                return element;
            }
        }
        
        return null; // Time not found in any segment
    }
    private Calendar getFlightStartTime(JoinedFlightData realFlight) {
        Calendar startTime = Calendar.getInstance();
        
        if (!realFlight.getTrackingPoints().isEmpty()) {
            TrackingPoint firstPoint = realFlight.getTrackingPoints().get(0);
            startTime.setTimeInMillis(firstPoint.getTimestamp());
        }
        
        return startTime;
    }
    
    /**
     * Batch densification for multiple flights.
     */
    public List<TrajectoryDensificationResult> densifyMultipleTrajectories(List<Long> planIds) {
        logger.info("Starting batch trajectory densification for {} flights", planIds.size());
        
        List<TrajectoryDensificationResult> results = new ArrayList<>();
        
        for (Long planId : planIds) {
            try {
                TrajectoryDensificationResult result = densifyPredictedTrajectory(planId);
                results.add(result);
            } catch (Exception e) {
                logger.error("Error densifying trajectory for planId: {}", planId, e);
                results.add(TrajectoryDensificationResult.error(planId, e.getMessage()));
            }
        }
        
        logger.info("Completed batch trajectory densification. Processed: {}, Successful: {}", 
                   results.size(), results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());
        
        return results;
    }
}
