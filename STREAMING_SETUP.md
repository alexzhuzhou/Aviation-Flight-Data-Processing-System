#  Streaming Flight Data System Setup Guide

## Overview

This guide explains how to set up the streaming flight data system that processes both actual flight tracking data (`ReplayPath` packets) and predicted flight data in real-time, storing them in MongoDB. The system supports individual packet processing, efficient batch processing for high-volume scenarios, and prediction comparison capabilities.

## Data Flow & Architecture

```
Actual Flight Data:
External System (PathVoGenerator/Test Code)
    ↓ HTTP POST requests
Your REST API (StreamingController)
    ↓ processes packets
StreamingFlightService
    ↓ upserts data
MongoDB Database (flights collection)

Predicted Flight Data:
Prediction System
    ↓ HTTP POST requests
Your REST API (PredictedFlightController)
    ↓ processes predicted flight data
PredictedFlightService
    ↓ stores predictions
MongoDB Database (predicted_flights collection)
```

### Key Data Structures

#### Actual Flight Data
- **ReplayPath**: Streaming packet format with `time` (String), `listRealPath`, `listFlightIntention`
- **Flight Linking**: `FlightIntention.indicative` ↔ `RealPathPoint.indicativeSafe` matching
- **Storage**: `JoinedFlightData` documents in MongoDB `flights` collection with automatic deduplication

#### Predicted Flight Data
- **PredictedFlightData**: Predicted flight route and timing data
- **RouteElement**: Individual route points with coordinates, speed, and timing predictions
- **RouteSegment**: Route connections with distance calculations
- **Storage**: `PredictedFlightData` documents in MongoDB `predicted_flights` collection
- **Comparison Key**: `planId` used to match predicted flights with actual flight performance

##  Prerequisites

1. **Java 17+** (verified working)
2. **Maven 3.6+**
3. **MongoDB** - Docker recommended
4. **Git** for version control
5. **curl** or HTTP client for testing

##  Quick Start

### 1. Start MongoDB with Docker

```bash
# Start MongoDB container
docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest

# Verify it's running
docker ps | grep mongo

# Should show something like:
# d729b12cbe55   mongo:latest   "docker-entrypoint.s…"   Up X hours   0.0.0.0:27017->27017/tcp   aviation_mongodb
```

### 2. Configure Database Connection

Create `src/main/resources/application.yml` (this file is **not** tracked in Git):

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: aviation_db  # or use 'test' for development

server:
  port: 8080

logging:
  level:
    com.example: INFO
    org.springframework.data.mongodb: WARN
    org.springframework.web: INFO
```

### 3. Build and Start the Service

```bash
# Navigate to project directory
cd path/to/your/streaming-flight-data-system

# Clean build (important after recent model changes)
mvn clean package

# Start the streaming service
mvn spring-boot:run
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.2)

 Streaming Flight Service is running!
 API endpoints:
   POST /api/flights/process-packet - Process single ReplayPath packet
   GET  /api/flights/stats         - Get flight statistics
   GET  /api/flights/health        - Health check
```

### 4. Verify Service is Running

```bash
# Health check
curl http://localhost:8080/api/flights/health
# Should return: "Streaming Flight Service is running"

# Check initial stats
curl http://localhost:8080/api/flights/stats
# Should return: {"totalFlights":0,"flightsWithTracking":0,"totalTrackingPoints":0}
```

##  API Endpoints Reference

### Flight Tracking Endpoints

| Method | Endpoint | Description | Input | Use Case |
|--------|----------|-------------|-------|----------|
| `POST` | `/api/flights/process-packet` | Process single ReplayPath packet | Single `ReplayPath` object | Real-time streaming |

| `GET` | `/api/flights/stats` | Get flight statistics | None | Monitoring |
| `GET` | `/api/flights/health` | Health check | None | Health monitoring |
| `GET` | `/api/flights/analyze-duplicates` | Analyze duplicate indicatives | None | Data quality monitoring |
| `POST` | `/api/flights/cleanup-duplicates` | Clean up duplicate tracking points | None | Maintenance |

### Predicted Flight Endpoints

| Method | Endpoint | Description | Input | Use Case |
|--------|----------|-------------|-------|----------|
| `POST` | `/api/predicted-flights/process` | Process predicted flight data | Predicted flight JSON object | Prediction data storage |
| `GET` | `/api/predicted-flights/stats` | Get predicted flight statistics | None | Prediction monitoring |
| `GET` | `/api/predicted-flights/health` | Health check for predictions | None | Service monitoring |

### Important API Notes

#### Flight Tracking
- **Production**: Use `/process-packet` for real-time single packet processing

- **JSON Order**: Property order in JSON doesn't matter - fields matched by name
- **Time Format**: `time` field accepts String format (timestamps, dates, etc.)

#### Predicted Flights
- **Data Format**: Accepts complex JSON with nested `routeElements` and `routeSegments`
- **planId Mapping**: JSON `id` field automatically mapped to `planId` for comparison
- **Comparison Ready**: Data stored for future comparison with actual flight performance
- **Upsert Behavior**: Updates existing predictions or creates new ones based on planId

##  Integration Examples

### Option 1: Real-Time Streaming (Recommended for PathVoGenerator)

```java
@Test
public void _01_realTimeStreaming() throws Exception {
    var streamPackets = repo.streamPackets(LocalDate.of(2025, 7, 11));
    
    HttpClient httpClient = HttpClient.newHttpClient();
    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    AtomicInteger processedCount = new AtomicInteger(0);
    AtomicInteger errorCount = new AtomicInteger(0);
    
    // Process each packet individually
    streamPackets.forEach(packet -> {
        try {
            final byte[] value = packet.getValue();
            final ReplayPath input = ReplaySerializer.input(value);
            
            // Convert to JSON
            String json = mapper.writeValueAsString(input);
            
            // Send individual packet to streaming API
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/flights/process-packet"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                processedCount.incrementAndGet();
                if (processedCount.get() % 100 == 0) {
                    log.info("Processed {} packets", processedCount.get());
                }
            } else {
                errorCount.incrementAndGet();
                log.warn("Error processing packet: {} - {}", 
                    response.statusCode(), response.body());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("Failed to process packet", e);
        }
    });
    
    log.info("Streaming completed. Processed: {}, Errors: {}", 
        processedCount.get(), errorCount.get());
}
```

### Option 2: Predicted Flight Integration\n\n```java\n@Test\npublic void _02_predictedFlightStreaming() throws Exception {\n    HttpClient httpClient = HttpClient.newHttpClient();\n    ObjectMapper mapper = new ObjectMapper();\n    \n    // Sample predicted flight data\n    String predictedFlightJson = \"\"\"{\n        \"instanceId\": 17879345,\n        \"routeId\": 51435982,\n        \"distance\": null,\n        \"routeElements\": [\n            {\n                \"latitude\": -23.43555556,\n                \"speedMeterPerSecond\": 74.594444,\n                \"eetMinutes\": 0.0,\n                \"id\": 169324506,\n                \"indicative\": \"SBGR\",\n                \"levelMeters\": 749.808,\n                \"elementType\": \"AERODROME\",\n                \"coordinateText\": \"2326S04628W\",\n                \"longitude\": -46.47305556\n            }\n        ],\n        \"id\": 51637804,\n        \"indicative\": \"TAM3886\",\n        \"time\": \"[Thu Jul 10 22:25:00 UTC 2025,Fri Jul 11 00:00:00 UTC 2025]\",\n        \"startPointIndicative\": \"SBGR\",\n        \"endPointIndicative\": \"SBCG\",\n        \"routeSegments\": [\n            {\n                \"elementBId\": 169324507,\n                \"elementAId\": 169324506,\n                \"distance\": 11074.472239127741,\n                \"id\": 107956339\n            }\n        ]\n    }\"\"\";\n    \n    // Send predicted flight data\n    HttpRequest request = HttpRequest.newBuilder()\n        .uri(URI.create(\"http://localhost:8080/api/predicted-flights/process\"))\n        .header(\"Content-Type\", \"application/json\")\n        .POST(HttpRequest.BodyPublishers.ofString(predictedFlightJson))\n        .build();\n    \n    HttpResponse<String> response = httpClient.send(request, \n        HttpResponse.BodyHandlers.ofString());\n    \n    if (response.statusCode() == 200) {\n        log.info(\"Predicted flight processed successfully: {}\", response.body());\n        \n        // Verify the prediction was stored\n        HttpRequest statsRequest = HttpRequest.newBuilder()\n            .uri(URI.create(\"http://localhost:8080/api/predicted-flights/stats\"))\n            .GET()\n            .build();\n        \n        HttpResponse<String> statsResponse = httpClient.send(statsRequest, \n            HttpResponse.BodyHandlers.ofString());\n        \n        log.info(\"Predicted flights stats: {}\", statsResponse.body());\n    } else {\n        log.error(\"Failed to process predicted flight: {} - {}\", \n            response.statusCode(), response.body());\n    }\n}\n```\n\n### Option 3: cURL Testing

```bash
# Test single packet
curl -X POST http://localhost:8080/api/flights/process-packet \
  -H "Content-Type: application/json" \
  -d '{
    "time": "1626789600000",
    "listRealPath": [
      {
        "planId": 12345,
        "indicativeSafe": "TEST123",
        "flightLevel": 350,
        "trackSpeed": 450,
        "seqNum": 1
      }
    ],
    "listFlightIntention": [
      {
        "planId": 12345,
        "indicative": "TEST123",
        "aircraftType": "B737",
        "airline": "TEST"
      }
    ]
  }'



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
```

##  Database Operations

### Connecting to MongoDB

```bash
# Connect to MongoDB shell
docker exec -it aviation_mongodb mongosh

# Alternative: use container ID if name doesn't work
docker ps | grep mongo  # Get container ID
docker exec -it <container_id> mongosh
```

### Basic Database Commands

```javascript
// In MongoDB shell

// List all databases
show dbs

// Switch to your application database
use aviation_db
// or
use test

// List collections
show collections

// Should show: flights, predicted_flights

// View actual flight data
db.flights.find().limit(5)

// View predicted flight data
db.predicted_flights.find().limit(5)

// Count documents
db.flights.countDocuments()
db.predicted_flights.countDocuments()

// Find by indicative
db.flights.find({"indicative": "GOL1234"})
db.predicted_flights.find({"indicative": "TAM3886"})

// Check flights with tracking data
db.flights.find({"hasTrackingData": true}).limit(3)

// Find matching flight and prediction by planId
db.flights.find({"planId": 51637804})
db.predicted_flights.find({"planId": 51637804})

// Get statistics
db.flights.aggregate([
  {
    $group: {
      _id: null,
      totalFlights: { $sum: 1 },
      avgTrackingPoints: { $avg: "$totalTrackingPoints" },
      flightsWithTracking: { 
        $sum: { $cond: ["$hasTrackingData", 1, 0] }
      }
    }
  }
])
```

### Sample Data Structure

```javascript
// Example document in MongoDB
{
  "_id": ObjectId("..."),
  "indicative": "GOL1234",
  "planId": 12345,
  "aircraftType": "B737",
  "airline": "GOL",
  "hasTrackingData": true,
  "totalTrackingPoints": 15,
  "lastUpdate": ISODate("..."),
  "trackingPoints": [
    {
      "seqNum": 1,
      "flightLevel": 350,
      "trackSpeed": 450,
      "timestamp": "1626789600000"
    }
  ]
}
```

##  Testing & Validation

### 1. Service Health Check

```bash
# Check flight tracking service
curl http://localhost:8080/api/flights/health

# Check predicted flights service
curl http://localhost:8080/api/predicted-flights/health
```



### 3. Verify Data Processing

```bash
# Check flight tracking statistics
curl http://localhost:8080/api/flights/stats

# Expected response:
{
  "totalFlights": 150,
  "flightsWithTracking": 120,
  "totalTrackingPoints": 2847
}

# Check predicted flights statistics
curl http://localhost:8080/api/predicted-flights/stats

# Expected response:
{
  "totalPredictedFlights": 25
}
```

### 4. Database Verification

```bash
# Connect to database and verify
docker exec -it aviation_mongodb mongosh
use aviation_db

# Check both collections
db.flights.countDocuments()
db.predicted_flights.countDocuments()

# View sample documents
db.flights.findOne()
db.predicted_flights.findOne()

# Verify planId matching for comparison
db.flights.find({"planId": 51637804})
db.predicted_flights.find({"planId": 51637804})
```

##  Performance Optimization

### Batch Size Recommendations

- **Small datasets** (< 1000 packets): Batch size 50-100
- **Medium datasets** (1000-10000 packets): Batch size 100-500  
- **Large datasets** (> 10000 packets): Batch size 500-1000
- **Network considerations**: Larger batches for high-latency networks

### Performance Metrics

- **Single packet**: ~10-50ms processing time
- **Batch processing**: ~5-10ms per packet in batch
- **Database operations**: ~1-5ms per lookup/insert
- **Memory usage**: ~50MB base + ~1KB per flight stored

### Monitoring

```bash
# Check flight tracking statistics
curl http://localhost:8080/api/flights/stats

# Check predicted flights statistics
curl http://localhost:8080/api/predicted-flights/stats

# Monitor MongoDB performance
docker exec -it aviation_mongodb mongosh
use aviation_db
db.runCommand({serverStatus: 1})

# Check collection sizes
db.stats()
db.flights.stats()
db.predicted_flights.stats()
```

## Troubleshooting

### Common Issues

**1. Port 8080 already in use**
```bash
# Find what's using the port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # macOS/Linux

# Solution: Stop the other service or change port in application.yml
```

**2. MongoDB connection failed**
```bash
# Check if MongoDB is running
docker ps | grep mongo

# Restart MongoDB if needed
docker start aviation_mongodb

# Check MongoDB logs
docker logs aviation_mongodb
```

**3. Compilation errors after git pull**
```bash
# Clean and rebuild (especially after model changes)
mvn clean compile
mvn clean package
```

**4. Test failures**
```bash
# Run tests to identify issues
mvn clean test

# Check if time field format changed
# Ensure ReplayPath.time is String, not long
```

**5. No data in database**
```bash
# Verify database connection
curl http://localhost:8080/api/flights/stats

# Check MongoDB directly
docker exec -it aviation_mongodb mongosh
use aviation_db
db.flights.find().limit(1)
```

### Debug Mode

Enable detailed logging in `application.yml`:

```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.data.mongodb: DEBUG
    org.springframework.web: DEBUG
```

### Network Issues

For remote deployment, ensure:
- Port 8080 is accessible
- MongoDB port 27017 is accessible (if remote)
- Firewall settings allow connections
- Network latency considerations for batch sizes

##  Deployment Options

### Local Development

```bash
# Start MongoDB
docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest

# Start application
mvn spring-boot:run
```

### Remote Server Deployment

```bash
# On remote server
git clone <your-repo>
cd streaming-flight-data-system

# Install Java 17 if needed
sudo apt update
sudo apt install openjdk-17-jdk

# Install Docker for MongoDB
sudo apt install docker.io
sudo systemctl start docker
sudo docker run -d --name aviation_mongodb -p 27017:27017 mongo:latest

# Create application.yml (not in git)
cat > src/main/resources/application.yml << EOF
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: aviation_db
server:
  port: 8080
EOF

# Build and run
mvn clean package
java -jar target/java-project-1.0.0.jar
```

### Docker Deployment (Future)

```bash
# Build container
docker build -t streaming-flight-service .

# Run with docker-compose
docker-compose up -d
```

##  Data File Organization

### Git Repository Structure
```
project/
├── src/main/java/          # Source code (tracked)
├── src/main/resources/     # Resources (tracked)
├── src/test/java/          # Tests (tracked) 
├── pom.xml                 # Maven config (tracked)
├── README.md               # Documentation (tracked)
├── STREAMING_SETUP.md      # This file (tracked)
├── inputData/              # Test data (NOT tracked)
├── outputData/             # Generated files (NOT tracked)
└── application.yml         # Local config (NOT tracked)
```



### Production Data Flow
- **Input**: HTTP POST requests with JSON
- **Processing**: Real-time via REST API
- **Storage**: MongoDB collections
- **Output**: API responses and database queries
- **No file dependencies**: Fully streaming-based

##  Next Steps

### Flight Tracking
1. **Start with single packet processing** using `/process-packet` endpoint
2. **Test with small datasets** first (100-1000 packets)
3. **Monitor performance** using stats endpoint and database queries
4. **Scale up batch sizes** based on performance

### Predicted Flights
1. **Test predicted flight processing** using `/predicted-flights/process` endpoint
2. **Verify planId mapping** between predicted and actual flights
3. **Monitor prediction storage** using predicted flights stats endpoint
4. **Prepare for comparison analysis** by ensuring consistent planId usage

### Production
1. **Add authentication** for production deployment
2. **Consider horizontal scaling** for very high volumes
3. **Implement comparison service** to analyze predicted vs actual performance
4. **Set up monitoring** for both flight tracking and prediction accuracy

