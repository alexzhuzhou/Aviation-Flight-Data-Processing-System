package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a single streaming packet from the real-time flight data system
 * 
 * This model is used for:
 * - Processing real-time streaming data from external systems
 * - REST API endpoints that receive live flight data
 * - Continuous data processing in production
 * 
 * Contains packetStoredTimestamp for tracking when the packet was received by the external system.
 * 
 * Used primarily by StreamingFlightService for real-time processing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplayPath {
    
    @JsonProperty("listRealPath")
    private List<RealPathPoint> listRealPath;
    
    @JsonProperty("listFlightIntention") 
    private List<FlightIntention> listFlightIntention;
    
    @JsonProperty("time")
    private String time;
    
    // Timestamp when this packet was stored in the original system
    @JsonProperty("packetStoredTimestamp")
    private String packetStoredTimestamp;
    
    // Constructors
    public ReplayPath() {}
    
    public ReplayPath(List<RealPathPoint> listRealPath, List<FlightIntention> listFlightIntention, String time) {
        this.listRealPath = listRealPath;
        this.listFlightIntention = listFlightIntention;
        this.time = time;
    }
    
    public ReplayPath(List<RealPathPoint> listRealPath, List<FlightIntention> listFlightIntention, String time, String packetStoredTimestamp) {
        this.listRealPath = listRealPath;
        this.listFlightIntention = listFlightIntention;
        this.time = time;
        this.packetStoredTimestamp = packetStoredTimestamp;
    }
    
    // Getters and Setters
    public List<RealPathPoint> getListRealPath() {
        return listRealPath;
    }
    
    public void setListRealPath(List<RealPathPoint> listRealPath) {
        this.listRealPath = listRealPath;
    }
    
    public List<FlightIntention> getListFlightIntention() {
        return listFlightIntention;
    }
    
    public void setListFlightIntention(List<FlightIntention> listFlightIntention) {
        this.listFlightIntention = listFlightIntention;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getPacketStoredTimestamp() {
        return packetStoredTimestamp;
    }
    
    public void setPacketStoredTimestamp(String packetStoredTimestamp) {
        this.packetStoredTimestamp = packetStoredTimestamp;
    }
    
    @Override
    public String toString() {
        return "ReplayPath{" +
                "realPathPoints=" + (listRealPath != null ? listRealPath.size() : 0) +
                ", flightIntentions=" + (listFlightIntention != null ? listFlightIntention.size() : 0) +
                ", time=" + time +
                ", packetStoredTimestamp=" + packetStoredTimestamp +
                '}';
    }
} 