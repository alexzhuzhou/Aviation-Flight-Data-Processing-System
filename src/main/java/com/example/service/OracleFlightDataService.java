package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import br.atech.commons.spring.orm.ExtendedHibernateOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Coordinate;

import br.atech.sigma.gfx.session.historical.flight.domain.HistoricFlightIntention;
import br.atech.sigma.gfx.session.historical.flight.domain.HistoricRoute;
import br.atech.sigma.gfx.session.historical.flight.domain.HistoricExtractionRouteSegment;
import br.atech.sigma.gfx.session.historical.flight.domain.HistoricExtractionRouteElement;

/**
 * Service for extracting flight data from Oracle database
 * 
 * Uses the EXACT same logic as SimpleHibernateTest:
 * - Same JPQL queries with JOIN FETCH
 * - Same JSON building logic
 * - Same coordinate extraction
 * - Same error handling
 * 
 * Uses ExtendedHibernateOperations for better serialization error handling
 * with downgraded Hibernate version for compatibility
 */
@Service
public class OracleFlightDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(OracleFlightDataService.class);
    
    @Autowired
    @Qualifier("hibernateOps")
    private ExtendedHibernateOperations hibernateOps;
    
    /**
     * Extract flight data for a single planId
     * Uses EXACT same approach as SimpleHibernateTest
     */
    @Transactional(transactionManager = "oracleTransactionManager", readOnly = true)
    public Map<String, Object> extractFlightData(Long planId) {
        try {
            logger.debug("Extracting flight data for planId: {}", planId);
            
            // Use EXACT same approach as SimpleHibernateTest with additional fetch for routeElements
            String jpql = "SELECT hfi FROM HistoricFlightIntention hfi " +
                         "LEFT JOIN FETCH hfi.sessionRoute sr " +
                         "LEFT JOIN FETCH sr.routeSegments " +
                         "LEFT JOIN FETCH sr.routeElements " +
                         "WHERE hfi.instanceId = ?";
            
            System.out.println("Query: " + jpql);
            System.out.println("Parameter: " + planId);
            
            // Use ExtendedHibernateOperations exactly like SimpleHibernateTest
            List<HistoricFlightIntention> result = (List<HistoricFlightIntention>) hibernateOps.find(jpql, planId);
            
            logger.debug("Query executed successfully, result size: {}", result.size());
            HistoricFlightIntention flightIntention = result.isEmpty() ? null : result.get(0);
            
            if (flightIntention != null) {
                logger.debug("Successfully loaded flight intention: {}", flightIntention.getIndicative());
                logger.debug("Instance ID: {}", flightIntention.getInstanceId());
                logger.debug("Primary Key: {}", flightIntention.getId());
                
                // Test accessing the lazy-loaded collections (same as SimpleHibernateTest)
                try {
                    if (flightIntention.getExtractionRoute() != null) {
                        logger.debug("Extraction route loaded: {}", flightIntention.getExtractionRoute().getClass().getSimpleName());
                        
                        if (flightIntention.getExtractionRoute().getRouteSegments() != null) {
                            logger.debug("Route segments loaded: {} segments", flightIntention.getExtractionRoute().getRouteSegments().size());
                        }
                    }
                } catch (Exception lazyLoadException) {
                    logger.warn("Error loading lazy collections for planId {} - this may be due to serialization issues: {}", 
                               planId, lazyLoadException.getMessage());
                    // Check if this is a serialization error
                    Throwable current = lazyLoadException;
                    while (current != null) {
                        if (current instanceof org.hibernate.type.SerializationException ||
                            (current.getMessage() != null && current.getMessage().contains("could not deserialize")) ||
                            current instanceof java.io.StreamCorruptedException) {
                            logger.warn("Serialization error detected during lazy loading for planId {} - skipping: {}", planId, current.getMessage());
                            return null; // Treat as not found
                        }
                        current = current.getCause();
                    }
                    // If not a serialization error, rethrow
                    throw lazyLoadException;
                }
                
                // EXACT same JSON building as SimpleHibernateTest
                Map<String, Object> flightData = buildFlightIntentionJson(flightIntention);
                logger.debug("Successfully extracted flight data for planId: {}", planId);
                return flightData;
            } else {
                logger.debug("No flight intention found with instanceId: {}", planId);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error in JPQL approach for planId {}: {}", planId, e.getMessage());
            logger.error("Root cause: {}", (e.getCause() != null ? e.getCause().getMessage() : "No cause available"));
            
            // Handle serialization errors gracefully
            Throwable current = e;
            while (current != null) {
                if (current instanceof org.hibernate.type.SerializationException ||
                    (current.getMessage() != null && current.getMessage().contains("could not deserialize")) ||
                    current instanceof java.io.StreamCorruptedException) {
                    logger.warn("Serialization error detected for planId {} - skipping: {}", planId, current.getMessage());
                    return null; // Treat as not found
                }
                current = current.getCause();
            }
            
            throw new RuntimeException("Failed to extract flight data for planId: " + planId, e);
        }
    }
    
    /**
     * Extract flight data for multiple planIds
     * Fixed approach: Use smaller chunks and avoid connection exhaustion
     */
    public OracleExtractionResult extractFlightDataBatch(List<Long> planIds) {
        logger.info("Starting batch extraction for {} planIds", planIds.size());
        
        OracleExtractionResult result = new OracleExtractionResult();
        result.setTotalRequested(planIds.size());
        
        List<Map<String, Object>> extractedFlights = new ArrayList<>();
        List<Long> notFoundPlanIds = new ArrayList<>();
        List<Long> errorPlanIds = new ArrayList<>();
        
        // Process individually to avoid connection issues
        // This is safer than trying to optimize with IN clauses for large batches
        for (Long planId : planIds) {
            try {
                // Use the existing single extraction method which works well
                Map<String, Object> flightData = extractFlightData(planId);
                
                if (flightData != null) {
                    extractedFlights.add(flightData);
                    logger.debug("Successfully extracted flight data for planId: {}", planId);
                } else {
                    notFoundPlanIds.add(planId);
                    logger.debug("No flight intention found for planId: {}", planId);
                }
                
            } catch (Exception e) {
                errorPlanIds.add(planId);
                logger.error("Error extracting flight data for planId {}: {}", planId, e.getMessage());
            }
            
            // Add small delay every 10 requests to prevent overwhelming the database
            if (extractedFlights.size() % 10 == 0 && extractedFlights.size() > 0) {
                try {
                    Thread.sleep(50); // 50ms delay every 10 requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Batch processing interrupted");
                    break;
                }
            }
        }
        
        result.setExtractedFlights(extractedFlights);
        result.setNotFoundPlanIds(notFoundPlanIds);
        result.setErrorPlanIds(errorPlanIds);
        result.setTotalExtracted(extractedFlights.size());
        result.setTotalNotFound(notFoundPlanIds.size());
        result.setTotalErrors(errorPlanIds.size());
        
        logger.info("Batch extraction complete: {}/{} extracted, {} not found, {} errors", 
                   extractedFlights.size(), planIds.size(), notFoundPlanIds.size(), errorPlanIds.size());
        
        return result;
    }
    
    /**
     * Build JSON representation of flight intention data
     * This is EXACTLY the same logic from SimpleHibernateTest - no changes!
     */
    private Map<String, Object> buildFlightIntentionJson(HistoricFlightIntention flight) {
        Map<String, Object> flightData = new HashMap<>();
        
        // Flight intention basic data
        flightData.put("id", flight.getId());
        flightData.put("instanceId", flight.getInstanceId());
        flightData.put("indicative", flight.getIndicative());
        flightData.put("time", flight.getTime() != null ? flight.getTime().toString() : null);
        
        // Merge extraction route data directly into flight data
        if (flight.getExtractionRoute() != null) {
            HistoricRoute route = flight.getExtractionRoute();
            
            // Route basic data (rename id to routeId to avoid conflict)
            flightData.put("routeId", route.getId());
            flightData.put("startPointIndicative", route.getStartPointIndicative());
            flightData.put("endPointIndicative", route.getEndPointIndicative());
            flightData.put("distance", route.getDistance() != null ? route.getDistance().getValue() : null);
            
            // Route segments
            List<Map<String, Object>> segments = new ArrayList<>();
            if (route.getRouteSegments() != null && !route.getRouteSegments().isEmpty()) {
                for (Object segmentObj : route.getRouteSegments()) {
                    if (segmentObj instanceof HistoricExtractionRouteSegment) {
                        HistoricExtractionRouteSegment segment = (HistoricExtractionRouteSegment) segmentObj;
                        segments.add(buildRouteSegmentJson(segment));
                    }
                }
            }
            flightData.put("routeSegments", segments);
            
            // Route elements
            List<Map<String, Object>> elements = new ArrayList<>();
            if (route.getRouteElements() != null && !route.getRouteElements().isEmpty()) {
                for (Object elementObj : route.getRouteElements()) {
                    if (elementObj instanceof HistoricExtractionRouteElement) {
                        HistoricExtractionRouteElement element = (HistoricExtractionRouteElement) elementObj;
                        elements.add(buildRouteElementJson(element));
                    }
                }
            }
            flightData.put("routeElements", elements);
            
        } else {
            // No extraction route available
            flightData.put("routeId", null);
            flightData.put("startPointIndicative", null);
            flightData.put("endPointIndicative", null);
            flightData.put("distance", null);
            flightData.put("routeSegments", new ArrayList<>());
            flightData.put("routeElements", new ArrayList<>());
        }
        
        return flightData;
    }
    
    /**
     * Build JSON representation of route segment
     * EXACTLY the same as SimpleHibernateTest
     */
    private Map<String, Object> buildRouteSegmentJson(HistoricExtractionRouteSegment segment) {
        Map<String, Object> segmentData = new HashMap<>();
        
        segmentData.put("id", segment.getId());
        segmentData.put("distance", segment.getDistance() != null ? segment.getDistance().getValue() : null);
        
        // Element A (start point) - only ID
        if (segment.getElementA() != null) {
            segmentData.put("elementAId", segment.getElementA().getId());
        } else {
            segmentData.put("elementAId", null);
        }
        
        // Element B (end point) - only ID
        if (segment.getElementB() != null) {
            segmentData.put("elementBId", segment.getElementB().getId());
        } else {
            segmentData.put("elementBId", null);
        }
        
        return segmentData;
    }
    
    /**
     * Build JSON representation of route element
     * EXACTLY the same as SimpleHibernateTest
     */
    private Map<String, Object> buildRouteElementJson(HistoricExtractionRouteElement element) {
        Map<String, Object> elementData = new HashMap<>();
        
        // Basic element data
        elementData.put("id", element.getId());
        elementData.put("indicative", element.getIndicative());
        
        // Add coordinate data directly to element
        Map<String, Object> coordinates = extractElementCoordinates(element);
        elementData.putAll(coordinates);
        
        // Add flight data directly to element
        if (element.getLevel() != null) {
            elementData.put("levelMeters", element.getLevel().getValue());
        }
        if (element.getSpeed() != null) {
            elementData.put("speedMeterPerSecond", element.getSpeed().getValue());
        }
        if (element.getEetMinutes() != null) {
            elementData.put("eetMinutes", element.getEetMinutes().getValue());
        }
        if (element.getElementType() != null) {
            elementData.put("elementType", element.getElementType().toString());
        }
        
        return elementData;
    }
    
    /**
     * Extract coordinates from route element
     * EXACTLY the same as SimpleHibernateTest
     */
    private Map<String, Object> extractElementCoordinates(HistoricExtractionRouteElement element) {
        Map<String, Object> coordinates = new HashMap<>();
        
        try {
            // Primary coordinate source: JTS Geometry
            if (element.getGeometry() != null) {
                Coordinate coord = element.getGeometry().getCoordinate();
                coordinates.put("latitude", coord.y);
                coordinates.put("longitude", coord.x);
                if (!Double.isNaN(coord.z)) {
                    coordinates.put("altitudeGeometry", coord.z);
                }
            } else {
                coordinates.put("latitude", null);
                coordinates.put("longitude", null);
            }
            
            // Additional coordinate representations
            String coordText = element.getCoordinateAsText();
            if (coordText != null && !coordText.isEmpty()) {
                coordinates.put("coordinateText", coordText);
            }
            
            if (element.getCoordinate() != null) {
                coordinates.put("geographicCoordinate", element.getCoordinate().toString());
            }
            
        } catch (Exception e) {
            logger.warn("Error extracting coordinates for element {}: {}", element.getId(), e.getMessage());
            coordinates.put("error", "Error extracting coordinates: " + e.getMessage());
            coordinates.put("latitude", null);
            coordinates.put("longitude", null);
        }
        
        return coordinates;
    }
    
    /**
     * Result class for batch extraction operations
     */
    public static class OracleExtractionResult {
        private int totalRequested;
        private int totalExtracted;
        private int totalNotFound;
        private int totalErrors;
        private List<Map<String, Object>> extractedFlights;
        private List<Long> notFoundPlanIds;
        private List<Long> errorPlanIds;
        
        // Getters and setters
        public int getTotalRequested() { return totalRequested; }
        public void setTotalRequested(int totalRequested) { this.totalRequested = totalRequested; }
        
        public int getTotalExtracted() { return totalExtracted; }
        public void setTotalExtracted(int totalExtracted) { this.totalExtracted = totalExtracted; }
        
        public int getTotalNotFound() { return totalNotFound; }
        public void setTotalNotFound(int totalNotFound) { this.totalNotFound = totalNotFound; }
        
        public int getTotalErrors() { return totalErrors; }
        public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }
        
        public List<Map<String, Object>> getExtractedFlights() { return extractedFlights; }
        public void setExtractedFlights(List<Map<String, Object>> extractedFlights) { this.extractedFlights = extractedFlights; }
        
        public List<Long> getNotFoundPlanIds() { return notFoundPlanIds; }
        public void setNotFoundPlanIds(List<Long> notFoundPlanIds) { this.notFoundPlanIds = notFoundPlanIds; }
        
        public List<Long> getErrorPlanIds() { return errorPlanIds; }
        public void setErrorPlanIds(List<Long> errorPlanIds) { this.errorPlanIds = errorPlanIds; }
    }
}
