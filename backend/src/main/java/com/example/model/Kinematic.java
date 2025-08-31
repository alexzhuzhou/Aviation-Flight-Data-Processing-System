package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents kinematic data including position, speed, and tracking information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Kinematic {
    
    private int kinematicData;
    private String trackingType;
    private String trackingNature;
    private String detectorSource;
    private String pathType;
    private Position position;
    private int speed;
    private float speedBearing;
    
    // Constructors
    public Kinematic() {}
    
    // Getters and Setters
    public int getKinematicData() { return kinematicData; }
    public void setKinematicData(int kinematicData) { this.kinematicData = kinematicData; }
    
    public String getTrackingType() { return trackingType; }
    public void setTrackingType(String trackingType) { this.trackingType = trackingType; }
    
    public String getTrackingNature() { return trackingNature; }
    public void setTrackingNature(String trackingNature) { this.trackingNature = trackingNature; }
    
    public String getDetectorSource() { return detectorSource; }
    public void setDetectorSource(String detectorSource) { this.detectorSource = detectorSource; }
    
    public String getPathType() { return pathType; }
    public void setPathType(String pathType) { this.pathType = pathType; }
    
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    
    public float getSpeedBearing() { return speedBearing; }
    public void setSpeedBearing(float speedBearing) { this.speedBearing = speedBearing; }
    
    @Override
    public String toString() {
        return "Kinematic{" +
                "detectorSource='" + detectorSource + '\'' +
                ", pathType='" + pathType + '\'' +
                ", position=" + position +
                ", speed=" + speed +
                ", speedBearing=" + speedBearing +
                '}';
    }
    
    // Nested Position class
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        private double latitude;
        private double longitude;
        private String name;
        private Coordinate coordinate;
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Coordinate getCoordinate() { return coordinate; }
        public void setCoordinate(Coordinate coordinate) { this.coordinate = coordinate; }
        
        @Override
        public String toString() {
            return "Position{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
        
        // Nested Coordinate class
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Coordinate {
            private double x;
            private double y;
            private String z;
            
            public double getX() { return x; }
            public void setX(double x) { this.x = x; }
            
            public double getY() { return y; }
            public void setY(double y) { this.y = y; }
            
            public String getZ() { return z; }
            public void setZ(String z) { this.z = z; }
            
            @Override
            public String toString() {
                return "Coordinate{" +
                        "x=" + x +
                        ", y=" + y +
                        ", z='" + z + '\'' +
                        '}';
            }
        }
    }
} 