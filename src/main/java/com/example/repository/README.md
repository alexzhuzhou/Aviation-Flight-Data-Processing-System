# Repository Layer Documentation

This document explains the data access layer of the Aviation Replay Data Processor.

## Repository Overview

The repository layer provides a clean abstraction for database operations using Spring Data MongoDB.

### **Current Repository**

| Repository | Purpose | Database | Entity |
|------------|---------|----------|--------|
| **FlightRepository** | Flight data operations | MongoDB | JoinedFlightData |

## FlightRepository Details

### **Purpose**
MongoDB repository for managing flight data with joined intentions and tracking points.

### **Key Features**
- **Primary Key**: Uses `planId` as the unique identifier for flights
- **MongoDB Integration**: Extends `MongoRepository` for automatic CRUD operations
- **Custom Queries**: Provides specialized methods for flight lookup and disambiguation
- **Spring Data**: Leverages Spring Data MongoDB for automatic query generation

### **Available Methods**

#### **Core CRUD Operations** (inherited from MongoRepository)
- `save(JoinedFlightData)` - Save or update a flight
- `findById(String)` - Find by MongoDB document ID
- `findAll()` - Get all flights
- `delete(JoinedFlightData)` - Delete a flight
- `count()` - Count total flights

#### **Custom Query Methods**
- `findByPlanId(long planId)` - Find flight by plan ID (unique)
- `existsByPlanId(long planId)` - Check if flight exists by plan ID
- `findByIndicative(String indicative)` - Find first flight by call sign
- `findAllByIndicative(String indicative)` - Find all flights by call sign

### **Usage Patterns**

#### **1. Creating New Flights**
```java
// Check if flight exists
Optional<JoinedFlightData> existing = flightRepository.findByPlanId(planId);
if (existing.isEmpty()) {
    // Create new flight
    JoinedFlightData newFlight = new JoinedFlightData(intention);
    flightRepository.save(newFlight);
}
```

#### **2. Updating Existing Flights**
```java
// Find and update
Optional<JoinedFlightData> flightOpt = flightRepository.findByPlanId(planId);
if (flightOpt.isPresent()) {
    JoinedFlightData flight = flightOpt.get();
    // Update flight data
    flight.setTrackingPoints(newTrackingPoints);
    flightRepository.save(flight);
}
```

#### **3. Disambiguation (Multiple Flights with Same Indicative)**
```java
// Get all flights with same call sign
List<JoinedFlightData> candidates = flightRepository.findAllByIndicative(indicative);
if (candidates.size() > 1) {
    // Apply disambiguation logic
    JoinedFlightData target = disambiguateFlights(candidates, trackingData);
}
```

## Database Schema

### **MongoDB Collection: flights**

```json
{
  "_id": "ObjectId",
  "planId": 12345,
  "indicative": "ABC123",
  "aircraftType": "B737",
  "airline": "TEST",
  "departureAirport": "SBGR",
  "arrivalAirport": "SBSP",
  "flightPlanDate": "2025-01-15",
  "currentDateTimeOfDeparture": "2025-01-15T10:30:00",
  "currentDateTimeOfArrival": "2025-01-15T11:45:00",
  "trackingPoints": [
    {
      "seqNum": 1,
      "timestamp": 1705312200000,
      "latitude": -23.4356,
      "longitude": -46.4731,
      "flightLevel": 350,
      "trackSpeed": 450,
      "indicativeSafe": "ABC123"
    }
  ],
  "hasTrackingData": true,
  "totalTrackingPoints": 15,
  "lastPacketTimestamp": "2025-01-15T10:35:00"
}
```

### **Indexes**
- **Primary Index**: `_id` (MongoDB default)
- **Unique Index**: `planId` (ensures uniqueness)
- **Secondary Index**: `indicative` (for call sign lookups)

## Performance Considerations

### **Query Optimization**
- **planId lookups**: Very fast (unique index)
- **indicative lookups**: Fast (secondary index)
- **Full collection scans**: Use sparingly for large datasets

### **Memory Management**
- **Large datasets**: Consider pagination for `findAll()` operations
- **Tracking points**: Embedded documents (no separate collection joins)

### **Best Practices**
1. **Use planId for unique lookups** (fastest)
2. **Use indicative for disambiguation** (returns multiple results)
3. **Avoid full collection scans** in production
4. **Batch operations** for bulk updates

## Error Handling

### **Common Scenarios**
- **Duplicate planId**: Handled by unique constraint
- **Missing flights**: Use `Optional` return types
- **Database connection**: Spring Boot auto-configuration

### **Recommended Patterns**
```java
// Safe lookup with error handling
Optional<JoinedFlightData> flight = flightRepository.findByPlanId(planId);
if (flight.isPresent()) {
    // Process flight
} else {
    // Handle missing flight
    logger.warn("Flight not found for planId: {}", planId);
}
```

## Testing Strategy

### **Unit Tests**
- **Mock repository** for service layer tests
- **Test custom query methods** independently
- **Verify query generation** for custom methods

### **Integration Tests**
- **Test with real MongoDB** (embedded or test container)
- **Verify data persistence** and retrieval
- **Test concurrent operations**

### **Performance Tests**
- **Test with large datasets** to verify index effectiveness
- **Monitor query performance** for slow operations
- **Test bulk operations** for data migration scenarios

## Future Enhancements

### **Potential Additions**
1. **Pagination support** for large result sets
2. **Custom aggregation queries** for analytics
3. **Audit trail** for data changes
4. **Soft delete** functionality
5. **Data archival** strategies

### **Monitoring**
- **Query performance metrics**
- **Database connection pooling**
- **Index usage statistics**
- **Storage growth monitoring** 