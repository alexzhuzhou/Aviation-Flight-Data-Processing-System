# Views Documentation

This folder contains the main page components for the Aviation Flight Dashboard. Each view represents a distinct page in the application with specific functionality.

## Views Overview

### `Overview.vue` - Dashboard Home (`/`)
**Main Dashboard Landing Page**

**Purpose**: Provides system overview and quick access to key functions

**Key Features:**
- **System Status Cards**: Real-time health indicators for API, database, processing
- **Quick Actions**: Buttons for common operations (process Oracle data, run analysis)
- **Statistics Overview**: Total flights, processing history, recent activity
- **Recent Activity Timeline**: Latest operations and their status
- **Performance Metrics**: Processing times, success rates, error counts

**API Integrations:**
- `flightAPI.getHealth()` - System health status
- `flightAPI.getStats()` - Flight statistics
- `processingHistoryAPI.getRecent()` - Recent operations

**Components Used:**
- Status indicator cards
- Action buttons with loading states
- Statistics charts
- Activity timeline

### `FlightData.vue` - Flight Data Table (`/flights`)
**Comprehensive Flight Data Management**

**Purpose**: Display, search, and manage flight data in tabular format

**Key Features:**
- **Data Table**: Paginated flight data with sorting capabilities
- **Search Functionality**: Real-time search across multiple fields
- **Filtering Options**: Filter by date, airline, aircraft type, status
- **Flight Details**: Click to open detailed flight information modal
- **Bulk Operations**: Select multiple flights for batch actions
- **Export Functions**: Download data as CSV, JSON, or Excel

**API Integrations:**
- `flightAPI.getAllFlights()` - Fetch flight data with pagination
- `flightAPI.searchFlights()` - Search flights by criteria
- `flightAPI.getFlightDetails()` - Get detailed flight information

**Components Used:**
- `FlightDetailsModal` - Detailed flight information
- Data table with sorting/filtering
- Search input with debouncing
- Pagination controls

### `FlightSearch.vue` - Advanced Search (`/search`)
**Multi-Criteria Flight Search Interface**

**Purpose**: Provide advanced search capabilities with multiple filters

**Key Features:**
- **Multi-Field Search**: planId, indicative, origin, destination
- **Advanced Filters**: Date range, airline, aircraft type, flight status
- **Real-Time Results**: Instant search results as user types
- **Search History**: Save and recall previous searches
- **Result Comparison**: Compare multiple flights side-by-side
- **Export Results**: Download search results

**API Integrations:**
- `searchAPI.searchByPlanId()` - Search by planId
- `searchAPI.searchByIndicative()` - Search by call sign
- `searchAPI.searchByRoute()` - Search by origin/destination
- `searchAPI.getSearchStats()` - Search performance metrics

**Search Types:**
- **Quick Search**: Single field search with autocomplete
- **Advanced Search**: Multiple criteria with boolean operators
- **Saved Searches**: Store frequently used search queries
- **Recent Searches**: Quick access to recent search terms

### `Analysis.vue` - Analytics Dashboard (`/analysis`)
**Flight Data Analysis and Reporting**

**Purpose**: Provide comprehensive analytics and KPI reporting

**Key Features:**
- **Punctuality Analysis**: ICAO KPI14 compliance charts and metrics
- **Trajectory Accuracy**: MSE/RMSE analysis with visualizations
- **Statistical Reports**: Flight performance statistics and trends
- **Interactive Charts**: Chart.js powered analytics with drill-down
- **Comparison Tools**: Compare predicted vs actual flight data
- **Export Reports**: Generate PDF/Excel reports

**Analysis Types:**
1. **Punctuality KPIs**: ±3, ±5, ±15 minute tolerance windows
2. **Trajectory Accuracy**: Horizontal and vertical accuracy metrics
3. **Flight Performance**: On-time performance, delay analysis
4. **Route Analysis**: Popular routes, efficiency metrics

**API Integrations:**
- `analysisAPI.getPunctualityKPIs()` - ICAO KPI14 analysis
- `analysisAPI.getTrajectoryAccuracy()` - Trajectory analysis
- `analysisAPI.getFlightPerformance()` - Performance metrics
- `analysisAPI.generateReport()` - Export analysis reports

**Charts and Visualizations:**
- Bar charts for KPI percentages
- Line charts for trend analysis
- Scatter plots for accuracy metrics
- Heat maps for route performance

### `Trajectory.vue` - Interactive Maps (`/trajectory`)
**Flight Trajectory Visualization**

**Purpose**: Visualize flight paths and trajectories on interactive maps

**Key Features:**
- **Interactive Maps**: Leaflet.js powered flight path visualization
- **Trajectory Overlay**: Display predicted vs actual flight paths
- **Densification Results**: Show trajectory densification improvements
- **Map Controls**: Zoom, pan, layer toggles, measurement tools
- **Flight Animation**: Animate flight progress along trajectory
- **Waypoint Details**: Click waypoints for detailed information

**Map Layers:**
- **Base Maps**: OpenStreetMap, satellite imagery
- **Flight Paths**: Actual vs predicted trajectories
- **Airports**: Major airports with ICAO codes
- **Waypoints**: Navigation points and route elements
- **Weather**: Optional weather overlay integration

**API Integrations:**
- `trajectoryAPI.getFlightPath()` - Flight trajectory data
- `trajectoryAPI.getDensificationResults()` - Densified trajectories
- `trajectoryAPI.getWaypoints()` - Route waypoint information

**Interactive Features:**
- **Click Events**: Get details on flight segments
- **Measurement Tools**: Distance and bearing calculations
- **Layer Control**: Toggle different map layers
- **Export Maps**: Save map views as images

### `SystemHealth.vue` - Health Monitoring (`/health`)
**System Health and Diagnostics**

**Purpose**: Monitor system health and performance metrics

**Key Features:**
- **API Health Status**: Real-time backend API connectivity
- **Database Status**: MongoDB connection and performance
- **Processing Metrics**: Operation success rates and timing
- **Error Tracking**: Recent errors and troubleshooting info
- **Performance Graphs**: System performance over time
- **Diagnostic Tools**: Test connections and system components

**Health Indicators:**
- **API Status**: Response time, availability, error rate
- **Database**: Connection status, query performance, storage usage
- **Processing**: Success rate, average duration, queue status
- **Memory**: Frontend memory usage, performance metrics

**API Integrations:**
- `healthAPI.getSystemHealth()` - Overall system status
- `healthAPI.getAPIStatus()` - Backend API health
- `healthAPI.getDatabaseStatus()` - Database connectivity
- `healthAPI.getProcessingMetrics()` - Processing performance

## View Architecture

### Common Patterns

#### Page Structure
```vue
<template>
  <div class="page-container">
    <!-- Page header -->
    <header class="page-header">
      <h1>Page Title</h1>
      <div class="page-actions">
        <!-- Action buttons -->
      </div>
    </header>
    
    <!-- Main content -->
    <main class="page-content">
      <!-- Page-specific content -->
    </main>
  </div>
</template>
```

#### Data Loading
```javascript
// Composition API pattern
import { ref, onMounted } from 'vue'
import { apiService } from '@/services/api'

export default {
  setup() {
    const loading = ref(false)
    const data = ref([])
    const error = ref(null)
    
    const loadData = async () => {
      loading.value = true
      try {
        const response = await apiService.getData()
        data.value = response.data
      } catch (err) {
        error.value = err.message
      } finally {
        loading.value = false
      }
    }
    
    onMounted(loadData)
    
    return { loading, data, error, loadData }
  }
}
```

#### Error Handling
```javascript
// Global error handling pattern
const handleError = (error) => {
  console.error('View Error:', error)
  // Show user-friendly error message
  // Log error for debugging
  // Provide recovery options
}
```

### Routing Configuration

#### Route Definitions (`src/router/index.js`)
```javascript
const routes = [
  { path: '/', name: 'Overview', component: Overview },
  { path: '/flights', name: 'FlightData', component: FlightData },
  { path: '/search', name: 'FlightSearch', component: FlightSearch },
  { path: '/analysis', name: 'Analysis', component: Analysis },
  { path: '/trajectory', name: 'Trajectory', component: Trajectory },
  { path: '/health', name: 'SystemHealth', component: SystemHealth }
]
```

#### Navigation Guards
```javascript
// Route-level guards for authentication/authorization
beforeEnter: (to, from, next) => {
  // Check permissions
  // Validate data requirements
  // Handle redirects
}
```

### State Management

#### Local State
- Each view manages its own local state
- Use Vue 3 Composition API for reactive data
- Implement loading, error, and success states

#### Shared State
- API service for data fetching
- Router for navigation state
- Local storage for user preferences

### Performance Optimization

#### Lazy Loading
```javascript
// Lazy load views for better initial load time
const Analysis = () => import('./views/Analysis.vue')
```

#### Data Caching
- Cache API responses where appropriate
- Implement refresh strategies
- Use computed properties for derived data

#### Virtual Scrolling
- For large data tables (FlightData.vue)
- Implement pagination for better performance
- Use intersection observer for infinite scroll

## Best Practices

### Code Organization
- **Single responsibility**: Each view has one primary purpose
- **Composition API**: Use for better code organization
- **Reusable logic**: Extract common functionality to composables
- **Type safety**: Use TypeScript or JSDoc for better development experience

### User Experience
- **Loading states**: Show loading indicators during data fetching
- **Error handling**: Provide meaningful error messages and recovery options
- **Responsive design**: Ensure views work on all screen sizes
- **Accessibility**: Follow WCAG guidelines for inclusive design

### Performance
- **Lazy loading**: Load views only when needed
- **Data pagination**: Handle large datasets efficiently
- **Debounced search**: Prevent excessive API calls
- **Memoization**: Cache expensive computations

### Testing
- **Unit tests**: Test view logic and data handling
- **Integration tests**: Test API integrations
- **E2E tests**: Test complete user workflows
- **Accessibility tests**: Ensure views are accessible
