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
        this(realPathPoint, null);
    }
    
    public TrackingPoint(RealPathPoint realPathPoint, String packetTimestamp) {
        if (realPathPoint != null) {
            this.planId = realPathPoint.getPlanId();
            this.indicativeSafe = realPathPoint.getIndicativeSafe();
            this.flightLevel = realPathPoint.getFlightLevel();
            this.seqNum = realPathPoint.getSeqNum();
            this.simulating = realPathPoint.isSimulating();
            
            // Set the packet timestamp when this tracking point was received
            if (packetTimestamp != null) {
                this.timestamp = parseTimestampToLong(packetTimestamp);
            }
            
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
    
    /**
     * Helper method to parse timestamp string to long
     * Handles various timestamp formats including ISO 8601
     */
    private long parseTimestampToLong(String timestampStr) {
        try {
            // Try to parse as ISO 8601 format: "2025-07-11T00:00:57.288+0000"
            if (timestampStr.contains("T")) {
                // Handle +0000 format by converting to Z for proper parsing
                String normalized = timestampStr.replaceAll("([+-])(\\d{2})(\\d{2})$", "$1$2:$3");
                if (normalized.endsWith("+00:00")) {
                    normalized = normalized.replace("+00:00", "Z");
                }
                return java.time.Instant.parse(normalized).toEpochMilli();
            }
            // Try to parse as long (Unix timestamp)
            return Long.parseLong(timestampStr);
        } catch (Exception e) {
            // Log the parsing error for debugging
            System.err.println("Failed to parse timestamp: " + timestampStr + ", error: " + e.getMessage());
            // If parsing fails, return current timestamp as fallback
            return System.currentTimeMillis();
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