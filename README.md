# Aviation Flight Data Processing System

A comprehensive Java Spring Boot application for processing and analyzing aviation tracking data, featuring both batch processing and real-time streaming capabilities.

## Overview

This application processes aviation replay data from JSON files and provides real-time streaming capabilities, designed to work with data containing:
- **Real Path Points** (`listRealPath`) - Real-time aircraft tracking data
- **Flight Intentions** (`listFlightIntention`) - Planned flight schedules and information
- **Timestamp** - Global reference time for the dataset (stored as String)

## Features

### Real-Time Streaming (Production)
- **Live Data Processing**: Process `ReplayPath` packets in real-time via REST API
- **Batch Processing**: Efficient batch processing of multiple packets
- **MongoDB Integration**: Store flight data and tracking points in MongoDB
- **RESTful API**: HTTP endpoints for packet processing and data retrieval
- **Upsert Operations**: Smart data merging and deduplication
- **Health Monitoring**: Built-in health checks and statistics endpoints

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
│   │   │   │   │   └── StreamingController.java     # REST API endpoints
│   │   │   │   ├── model/                           # Data models
│   │   │   │   │   ├── ReplayData.java              # Batch data container
│   │   │   │   │   ├── ReplayPath.java              # Streaming packet format
│   │   │   │   │   ├── RealPathPoint.java           # Tracking data points
│   │   │   │   │   ├── FlightIntention.java         # Flight plan data
│   │   │   │   │   ├── Kinematic.java               # Position/movement data
│   │   │   │   │   ├── JoinedFlightData.java        # MongoDB storage format
│   │   │   │   │   └── TrackingPoint.java           # Individual tracking points
│   │   │   │   ├── service/                         # Business logic
│   │   │   │   │   ├── ReplayDataService.java       # Data analysis service
│   │   │   │   │   ├── StreamingFlightService.java  # Core streaming logic
│   │   │   │   │   ├── FlightDataJoinService.java   # Data joining service
│   │   │   │   │   └── DataAnalysisService.java     # Statistical analysis
│   │   │   │   └── repository/
│   │   │   │       └── FlightRepository.java        # MongoDB repository
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

| Method | Endpoint | Description | Input Format |
|--------|----------|-------------|--------------|
| `POST` | `/api/flights/process-packet` | Process single ReplayPath packet | Single `ReplayPath` object |
| `POST` | `/api/flights/process-batch-packets` | Process multiple ReplayPath packets efficiently | Array of `ReplayPath` objects |
| `POST` | `/api/flights/process-batch` | Process batch ReplayData (legacy, for testing) | Single `ReplayData` object |
| `GET` | `/api/flights/stats` | Get flight statistics | None |
| `GET` | `/api/flights/health` | Health check | None |

### Important API Notes

- **Production**: Use `/process-packet` for real-time single packets or `/process-batch-packets` for efficient batch processing
- **Testing**: Use `/process-batch` for testing with legacy JSON file format
- **Data Order**: JSON property order doesn't matter - fields are matched by name
- **Time Format**: The `time` field is stored as a String (can be timestamp or formatted date)

### Example API Usage

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

# Process multiple packets efficiently (recommended for batch operations)
curl -X POST http://localhost:8080/api/flights/process-batch-packets \
  -H "Content-Type: application/json" \
  -d '[
    {"time": "1626789600000", "listRealPath": [...], "listFlightIntention": [...]},
    {"time": "1626789600100", "listRealPath": [...], "listFlightIntention": [...]}
  ]'

# Process legacy batch file (for testing)
curl -X POST http://localhost:8080/api/flights/process-batch \
  -H "Content-Type: application/json" \
  -d @inputData/replay2.json

# Get statistics
curl http://localhost:8080/api/flights/stats
```

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
db.flights.find().limit(5)
db.flights.countDocuments()
```



### Production Data Flow
In production, data comes through the streaming API:
- **Real-time packets**: `POST /api/flights/process-packet` - Processes individual `ReplayPath` packets
- **Batch processing**: `POST /api/flights/process-batch-packets` - Efficiently processes multiple `ReplayPath` packets



##  Data Models

The application uses strongly-typed Java models:

- **ReplayPath**: Real-time packet structure for streaming (time as String, listRealPath, listFlightIntention)
- **ReplayData**: Legacy batch container with same fields as ReplayPath
- **RealPathPoint**: Individual tracking points with position, speed, flight level
- **FlightIntention**: Planned flight data with call signs, aircraft types, routes
- **Kinematic**: Position and movement data including lat/lon coordinates
- **JoinedFlightData**: Combined flight and tracking data for MongoDB storage
- **TrackingPoint**: Individual tracking data points within flights

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
# Test with existing data
curl -X POST http://localhost:8080/api/flights/process-batch \
  -H "Content-Type: application/json" \
  -d @inputData/replay2.json

# Check results
curl http://localhost:8080/api/flights/stats
```

### Performance Testing
```bash
# Test batch processing performance
curl -X POST http://localhost:8080/api/flights/process-batch-packets \
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


2. **MongoDB connection failed**: Ensure MongoDB is running and accessible
3. **Compilation errors**: Run `mvn clean compile` after model changes
4. **Test failures**: Check if test data format matches current model structure

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.data.mongodb: DEBUG
```


