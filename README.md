# Aviation Flight Data Processing System

A comprehensive Java Spring Boot application for processing and analyzing aviation tracking data, featuring both batch processing and real-time streaming capabilities, with support for predicted flight data comparison.

## Overview

This application processes aviation replay data from JSON files and provides real-time streaming capabilities, designed to work with data containing:
- **Real Path Points** (`listRealPath`) - Real-time aircraft tracking data
- **Flight Intentions** (`listFlightIntention`) - Planned flight schedules and information
- **Predicted Flight Data** - Predicted flight routes and timing for comparison with actual data
- **Timestamp** - Global reference time for the dataset (stored as String)

## Features

### Real-Time Streaming (Production)
- **Live Data Processing**: Process `ReplayPath` packets in real-time via REST API
- **Predicted Flight Processing**: Process predicted flight data for comparison analysis
- **Batch Processing**: Efficient batch processing of multiple packets
- **MongoDB Integration**: Store flight data and tracking points in MongoDB with separate collections
- **RESTful API**: HTTP endpoints for packet processing and data retrieval
- **Upsert Operations**: Smart data merging and deduplication
- **Health Monitoring**: Built-in health checks and statistics endpoints
- **Data Comparison**: Separate storage for predicted vs actual flight data using planId matching

### Punctuality Analysis (ICAO KPI14)
- **Arrival Punctuality Analysis**: Compare predicted en-route time with executed flight time
- **KPI Calculation**: Calculate percentage of flights within delay tolerance windows (±3, ±5, ±15 minutes)
- **Flight Matching**: Match predicted flights with real flights via instanceId/planId
- **Time Parsing**: Parse complex time formats from predicted flight data
- **Statistical Reporting**: Generate comprehensive punctuality analysis reports

### Batch Processing (Development/Testing)
- **Data Loading**: Parse large JSON replay files efficiently for testing
- **Statistical Analysis**: Generate summaries and statistics about flight data

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MongoDB (for streaming features) - can be run via Docker
- Data files in the `inputData/` folder (for testing only)

##  Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/example/
│   │   │   │   ├── StreamingFlightApplication.java  # Spring Boot main class
│   │   │   │   ├── controller/
│   │   │   │   │   ├── StreamingController.java     # REST API endpoints
│   │   │   │   │   ├── PredictedFlightController.java # Predicted flights API
│   │   │   │   │   └── PunctualityAnalysisController.java # Punctuality analysis API
│   │   │   │   ├── model/                           # Data models
│   │   │   │   │   ├── ReplayData.java              # Batch data container
│   │   │   │   │   ├── ReplayPath.java              # Streaming packet format
│   │   │   │   │   ├── RealPathPoint.java           # Tracking data points
│   │   │   │   │   ├── FlightIntention.java         # Flight plan data
│   │   │   │   │   ├── Kinematic.java               # Position/movement data
│   │   │   │   │   ├── JoinedFlightData.java        # MongoDB storage format
│   │   │   │   │   ├── TrackingPoint.java           # Individual tracking points
│   │   │   │   │   ├── PredictedFlightData.java     # Predicted flight data
│   │   │   │   │   ├── RouteElement.java            # Predicted route elements
│   │   │   │   │   ├── RouteSegment.java            # Predicted route segments
│   │   │   │   │   └── PunctualityAnalysisResult.java # Punctuality analysis results
│   │   │   │   ├── service/                         # Business logic
│   │   │   │   │   ├── ReplayDataService.java       # Data analysis service
│   │   │   │   │   ├── StreamingFlightService.java  # Core streaming logic
│   │   │   │   │   ├── FlightDataJoinService.java   # Data joining service
│   │   │   │   │   ├── DataAnalysisService.java     # Statistical analysis
│   │   │   │   │   ├── PredictedFlightService.java  # Predicted flight processing
│   │   │   │   │   └── PunctualityAnalysisService.java # Punctuality analysis service
│   │   │   │   └── repository/
│   │   │   │       ├── FlightRepository.java        # MongoDB repository
│   │   │   │       └── PredictedFlightRepository.java # Predicted flights repository
│   │   │   └── resources/
│   │   │       └── application.yml                  # Configuration
│   │   └── test/
│   │       ├── java/                               # Test source code
│   │       └── resources/                          # Test resources
├── pom.xml                                         # Maven configuration
├── STREAMING_SETUP.md                              # Streaming setup guide
└── README.md                                       # This file
```

##  Building the Project

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Build the JAR file
mvn package

# Clean build (recommended after model changes)
mvn clean package
```

##  Running the Application

### Option 1: Streaming Service (Production Mode)

```bash
# Start the Spring Boot streaming service
mvn spring-boot:run
```

The streaming service will start on `http://localhost:8080`

### Option 2: Batch Processing (Development/Testing Only)

```bash
# Run the batch processing application (for testing with JSON files)
mvn exec:java -Dexec.mainClass="com.example.App"
```

##  Streaming API Endpoints

### Flight Tracking Endpoints

| Method | Endpoint | Description | Input Format |
|--------|----------|-------------|--------------|
| `POST` | `/api/flights/process-packet` | Process single ReplayPath packet | Single `ReplayPath` object |

| `GET` | `/api/flights/plan-ids` | **NEW:** Get all planIds for prediction scripts | None |
| `GET` | `/api/flights/stats` | Get flight statistics | None |
| `GET` | `/api/flights/health` | Health check | None |
| `GET` | `/api/flights/analyze-duplicates` | Analyze duplicate indicatives | None |
| `POST` | `/api/flights/cleanup-duplicates` | Clean up duplicate tracking points | None |

### Predicted Flight Endpoints

| Method | Endpoint | Description | Input Format |
|--------|----------|-------------|--------------|
| `POST` | `/api/predicted-flights/process` | Process predicted flight data | Predicted flight JSON object |
| `POST` | `/api/predicted-flights/batch` | **NEW:** Batch process multiple predicted flights | Array of predicted flight objects |
| `GET` | `/api/predicted-flights/stats` | Get predicted flight statistics | None |
| `GET` | `/api/predicted-flights/health` | Health check for predicted flights service | None |

### Important API Notes

- **Production**: Use `/process-packet` for real-time single packet processing

- **Data Order**: JSON property order doesn't matter - fields are matched by name
- **Time Format**: The `time` field is stored as a String (can be timestamp or formatted date)

### Example API Usage

#### Flight Tracking API

```bash
# Health check
curl http://localhost:8080/api/flights/health

# Process a single packet
curl -X POST http://localhost:8080/api/flights/process-packet \
  -H "Content-Type: application/json" \
  -d '{
    "time": "1626789600000",
    "listRealPath": [...],
    "listFlightIntention": [...]
  }'



# Get statistics
curl http://localhost:8080/api/flights/stats

# NEW: Get all planIds for prediction scripts
curl http://localhost:8080/api/flights/plan-ids
```

#### Predicted Flights API

```bash
# Process single predicted flight data
curl -X POST http://localhost:8080/api/predicted-flights/process \
  -H "Content-Type: application/json" \
  -d '{
    "instanceId": 17879345,
    "routeId": 51435982,
    "id": 51637804,
    "indicative": "TAM3886",
    "time": "[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]",
    "routeElements": [...],
    "routeSegments": [...]
  }'

# NEW: Batch process multiple predicted flights (efficient for 1000+ records)
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d @predicted_flights_batch.json

# Get predicted flight statistics
curl http://localhost:8080/api/predicted-flights/stats

# Health check for predicted flights
curl http://localhost:8080/api/predicted-flights/health
```

## 🚀 **NEW: Batch Processing Workflow**

### **Efficient Prediction Data Processing**

For processing large batches of predicted flight data (1000+ records), use this optimized workflow:

#### **Step 1: Get All PlanIds**
```bash
# Get all planIds from actual flight data
curl http://localhost:8080/api/flights/plan-ids
# Response: {"totalCount": 1243, "planIds": [17871744, 17873112, ...], "processingTimeMs": 45}
```

#### **Step 2: Generate Predictions** 
```python
# In your prediction script
import requests

# Get planIds
response = requests.get("http://localhost:8080/api/flights/plan-ids")
data = response.json()
planIds = data["planIds"]  # [17871744, 17873112, ...]

# Generate predictions for all planIds
predictedFlights = []
for planId in planIds:
    predicted = generatePredictedFlight(planId)  # Your prediction logic
    predictedFlights.append(predicted)
```

#### **Step 3: Batch Upload** 
```python
# Send all predictions in one efficient batch
result = requests.post(
    "http://localhost:8080/api/predicted-flights/batch",
    json=predictedFlights
)

print(f"Processed: {result.json()}")
# Response: {"totalReceived": 1243, "totalProcessed": 1200, "totalSkipped": 43, "totalFailed": 0}
```

### **Batch Processing Features**
- ✅ **Optimal Performance**: Processes 500 records per database batch
- ✅ **Skip Duplicates**: Automatically skips existing planIds
- ✅ **Error Resilience**: Saves what it can, reports failures
- ✅ **Progress Tracking**: Detailed processing metrics
- ✅ **Transaction Safety**: Database consistency guaranteed

##  Configuration

### MongoDB Setup

Create `src/main/resources/application.yml` (not tracked in Git):
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: aviation_db

server:
  port: 8080

logging:
  level:
    com.example: INFO
    org.springframework.data.mongodb: WARN
```

### Running MongoDB

```bash
# Using Docker (recommended)
docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest

# Check if running
docker ps | grep mongo

# Connect to MongoDB shell
docker exec -it aviation_mongodb mongosh

# Or install MongoDB locally
# Follow instructions at https://www.mongodb.com/try/download/community
```

### Database Usage

```bash
# In MongoDB shell
use aviation_db
show collections

# View actual flight data
db.flights.find().limit(5)
db.flights.countDocuments()

# View predicted flight data
db.predicted_flights.find().limit(5)
db.predicted_flights.countDocuments()

# Find flights by planId for comparison
db.flights.find({"planId": 51637804})
db.predicted_flights.find({"planId": 51637804})
```



### Production Data Flow
In production, data comes through the streaming API:
- **Real-time packets**: `POST /api/flights/process-packet` - Processes individual `ReplayPath` packets
- **Single packet processing**: `POST /api/flights/process-packet` - Processes individual `ReplayPath` packets



##  Data Models

The application uses strongly-typed Java models:

- **ReplayPath**: Real-time packet structure for streaming (time as String, listRealPath, listFlightIntention)
- **ReplayData**: Legacy batch container with same fields as ReplayPath
- **RealPathPoint**: Individual tracking points with position, speed, flight level
- **FlightIntention**: Planned flight data with call signs, aircraft types, routes
- **Kinematic**: Position and movement data including lat/lon coordinates
- **JoinedFlightData**: Combined flight and tracking data for MongoDB storage (flights collection)
- **TrackingPoint**: Individual tracking data points within flights
- **PredictedFlightData**: Predicted flight route and timing data for MongoDB storage (predicted_flights collection)
- **RouteElement**: Individual route elements in predicted flight paths
- **RouteSegment**: Route segments connecting route elements
- **PunctualityAnalysisResult**: Results of arrival punctuality analysis with KPI metrics and delay tolerance windows

### Key Model Changes
- **Time Field**: Changed from `long` to `String` to handle various timestamp formats
- **Flexible JSON**: Property order in JSON doesn't matter - matched by field name
- **MongoDB Ready**: Optimized for efficient storage and querying

## Dependencies

- **Spring Boot 3.1.2**: Web framework and auto-configuration
- **Spring Data MongoDB**: MongoDB integration with automatic connection pooling
- **Jackson**: JSON parsing and data binding with flexible field matching
- **JUnit 5**: Testing framework with container support
- **Java Time API**: Timestamp handling and formatting

##  Testing

### Run Tests
```bash
# Run all tests
mvn test

# Clean and test (recommended after model changes)
mvn clean test

# Test specific class
mvn test -Dtest=StreamingFlightServiceTest
```

### Integration Testing
```bash


# Check flight tracking results
curl http://localhost:8080/api/flights/stats

# Test predicted flight processing
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

# Check predicted flights results
curl http://localhost:8080/api/predicted-flights/stats

### Punctuality Analysis API

#### **Run Punctuality Analysis (ICAO KPI14)**
```bash
# Perform arrival punctuality analysis
curl http://localhost:8080/api/punctuality-analysis/run

# Expected response:
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

#### **Get Analysis Statistics**
```bash
# Get statistics about available data for analysis
curl http://localhost:8080/api/punctuality-analysis/stats

# Response:
{
  "totalPredictedFlights": 150,
  "totalRealFlights": 200,
  "analysisCapability": true
}
```

#### **Health Check**
```bash
# Check if punctuality analysis service is running
curl http://localhost:8080/api/punctuality-analysis/health

# Response: "Punctuality Analysis Service is running"
```

### Performance Testing
```bash
# Test batch processing performance
# Use the predicted flights batch endpoint for performance testing
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d @your_batch_file.json
```

##  Deployment

### Local Development
1. Start MongoDB: `docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest`
2. Create `application.yml` with database settings
3. Run: `mvn spring-boot:run`

### Remote/Production
1. Ensure Java 17+ and MongoDB are available
2. Create appropriate `application.yml` for the environment
3. Build: `mvn clean package`
4. Deploy: `java -jar target/java-project-1.0.0.jar`

### Docker Deployment (Future)
```dockerfile
# Dockerfile example for containerized deployment
FROM openjdk:17-jdk-slim
COPY target/java-project-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

##  Documentation

- **STREAMING_SETUP.md**: Detailed setup guide for streaming features
- **API Documentation**: Available endpoints and usage examples
- **Model Documentation**: Data structure specifications

##  Troubleshooting

### Common Issues

1. **Port 8080 already in use**: Change port in `application.yml` or stop other services
2. **MongoDB connection failed**: Ensure MongoDB is running and accessible
3. **Compilation errors**: Run `mvn clean compile` after model changes
4. **Test failures**: Check if test data format matches current model structure
5. **Predicted flight processing errors**: Verify JSON format includes 'id' field for planId mapping

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.data.mongodb: DEBUG
```


