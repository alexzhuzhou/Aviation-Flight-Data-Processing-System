# API Documentation

This document provides comprehensive information about the Aviation Replay Data Processor REST API, including both flight tracking and predicted flight endpoints.

## API Overview

The API provides endpoints for real-time processing of aviation flight tracking data and predicted flight data, including packet processing, statistics, data quality monitoring, and prediction comparison capabilities.

### **Base URL**
- **Development**: `http://localhost:8080`
- **Production**: `https://api.aviation.example.com`

### **API Documentation**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## Flight Tracking Endpoints

These endpoints handle actual flight tracking data and real-time packet processing.

### **1. Process Streaming Packet** 🚀
**POST** `/api/flights/process-packet`

Processes a single ReplayPath packet containing flight intentions and tracking points.

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

### **2. Get Statistics** 📊
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

### **3. Analyze Duplicates** 🔍
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

### **5. Process Batch Data** 📦
**POST** `/api/flights/process-batch`

Processes a complete ReplayData object (typically from a JSON file).

#### **Request Body**
```json
{
  "time": "1705312200000",
  "listFlightIntention": [...],
  "listRealPath": [...]
}
```

#### **Response**
```json
{
  "newFlights": 5,
  "updatedFlights": 3,
  "message": "Processed 5 new flights, updated 3 flights with tracking data"
}
```

#### **Status Codes**
- `200` - Batch processing completed successfully
- `500` - Error during batch processing

---

### **6. Cleanup Duplicates** 🧹
**POST** `/api/flights/cleanup-duplicates`

Removes duplicate tracking points from all flights in the database.

#### **Response**
```
Cleanup completed: 1250 duplicate tracking points removed
```

#### **Status Codes**
- `200` - Cleanup completed successfully
- `500` - Error during cleanup

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