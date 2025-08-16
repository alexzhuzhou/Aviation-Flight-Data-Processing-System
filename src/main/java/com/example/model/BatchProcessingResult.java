package com.example.model;

import java.util.List;
import java.util.Map;

/**
 * Response model for batch processing operations
 * Used to report results of bulk operations including successes and failures
 */
public class BatchProcessingResult {
    
    private int totalReceived;
    private int totalProcessed;
    private int totalSkipped;
    private int totalFailed;
    private long processingTimeMs;
    private String message;
    private List<String> errors;
    private List<String> skippedDetails; // Detailed information about skipped records
    private Map<String, Integer> skipReasons; // Summary of skip reasons
    
    // Additional fields for auto-sync functionality
    private int totalRequested;
    private int totalNotFound;
    private int totalErrors;
    private long extractionTimeMs;
    private List<Long> processedPlanIds;
    private List<Long> notFoundPlanIds;
    private List<Long> errorPlanIds;
    
    // Default constructor
    public BatchProcessingResult() {}
    
    // Constructor for successful operations
    public BatchProcessingResult(int totalReceived, int totalProcessed, int totalSkipped, 
                                long processingTimeMs, String message) {
        this.totalReceived = totalReceived;
        this.totalProcessed = totalProcessed;
        this.totalSkipped = totalSkipped;
        this.totalFailed = 0;
        this.processingTimeMs = processingTimeMs;
        this.message = message;
    }
    
    // Constructor with errors
    public BatchProcessingResult(int totalReceived, int totalProcessed, int totalSkipped, 
                                int totalFailed, long processingTimeMs, String message, 
                                List<String> errors) {
        this.totalReceived = totalReceived;
        this.totalProcessed = totalProcessed;
        this.totalSkipped = totalSkipped;
        this.totalFailed = totalFailed;
        this.processingTimeMs = processingTimeMs;
        this.message = message;
        this.errors = errors;
    }
    
    // Constructor with detailed skip information
    public BatchProcessingResult(int totalReceived, int totalProcessed, int totalSkipped, 
                                int totalFailed, long processingTimeMs, String message, 
                                List<String> errors, List<String> skippedDetails, 
                                Map<String, Integer> skipReasons) {
        this.totalReceived = totalReceived;
        this.totalProcessed = totalProcessed;
        this.totalSkipped = totalSkipped;
        this.totalFailed = totalFailed;
        this.processingTimeMs = processingTimeMs;
        this.message = message;
        this.errors = errors;
        this.skippedDetails = skippedDetails;
        this.skipReasons = skipReasons;
    }
    
    // Getters and setters
    public int getTotalReceived() { return totalReceived; }
    public void setTotalReceived(int totalReceived) { this.totalReceived = totalReceived; }
    
    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }
    
    public int getTotalSkipped() { return totalSkipped; }
    public void setTotalSkipped(int totalSkipped) { this.totalSkipped = totalSkipped; }
    
    public int getTotalFailed() { return totalFailed; }
    public void setTotalFailed(int totalFailed) { this.totalFailed = totalFailed; }
    
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    
    public List<String> getSkippedDetails() { return skippedDetails; }
    public void setSkippedDetails(List<String> skippedDetails) { this.skippedDetails = skippedDetails; }
    
    public Map<String, Integer> getSkipReasons() { return skipReasons; }
    public void setSkipReasons(Map<String, Integer> skipReasons) { this.skipReasons = skipReasons; }
    
    // Auto-sync specific getters and setters
    public int getTotalRequested() { return totalRequested; }
    public void setTotalRequested(int totalRequested) { this.totalRequested = totalRequested; }
    
    public int getTotalNotFound() { return totalNotFound; }
    public void setTotalNotFound(int totalNotFound) { this.totalNotFound = totalNotFound; }
    
    public int getTotalErrors() { return totalErrors; }
    public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }
    
    public long getExtractionTimeMs() { return extractionTimeMs; }
    public void setExtractionTimeMs(long extractionTimeMs) { this.extractionTimeMs = extractionTimeMs; }
    
    public List<Long> getProcessedPlanIds() { return processedPlanIds; }
    public void setProcessedPlanIds(List<Long> processedPlanIds) { this.processedPlanIds = processedPlanIds; }
    
    public List<Long> getNotFoundPlanIds() { return notFoundPlanIds; }
    public void setNotFoundPlanIds(List<Long> notFoundPlanIds) { this.notFoundPlanIds = notFoundPlanIds; }
    
    public List<Long> getErrorPlanIds() { return errorPlanIds; }
    public void setErrorPlanIds(List<Long> errorPlanIds) { this.errorPlanIds = errorPlanIds; }
    
    // Utility methods
    public boolean hasErrors() {
        return totalFailed > 0 || (errors != null && !errors.isEmpty());
    }
    
    public boolean isFullySuccessful() {
        return totalFailed == 0 && totalReceived == (totalProcessed + totalSkipped);
    }
    
    @Override
    public String toString() {
        return "BatchProcessingResult{" +
                "totalReceived=" + totalReceived +
                ", totalProcessed=" + totalProcessed +
                ", totalSkipped=" + totalSkipped +
                ", totalFailed=" + totalFailed +
                ", processingTimeMs=" + processingTimeMs +
                ", message='" + message + '\'' +
                ", hasErrors=" + hasErrors() +
                ", skipReasons=" + skipReasons +
                '}';
    }
}