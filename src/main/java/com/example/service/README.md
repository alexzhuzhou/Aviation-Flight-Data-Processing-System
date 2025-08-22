# Service Layer Documentation

This document explains all the services in the Aviation Flight Data Processing System and their responsibilities.

## Service Overview

The services are organized by their primary responsibilities:

### **1. Core Processing Services**
| Service | Purpose | Primary Use |
|---------|---------|-------------|
| **StreamingFlightService** | Real-time data processing | Production streaming API |
| **PredictedFlightService** | Predicted flight processing | Prediction data storage and analysis |
| **OracleDataExtractionService** | Oracle database integration | Direct data extraction from Sigma production database |
| **OracleFlightDataService** | Oracle flight data operations | Predicted flight data extraction |

### **2. Analysis Services**
| Service | Purpose | Primary Use |
|---------|---------|-------------|
| **PunctualityAnalysisService** | Arrival punctuality analysis (ICAO KPI14) | KPI metrics and comparison |
| **TrajectoryAccuracyAnalysisService** | Trajectory accuracy analysis (MSE/RMSE) | Flight path accuracy metrics |
| **TrajectoryDensificationService** | Trajectory densification | Route interpolation and densification |

## Service Responsibilities

### **StreamingFlightService** 
**Primary Purpose**: Process real-time streaming flight data

**Key Features**:
- Process ReplayPath packets from streaming API
- Create and update flights in MongoDB
- Handle timestamp-based disambiguation
- Manage duplicate tracking points
- Provide database statistics

**Main Methods**:
- `processReplayPath()` - Main entry point for streaming
- `getStats()` - Database statistics
- `analyzeDuplicateIndicatives()` - Duplicate analysis
- `cleanupDuplicateTrackingPoints()` - Data cleanup

**Used By**: StreamingController (REST API)

### **PredictedFlightService** 🔮
**Primary Purpose**: Process predicted flight data for comparison analysis

**Key Features**:
- Process predicted flight JSON data from external prediction systems
- Map JSON 'id' field to 'planId' for matching with actual flights
- Store predicted flight data in MongoDB (predicted_flights collection)
- Provide statistics for predicted flight data
- Support future comparison analysis with actual flight performance

**Main Methods**:
- `processPredictedFlight()` - Main entry point for prediction processing
- `getStats()` - Predicted flight statistics
- `findByPlanId()` - Find predicted flight for comparison
- `existsByPlanId()` - Check if prediction exists

**Used By**: PredictedFlightController (REST API)

### **OracleDataExtractionService**  **NEW**
**Primary Purpose**: Extract flight data directly from Sigma Oracle production database

**Key Features**:
- **Direct Database Access**: Connect to Sigma Oracle database and extract ReplayPath packets
- **Hardcoded Date Processing**: Process flight data for specific date (2025-07-11) as configured
- **Performance Monitoring**: Track database connection, extraction, and processing times
- **Error Handling**: Comprehensive error handling for database operations
- **Integration with Streaming**: Seamlessly integrates with existing StreamingFlightService
- **Production Ready**: Replaces file-based input with direct database access

**Main Methods**:
- `processFlightDataFromOracle()` - Main entry point for Oracle data extraction and processing
- `testOracleConnection()` - Test database connectivity
- `getIntegrationSummary()` - Get comprehensive integration status and metrics

**Technical Details**:
- Uses Spring JDBC Template for Oracle database operations
- Integrates with existing `IMergeSupport` and `ReplaySerializer` from Sigma codebase
- Processes data through existing `StreamingFlightService` for consistency
- Returns detailed `OracleProcessingResult` with performance metrics

**Used By**: StreamingController (REST API endpoints)

**Configuration Requirements**:
- Oracle database connection configuration in `application.yml`
- Environment variables for database credentials
- Access to Sigma production database schema
**Primary Purpose**: Perform comprehensive arrival punctuality analysis (ICAO KPI14)

**Key Features**:
- **Flight Qualification**: Find flights with SBSP ↔ SBRJ routes and AERODROME endpoints
- **Flight Matching**: Match predicted flights with real flights via instanceId/planId
- **Geographic Validation**: Filter flights based on 2 NM threshold and flight level ≤ 4
- **Coordinate Extraction**: Extract airport coordinates from route elements
- **Time Comparison**: Compare predicted vs actual flight times
- **KPI Calculation**: Calculate percentages within tolerance windows (±3, ±5, ±15 minutes)
- **Statistical Reporting**: Generate comprehensive analysis reports

**Main Methods**:
- `findQualifyingFlights()` - Find flights meeting route criteria
- `matchPredictedWithRealFlights()` - Match predicted with real flights
- `filterFlightsByGeographicValidation()` - Apply geographic validation filters
- `extractAirportCoordinates()` - Extract airport coordinates
- `calculatePunctualityKPIs()` - Calculate KPI percentages
- `getQualifyingFlightsStatistics()` - Statistics about qualifying flights
- `getFlightMatchingStatistics()` - Statistics about flight matching
- `getGeographicValidationStatistics()` - Statistics about geographic validation

**Analysis Pipeline**:
1. **Step 1**: Find qualifying flights (SBSP ↔ SBRJ with AERODROME endpoints)
2. **Step 2**: Match predicted flights with real flights (instanceId ↔ planId)
3. **Step 3**: Apply geographic validation (2 NM threshold + flight level ≤ 4)
4. **Step 4**: Calculate punctuality KPIs (time comparison with tolerance windows)

**Used By**: PunctualityAnalysisController (REST API)

### **ReplayDataService** 
**Primary Purpose**: Load and access replay data from files

**Key Features**:
- Load ReplayData from JSON files
- Provide basic data access methods
- Format timestamps for display
- Find flights by call sign

**Main Methods**:
- `loadReplayData()` - Load from JSON file
- `printDataSummary()` - Basic statistics
- `findFlightsByCallSign()` - Search functionality
- `getTrackingPointsForPlan()` - Data access

**Used By**: Test classes, batch processing

### **DataAnalysisService** 
**Primary Purpose**: Comprehensive data analysis and insights

**Key Features**:
- Analyze data patterns and distributions
- Provide join strategy recommendations
- Generate statistical reports
- Support data quality assessment

**Main Methods**:
- `analyzePlanIds()` - Plan ID distribution analysis
- `analyzeIndicatives()` - Indicative matching analysis
- `analyzeRealPathData()` - Tracking data analysis
- `analyzeFlightIntentions()` - Flight plan analysis
- `analyzeCorrelations()` - Cross-data correlation
- `recommendJoinStrategy()` - Strategy recommendations

**Used By**: Analysis tools, data quality assessment

### **FlightDataJoinService** 
**Primary Purpose**: Join flight intentions with tracking points

**Key Features**:
- Join data using indicative matching
- Export joined data to JSON
- Provide search functionality
- Handle unmatched tracking points

**Main Methods**:
- `joinFlightData()` - Main joining logic
- `exportToJson()` - Data export
- `getUnmatchedTrackingPoints()` - Find orphaned data
- `searchFlights()` - Search functionality

**Used By**: Batch processing, data export tools

## Data Flow Between Services

```
File Input → ReplayDataService → DataAnalysisService → FlightDataJoinService → Export
     ↓              ↓                    ↓                      ↓
ReplayData    Basic Access        Analysis & Insights    Joined Data
     ↓              ↓                    ↓                      ↓
StreamingFlightService ← ReplayPath (from API or Oracle)
     ↓
MongoDB Storage (flights collection)

Oracle Database → OracleDataExtractionService → StreamingFlightService
     ↓                      ↓                           ↓
Sigma Production      ReplayPath Extraction      MongoDB Storage
     ↓                      ↓                           ↓
Flight Data          Performance Metrics        Processed Flights

Prediction Input → PredictedFlightService
     ↓                      ↓
Predicted Flight JSON   Processing & planId mapping
     ↓                      ↓
                    MongoDB Storage (predicted_flights collection)
```

## Service Dependencies

### **No Dependencies** (Independent Services)
- **ReplayDataService**: Standalone file operations
- **DataAnalysisService**: Pure analysis, no external dependencies

### **Database Dependencies**
- **StreamingFlightService**: Uses FlightRepository for MongoDB operations
- **PredictedFlightService**: Uses PredictedFlightRepository for MongoDB operations
- **OracleDataExtractionService**: Uses Oracle JDBC Template and integrates with StreamingFlightService

### **External System Dependencies**
- **OracleDataExtractionService**: Requires Oracle database connection and Sigma codebase integration

### **Model Dependencies**
- **FlightDataJoinService**: Uses ObjectMapper for JSON operations
- **ReplayDataService**: Uses ObjectMapper for JSON operations

## Best Practices

### **When to Use Each Service**

1. **For Production Oracle Integration**: Use `OracleDataExtractionService`
2. **For Real-time Processing**: Use `StreamingFlightService`
3. **For Predicted Flight Processing**: Use `PredictedFlightService`
4. **For File Loading**: Use `ReplayDataService`
5. **For Data Analysis**: Use `DataAnalysisService`
6. **For Data Joining**: Use `FlightDataJoinService`

### **Service Communication**

- **Services should not call each other** unless necessary
- **Use models for data transfer** between services
- **Keep services focused** on their primary responsibility
- **Use dependency injection** for required dependencies

### **Error Handling**

- **StreamingFlightService**: Logs errors and continues processing
- **PredictedFlightService**: Returns ProcessingResult with success/failure status
- **ReplayDataService**: Throws IOException for file issues
- **DataAnalysisService**: Graceful handling of missing data
- **FlightDataJoinService**: Returns empty collections for errors

## Performance Considerations

### **StreamingFlightService**
- **High Performance**: Optimized for real-time processing
- **Batch Operations**: Processes multiple flights efficiently
- **Database Optimization**: Uses indexes for fast lookups

### **PredictedFlightService**
- **JSON Processing**: Efficient parsing of complex predicted flight data
- **planId Mapping**: Automatic mapping of JSON 'id' to 'planId' for comparison
- **Upsert Operations**: Updates existing predictions or creates new ones

### **OracleDataExtractionService** **NEW**
- **Database Performance**: Optimized Oracle queries for efficient data extraction
- **Connection Management**: Proper connection pooling and resource management
- **Batch Processing**: Processes multiple ReplayPath packets efficiently
- **Performance Monitoring**: Detailed timing metrics for all operations
- **Error Recovery**: Robust error handling for database connectivity issues

### **DataAnalysisService**
- **Memory Efficient**: Streams data for large datasets
- **Lazy Evaluation**: Only processes data when needed
- **Caching**: Consider caching for repeated analyses

### **FlightDataJoinService**
- **Join Optimization**: Efficient indicative matching
- **Memory Management**: Processes data in chunks for large files
- **Export Efficiency**: Streaming JSON export for large datasets

## Testing Strategy

### **Unit Tests**
- Test each service independently
- Mock dependencies where appropriate
- Test error conditions and edge cases

### **Integration Tests**
- Test service interactions
- Test with real data files
- Test database operations

### **Performance Tests**
- Test with large datasets
- Monitor memory usage
- Test concurrent operations 