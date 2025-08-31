<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Trajectory Analysis</h1>
          <p class="mt-1 text-sm text-gray-600">
            Trajectory accuracy analysis using MSE and RMSE metrics for horizontal and vertical deviations
          </p>
          <div v-if="analysisResults && analysisStore.hasTrajectoryAnalysis()" class="mt-2">
            <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              <svg class="w-3 h-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Results loaded from storage
            </span>
          </div>
        </div>
        <div class="flex space-x-3">
          <button 
            @click="runTrajectoryAnalysis" 
            :disabled="loading.analysis"
            class="btn-primary"
          >
            <svg v-if="loading.analysis" class="spinner mr-2" viewBox="0 0 24 24"></svg>
            <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            {{ loading.analysis ? 'Running Analysis...' : 'Run Trajectory Analysis' }}
          </button>
          
          <button 
            v-if="analysisResults"
            @click="clearResults" 
            class="btn-secondary"
          >
            <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            Clear Results
          </button>
          
          <button 
            v-if="analysisResults"
            @click="initCharts" 
            class="btn-secondary"
          >
            <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Regenerate Charts
          </button>
        </div>
      </div>
    </div>

    <!-- Analysis Results Section -->
    <div v-if="analysisResults" class="space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <!-- Total Flights -->
        <div class="card bg-blue-50 border-blue-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-blue-600">{{ analysisResults.totalAnalyzedFlights }}</div>
            <div class="text-sm text-blue-700 mt-1">Flights Analyzed</div>
            <div class="text-xs text-blue-600 mt-1">{{ analysisResults.totalQualifiedFlights }} qualified</div>
          </div>
        </div>

        <!-- Horizontal RMSE -->
        <div class="card bg-green-50 border-green-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-green-600">{{ formatDistance(analysisResults.aggregateMetrics?.horizontalRMSEMeters) }}</div>
            <div class="text-sm text-green-700 mt-1">Horizontal RMSE</div>
            <div class="text-xs text-green-600 mt-1">meters</div>
          </div>
        </div>

        <!-- Vertical RMSE -->
        <div class="card bg-orange-50 border-orange-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-orange-600">{{ formatNumber(analysisResults.aggregateMetrics?.verticalRMSE) }}</div>
            <div class="text-sm text-orange-700 mt-1">Vertical RMSE</div>
            <div class="text-xs text-orange-600 mt-1">meters</div>
          </div>
        </div>

        <!-- Processing Time -->
        <div class="card bg-purple-50 border-purple-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-purple-600">{{ formatDuration(analysisResults.processingTimeMs) }}</div>
            <div class="text-sm text-purple-700 mt-1">Processing Time</div>
            <div class="text-xs text-purple-600 mt-1">{{ formatNumber(analysisResults.aggregateMetrics?.totalPointsAnalyzed) }} points</div>
          </div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Horizontal RMSE Distribution -->
        <div class="card">
          <div class="card-header">
            <h3 class="text-lg font-medium text-gray-900">Horizontal RMSE Distribution</h3>
            <span class="status-badge status-success">{{ analysisResults.flightResults?.length || 0 }} flights</span>
          </div>
          <div class="chart-container">
            <canvas ref="horizontalChart"></canvas>
          </div>
        </div>

        <!-- Vertical RMSE Distribution -->
        <div class="card">
          <div class="card-header">
            <h3 class="text-lg font-medium text-gray-900">Vertical RMSE Distribution</h3>
            <span class="status-badge status-info">meters</span>
          </div>
          <div class="chart-container">
            <canvas ref="verticalChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Accuracy Metrics Comparison -->
      <div class="card">
        <div class="card-header">
          <h3 class="text-lg font-medium text-gray-900">Accuracy Metrics Overview</h3>
          <span class="status-badge status-info">Aggregate Statistics</span>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6 p-6">
          <div class="text-center">
            <div class="text-2xl font-bold text-gray-900">{{ formatDistance(analysisResults.aggregateMetrics?.minHorizontalRMSEMeters) }}</div>
            <div class="text-sm text-gray-600">Min Horizontal RMSE (m)</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-gray-900">{{ formatDistance(analysisResults.aggregateMetrics?.maxHorizontalRMSEMeters) }}</div>
            <div class="text-sm text-gray-600">Max Horizontal RMSE (m)</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-gray-900">{{ formatNumber(analysisResults.aggregateMetrics?.minVerticalRMSE) }}</div>
            <div class="text-sm text-gray-600">Min Vertical RMSE (m)</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-gray-900">{{ formatNumber(analysisResults.aggregateMetrics?.maxVerticalRMSE) }}</div>
            <div class="text-sm text-gray-600">Max Vertical RMSE (m)</div>
          </div>
        </div>
      </div>

      <!-- Detailed Results Table -->
      <div class="card">
        <div class="card-header">
          <h3 class="text-lg font-medium text-gray-900">Flight Trajectory Analysis Results</h3>
          <div class="flex items-center space-x-3">
            <span class="status-badge status-info">
              {{ showAllResults ? (analysisResults.flightResults?.length || 0) : Math.min(10, analysisResults.flightResults?.length || 0) }} 
              of {{ analysisResults.flightResults?.length || 0 }} flights
            </span>
            <button 
              v-if="(analysisResults.flightResults?.length || 0) > 10"
              @click="toggleShowAllResults" 
              class="btn-secondary text-sm"
            >
              {{ showAllResults ? 'Show Less' : 'Show More' }}
            </button>
          </div>
        </div>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Plan ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Flight</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Points</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Horizontal RMSE</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vertical RMSE</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Max H Error</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Max V Error</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Accuracy</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="flight in displayedFlights" :key="flight.planId">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-600">
                  {{ flight.planId || 'N/A' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ flight.predictedIndicative }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatNumber(flight.pointCount) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDistance(flight.horizontalRMSEMeters) }}m
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatNumber(flight.verticalRMSE) }}m
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDistance(flight.maxHorizontalErrorMeters) }}m
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatNumber(flight.maxVerticalError) }}m
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="status-badge" :class="getAccuracyStatus(flight.horizontalRMSEMeters, flight.verticalRMSE)">
                    {{ getAccuracyLabel(flight.horizontalRMSEMeters, flight.verticalRMSE) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- Pagination for large datasets -->
        <div v-if="showAllResults && (analysisResults.flightResults?.length || 0) > 50" class="px-6 py-3 bg-gray-50 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-700">
              Showing {{ ((currentPage - 1) * pageSize) + 1 }} to {{ Math.min(currentPage * pageSize, analysisResults.flightResults?.length || 0) }} 
              of {{ analysisResults.flightResults?.length || 0 }} results
            </div>
            <div class="flex space-x-2">
              <button 
                @click="currentPage--" 
                :disabled="currentPage <= 1"
                class="btn-secondary text-sm"
              >
                Previous
              </button>
              <span class="px-3 py-1 text-sm text-gray-700">
                Page {{ currentPage }} of {{ totalPages }}
              </span>
              <button 
                @click="currentPage++" 
                :disabled="currentPage >= totalPages"
                class="btn-secondary text-sm"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- No Results State -->
    <div v-else class="card">
      <div class="text-center py-12">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No Analysis Results</h3>
        <p class="mt-1 text-sm text-gray-500">Run the trajectory analysis to see accuracy metrics and flight performance data.</p>
        <div class="mt-6">
          <button @click="runTrajectoryAnalysis" :disabled="loading.analysis" class="btn-primary">
            <svg v-if="loading.analysis" class="spinner mr-2" viewBox="0 0 24 24"></svg>
            <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            {{ loading.analysis ? 'Running Analysis (this may take several minutes)...' : 'Start Trajectory Analysis' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading Overlay for Analysis -->
    <div v-if="loading.analysis" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-8 max-w-md mx-4">
        <div class="text-center">
          <div class="spinner w-12 h-12 mx-auto mb-4"></div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Running Trajectory Analysis</h3>
          <p class="text-sm text-gray-600 mb-4">
            This analysis compares predicted flight trajectories with actual tracking points using MSE and RMSE metrics. 
            Please wait, this may take several minutes to complete.
          </p>
          <div class="text-xs text-gray-500">
            Analyzing trajectory accuracy for horizontal and vertical deviations...
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, inject, nextTick, computed, onMounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import { trajectoryAccuracyAPI, apiUtils } from '../services/api'
import analysisStore from '../utils/analysisStore.js'

Chart.register(...registerables)

export default {
  name: 'Trajectory',
  setup() {
    const showToast = inject('showToast')
    const setGlobalLoading = inject('setGlobalLoading')
    
    const loading = ref({
      analysis: false
    })
    
    const analysisResults = ref(null)
    const horizontalChart = ref(null)
    const verticalChart = ref(null)
    let horizontalChartInstance = null
    let verticalChartInstance = null

    // Load existing analysis results on component mount
    const loadExistingResults = () => {
      const existingResults = analysisStore.getTrajectoryAnalysis()
      if (existingResults) {
        analysisResults.value = existingResults
        showToast('Loaded previous trajectory analysis results', 'info')
        
        // Regenerate charts after loading data with proper timing
        nextTick(() => {
          setTimeout(() => {
            initCharts()
          }, 100) // Small delay to ensure DOM is ready
        })
      }
    }

    // Table display controls
    const showAllResults = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(50)

    // Computed properties for table display
    const displayedFlights = computed(() => {
      if (!analysisResults.value?.flightResults) return []
      
      if (showAllResults.value) {
        const allFlights = analysisResults.value.flightResults
        const startIndex = (currentPage.value - 1) * pageSize.value
        const endIndex = startIndex + pageSize.value
        return allFlights.slice(startIndex, endIndex)
      } else {
        return analysisResults.value.flightResults.slice(0, 10)
      }
    })

    const totalPages = computed(() => {
      if (!analysisResults.value?.flightResults) return 1
      return Math.ceil(analysisResults.value.flightResults.length / pageSize.value)
    })

    // Table control functions
    const toggleShowAllResults = () => {
      showAllResults.value = !showAllResults.value
      currentPage.value = 1 // Reset to first page when toggling
    }

    // Utility functions
    const formatNumber = (num) => {
      if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
      if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
      return num?.toLocaleString() || '0'
    }

    const formatRMSE = (value) => {
      if (!value) return 'N/A'
      return value.toFixed(6)
    }

    const formatDistance = (meters) => {
      if (!meters) return 'N/A'
      if (meters >= 1000) {
        return (meters / 1000).toFixed(1) + 'k'
      }
      return Math.round(meters).toString()
    }

    const formatDuration = (ms) => {
      if (!ms) return 'N/A'
      const seconds = Math.round(ms / 1000)
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return minutes > 0 ? `${minutes}m ${remainingSeconds}s` : `${remainingSeconds}s`
    }

    const getAccuracyStatus = (horizontalRMSEMeters, verticalRMSE) => {
      // Define accuracy thresholds in meters
      const horizontalThreshold = 50000 // 50 km in meters (equivalent to ~0.008 radians)
      const verticalThreshold = 15000 // meters
      
      if (horizontalRMSEMeters <= horizontalThreshold && verticalRMSE <= verticalThreshold) return 'status-success'
      if (horizontalRMSEMeters <= horizontalThreshold * 1.5 && verticalRMSE <= verticalThreshold * 1.2) return 'status-warning'
      return 'status-error'
    }

    const getAccuracyLabel = (horizontalRMSEMeters, verticalRMSE) => {
      const horizontalThreshold = 50000 // 50 km in meters
      const verticalThreshold = 15000
      
      if (horizontalRMSEMeters <= horizontalThreshold && verticalRMSE <= verticalThreshold) return 'Excellent'
      if (horizontalRMSEMeters <= horizontalThreshold * 1.5 && verticalRMSE <= verticalThreshold * 1.2) return 'Good'
      return 'Poor'
    }

    // Analysis function
    const runTrajectoryAnalysis = async () => {
      loading.value.analysis = true
      try {
        showToast('Starting trajectory analysis - this may take several minutes...', 'info')
        
        const response = await trajectoryAccuracyAPI.runAnalysis()
        analysisResults.value = response.data
        
        // Save results to persistent store
        analysisStore.setTrajectoryAnalysis(response.data)
        
        showToast(`Analysis completed! ${response.data.totalAnalyzedFlights} flights analyzed`, 'success')
        
        // Initialize charts after getting results
        await nextTick()
        initCharts()
        
      } catch (error) {
        showToast(`Analysis failed: ${apiUtils.formatError(error)}`, 'error')
        console.error('Trajectory Analysis error:', error)
      } finally {
        loading.value.analysis = false
      }
    }

    // Clear results function
    const clearResults = () => {
      analysisResults.value = null
      analysisStore.clearTrajectoryAnalysis()
      
      // Destroy existing charts
      if (horizontalChartInstance) {
        horizontalChartInstance.destroy()
        horizontalChartInstance = null
      }
      if (verticalChartInstance) {
        verticalChartInstance.destroy()
        verticalChartInstance = null
      }
      
      showToast('Analysis results cleared', 'info')
    }

    // Chart initialization
    const initCharts = async () => {
      try {
        if (!analysisResults.value?.flightResults) {
          console.log('No flight results available for charts')
          return
        }
        
        console.log('Initializing charts with', analysisResults.value.flightResults.length, 'flights')
        
        await nextTick()
        
        // Destroy existing charts
        if (horizontalChartInstance) {
          horizontalChartInstance.destroy()
        }
        if (verticalChartInstance) {
          verticalChartInstance.destroy()
        }
        
        // Check if chart elements are available
        if (!horizontalChart.value || !verticalChart.value) {
          console.warn('Chart canvas elements not available yet')
          return
        }
        
        // Horizontal RMSE Histogram
        if (horizontalChart.value) {
          const horizontalRMSEs = analysisResults.value.flightResults.map(flight => flight.horizontalRMSEMeters)
          const horizontalHistogramData = createHistogramData(horizontalRMSEs, 'horizontal')
          
          horizontalChartInstance = new Chart(horizontalChart.value, {
          type: 'bar',
          data: {
            labels: horizontalHistogramData.labels,
            datasets: [{
              label: 'Number of Flights',
              data: horizontalHistogramData.data,
              backgroundColor: 'rgba(34, 197, 94, 0.6)',
              borderColor: 'rgb(34, 197, 94)',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              }
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text: 'Horizontal RMSE (meters)'
                }
              },
              y: {
                title: {
                  display: true,
                  text: 'Number of Flights'
                },
                beginAtZero: true
              }
            }
          }
        })
      }

      // Vertical RMSE Histogram
      if (verticalChart.value) {
        const verticalRMSEs = analysisResults.value.flightResults.map(flight => flight.verticalRMSE)
        const verticalHistogramData = createHistogramData(verticalRMSEs, 'vertical')
        
        verticalChartInstance = new Chart(verticalChart.value, {
          type: 'bar',
          data: {
            labels: verticalHistogramData.labels,
            datasets: [{
              label: 'Number of Flights',
              data: verticalHistogramData.data,
              backgroundColor: 'rgba(251, 146, 60, 0.6)',
              borderColor: 'rgb(251, 146, 60)',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              }
            },
            scales: {
              x: {
                title: {
                  display: true,
                  text: 'Vertical RMSE (meters)'
                }
              },
              y: {
                title: {
                  display: true,
                  text: 'Number of Flights'
                },
                beginAtZero: true
              }
            }
          }
        })
      }
      
      console.log('Charts initialized successfully')
    } catch (error) {
      console.error('Error initializing charts:', error)
      showToast('Error initializing charts. Please try refreshing the page.', 'error')
    }
    }

    const createHistogramData = (values, type) => {
      let bins, labels = [], data = []
      
      if (type === 'horizontal') {
        // Horizontal RMSE bins (meters) - converted from previous degree thresholds
        bins = [0, 30000, 40000, 50000, 60000, 70000, 80000] // in meters (roughly equivalent to previous degree bins)
        for (let i = 0; i < bins.length - 1; i++) {
          const min = bins[i]
          const max = bins[i + 1]
          labels.push(`${min/1000}k-${max/1000}k`)
          const count = values.filter(val => val >= min && val < max).length
          data.push(count)
        }
        // Add final bin for values >= last bin
        labels.push(`${bins[bins.length - 1]/1000}k+`)
        const countFinal = values.filter(val => val >= bins[bins.length - 1]).length
        data.push(countFinal)
      } else {
        // Vertical RMSE bins (meters)
        bins = [0, 12000, 13000, 14000, 15000, 16000, 17000, 18000]
        for (let i = 0; i < bins.length - 1; i++) {
          const min = bins[i]
          const max = bins[i + 1]
          labels.push(`${min/1000}k-${max/1000}k`)
          const count = values.filter(val => val >= min && val < max).length
          data.push(count)
        }
        // Add final bin for values >= last bin
        labels.push(`${bins[bins.length - 1]/1000}k+`)
        const countFinal = values.filter(val => val >= bins[bins.length - 1]).length
        data.push(countFinal)
      }
      
      return { labels, data }
    }

    // Load existing results on component mount
    onMounted(() => {
      loadExistingResults()
    })

    return {
      loading,
      analysisResults,
      horizontalChart,
      verticalChart,
      showAllResults,
      currentPage,
      displayedFlights,
      totalPages,
      toggleShowAllResults,
      formatNumber,
      formatRMSE,
      formatDistance,
      formatDuration,
      getAccuracyStatus,
      getAccuracyLabel,
      runTrajectoryAnalysis,
      clearResults,
      initCharts,
      analysisStore
    }
  }
}
</script>
