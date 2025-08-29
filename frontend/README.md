# Aviation Flight Data Dashboard - Frontend

A modern Vue.js dashboard for visualizing and analyzing aviation flight data with real-time monitoring, interactive maps, and comprehensive analytics.

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Setup](#quick-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Features](#features)
- [Architecture](#architecture)
- [Development](#development)
- [Troubleshooting](#troubleshooting)

## Overview

This Vue.js application provides a comprehensive dashboard for aviation flight data analysis:

- **Real-time flight monitoring** with live data updates
- **Interactive flight maps** using Leaflet.js
- **Punctuality analysis** with ICAO KPI14 compliance charts
- **Trajectory accuracy visualization** with MSE/RMSE metrics
- **Flight search and filtering** capabilities
- **System health monitoring** and performance metrics
- **Responsive design** with Tailwind CSS

## Prerequisites

### Required Software
- **Node.js 16+** (tested with Node.js 18)
- **npm 8+** or **yarn 1.22+**
- **Backend API** running on port 8080

### System Requirements
- **Memory**: Minimum 2GB RAM (4GB recommended)
- **Storage**: At least 500MB free space
- **Browser**: Modern browser with ES6+ support

## Quick Setup

### 1. Install Dependencies
```bash
npm install
```

### 2. Start Development Server
```bash
npm run dev
```

### 3. Verify Setup
Open browser to `http://localhost:5173` and check:
- Dashboard loads successfully
- Backend API connection (green status indicator)
- Flight data displays properly

## Configuration

### Backend API Configuration
The frontend connects to the backend API at `http://localhost:8080/api`

**API Configuration**: `src/services/api.js`
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  }
})
```

### Environment Variables
Create `.env` file for custom configuration:
```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=Aviation Flight Dashboard
```

## Running the Application

### Development Mode
```bash
npm run dev
# Runs on http://localhost:5173
```

### Production Build
```bash
npm run build
npm run preview
# Builds to dist/ folder and serves on http://localhost:3000
```

### Custom Port
```bash
npm run dev -- --port 3001
```

## Features

### Dashboard Pages

#### 1. Overview (`/`)
- **System status** indicators
- **Real-time metrics** (total flights, processing status)
- **Quick actions** (process Oracle data, run analysis)
- **Recent activity** timeline

#### 2. Flight Data (`/flights`)
- **Flight data table** with search and filtering
- **Pagination** and sorting capabilities
- **Flight details modal** with comprehensive information
- **Export functionality**

#### 3. Flight Search (`/search`)
- **Multi-criteria search** (planId, indicative, origin, destination)
- **Advanced filtering** options
- **Real-time search results**
- **Flight comparison** tools

#### 4. Analysis (`/analysis`)
- **Punctuality Analysis** with ICAO KPI14 charts
- **Trajectory Accuracy** visualization
- **Statistical reports** and metrics
- **Interactive charts** using Chart.js

#### 5. Trajectory (`/trajectory`)
- **Interactive flight maps** using Leaflet.js
- **Trajectory visualization** with predicted vs actual routes
- **Densification results** display
- **Map controls** and overlays

#### 6. System Health (`/health`)
- **API health monitoring**
- **Performance metrics**
- **Error tracking**
- **System diagnostics**

### Key Components

#### FlightDetailsModal.vue
- **Comprehensive flight information** display
- **Tracking points** visualization
- **Route elements** and segments
- **Performance metrics**

### API Integration

#### Flight Data API (`src/services/api.js`)
- **Health monitoring**: `getHealth()`, `getStats()`
- **Data processing**: `processOracleData()`, `processPredictedFlights()`
- **Search operations**: `searchFlights()`, `getFlightDetails()`
- **Analysis functions**: `getPunctualityAnalysis()`, `getTrajectoryAccuracy()`

## Architecture

### Project Structure
```
src/
├── components/          # Reusable Vue components
│   └── FlightDetailsModal.vue
├── views/              # Page components (6 pages)
│   ├── Overview.vue    # Dashboard home
│   ├── FlightData.vue  # Flight data table
│   ├── FlightSearch.vue # Search interface
│   ├── Analysis.vue    # Analytics dashboard
│   ├── Trajectory.vue  # Interactive maps
│   └── SystemHealth.vue # Health monitoring
├── services/           # API and utility services
│   └── api.js         # Backend API integration
├── router/            # Vue Router configuration
├── assets/            # Static assets and styles
└── utils/             # Utility functions
```

### Technology Stack
- **Vue.js 3** - Progressive JavaScript framework
- **Vue Router 4** - Client-side routing
- **Vite** - Build tool and development server
- **Tailwind CSS** - Utility-first CSS framework
- **Chart.js** - Interactive charts and graphs
- **Leaflet.js** - Interactive maps
- **Axios** - HTTP client for API calls
- **Headless UI** - Accessible UI components

### Data Flow
```
Backend API → API Service → Vue Components → User Interface
User Actions → Vue Components → API Service → Backend API
```

## Development

### Adding New Features

#### 1. Create New Page
```bash
# Create new view component
touch src/views/NewPage.vue

# Add route in src/router/index.js
{
  path: '/new-page',
  name: 'NewPage',
  component: () => import('../views/NewPage.vue')
}
```

#### 2. Add API Integration
```javascript
// Add to src/services/api.js
export const newAPI = {
  getData: () => api.get('/new-endpoint'),
  postData: (data) => api.post('/new-endpoint', data)
}
```

#### 3. Create Components
```bash
# Create reusable component
touch src/components/NewComponent.vue
```

### Code Style Guidelines
- **Vue 3 Composition API** preferred
- **Tailwind CSS** for styling
- **ESLint** configuration for code quality
- **Responsive design** principles

### Testing
```bash
# Run development server with hot reload
npm run dev

# Build for production testing
npm run build
npm run preview
```

## Troubleshooting

### Application Won't Start

**Node.js Version Issues:**
```bash
# Check Node.js version
node --version

# Use Node Version Manager if needed
nvm use 18
```

**Dependency Issues:**
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Backend Connection Issues

**API Connection Failed:**
```bash
# Check backend is running
curl http://localhost:8080/api/flights/health

# Check network connectivity
netstat -tulpn | grep :8080
```

**CORS Issues:**
- Ensure backend allows frontend origin
- Check browser developer console for CORS errors

### Build Issues

**Vite Build Errors:**
```bash
# Clear Vite cache
rm -rf node_modules/.vite
npm run dev
```

**Memory Issues:**
```bash
# Increase Node.js memory limit
export NODE_OPTIONS="--max-old-space-size=4096"
npm run build
```

### Performance Issues

**Slow Loading:**
- Check network tab in browser developer tools
- Verify backend API response times
- Monitor memory usage in browser

**Map Performance:**
- Reduce number of markers on Leaflet maps
- Implement marker clustering for large datasets

### Common Commands
```bash
# Check versions
node --version
npm --version

# Clear cache
npm cache clean --force

# Analyze bundle size
npm run build
npx vite-bundle-analyzer dist

# Check for updates
npm outdated
```

### Debug Mode
Enable debug logging in browser console:
```javascript
// In browser console
localStorage.setItem('debug', 'true')
```

For detailed component documentation, see the individual folders:
- **Components Documentation**: `src/components/README.md`
- **Views Documentation**: `src/views/README.md`
- **Services Documentation**: `src/services/README.md`
