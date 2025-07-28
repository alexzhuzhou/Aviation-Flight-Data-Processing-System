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
| **JoinedFlightData** | MongoDB document | Database storage |
| **TrackingPoint** | Simplified tracking | Embedded in JoinedFlightData |

## Data Flow

```
External System → Your System → MongoDB
     ↓              ↓           ↓
ReplayPath    → Processing → JoinedFlightData
FlightIntention → Service   → TrackingPoint
RealPathPoint
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
JoinedFlightData (MongoDB Document)
├── planId (unique, indexed)
├── indicative, aircraftType, airline
├── trackingPoints: List<TrackingPoint>
│   ├── latitude, longitude
│   ├── flightLevel, speed
│   └── timestamp
└── metadata (totalTrackingPoints, hasTrackingData)
```

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

## Usage Guidelines

### **When to Use Each Model**

1. **For File Processing**: Use `ReplayData`
2. **For API Endpoints**: Use `ReplayPath`
3. **For Database Storage**: Use `JoinedFlightData`
4. **For Data Analysis**: Use `TrackingPoint`

### **Data Transformation**
- `RealPathPoint` → `TrackingPoint` (simplification)
- `FlightIntention` → `JoinedFlightData` (enrichment)
- `ReplayData` → `ReplayPath` (conversion for streaming)

## Best Practices

1. **Input Models**: Don't modify, they must match external format
2. **Output Models**: Optimize for your system's needs
3. **Transformations**: Use services to convert between models
4. **Documentation**: Keep this document updated when models change 