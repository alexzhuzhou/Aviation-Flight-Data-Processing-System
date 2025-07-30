package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a segment between two route elements in a predicted flight path
 * Contains distance and connecting element information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteSegment {
    
    private long elementBId;
    private long elementAId;
    private double distance;
    private long id;
    
    // Default constructor
    public RouteSegment() {}
    
    // Constructor with all fields
    public RouteSegment(long elementBId, long elementAId, double distance, long id) {
        this.elementBId = elementBId;
        this.elementAId = elementAId;
        this.distance = distance;
        this.id = id;
    }
    
    // Getters and setters
    public long getElementBId() { return elementBId; }
    public void setElementBId(long elementBId) { this.elementBId = elementBId; }
    
    public long getElementAId() { return elementAId; }
    public void setElementAId(long elementAId) { this.elementAId = elementAId; }
    
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    @Override
    public String toString() {
        return "RouteSegment{" +
                "elementBId=" + elementBId +
                ", elementAId=" + elementAId +
                ", distance=" + distance +
                ", id=" + id +
                '}';
    }
}