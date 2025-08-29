# Repository Package Documentation

This package contains all MongoDB repository interfaces for the Aviation Flight Data Processing System, providing data access layer for flight data, predictions, and processing history.

## Repository Overview

### `FlightRepository.java`
**Actual Flight Data Repository** - Collection: `flights`

**Purpose**: MongoDB operations for actual flight tracking data (JoinedFlightData)

**Key Methods:**
- `findByPlanId(long planId)` - Find flight by unique planId
- `existsByPlanId(long planId)` - Check if flight exists
- `findByIndicative(String indicative)` - Find by call sign (first match only)
- `findAllByIndicative(String indicative)` - Find ALL flights with same call sign
- `findAllPlanIdsProjection()` - Efficiently get all planIds (projection query)

**Features:**
- planId as primary unique identifier
- Handles indicative disambiguation (multiple flights with same call sign)
- Optimized projection queries for performance
- Used by StreamingFlightService, FlightSearchService

### `PredictedFlightRepository.java`
**Predicted Flight Data Repository** - Collection: `predicted_flights`

**Purpose**: MongoDB operations for predicted flight data (PredictedFlightData)

**Key Methods:**
- `findByInstanceId(long instanceId)` - Find by instanceId (matches planId in actual flights)
- `existsByInstanceId(long instanceId)` - Check if prediction exists
- `findByIndicative(String indicative)` - Find by call sign (first match)
- `findAllByIndicative(String indicative)` - Find ALL predictions with same call sign
- `findByRouteId(long routeId)` - Find predictions by route ID
- `findByStartPointIndicativeAndEndPointIndicative()` - Find by route endpoints

**Features:**
- instanceId for matching with actual flights (JoinedFlightData.planId)
- Route-based queries for analysis
- Call sign queries with multiple match support
- Used by PredictedFlightService, PunctualityAnalysisService

### `ProcessingHistoryRepository.java`
**Processing History Repository** - Collection: `processing_history`

**Purpose**: MongoDB operations for tracking processing operations and performance

**Key Methods:**
- `findAllByOrderByTimestampDesc()` - Get all history ordered by most recent
- `findAllByOrderByTimestampDesc(Pageable)` - Paginated history queries
- `findByOperationOrderByTimestampDesc()` - Filter by operation type
- `findByStatusOrderByTimestampDesc()` - Filter by status
- `findByTimestampBetweenOrderByTimestampDesc()` - Date range queries
- `findRecentEntries(Pageable)` - Get recent N entries
- `findEntriesWithErrors()` - Find failed/partial operations
- `findTodayEntries()` - Get today's operations
- `countByStatus()` / `countByOperation()` - Statistics queries
- `findDurationsByOperation()` - Performance analysis queries

**Features:**
- Comprehensive filtering and pagination
- Error tracking and troubleshooting queries
- Performance metrics and statistics
- Dashboard integration support
- Used by ProcessingHistoryService, ProcessingHistoryController

## Repository Patterns

### **Spring Data MongoDB**
All repositories extend `MongoRepository<Entity, String>` providing:
- Basic CRUD operations (save, findById, delete, etc.)
- Automatic query generation from method names
- Custom queries using `@Query` annotation
- Pagination and sorting support

### **Query Method Naming**
```java
// Find by single field
Optional<Entity> findByFieldName(Type value)

// Find multiple results
List<Entity> findAllByFieldName(Type value)

// Existence check
boolean existsByFieldName(Type value)

// Ordering
List<Entity> findByFieldNameOrderByTimestampDesc(Type value)

// Multiple conditions
List<Entity> findByField1AndField2(Type1 value1, Type2 value2)
```

### **Custom Queries**
```java
// Projection query (performance optimization)
@Query(value = "{}", fields = "{ planId: 1, _id: 0 }")
List<JoinedFlightData> findAllPlanIdsProjection();

// Complex conditions
@Query("{ $or: [ { 'status': 'FAILURE' }, { 'status': 'PARTIAL_SUCCESS' } ] }")
List<ProcessingHistory> findEntriesWithErrors();

// Date range queries
@Query("{ 'timestamp': { $gte: ?0, $lt: ?1 } }")
List<ProcessingHistory> findTodayEntries(LocalDateTime start, LocalDateTime end);
```

## Key Relationships

### **Flight Matching**
```java
// Actual flight
JoinedFlightData actualFlight = flightRepository.findByPlanId(planId);

// Corresponding prediction
PredictedFlightData prediction = predictedFlightRepository.findByInstanceId(planId);
```

### **Indicative Disambiguation**
```java
// Single match (may not be unique)
Optional<JoinedFlightData> flight = flightRepository.findByIndicative("TAM3886");

// All matches (proper disambiguation)
List<JoinedFlightData> flights = flightRepository.findAllByIndicative("TAM3886");
```

### **Processing Tracking**
```java
// Track operation
ProcessingHistory history = new ProcessingHistory();
processingHistoryRepository.save(history);

// Query by operation type
List<ProcessingHistory> operations = processingHistoryRepository
    .findByOperationOrderByTimestampDesc(OperationType.PROCESS_REAL_DATA);
```

## Usage Patterns

### **Service Layer Integration**
```java
@Service
public class FlightService {
    @Autowired
    private FlightRepository flightRepository;
    
    public void processFlightData(ReplayPath replayPath) {
        // Check if flight exists
        if (flightRepository.existsByPlanId(planId)) {
            // Update existing
            JoinedFlightData existing = flightRepository.findByPlanId(planId).get();
            // ... update logic
            flightRepository.save(existing);
        } else {
            // Create new
            JoinedFlightData newFlight = new JoinedFlightData();
            // ... creation logic
            flightRepository.save(newFlight);
        }
    }
}
```

### **Analysis Operations**
```java
// Get all flights for analysis
List<JoinedFlightData> allFlights = flightRepository.findAll();

// Get matching predictions
List<PredictedFlightData> predictions = allFlights.stream()
    .map(flight -> predictedFlightRepository.findByInstanceId(flight.getPlanId()))
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toList());
```

### **Performance Optimization**
```java
// Efficient planId retrieval (projection)
List<JoinedFlightData> planIdProjections = flightRepository.findAllPlanIdsProjection();
List<Long> planIds = planIdProjections.stream()
    .map(JoinedFlightData::getPlanId)
    .collect(Collectors.toList());
```

## MongoDB Collections

| Collection | Repository | Model | Primary Key | Indexes |
|------------|------------|-------|-------------|---------|
| `flights` | FlightRepository | JoinedFlightData | planId (unique) | planId, indicative |
| `predicted_flights` | PredictedFlightRepository | PredictedFlightData | instanceId | instanceId |
| `processing_history` | ProcessingHistoryRepository | ProcessingHistory | _id (auto) | timestamp, operation |

## Performance Considerations

### **Indexing Strategy**
- **planId**: Unique index on flights collection for fast lookups
- **instanceId**: Index on predicted_flights for matching operations
- **indicative**: Index for call sign searches (non-unique)
- **timestamp**: Index on processing_history for chronological queries

### **Query Optimization**
- Use projection queries for large datasets (`findAllPlanIdsProjection`)
- Prefer `existsByField` over `findByField` for existence checks
- Use pagination for large result sets
- Leverage MongoDB's compound indexes for multi-field queries

### **Best Practices**
- Always use `findAllBy*` methods when multiple results are expected
- Handle `Optional` results properly to avoid null pointer exceptions
- Use batch operations for bulk inserts/updates
- Monitor query performance with MongoDB profiling

## Error Handling

### **Common Patterns**
```java
// Safe optional handling
Optional<JoinedFlightData> flightOpt = flightRepository.findByPlanId(planId);
if (flightOpt.isPresent()) {
    JoinedFlightData flight = flightOpt.get();
    // Process flight
} else {
    // Handle not found
}

// Existence check before operations
if (flightRepository.existsByPlanId(planId)) {
    // Safe to proceed
} else {
    throw new FlightNotFoundException("Flight not found: " + planId);
}
```

### **Exception Handling**
- `DataAccessException` - MongoDB connection/query issues
- `OptimisticLockingFailureException` - Concurrent modification conflicts
- `DuplicateKeyException` - Unique constraint violations

## Testing

### **Repository Testing**
```java
@DataMongoTest
class FlightRepositoryTest {
    @Autowired
    private FlightRepository flightRepository;
    
    @Test
    void testFindByPlanId() {
        // Given
        JoinedFlightData flight = new JoinedFlightData();
        flight.setPlanId(12345L);
        flightRepository.save(flight);
        
        // When
        Optional<JoinedFlightData> result = flightRepository.findByPlanId(12345L);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPlanId()).isEqualTo(12345L);
    }
}
```
- `findAllByIndicative(String indicative)` - Find all predictions with same call sign
- `findByRouteId(long routeId)` - Find predictions by route
- `findByStartPointIndicativeAndEndPointIndicative()` - Find by route endpoints

**Used By**: PredictedFlightService

## Data Access Patterns

### **planId-Based Queries**
Both repositories support planId-based queries for data comparison:

```java
// Find actual flight
Optional<JoinedFlightData> actualFlight = flightRepository.findByPlanId(planId);

// Find predicted flight for same planId
Optional<PredictedFlightData> predictedFlight = predictedFlightRepository.findByPlanId(planId);
```

### **Indicative-Based Queries**
Both repositories handle indicative (call sign) queries with duplicate awareness:

```java
// Single result (may not be deterministic with duplicates)
Optional<JoinedFlightData> flight = flightRepository.findByIndicative("ABC123");

// All results (handles duplicates properly)
List<JoinedFlightData> flights = flightRepository.findAllByIndicative("ABC123");
```

### **Existence Checks**
Both repositories provide efficient existence checking:

```java
// Check if actual flight exists
boolean actualExists = flightRepository.existsByPlanId(planId);

// Check if prediction exists
boolean predictionExists = predictedFlightRepository.existsByPlanId(planId);
```

## MongoDB Collections

### **flights Collection**
- **Document Type**: JoinedFlightData
- **Primary Index**: planId (unique)
- **Secondary Index**: indicative (non-unique, for call sign queries)
- **Purpose**: Store actual flight tracking data

### **predicted_flights Collection**
- **Document Type**: PredictedFlightData
- **Primary Index**: planId (for comparison matching)
- **Secondary Indexes**: indicative, routeId
- **Purpose**: Store predicted flight route and timing data

## Data Comparison Queries

### **Finding Matching Data**
```java
// Find both actual and predicted data for comparison
long planId = 51637804;

Optional<JoinedFlightData> actual = flightRepository.findByPlanId(planId);
Optional<PredictedFlightData> predicted = predictedFlightRepository.findByPlanId(planId);

if (actual.isPresent() && predicted.isPresent()) {
    // Compare actual vs predicted performance
    JoinedFlightData actualFlight = actual.get();
    PredictedFlightData predictedFlight = predicted.get();
    
    // Comparison logic here
}
```

### **Route-Based Analysis**
```java
// Find predictions by route for analysis
List<PredictedFlightData> routePredictions = 
    predictedFlightRepository.findByStartPointIndicativeAndEndPointIndicative("SBGR", "SBCG");

// Find actual flights with same route for comparison
List<JoinedFlightData> actualFlights = 
    flightRepository.findAllByIndicative("TAM3886");
```

## Performance Considerations

### **Index Usage**
- **planId**: Primary key for both collections, ensures fast lookups
- **indicative**: Secondary index for call sign searches
- **routeId**: Index on predicted flights for route analysis

### **Query Optimization**
- Use `existsByPlanId()` for existence checks rather than `findByPlanId().isPresent()`
- Use `findAllByIndicative()` when handling potential duplicates
- Consider pagination for large result sets

### **Memory Management**
- Repositories return Optional for single results
- Large queries should be paginated or streamed
- Consider projection queries for analysis scenarios

## Best Practices

### **Repository Usage**
1. **Use planId for primary lookups** - It's the unique identifier
2. **Handle indicative duplicates** - Use `findAllByIndicative()` when multiple results expected
3. **Check existence efficiently** - Use `existsByPlanId()` for boolean checks
4. **Consider comparison patterns** - Design queries for actual vs predicted analysis

### **Data Integrity**
1. **planId consistency** - Ensure same planId used across both collections for comparison
2. **Null handling** - Always use Optional pattern for safe data access
3. **Index maintenance** - Ensure indexes support your query patterns

### **Error Handling**
- Repository methods return Optional for not-found scenarios
- Use `existsByPlanId()` to avoid unnecessary data loading
- Handle empty collections gracefully in list operations

## Future Enhancements

### **Planned Repository Features**
1. **Pagination Support**: Spring Data Pageable for large result sets
2. **Custom Queries**: @Query annotations for complex comparison operations
3. **Aggregation Support**: MongoDB aggregation pipeline for analytics
4. **Projection Queries**: Return only required fields for performance
5. **Batch Operations**: Bulk insert/update operations for large datasets

### **Comparison Repository**
Consider creating a dedicated comparison service that uses both repositories:
```java
@Service
public class FlightComparisonService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private PredictedFlightRepository predictedFlightRepository;
    
    public ComparisonResult compareFlightByPlanId(long planId) {
        // Implementation for comparing actual vs predicted
    }
}
```