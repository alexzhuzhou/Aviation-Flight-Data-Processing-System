import axios from 'axios'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Request interceptor for logging
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

// Response interceptor for error handling
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

// Flight Data API
export const flightAPI = {
  // Health and Status
  getHealth: () => api.get('/flights/health'),
  getPlanIds: () => api.get('/flights/plan-ids'),
  
  // Oracle Integration
  processOracleData: (date, startTime, endTime) => {
    const params = new URLSearchParams()
    if (date) params.append('date', date)
    if (startTime) params.append('startTime', startTime)
    if (endTime) params.append('endTime', endTime)
    
    const queryString = params.toString()
    const url = queryString ? `/flights/process-packet?${queryString}` : '/flights/process-packet'
    
    return api.post(url, {}, { timeout: 604800000 }) // 1 week timeout (7 days * 24 hours * 60 minutes * 60 seconds * 1000 ms)
  },
  getIntegrationSummary: () => api.get('/flights/integration-summary'),
  
  // Data Analysis
  analyzeDuplicates: () => api.get('/flights/analyze-duplicates'),
  cleanupDuplicates: () => api.post('/flights/cleanup-duplicates'),
}

// Predicted Flights API
export const predictedFlightAPI = {
  // Health and Status
  getHealth: () => api.get('/predicted-flights/health'),
  getStats: () => api.get('/predicted-flights/stats'),
  
  // Processing
  processSingle: (planId) => api.post('/predicted-flights/process', { planId }),
  processBatch: (planIds) => api.post('/predicted-flights/batch', { planIds }),
  autoSync: () => api.post('/predicted-flights/auto-sync', {}, { timeout: 3600000 }), // 1 hour timeout
}

// Punctuality Analysis API
export const punctualityAPI = {
  // Health and Status
  getHealth: () => api.get('/punctuality-analysis/health'),
  getStats: () => api.get('/punctuality-analysis/stats'),
  
  // Analysis
  matchFlights: () => api.get('/punctuality-analysis/match-flights'),
  getAirportCoordinates: () => api.get('/punctuality-analysis/airport-coordinates'),
  getQualifyingFlights: () => api.get('/punctuality-analysis/qualifying-flights'),
  getGeographicValidation: () => api.get('/punctuality-analysis/geographic-validation'),
  calculateKPIs: () => api.get('/punctuality-analysis/punctuality-kpis', { timeout: 300000 }), // 5 minutes timeout
}

// Trajectory Accuracy API
export const trajectoryAccuracyAPI = {
  // Health and Status
  getHealth: () => api.get('/trajectory-accuracy/health'),
  getStats: () => api.get('/trajectory-accuracy/stats'),
  getInfo: () => api.get('/trajectory-accuracy/info'),
  
  // Analysis
  runAnalysis: () => api.get('/trajectory-accuracy/run'),
}

// Trajectory Densification API
export const trajectoryDensificationAPI = {
  // Health and Status
  getHealth: () => api.get('/trajectory-densification/health'),
  getStats: () => api.get('/trajectory-densification/stats'),
  getInfo: () => api.get('/trajectory-densification/info'),
  
  // Processing
  densifySingle: (planId) => api.post(`/trajectory-densification/densify/${planId}`),
  densifyBatch: (planIds) => api.post('/trajectory-densification/densify/batch', { planIds }),
  autoSync: () => api.post('/trajectory-densification/auto-sync', {}, { timeout: 7200000 }), // 2 hours timeout
}

// Flight Search API
export const flightSearchAPI = {
  // Search operations
  searchByPlanId: (query) => api.get(`/flight-search/by-plan-id?query=${encodeURIComponent(query)}`),
  searchByIndicative: (query) => api.get(`/flight-search/by-indicative?query=${encodeURIComponent(query)}`),
  searchByOrigin: (query) => api.get(`/flight-search/by-origin?query=${encodeURIComponent(query)}`),
  searchByDestination: (query) => api.get(`/flight-search/by-destination?query=${encodeURIComponent(query)}`),
  getFlightDetails: (planId) => api.get(`/flight-search/details/${planId}`),
  
  // Delete operations
  deleteRealFlight: (planId, deleteMatching = false) => 
    api.delete(`/flight-search/real/${planId}?deleteMatching=${deleteMatching}`),
  deletePredictedFlight: (instanceId, deleteMatching = false) => 
    api.delete(`/flight-search/predicted/${instanceId}?deleteMatching=${deleteMatching}`),
  bulkDelete: (realFlightIds, predictedFlightIds, deleteMatching = false) => 
    api.delete('/flight-search/bulk', {
      data: { realFlightIds, predictedFlightIds, deleteMatching }
    }),
  
  // Statistics
  getStats: () => api.get('/flight-search/stats'),
}

// Processing History API
export const processingHistoryAPI = {
  // Health and Status
  getHealth: () => api.get('/processing-history/health'),
  
  // History queries
  getRecent: (limit = 20) => api.get(`/processing-history/recent?limit=${limit}`),
  getHistory: (page = 0, size = 20) => api.get(`/processing-history?page=${page}&size=${size}`),
  getToday: () => api.get('/processing-history/today'),
  getByOperation: (operation) => api.get(`/processing-history/by-operation/${operation}`),
  getByDateRange: (startDate, endDate) => api.get('/processing-history/by-date-range', {
    params: { startDate, endDate }
  }),
  
  // Statistics and analysis
  getStatistics: () => api.get('/processing-history/statistics'),
  getAverageDurations: () => api.get('/processing-history/average-durations'),
  getErrors: () => api.get('/processing-history/errors'),
  
  // Maintenance
  cleanup: (daysToKeep = 30) => api.delete(`/processing-history/cleanup?daysToKeep=${daysToKeep}`),
}

// Utility functions
export const apiUtils = {
  // Format error messages
  formatError: (error) => {
    if (error.response?.data?.message) {
      return error.response.data.message
    }
    if (error.response?.data?.error) {
      return error.response.data.error
    }
    if (error.message) {
      return error.message
    }
    return 'An unknown error occurred'
  },
  
  // Check if API is available
  checkHealth: async () => {
    try {
      await flightAPI.getHealth()
      return true
    } catch (error) {
      return false
    }
  },
  
  // Get all system health status
  getSystemHealth: async () => {
    const services = [
      { name: 'Flights', api: flightAPI.getHealth },
      { name: 'Predicted Flights', api: predictedFlightAPI.getHealth },
      { name: 'Punctuality Analysis', api: punctualityAPI.getHealth },
      { name: 'Trajectory Accuracy', api: trajectoryAccuracyAPI.getHealth },
      { name: 'Trajectory Densification', api: trajectoryDensificationAPI.getHealth },
      { name: 'Processing History', api: processingHistoryAPI.getHealth },
    ]
    
    const results = await Promise.allSettled(
      services.map(async (service) => {
        try {
          await service.api()
          return { name: service.name, status: 'healthy', error: null }
        } catch (error) {
          return { 
            name: service.name, 
            status: 'unhealthy', 
            error: apiUtils.formatError(error) 
          }
        }
      })
    )
    
    return results.map(result => result.value)
  }
}

export default api
