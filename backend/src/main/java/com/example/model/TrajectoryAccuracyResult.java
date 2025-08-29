package com.example.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Results from trajectory accuracy analysis comparing predicted flight routes
 * with actual flight tracking points using MSE and RMSE metrics.
 */
public class TrajectoryAccuracyResult {
    
    // Overall analysis metadata
    private int totalQualifiedFlights;
    private int totalAnalyzedFlights;
    private int totalSkippedFlights;
    private String analysisTimestamp;
    private long processingTimeMs;
    private String message;
    
    // Aggregate accuracy metrics
    private AggregateAccuracyMetrics aggregateMetrics;
    
    // Per-flight accuracy results
    private List<FlightAccuracyMetrics> flightResults;
    
    // Constructors
    public TrajectoryAccuracyResult() {
        this.analysisTimestamp = LocalDateTime.now().toString();
    }
    
    // Nested class for aggregate metrics
    public static class AggregateAccuracyMetrics {
        // Horizontal metrics (original in radians)
        private double horizontalMSE;        // radians²
        private double horizontalRMSE;       // radians
        
        // Horizontal metrics (converted to meters)
        private double horizontalMSEMeters;  // meters²
        private double horizontalRMSEMeters; // meters
        
        // Vertical metrics (always in meters)
        private double verticalMSE;          // meters²
        private double verticalRMSE;         // meters
        
        private double averagePointsPerFlight;
        private int totalPointsAnalyzed;
        
        // Statistics (both radians and meters for horizontal)
        private double minHorizontalRMSE;        // radians
        private double maxHorizontalRMSE;        // radians
        private double minHorizontalRMSEMeters;  // meters
        private double maxHorizontalRMSEMeters;  // meters
        
        // Vertical statistics (always in meters)
        private double minVerticalRMSE;      // meters
        private double maxVerticalRMSE;      // meters
        
        // Constructors
        public AggregateAccuracyMetrics() {}
        
        public AggregateAccuracyMetrics(double horizontalMSE, double horizontalRMSE, 
                                      double verticalMSE, double verticalRMSE,
                                      double averagePointsPerFlight, int totalPointsAnalyzed) {
            this.horizontalMSE = horizontalMSE;
            this.horizontalRMSE = horizontalRMSE;
            this.verticalMSE = verticalMSE;
            this.verticalRMSE = verticalRMSE;
            this.averagePointsPerFlight = averagePointsPerFlight;
            this.totalPointsAnalyzed = totalPointsAnalyzed;
        }
        
        // Getters and setters for horizontal metrics (radians)
        public double getHorizontalMSE() { return horizontalMSE; }
        public void setHorizontalMSE(double horizontalMSE) { this.horizontalMSE = horizontalMSE; }
        
        public double getHorizontalRMSE() { return horizontalRMSE; }
        public void setHorizontalRMSE(double horizontalRMSE) { this.horizontalRMSE = horizontalRMSE; }
        
        // Getters and setters for horizontal metrics (meters)
        public double getHorizontalMSEMeters() { return horizontalMSEMeters; }
        public void setHorizontalMSEMeters(double horizontalMSEMeters) { this.horizontalMSEMeters = horizontalMSEMeters; }
        
        public double getHorizontalRMSEMeters() { return horizontalRMSEMeters; }
        public void setHorizontalRMSEMeters(double horizontalRMSEMeters) { this.horizontalRMSEMeters = horizontalRMSEMeters; }
        
        // Getters and setters for vertical metrics (always meters)
        public double getVerticalMSE() { return verticalMSE; }
        public void setVerticalMSE(double verticalMSE) { this.verticalMSE = verticalMSE; }
        
        public double getVerticalRMSE() { return verticalRMSE; }
        public void setVerticalRMSE(double verticalRMSE) { this.verticalRMSE = verticalRMSE; }
        
        public double getAveragePointsPerFlight() { return averagePointsPerFlight; }
        public void setAveragePointsPerFlight(double averagePointsPerFlight) { this.averagePointsPerFlight = averagePointsPerFlight; }
        
        public int getTotalPointsAnalyzed() { return totalPointsAnalyzed; }
        public void setTotalPointsAnalyzed(int totalPointsAnalyzed) { this.totalPointsAnalyzed = totalPointsAnalyzed; }
        
        // Getters and setters for horizontal statistics (radians)
        public double getMinHorizontalRMSE() { return minHorizontalRMSE; }
        public void setMinHorizontalRMSE(double minHorizontalRMSE) { this.minHorizontalRMSE = minHorizontalRMSE; }
        
        public double getMaxHorizontalRMSE() { return maxHorizontalRMSE; }
        public void setMaxHorizontalRMSE(double maxHorizontalRMSE) { this.maxHorizontalRMSE = maxHorizontalRMSE; }
        
        // Getters and setters for horizontal statistics (meters)
        public double getMinHorizontalRMSEMeters() { return minHorizontalRMSEMeters; }
        public void setMinHorizontalRMSEMeters(double minHorizontalRMSEMeters) { this.minHorizontalRMSEMeters = minHorizontalRMSEMeters; }
        
        public double getMaxHorizontalRMSEMeters() { return maxHorizontalRMSEMeters; }
        public void setMaxHorizontalRMSEMeters(double maxHorizontalRMSEMeters) { this.maxHorizontalRMSEMeters = maxHorizontalRMSEMeters; }
        
        // Getters and setters for vertical statistics (always meters)
        public double getMinVerticalRMSE() { return minVerticalRMSE; }
        public void setMinVerticalRMSE(double minVerticalRMSE) { this.minVerticalRMSE = minVerticalRMSE; }
        
        public double getMaxVerticalRMSE() { return maxVerticalRMSE; }
        public void setMaxVerticalRMSE(double maxVerticalRMSE) { this.maxVerticalRMSE = maxVerticalRMSE; }
    }
    
    // Nested class for per-flight metrics
    public static class FlightAccuracyMetrics {
        private Long planId;
        private String predictedIndicative;
        private String realIndicative;
        private int pointCount;
        
        // Horizontal accuracy metrics (original in radians)
        private double horizontalMSE;        // radians²
        private double horizontalRMSE;       // radians
        
        // Horizontal accuracy metrics (converted to meters)
        private double horizontalMSEMeters;  // meters²
        private double horizontalRMSEMeters; // meters
        
        // Vertical accuracy metrics (always in meters)
        private double verticalMSE;          // meters²
        private double verticalRMSE;         // meters
        
        // Additional metrics (both radians and meters for horizontal)
        private double maxHorizontalError;        // radians
        private double maxHorizontalErrorMeters;  // meters
        private double averageHorizontalError;        // radians
        private double averageHorizontalErrorMeters;  // meters
        
        // Vertical additional metrics (always in meters)
        private double maxVerticalError;      // meters
        private double averageVerticalError;  // meters
        
        // Constructors
        public FlightAccuracyMetrics() {}
        
        public FlightAccuracyMetrics(Long planId, String predictedIndicative, String realIndicative, 
                                   int pointCount, double horizontalMSE, double horizontalRMSE,
                                   double verticalMSE, double verticalRMSE) {
            this.planId = planId;
            this.predictedIndicative = predictedIndicative;
            this.realIndicative = realIndicative;
            this.pointCount = pointCount;
            this.horizontalMSE = horizontalMSE;
            this.horizontalRMSE = horizontalRMSE;
            this.verticalMSE = verticalMSE;
            this.verticalRMSE = verticalRMSE;
        }
        
        // Basic getters and setters
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        
        public String getPredictedIndicative() { return predictedIndicative; }
        public void setPredictedIndicative(String predictedIndicative) { this.predictedIndicative = predictedIndicative; }
        
        public String getRealIndicative() { return realIndicative; }
        public void setRealIndicative(String realIndicative) { this.realIndicative = realIndicative; }
        
        public int getPointCount() { return pointCount; }
        public void setPointCount(int pointCount) { this.pointCount = pointCount; }
        
        // Getters and setters for horizontal metrics (radians)
        public double getHorizontalMSE() { return horizontalMSE; }
        public void setHorizontalMSE(double horizontalMSE) { this.horizontalMSE = horizontalMSE; }
        
        public double getHorizontalRMSE() { return horizontalRMSE; }
        public void setHorizontalRMSE(double horizontalRMSE) { this.horizontalRMSE = horizontalRMSE; }
        
        // Getters and setters for horizontal metrics (meters)
        public double getHorizontalMSEMeters() { return horizontalMSEMeters; }
        public void setHorizontalMSEMeters(double horizontalMSEMeters) { this.horizontalMSEMeters = horizontalMSEMeters; }
        
        public double getHorizontalRMSEMeters() { return horizontalRMSEMeters; }
        public void setHorizontalRMSEMeters(double horizontalRMSEMeters) { this.horizontalRMSEMeters = horizontalRMSEMeters; }
        
        // Getters and setters for vertical metrics (always meters)
        public double getVerticalMSE() { return verticalMSE; }
        public void setVerticalMSE(double verticalMSE) { this.verticalMSE = verticalMSE; }
        
        public double getVerticalRMSE() { return verticalRMSE; }
        public void setVerticalRMSE(double verticalRMSE) { this.verticalRMSE = verticalRMSE; }
        
        // Getters and setters for additional horizontal metrics (radians)
        public double getMaxHorizontalError() { return maxHorizontalError; }
        public void setMaxHorizontalError(double maxHorizontalError) { this.maxHorizontalError = maxHorizontalError; }
        
        public double getAverageHorizontalError() { return averageHorizontalError; }
        public void setAverageHorizontalError(double averageHorizontalError) { this.averageHorizontalError = averageHorizontalError; }
        
        // Getters and setters for additional horizontal metrics (meters)
        public double getMaxHorizontalErrorMeters() { return maxHorizontalErrorMeters; }
        public void setMaxHorizontalErrorMeters(double maxHorizontalErrorMeters) { this.maxHorizontalErrorMeters = maxHorizontalErrorMeters; }
        
        public double getAverageHorizontalErrorMeters() { return averageHorizontalErrorMeters; }
        public void setAverageHorizontalErrorMeters(double averageHorizontalErrorMeters) { this.averageHorizontalErrorMeters = averageHorizontalErrorMeters; }
        
        // Getters and setters for vertical additional metrics (always meters)
        public double getMaxVerticalError() { return maxVerticalError; }
        public void setMaxVerticalError(double maxVerticalError) { this.maxVerticalError = maxVerticalError; }
        
        public double getAverageVerticalError() { return averageVerticalError; }
        public void setAverageVerticalError(double averageVerticalError) { this.averageVerticalError = averageVerticalError; }
    }
    
    // Main class getters and setters
    public int getTotalQualifiedFlights() { return totalQualifiedFlights; }
    public void setTotalQualifiedFlights(int totalQualifiedFlights) { this.totalQualifiedFlights = totalQualifiedFlights; }
    
    public int getTotalAnalyzedFlights() { return totalAnalyzedFlights; }
    public void setTotalAnalyzedFlights(int totalAnalyzedFlights) { this.totalAnalyzedFlights = totalAnalyzedFlights; }
    
    public int getTotalSkippedFlights() { return totalSkippedFlights; }
    public void setTotalSkippedFlights(int totalSkippedFlights) { this.totalSkippedFlights = totalSkippedFlights; }
    
    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
    
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public AggregateAccuracyMetrics getAggregateMetrics() { return aggregateMetrics; }
    public void setAggregateMetrics(AggregateAccuracyMetrics aggregateMetrics) { this.aggregateMetrics = aggregateMetrics; }
    
    public List<FlightAccuracyMetrics> getFlightResults() { return flightResults; }
    public void setFlightResults(List<FlightAccuracyMetrics> flightResults) { this.flightResults = flightResults; }
}
