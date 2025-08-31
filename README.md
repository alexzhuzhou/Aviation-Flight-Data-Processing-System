# Aviation Flight Data Processing System

A comprehensive full-stack application for processing, analyzing, and visualizing aviation flight data with Oracle database integration, real-time streaming capabilities, and ICAO KPI14 compliance analysis.

## Table of Contents
- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Features](#features)
- [Running the Application](#running-the-application)
- [Sample Data](#sample-data)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Troubleshooting](#troubleshooting)

## Overview

This application provides a complete solution for aviation flight data processing and analysis:

**Backend (Java Spring Boot):**
- Real-time flight data processing with Oracle database integration
- Predicted flight data analysis and comparison
- ICAO KPI14 punctuality analysis with multiple tolerance windows
- Trajectory accuracy analysis using MSE/RMSE metrics
- Trajectory densification using Sigma simulation engine
- MongoDB storage with comprehensive data models
- RESTful API for all operations

**Frontend (Vue.js Dashboard):**
- Interactive flight data visualization and monitoring
- Real-time analytics dashboard with charts and maps
- Flight search and filtering capabilities
- System health monitoring and diagnostics
- Responsive design with modern UI components

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │   Databases     │
│   (Vue.js)      │◄──►│  (Spring Boot)  │◄──►│                 │
│                 │    │                 │    │  ┌─────────────┐│
│ • Dashboard     │    │ • REST API      │    │  │  MongoDB    ││
│ • Analytics     │    │ • Data Processing│    │  │ (Flight Data)││
│ • Maps          │    │ • Analysis      │    │  └─────────────┘│
│ • Search        │    │ • Integration   │    │  ┌─────────────┐│
│                 │    │                 │    │  │   Oracle    ││
└─────────────────┘    └─────────────────┘    │  │(Sigma DB)   ││
                                              │  └─────────────┘│
                                              └─────────────────┘
```

### Data Flow
1. **Oracle Database** → Backend extracts flight data
2. **Backend Processing** → Processes and stores in MongoDB
3. **Analysis Engine** → Performs punctuality and trajectory analysis
4. **REST API** → Exposes data and operations
5. **Frontend Dashboard** → Visualizes data and provides user interface

## Prerequisites

### Required Software
- **Java 13+** (for backend)
- **Node.js 16+** (for frontend)
- **Maven 3.8+** (for backend build)
- **Docker** (for MongoDB database)



### Sigma Integration Requirement
**IMPORTANT**: This project must be located under the `/sigma/modules/test/` directory structure to work properly, as it is integrated within the Sigma system and depends on Sigma libraries and configurations.

**Required Path**: `/sigma/modules/test/streaming-flight-data-system/`

The project uses Sigma dependencies including:
- `sigma-gsa-commons` for ReplayPath and data processing
- `sigma-gfx-domain` for Oracle database entities
- Sigma configuration for Oracle database connectivity



### Optional
- **MongoDB Compass** (for database inspection)
- **Postman** (for API testing)

## Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd streaming-flight-data-system
```

### 2. Start Database
```bash
# Start MongoDB container
docker run -d \
  --name aviation_mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_DATABASE=aviation_db \
  mongo:latest
```

### 3. Configure Environment
```bash
# Set Oracle database credentials
export ORACLE_HOST=your_oracle_host
export ORACLE_PORT=1521
export ORACLE_SERVICE=your_service_name
export ORACLE_USERNAME=your_username
export ORACLE_PASSWORD=your_password
```

### 4. Start Backend
```bash
cd backend
mvn clean compile
mvn spring-boot:run
# Backend runs on http://localhost:8080
```

### 5. Start Frontend
```bash
cd frontend
npm install
npm run dev
# Frontend runs on http://localhost:5173
```

### 6. Verify Setup
- **Frontend**: Open http://localhost:5173
- **Backend API**: Check http://localhost:8080/api/flights/health
- **Database**: Verify MongoDB container is running

## Project Structure

```
streaming-flight-data-system/
├── backend/                    # Java Spring Boot application
│   ├── src/main/java/com/example/
│   │   ├── controller/         # REST API controllers (7 controllers)
│   │   ├── service/           # Business logic services (9 services)
│   │   ├── model/             # Data models (16 models)
│   │   ├── repository/        # MongoDB repositories (3 repositories)
│   │   └── config/            # Configuration classes
│   ├── database-backup/       # Sample database backup
│   ├── pom.xml               # Maven configuration
│   └── README.md             # Backend documentation
├── frontend/                  # Vue.js dashboard application
│   ├── src/
│   │   ├── views/            # Page components (6 pages)
│   │   ├── components/       # Reusable components
│   │   ├── services/         # API integration
│   │   └── router/           # Vue Router configuration
│   ├── package.json          # Node.js dependencies
│   └── README.md             # Frontend documentation
└── README.md                 # This file
```

## Features

### Core Functionality

#### Real-time Flight Data Processing
- **Oracle Integration**: Direct extraction from Sigma production database
- **Streaming Processing**: Real-time ReplayPath packet processing
- **Data Deduplication**: Advanced duplicate detection and removal
- **Timestamp Disambiguation**: Handle flights with same call sign
- **MongoDB Storage**: Efficient storage with proper indexing

#### Predicted Flight Analysis
- **Oracle Extraction**: Extract predictions from Oracle database
- **Batch Processing**: Process multiple predictions efficiently
- **Flight Matching**: Match predictions with actual flights via planId
- **Data Validation**: Comprehensive validation and error handling

#### ICAO KPI14 Punctuality Analysis
- **Route Filtering**: SBSP ↔ SBRJ routes with AERODROME endpoints (only those aerodroms for now, need to remove filtering to be able to run analysis for all routes!!!)
- **Geographic Validation**: 2 NM threshold + flight level ≤ 4 filtering
- **Multiple Tolerance Windows**: ±3, ±5, ±15 minutes analysis
- **KPI Reporting**: ICAO compliant punctuality reports

#### Trajectory Analysis (Only SBSP SBRJ routes , need to remove filtering to run all the routes!!)
- **Accuracy Analysis**: MSE/RMSE calculations for trajectory accuracy
- **Densification**: Sigma simulation engine for trajectory interpolation
- **Unit Conversions**: Handle different coordinate systems and units
- **Visualization**: Interactive maps with trajectory overlays

### Dashboard Features

#### Interactive Dashboard
- **Overview Page**: System status and quick actions
- **Flight Data**: Comprehensive flight data table with search
- **Analytics**: Charts and KPI visualizations
- **Maps**: Interactive flight trajectory visualization
- **Search**: Advanced multi-criteria search capabilities
- **Health Monitoring**: System health and diagnostics

#### Data Visualization
- **Real-time Charts**: Chart.js powered analytics
- **Interactive Maps**: Leaflet.js flight path visualization
- **Performance Metrics**: Processing times and success rates
- **Export Functions**: Download data and reports

## Running the Application

### Development Mode

#### Backend Development
```bash
cd backend
mvn spring-boot:run
# Hot reload enabled for development
```

#### Frontend Development
```bash
cd frontend
npm run dev
# Hot reload enabled, runs on http://localhost:3000
```

### Production Mode

#### Backend Production
```bash
cd backend
mvn package -DskipTests
java -jar target/streaming-flight-data-system-14.2.0-SNAPSHOT.jar
```

#### Frontend Production
```bash
cd frontend
npm run build
npm run preview
# Serves production build on http://localhost:3000
```

### Docker Deployment (Future)
```bash
# Build and run with Docker Compose
docker-compose up -d
```

## Sample Data

### Pre-populated Database Backup
For quick setup with sample data:

**Location**: `backend/database-backup/aviation_db_backup.tar`
- **Contains**: 1,214 flights from 07/11/2025
- **Extraction Time**: ~6 days (use backup to skip this process)
- **Collections**: flights, predicted_flights, processing_history

**Usage**:
```bash
cd backend/database-backup
# Follow instructions in database-backup/README.md
```

### Data Processing Workflow
1. **Extract from Oracle**: Process flight data from Oracle database
2. **Store in MongoDB**: Save processed data with proper structure
3. **Generate Predictions**: Extract and process predicted flight data
4. **Run Analysis**: Perform punctuality and trajectory analysis
5. **Visualize Results**: View results in dashboard

## API Documentation

### Backend API Endpoints
- **Base URL**: `http://localhost:8080/api`
- **Documentation**: See `backend/src/main/java/com/example/controller/README.md`

### Key API Categories
- **Flight Processing**: `/api/flights/*` - Flight data operations
- **Predicted Flights**: `/api/predicted-flights/*` - Prediction processing
- **Analysis**: `/api/punctuality-analysis/*`, `/api/trajectory-accuracy/*`
- **Search**: `/api/flight-search/*` - Search and management
- **Monitoring**: `/api/processing-history/*` - Operation tracking

### Example API Usage
```bash
# Test system health
curl http://localhost:8080/api/flights/health

# Process Oracle data
curl -X POST http://localhost:8080/api/flights/process-packet

# Get punctuality analysis
curl http://localhost:8080/api/punctuality-analysis/punctuality-kpis

# Search flights
curl "http://localhost:8080/api/flight-search/by-plan-id?query=17879"
```

## Development

### Backend Development
- **Technology**: Java 13, Spring Boot, MongoDB, Oracle integration
- **Documentation**: `backend/README.md`
- **API Docs**: `backend/src/main/java/com/example/controller/README.md`

### Frontend Development
- **Technology**: Vue.js 3, Vite, Tailwind CSS, Chart.js, Leaflet.js
- **Documentation**: `frontend/README.md`
- **Component Docs**: `frontend/src/components/README.md`

### Adding New Features
1. **Backend**: Add controller, service, model, repository as needed
2. **Frontend**: Add view, component, API integration
3. **Documentation**: Update relevant README files
4. **Testing**: Add unit and integration tests

## Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check if ports are available
lsof -i :8080  # Backend
lsof -i :5173  # Frontend

# Check MongoDB container
docker ps | grep aviation_mongodb
docker logs aviation_mongodb
```

#### Database Connection Issues
```bash
# Restart MongoDB container
docker restart aviation_mongodb

# Check database connectivity
docker exec -it aviation_mongodb mongosh aviation_db --eval "db.stats()"
```

#### Oracle Connection Issues
```bash
# Verify environment variables
echo $ORACLE_HOST
echo $ORACLE_USERNAME

# Test Oracle connectivity
curl http://localhost:8080/api/flights/test-oracle-connection
```

#### Frontend API Connection
```bash
# Check backend is running
curl http://localhost:8080/api/flights/health

# Check browser console for CORS errors
# Verify API base URL in frontend configuration
```

### Performance Issues
- **Memory**: Increase JVM heap size for backend
- **Database**: Monitor MongoDB performance and indexing
- **Network**: Check Oracle database connectivity and performance
- **Frontend**: Monitor browser memory usage and API response times

### Getting Help

#### Log Files
- **Backend**: Console output or configured log files
- **Frontend**: Browser developer console
- **Database**: `docker logs aviation_mongodb`

#### Documentation
- **Backend**: `backend/README.md` and package-specific READMEs
- **Frontend**: `frontend/README.md` and component documentation
- **API**: Controller documentation for endpoint details

#### Health Checks
```bash
# System health endpoints
curl http://localhost:8080/api/flights/health
curl http://localhost:8080/api/predicted-flights/health
curl http://localhost:8080/api/processing-history/health
```

## Contributing

### Development Workflow
1. **Setup**: Follow quick start guide
2. **Development**: Make changes in appropriate backend/frontend folders
3. **Testing**: Test changes locally
4. **Documentation**: Update relevant README files
5. **Commit**: Follow conventional commit messages

### Code Standards
- **Backend**: Follow Spring Boot best practices
- **Frontend**: Follow Vue.js 3 Composition API patterns
- **Documentation**: Keep READMEs updated with changes
- **Testing**: Add tests for new functionality

For detailed component documentation, see the README files in the `backend/` and `frontend/` directories.
