package com.example.service;

import com.example.model.ProcessingHistory;
import com.example.repository.ProcessingHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service for managing processing history operations
 * 
 * Provides methods for recording, querying, and analyzing processing history
 * for audit trails and dashboard display.
 */
@Service
public class ProcessingHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessingHistoryService.class);
    
    @Autowired
    private ProcessingHistoryRepository historyRepository;
    
    /**
     * Start tracking a new processing operation
     */
    public ProcessingHistory startOperation(ProcessingHistory.OperationType operation, String endpoint) {
        ProcessingHistory history = ProcessingHistory.startProcessing(operation, endpoint);
        ProcessingHistory saved = historyRepository.save(history);
        logger.info("Started tracking operation: {} on endpoint: {}", operation, endpoint);
        return saved;
    }
    
    /**
     * Start tracking a new processing operation with request parameters
     */
    public ProcessingHistory startOperation(ProcessingHistory.OperationType operation, String endpoint, String requestParameters) {
        ProcessingHistory history = ProcessingHistory.startProcessing(operation, endpoint, requestParameters);
        ProcessingHistory saved = historyRepository.save(history);
        logger.info("Started tracking operation: {} on endpoint: {} with parameters: {}", operation, endpoint, requestParameters);
        return saved;
    }
    
    /**
     * Complete an operation successfully
     */
    public void completeSuccess(String historyId, long durationMs, String details) {
        ProcessingHistory history = historyRepository.findById(historyId).orElse(null);
        if (history != null) {
            history.completeSuccess(durationMs, details);
            historyRepository.save(history);
            logger.info("Completed operation successfully: {} in {}ms", history.getOperation(), durationMs);
        }
    }
    
    /**
     * Complete an operation successfully with record counts
     */
    public void completeSuccess(String historyId, long durationMs, String details, int recordsProcessed) {
        ProcessingHistory history = historyRepository.findById(historyId).orElse(null);
        if (history != null) {
            history.completeSuccess(durationMs, details, recordsProcessed);
            historyRepository.save(history);
            logger.info("Completed operation successfully: {} in {}ms, processed {} records", 
                       history.getOperation(), durationMs, recordsProcessed);
        }
    }
    
    /**
     * Complete an operation with partial success (some errors)
     */
    public void completePartialSuccess(String historyId, long durationMs, String details, 
                                     int recordsProcessed, int recordsWithErrors) {
        ProcessingHistory history = historyRepository.findById(historyId).orElse(null);
        if (history != null) {
            history.completePartialSuccess(durationMs, details, recordsProcessed, recordsWithErrors);
            historyRepository.save(history);
            logger.warn("Completed operation with partial success: {} in {}ms, processed {} records, {} errors", 
                       history.getOperation(), durationMs, recordsProcessed, recordsWithErrors);
        }
    }
    
    /**
     * Complete an operation with failure
     */
    public void completeFailure(String historyId, long durationMs, String errorMessage) {
        ProcessingHistory history = historyRepository.findById(historyId).orElse(null);
        if (history != null) {
            history.completeFailure(durationMs, errorMessage);
            historyRepository.save(history);
            logger.error("Operation failed: {} in {}ms, error: {}", history.getOperation(), durationMs, errorMessage);
        }
    }
    
    /**
     * Get recent processing history for dashboard display
     */
    public List<ProcessingHistory> getRecentHistory(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return historyRepository.findRecentEntries(pageable);
    }
    
    /**
     * Get processing history with pagination
     */
    public Page<ProcessingHistory> getHistoryWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findAllByOrderByTimestampDesc(pageable);
    }
    
    /**
     * Get processing history by operation type
     */
    public List<ProcessingHistory> getHistoryByOperation(ProcessingHistory.OperationType operation) {
        return historyRepository.findByOperationOrderByTimestampDesc(operation);
    }
    
    /**
     * Get processing history within date range
     */
    public List<ProcessingHistory> getHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return historyRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
    }
    
    /**
     * Get today's processing history
     */
    public List<ProcessingHistory> getTodayHistory() {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return historyRepository.findTodayEntries(startOfDay, endOfDay);
    }
    
    /**
     * Get processing statistics for dashboard
     */
    public Map<String, Object> getProcessingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count by status
        long successCount = historyRepository.countByStatus(ProcessingHistory.Status.SUCCESS);
        long partialSuccessCount = historyRepository.countByStatus(ProcessingHistory.Status.PARTIAL_SUCCESS);
        long failureCount = historyRepository.countByStatus(ProcessingHistory.Status.FAILURE);
        long inProgressCount = historyRepository.countByStatus(ProcessingHistory.Status.IN_PROGRESS);
        
        stats.put("totalOperations", successCount + partialSuccessCount + failureCount + inProgressCount);
        stats.put("successfulOperations", successCount);
        stats.put("partialSuccessOperations", partialSuccessCount);
        stats.put("failedOperations", failureCount);
        stats.put("inProgressOperations", inProgressCount);
        
        // Count by operation type
        long realDataOps = historyRepository.countByOperation(ProcessingHistory.OperationType.PROCESS_REAL_DATA);
        long predictedSyncOps = historyRepository.countByOperation(ProcessingHistory.OperationType.SYNC_PREDICTED_DATA);
        long densifyOps = historyRepository.countByOperation(ProcessingHistory.OperationType.DENSIFY_PREDICTED_DATA);
        
        stats.put("realDataOperations", realDataOps);
        stats.put("predictedSyncOperations", predictedSyncOps);
        stats.put("densifyOperations", densifyOps);
        
        // Success rate
        long totalCompleted = successCount + partialSuccessCount + failureCount;
        double successRate = totalCompleted > 0 ? ((successCount + partialSuccessCount) * 100.0) / totalCompleted : 0.0;
        stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        // Recent activity
        List<ProcessingHistory> recentHistory = getRecentHistory(10);
        stats.put("recentOperations", recentHistory);
        
        // Today's activity
        List<ProcessingHistory> todayHistory = getTodayHistory();
        stats.put("todayOperations", todayHistory.size());
        
        return stats;
    }
    
    /**
     * Get average duration by operation type
     */
    public Map<String, Double> getAverageDurationByOperation() {
        Map<String, Double> averages = new HashMap<>();
        
        for (ProcessingHistory.OperationType operation : ProcessingHistory.OperationType.values()) {
            List<ProcessingHistory> durations = historyRepository.findDurationsByOperation(operation);
            if (!durations.isEmpty()) {
                double average = durations.stream()
                    .mapToLong(ProcessingHistory::getDurationMs)
                    .average()
                    .orElse(0.0);
                averages.put(operation.name(), average);
            }
        }
        
        return averages;
    }
    
    /**
     * Get entries with errors for troubleshooting
     */
    public List<ProcessingHistory> getEntriesWithErrors() {
        return historyRepository.findEntriesWithErrors();
    }
    
    /**
     * Clean up old history entries (optional - for maintenance)
     */
    public void cleanupOldEntries(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(daysToKeep);
        List<ProcessingHistory> oldEntries = historyRepository.findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime.of(2020, 1, 1, 0, 0), cutoffDate);
        
        if (!oldEntries.isEmpty()) {
            historyRepository.deleteAll(oldEntries);
            logger.info("Cleaned up {} old processing history entries older than {} days", 
                       oldEntries.size(), daysToKeep);
        }
    }
}
