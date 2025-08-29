# Services Documentation

This folder contains API integration services and utility functions for the Aviation Flight Dashboard frontend.

## Services Overview

### `api.js` - Backend API Integration
**Comprehensive API Service Layer**

**Purpose**: Centralized API communication with the backend Spring Boot application

**Configuration:**
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  }
})
```

## API Service Categories

### Flight Data API (`flightAPI`)
**Core flight data operations**

#### Health and Status
- `getHealth()` - Check API health status
- `getStats()` - Get flight processing statistics
- `getPlanIds()` - Get all available planIds

#### Oracle Integration
- `processOracleData(date, startTime, endTime)` - Process flight data from Oracle
- `testOracleConnection()` - Test Oracle database connectivity

#### Flight Operations
- `getAllFlights(page, size)` - Get paginated flight data
- `getFlightDetails(planId)` - Get detailed flight information
- `searchFlights(query)` - Search flights by various criteria

**Usage Example:**
```javascript
import { flightAPI } from '@/services/api'

// Get flight statistics
const stats = await flightAPI.getStats()

// Process Oracle data
const result = await flightAPI.processOracleData('2025-07-11')

// Search flights
const flights = await flightAPI.searchFlights('TAM3886')
```

### Predicted Flight API (`predictedFlightAPI`)
**Predicted flight data operations**

#### Processing Operations
- `processSingle(planId)` - Process single predicted flight
- `processBatch(planIds)` - Batch process multiple predictions
- `autoSync()` - Auto-sync predicted flights

#### Data Retrieval
- `getStats()` - Get predicted flight statistics
- `getHealth()` - Check predicted flight service health

**Usage Example:**
```javascript
import { predictedFlightAPI } from '@/services/api'

// Process single prediction
const result = await predictedFlightAPI.processSingle(17879345)

// Batch process predictions
const batchResult = await predictedFlightAPI.processBatch([17879345, 17879346])
```

### Analysis API (`analysisAPI`)
**Flight analysis and KPI operations**

#### Punctuality Analysis
- `getPunctualityKPIs()` - Get ICAO KPI14 punctuality analysis
- `matchFlights()` - Match predicted flights with real flights
- `getStats()` - Get punctuality analysis statistics

#### Trajectory Analysis
- `getTrajectoryAccuracy()` - Get MSE/RMSE trajectory accuracy analysis
- `getAccuracyStats()` - Get trajectory accuracy statistics

#### Densification
- `autoSync()` - Auto-sync trajectory densification
- `getDensificationResults(planId)` - Get densification results for specific flight

**Usage Example:**
```javascript
import { analysisAPI } from '@/services/api'

// Get punctuality KPIs
const kpis = await analysisAPI.getPunctualityKPIs()

// Get trajectory accuracy
const accuracy = await analysisAPI.getTrajectoryAccuracy()
```

### Search API (`searchAPI`)
**Advanced search operations**

#### Search Functions
- `searchByPlanId(query)` - Search flights by planId
- `searchByIndicative(query)` - Search flights by call sign
- `searchByOrigin(query)` - Search flights by origin airport
- `searchByDestination(query)` - Search flights by destination airport

#### Management Operations
- `deleteRealFlight(planId)` - Delete real flight data
- `deletePredictedFlight(instanceId)` - Delete predicted flight data
- `bulkDelete(planIds, options)` - Bulk delete operations
- `getStats()` - Get search performance statistics

**Usage Example:**
```javascript
import { searchAPI } from '@/services/api'

// Search by planId
const flights = await searchAPI.searchByPlanId('17879')

// Search by call sign
const results = await searchAPI.searchByIndicative('TAM3886')
```

### Processing History API (`processingHistoryAPI`)
**Operation tracking and monitoring**

#### History Operations
- `getRecent(limit)` - Get recent processing history
- `getAll(page, size)` - Get paginated processing history
- `getByOperation(operation)` - Get history by operation type
- `getByStatus(status)` - Get history by status

#### Statistics
- `getStatistics()` - Get processing statistics
- `getHealth()` - Check processing history service health

**Usage Example:**
```javascript
import { processingHistoryAPI } from '@/services/api'

// Get recent operations
const recent = await processingHistoryAPI.getRecent(10)

// Get statistics
const stats = await processingHistoryAPI.getStatistics()
```

## API Response Patterns

### Standard Response Format
```javascript
// Success Response
{
  status: 'SUCCESS',
  data: { /* response data */ },
  message: 'Operation completed successfully',
  timestamp: '2024-12-19T10:30:45Z'
}

// Error Response
{
  status: 'ERROR',
  error: 'Error message',
  details: { /* error details */ },
  timestamp: '2024-12-19T10:30:45Z'
}
```

### Processing Results
```javascript
// Oracle Processing Result
{
  totalFlightsExtracted: 1243,
  totalFlightsProcessed: 1200,
  totalTrackingPoints: 45678,
  extractionTimeMs: 2345,
  processingTimeMs: 5678,
  message: 'Successfully processed flights'
}

// Batch Processing Result
{
  totalRequested: 3,
  totalProcessed: 2,
  totalNotFound: 1,
  totalErrors: 0,
  processedPlanIds: [17879345, 17879346],
  notFoundPlanIds: [17879347],
  errorPlanIds: []
}
```

## Error Handling

### Request Interceptor
```javascript
api.interceptors.request.use(
  (config) => {
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  (error) => {
    console.error('API Request Error:', error)
    return Promise.reject(error)
  }
)
```

### Response Interceptor
```javascript
api.interceptors.response.use(
  (response) => {
    console.log(`API Response: ${response.status} ${response.config.url}`)
    return response
  },
  (error) => {
    console.error('API Response Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)
```

### Error Types
- **Network Errors**: Connection timeout, server unavailable
- **HTTP Errors**: 4xx client errors, 5xx server errors
- **Validation Errors**: Invalid request data
- **Business Logic Errors**: Application-specific errors

## Performance Optimization

### Request Caching
```javascript
// Simple cache implementation
const cache = new Map()

const getCachedData = async (key, fetchFn, ttl = 300000) => {
  const cached = cache.get(key)
  if (cached && Date.now() - cached.timestamp < ttl) {
    return cached.data
  }
  
  const data = await fetchFn()
  cache.set(key, { data, timestamp: Date.now() })
  return data
}
```

### Request Debouncing
```javascript
// Debounce search requests
import { debounce } from 'lodash-es'

const debouncedSearch = debounce(async (query) => {
  return await searchAPI.searchFlights(query)
}, 300)
```

### Batch Requests
```javascript
// Batch multiple requests
const batchRequests = async (requests) => {
  try {
    const results = await Promise.allSettled(requests)
    return results.map(result => 
      result.status === 'fulfilled' ? result.value : null
    )
  } catch (error) {
    console.error('Batch request error:', error)
    throw error
  }
}
```

## Configuration Management

### Environment Variables
```javascript
// Use Vite environment variables
const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: import.meta.env.VITE_API_TIMEOUT || 30000,
  retryAttempts: import.meta.env.VITE_API_RETRY_ATTEMPTS || 3
}
```

### API Versioning
```javascript
// Support for API versioning
const createVersionedAPI = (version = 'v1') => {
  return axios.create({
    baseURL: `http://localhost:8080/api/${version}`,
    timeout: 30000
  })
}
```

## Testing API Services

### Unit Testing
```javascript
// Mock API responses for testing
import { vi } from 'vitest'
import { flightAPI } from '@/services/api'

vi.mock('@/services/api', () => ({
  flightAPI: {
    getStats: vi.fn().mockResolvedValue({
      totalFlights: 1200,
      totalTrackingPoints: 45000
    })
  }
}))
```

### Integration Testing
```javascript
// Test actual API endpoints
describe('Flight API Integration', () => {
  test('should fetch flight statistics', async () => {
    const stats = await flightAPI.getStats()
    expect(stats).toHaveProperty('totalFlights')
    expect(typeof stats.totalFlights).toBe('number')
  })
})
```

## Best Practices

### API Design
- **Consistent naming**: Use clear, descriptive function names
- **Error handling**: Always handle and propagate errors appropriately
- **Type safety**: Use TypeScript or JSDoc for better development experience
- **Documentation**: Document all API functions with examples

### Performance
- **Caching**: Cache frequently requested data
- **Debouncing**: Prevent excessive API calls from user input
- **Batch requests**: Combine multiple requests when possible
- **Lazy loading**: Load data only when needed

### Security
- **Input validation**: Validate all user inputs before sending to API
- **Error messages**: Don't expose sensitive information in error messages
- **Authentication**: Handle authentication tokens securely
- **HTTPS**: Always use HTTPS in production

### Monitoring
- **Logging**: Log all API requests and responses for debugging
- **Metrics**: Track API performance and error rates
- **Health checks**: Implement health check endpoints
- **Alerting**: Set up alerts for API failures or performance issues
