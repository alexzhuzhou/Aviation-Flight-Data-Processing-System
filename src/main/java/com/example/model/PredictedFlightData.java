package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.List;

/**
 * MongoDB document representing predicted flight information
 * 
 * This model is used for:
 * - Storing predicted flight route and timing data in MongoDB
 * - Comparing with actual flight data using planId matching
 * - Analyzing prediction accuracy vs real flight performance
 * 
 * Key features:
 * - Contains complete route prediction with elements and segments
 * - Indexed by planId for efficient matching with actual flights
 * - Stored in separate 'predicted_flights' collection
 * - Includes timing predictions and route planning data
 */
@Document(collection = "predicted_flights")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictedFlightData {
    
    @Id
    private String id; // MongoDB will auto-generate this
    
    private long instanceId;
    private long routeId;
    private Double distance; // Can be null
    private List<RouteElement> routeElements;
    
    // Note: This 'id' field from JSON will be mapped to planId for matching with actual flights
    @Indexed
    private long planId; // This corresponds to the 'id' field in the JSON (e.g., 51637804)
    
    private String indicative;
    private String time;
    private String startPointIndicative;
    private String endPointIndicative;
    private List<RouteSegment> routeSegments;
    
    // Default constructor
    public PredictedFlightData() {}
    
    // Constructor with main fields
    public PredictedFlightData(long instanceId, long routeId, Double distance, 
                              List<RouteElement> routeElements, long planId, String indicative, 
                              String time, String startPointIndicative, String endPointIndicative, 
                              List<RouteSegment> routeSegments) {
        this.instanceId = instanceId;
        this.routeId = routeId;
        this.distance = distance;
        this.routeElements = routeElements;
        this.planId = planId;
        this.indicative = indicative;
        this.time = time;
        this.startPointIndicative = startPointIndicative;
        this.endPointIndicative = endPointIndicative;
        this.routeSegments = routeSegments;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public long getInstanceId() { return instanceId; }
    public void setInstanceId(long instanceId) { this.instanceId = instanceId; }
    
    public long getRouteId() { return routeId; }
    public void setRouteId(long routeId) { this.routeId = routeId; }
    
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    
    public List<RouteElement> getRouteElements() { return routeElements; }
    public void setRouteElements(List<RouteElement> routeElements) { this.routeElements = routeElements; }
    
    public long getPlanId() { return planId; }
    public void setPlanId(long planId) { this.planId = planId; }
    
    public String getIndicative() { return indicative; }
    public void setIndicative(String indicative) { this.indicative = indicative; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public String getStartPointIndicative() { return startPointIndicative; }
    public void setStartPointIndicative(String startPointIndicative) { this.startPointIndicative = startPointIndicative; }
    
    public String getEndPointIndicative() { return endPointIndicative; }
    public void setEndPointIndicative(String endPointIndicative) { this.endPointIndicative = endPointIndicative; }
    
    public List<RouteSegment> getRouteSegments() { return routeSegments; }
    public void setRouteSegments(List<RouteSegment> routeSegments) { this.routeSegments = routeSegments; }
    
    @Override
    public String toString() {
        return "PredictedFlightData{" +
                "id='" + id + '\'' +
                ", instanceId=" + instanceId +
                ", routeId=" + routeId +
                ", distance=" + distance +
                ", planId=" + planId +
                ", indicative='" + indicative + '\'' +
                ", time='" + time + '\'' +
                ", startPointIndicative='" + startPointIndicative + '\'' +
                ", endPointIndicative='" + endPointIndicative + '\'' +
                ", routeElementsCount=" + (routeElements != null ? routeElements.size() : 0) +
                ", routeSegmentsCount=" + (routeSegments != null ? routeSegments.size() : 0) +
                '}';
    }
}