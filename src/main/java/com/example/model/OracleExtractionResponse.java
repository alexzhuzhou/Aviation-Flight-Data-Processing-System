package com.example.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for Oracle extraction operations
 * 
 * Provides detailed information about extraction results following Option A approach:
 * Skip missing planIds and report detailed statistics
 */
public class OracleExtractionResponse {
    
    @JsonProperty("totalRequested")
    private int totalRequested;
    
    @JsonProperty("totalProcessed")
    private int totalProcessed;
    
    @JsonProperty("totalNotFound")
    private int totalNotFound;
    
    @JsonProperty("totalErrors")
    private int totalErrors;
    
    @JsonProperty("processedPlanIds")
    private List<Long> processedPlanIds;
    
    @JsonProperty("notFoundPlanIds")
    private List<Long> notFoundPlanIds;
    
    @JsonProperty("errorPlanIds")
    private List<Long> errorPlanIds;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("processingTimeMs")
    private long processingTimeMs;
    
    @JsonProperty("extractionTimeMs")
    private long extractionTimeMs;
    
    // Constructors
    public OracleExtractionResponse() {}
    
    // Getters and setters
    public int getTotalRequested() {
        return totalRequested;
    }
    
    public void setTotalRequested(int totalRequested) {
        this.totalRequested = totalRequested;
    }
    
    public int getTotalProcessed() {
        return totalProcessed;
    }
    
    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }
    
    public int getTotalNotFound() {
        return totalNotFound;
    }
    
    public void setTotalNotFound(int totalNotFound) {
        this.totalNotFound = totalNotFound;
    }
    
    public int getTotalErrors() {
        return totalErrors;
    }
    
    public void setTotalErrors(int totalErrors) {
        this.totalErrors = totalErrors;
    }
    
    public List<Long> getProcessedPlanIds() {
        return processedPlanIds;
    }
    
    public void setProcessedPlanIds(List<Long> processedPlanIds) {
        this.processedPlanIds = processedPlanIds;
    }
    
    public List<Long> getNotFoundPlanIds() {
        return notFoundPlanIds;
    }
    
    public void setNotFoundPlanIds(List<Long> notFoundPlanIds) {
        this.notFoundPlanIds = notFoundPlanIds;
    }
    
    public List<Long> getErrorPlanIds() {
        return errorPlanIds;
    }
    
    public void setErrorPlanIds(List<Long> errorPlanIds) {
        this.errorPlanIds = errorPlanIds;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public long getExtractionTimeMs() {
        return extractionTimeMs;
    }
    
    public void setExtractionTimeMs(long extractionTimeMs) {
        this.extractionTimeMs = extractionTimeMs;
    }
    
    /**
     * Calculate success rate as percentage
     */
    public double getSuccessRate() {
        if (totalRequested == 0) return 0.0;
        return (totalProcessed * 100.0) / totalRequested;
    }
    
    /**
     * Generate summary message
     */
    public void generateMessage() {
        if (totalRequested == 0) {
            this.message = "No planIds provided for processing";
        } else if (totalProcessed == totalRequested) {
            this.message = String.format("Successfully processed all %d flights", totalProcessed);
        } else {
            this.message = String.format("Processed %d/%d flights successfully", 
                                        totalProcessed, totalRequested);
            if (totalNotFound > 0) {
                this.message += String.format(", %d planIds not found in database", totalNotFound);
            }
            if (totalErrors > 0) {
                this.message += String.format(", %d processing errors", totalErrors);
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("OracleExtractionResponse{requested=%d, processed=%d, notFound=%d, errors=%d, successRate=%.1f%%}",
                           totalRequested, totalProcessed, totalNotFound, totalErrors, getSuccessRate());
    }
}
