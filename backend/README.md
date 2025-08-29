# Aviation Flight Data Processing System - Backend

A comprehensive Java Spring Boot application for processing and analyzing aviation tracking data with Oracle database integration, real-time streaming capabilities, and ICAO KPI14 compliance analysis.

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Setup](#quick-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)

## Overview

This Spring Boot application processes aviation flight data from multiple sources:

- **Real-time flight data processing** with Oracle database integration
- **Predicted flight data analysis** for comparison with actual flights
- **ICAO KPI14 punctuality analysis** with multiple tolerance windows
- **Trajectory accuracy analysis** using MSE/RMSE metrics
- **Trajectory densification** using Sigma simulation engine
- **MongoDB storage** for flight data and processing history
- **RESTful API** for data processing and analysis operations

## Prerequisites

### Required Software
- **Java 13+** (tested with Java 13)
- **Maven 3.8+** (for building the application)
- **Docker** (for MongoDB database)

### System Requirements
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: At least 2GB free space
- **Network**: Access to Oracle database (for Oracle integration features)

## Quick Setup

### 1. Start MongoDB Database
```bash
docker run -d \
  --name aviation_mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_DATABASE=aviation_db \
  mongo:latest
```

### 2. Configure Environment Variables
```bash
export ORACLE_HOST=your_oracle_host
export ORACLE_PORT=1521
export ORACLE_SERVICE=your_service_name
export ORACLE_USERNAME=your_username
export ORACLE_PASSWORD=your_password
```

### 3. Build and Run
```bash
mvn clean compile
mvn spring-boot:run
```

### 4. Verify Setup
```bash
curl http://localhost:8080/api/flights/health
curl http://localhost:8080/api/flights/test-oracle-connection
```

## Configuration

### Database Configuration
The application uses MongoDB for data storage:
- **Container Name**: `aviation_mongodb`
- **Database Name**: `aviation_db`
- **Port**: `27017`
- **Collections**: `flights`, `predicted_flights`, `processing_history`

### Sample Data Backup
For quick setup with sample data, use the pre-populated database backup:
- **Location**: `database-backup/aviation_db_backup.tar`
- **Contains**: 1,214 flights from 07/11/2025
- **Extraction Time**: ~6 days (use backup to skip this process)
- **Instructions**: See `database-backup/README.md`

### Application Configuration
Edit `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: aviation_db

oracle:
  host: ${ORACLE_HOST:your_oracle_host}
  port: ${ORACLE_PORT:1521}
  service: ${ORACLE_SERVICE:your_service_name}
  username: ${ORACLE_USERNAME:your_username}
  password: ${ORACLE_PASSWORD:your_password}

server:
  port: 8080
```

### Environment Variables
Set these before running the application:

```bash
# Required for Oracle integration
export ORACLE_HOST=your_oracle_host
export ORACLE_PORT=1521
export ORACLE_SERVICE=your_service_name
export ORACLE_USERNAME=your_username
export ORACLE_PASSWORD=your_password

# Optional
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=dev
```

## Running the Application

### Development Mode
```bash
mvn spring-boot:run
```

### Production Mode
```bash
mvn package -DskipTests
java -jar target/streaming-flight-data-system-14.2.0-SNAPSHOT.jar
```

### With Custom Configuration
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## API Endpoints

For detailed API endpoint documentation, see the controller documentation:
**Controller Documentation**: `src/main/java/com/example/controller/README.md`

## Architecture

### Package Structure
```
src/main/java/com/example/
├── config/          # Configuration classes (Oracle, Security, Sigma)
├── controller/      # REST API controllers (7 controllers)
├── model/          # Data models and entities (16 models)
├── repository/     # MongoDB repositories (3 repositories)
├── service/        # Business logic services (9 services)
└── StreamingFlightDataApplication.java  # Main application class
```

### Key Components

#### Controllers (7)
- **StreamingController** - Main flight data processing
- **PredictedFlightController** - Predicted flight operations
- **PunctualityAnalysisController** - ICAO KPI14 analysis
- **TrajectoryAccuracyAnalysisController** - MSE/RMSE analysis
- **TrajectoryDensificationController** - Trajectory densification
- **FlightSearchController** - Search and management
- **ProcessingHistoryController** - Operation tracking

#### Services (9)
- **StreamingFlightService** - Real-time data processing
- **OracleDataExtractionService** - Oracle database integration
- **PredictedFlightService** - Prediction processing
- **PunctualityAnalysisService** - ICAO KPI14 analysis
- **TrajectoryAccuracyAnalysisService** - Accuracy analysis
- **TrajectoryDensificationService** - Trajectory densification
- **FlightSearchService** - Search operations
- **ProcessingHistoryService** - Operation tracking
- **OracleFlightDataService** - Oracle data extraction

#### Repositories (3)
- **FlightRepository** - Actual flight data (`flights` collection)
- **PredictedFlightRepository** - Predicted flight data (`predicted_flights` collection)
- **ProcessingHistoryRepository** - Operation history (`processing_history` collection)

### Data Flow
```
Oracle Database → OracleDataExtractionService → StreamingFlightService → MongoDB
Oracle Database → OracleFlightDataService → PredictedFlightService → MongoDB
MongoDB Data → Analysis Services → API Results
```

## Troubleshooting

### Application Won't Start

**MongoDB Connection Issues:**
```bash
# Check container status
docker ps | grep aviation_mongodb

# Restart container
docker restart aviation_mongodb

# Check logs
docker logs aviation_mongodb
```

**Port Already in Use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Oracle Connection Issues

**Environment Variables:**
```bash
# Check variables are set
echo $ORACLE_HOST
echo $ORACLE_USERNAME

# Test connectivity
telnet 10.103.3.8 1521
```

### Database Issues

**MongoDB Connection:**
```bash
# Test MongoDB connection
docker exec -it aviation_mongodb mongosh aviation_db --eval "db.stats()"

# Check collections
docker exec -it aviation_mongodb mongosh aviation_db --eval "show collections"
```

### Memory Issues

**OutOfMemoryError:**
```bash
# Increase heap size
java -Xmx4g -jar target/streaming-flight-data-system-14.2.0-SNAPSHOT.jar

# Or with Maven
export MAVEN_OPTS="-Xmx4g"
mvn spring-boot:run
```

### Build Issues

**Maven Dependencies:**
```bash
# Clean rebuild
mvn clean install -U

# Skip tests
mvn clean install -DskipTests

# Check dependencies
mvn dependency:tree
```

### Debug Logging

Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.data.mongodb: DEBUG
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/api/flights/health
curl http://localhost:8080/api/predicted-flights/health

# Database connectivity
curl http://localhost:8080/api/flights/test-oracle-connection

# Statistics
curl http://localhost:8080/api/flights/stats
curl http://localhost:8080/api/processing-history/statistics
```

### Common Commands
```bash
# Check versions
java -version
mvn -version
docker --version

# Monitor processes
ps aux | grep java
netstat -tulpn | grep :8080

# Database operations
docker exec -it aviation_mongodb mongosh aviation_db
```

For detailed component documentation, see the individual package READMEs:
- **Controller Documentation**: `src/main/java/com/example/controller/README.md`
- **Service Documentation**: `src/main/java/com/example/service/README.md`
- **Model Documentation**: `src/main/java/com/example/model/README.md`
- **Repository Documentation**: `src/main/java/com/example/repository/README.md`
