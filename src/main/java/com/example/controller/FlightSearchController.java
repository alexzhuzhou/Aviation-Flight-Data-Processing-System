package com.example.controller;

import com.example.model.JoinedFlightData;
import com.example.model.PredictedFlightData;
import com.example.service.FlightSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST controller for flight search and management operations
 */
@RestController
@RequestMapping("/api/flight-search")
@CrossOrigin(origins = "*")
public class FlightSearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightSearchController.class);
    
    @Autowired
    private FlightSearchService flightSearchService;
    
    /**
     * Search flights by planId with partial matching
     */
    @GetMapping("/by-plan-id")
    public ResponseEntity<Map<String, Object>> searchByPlanId(@RequestParam String query) {
        try {
            logger.info("Searching flights by planId with query: {}", query);
            
            List<JoinedFlightData> realFlights = flightSearchService.searchRealFlightsByPlanId(query);
            List<PredictedFlightData> predictedFlights = flightSearchService.searchPredictedFlightsByInstanceId(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("realFlights", realFlights);
            response.put("predictedFlights", predictedFlights);
            response.put("totalReal", realFlights.size());
            response.put("totalPredicted", predictedFlights.size());
            response.put("searchType", "planId");
            response.put("query", query);
            
            logger.info("Found {} real flights and {} predicted flights for planId query: {}", 
                realFlights.size(), predictedFlights.size(), query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching by planId: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
    
    /**
     * Search flights by indicative with partial matching
     */
    @GetMapping("/by-indicative")
    public ResponseEntity<Map<String, Object>> searchByIndicative(@RequestParam String query) {
        try {
            logger.info("Searching flights by indicative with query: {}", query);
            
            List<JoinedFlightData> realFlights = flightSearchService.searchRealFlightsByIndicative(query);
            List<PredictedFlightData> predictedFlights = flightSearchService.searchPredictedFlightsByIndicative(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("realFlights", realFlights);
            response.put("predictedFlights", predictedFlights);
            response.put("totalReal", realFlights.size());
            response.put("totalPredicted", predictedFlights.size());
            response.put("searchType", "indicative");
            response.put("query", query);
            
            logger.info("Found {} real flights and {} predicted flights for indicative query: {}", 
                realFlights.size(), predictedFlights.size(), query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching by indicative: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
    
    /**
     * Search flights by origin airport with partial matching
     */
    @GetMapping("/by-origin")
    public ResponseEntity<Map<String, Object>> searchByOrigin(@RequestParam String query) {
        try {
            logger.info("Searching flights by origin airport with query: {}", query);
            
            List<JoinedFlightData> realFlights = flightSearchService.searchRealFlightsByOrigin(query);
            List<PredictedFlightData> predictedFlights = flightSearchService.searchPredictedFlightsByOrigin(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("realFlights", realFlights);
            response.put("predictedFlights", predictedFlights);
            response.put("totalReal", realFlights.size());
            response.put("totalPredicted", predictedFlights.size());
            response.put("searchType", "origin");
            response.put("query", query);
            
            logger.info("Found {} real flights and {} predicted flights for origin query: {}", 
                realFlights.size(), predictedFlights.size(), query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching by origin: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
    
    /**
     * Search flights by destination airport with partial matching
     */
    @GetMapping("/by-destination")
    public ResponseEntity<Map<String, Object>> searchByDestination(@RequestParam String query) {
        try {
            logger.info("Searching flights by destination airport with query: {}", query);
            
            List<JoinedFlightData> realFlights = flightSearchService.searchRealFlightsByDestination(query);
            List<PredictedFlightData> predictedFlights = flightSearchService.searchPredictedFlightsByDestination(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("realFlights", realFlights);
            response.put("predictedFlights", predictedFlights);
            response.put("totalReal", realFlights.size());
            response.put("totalPredicted", predictedFlights.size());
            response.put("searchType", "destination");
            response.put("query", query);
            
            logger.info("Found {} real flights and {} predicted flights for destination query: {}", 
                realFlights.size(), predictedFlights.size(), query);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching by destination: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get flight details by exact planId
     */
    @GetMapping("/details/{planId}")
    public ResponseEntity<Map<String, Object>> getFlightDetails(@PathVariable Long planId) {
        try {
            logger.info("Getting flight details for planId: {}", planId);
            
            JoinedFlightData realFlight = flightSearchService.getRealFlightByPlanId(planId);
            PredictedFlightData predictedFlight = flightSearchService.getPredictedFlightByInstanceId(planId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("realFlight", realFlight);
            response.put("predictedFlight", predictedFlight);
            response.put("planId", planId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting flight details: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get flight details: " + e.getMessage()));
        }
    }
    
    /**
     * Delete real flight by planId
     */
    @DeleteMapping("/real/{planId}")
    public ResponseEntity<Map<String, Object>> deleteRealFlight(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "false") boolean deleteMatching) {
        try {
            logger.info("Deleting real flight with planId: {}, deleteMatching: {}", planId, deleteMatching);
            
            boolean realDeleted = flightSearchService.deleteRealFlight(planId);
            boolean predictedDeleted = false;
            
            if (deleteMatching) {
                predictedDeleted = flightSearchService.deletePredictedFlight(planId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("realFlightDeleted", realDeleted);
            response.put("predictedFlightDeleted", predictedDeleted);
            response.put("planId", planId);
            response.put("message", String.format("Real flight %s. Predicted flight %s.", 
                realDeleted ? "deleted" : "not found",
                deleteMatching ? (predictedDeleted ? "deleted" : "not found") : "not deleted"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting real flight: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }
    
    /**
     * Delete predicted flight by instanceId
     */
    @DeleteMapping("/predicted/{instanceId}")
    public ResponseEntity<Map<String, Object>> deletePredictedFlight(
            @PathVariable Long instanceId,
            @RequestParam(defaultValue = "false") boolean deleteMatching) {
        try {
            logger.info("Deleting predicted flight with instanceId: {}, deleteMatching: {}", instanceId, deleteMatching);
            
            boolean predictedDeleted = flightSearchService.deletePredictedFlight(instanceId);
            boolean realDeleted = false;
            
            if (deleteMatching) {
                realDeleted = flightSearchService.deleteRealFlight(instanceId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("predictedFlightDeleted", predictedDeleted);
            response.put("realFlightDeleted", realDeleted);
            response.put("instanceId", instanceId);
            response.put("message", String.format("Predicted flight %s. Real flight %s.", 
                predictedDeleted ? "deleted" : "not found",
                deleteMatching ? (realDeleted ? "deleted" : "not found") : "not deleted"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting predicted flight: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }
    
    /**
     * Bulk delete flights
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkDeleteFlights(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> realFlightIds = (List<Long>) request.get("realFlightIds");
            @SuppressWarnings("unchecked")
            List<Long> predictedFlightIds = (List<Long>) request.get("predictedFlightIds");
            boolean deleteMatching = (Boolean) request.getOrDefault("deleteMatching", false);
            
            logger.info("Bulk deleting {} real flights and {} predicted flights, deleteMatching: {}", 
                realFlightIds != null ? realFlightIds.size() : 0,
                predictedFlightIds != null ? predictedFlightIds.size() : 0,
                deleteMatching);
            
            Map<String, Object> result = flightSearchService.bulkDeleteFlights(realFlightIds, predictedFlightIds, deleteMatching);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error in bulk delete: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Bulk delete failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get search statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSearchStats() {
        try {
            Map<String, Object> stats = flightSearchService.getSearchStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting search stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get stats: " + e.getMessage()));
        }
    }
}
