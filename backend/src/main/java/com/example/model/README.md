# Model Package Documentation

This package contains all data models for the Aviation Flight Data Processing System, organized by their purpose and usage in the data processing pipeline.

## Model Categories

### **1. Input/Streaming Models**
Models for receiving and processing external flight data.

#### `ReplayPath.java`
**Real-time streaming packet container**
- **Purpose**: Processes live flight data from external systems
- **Key Fields**: `listRealPath`, `listFlightIntention`, `time`, `packetStoredTimestamp`
- **Usage**: StreamingController endpoints, real-time processing
- **Collection**: Not stored directly (processed into JoinedFlightData)



#### `RealPathPoint.java`
**Raw flight tracking data**
- **Purpose**: Individual GPS tracking points from aircraft
- **Key Fields**: `planId`, `latitude`, `longitude`, `flightLevel`, `speed`, `indicativeSafe`
- **Usage**: Embedded in ReplayPath, processed into TrackingPoint
- **Features**: Contains kinematic data, transponder info, timing data

#### `FlightIntention.java`
**Flight plan and intention data**
- **Purpose**: Planned flight information (routes, timing, aircraft details)
- **Key Fields**: `planId`, `indicative`, `aircraftType`, `eobt`, `eta`, `startPointIndicative`, `endPointIndicative`
- **Usage**: Embedded in ReplayPath, processed into JoinedFlightData
- **Features**: Complete flight planning information

#### `Kinematic.java`
**Movement and position data**
- **Purpose**: Detailed kinematic information for tracking points
- **Key Fields**: `latitude`, `longitude`, `speed`, `bearing`, `altitude`
- **Usage**: Embedded in RealPathPoint
- **Features**: Precise movement calculations

### **2. Storage Models (MongoDB Documents)**
Models optimized for database storage and querying.

#### `JoinedFlightData.java`
**Primary flight document** - Collection: `flights`
- **Purpose**: Complete flight record combining intentions and tracking data
- **Key Fields**: `planId` (indexed, unique), `indicative`, `trackingPoints`, flight timing data
- **Usage**: Main storage for actual flight data, used by FlightRepository
- **Features**: Embedded TrackingPoint list, optimized for MongoDB queries
- **Indexes**: planId (unique), indicative

#### `PredictedFlightData.java`
**Predicted flight document** - Collection: `predicted_flights`
- **Purpose**: Stores predicted flight routes and timing for comparison analysis
- **Key Fields**: `instanceId` (indexed), `routeElements`, `routeSegments`, `indicative`
- **Usage**: Prediction analysis, punctuality comparison
- **Features**: Complete route prediction with elements and segments
- **Indexes**: instanceId (for matching with JoinedFlightData.planId)

#### `ProcessingHistory.java`
**Operation tracking document** - Collection: `processing_history`
- **Purpose**: Tracks all processing operations for monitoring and debugging
- **Key Fields**: `operation`, `status`, `timestamp`, `duration`, `endpoint`
- **Usage**: Operation monitoring, performance analysis, error tracking
- **Features**: Enum-based operation types, status tracking, error details

### **3. Processing Support Models**
Models that support data processing and transformation.

#### `TrackingPoint.java`
**Simplified tracking data**
- **Purpose**: Clean, simplified tracking information extracted from RealPathPoint
- **Key Fields**: `planId`, `latitude`, `longitude`, `flightLevel`, `speed`, `timestamp`
- **Usage**: Embedded in JoinedFlightData, optimized for storage
- **Features**: Essential tracking fields only, MongoDB-optimized

#### `RouteElement.java`
**Predicted route point**
- **Purpose**: Individual points in predicted flight routes
- **Key Fields**: `latitude`, `longitude`, `levelMeters`, `speedMeterPerSecond`, `eetMinutes`
- **Usage**: Embedded in PredictedFlightData, trajectory analysis
- **Features**: Supports densification (interpolated flag), sequence numbering

#### `RouteSegment.java`
**Route connection data**
- **Purpose**: Connections between route elements in predicted flights
- **Key Fields**: Connection information between route points
- **Usage**: Embedded in PredictedFlightData
- **Features**: Route continuity and path planning

### **4. Request/Response Models**
Models for API communication and data exchange.

#### `PlanIdRequest.java`
**API request for planId operations**
- **Purpose**: Handles single planId or batch planId requests
- **Key Fields**: `planId`, `planIds`
- **Usage**: PredictedFlightController endpoints
- **Features**: Validation methods, supports both single and batch operations

#### `OracleProcessingResult.java`
**Oracle operation results**
- **Purpose**: Results from Oracle database processing operations
- **Key Fields**: `newFlights`, `updatedFlights`, `processingTimeMs`, `dataSource`
- **Usage**: StreamingController Oracle integration responses
- **Features**: Performance metrics, error tracking, detailed statistics

#### `OracleExtractionResponse.java`
**Oracle extraction results**
- **Purpose**: Results from Oracle data extraction operations
- **Key Fields**: `totalRequested`, `totalProcessed`, `totalNotFound`, `totalErrors`
- **Usage**: PredictedFlightController batch processing responses
- **Features**: Detailed breakdown of processing results, "Option A" error handling

#### `BatchProcessingResult.java`
**Batch operation results**
- **Purpose**: Generic batch processing results
- **Key Fields**: `totalRequested`, `totalProcessed`, processing statistics
- **Usage**: Various batch operations across controllers
- **Features**: Comprehensive batch operation tracking

### **5. Analysis Result Models**
Models representing analysis outputs and KPI calculations.

#### `PunctualityAnalysisResult.java`
**Punctuality analysis results**
- **Purpose**: ICAO KPI14 punctuality analysis results
- **Key Fields**: `delayToleranceWindows`, `percentageWithinTolerance`, KPI metrics
- **Usage**: PunctualityAnalysisController responses
- **Features**: Multiple tolerance windows (±3, ±5, ±15 minutes), statistical reporting

#### `TrajectoryAccuracyResult.java`
**Trajectory accuracy analysis results**
- **Purpose**: MSE/RMSE trajectory accuracy analysis results
- **Key Fields**: `aggregateMetrics`, `flightResults`, horizontal/vertical accuracy
- **Usage**: TrajectoryAccuracyAnalysisController responses
- **Features**: Per-flight and aggregate metrics, unit conversions (radians ↔ meters)

#### `TrajectoryDensificationResult.java`
**Trajectory densification results**
- **Purpose**: Results from trajectory densification operations
- **Key Fields**: `planId`, `originalRouteElementCount`, `finalRouteElementCount`, `sigmaSuccessRate`
- **Usage**: TrajectoryDensificationController responses
- **Features**: Success rate tracking, detailed densification metrics

## Data Flow Architecture

```
External Data → Processing → Storage → Analysis
     ↓              ↓          ↓         ↓
ReplayPath → StreamingService → JoinedFlightData → Analysis Results
     ↓              ↓          ↓         ↓
Oracle DB  → OracleService → PredictedFlightData → KPI Reports
```

## Key Relationships

### **Flight Matching**
- `JoinedFlightData.planId` ↔ `PredictedFlightData.instanceId`
- Used for comparing actual vs predicted flight data

### **Data Embedding**
- `JoinedFlightData` contains `List<TrackingPoint>`
- `PredictedFlightData` contains `List<RouteElement>` and `List<RouteSegment>`
- `RealPathPoint` contains `Kinematic` data

### **Processing Tracking**
- All major operations create `ProcessingHistory` records
- Links operations to their results and performance metrics

## MongoDB Collections

| Collection | Model | Purpose | Key Indexes |
|------------|-------|---------|-------------|
| `flights` | JoinedFlightData | Actual flight data | planId (unique), indicative |
| `predicted_flights` | PredictedFlightData | Predicted flight data | instanceId |
| `processing_history` | ProcessingHistory | Operation tracking | timestamp, operation |

## Model Relationships

### **Input Models**
```
ReplayPath (Streaming Input)
├── listFlightIntention: List<FlightIntention>
│   ├── planId (unique identifier)
│   ├── indicative (call sign)
│   ├── aircraftType, airline
│   ├── eobt, eta (timing)
│   └── startPointIndicative, endPointIndicative
└── listRealPath: List<RealPathPoint>
    ├── planId (may be 0)
    ├── indicativeSafe (call sign)
    ├── flightLevel, speed
    ├── latitude, longitude
    └── kinematic: Kinematic
        ├── position (lat/lng)
        └── speed, bearing

Oracle Database (Prediction Input)
├── Flight Data by planId
├── Route Elements
└── Route Segments
```

### **Output Models**
```
JoinedFlightData (MongoDB: flights collection)
├── planId (unique, indexed)
├── indicative, aircraftType, airline
├── eobt, eta, flightPlanDate
├── startPointIndicative, endPointIndicative
├── trackingPoints: List<TrackingPoint>
│   ├── latitude, longitude
│   ├── flightLevel, speed
│   ├── timestamp, seqNum
│   └── indicativeSafe
└── metadata (totalTrackingPoints, hasTrackingData)

PredictedFlightData (MongoDB: predicted_flights collection)
├── instanceId (indexed, matches planId)
├── routeId, indicative
├── time, startPointIndicative, endPointIndicative
├── routeElements: List<RouteElement>
│   ├── latitude, longitude, levelMeters
│   ├── speedMeterPerSecond, eetMinutes
│   ├── elementType, coordinateText
│   └── interpolated (for densification)
└── routeSegments: List<RouteSegment>

ProcessingHistory (MongoDB: processing_history collection)
├── operation (enum: PROCESS_REAL_DATA, SYNC_PREDICTED_DATA, etc.)
├── status (enum: SUCCESS, PARTIAL_SUCCESS, FAILURE, IN_PROGRESS)
├── timestamp, duration
├── endpoint, parameters
└── message, errorDetails

Analysis Result Models (API Responses)
├── PunctualityAnalysisResult
│   ├── delayToleranceWindows (±3, ±5, ±15 minutes)
│   ├── percentageWithinTolerance
│   └── kpiOutput (ICAO KPI14)
├── TrajectoryAccuracyResult
│   ├── aggregateMetrics (MSE/RMSE)
│   ├── flightResults (per-flight accuracy)
│   └── horizontalRMSEMeters, verticalRMSEMeters
└── TrajectoryDensificationResult
    ├── originalRouteElementCount
    ├── finalRouteElementCount
    └── sigmaSuccessRate
```

## Usage Patterns

### **Real-time Processing**
```java
ReplayPath → StreamingFlightService → JoinedFlightData → MongoDB
```

### **Prediction Processing**
```java
Oracle DB → OracleFlightDataService → PredictedFlightData → MongoDB
```

### **Analysis Operations**
```java
JoinedFlightData + PredictedFlightData → AnalysisService → Result Models
```

### **Batch Operations**
```java
List<PlanId> → BatchProcessing → BatchProcessingResult
```

## Key Features

### **Flexible JSON Handling**
- All models use `@JsonIgnoreProperties(ignoreUnknown = true)`
- Property order doesn't matter in JSON input
- Robust handling of missing or extra fields

### **MongoDB Optimization**
- Strategic indexing on key fields (planId, instanceId)
- Embedded documents for related data
- Efficient querying patterns

### **Performance Tracking**
- All result models include `processingTimeMs`
- Detailed timing breakdowns for complex operations
- Performance metrics in all API responses

### **Error Handling**
- "Option A" strategy: skip missing data, report detailed statistics
- Comprehensive error tracking in result models
- Processing history for debugging and monitoring

### **Analysis Support**
- Models designed for trajectory accuracy analysis
- Punctuality analysis with ICAO KPI14 compliance
- Densification support with success rate tracking

## Validation and Constraints

### **Business Rules**
- `planId` is the primary business identifier (unique in flights collection)
- `instanceId` in predicted flights matches `planId` in actual flights
- Time fields stored as strings for flexibility
- Coordinates in various units (degrees, radians, meters) with conversion support

### **Data Integrity**
- MongoDB document validation through Spring Data annotations
- Indexed fields for performance and uniqueness constraints
- Embedded document validation for complex nested structures 