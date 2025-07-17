package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.List;

/**
 * Joined flight data structure optimized for MongoDB
 * Contains flight intention data with embedded tracking points
 */
@Document(collection = "flights")
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinedFlightData {
    
    @Id
    private String id; // MongoDB will auto-generate this
    
    // Flight intention fields 
    @Indexed(unique = true) // planId is now the unique business identifier
    private long planId;
    
    private String indicative; // Call sign, no longer unique constraint
    
    private String eobt;
    private String eta;
    private String startDate;
    private String endDate;
    private float eetMinute;
    private String ssrCode;
    private long trackId;
    private String aircraftType;
    private String airline;
    private String flightPlanDate;
    private String currentDateTimeOfArrival;
    private boolean finished;
    private String startPointIndicative;
    private String endPointIndicative;
    private String cruiseLevel;
    private String cruiseSpeed;
    private String simpleRouteIndicative;
    private long routeId;
    private String wakeTurbulence;
    private String aircraftEquipmentName;
    
    // Embedded tracking points for this flight
    private List<TrackingPoint> trackingPoints;
    
    // Metadata
    private int totalTrackingPoints;
    private boolean hasTrackingData;
    
    // Constructors
    public JoinedFlightData() {}
    
    public JoinedFlightData(FlightIntention flightIntention) {
        if (flightIntention != null) {
            this.planId = flightIntention.getPlanId();
            this.indicative = flightIntention.getIndicative();
            this.eobt = flightIntention.getEobt();
            this.eta = flightIntention.getEta();
            this.startDate = flightIntention.getStartDate();
            this.endDate = flightIntention.getEndDate();
            this.eetMinute = flightIntention.getEetMinute();
            this.ssrCode = String.valueOf(flightIntention.getSsrCode());
            this.trackId = flightIntention.getTrackId();
            this.aircraftType = flightIntention.getAircraftType();
            this.airline = flightIntention.getAirline();
            this.flightPlanDate = flightIntention.getFlightPlanDate();
            this.currentDateTimeOfArrival = flightIntention.getCurrentDateTimeOfArrival();
            this.finished = flightIntention.isFinished();
            
            // Extract route information
            if (flightIntention.getSimpleRoute() != null) {
                this.startPointIndicative = flightIntention.getSimpleRoute().getStartPointIndicative();
                this.endPointIndicative = flightIntention.getSimpleRoute().getEndPointIndicative();
                this.cruiseLevel = flightIntention.getSimpleRoute().getCruiseLevel();
                this.cruiseSpeed = flightIntention.getSimpleRoute().getCruiseSpeed();
                this.simpleRouteIndicative = flightIntention.getSimpleRoute().getIndicative();
                this.routeId = flightIntention.getSimpleRoute().getRouteId();
            }
            
            // Extract equipment information
            if (flightIntention.getFlightIntentionEquipment() != null) {
                this.wakeTurbulence = flightIntention.getFlightIntentionEquipment().getWakeTurbulenceCategory();
                this.aircraftEquipmentName = flightIntention.getFlightIntentionEquipment().getEquipmentName();
            }
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public long getPlanId() { return planId; }
    public void setPlanId(long planId) { this.planId = planId; }
    
    public String getIndicative() { return indicative; }
    public void setIndicative(String indicative) { this.indicative = indicative; }
    
    public String getEobt() { return eobt; }
    public void setEobt(String eobt) { this.eobt = eobt; }
    
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public float getEetMinute() { return eetMinute; }
    public void setEetMinute(float eetMinute) { this.eetMinute = eetMinute; }
    
    public String getSsrCode() { return ssrCode; }
    public void setSsrCode(String ssrCode) { this.ssrCode = ssrCode; }
    
    public long getTrackId() { return trackId; }
    public void setTrackId(long trackId) { this.trackId = trackId; }
    
    public String getAircraftType() { return aircraftType; }
    public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }
    
    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }
    
    public String getFlightPlanDate() { return flightPlanDate; }
    public void setFlightPlanDate(String flightPlanDate) { this.flightPlanDate = flightPlanDate; }
    
    public String getCurrentDateTimeOfArrival() { return currentDateTimeOfArrival; }
    public void setCurrentDateTimeOfArrival(String currentDateTimeOfArrival) { this.currentDateTimeOfArrival = currentDateTimeOfArrival; }
    
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
    
    public String getStartPointIndicative() { return startPointIndicative; }
    public void setStartPointIndicative(String startPointIndicative) { this.startPointIndicative = startPointIndicative; }
    
    public String getEndPointIndicative() { return endPointIndicative; }
    public void setEndPointIndicative(String endPointIndicative) { this.endPointIndicative = endPointIndicative; }
    
    public String getCruiseLevel() { return cruiseLevel; }
    public void setCruiseLevel(String cruiseLevel) { this.cruiseLevel = cruiseLevel; }
    
    public String getCruiseSpeed() { return cruiseSpeed; }
    public void setCruiseSpeed(String cruiseSpeed) { this.cruiseSpeed = cruiseSpeed; }
    
    public String getSimpleRouteIndicative() { return simpleRouteIndicative; }
    public void setSimpleRouteIndicative(String simpleRouteIndicative) { this.simpleRouteIndicative = simpleRouteIndicative; }
    
    public long getRouteId() { return routeId; }
    public void setRouteId(long routeId) { this.routeId = routeId; }
    
    public String getWakeTurbulence() { return wakeTurbulence; }
    public void setWakeTurbulence(String wakeTurbulence) { this.wakeTurbulence = wakeTurbulence; }
    
    public String getAircraftEquipmentName() { return aircraftEquipmentName; }
    public void setAircraftEquipmentName(String aircraftEquipmentName) { this.aircraftEquipmentName = aircraftEquipmentName; }
    
    public List<TrackingPoint> getTrackingPoints() { return trackingPoints; }
    public void setTrackingPoints(List<TrackingPoint> trackingPoints) { 
        this.trackingPoints = trackingPoints; 
        this.totalTrackingPoints = trackingPoints != null ? trackingPoints.size() : 0;
        this.hasTrackingData = trackingPoints != null && !trackingPoints.isEmpty();
    }
    
    public int getTotalTrackingPoints() { return totalTrackingPoints; }
    public void setTotalTrackingPoints(int totalTrackingPoints) { this.totalTrackingPoints = totalTrackingPoints; }
    
    public boolean isHasTrackingData() { return hasTrackingData; }
    public void setHasTrackingData(boolean hasTrackingData) { this.hasTrackingData = hasTrackingData; }
    
    @Override
    public String toString() {
        return "JoinedFlightData{" +
                "indicative='" + indicative + '\'' +
                ", aircraftType='" + aircraftType + '\'' +
                ", airline='" + airline + '\'' +
                ", trackingPoints=" + totalTrackingPoints +
                ", hasTrackingData=" + hasTrackingData +
                '}';
    }
} 