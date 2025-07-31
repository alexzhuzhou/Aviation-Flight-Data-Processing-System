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