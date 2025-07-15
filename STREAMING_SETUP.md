# 🛫 Streaming Flight Data System Setup Guide

## Overview

This guide explains how to set up the new streaming flight data system that processes `ReplayPath` packets in real-time and stores them in MongoDB, replacing the batch JSON file processing.

## 🔑 **NEW: Identifier Strategy**

**Updated approach (latest version):**
- **Flight Instances**: Use `planId` as unique identifier
- **Tracking Points**: TBD (unique identifier not yet determined)  
- **Linking**: `FlightIntention.indicative` ↔ `RealPathPoint.indicativeSafe` matching

## 🏗️ Architecture

```
External System (PathVoGeneratorTestNew.java)
    ↓ HTTP POST requests
Your REST API (StreamingController)
    ↓ processes packets
StreamingFlightService
    ↓ upserts data
MongoDB Database
```

## 📋 Prerequisites

1. **Java 17+**
2. **Maven 3.6+**
3. **MongoDB** running locally or remotely
4. **Docker** (optional, for running MongoDB)

## 🚀 Quick Start

### 1. Start MongoDB

**Option A: Using Docker (Recommended)**
```bash
docker run -d --name mongodb -p 27017:27017 mongo:latest
```

**Option B: Local Installation**
- Install MongoDB from [mongodb.com](https://www.mongodb.com/try/download/community)
- Start the service: `brew services start mongodb-community` (macOS) or `sudo systemctl start mongod` (Linux)

### 2. Configure Database Connection

Edit `src/main/resources/application.yml` if needed:
```yaml
spring:
  data:
    mongodb:
      host: localhost        # Change if MongoDB is remote
      port: 27017           # Change if using different port
      database: aviation_db # Change database name if needed
```

### 3. Start the Streaming Service

```bash
# Clean and build
mvn clean package

# Run the streaming service
mvn spring-boot:run
# OR
java -jar target/java-project-1.0.0.jar
```

The service will start on `http://localhost:8080` and display available endpoints.

### 4. Verify Service is Running

```bash
curl http://localhost:8080/api/flights/health
# Should return: "Streaming Flight Service is running"
```

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/flights/process-packet` | Process single ReplayPath packet |
| `POST` | `/api/flights/process-batch` | Process batch ReplayData (for testing) |
| `GET` | `/api/flights/stats` | Get flight statistics |
| `GET` | `/api/flights/health` | Health check |

## 🔌 Integration Options

### Option 1: Modify PathVoGeneratorTestNew.java (Recommended)

Replace the batch processing in your external codebase:

```java
@Test
public void _00_run() throws Exception {
    // 1) stream all packets for 2025-07-11
    var streamPackets = repo.streamPackets(LocalDate.of(2025, 7, 11));
    
    // 2) HTTP client for calling your API
    HttpClient httpClient = HttpClient.newHttpClient();
    ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    AtomicInteger processedCount = new AtomicInteger(0);
    AtomicInteger errorCount = new AtomicInteger(0);
    
    // 3) Process each packet individually
    streamPackets.forEach(packet -> {
        try {
            // Extract and deserialize
            final byte[] value = packet.getValue();
            final ReplayPath input = ReplaySerializer.input(value);
            
            // Convert to JSON
            String json = mapper.writeValueAsString(input);
            
            // Send to your streaming API
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
    
    // Get final statistics
    HttpRequest statsRequest = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/api/flights/stats"))
        .GET()
        .build();
    
    HttpResponse<String> statsResponse = httpClient.send(statsRequest, 
        HttpResponse.BodyHandlers.ofString());
    
    log.info("Final database stats: {}", statsResponse.body());
}
```

### Option 2: Shared Library Approach

If you prefer direct integration without HTTP calls:

1. Package your streaming service as a library
2. Add it as a dependency to the external codebase
3. Call `StreamingFlightService.processReplayPath()` directly

### Option 3: Message Queue (Future Enhancement)

For high-volume scenarios, consider using:
- Apache Kafka
- RabbitMQ
- Apache Pulsar

## 🧪 Testing

### Test with Existing JSON Files (Development/Testing)

You can test the system using your existing data files from the `inputData/` folder:

```bash
curl -X POST http://localhost:8080/api/flights/process-batch \
  -H "Content-Type: application/json" \
  -d @inputData/replay2.json
```

**Note**: This endpoint is for testing purposes. In production, use `/api/flights/process-packet` for real-time data.

### Run Unit Tests

```bash
mvn test
```

### View Database Content

```javascript
// Connect to MongoDB
use aviation_db

// View flights
db.flights.find().limit(5)

// Count flights with tracking data
db.flights.countDocuments({"hasTrackingData": true})

// Get flights by airline
db.flights.find({"airline": "GOL"})
```

## 📊 Monitoring

### Check Statistics

```bash
curl http://localhost:8080/api/flights/stats
```

Example response:
```json
{
  "totalFlights": 150,
  "flightsWithTracking": 120,
  "totalTrackingPoints": 2847
}
```

### View Logs

The application logs show detailed processing information:
```
INFO  StreamingFlightService - Processing ReplayPath with 5 flight intentions and 23 real path points
INFO  StreamingFlightService - Created new flight: GOL1234
INFO  StreamingFlightService - Updated flight GLO9610 with 3 new tracking points (total: 15)
```

## ⚡ Performance Considerations

### Database Indexing
The system automatically creates indexes on:
- `indicative` (unique index for fast lookups)
- MongoDB `_id` field

### Optimization Tips

1. **Batch Size**: Process multiple packets in a single request if network latency is high
2. **Connection Pooling**: The service uses MongoDB connection pooling automatically
3. **Async Processing**: For high volume, consider making the API asynchronous
4. **Memory**: Monitor JVM memory usage with flight count growth

### Expected Performance

- **Single packet**: ~10-50ms processing time
- **Database lookups**: ~1-5ms per flight check
- **Insertion**: ~5-10ms per new flight
- **Update**: ~10-20ms per tracking data update

## 🔧 Troubleshooting

### Common Issues

**1. MongoDB Connection Failed**
```
Error: com.mongodb.MongoTimeoutException
```
Solution: Check MongoDB is running and accessible

**2. Duplicate Key Error**
```
Error: E11000 duplicate key error collection
```
Solution: Flight with same `indicative` already exists (this is expected behavior)

**3. Out of Memory**
```
Error: java.lang.OutOfMemoryError
```
Solution: Increase JVM heap size: `-Xmx2g`

### Debug Mode

Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.data.mongodb: DEBUG
```

## 📁 Data File Organization

### Input Data (Development Only)
The project uses an `inputData/` folder to organize **input data files**:
```
inputData/
├── replay.json      # Sample replay data for testing
├── replay2.json     # Additional replay data for testing
└── replay3.json     # Your replay data file for testing
```

### Output Data
Generated files are saved to the `outputData/` folder:
```
outputData/
├── joined_flights_output.json        # Basic joined flight data
├── joined_flights_consistent.json    # Joined data with deduplication
└── joined_flights_replay2_mongodb.json # MongoDB-ready format
```

**Important**: These JSON files are for **development and testing only**. In production, data comes through the streaming API endpoints.

### Git Ignore
Both data folders are excluded from Git to keep the repository size small:
```gitignore
inputData/
outputData/
```

### Using Data Files
1. Place your input data files in the `inputData/` folder for testing
2. Generated output files will be saved to the `outputData/` folder
3. Use the batch processing mode for development and analysis
4. Use the streaming API for production data processing

### Production Data Flow
- **Real-time**: `POST /api/flights/process-packet` - Individual ReplayPath packets
- **Testing**: `POST /api/flights/process-batch` - JSON files for testing
- **No file dependencies**: Production system is designed for streaming, not file processing

## 🎯 Next Steps

1. **Start with Option 1** (modify PathVoGeneratorTestNew.java)
2. **Test with small packet volumes** first
3. **Monitor performance and memory usage**
4. **Scale horizontally** if needed (multiple service instances)
5. **Add authentication** for production use

## 📞 Support

If you encounter issues:
1. Check the logs for error details
2. Verify MongoDB connectivity
3. Test with the health endpoint
4. Use the stats endpoint to verify data is being processed

The system is designed to be **fail-safe** - if a packet fails to process, it won't crash the entire service. 