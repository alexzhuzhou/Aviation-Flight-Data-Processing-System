# API Documentation

This document provides comprehensive information about the Aviation Replay Data Processor REST API, including Oracle database integration, flight tracking, predicted flight endpoints, and punctuality analysis.

## API Overview

The API provides endpoints for real-time processing of aviation flight tracking data with direct Oracle database integration, predicted flight data processing, packet processing, statistics, data quality monitoring, prediction comparison capabilities, and punctuality analysis (ICAO KPI14).

### **Base URL**
- **Development**: `http://localhost:8080`
- **Production**: `https://api.aviation.example.com`

### **API Documentation**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## Flight Tracking Endpoints

These endpoints handle actual flight tracking data with Oracle database integration and real-time packet processing.

### **1. Process Flight Data from Oracle**  **NEW**
**POST** `/api/flights/process-packet`

Processes flight data directly from the Sigma Oracle database for the hardcoded date (2025-07-11). This is the main production endpoint that replaces JSON-based input with direct database access.

#### **Request Body**
None required - triggers automatic processing from Oracle database.

#### **Response**
```json
{
  "newFlights": 150,
  "updatedFlights": 200,
  "totalPacketsProcessed": 25,
  "packetsWithErrors": 0,
  "processingTimeMs": 15420,
  "dataSource": "Oracle Database - Sigma Production",
  "extractionDate": "2025-07-11",
  "message": "Successfully processed 25 packets from Oracle database",
  "databaseConnectionTime": 1200,
  "dataExtractionTime": 8500,
  "dataProcessingTime": 5720
}
```

#### **Status Codes**
- `200` - Data processed successfully from Oracle
- `500` - Database connection or processing error

---

### **2. Test Oracle Connection**  **NEW**
**GET** `/api/flights/test-oracle-connection`

Tests the connection to the Sigma Oracle database and returns connection status.

#### **Response**
```json
{
  "connectionStatus": "SUCCESS",
  "connectionTimeMs": 850,
  "databaseVersion": "Oracle Database 19c",
  "message": "Successfully connected to Sigma Oracle database"
}
```

#### **Status Codes**
- `200` - Connection test successful
- `500` - Connection failed

---

### **3. Integration Summary**  **NEW**
**GET** `/api/flights/integration-summary`

Provides comprehensive summary of the Oracle integration status, database statistics, and processing capabilities.

#### **Response**
```json
{
  "oracleIntegration": {
    "status": "ACTIVE",
    "lastConnectionTest": "2024-12-19T10:30:00",
    "connectionTimeMs": 850
  },
  "mongodbIntegration": {
    "status": "ACTIVE",
    "totalFlights": 1250,
    "totalTrackingPoints": 45678
  },
  "processingCapabilities": {
    "oracleDataExtraction": true,
    "realtimeProcessing": true,
    "predictedFlightAnalysis": true,
    "punctualityAnalysis": true
  },
  "lastProcessingRun": {
    "timestamp": "2024-12-19T09:45:00",
    "packetsProcessed": 25,
    "processingTimeMs": 15420
  }
}
```

#### **Status Codes**
- `200` - Summary retrieved successfully
- `500` - Error retrieving integration status

---

### **4. Process Legacy Packet** 
**POST** `/api/flights/process-packet-legacy`

Processes a single ReplayPath packet containing flight intentions and tracking points (legacy JSON input method).

#### **Request Body**
```json
{
  "time": "1705312200000",
  "packetStoredTimestamp": "2025-01-15T10:35:00",
  "listFlightIntention": [
    {
      "planId": 12345,
      "indicative": "ABC123",
      "aircraftType": "B737",
      "airline": "TEST",
      "departureAirport": "SBGR",
      "arrivalAirport": "SBSP"
    }
  ],
  "listRealPath": [
    {
      "planId": 0,
      "indicativeSafe": "ABC123",
      "seqNum": 1,
      "latitude": -23.4356,
      "longitude": -46.4731,
      "flightLevel": 350,
      "trackSpeed": 450
    }
  ]
}
```

#### **Response**
```json
{
  "newFlights": 1,
  "updatedFlights": 1,
  "message": "Processed 1 new flights, updated 1 flights with tracking data (cross-packet progress tracking)"
}
```

#### **Status Codes**
- `200` - Packet processed successfully
- `400` - Invalid request data
- `500` - Internal server error

---

### **5. Get Plan IDs** 
**GET** `/api/flights/plan-ids`

Retrieves all planIds from processed flight data for use in prediction scripts.

#### **Response**
```json
{
  "totalCount": 1243,
  "planIds": [17871744, 17873112, 17879345, ...],
  "processingTimeMs": 45
}
```

#### **Status Codes**
- `200` - Plan IDs retrieved successfully
- `500` - Error retrieving plan IDs

---

### **6. Get Statistics** 
**GET** `/api/flights/stats`

Retrieves current statistics about processed flight data.

#### **Response**
```json
{
  "totalFlights": 1250,
  "flightsWithTracking": 1180,
  "totalTrackingPoints": 45678
}
```

#### **Status Codes**
- `200` - Statistics retrieved successfully
- `500` - Error retrieving statistics

---

### **7. Analyze Duplicates** 🔍
**GET** `/api/flights/analyze-duplicates`

Analyzes the database for flights with duplicate call signs (indicatives).

#### **Response**
```json
{
  "duplicateIndicatives": 15,
  "affectedFlights": 45,
  "duplicateDetails": {
    "ABC123": [
      {"id": "1", "planId": 12345, "indicative": "ABC123"},
      {"id": "2", "planId": 12346, "indicative": "ABC123"}
    ]
  }
}
```

#### **Status Codes**
- `200` - Analysis completed successfully
- `500` - Error during analysis

---

### **4. Health Check** 💚
**GET** `/api/flights/health`

Simple health check endpoint to verify the service is running.

#### **Response**
```
Streaming Flight Service is running
```

#### **Status Codes**
- `200` - Service is healthy

---



---

### **5. Cleanup Duplicates** 🧹
**POST** `/api/flights/cleanup-duplicates`

Removes duplicate tracking points from all flights in the database.

#### **Response**
```
Cleanup completed: 1250 duplicate tracking points removed
```

#### **Status Codes**
- `200` - Cleanup completed successfully
- `500` - Error during cleanup

---

### **6. Get All PlanIds**  **NEW**
**GET** `/api/flights/plan-ids`

Retrieves all planIds from the flights collection for feeding into prediction scripts.

#### **Response**
```json
{
  "totalCount": 1243,
  "planIds": [17871744, 17873112, 17873136, 17873336, 17875545],
  "processingTimeMs": 45,
  "message": "Retrieved 1243 planIds successfully"
}
```

#### **Status Codes**
- `200` - PlanIds retrieved successfully
- `500` - Error retrieving planIds

#### **Use Case**
This endpoint is designed for external prediction scripts that need all planIds to generate predicted flight data.

---

## Data Models

### **ReplayPath**
Represents a streaming packet with flight intentions and tracking points.

```json
{
  "time": "string",
  "packetStoredTimestamp": "string",
  "listFlightIntention": ["FlightIntention"],
  "listRealPath": ["RealPathPoint"]
}
```

### **ProcessingResult**
Result of packet processing operation.

```json
{
  "newFlights": "number",
  "updatedFlights": "number",
  "message": "string"
}
```

### **FlightStats**
Statistics about processed flight data.

```json
{
  "totalFlights": "number",
  "flightsWithTracking": "number",
  "totalTrackingPoints": "number"
}
```

### **DuplicateIndicativeAnalysis**
Analysis of duplicate call signs.

```json
{
  "duplicateIndicatives": "number",
  "affectedFlights": "number",
  "duplicateDetails": "object"
}
```

## Error Handling

### **Standard Error Response**
```json
{
  "newFlights": 0,
  "updatedFlights": 0,
  "message": "Error: [error description]"
}
```

### **HTTP Status Codes**
- `200` - Success
- `400` - Bad Request (invalid data)
- `500` - Internal Server Error

## Authentication

Currently, no authentication is required (development mode).

## Rate Limiting

No rate limiting is currently implemented.

## CORS

Cross-Origin Resource Sharing is enabled for all origins:
- Allowed Origins: `*`
- Allowed Methods: `GET, POST, PUT, DELETE, OPTIONS`
- Allowed Headers: `*`

## Testing

### **Using cURL**

#### **Process Packet**
```bash
curl -X POST http://localhost:8080/api/flights/process-packet \
  -H "Content-Type: application/json" \
  -d '{
    "time": "1705312200000",
    "packetStoredTimestamp": "2025-01-15T10:35:00",
    "listFlightIntention": [
      {
        "planId": 12345,
        "indicative": "ABC123",
        "aircraftType": "B737"
      }
    ],
    "listRealPath": []
  }'
```

#### **Get Statistics**
```bash
curl -X GET http://localhost:8080/api/flights/stats
```

#### **Health Check**
```bash
curl -X GET http://localhost:8080/api/flights/health
```

### **Using Swagger UI**

1. Start the application
2. Navigate to `http://localhost:8080/swagger-ui.html`
3. Use the interactive documentation to test endpoints

## Monitoring

### **Health Checks**
- **Endpoint**: `GET /api/flights/health`
- **Use Case**: Load balancer health checks, monitoring systems

### **Statistics**
- **Endpoint**: `GET /api/flights/stats`
- **Use Case**: Performance monitoring, data quality metrics

### **Duplicate Analysis**
- **Endpoint**: `GET /api/flights/analyze-duplicates`
- **Use Case**: Data quality monitoring, debugging

## Best Practices

### **For API Consumers**

1. **Error Handling**: Always check HTTP status codes
2. **Retry Logic**: Implement exponential backoff for 500 errors
3. **Validation**: Validate request data before sending
4. **Monitoring**: Use health check endpoint for service monitoring

### **For Development**

1. **Testing**: Use Swagger UI for endpoint testing
2. **Logging**: Check application logs for debugging
3. **Data Quality**: Monitor duplicate analysis for data issues
4. **Performance**: Use statistics endpoint to monitor processing

## Predicted Flight Endpoints

These endpoints handle predicted flight data for comparison with actual flight performance.

### **1. Process Predicted Flight** 🔮
**POST** `/api/predicted-flights/process`

Processes predicted flight data containing route elements, segments, and timing predictions.

#### **Request Body**
```json
{
  "instanceId": 17879345,
  "routeId": 51435982,
  "distance": null,
  "routeElements": [
    {
      "latitude": -23.43555556,
      "speedMeterPerSecond": 74.594444,
      "eetMinutes": 0.0,
      "id": 169324506,
      "indicative": "SBGR",
      "levelMeters": 749.808,
      "elementType": "AERODROME",
      "coordinateText": "2326S04628W",
      "longitude": -46.47305556
    }
  ],
  "id": 51637804,
  "indicative": "TAM3886",
  "time": "[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]",
  "startPointIndicative": "SBGR",
  "endPointIndicative": "SBCG",
  "routeSegments": [
    {
      "elementBId": 169324507,
      "elementAId": 169324506,
      "distance": 11074.472239127741,
      "id": 107956339
    }
  ]
}
```

#### **Response**
```json
{
  "success": true,
  "message": "Successfully processed predicted flight: TAM3886 (planId: 51637804)"
}
```

#### **Status Codes**
- `200` - Predicted flight processed successfully
- `400` - Invalid request data
- `500` - Internal server error

---

### **2. Get Predicted Flight Statistics** 📊
**GET** `/api/predicted-flights/stats`

Retrieves statistics about stored predicted flight data.

#### **Response**
```json
{
  "totalPredictedFlights": 125
}
```

#### **Status Codes**
- `200` - Statistics retrieved successfully
- `500` - Error retrieving statistics

---

### **3. Predicted Flight Health Check** 💚
**GET** `/api/predicted-flights/health`

Health check endpoint for the predicted flights service.

#### **Response**
```
Predicted Flight Service is running
```

#### **Status Codes**
- `200` - Service is healthy

---

### **4. Batch Process Predicted Flights** 🚀 **NEW**
**POST** `/api/predicted-flights/batch`

Processes multiple predicted flights efficiently in a single batch operation. Optimized for high-volume processing (1000+ records).

#### **Request Body**
```json
[
  {
    "instanceId": 17879345,
    "routeId": 51435982,
    "distance": null,
    "routeElements": [...],
    "id": 51637804,
    "indicative": "TAM3886",
    "time": "[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]",
    "startPointIndicative": "SBGR",
    "endPointIndicative": "SBCG",
    "routeSegments": [...]
  },
  {
    "instanceId": 17879346,
    "routeId": 51435983,
    "id": 51637805,
    "indicative": "TAM3887",
    "routeElements": [...],
    "routeSegments": [...]
  }
]
```

#### **Response**
```json
{
  "totalReceived": 1243,
  "totalProcessed": 1200,
  "totalSkipped": 43,
  "totalFailed": 0,
  "processingTimeMs": 2340,
  "message": "Batch processing completed: 1243 received, 1200 processed, 43 skipped, 0 failed",
  "errors": null
}
```

#### **Status Codes**
- `200` - Batch processing completed successfully
- `500` - Error during batch processing

#### **Features**
- **Optimal Performance**: Processes 500 records per database batch
- **Skip Duplicates**: Automatically skips existing planIds
- **Error Resilience**: Saves what it can, reports failures
- **Progress Tracking**: Detailed processing metrics
- **Transaction Safety**: Database consistency guaranteed

#### **Use Case**
Designed for external prediction scripts that need to upload large batches of predicted flight data efficiently.

---

## Data Comparison

### **planId Matching**
Both actual flights and predicted flights use `planId` as the primary key for matching:
- **Actual Flight Data**: Stored in `flights` collection
- **Predicted Flight Data**: Stored in `predicted_flights` collection
- **Comparison**: Use same `planId` to match predicted vs actual performance

### **Example Comparison Query**
```bash
# Find actual flight by planId
curl http://localhost:8080/api/flights/stats

# Find predicted flight by same planId  
curl http://localhost:8080/api/predicted-flights/stats
```

## Testing Predicted Flight Endpoints

### **Using cURL**

#### **Process Predicted Flight**
```bash
curl -X POST http://localhost:8080/api/predicted-flights/process \
  -H "Content-Type: application/json" \
  -d '{
    "instanceId": 17879345,
    "routeId": 51435982,
    "id": 51637804,
    "indicative": "TAM3886",
    "time": "[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]",
    "startPointIndicative": "SBGR",
    "endPointIndicative": "SBCG",
    "routeElements": [],
    "routeSegments": []
  }'
```

#### **Get Predicted Flight Statistics**
```bash
curl -X GET http://localhost:8080/api/predicted-flights/stats
```

#### **Health Check**
```bash
curl -X GET http://localhost:8080/api/predicted-flights/health
```

## Punctuality Analysis Endpoints

These endpoints handle arrival punctuality analysis (ICAO KPI14) by comparing predicted flight times with actual flight times.

### **1. Run Punctuality Analysis** ⏰
**GET** `/api/punctuality-analysis/run`

Performs arrival punctuality analysis comparing predicted en-route time with executed flight time.

#### **Response**
```json
{
  "totalMatchedFlights": 150,
  "totalAnalyzedFlights": 140,
  "delayToleranceWindows": [
    {
      "windowDescription": "± 3 minutes",
      "toleranceMinutes": 3,
      "flightsWithinTolerance": 85,
      "percentageWithinTolerance": 60.7,
      "kpiOutput": "60.7% of flights where predicted time was within ± 3 minutes of actual time"
    },
    {
      "windowDescription": "± 5 minutes",
      "toleranceMinutes": 5,
      "flightsWithinTolerance": 112,
      "percentageWithinTolerance": 80.0,
      "kpiOutput": "80.0% of flights where predicted time was within ± 5 minutes of actual time"
    },
    {
      "windowDescription": "± 15 minutes",
      "toleranceMinutes": 15,
      "flightsWithinTolerance": 134,
      "percentageWithinTolerance": 95.7,
      "kpiOutput": "95.7% of flights where predicted time was within ± 15 minutes of actual time"
    }
  ],
  "analysisTimestamp": "2024-12-19T10:30:45",
  "message": "Analysis completed: 150 predicted flights, 150 matched with real flights, 140 analyzed successfully"
}
```

#### **Status Codes**
- `200` - Analysis completed successfully
- `500` - Error during analysis

---

### **2. Get Analysis Statistics** 📊
**GET** `/api/punctuality-analysis/stats`

Retrieves statistics about available data for punctuality analysis.

#### **Response**
```json
{
  "totalPredictedFlights": 150,
  "totalRealFlights": 200,
  "analysisCapability": true
}
```

#### **Status Codes**
- `200` - Statistics retrieved successfully
- `500` - Error retrieving statistics

---

### **3. Health Check** 🏥
**GET** `/api/punctuality-analysis/health`

Checks if the punctuality analysis service is running.

#### **Response**
```
Punctuality Analysis Service is running
```

#### **Status Codes**
- `200` - Service is running
- `500` - Service error

---

### **4. Find Qualifying Flights** 🔍
**GET** `/api/punctuality-analysis/qualifying-flights`

Finds predicted flights that meet the specific route conditions for punctuality analysis (SBSP ↔ SBRJ with AERODROME endpoints).

#### **Response**
```json
{
  "totalQualifyingFlights": 45,
  "sbspToSbrj": 23,
  "sbrjToSbsp": 22,
  "analysisCapability": true,
  "qualifyingFlights": [
    {
      "instanceId": 17879345,
      "indicative": "TAM3886",
      "startPointIndicative": "SBSP",
      "endPointIndicative": "SBRJ",
      "routeElements": [...]
    }
  ]
}
```

#### **Status Codes**
- `200` - Qualifying flights found successfully
- `500` - Error finding qualifying flights

---

### **5. Extract Airport Coordinates** 📍
**GET** `/api/punctuality-analysis/airport-coordinates`

Extracts airport coordinates from qualifying flights for analysis.

#### **Response**
```json
{
  "totalQualifyingFlights": 45,
  "sbspToSbrj": 23,
  "sbrjToSbsp": 22,
  "analysisCapability": true,
  "flightsWithCoordinates": [
    {
      "planId": 17879345,
      "indicative": "TAM3886",
      "startPointIndicative": "SBSP",
      "endPointIndicative": "SBRJ",
      "departureAirport": {
        "indicative": "SBSP",
        "latitude": -23.43555556,
        "longitude": -46.47305556,
        "elementType": "AERODROME",
        "coordinateText": "2326S04628W"
      },
      "arrivalAirport": {
        "indicative": "SBRJ",
        "latitude": -22.91083333,
        "longitude": -43.16305556,
        "elementType": "AERODROME",
        "coordinateText": "2254S04306W"
      },
      "distanceKm": 358.45
    }
  ]
}
```

#### **Status Codes**
- `200` - Coordinates extracted successfully
- `500` - Error extracting coordinates

---

### **6. Match Predicted with Real Flights** 🔗
**GET** `/api/punctuality-analysis/match-flights`

Matches qualifying predicted flights with their corresponding real flights using instanceId/planId matching.

#### **Response**
```json
{
  "totalQualifyingFlights": 45,
  "matchedFlights": 38,
  "unmatchedFlights": 7,
  "matchRate": 84.4,
  "analysisCapability": true,
  "matchedFlights": [
    {
      "instanceId": 17879345,
      "predictedIndicative": "TAM3886",
      "planId": 17879345,
      "realIndicative": "TAM3886",
      "hasRealFlight": true,
      "trackingPointsCount": 156,
      "predictedFlight": {...},
      "realFlight": {...}
    }
  ]
}
```

#### **Status Codes**
- `200` - Flight matching completed successfully
- `500` - Error during flight matching

---

### **7. Apply Geographic Validation** 🌍
**GET** `/api/punctuality-analysis/geographic-validation`

Applies geographic validation filters to matched flights (2 NM threshold + flight level ≤ 4).

#### **Response**
```json
{
  "totalMatchedFlights": 38,
  "totalValidatedFlights": 32,
  "totalRejectedFlights": 6,
  "validationRate": "84.2%",
  "sampleValidatedFlights": [
    {
      "instanceId": 17879345,
      "predictedIndicative": "TAM3886",
      "planId": 17879345,
      "realIndicative": "TAM3886",
      "hasRealFlight": true,
      "trackingPointsCount": 156,
      "departureDistanceNM": 1.2,
      "arrivalDistanceNM": 0.8,
      "departureDistanceKm": 2.22,
      "arrivalDistanceKm": 1.48,
      "departureFlightLevel": 3,
      "arrivalFlightLevel": 2,
      "departureFlightLevelFeet": 300,
      "arrivalFlightLevelFeet": 200,
      "predictedFlight": {...},
      "realFlight": {...}
    }
  ]
}
```

#### **Status Codes**
- `200` - Geographic validation completed successfully
- `500` - Error during geographic validation

---

### **8. Calculate Punctuality KPIs** 📊
**GET** `/api/punctuality-analysis/punctuality-kpis`

Calculates punctuality KPIs by comparing predicted vs actual flight times within tolerance windows.

#### **Response**
```json
{
  "totalAnalyzed": 32,
  "totalErrors": 0,
  "within3MinCount": 19,
  "within3MinPercentage": "59.4%",
  "within5MinCount": 26,
  "within5MinPercentage": "81.3%",
  "within15MinCount": 31,
  "within15MinPercentage": "96.9%",
  "sampleDetailedResults": [
    {
      "flightIndicative": "TAM3886",
      "actualDurationMs": 5400000,
      "predictedDurationMs": 5520000,
      "timeDifferenceMs": 120000,
      "timeDifferenceMinutes": 2.0,
      "within3Min": true,
      "within5Min": true,
      "within15Min": true,
      "actualDepartureTime": 1705312200000,
      "actualArrivalTime": 1705317600000,
      "predictedTimeString": "[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]"
    }
  ]
}
```

#### **Status Codes**
- `200` - KPIs calculated successfully
- `500` - Error calculating KPIs

---

## Testing Punctuality Analysis Endpoints

### **Using cURL**

#### **Run Punctuality Analysis**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/run
```

#### **Get Analysis Statistics**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/stats
```

#### **Find Qualifying Flights**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/qualifying-flights
```

#### **Extract Airport Coordinates**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/airport-coordinates
```

#### **Match Predicted with Real Flights**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/match-flights
```

#### **Apply Geographic Validation**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/geographic-validation
```

#### **Calculate Punctuality KPIs**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/punctuality-kpis
```

#### **Health Check**
```bash
curl -X GET http://localhost:8080/api/punctuality-analysis/health
```

---

## Future Enhancements

### **Planned Features**
1. **Authentication**: JWT-based authentication
2. **Rate Limiting**: API rate limiting
3. **Pagination**: Support for large result sets
4. **Webhooks**: Real-time notifications
5. **Metrics**: Prometheus metrics endpoint
6. **Comparison Service**: Direct API endpoints for comparing predicted vs actual flights
7. **Prediction Analytics**: Accuracy metrics and performance analysis

### **API Versioning**
- Current version: `v1`
- Future versions will use URL versioning: `/api/v2/flights/...` and `/api/v2/predicted-flights/...` 