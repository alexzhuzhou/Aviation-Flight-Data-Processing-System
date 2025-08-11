package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enhanced processing result for Oracle database extraction operations
 * 
 * Provides comprehensive information about the data extraction and processing
 * from the Sigma Oracle database, including performance metrics and error details.
 */
public class OracleProcessingResult {
    
    @JsonProperty("newFlights")
    private int newFlights;
    
    @JsonProperty("updatedFlights") 
    private int updatedFlights;
    
    @JsonProperty("totalPacketsProcessed")
    private int totalPacketsProcessed;
    
    @JsonProperty("packetsWithErrors")
    private int packetsWithErrors;
    
    @JsonProperty("processingTimeMs")
    private long processingTimeMs;
    
    @JsonProperty("dataSource")
    private String dataSource;
    
    @JsonProperty("extractionDate")
    private String extractionDate;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("databaseConnectionTime")
    private long databaseConnectionTime;
    
    @JsonProperty("dataExtractionTime")
    private long dataExtractionTime;
    
    @JsonProperty("dataProcessingTime")
    private long dataProcessingTime;
    
    // Constructors
    public OracleProcessingResult() {}
    
    public OracleProcessingResult(int newFlights, int updatedFlights, int totalPacketsProcessed, 
                                int packetsWithErrors, long processingTimeMs, String dataSource, 
                                String extractionDate, String message) {
        this.newFlights = newFlights;
        this.updatedFlights = updatedFlights;
        this.totalPacketsProcessed = totalPacketsProcessed;
        this.packetsWithErrors = packetsWithErrors;
        this.processingTimeMs = processingTimeMs;
        this.dataSource = dataSource;
        this.extractionDate = extractionDate;
        this.message = message;
    }
    
    // Getters and Setters
    public int getNewFlights() {
        return newFlights;
    }
    
    public void setNewFlights(int newFlights) {
        this.newFlights = newFlights;
    }
    
    public int getUpdatedFlights() {
        return updatedFlights;
    }
    
    public void setUpdatedFlights(int updatedFlights) {
        this.updatedFlights = updatedFlights;
    }
    
    public int getTotalPacketsProcessed() {
        return totalPacketsProcessed;
    }
    
    public void setTotalPacketsProcessed(int totalPacketsProcessed) {
        this.totalPacketsProcessed = totalPacketsProcessed;
    }
    
    public int getPacketsWithErrors() {
        return packetsWithErrors;
    }
    
    public void setPacketsWithErrors(int packetsWithErrors) {
        this.packetsWithErrors = packetsWithErrors;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    public String getExtractionDate() {
        return extractionDate;
    }
    
    public void setExtractionDate(String extractionDate) {
        this.extractionDate = extractionDate;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getDatabaseConnectionTime() {
        return databaseConnectionTime;
    }
    
    public void setDatabaseConnectionTime(long databaseConnectionTime) {
        this.databaseConnectionTime = databaseConnectionTime;
    }
    
    public long getDataExtractionTime() {
        return dataExtractionTime;
    }
    
    public void setDataExtractionTime(long dataExtractionTime) {
        this.dataExtractionTime = dataExtractionTime;
    }
    
    public long getDataProcessingTime() {
        return dataProcessingTime;
    }
    
    public void setDataProcessingTime(long dataProcessingTime) {
        this.dataProcessingTime = dataProcessingTime;
    }
    
    @Override
    public String toString() {
        return String.format("OracleProcessingResult{newFlights=%d, updatedFlights=%d, totalPackets=%d, errors=%d, timeMs=%d, source='%s', date='%s'}",
                newFlights, updatedFlights, totalPacketsProcessed, packetsWithErrors, processingTimeMs, dataSource, extractionDate);
    }
}
