package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Model for tracking processing operation history
 * 
 * Stores information about when operations were called, their status,
 * duration, and other relevant metadata for audit and monitoring purposes.
 */
@Document(collection = "processing_history")
public class ProcessingHistory {
    
    public enum OperationType {
        PROCESS_REAL_DATA("Process real data"),
        SYNC_PREDICTED_DATA("Synchronize predicted data"),
        DENSIFY_PREDICTED_DATA("Densify predicted data");
        
        private final String displayName;
        
        OperationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILURE,
        IN_PROGRESS
    }
    
    @Id
    private String id;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("operation")
    private OperationType operation;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("durationMs")
    private long durationMs;
    
    @JsonProperty("endpoint")
    private String endpoint;
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("recordsProcessed")
    private Integer recordsProcessed;
    
    @JsonProperty("recordsWithErrors")
    private Integer recordsWithErrors;
    
    @JsonProperty("requestParameters")
    private String requestParameters;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    // Constructors
    public ProcessingHistory() {
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
    }
    
    public ProcessingHistory(OperationType operation, String endpoint) {
        this();
        this.operation = operation;
        this.endpoint = endpoint;
        this.status = Status.IN_PROGRESS;
    }
    
    // Static factory methods for easy creation
    public static ProcessingHistory startProcessing(OperationType operation, String endpoint) {
        return new ProcessingHistory(operation, endpoint);
    }
    
    public static ProcessingHistory startProcessing(OperationType operation, String endpoint, String requestParameters) {
        ProcessingHistory history = new ProcessingHistory(operation, endpoint);
        history.setRequestParameters(requestParameters);
        return history;
    }
    
    // Convenience methods for completing operations
    public void completeSuccess(long durationMs, String details) {
        this.status = Status.SUCCESS;
        this.durationMs = durationMs;
        this.details = details;
    }
    
    public void completeSuccess(long durationMs, String details, int recordsProcessed) {
        completeSuccess(durationMs, details);
        this.recordsProcessed = recordsProcessed;
    }
    
    public void completePartialSuccess(long durationMs, String details, int recordsProcessed, int recordsWithErrors) {
        this.status = Status.PARTIAL_SUCCESS;
        this.durationMs = durationMs;
        this.details = details;
        this.recordsProcessed = recordsProcessed;
        this.recordsWithErrors = recordsWithErrors;
    }
    
    public void completeFailure(long durationMs, String errorMessage) {
        this.status = Status.FAILURE;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public OperationType getOperation() {
        return operation;
    }
    
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }
    
    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
    
    public Integer getRecordsWithErrors() {
        return recordsWithErrors;
    }
    
    public void setRecordsWithErrors(Integer recordsWithErrors) {
        this.recordsWithErrors = recordsWithErrors;
    }
    
    public String getRequestParameters() {
        return requestParameters;
    }
    
    public void setRequestParameters(String requestParameters) {
        this.requestParameters = requestParameters;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    // Utility methods
    public String getFormattedDuration() {
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.1fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    public boolean isSuccessful() {
        return status == Status.SUCCESS || status == Status.PARTIAL_SUCCESS;
    }
    
    @Override
    public String toString() {
        return String.format("ProcessingHistory{operation=%s, status=%s, duration=%s, endpoint='%s'}",
                operation, status, getFormattedDuration(), endpoint);
    }
}
