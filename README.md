# Aviation Flight Data Processing System

A comprehensive Java Spring Boot application for processing and analyzing aviation tracking data, featuring Oracle database integration, real-time streaming capabilities, and advanced punctuality analysis, with support for predicted flight data comparison.

## Overview

This application processes aviation flight data from multiple sources and provides comprehensive analysis capabilities, designed to work with:
- **Oracle Database Integration** - Direct extraction from Sigma production database
- **Real Path Points** (`listRealPath`) - Real-time aircraft tracking data
- **Flight Intentions** (`listFlightIntention`) - Planned flight schedules and information
- **Predicted Flight Data** - Predicted flight routes and timing for comparison with actual data
- **Punctuality Analysis** - ICAO KPI14 compliance analysis with multiple tolerance windows
- **Timestamp** - Global reference time for the dataset (stored as String)

## Features

### Oracle Database Integration (Production)
- **Direct Database Access**: Extract flight data directly from Sigma Oracle production database
- **Automated Processing**: Process hardcoded date (2025-07-11) flight data automatically
- **Performance Metrics**: Detailed timing metrics for database operations
- **Connection Management**: Robust Oracle database connection handling
- **Error Resilience**: Comprehensive error handling and reporting

### Real-Time Streaming (Production)
- **Live Data Processing**: Process `ReplayPath` packets in real-time via REST API
- **Oracle Data Processing**: Direct processing from Oracle database via REST endpoints
- **Predicted Flight Processing**: Process predicted flight data for comparison analysis
- **Batch Processing**: Efficient batch processing of multiple packets
- **MongoDB Integration**: Store flight data and tracking points in MongoDB with separate collections
- **RESTful API**: HTTP endpoints for packet processing and data retrieval
- **Upsert Operations**: Smart data merging and deduplication
- **Health Monitoring**: Built-in health checks and statistics endpoints
- **Data Comparison**: Separate storage for predicted vs actual flight data using planId matching

### Punctuality Analysis (ICAO KPI14)
- **Flight Qualification**: Find flights with SBSP ↔ SBRJ routes and AERODROME endpoints
- **Flight Matching**: Match predicted flights with real flights via instanceId/planId
- **Geographic Validation**: Filter flights based on 2 NM threshold and flight level ≤ 4
- **Coordinate Extraction**: Extract airport coordinates from route elements
- **Time Comparison**: Compare predicted vs actual flight times
- **KPI Calculation**: Calculate percentage of flights within delay tolerance windows (±3, ±5, ±15 minutes)
- **Statistical Reporting**: Generate comprehensive punctuality analysis reports
- **Analysis Pipeline**: Step-by-step validation and analysis workflow



## Prerequisites

- Java 15 or higher
- Maven 3.8+ (tested with 3.8.3)
- MongoDB (for streaming features) - can be run via Docker
- Oracle Database access (for production Sigma integration)
- Data files in the `inputData/` folder (for testing only)

## 🚀 Quick Setup

**For detailed setup instructions, see [SETUP.md](SETUP.md)**

### Essential Steps:
1. **Clone and build**: `mvn clean compile`
2. **Setup MongoDB**: `docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest`
3. **Create config files**:
   ```bash
   cp src/main/java/com/example/config/OracleConfig.java.template src/main/java/com/example/config/OracleConfig.java
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   ```
4. **Set credentials**: Update `application.yml` and set environment variables
5. **Run**: `mvn spring-boot:run`

⚠️ **Security Note**: Configuration files with actual credentials are automatically excluded from git commits.

##  Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/example/
│   │   │   │   ├── StreamingFlightApplication.java  # Spring Boot main class
│   │   │   │   ├── controller/
│   │   │   │   │   ├── StreamingController.java     # REST API endpoints with Oracle integration
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
│   │   │   │   │   ├── PunctualityAnalysisResult.java # Punctuality analysis results
│   │   │   │   │   ├── OracleProcessingResult.java  # NEW: Oracle processing results
│   │   │   │   │   └── BatchProcessingResult.java   # NEW: Batch processing results
│   │   │   │   ├── service/                         # Business logic
│   │   │   │   │   ├── StreamingFlightService.java  # Core streaming logic
│   │   │   │   │   ├── OracleDataExtractionService.java # Oracle database integration
│   │   │   │   │   ├── OracleFlightDataService.java # Oracle flight data operations
│   │   │   │   │   ├── PredictedFlightService.java  # Predicted flight processing
│   │   │   │   │   ├── PunctualityAnalysisService.java # Punctuality analysis service
│   │   │   │   │   ├── TrajectoryAccuracyAnalysisService.java # Trajectory accuracy analysis
│   │   │   │   │   └── TrajectoryDensificationService.java # Trajectory densification
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
# Note: Batch processing is now handled through REST API endpoints
# Use the streaming service endpoints instead
```

##  Streaming API Endpoints

### Flight Tracking Endpoints

| Method | Endpoint | Description | Input Format |
|--------|----------|-------------|--------------|
| `POST` | `/api/flights/process-packet` | **NEW:** Process flight data directly from Oracle database | None (uses hardcoded date 2025-07-11) |
| `POST` | `/api/flights/process-packet-legacy` | Process single ReplayPath packet (legacy JSON input) | Single `ReplayPath` object |
| `GET` | `/api/flights/test-oracle-connection` | **NEW:** Test Oracle database connection | None |

| `GET` | `/api/flights/plan-ids` | Get all planIds for prediction scripts | None |
| `GET` | `/api/flights/stats` | Get flight statistics | None |
| `GET` | `/api/flights/health` | Health check | None |
| `GET` | `/api/flights/analyze-duplicates` | Analyze duplicate indicatives | None |
| `POST` | `/api/flights/cleanup-duplicates` | Clean up duplicate tracking points | None |

### **🆕 Oracle-Based Predicted Flight Endpoints**

| Method | Endpoint | Description | Input Format |
|--------|----------|-------------|--------------|
| `POST` | `/api/predicted-flights/process` | **NEW:** Process single planId from Oracle | `{"planId": 17879345}` |
| `POST` | `/api/predicted-flights/batch` | **NEW:** Batch process multiple planIds from Oracle | `{"planIds": [17879345, 17879346]}` |
| `GET` | `/api/predicted-flights/stats` | Get predicted flight statistics | None |
| `GET` | `/api/predicted-flights/health` | Health check for predicted flights service | None |

### Punctuality Analysis Endpoints

| Method | Endpoint | Description | Input Format |
|--------|----------|-------------|--------------|
| `GET` | `/api/punctuality-analysis/match-flights` | Match predicted flights with real flights | None |

| `GET` | `/api/punctuality-analysis/stats` | Get analysis statistics | None |
| `GET` | `/api/punctuality-analysis/health` | Health check for punctuality analysis | None |

### Important API Notes

- **Production**: Use `/process-packet` for Oracle database integration (no JSON input required)
- **Legacy**: Use `/process-packet-legacy` for JSON-based packet processing
- **Oracle Integration**: Direct database access eliminates need for external data files
- **Data Order**: JSON property order doesn't matter - fields are matched by name
- **Time Format**: The `time` field is stored as a String (can be timestamp or formatted date)

### Example API Usage

#### Flight Tracking API

```bash
# Health check
curl http://localhost:8080/api/flights/health

# NEW: Process flight data directly from Oracle database (production)
curl -X POST http://localhost:8080/api/flights/process-packet

# NEW: Test Oracle database connection
curl http://localhost:8080/api/flights/test-oracle-connection



# Process a single packet (legacy JSON input)
curl -X POST http://localhost:8080/api/flights/process-packet-legacy \
  -H "Content-Type: application/json" \
  -d '{
    "time": "1626789600000",
    "listRealPath": [...],
    "listFlightIntention": [...]
  }'

# Get statistics
curl http://localhost:8080/api/flights/stats

# Get all planIds for prediction scripts
curl http://localhost:8080/api/flights/plan-ids
```

#### **🆕 Oracle-Based Predicted Flights API**

```bash
# Process single planId from Oracle database
curl -X POST http://localhost:8080/api/predicted-flights/process \
  -H "Content-Type: application/json" \
  -d '{"planId": 17879345}'

# Response example:
{
  "totalRequested": 1,
  "totalProcessed": 1,
  "totalNotFound": 0,
  "totalErrors": 0,
  "processedPlanIds": [17879345],
  "notFoundPlanIds": [],
  "errorPlanIds": [],
  "extractionTimeMs": 45,
  "processingTimeMs": 123,
  "message": "Successfully processed 1 out of 1 requested planIds"
}

# Batch process multiple planIds from Oracle database
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d '{"planIds": [17879345, 17879346, 17879347]}'

# Response example:
{
  "totalRequested": 3,
  "totalProcessed": 2,
  "totalNotFound": 1,
  "totalErrors": 0,
  "processedPlanIds": [17879345, 17879346],
  "notFoundPlanIds": [17879347],
  "errorPlanIds": [],
  "extractionTimeMs": 89,
  "processingTimeMs": 234,
  "message": "Successfully processed 2 out of 3 requested planIds. 1 planId not found in Oracle database."
}

# Get predicted flight statistics
curl http://localhost:8080/api/predicted-flights/stats

# Health check for predicted flights
curl http://localhost:8080/api/predicted-flights/health
```

#### **🆕 Punctuality Analysis API**

```bash
# Match predicted flights with real flights
curl http://localhost:8080/api/punctuality-analysis/match-flights

# Response example:
{
  "totalPredictedFlights": 150,
  "totalRealFlights": 200,
  "totalMatched": 145,
  "matchingRate": 96.7,
  "matchedFlights": [
    {
      "planId": 17879345,
      "predictedIndicative": "TAM3886",
      "realIndicative": "TAM3886",
      "predictedTime": "2025-07-11T10:30:00Z",
      "realTime": "2025-07-11T10:32:15Z",
      "timeDifferenceMinutes": 2.25
    }
  ],
  "processingTimeMs": 456
}

# Run punctuality KPI analysis (ICAO KPI14)
curl http://localhost:8080/api/punctuality-analysis/punctuality-kpis

# Response example:
{
  "totalMatchedFlights": 145,
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
  "analysisTimestamp": "2024-12-19T10:30:45Z",
  "processingTimeMs": 1234,
  "message": "Analysis completed: 145 predicted flights matched, 140 analyzed successfully"
}

# Get analysis statistics
curl http://localhost:8080/api/punctuality-analysis/stats

# Health check
curl http://localhost:8080/api/punctuality-analysis/health
```

## 🚀 **NEW: Oracle Integration Workflow**

### **Complete Oracle-Based Processing Pipeline**

The application now provides a complete Oracle-based workflow that eliminates the need for external JSON files:

#### **Step 1: Test Oracle Connection**
```bash
# Verify Oracle database connectivity
curl http://localhost:8080/api/flights/test-oracle-connection

# Expected response:
{
  "status": "SUCCESS",
  "message": "Oracle connection successful",
  "connectionTimeMs": 234,
  "databaseInfo": {
    "host": "10.103.3.8:1521",
    "serviceName": "SIGMA_PLT3_DEV1_APP",
    "username": "sigma"
  }
}
```

#### **Step 2: Process Flight Data from Oracle**
```bash
# Extract and process flight data directly from Oracle database
curl -X POST http://localhost:8080/api/flights/process-packet

# Expected response:
{
  "status": "SUCCESS",
  "totalFlightsExtracted": 1243,
  "totalFlightsProcessed": 1200,
  "totalTrackingPoints": 45678,
  "extractionTimeMs": 2345,
  "processingTimeMs": 5678,
  "message": "Successfully processed 1200 flights from Oracle database for date 2025-07-11"
}
```

#### **Step 3: Get Available PlanIds for Predictions**
```bash
# Get all planIds that can be used for prediction generation
curl http://localhost:8080/api/flights/plan-ids

# Expected response:
{
  "totalCount": 1200,
  "planIds": [17879345, 17879346, 17879347, ...],
  "processingTimeMs": 45,
  "message": "Retrieved planIds from processed flights"
}
```

#### **Step 4: Process Predicted Flights from Oracle**
```bash
# Process single planId prediction
curl -X POST http://localhost:8080/api/predicted-flights/process \
  -H "Content-Type: application/json" \
  -d '{"planId": 17879345}'

# Process multiple planIds in batch
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d '{"planIds": [17879345, 17879346, 17879347]}'
```

#### **Step 5: Run Punctuality Analysis**
```bash
# Match predicted flights with real flights and analyze punctuality KPIs
curl http://localhost:8080/api/punctuality-analysis/punctuality-kpis

# Get detailed flight matching information
curl http://localhost:8080/api/punctuality-analysis/match-flights
```

### **Oracle Integration Benefits**

- ✅ **No External Files**: Direct database access eliminates JSON file dependencies
- ✅ **Real Production Data**: Uses actual Sigma production database
- ✅ **Automatic Processing**: Processes hardcoded date (2025-07-11) automatically
- ✅ **Performance Metrics**: Detailed timing for all database operations
- ✅ **Error Handling**: Comprehensive error reporting and recovery
- ✅ **Option A Strategy**: Skip missing planIds, report detailed statistics

## 🚀 **NEW: Batch Processing Workflow**

### **Efficient Oracle-Based Prediction Processing**

For processing large batches of predicted flight data using Oracle database integration:

#### **Step 1: Process Real Flight Data from Oracle**
```bash
# First, extract and process real flight data from Oracle
curl -X POST http://localhost:8080/api/flights/process-packet

# This populates the database with real flight data for comparison
```

#### **Step 2: Get All Available PlanIds**
```bash
# Get all planIds from processed real flight data
curl http://localhost:8080/api/flights/plan-ids

# Response: {"totalCount": 1243, "planIds": [17871744, 17873112, ...], "processingTimeMs": 45}
```

#### **Step 3: Generate Predictions Using Oracle Data** 
```python
# In your prediction script - now using Oracle-extracted planIds
import requests

# Get planIds from Oracle-processed data
response = requests.get("http://localhost:8080/api/flights/plan-ids")
data = response.json()
planIds = data["planIds"]  # [17871744, 17873112, ...]

# Generate predictions for Oracle planIds
# Your prediction algorithm can now use the same planIds that exist in the real data
```

#### **Step 4: Process Predictions via Oracle Integration** 
```python
# Send planIds for Oracle-based prediction processing
# The system will extract flight data from Oracle for each planId
planIds_to_process = [17879345, 17879346, 17879347]

result = requests.post(
    "http://localhost:8080/api/predicted-flights/batch",
    json={"planIds": planIds_to_process}
)

print(f"Oracle Processing Result: {result.json()}")
# Response: {
#   "totalRequested": 3,
#   "totalProcessed": 2, 
#   "totalNotFound": 1,
#   "totalErrors": 0,
#   "processedPlanIds": [17879345, 17879346],
#   "notFoundPlanIds": [17879347],
#   "errorPlanIds": [],
#   "extractionTimeMs": 89,
#   "processingTimeMs": 234
# }
```

### **Oracle-Based Batch Processing Features**
- ✅ **Direct Oracle Access**: Extracts flight data directly from Sigma database
- ✅ **Option A Error Handling**: Skip missing planIds, report detailed statistics  
- ✅ **Performance Metrics**: Separate timing for extraction vs processing
- ✅ **Automatic Data Validation**: Ensures planIds exist in Oracle before processing
- ✅ **Detailed Reporting**: Complete breakdown of processed, not found, and error planIds

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

### Oracle Database Setup (Production)

For Oracle integration, ensure your `application.yml` includes Oracle database configuration:
```yaml
# Oracle database configuration for Sigma integration
spring:
  datasource:
    sigma:
      url: jdbc:oracle:thin:@//your-oracle-host:1521/your-service-name
      username: ${ORACLE_USERNAME}
      password: ${ORACLE_PASSWORD}
      driver-class-name: oracle.jdbc.OracleDriver
```

**Environment Variables:**
- `ORACLE_USERNAME`: Oracle database username
- `ORACLE_PASSWORD`: Oracle database password

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
- **OracleProcessingResult**: **NEW** - Results from Oracle database processing with performance metrics
- **BatchProcessingResult**: **NEW** - Results from batch processing operations

### Key Model Changes
- **Time Field**: Changed from `long` to `String` to handle various timestamp formats
- **Flexible JSON**: Property order in JSON doesn't matter - matched by field name
- **MongoDB Ready**: Optimized for efficient storage and querying
- **Oracle Integration**: New models support direct database extraction and processing metrics

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
# Perform arrival punctuality KPI analysis
curl http://localhost:8080/api/punctuality-analysis/punctuality-kpis

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


