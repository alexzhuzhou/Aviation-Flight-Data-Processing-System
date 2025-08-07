# Model Documentation

This document explains all the data models in the Aviation Replay Data Processor and their relationships.

## Model Overview

The models are organized into three categories:

### **1. Input/External Models** (Raw Data from External System)
These models match the external system's JSON structure exactly.

| Model | Purpose | Usage |
|-------|---------|-------|
| **ReplayData** | Static file data container | Batch processing from JSON files |
| **ReplayPath** | Streaming packet container | Real-time processing from API |
| **FlightIntention** | Flight plan data | Flight planning information |
| **RealPathPoint** | Raw tracking data | Real-time position updates |
| **Kinematic** | Movement data | Speed, position, bearing |

### **2. Internal/Processed Models** (Your System's Data)
These models are optimized for your system's needs.

| Model | Purpose | Usage |
|-------|---------|-------|
| **JoinedFlightData** | MongoDB document | Database storage (actual flights) |
| **TrackingPoint** | Simplified tracking | Embedded in JoinedFlightData |

### **3. Predicted Flight Models** (Prediction Data)
These models handle predicted flight information for comparison analysis.

| Model | Purpose | Usage |
|-------|---------|-------|
| **PredictedFlightData** | MongoDB document | Database storage (predicted flights) |
| **RouteElement** | Route point data | Embedded in PredictedFlightData |
| **RouteSegment** | Route connection data | Embedded in PredictedFlightData |

### **4. Analysis Models** (Analysis Results)
These models represent analysis results and KPI outputs.

| Model | Purpose | Usage |
|-------|---------|-------|
| **PunctualityAnalysisResult** | Punctuality analysis results | ICAO KPI14 analysis output |
| **BatchProcessingResult** | Batch processing results | Processing statistics and results |

## Data Flow

```
External System → Your System → MongoDB
     ↓              ↓           ↓
ReplayPath    → Processing → JoinedFlightData (flights collection)
FlightIntention → Service   → TrackingPoint
RealPathPoint

Prediction System → Your System → MongoDB
     ↓                ↓           ↓
Predicted Flight → Processing → PredictedFlightData (predicted_flights collection)
JSON Data       → Service    → RouteElement, RouteSegment

Analysis Service → Your System → API Response
     ↓                ↓           ↓
Flight Data     → Analysis   → PunctualityAnalysisResult (KPI output)
Predicted Data  → Service    → BatchProcessingResult (processing stats)
```

## Model Relationships

### **Input Models**
```
ReplayData/ReplayPath
├── listFlightIntention: List<FlightIntention>
│   ├── planId (unique identifier)
│   ├── indicative (call sign)
│   ├── aircraftType, airline
│   └── SimpleRoute (nested class)
└── listRealPath: List<RealPathPoint>
    ├── planId (may be 0)
    ├── indicativeSafe (call sign)
    ├── flightLevel, speed
    └── Kinematic (nested class)
        ├── position (lat/lng)
        └── speed, bearing
```

### **Output Models**
```
JoinedFlightData (MongoDB Document - flights collection)
├── planId (unique, indexed)
├── indicative, aircraftType, airline
├── trackingPoints: List<TrackingPoint>
│   ├── latitude, longitude
│   ├── flightLevel, speed
│   └── timestamp
└── metadata (totalTrackingPoints, hasTrackingData)

PredictedFlightData (MongoDB Document - predicted_flights collection)
├── planId (indexed, for matching with actual flights)
├── instanceId, routeId, indicative
├── time, startPointIndicative, endPointIndicative
├── routeElements: List<RouteElement>
│   ├── latitude, longitude, levelMeters
│   ├── speedMeterPerSecond, eetMinutes
│   ├── indicative, elementType
│   └── coordinateText
└── routeSegments: List<RouteSegment>
    ├── elementAId, elementBId
    └── distance

PunctualityAnalysisResult (Analysis Output)
├── totalMatchedFlights, totalAnalyzedFlights
├── delayToleranceWindows: List<DelayToleranceWindow>
│   ├── windowDescription (e.g., "± 3 minutes")
│   ├── toleranceMinutes, flightsWithinTolerance
│   ├── percentageWithinTolerance
│   └── kpiOutput (formatted KPI text)
├── analysisTimestamp
└── message (analysis status/notes)

## Key Differences

### **ReplayData vs ReplayPath**
- **ReplayData**: For static files, no packet timestamp
- **ReplayPath**: For streaming, includes packetStoredTimestamp

### **RealPathPoint vs TrackingPoint**
- **RealPathPoint**: Complex, nested structure (external format)
- **TrackingPoint**: Simple, flat structure (internal format)

### **FlightIntention vs JoinedFlightData**
- **FlightIntention**: Raw flight plan data (external format)
- **JoinedFlightData**: Processed flight data with tracking (internal format)

### **Predicted Flight Models vs Actual Flight Models**
- **PredictedFlightData**: Contains route predictions and timing estimates
- **JoinedFlightData**: Contains actual flight tracking and performance data
- **Comparison**: Both use `planId` as the primary key for matching and comparison analysis

## Usage Guidelines

### **When to Use Each Model**

1. **For File Processing**: Use `ReplayData`
2. **For API Endpoints**: Use `ReplayPath`
3. **For Database Storage (Actual Flights)**: Use `JoinedFlightData`
4. **For Database Storage (Predicted Flights)**: Use `PredictedFlightData`
5. **For Data Analysis**: Use `TrackingPoint`
6. **For Prediction Analysis**: Use `RouteElement` and `RouteSegment`

### **Data Transformation**
- `RealPathPoint` → `TrackingPoint` (simplification)
- `FlightIntention` → `JoinedFlightData` (enrichment)
- `ReplayData` → `ReplayPath` (conversion for streaming)
- `Predicted Flight JSON` → `PredictedFlightData` (JSON parsing and planId mapping)
- `JSON RouteElements` → `RouteElement` objects (nested data structure)
- `JSON RouteSegments` → `RouteSegment` objects (nested data structure)

## Best Practices

1. **Input Models**: Don't modify, they must match external format
2. **Output Models**: Optimize for your system's needs
3. **Predicted Flight Models**: Designed for comparison analysis with actual flight data
4. **Transformations**: Use services to convert between models
5. **planId Consistency**: Ensure planId is used consistently across actual and predicted flight models
6. **Documentation**: Keep this document updated when models change 