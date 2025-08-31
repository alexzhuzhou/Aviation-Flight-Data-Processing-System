package com.example.model;

import java.util.List;

/**
 * Model representing the results of arrival punctuality analysis (ICAO KPI14)
 * Compares predicted en-route time with executed flight time
 */
public class PunctualityAnalysisResult {
    
    private int totalMatchedFlights;
    private int totalAnalyzedFlights; // Flights with valid time data
    private List<DelayToleranceWindow> delayToleranceWindows;
    private String analysisTimestamp;
    private String message;
    
    // Constructor
    public PunctualityAnalysisResult() {}
    
    public PunctualityAnalysisResult(int totalMatchedFlights, int totalAnalyzedFlights, 
                                   List<DelayToleranceWindow> delayToleranceWindows, 
                                   String analysisTimestamp, String message) {
        this.totalMatchedFlights = totalMatchedFlights;
        this.totalAnalyzedFlights = totalAnalyzedFlights;
        this.delayToleranceWindows = delayToleranceWindows;
        this.analysisTimestamp = analysisTimestamp;
        this.message = message;
    }
    
    // Getters and setters
    public int getTotalMatchedFlights() { return totalMatchedFlights; }
    public void setTotalMatchedFlights(int totalMatchedFlights) { this.totalMatchedFlights = totalMatchedFlights; }
    
    public int getTotalAnalyzedFlights() { return totalAnalyzedFlights; }
    public void setTotalAnalyzedFlights(int totalAnalyzedFlights) { this.totalAnalyzedFlights = totalAnalyzedFlights; }
    
    public List<DelayToleranceWindow> getDelayToleranceWindows() { return delayToleranceWindows; }
    public void setDelayToleranceWindows(List<DelayToleranceWindow> delayToleranceWindows) { this.delayToleranceWindows = delayToleranceWindows; }
    
    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    /**
     * Represents a delay tolerance window with KPI output
     */
    public static class DelayToleranceWindow {
        private String windowDescription; // e.g., "± 3 minutes"
        private int toleranceMinutes;     // e.g., 3
        private int flightsWithinTolerance;
        private double percentageWithinTolerance;
        private String kpiOutput; // e.g., "% of flights where predicted time was within ± 3 minutes of actual time"
        
        public DelayToleranceWindow() {}
        
        public DelayToleranceWindow(String windowDescription, int toleranceMinutes, 
                                  int flightsWithinTolerance, double percentageWithinTolerance, 
                                  String kpiOutput) {
            this.windowDescription = windowDescription;
            this.toleranceMinutes = toleranceMinutes;
            this.flightsWithinTolerance = flightsWithinTolerance;
            this.percentageWithinTolerance = percentageWithinTolerance;
            this.kpiOutput = kpiOutput;
        }
        
        // Getters and setters
        public String getWindowDescription() { return windowDescription; }
        public void setWindowDescription(String windowDescription) { this.windowDescription = windowDescription; }
        
        public int getToleranceMinutes() { return toleranceMinutes; }
        public void setToleranceMinutes(int toleranceMinutes) { this.toleranceMinutes = toleranceMinutes; }
        
        public int getFlightsWithinTolerance() { return flightsWithinTolerance; }
        public void setFlightsWithinTolerance(int flightsWithinTolerance) { this.flightsWithinTolerance = flightsWithinTolerance; }
        
        public double getPercentageWithinTolerance() { return percentageWithinTolerance; }
        public void setPercentageWithinTolerance(double percentageWithinTolerance) { this.percentageWithinTolerance = percentageWithinTolerance; }
        
        public String getKpiOutput() { return kpiOutput; }
        public void setKpiOutput(String kpiOutput) { this.kpiOutput = kpiOutput; }
    }
}