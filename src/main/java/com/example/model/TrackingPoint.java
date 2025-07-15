package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a single tracking point extracted from RealPathPoint
 * Contains only the essential tracking information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingPoint {
    
    // Core tracking fields
    private long planId;
    private String ssrRegistration;
    private int transponderCode;
    private String indicativeSafe;
    private int flightLevel;
    private int speed;
    private double latitude;
    private double longitude;
    private float speedBearing;
    
    // Additional useful fields
    private int seqNum;
    private String detectorSource;
    private boolean simulating;
    private long timestamp; // Can be derived from durationSinceFirstTrack or other time fields
    
    // Constructors
    public TrackingPoint() {}
    
    public TrackingPoint(RealPathPoint realPathPoint) {
        if (realPathPoint != null) {
            this.planId = realPathPoint.getPlanId();
            this.indicativeSafe = realPathPoint.getIndicativeSafe();
            this.flightLevel = realPathPoint.getFlightLevel();
            this.seqNum = realPathPoint.getSeqNum();
            this.simulating = realPathPoint.isSimulating();
            
            // Extract SSR information
            if (realPathPoint.getSsr() != null) {
                this.ssrRegistration = realPathPoint.getSsr().getRegistration();
                if (realPathPoint.getSsr().getTransponder() != null) {
                    this.transponderCode = realPathPoint.getSsr().getTransponder().getCode();
                }
            }
            
            // Extract kinematic information
            if (realPathPoint.getKinematic() != null) {
                this.speed = realPathPoint.getKinematic().getSpeed();
                this.speedBearing = realPathPoint.getKinematic().getSpeedBearing();
                this.detectorSource = realPathPoint.getKinematic().getDetectorSource();
                
                // Extract position
                if (realPathPoint.getKinematic().getPosition() != null) {
                    this.latitude = realPathPoint.getKinematic().getPosition().getLatitude();
                    this.longitude = realPathPoint.getKinematic().getPosition().getLongitude();
                }
            }
        }
    }
    
    // Getters and Setters
    public long getPlanId() { return planId; }
    public void setPlanId(long planId) { this.planId = planId; }
    
    public String getSsrRegistration() { return ssrRegistration; }
    public void setSsrRegistration(String ssrRegistration) { this.ssrRegistration = ssrRegistration; }
    
    public int getTransponderCode() { return transponderCode; }
    public void setTransponderCode(int transponderCode) { this.transponderCode = transponderCode; }
    
    public String getIndicativeSafe() { return indicativeSafe; }
    public void setIndicativeSafe(String indicativeSafe) { this.indicativeSafe = indicativeSafe; }
    
    public int getFlightLevel() { return flightLevel; }
    public void setFlightLevel(int flightLevel) { this.flightLevel = flightLevel; }
    
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public float getSpeedBearing() { return speedBearing; }
    public void setSpeedBearing(float speedBearing) { this.speedBearing = speedBearing; }
    
    public int getSeqNum() { return seqNum; }
    public void setSeqNum(int seqNum) { this.seqNum = seqNum; }
    
    public String getDetectorSource() { return detectorSource; }
    public void setDetectorSource(String detectorSource) { this.detectorSource = detectorSource; }
    
    public boolean isSimulating() { return simulating; }
    public void setSimulating(boolean simulating) { this.simulating = simulating; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "TrackingPoint{" +
                "indicativeSafe='" + indicativeSafe + '\'' +
                ", flightLevel=" + flightLevel +
                ", speed=" + speed +
                ", position=[" + latitude + "," + longitude + "]" +
                ", seqNum=" + seqNum +
                ", detectorSource='" + detectorSource + '\'' +
                '}';
    }
} 