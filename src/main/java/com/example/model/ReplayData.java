package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Main data model representing the entire replay.json structure
 */
public class ReplayData {
    
    @JsonProperty("listRealPath")
    private List<RealPathPoint> listRealPath;
    
    @JsonProperty("listFlightIntention")
    private List<FlightIntention> listFlightIntention;
    
    @JsonProperty("time")
    private long time;
    
    // Constructors
    public ReplayData() {}
    
    public ReplayData(List<RealPathPoint> listRealPath, List<FlightIntention> listFlightIntention, long time) {
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
    
    public long getTime() {
        return time;
    }
    
    public void setTime(long time) {
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