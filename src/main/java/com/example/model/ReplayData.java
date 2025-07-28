package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the complete structure of a static replay.json file
 * 
 * This model is used for:
 * - Loading data from static JSON files (batch processing)
 * - File-based data analysis and testing
 * - One-time data import operations
 * 
 * Contains all flight intentions and tracking points from a single file.
 * Used primarily by ReplayDataService for file processing.
 */
public class ReplayData {
    
    @JsonProperty("listRealPath")
    private List<RealPathPoint> listRealPath;
    
    @JsonProperty("listFlightIntention")
    private List<FlightIntention> listFlightIntention;
    
    @JsonProperty("time")
    private String time;
    
    // Constructors
    public ReplayData() {}
    
    public ReplayData(List<RealPathPoint> listRealPath, List<FlightIntention> listFlightIntention, String time) {
        this.listRealPath = listRealPath;
        this.listFlightIntention = listFlightIntention;
        this.time = time;
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
    
    @Override
    public String toString() {
        return "ReplayData{" +
                "realPathPoints=" + (listRealPath != null ? listRealPath.size() : 0) +
                ", flightIntentions=" + (listFlightIntention != null ? listFlightIntention.size() : 0) +
                ", time=" + time +
                '}';
    }
} 