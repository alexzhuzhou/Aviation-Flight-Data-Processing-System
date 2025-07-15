# 🛫 Aviation Flight Data Processing System

A comprehensive Java Spring Boot application for processing and analyzing aviation tracking data, featuring both batch processing and real-time streaming capabilities.

## Overview

This application processes aviation replay data from JSON files and provides real-time streaming capabilities, designed to work with data containing:
- **Real Path Points** (`listRealPath`) - Real-time aircraft tracking data
- **Flight Intentions** (`listFlightIntention`) - Planned flight schedules and information
- **Timestamp** - Global reference time for the dataset

## 🚀 Features

### Batch Processing
- **Data Loading**: Parse large JSON replay files efficiently
- **Statistical Analysis**: Generate summaries and statistics about flight data
- **Interactive Exploration**: Search flights, analyze tracking points, and explore correlations
- **Real-time Tracking Analysis**: Analyze flight levels, speeds, detector sources, and control sectors
- **Flight Planning Analysis**: Examine aircraft types, airlines, RVSM capabilities, and route information
- **Data Correlation**: Match flight intentions with actual tracking data

### 🎯 Real-Time Streaming (NEW!)
- **Live Data Processing**: Process `ReplayPath` packets in real-time via REST API
- **MongoDB Integration**: Store flight data and tracking points in MongoDB
- **RESTful API**: HTTP endpoints for packet processing and data retrieval
- **Upsert Operations**: Smart data merging and deduplication
- **Health Monitoring**: Built-in health checks and statistics endpoints

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MongoDB (for streaming features)
- Data files in the `inputData/` folder

## 📁 Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/example/
│   │   │   │   ├── StreamingFlightApplication.java  # Spring Boot main class
│   │   │   │   ├── controller/
│   │   │   │   │   └── StreamingController.java     # REST API endpoints
│   │   │   │   ├── model/                           # Data models
│   │   │   │   │   ├── ReplayData.java
│   │   │   │   │   ├── RealPathPoint.java
│   │   │   │   │   ├── FlightIntention.java
│   │   │   │   │   ├── Kinematic.java
│   │   │   │   │   ├── JoinedFlightData.java
│   │   │   │   │   ├── TrackingPoint.java
│   │   │   │   │   └── ReplayPath.java
│   │   │   │   ├── service/                         # Business logic
│   │   │   │   │   ├── ReplayDataService.java
│   │   │   │   │   ├── StreamingFlightService.java
│   │   │   │   │   ├── FlightDataJoinService.java
│   │   │   │   │   └── DataAnalysisService.java
│   │   │   │   └── repository/
│   │   │   │       └── FlightRepository.java        # MongoDB repository
│   │   │   └── resources/
│   │   │       └── application.yml                  # Configuration
│   │   └── test/
│   │       ├── java/                               # Test source code
│   │       └── resources/                          # Test resources
├── inputData/                                      # Input data files (gitignored)
│   ├── replay.json
│   ├── replay2.json
│   └── replay3.json
├── outputData/                                     # Output data files (gitignored)
│   ├── joined_flights_output.json
│   ├── joined_flights_consistent.json
│   └── joined_flights_replay2_mongodb.json
├── pom.xml                                         # Maven configuration
├── STREAMING_SETUP.md                              # Streaming setup guide
└── README.md                                       # This file
```

## 🏗️ Building the Project

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Build the JAR file
mvn package
```

## 🚀 Running the Application

### Option 1: Batch Processing (Development/Testing Only)

```bash
# Run the batch processing application (for testing with JSON files)
mvn exec:java -Dexec.mainClass="com.example.App"
```

### Option 2: Streaming Service (Production Mode)

```bash
# Start the Spring Boot streaming service
mvn spring-boot:run
```

The streaming service will start on `http://localhost:8080`

## 📡 Streaming API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/flights/process-packet` | Process single ReplayPath packet |
| `POST` | `/api/flights/process-batch` | Process batch ReplayData (for testing) |
| `GET` | `/api/flights/stats` | Get flight statistics |
| `GET` | `/api/flights/health` | Health check |

### Example API Usage

```bash
# Health check
curl http://localhost:8080/api/flights/health

# Process a batch file
curl -X POST http://localhost:8080/api/flights/process-batch \
  -H "Content-Type: application/json" \
  -d @inputData/replay2.json

# Get statistics
curl http://localhost:8080/api/flights/stats
```

## 📊 Data Files

### Input Data (Development/Testing Only)
Place your replay data files in the `inputData/` folder for testing and development:
- `inputData/replay.json` - Sample replay data for testing
- `inputData/replay2.json` - Additional replay data for testing
- `inputData/replay3.json` - Your replay data file for testing

### Output Data
Generated JSON files are saved to the `outputData/` folder:
- `outputData/joined_flights_output.json` - Basic joined flight data
- `outputData/joined_flights_consistent.json` - Joined data with deduplication
- `outputData/joined_flights_replay2_mongodb.json` - MongoDB-ready format

**Note**: Both `inputData/` and `outputData/` folders are excluded from Git to keep the repository size small.

### Production Data Flow
In production, data comes through the streaming API:
- **Real-time packets**: `POST /api/flights/process-packet` - Processes individual `ReplayPath` packets
- **Batch testing**: `POST /api/flights/process-batch` - For testing with JSON files
- **No file dependencies**: The system is designed for real-time streaming, not file processing

## 🔧 Configuration

### MongoDB Setup

Edit `src/main/resources/application.yml` if needed:
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: aviation_db
```

### Running MongoDB

```bash
# Using Docker (recommended)
docker run -d --name mongodb -p 27017:27017 mongo:latest

# Or install MongoDB locally
# Follow instructions at https://www.mongodb.com/try/download/community
```

## 🎯 Application Features

### Batch Processing Features (Development/Testing)
- Load and parse replay JSON files for testing
- Interactive menu for data exploration
- Search flights by call sign
- Analyze tracking points and flight intentions
- Generate statistical summaries
- **Purpose**: Development, testing, and data analysis

### Streaming Features (Production)
- Real-time packet processing via REST API
- MongoDB data persistence
- RESTful API for integration
- Automatic data deduplication
- Health monitoring and statistics
- **Purpose**: Live production data processing

## 📚 Data Models

The application uses strongly-typed Java models:

- **ReplayData**: Main container with listRealPath, listFlightIntention, and timestamp
- **RealPathPoint**: Individual tracking points with position, speed, flight level
- **FlightIntention**: Planned flight data with call signs, aircraft types, routes
- **Kinematic**: Position and movement data including lat/lon coordinates
- **JoinedFlightData**: Combined flight and tracking data for MongoDB storage
- **ReplayPath**: Real-time packet structure for streaming

## 🔗 Dependencies

- **Spring Boot**: Web framework and auto-configuration
- **Spring Data MongoDB**: MongoDB integration
- **Jackson**: JSON parsing and data binding
- **JUnit 5**: Testing framework
- **Java Time API**: Timestamp handling

## 🧪 Testing

```bash
# Run all tests
mvn test

# Test with existing data
curl -X POST http://localhost:8080/api/flights/process-batch \
  -H "Content-Type: application/json" \
  -d @inputData/replay2.json
```

## 📖 Documentation

- **STREAMING_SETUP.md**: Detailed setup guide for streaming features
- **API Documentation**: Available at `http://localhost:8080` when running

## 🤝 Contributing

1. Add new analysis methods to services
2. Create additional data models as needed
3. Extend the REST API with new endpoints
4. Add comprehensive tests for new functionality
5. Update documentation for new features

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details. 