# Service Package Documentation

This package contains the core business logic services for the Aviation Flight Data Processing System. These services handle data processing, analysis, and integration with external systems.

## Service Architecture Overview

### **Core Data Processing Services**
- `StreamingFlightService` - Real-time flight data processing
- `OracleDataExtractionService` - Oracle database integration
- `PredictedFlightService` - Predicted flight data processing
- `OracleFlightDataService` - Oracle flight data extraction
- `TrajectoryDensificationService` - Trajectory point densification

### **Analysis Services**
- `PunctualityAnalysisService` - ICAO KPI14 punctuality analysis
- `TrajectoryAccuracyAnalysisService` - MSE/RMSE trajectory accuracy


### **Support Services**
- `FlightSearchService` - Flight search and management
- `ProcessingHistoryService` - Operation tracking and monitoring

## Detailed Service Documentation

### `StreamingFlightService.java`
**Core Real-time Flight Data Processing**

**Purpose**: Processes streaming flight data with advanced disambiguation and deduplication

**Key Features:**
- **Timestamp-based disambiguation** for flights with same indicative
- **Cross-packet tracking point assignment** using indicative ↔ indicativeSafe matching
- **Deduplication** using timestamp + coordinates + indicativeSafe for uniqueness
- **Flight time window matching** with 30-minute tolerance
- **Data contamination prevention** by discarding ambiguous tracking points

**Core Methods:**
- `processReplayPath(ReplayPath)` - Main entry point for streaming data
- `processFlightIntentions(List<FlightIntention>)` - Process flight plans
- `processRealPathPoints(List<RealPathPoint>)` - Process tracking points
- `getStats()` - Get processing statistics
- `analyzeDuplicateIndicatives()` - Analyze data quality issues
- `cleanupDuplicateTrackingPoints()` - Remove duplicate points

**Disambiguation Strategy:**
1. Match tracking point timestamp to flight time window (flightPlanDate to currentDateTimeOfArrival)
2. Find closest time window with 30-minute tolerance
3. If no temporal match found, discard tracking points to prevent data contamination

**Used By**: StreamingController, OracleDataExtractionService

### `OracleDataExtractionService.java`
**Sigma Oracle Database Integration**

**Purpose**: Extracts flight data directly from Sigma Oracle database (replicates PathVoGeneratorTest functionality)

**Key Features:**
- **Direct Oracle connectivity** using Sigma configuration
- **ReplayPath packet generation** from database queries
- **Flexible date/time range extraction** with optional parameters
- **Performance monitoring** with detailed timing metrics
- **Error handling** with connection testing and retry logic

**Core Methods:**
- `extractAndProcessFlightData()`
- `extractAndProcessFlightData(date, startTime, endTime)` - Extract with parameters
- `testDatabaseConnection()` - Test Oracle connectivity
- `extractReplayPathsFromDatabase()` - Raw data extraction

**Configuration:**
- Uses `@Qualifier("jdbcTemplateSigma")` for Oracle connection
- Integrates with `SigmaConfig.ExtendedIMergeSupport`


**Used By**: StreamingController

### `PredictedFlightService.java`
**Predicted Flight Data Processing**

**Purpose**: Processes predicted flight data from various sources and stores in MongoDB

**Key Features:**
- **JSON to model conversion** with field mapping (id → instanceId)
- **MongoDB storage** in predicted_flights collection
- **Batch processing** with detailed result tracking
- **Data validation** and error handling
- **Statistics and reporting**

**Core Methods:**
- `processPredictedFlight(JsonNode)` - Process single prediction from JSON
- `processPredictedFlightFromMap(Map)` - Process from Oracle extraction
- `getStats()` - Get prediction processing statistics
- `validatePredictedFlightData()` - Data validation

**Data Mapping:**
- Maps JSON 'id' field to model 'instanceId' for matching with actual flights
- Handles RouteElement and RouteSegment nested structures
- Supports both JSON and Map input formats

**Used By**: PredictedFlightController, OracleFlightDataService

### `PunctualityAnalysisService.java`
**ICAO KPI14 Punctuality Analysis**

**Purpose**: Performs comprehensive arrival punctuality analysis according to ICAO KPI14 standards

**Key Features:**
- **Route filtering** for SBSP ↔ SBRJ flights only
- **Geographic validation** with 2 NM threshold and flight level ≤ 4
- **Flight matching** via instanceId (predicted) ↔ planId (actual)
- **Multiple tolerance windows** (±3, ±5, ±15 minutes)
- **Airport coordinate extraction** from route elements
- **KPI calculation** with percentage reporting

**Core Methods:**
- `findQualifyingFlights()` - Find SBSP ↔ SBRJ routes with AERODROME endpoints
- `extractAirportCoordinates()` - Extract departure/arrival coordinates
- `matchPredictedWithRealFlights()` - Match predictions with actual flights
- `filterFlightsByGeographicValidation()` - Apply 2 NM + FL≤4 validation
- `calculatePunctualityKPIs()` - Calculate ICAO KPI14 metrics

**Analysis Pipeline:**
1. Filter predicted flights for SBSP ↔ SBRJ routes
2. Extract airport coordinates from route elements
3. Match with real flights by planId
4. Apply geographic validation (2 NM threshold)
5. Calculate punctuality within tolerance windows
6. Generate ICAO KPI14 compliant reports

**Used By**: PunctualityAnalysisController, TrajectoryAccuracyAnalysisService

### `TrajectoryAccuracyAnalysisService.java`
**MSE/RMSE Trajectory Accuracy Analysis**

**Purpose**: Analyzes trajectory accuracy by comparing predicted routes with actual tracking points

**Key Features:**
- **Reuses punctuality filtering pipeline** for consistency
- **Point-by-point comparison** with equal count requirement
- **MSE/RMSE calculations** for horizontal and vertical accuracy
- **Unit conversions** (degrees ↔ radians, meters ↔ flight levels)
- **Per-flight and aggregate metrics**

**Core Methods:**
- `runTrajectoryAccuracyAnalysis()` - Complete accuracy analysis
- `getTrajectoryAccuracyStats()` - Get analysis statistics
- `calculateAccuracyMetrics()` - MSE/RMSE calculations
- `convertUnits()` - Handle coordinate and altitude conversions

**Analysis Process:**
1. Use PunctualityAnalysisService filtering (SBSP ↔ SBRJ, geographic validation)
2. Match predicted flights with real flights
3. Ensure equal point counts (requires densification)
4. Calculate horizontal MSE/RMSE (lat/lng coordinates)
5. Calculate vertical MSE/RMSE (altitude/flight level)
6. Provide both per-flight and aggregate statistics

**Unit Conversions:**
- Predicted coordinates (degrees) → Real coordinates (radians)
- Predicted altitude (meters/levelmeters) → Real flight levels × 30.48m
- MSE/RMSE results in both radians and meters

**Used By**: TrajectoryAccuracyAnalysisController

### `TrajectoryDensificationService.java`
**Trajectory Point Densification**

**Purpose**: Densifies predicted flight trajectories using Sigma simulation engine to match real flight tracking density

**Key Features:**
- **Sigma SimTrackSimulator integration** for accurate interpolation
- **Linear interpolation** along route segments
- **Batch processing** with success rate tracking
- **Sigma success rate calculation** (≥90% threshold)
- **UTC timezone handling** for aviation calculations

**Core Methods:**
- `densifyPredictedTrajectory(planId)` - Densify single flight
- `densifyMultipleTrajectories(List<planId>)` - Batch densification
- `buildFlightIntentionVO()` - Convert to Sigma format
- `simulateTrajectory()` - Run Sigma simulation
- `extractDensifiedElements()` - Extract interpolated points

**Densification Process:**
1. Get real flight tracking point count
2. Get predicted flight route elements
3. Convert to Sigma FlightIntentionVO format
4. Run SimTrackSimulator for interpolation
5. Extract densified route elements
6. Calculate success rate and metrics

**Success Rate Calculation:**
- Sigma Success Rate = (densified points / target points) × 100%
- ≥90% considered high success rate
- Tracks interpolation quality and coverage

**Used By**: TrajectoryDensificationController

### `OracleFlightDataService.java`
**Oracle Flight Data Extraction**

**Purpose**: Extracts flight data from Oracle database using Hibernate (replicates SimpleHibernateTest logic)

**Key Features:**
- **ExtendedHibernateOperations** for better serialization handling
- **JPQL queries with JOIN FETCH** for efficient data loading
- **JSON building logic** matching SimpleHibernateTest
- **Batch extraction** with error handling
- **Coordinate extraction** from JTS geometry

**Core Methods:**
- `extractFlightData(planId)` - Extract single flight
- `extractFlightDataBatch(List<planId>)` - Batch extraction
- `buildFlightDataMap()` - Convert to Map format
- `extractCoordinates()` - Handle JTS geometry

**Database Integration:**
- Uses `@Qualifier("hibernateOps")` for Hibernate operations
- Same JPQL queries as SimpleHibernateTest
- Handles HistoricFlightIntention and HistoricRoute entities
- Coordinate extraction from JTS geometry objects

**Used By**: PredictedFlightController

### `FlightSearchService.java`
**Flight Search and Management**

**Purpose**: Provides comprehensive search, filtering, and management operations for flight data

**Key Features:**
- **Multi-criteria search** (planId, indicative, origin, destination)
- **Partial matching** with case-insensitive queries
- **Bulk operations** (delete, update)
- **Search statistics** and performance metrics
- **Both real and predicted flight search**

**Core Methods:**
- `searchRealFlightsByPlanId()` - Search actual flights
- `searchPredictedFlightsByInstanceId()` - Search predictions
- `searchByIndicative()`, `searchByOrigin()`, `searchByDestination()` - Various search criteria
- `deleteRealFlight()`, `deletePredictedFlight()` - Delete operations
- `bulkDeleteFlights()` - Bulk delete with matching options
- `getSearchStats()` - Search performance statistics

**Search Features:**
- Regex-based partial matching
- Case-insensitive search
- Multiple result handling
- Performance optimization with indexes

**Used By**: FlightSearchController

### `ProcessingHistoryService.java`
**Processing Operation Tracking**

**Purpose**: Tracks all processing operations for monitoring, debugging, and performance analysis

**Key Features:**
- **Operation lifecycle tracking** (start, complete, failure)
- **Performance metrics** with duration tracking
- **Error details** and troubleshooting information
- **Dashboard integration** with statistics
- **Data cleanup** and maintenance

**Core Methods:**
- `startOperation()` - Begin operation tracking
- `completeSuccess()`, `completeFailure()`, `completePartialSuccess()` - End tracking
- `getRecentHistory()` - Get recent operations
- `getProcessingStatistics()` - Dashboard statistics
- `getAverageDurationByOperation()` - Performance analysis
- `cleanupOldEntries()` - Data maintenance

**Operation Types:**
- `PROCESS_REAL_DATA` - Real flight data processing
- `SYNC_PREDICTED_DATA` - Predicted flight synchronization
- `DENSIFY_PREDICTED_DATA` - Trajectory densification

**Status Types:**
- `SUCCESS` - Operation completed successfully
- `PARTIAL_SUCCESS` - Completed with some errors/warnings
- `FAILURE` - Operation failed
- `IN_PROGRESS` - Currently running

**Used By**: All controllers for operation tracking

## Service Integration Patterns

### **Data Flow Architecture**
```
External Data → Processing Services → Storage → Analysis Services
     ↓                    ↓             ↓            ↓
Oracle/Stream → StreamingFlightService → MongoDB → Analysis Results
Oracle DB → OracleFlightDataService → PredictedFlightData → KPI Reports
```

### **Service Dependencies**
```
Controllers
    ↓
StreamingFlightService ← OracleDataExtractionService
    ↓
FlightRepository (MongoDB)
    ↓
PunctualityAnalysisService → TrajectoryAccuracyAnalysisService
    ↓
TrajectoryDensificationService
    ↓
Analysis Results
```

### **Cross-Service Communication**
- **PunctualityAnalysisService** provides filtering pipeline for **TrajectoryAccuracyAnalysisService**
- **OracleDataExtractionService** uses **StreamingFlightService** for processing
- **OracleFlightDataService** provides data for **PredictedFlightService**
- **ProcessingHistoryService** tracks operations across all services

## Key Design Patterns

### **Template Method Pattern**
Services follow consistent processing patterns:
1. Input validation
2. Data extraction/transformation
3. Business logic processing
4. Result generation
5. Error handling
6. Performance tracking

### **Strategy Pattern**
Different processing strategies for:
- **Real-time vs Batch processing** (StreamingFlightService)
- **Single vs Batch operations** (PredictedFlightService)
- **Different analysis types** (Punctuality vs Accuracy)

### **Observer Pattern**
**ProcessingHistoryService** observes operations across all services for tracking and monitoring.

### **Adapter Pattern**
Services adapt between different data formats:
- **Oracle data** → **ReplayPath** (OracleDataExtractionService)
- **JSON** → **PredictedFlightData** (PredictedFlightService)
- **MongoDB entities** → **Analysis results** (Analysis services)



