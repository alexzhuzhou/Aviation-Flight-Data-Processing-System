# 🚀 Complete API Usage Guide

## Overview

This guide provides comprehensive examples for using all API endpoints in the Aviation Flight Data Processing System with Oracle integration.

## 🔧 Prerequisites

1. **Start the application**: `mvn spring-boot:run`
2. **Verify it's running**: `curl http://localhost:8080/api/flights/health`
3. **Oracle connection**: Ensure Oracle database credentials are configured

## 📋 Complete Workflow Examples

### 1. 🗄️ Oracle Database Integration

#### Test Oracle Connection
```bash
curl http://localhost:8080/api/flights/test-oracle-connection
```

**Expected Response:**
```json
{
  "status": "SUCCESS",
  "message": "Oracle connection successful",
  "connectionTimeMs": 234,
  "databaseInfo": {
    "host": "10.103.3.8:1521",
    "serviceName": "SIGMA_PLT3_DEV1_APP",
    "username": "sigma"
  }
}
```

#### Process Flight Data from Oracle
```bash
curl -X POST http://localhost:8080/api/flights/process-packet
```

**Expected Response:**
```json
{
  "status": "SUCCESS",
  "totalFlightsExtracted": 1243,
  "totalFlightsProcessed": 1200,
  "totalTrackingPoints": 45678,
  "extractionTimeMs": 2345,
  "processingTimeMs": 5678,
  "message": "Successfully processed 1200 flights from Oracle database for date 2025-07-11"
}
```

### 2. 📊 Flight Data Analysis

#### Get Flight Statistics
```bash
curl http://localhost:8080/api/flights/stats
```

**Expected Response:**
```json
{
  "totalFlights": 1200,
  "totalTrackingPoints": 45678,
  "averagePointsPerFlight": 38.1,
  "dateRange": {
    "earliest": "2025-07-11T00:00:00Z",
    "latest": "2025-07-11T23:59:59Z"
  },
  "topIndicatives": [
    {"indicative": "TAM3886", "count": 15},
    {"indicative": "GLO1234", "count": 12}
  ]
}
```

#### Get Available PlanIds
```bash
curl http://localhost:8080/api/flights/plan-ids
```

**Expected Response:**
```json
{
  "totalCount": 1200,
  "planIds": [17879345, 17879346, 17879347, 17879348, 17879349],
  "processingTimeMs": 45,
  "message": "Retrieved planIds from processed flights"
}
```

### 3. 🎯 Predicted Flight Processing (Oracle-Based)

#### Process Single PlanId
```bash
curl -X POST http://localhost:8080/api/predicted-flights/process \
  -H "Content-Type: application/json" \
  -d '{"planId": 17879345}'
```

**Expected Response:**
```json
{
  "totalRequested": 1,
  "totalProcessed": 1,
  "totalNotFound": 0,
  "totalErrors": 0,
  "processedPlanIds": [17879345],
  "notFoundPlanIds": [],
  "errorPlanIds": [],
  "extractionTimeMs": 45,
  "processingTimeMs": 123,
  "message": "Successfully processed 1 out of 1 requested planIds"
}
```

#### Process Multiple PlanIds (Batch)
```bash
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d '{"planIds": [17879345, 17879346, 17879347, 99999999]}'
```

**Expected Response:**
```json
{
  "totalRequested": 4,
  "totalProcessed": 3,
  "totalNotFound": 1,
  "totalErrors": 0,
  "processedPlanIds": [17879345, 17879346, 17879347],
  "notFoundPlanIds": [99999999],
  "errorPlanIds": [],
  "extractionTimeMs": 89,
  "processingTimeMs": 234,
  "message": "Successfully processed 3 out of 4 requested planIds. 1 planId not found in Oracle database."
}
```

#### Get Predicted Flight Statistics
```bash
curl http://localhost:8080/api/predicted-flights/stats
```

**Expected Response:**
```json
{
  "totalPredictedFlights": 150,
  "uniquePlanIds": 150,
  "averageRouteElements": 12.5,
  "averageRouteSegments": 8.3,
  "dateRange": {
    "earliest": "2025-07-11T00:00:00Z",
    "latest": "2025-07-11T23:59:59Z"
  },
  "processingStats": {
    "totalProcessed": 150,
    "totalSkipped": 0,
    "totalFailed": 0
  }
}
```

### 4. 📈 Punctuality Analysis (ICAO KPI14)

#### Match Predicted with Real Flights
```bash
curl http://localhost:8080/api/punctuality-analysis/match-flights
```

**Expected Response:**
```json
{
  "totalPredictedFlights": 150,
  "totalRealFlights": 1200,
  "totalMatched": 145,
  "matchingRate": 96.7,
  "unmatchedPredicted": 5,
  "matchedFlights": [
    {
      "planId": 17879345,
      "predictedIndicative": "TAM3886",
      "realIndicative": "TAM3886",
      "predictedTime": "2025-07-11T10:30:00Z",
      "realTime": "2025-07-11T10:32:15Z",
      "timeDifferenceMinutes": 2.25,
      "withinTolerance3min": true,
      "withinTolerance5min": true,
      "withinTolerance15min": true
    }
  ],
  "processingTimeMs": 456,
  "message": "Successfully matched 145 out of 150 predicted flights with real flights"
}
```

#### Run Full Punctuality Analysis
```bash
curl http://localhost:8080/api/punctuality-analysis/run
```

**Expected Response:**
```json
{
  "totalMatchedFlights": 145,
  "totalAnalyzedFlights": 140,
  "qualificationCriteria": {
    "routeFilter": "SBSP ↔ SBRJ routes only",
    "endpointFilter": "AERODROME endpoints only",
    "distanceThreshold": "2 NM from airport",
    "altitudeThreshold": "Flight level ≤ 4"
  },
  "delayToleranceWindows": [
    {
      "windowDescription": "± 3 minutes",
      "toleranceMinutes": 3,
      "flightsWithinTolerance": 85,
      "percentageWithinTolerance": 60.7,
      "kpiOutput": "60.7% of flights where predicted time was within ± 3 minutes of actual time"
    },
    {
      "windowDescription": "± 5 minutes",
      "toleranceMinutes": 5,
      "flightsWithinTolerance": 112,
      "percentageWithinTolerance": 80.0,
      "kpiOutput": "80.0% of flights where predicted time was within ± 5 minutes of actual time"
    },
    {
      "windowDescription": "± 15 minutes",
      "toleranceMinutes": 15,
      "flightsWithinTolerance": 134,
      "percentageWithinTolerance": 95.7,
      "kpiOutput": "95.7% of flights where predicted time was within ± 15 minutes of actual time"
    }
  ],
  "analysisTimestamp": "2024-12-19T10:30:45Z",
  "processingTimeMs": 1234,
  "message": "Analysis completed: 145 predicted flights matched, 140 analyzed successfully"
}
```

#### Get Analysis Statistics
```bash
curl http://localhost:8080/api/punctuality-analysis/stats
```

**Expected Response:**
```json
{
  "totalPredictedFlights": 150,
  "totalRealFlights": 1200,
  "totalMatched": 145,
  "analysisCapability": true,
  "lastAnalysisTimestamp": "2024-12-19T10:30:45Z",
  "dataQuality": {
    "predictedFlightsWithRoutes": 150,
    "realFlightsWithTrackingPoints": 1200,
    "matchingRate": 96.7
  }
}
```

## 🔄 Complete Processing Pipeline

### Step-by-Step Workflow

```bash
# 1. Test Oracle connection
curl http://localhost:8080/api/flights/test-oracle-connection

# 2. Process real flight data from Oracle
curl -X POST http://localhost:8080/api/flights/process-packet

# 3. Get available planIds for predictions
curl http://localhost:8080/api/flights/plan-ids

# 4. Process predicted flights (example with first 3 planIds)
curl -X POST http://localhost:8080/api/predicted-flights/batch \
  -H "Content-Type: application/json" \
  -d '{"planIds": [17879345, 17879346, 17879347]}'

# 5. Run punctuality analysis
curl http://localhost:8080/api/punctuality-analysis/run

# 6. Get final statistics
curl http://localhost:8080/api/flights/stats
curl http://localhost:8080/api/predicted-flights/stats
curl http://localhost:8080/api/punctuality-analysis/stats
```

## 🐍 Python Integration Example

### Complete Python Workflow

```python
import requests
import json
from typing import List, Dict, Any

class AviationAPIClient:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
    
    def test_oracle_connection(self) -> Dict[str, Any]:
        """Test Oracle database connection"""
        response = requests.get(f"{self.base_url}/api/flights/test-oracle-connection")
        return response.json()
    
    def process_oracle_flights(self) -> Dict[str, Any]:
        """Process flight data from Oracle database"""
        response = requests.post(f"{self.base_url}/api/flights/process-packet")
        return response.json()
    
    def get_plan_ids(self) -> List[int]:
        """Get all available planIds"""
        response = requests.get(f"{self.base_url}/api/flights/plan-ids")
        data = response.json()
        return data.get("planIds", [])
    
    def process_predicted_flights_batch(self, plan_ids: List[int]) -> Dict[str, Any]:
        """Process multiple predicted flights"""
        payload = {"planIds": plan_ids}
        response = requests.post(
            f"{self.base_url}/api/predicted-flights/batch",
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        return response.json()
    
    def run_punctuality_analysis(self) -> Dict[str, Any]:
        """Run complete punctuality analysis"""
        response = requests.get(f"{self.base_url}/api/punctuality-analysis/run")
        return response.json()
    
    def get_all_stats(self) -> Dict[str, Any]:
        """Get comprehensive statistics"""
        flights_stats = requests.get(f"{self.base_url}/api/flights/stats").json()
        predicted_stats = requests.get(f"{self.base_url}/api/predicted-flights/stats").json()
        analysis_stats = requests.get(f"{self.base_url}/api/punctuality-analysis/stats").json()
        
        return {
            "flights": flights_stats,
            "predicted": predicted_stats,
            "analysis": analysis_stats
        }

# Usage example
def main():
    client = AviationAPIClient()
    
    # 1. Test connection
    print("Testing Oracle connection...")
    connection_result = client.test_oracle_connection()
    print(f"Connection: {connection_result['status']}")
    
    # 2. Process Oracle data
    print("Processing Oracle flight data...")
    oracle_result = client.process_oracle_flights()
    print(f"Processed {oracle_result['totalFlightsProcessed']} flights")
    
    # 3. Get planIds and process predictions
    print("Getting planIds...")
    plan_ids = client.get_plan_ids()
    print(f"Found {len(plan_ids)} planIds")
    
    # Process first 10 planIds as example
    if plan_ids:
        sample_ids = plan_ids[:10]
        print(f"Processing predictions for {len(sample_ids)} planIds...")
        prediction_result = client.process_predicted_flights_batch(sample_ids)
        print(f"Processed: {prediction_result['totalProcessed']}/{prediction_result['totalRequested']}")
    
    # 4. Run punctuality analysis
    print("Running punctuality analysis...")
    analysis_result = client.run_punctuality_analysis()
    print(f"Analyzed {analysis_result['totalAnalyzedFlights']} flights")
    
    # 5. Get final statistics
    print("Getting final statistics...")
    stats = client.get_all_stats()
    print(f"Final stats: {json.dumps(stats, indent=2)}")

if __name__ == "__main__":
    main()
```

## 🚨 Error Handling

### Common Error Responses

#### Oracle Connection Failed
```json
{
  "status": "ERROR",
  "message": "Oracle connection failed: Connection timeout",
  "connectionTimeMs": 5000,
  "error": "java.sql.SQLException: Connection timeout"
}
```

#### PlanId Not Found
```json
{
  "totalRequested": 1,
  "totalProcessed": 0,
  "totalNotFound": 1,
  "totalErrors": 0,
  "processedPlanIds": [],
  "notFoundPlanIds": [99999999],
  "errorPlanIds": [],
  "message": "0 out of 1 requested planIds processed. 1 planId not found in Oracle database."
}
```

#### Invalid Request Format
```json
{
  "timestamp": "2024-12-19T10:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request: must provide either 'planId' or 'planIds'",
  "path": "/api/predicted-flights/process"
}
```

## 🔍 Health Checks

### All Health Endpoints
```bash
# Main application health
curl http://localhost:8080/api/flights/health

# Predicted flights service health  
curl http://localhost:8080/api/predicted-flights/health

# Punctuality analysis service health
curl http://localhost:8080/api/punctuality-analysis/health
```

## 📝 Notes

- **Oracle Integration**: All endpoints now use direct Oracle database access
- **Option A Strategy**: Missing planIds are skipped with detailed reporting
- **Performance Metrics**: All responses include timing information
- **Error Resilience**: Comprehensive error handling and recovery
- **Data Validation**: Automatic validation of planIds against Oracle database
- **Batch Processing**: Optimized for processing large numbers of planIds efficiently

## 🔗 Related Documentation

- [README.md](README.md) - Main project documentation
- [SETUP.md](SETUP.md) - Detailed setup instructions
- [Oracle Integration Details](README.md#oracle-database-integration-production) - Oracle-specific configuration
