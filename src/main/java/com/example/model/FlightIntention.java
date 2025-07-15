package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a planned flight intention from listFlightIntention
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightIntention {
    
    private int planId;
    private String indicative;
    private String eta;
    private String startDate;
    private String endDate;
    private Object previewDeparture; // Can be PreviewDeparture object or String date
    private int eetMinute;
    private int ssrCode;
    private String observation;
    private String annotation;
    private String flightIntentionType;
    private SimpleRoute simpleRoute;
    private Object extractionRoute; // Often null
    private FlightIntentionEquipment flightIntentionEquipment;
    private String correlationSymbol;
    private String controlStatus;
    private boolean rvsm;
    private int trackId;
    private String flightType;
    private String aircraftType;
    private String airline;
    private String flightPlanDate;
    private String currentDateTimeOfArrival;
    private boolean finished;
    private String eobt;
    private boolean correlated;
    private int cruiseLevelFlight;
    private boolean managerAdep;
    
    // Constructors
    public FlightIntention() {}
    
    // Getters and Setters
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    
    public String getIndicative() { return indicative; }
    public void setIndicative(String indicative) { this.indicative = indicative; }
    
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public Object getPreviewDeparture() { return previewDeparture; }
    public void setPreviewDeparture(Object previewDeparture) { this.previewDeparture = previewDeparture; }
    
    public int getEetMinute() { return eetMinute; }
    public void setEetMinute(int eetMinute) { this.eetMinute = eetMinute; }
    
    public int getSsrCode() { return ssrCode; }
    public void setSsrCode(int ssrCode) { this.ssrCode = ssrCode; }
    
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    
    public String getAnnotation() { return annotation; }
    public void setAnnotation(String annotation) { this.annotation = annotation; }
    
    public String getFlightIntentionType() { return flightIntentionType; }
    public void setFlightIntentionType(String flightIntentionType) { this.flightIntentionType = flightIntentionType; }
    
    public SimpleRoute getSimpleRoute() { return simpleRoute; }
    public void setSimpleRoute(SimpleRoute simpleRoute) { this.simpleRoute = simpleRoute; }
    
    public Object getExtractionRoute() { return extractionRoute; }
    public void setExtractionRoute(Object extractionRoute) { this.extractionRoute = extractionRoute; }
    
    public FlightIntentionEquipment getFlightIntentionEquipment() { return flightIntentionEquipment; }
    public void setFlightIntentionEquipment(FlightIntentionEquipment flightIntentionEquipment) { this.flightIntentionEquipment = flightIntentionEquipment; }
    
    public String getCorrelationSymbol() { return correlationSymbol; }
    public void setCorrelationSymbol(String correlationSymbol) { this.correlationSymbol = correlationSymbol; }
    
    public String getControlStatus() { return controlStatus; }
    public void setControlStatus(String controlStatus) { this.controlStatus = controlStatus; }
    
    public boolean isRvsm() { return rvsm; }
    public void setRvsm(boolean rvsm) { this.rvsm = rvsm; }
    
    public int getTrackId() { return trackId; }
    public void setTrackId(int trackId) { this.trackId = trackId; }
    
    public String getFlightType() { return flightType; }
    public void setFlightType(String flightType) { this.flightType = flightType; }
    
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
    
    public String getEobt() { return eobt; }
    public void setEobt(String eobt) { this.eobt = eobt; }
    
    public boolean isCorrelated() { return correlated; }
    public void setCorrelated(boolean correlated) { this.correlated = correlated; }
    
    public int getCruiseLevelFlight() { return cruiseLevelFlight; }
    public void setCruiseLevelFlight(int cruiseLevelFlight) { this.cruiseLevelFlight = cruiseLevelFlight; }
    
    public boolean isManagerAdep() { return managerAdep; }
    public void setManagerAdep(boolean managerAdep) { this.managerAdep = managerAdep; }
    
    @Override
    public String toString() {
        return "FlightIntention{" +
                "planId=" + planId +
                ", indicative='" + indicative + '\'' +
                ", aircraftType='" + aircraftType + '\'' +
                ", airline='" + airline + '\'' +
                ", cruiseLevelFlight=" + cruiseLevelFlight +
                ", finished=" + finished +
                '}';
    }
    
    // Nested classes for complex objects
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PreviewDeparture {
        private int year;
        private String month;  // Changed to String to handle "JULY" format
        private int monthValue;
        private int day;
        private int hour;
        private int minute;
        private int second;
        private int nano;
        private long epochSecond;
        private String dayOfWeek;
        private int dayOfYear;
        private Chronology chronology;
        
        // Getters and setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        
        public int getMonthValue() { return monthValue; }
        public void setMonthValue(int monthValue) { this.monthValue = monthValue; }
        
        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
        
        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }
        
        public int getMinute() { return minute; }
        public void setMinute(int minute) { this.minute = minute; }
        
        public int getSecond() { return second; }
        public void setSecond(int second) { this.second = second; }
        
        public int getNano() { return nano; }
        public void setNano(int nano) { this.nano = nano; }
        
        public long getEpochSecond() { return epochSecond; }
        public void setEpochSecond(long epochSecond) { this.epochSecond = epochSecond; }
        
        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        
        public int getDayOfYear() { return dayOfYear; }
        public void setDayOfYear(int dayOfYear) { this.dayOfYear = dayOfYear; }
        
        public Chronology getChronology() { return chronology; }
        public void setChronology(Chronology chronology) { this.chronology = chronology; }
        
        @Override
        public String toString() {
            return year + "-" + (monthValue > 0 ? monthValue : month) + "-" + day + " " + hour + ":" + minute + ":" + second;
        }
        
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Chronology {
            private String id;
            private String calendarType;
            
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            
            public String getCalendarType() { return calendarType; }
            public void setCalendarType(String calendarType) { this.calendarType = calendarType; }
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SimpleRoute {
        private int routeId;
        private String cruiseSpeed;  // Changed to String to handle "N0450" format
        private String cruiseLevel;  // Changed to String to handle similar format
        private String startPointIndicative;
        private String endPointIndicative;
        private String alternative;
        private String indicative;  // Added missing field
        private int classId;
        private int factoryId;
        
        // Getters and setters
        public int getRouteId() { return routeId; }
        public void setRouteId(int routeId) { this.routeId = routeId; }
        
        public String getCruiseSpeed() { return cruiseSpeed; }
        public void setCruiseSpeed(String cruiseSpeed) { this.cruiseSpeed = cruiseSpeed; }
        
        public String getCruiseLevel() { return cruiseLevel; }
        public void setCruiseLevel(String cruiseLevel) { this.cruiseLevel = cruiseLevel; }
        
        public String getStartPointIndicative() { return startPointIndicative; }
        public void setStartPointIndicative(String startPointIndicative) { this.startPointIndicative = startPointIndicative; }
        
        public String getEndPointIndicative() { return endPointIndicative; }
        public void setEndPointIndicative(String endPointIndicative) { this.endPointIndicative = endPointIndicative; }
        
        public String getAlternative() { return alternative; }
        public void setAlternative(String alternative) { this.alternative = alternative; }
        
        public String getIndicative() { return indicative; }
        public void setIndicative(String indicative) { this.indicative = indicative; }
        
        public int getClassId() { return classId; }
        public void setClassId(int classId) { this.classId = classId; }
        
        public int getFactoryId() { return factoryId; }
        public void setFactoryId(int factoryId) { this.factoryId = factoryId; }
        
        @Override
        public String toString() {
            return "SimpleRoute{" +
                    "routeId=" + routeId +
                    ", cruiseSpeed=" + cruiseSpeed +
                    ", cruiseLevel=" + cruiseLevel +
                    ", startPoint='" + startPointIndicative + '\'' +
                    ", endPoint='" + endPointIndicative + '\'' +
                    '}';
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightIntentionEquipment {
        private String wakeTurbulenceCategory;
        private String equipmentName;
        
        public String getWakeTurbulenceCategory() { return wakeTurbulenceCategory; }
        public void setWakeTurbulenceCategory(String wakeTurbulenceCategory) { this.wakeTurbulenceCategory = wakeTurbulenceCategory; }
        
        public String getEquipmentName() { return equipmentName; }
        public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
        
        @Override
        public String toString() {
            return "FlightIntentionEquipment{" +
                    "wakeTurbulenceCategory='" + wakeTurbulenceCategory + '\'' +
                    ", equipmentName='" + equipmentName + '\'' +
                    '}';
        }
    }
} 