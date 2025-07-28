# Service Layer Documentation

This document explains all the services in the Aviation Replay Data Processor and their responsibilities.

## Service Overview

The services are organized by their primary responsibilities:

### **1. Data Processing Services**
| Service | Purpose | Primary Use |
|---------|---------|-------------|
| **StreamingFlightService** | Real-time data processing | Production streaming API |
| **FlightDataJoinService** | Data joining and export | Batch processing and export |

### **2. Data Access Services**
| Service | Purpose | Primary Use |
|---------|---------|-------------|
| **ReplayDataService** | File loading and basic access | File-based operations |

### **3. Analysis Services**
| Service | Purpose | Primary Use |
|---------|---------|-------------|
| **DataAnalysisService** | Comprehensive data analysis | Data quality and insights |

## Service Responsibilities

### **StreamingFlightService** 🚀
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

### **ReplayDataService** 📁
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

### **DataAnalysisService** 📊
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

### **FlightDataJoinService** 🔗
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
StreamingFlightService ← ReplayPath (from API)
     ↓
MongoDB Storage
```

## Service Dependencies

### **No Dependencies** (Independent Services)
- **ReplayDataService**: Standalone file operations
- **DataAnalysisService**: Pure analysis, no external dependencies

### **Database Dependencies**
- **StreamingFlightService**: Uses FlightRepository for MongoDB operations

### **Model Dependencies**
- **FlightDataJoinService**: Uses ObjectMapper for JSON operations
- **ReplayDataService**: Uses ObjectMapper for JSON operations

## Best Practices

### **When to Use Each Service**

1. **For Real-time Processing**: Use `StreamingFlightService`
2. **For File Loading**: Use `ReplayDataService`
3. **For Data Analysis**: Use `DataAnalysisService`
4. **For Data Joining**: Use `FlightDataJoinService`

### **Service Communication**

- **Services should not call each other** unless necessary
- **Use models for data transfer** between services
- **Keep services focused** on their primary responsibility
- **Use dependency injection** for required dependencies

### **Error Handling**

- **StreamingFlightService**: Logs errors and continues processing
- **ReplayDataService**: Throws IOException for file issues
- **DataAnalysisService**: Graceful handling of missing data
- **FlightDataJoinService**: Returns empty collections for errors

## Performance Considerations

### **StreamingFlightService**
- **High Performance**: Optimized for real-time processing
- **Batch Operations**: Processes multiple flights efficiently
- **Database Optimization**: Uses indexes for fast lookups

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