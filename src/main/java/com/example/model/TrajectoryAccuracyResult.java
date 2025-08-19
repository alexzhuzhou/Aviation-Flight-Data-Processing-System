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
        private double horizontalMSE;
        private double horizontalRMSE;
        private double verticalMSE;
        private double verticalRMSE;
        private double averagePointsPerFlight;
        private int totalPointsAnalyzed;
        
        // Statistics
        private double minHorizontalRMSE;
        private double maxHorizontalRMSE;
        private double minVerticalRMSE;
        private double maxVerticalRMSE;
        
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
        
        // Getters and setters
        public double getHorizontalMSE() { return horizontalMSE; }
        public void setHorizontalMSE(double horizontalMSE) { this.horizontalMSE = horizontalMSE; }
        
        public double getHorizontalRMSE() { return horizontalRMSE; }
        public void setHorizontalRMSE(double horizontalRMSE) { this.horizontalRMSE = horizontalRMSE; }
        
        public double getVerticalMSE() { return verticalMSE; }
        public void setVerticalMSE(double verticalMSE) { this.verticalMSE = verticalMSE; }
        
        public double getVerticalRMSE() { return verticalRMSE; }
        public void setVerticalRMSE(double verticalRMSE) { this.verticalRMSE = verticalRMSE; }
        
        public double getAveragePointsPerFlight() { return averagePointsPerFlight; }
        public void setAveragePointsPerFlight(double averagePointsPerFlight) { this.averagePointsPerFlight = averagePointsPerFlight; }
        
        public int getTotalPointsAnalyzed() { return totalPointsAnalyzed; }
        public void setTotalPointsAnalyzed(int totalPointsAnalyzed) { this.totalPointsAnalyzed = totalPointsAnalyzed; }
        
        public double getMinHorizontalRMSE() { return minHorizontalRMSE; }
        public void setMinHorizontalRMSE(double minHorizontalRMSE) { this.minHorizontalRMSE = minHorizontalRMSE; }
        
        public double getMaxHorizontalRMSE() { return maxHorizontalRMSE; }
        public void setMaxHorizontalRMSE(double maxHorizontalRMSE) { this.maxHorizontalRMSE = maxHorizontalRMSE; }
        
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
        
        // Accuracy metrics
        private double horizontalMSE;
        private double horizontalRMSE;
        private double verticalMSE;
        private double verticalRMSE;
        
        // Additional metrics
        private double maxHorizontalError;
        private double maxVerticalError;
        private double averageHorizontalError;
        private double averageVerticalError;
        
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
        
        // Getters and setters
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        
        public String getPredictedIndicative() { return predictedIndicative; }
        public void setPredictedIndicative(String predictedIndicative) { this.predictedIndicative = predictedIndicative; }
        
        public String getRealIndicative() { return realIndicative; }
        public void setRealIndicative(String realIndicative) { this.realIndicative = realIndicative; }
        
        public int getPointCount() { return pointCount; }
        public void setPointCount(int pointCount) { this.pointCount = pointCount; }
        
        public double getHorizontalMSE() { return horizontalMSE; }
        public void setHorizontalMSE(double horizontalMSE) { this.horizontalMSE = horizontalMSE; }
        
        public double getHorizontalRMSE() { return horizontalRMSE; }
        public void setHorizontalRMSE(double horizontalRMSE) { this.horizontalRMSE = horizontalRMSE; }
        
        public double getVerticalMSE() { return verticalMSE; }
        public void setVerticalMSE(double verticalMSE) { this.verticalMSE = verticalMSE; }
        
        public double getVerticalRMSE() { return verticalRMSE; }
        public void setVerticalRMSE(double verticalRMSE) { this.verticalRMSE = verticalRMSE; }
        
        public double getMaxHorizontalError() { return maxHorizontalError; }
        public void setMaxHorizontalError(double maxHorizontalError) { this.maxHorizontalError = maxHorizontalError; }
        
        public double getMaxVerticalError() { return maxVerticalError; }
        public void setMaxVerticalError(double maxVerticalError) { this.maxVerticalError = maxVerticalError; }
        
        public double getAverageHorizontalError() { return averageHorizontalError; }
        public void setAverageHorizontalError(double averageHorizontalError) { this.averageHorizontalError = averageHorizontalError; }
        
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
