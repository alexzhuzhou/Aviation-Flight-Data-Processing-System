package com.example.controller;

import com.example.model.ProcessingHistory;
import com.example.service.ProcessingHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for processing history operations
 * 
 * Provides endpoints for the frontend dashboard to query and display
 * processing history information.
 */
@RestController
@RequestMapping("/api/processing-history")
@CrossOrigin(origins = "*")
public class ProcessingHistoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessingHistoryController.class);
    
    @Autowired
    private ProcessingHistoryService historyService;
    
    /**
     * Get recent processing history for dashboard display
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ProcessingHistory>> getRecentHistory(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<ProcessingHistory> history = historyService.getRecentHistory(limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving recent processing history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get processing history with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ProcessingHistory>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<ProcessingHistory> history = historyService.getHistoryWithPagination(page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving processing history with pagination", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get processing history by operation type
     */
    @GetMapping("/by-operation/{operation}")
    public ResponseEntity<List<ProcessingHistory>> getHistoryByOperation(
            @PathVariable ProcessingHistory.OperationType operation) {
        try {
            List<ProcessingHistory> history = historyService.getHistoryByOperation(operation);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving processing history by operation: {}", operation, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get processing history within date range
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<List<ProcessingHistory>> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<ProcessingHistory> history = historyService.getHistoryByDateRange(startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving processing history by date range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get today's processing history
     */
    @GetMapping("/today")
    public ResponseEntity<List<ProcessingHistory>> getTodayHistory() {
        try {
            List<ProcessingHistory> history = historyService.getTodayHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving today's processing history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get processing statistics for dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getProcessingStatistics() {
        try {
            Map<String, Object> stats = historyService.getProcessingStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving processing statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get average duration by operation type
     */
    @GetMapping("/average-durations")
    public ResponseEntity<Map<String, Double>> getAverageDurations() {
        try {
            Map<String, Double> averages = historyService.getAverageDurationByOperation();
            return ResponseEntity.ok(averages);
        } catch (Exception e) {
            logger.error("Error retrieving average durations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get entries with errors for troubleshooting
     */
    @GetMapping("/errors")
    public ResponseEntity<List<ProcessingHistory>> getEntriesWithErrors() {
        try {
            List<ProcessingHistory> errors = historyService.getEntriesWithErrors();
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            logger.error("Error retrieving entries with errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Processing History Service is running");
    }
    
    /**
     * Clean up old entries (maintenance endpoint)
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldEntries(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        try {
            historyService.cleanupOldEntries(daysToKeep);
            return ResponseEntity.ok("Cleanup completed successfully");
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cleanup failed: " + e.getMessage());
        }
    }
}
