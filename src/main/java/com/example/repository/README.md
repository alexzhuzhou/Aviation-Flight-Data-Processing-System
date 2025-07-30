# Repository Layer Documentation

This document explains the repository layer in the Aviation Replay Data Processor, including both flight tracking and predicted flight repositories.

## Repository Overview

The repositories follow the Spring Data MongoDB pattern and provide data access for different flight data types:

### **1. Flight Tracking Repository**
| Repository | Purpose | Collection |
|------------|---------|------------|
| **FlightRepository** | Actual flight data operations | `flights` |

### **2. Predicted Flight Repository**
| Repository | Purpose | Collection |
|------------|---------|------------|
| **PredictedFlightRepository** | Predicted flight data operations | `predicted_flights` |

## Repository Responsibilities

### **FlightRepository** 🛩️
**Primary Purpose**: MongoDB operations for actual flight tracking data

**Key Features**:
- CRUD operations for JoinedFlightData
- Query by planId (unique identifier)
- Query by indicative (call sign) with duplicate handling
- Support for upsert operations in streaming scenarios

**Main Methods**:
- `findByPlanId(long planId)` - Find flight by unique planId
- `existsByPlanId(long planId)` - Check if flight exists
- `findByIndicative(String indicative)` - Find by call sign (returns first match)
- `findAllByIndicative(String indicative)` - Find all flights with same call sign

**Used By**: StreamingFlightService

### **PredictedFlightRepository** 🔮
**Primary Purpose**: MongoDB operations for predicted flight data

**Key Features**:
- CRUD operations for PredictedFlightData
- Query by planId for comparison with actual flights
- Query by indicative and route information
- Support for prediction updates and analysis

**Main Methods**:
- `findByPlanId(long planId)` - Find predicted flight for comparison
- `existsByPlanId(long planId)` - Check if prediction exists
- `findByIndicative(String indicative)` - Find by call sign
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