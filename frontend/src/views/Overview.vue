<template>
  <div class="min-h-screen bg-gradient-to-br from-aviation-50 to-blue-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      
      <!-- Welcome Header -->
      <div class="text-center mb-16">
        <div class="flex justify-center mb-8">
          <div class="flex items-center justify-center w-24 h-24 bg-aviation-100 rounded-full shadow-lg">
            <svg class="w-12 h-12 text-aviation-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </div>
        </div>
        <h1 class="text-5xl font-bold text-gray-900 mb-6">
          Aviation Flight Data Platform
        </h1>
        <p class="text-xl text-gray-600 max-w-3xl mx-auto leading-relaxed">
          A comprehensive aviation data management system for processing, analyzing, and visualizing real and predicted flight data. 
          Streamline your flight operations with powerful data processing workflows and detailed analytics.
        </p>
      </div>

      <!-- Key Features Grid -->
      <div class="grid lg:grid-cols-3 md:grid-cols-2 gap-8 mb-16">
        
        <!-- Flight Data Management -->
        <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8 hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
          <div class="flex items-center mb-6">
            <div class="flex items-center justify-center w-12 h-12 bg-blue-100 rounded-lg mr-4">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-900">Flight Data Processing</h3>
          </div>
          <p class="text-gray-600 mb-6">
            Process and store flight data through guided workflows. Extract real flight data from Oracle databases, synchronize predicted flights, and densify trajectories.
          </p>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Oracle database integration
            </li>
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Automated data synchronization
            </li>
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Trajectory densification
            </li>
          </ul>
        </div>

        <!-- Flight Search & Analysis -->
        <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8 hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
          <div class="flex items-center mb-6">
            <div class="flex items-center justify-center w-12 h-12 bg-green-100 rounded-lg mr-4">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-900">Flight Search & Details</h3>
          </div>
          <p class="text-gray-600 mb-6">
            Search and explore flight data with powerful filtering capabilities. View detailed flight information, tracking points, and route comparisons.
          </p>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Multi-criteria search (Plan ID, flight number, airports)
            </li>
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Detailed flight information modal
            </li>
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Real vs predicted flight comparison
            </li>
          </ul>
        </div>

        <!-- Analytics & Visualization -->
        <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8 hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
          <div class="flex items-center mb-6">
            <div class="flex items-center justify-center w-12 h-12 bg-purple-100 rounded-lg mr-4">
              <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-900">Analytics & Insights</h3>
          </div>
          <p class="text-gray-600 mb-6">
            Analyze flight performance with advanced analytics tools. Generate insights on punctuality, trajectory accuracy, and operational efficiency.
          </p>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Punctuality analysis
            </li>
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Trajectory accuracy metrics
            </li>
            <li class="flex items-center">
              <svg class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Interactive visualizations
            </li>
          </ul>
        </div>

      </div>

      <!-- Navigation Cards -->
      <div class="grid lg:grid-cols-2 gap-8 mb-16">
        
        <!-- Quick Start -->
        <div class="bg-gradient-to-r from-aviation-500 to-blue-600 rounded-xl shadow-lg p-8 text-white">
          <h3 class="text-2xl font-bold mb-4">Quick Start Guide</h3>
          <p class="text-aviation-100 mb-6">
            Get started with the platform in three simple steps:
          </p>
          <div class="space-y-4">
            <div class="flex items-center">
              <div class="flex items-center justify-center w-8 h-8 bg-white bg-opacity-20 rounded-full mr-4 text-sm font-bold">1</div>
              <span>Process flight data using the guided workflow</span>
            </div>
            <div class="flex items-center">
              <div class="flex items-center justify-center w-8 h-8 bg-white bg-opacity-20 rounded-full mr-4 text-sm font-bold">2</div>
              <span>Search and explore your flight database</span>
            </div>
            <div class="flex items-center">
              <div class="flex items-center justify-center w-8 h-8 bg-white bg-opacity-20 rounded-full mr-4 text-sm font-bold">3</div>
              <span>Analyze performance with advanced analytics</span>
            </div>
          </div>
          <router-link 
            to="/flights" 
            class="inline-flex items-center mt-6 px-6 py-3 bg-white text-aviation-600 rounded-lg font-semibold hover:bg-aviation-50 transition-colors"
          >
            Start Processing Data
            <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </router-link>
        </div>

        <!-- System Status -->
        <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8">
          <h3 class="text-2xl font-bold text-gray-900 mb-4">Trajectory Analysis</h3>
          <p class="text-gray-600 mb-6">
            Advanced trajectory analysis and visualization capabilities:
          </p>
          <div class="space-y-4">
            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <span class="text-gray-700">Flight Path Visualization</span>
              <span class="flex items-center text-blue-600">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-1.447-.894L15 9m0 8V9m0 0V7" />
                </svg>
                Interactive Maps
              </span>
            </div>
            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <span class="text-gray-700">Route Comparison</span>
              <span class="flex items-center text-purple-600">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                Real vs Predicted
              </span>
            </div>
            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <span class="text-gray-700">Performance Metrics</span>
              <span class="flex items-center text-green-600">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
                Accuracy Analysis
              </span>
            </div>
          </div>
          <router-link 
            to="/trajectory" 
            class="inline-flex items-center mt-6 px-6 py-3 bg-gray-100 text-gray-700 rounded-lg font-semibold hover:bg-gray-200 transition-colors"
          >
            Explore Trajectories
            <svg class="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </router-link>
        </div>

      </div>

      <!-- Feature Highlights -->
      <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8 mb-16">
        <h3 class="text-2xl font-bold text-gray-900 mb-8 text-center">Platform Capabilities</h3>
        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          
          <div class="text-center">
            <div class="flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mx-auto mb-4">
              <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h4 class="text-lg font-semibold text-gray-900 mb-2">Real-time Processing</h4>
            <p class="text-sm text-gray-600">Process flight data in real-time with automated workflows and monitoring</p>
          </div>

          <div class="text-center">
            <div class="flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mx-auto mb-4">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h4 class="text-lg font-semibold text-gray-900 mb-2">Data Validation</h4>
            <p class="text-sm text-gray-600">Comprehensive data validation and quality assurance mechanisms</p>
          </div>

          <div class="text-center">
            <div class="flex items-center justify-center w-16 h-16 bg-purple-100 rounded-full mx-auto mb-4">
              <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" />
              </svg>
            </div>
            <h4 class="text-lg font-semibold text-gray-900 mb-2">Advanced Analytics</h4>
            <p class="text-sm text-gray-600">Powerful analytics tools for flight performance and operational insights</p>
          </div>

          <div class="text-center">
            <div class="flex items-center justify-center w-16 h-16 bg-yellow-100 rounded-full mx-auto mb-4">
              <svg class="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h4 class="text-lg font-semibold text-gray-900 mb-2">Data Export</h4>
            <p class="text-sm text-gray-600">Export flight data and analysis results in multiple formats</p>
          </div>

        </div>
      </div>

      <!-- Navigation Menu -->
      <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8">
        <h3 class="text-2xl font-bold text-gray-900 mb-8 text-center">Explore the Platform</h3>
        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
          
          <router-link 
            to="/flight-data" 
            class="flex items-center p-4 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors group"
          >
            <svg class="w-8 h-8 text-blue-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
            </svg>
            <div>
              <h4 class="font-semibold text-gray-900 group-hover:text-blue-600">Flight Data</h4>
              <p class="text-xs text-gray-600">Process & manage data</p>
            </div>
          </router-link>

          <router-link 
            to="/flight-search" 
            class="flex items-center p-4 bg-green-50 hover:bg-green-100 rounded-lg transition-colors group"
          >
            <svg class="w-8 h-8 text-green-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <div>
              <h4 class="font-semibold text-gray-900 group-hover:text-green-600">Flight Search</h4>
              <p class="text-xs text-gray-600">Search & explore flights</p>
            </div>
          </router-link>

          <router-link 
            to="/analysis" 
            class="flex items-center p-4 bg-purple-50 hover:bg-purple-100 rounded-lg transition-colors group"
          >
            <svg class="w-8 h-8 text-purple-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            <div>
              <h4 class="font-semibold text-gray-900 group-hover:text-purple-600">Analytics</h4>
              <p class="text-xs text-gray-600">Performance insights</p>
            </div>
          </router-link>

          <router-link 
            to="/trajectory" 
            class="flex items-center p-4 bg-orange-50 hover:bg-orange-100 rounded-lg transition-colors group"
          >
            <svg class="w-8 h-8 text-orange-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-1.447-.894L15 9m0 8V9m0 0V7" />
            </svg>
            <div>
              <h4 class="font-semibold text-gray-900 group-hover:text-orange-600">Trajectory Analysis</h4>
              <p class="text-xs text-gray-600">Calculate RMSE metrics</p>
            </div>
          </router-link>

        </div>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'Overview'
}
</script>
