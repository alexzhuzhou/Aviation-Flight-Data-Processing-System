package com.example.repository;

import com.example.model.ProcessingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ProcessingHistory operations
 * 
 * Provides methods for querying processing history with various filters
 * and pagination support for the frontend dashboard.
 */
@Repository
public interface ProcessingHistoryRepository extends MongoRepository<ProcessingHistory, String> {
    
    /**
     * Find all processing history entries ordered by timestamp descending (most recent first)
     */
    List<ProcessingHistory> findAllByOrderByTimestampDesc();
    
    /**
     * Find processing history with pagination, ordered by timestamp descending
     */
    Page<ProcessingHistory> findAllByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * Find processing history by operation type
     */
    List<ProcessingHistory> findByOperationOrderByTimestampDesc(ProcessingHistory.OperationType operation);
    
    /**
     * Find processing history by status
     */
    List<ProcessingHistory> findByStatusOrderByTimestampDesc(ProcessingHistory.Status status);
    
    /**
     * Find processing history within a date range
     */
    List<ProcessingHistory> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find recent processing history (last N entries)
     */
    @Query("{ }")
    List<ProcessingHistory> findRecentEntries(Pageable pageable);
    
    /**
     * Find processing history by operation and status
     */
    List<ProcessingHistory> findByOperationAndStatusOrderByTimestampDesc(
            ProcessingHistory.OperationType operation, 
            ProcessingHistory.Status status);
    
    /**
     * Count entries by status
     */
    long countByStatus(ProcessingHistory.Status status);
    
    /**
     * Count entries by operation type
     */
    long countByOperation(ProcessingHistory.OperationType operation);
    
    /**
     * Find entries with errors (FAILURE or PARTIAL_SUCCESS with errors)
     */
    @Query("{ $or: [ { 'status': 'FAILURE' }, { $and: [ { 'status': 'PARTIAL_SUCCESS' }, { 'recordsWithErrors': { $gt: 0 } } ] } ] }")
    List<ProcessingHistory> findEntriesWithErrors();
    
    /**
     * Find processing history for today
     */
    @Query("{ 'timestamp': { $gte: ?0, $lt: ?1 } }")
    List<ProcessingHistory> findTodayEntries(LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    /**
     * Get average duration by operation type
     */
    @Query(value = "{ 'operation': ?0, 'status': { $in: ['SUCCESS', 'PARTIAL_SUCCESS'] } }", 
           fields = "{ 'durationMs': 1 }")
    List<ProcessingHistory> findDurationsByOperation(ProcessingHistory.OperationType operation);
}
