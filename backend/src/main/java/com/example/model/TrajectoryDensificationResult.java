package com.example.model;

import java.time.LocalDateTime;

/**
 * Result model for trajectory densification operations.
 * 
 * Contains information about the densification process including
 * original and final route element counts, success status, and timing.
 */
public class TrajectoryDensificationResult {
    
    public enum Status {
        SUCCESS,
        NOT_FOUND,
        NO_ACTION_NEEDED,
        ERROR
    }
    
    private Long planId;
    private Status status;
    private String message;
    private int originalRouteElementCount;
    private int finalRouteElementCount;
    private int targetPointCount;
    private long processingTimeMs;
    private LocalDateTime processedAt;
    private String errorDetails;
    
    // Interpolation method tracking
    private int sigmaSimulatedPoints = 0;
    private int linearInterpolatedPoints = 0;
    private double sigmaSuccessRate = 0.0;
    
    // Private constructor to enforce factory methods
    private TrajectoryDensificationResult() {
        this.processedAt = LocalDateTime.now();
    }
    
    /**
     * Creates a successful densification result.
     */
    public static TrajectoryDensificationResult success(Long planId, int originalCount, 
                                                       int finalCount, int targetCount) {
        TrajectoryDensificationResult result = new TrajectoryDensificationResult();
        result.planId = planId;
        result.status = Status.SUCCESS;
        result.originalRouteElementCount = originalCount;
        result.finalRouteElementCount = finalCount;
        result.targetPointCount = targetCount;
        result.message = String.format("Successfully densified trajectory from %d to %d route elements (target: %d)", 
                                     originalCount, finalCount, targetCount);
        return result;
    }
    
    /**
     * Creates a successful densification result with interpolation method statistics.
     */
    public static TrajectoryDensificationResult success(Long planId, int originalCount, 
                                                       int finalCount, int targetCount,
                                                       int sigmaPoints, int linearPoints) {
        TrajectoryDensificationResult result = new TrajectoryDensificationResult();
        result.planId = planId;
        result.status = Status.SUCCESS;
        result.originalRouteElementCount = originalCount;
        result.finalRouteElementCount = finalCount;
        result.targetPointCount = targetCount;
        result.sigmaSimulatedPoints = sigmaPoints;
        result.linearInterpolatedPoints = linearPoints;
        result.calculateSigmaSuccessRate();
        result.message = String.format("Successfully densified trajectory from %d to %d route elements (target: %d) - %d Sigma, %d linear (%.1f%% Sigma success)", 
                                     originalCount, finalCount, targetCount, sigmaPoints, linearPoints, result.sigmaSuccessRate);
        return result;
    }
    
    /**
     * Creates a result when flights are not found.
     */
    public static TrajectoryDensificationResult notFound(Long planId) {
        TrajectoryDensificationResult result = new TrajectoryDensificationResult();
        result.planId = planId;
        result.status = Status.NOT_FOUND;
        result.message = "Could not find matching real and predicted flights for planId: " + planId;
        return result;
    }
    
    /**
     * Creates a result when no densification is needed.
     */
    public static TrajectoryDensificationResult noActionNeeded(Long planId, int targetCount, int currentCount) {
        TrajectoryDensificationResult result = new TrajectoryDensificationResult();
        result.planId = planId;
        result.status = Status.NO_ACTION_NEEDED;
        result.originalRouteElementCount = currentCount;
        result.finalRouteElementCount = currentCount;
        result.targetPointCount = targetCount;
        result.message = String.format("Predicted flight already has sufficient density (%d >= %d)", 
                                     currentCount, targetCount);
        return result;
    }
    
    /**
     * Creates an error result.
     */
    public static TrajectoryDensificationResult error(Long planId, String errorMessage) {
        TrajectoryDensificationResult result = new TrajectoryDensificationResult();
        result.planId = planId;
        result.status = Status.ERROR;
        result.message = "Error during trajectory densification";
        result.errorDetails = errorMessage;
        return result;
    }
    
    // Getters and setters
    public Long getPlanId() {
        return planId;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getOriginalRouteElementCount() {
        return originalRouteElementCount;
    }
    
    public int getFinalRouteElementCount() {
        return finalRouteElementCount;
    }
    
    public int getTargetPointCount() {
        return targetPointCount;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean needsAction() {
        return status == Status.SUCCESS;
    }
    
    /**
     * Gets the densification ratio (final/original).
     */
    public double getDensificationRatio() {
        if (originalRouteElementCount == 0) return 0.0;
        return (double) finalRouteElementCount / originalRouteElementCount;
    }
    
    /**
     * Gets the target achievement percentage.
     */
    public double getTargetAchievementPercentage() {
        if (targetPointCount == 0) return 0.0;
        return (double) finalRouteElementCount / targetPointCount * 100.0;
    }
    
    // Interpolation method tracking getters and setters
    public int getSigmaSimulatedPoints() {
        return sigmaSimulatedPoints;
    }
    
    public void setSigmaSimulatedPoints(int sigmaSimulatedPoints) {
        this.sigmaSimulatedPoints = sigmaSimulatedPoints;
    }
    
    public int getLinearInterpolatedPoints() {
        return linearInterpolatedPoints;
    }
    
    public void setLinearInterpolatedPoints(int linearInterpolatedPoints) {
        this.linearInterpolatedPoints = linearInterpolatedPoints;
    }
    
    public double getSigmaSuccessRate() {
        return sigmaSuccessRate;
    }
    
    public void setSigmaSuccessRate(double sigmaSuccessRate) {
        this.sigmaSuccessRate = sigmaSuccessRate;
    }
    
    /**
     * Calculates and updates the Sigma success rate based on point counts.
     */
    public void calculateSigmaSuccessRate() {
        int totalPoints = sigmaSimulatedPoints + linearInterpolatedPoints;
        if (totalPoints > 0) {
            this.sigmaSuccessRate = (double) sigmaSimulatedPoints / totalPoints * 100.0;
        } else {
            this.sigmaSuccessRate = 0.0;
        }
    }
    
    @Override
    public String toString() {
        return String.format("TrajectoryDensificationResult{planId=%d, status=%s, original=%d, final=%d, target=%d, ratio=%.2f, achievement=%.1f%%, sigma=%d, linear=%d, sigmaRate=%.1f%%}", 
                           planId, status, originalRouteElementCount, finalRouteElementCount, 
                           targetPointCount, getDensificationRatio(), getTargetAchievementPercentage(),
                           sigmaSimulatedPoints, linearInterpolatedPoints, sigmaSuccessRate);
    }
}
