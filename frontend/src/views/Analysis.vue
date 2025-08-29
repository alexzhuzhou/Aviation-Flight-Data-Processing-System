<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Flight Analysis</h1>
          <p class="mt-1 text-sm text-gray-600">
            Punctuality analysis and flight performance metrics (ICAO KPI14)
          </p>
          <div v-if="kpiResults && analysisStore.hasPunctualityAnalysis()" class="mt-2">
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
            @click="runKPIAnalysis" 
            :disabled="loading.kpis"
            class="btn-primary"
          >
            <svg v-if="loading.kpis" class="spinner mr-2" viewBox="0 0 24 24"></svg>
            <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            {{ loading.kpis ? 'Running Analysis...' : 'Run KPI Analysis' }}
          </button>
          
          <button 
            v-if="kpiResults"
            @click="clearResults" 
            class="btn-secondary"
          >
            <svg class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            Clear Results
          </button>
          
          <button 
            v-if="kpiResults"
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


    <!-- KPI Results Section -->
    <div v-if="kpiResults" class="space-y-6">
      <!-- KPI Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Within 3 Minutes -->
        <div class="card bg-green-50 border-green-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-green-600">{{ kpiResults.within3MinPercentage }}</div>
            <div class="text-sm text-green-700 mt-1">Within ±3 Minutes</div>
            <div class="text-xs text-green-600 mt-1">{{ kpiResults.within3MinCount }} flights</div>
          </div>
        </div>

        <!-- Within 5 Minutes -->
        <div class="card bg-yellow-50 border-yellow-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-yellow-600">{{ kpiResults.within5MinPercentage }}</div>
            <div class="text-sm text-yellow-700 mt-1">Within ±5 Minutes</div>
            <div class="text-xs text-yellow-600 mt-1">{{ kpiResults.within5MinCount }} flights</div>
          </div>
        </div>

        <!-- Within 15 Minutes -->
        <div class="card bg-blue-50 border-blue-200">
          <div class="text-center">
            <div class="text-3xl font-bold text-blue-600">{{ kpiResults.within15MinPercentage }}</div>
            <div class="text-sm text-blue-700 mt-1">Within ±15 Minutes</div>
            <div class="text-xs text-blue-600 mt-1">{{ kpiResults.within15MinCount }} flights</div>
          </div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Punctuality Distribution Chart -->
        <div class="card">
          <div class="card-header">
            <h3 class="text-lg font-medium text-gray-900">Punctuality Distribution</h3>
            <span class="status-badge status-success">{{ kpiResults.totalAnalyzed }} flights</span>
          </div>
          <div class="chart-container">
            <canvas ref="punctualityChart"></canvas>
          </div>
        </div>

        <!-- Time Difference Histogram -->
        <div class="card">
          <div class="card-header">
            <h3 class="text-lg font-medium text-gray-900">Punctuality Deviation Distribution</h3>
            <span class="status-badge status-info">Sample of {{ Math.min(1000, kpiResults.detailedResults?.length || 0) }} flights</span>
          </div>
          <div class="chart-container">
            <canvas ref="histogramChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Detailed Results Table -->
      <div class="card">
        <div class="card-header">
          <h3 class="text-lg font-medium text-gray-900">Flight Analysis Results</h3>
          <div class="flex items-center space-x-3">
            <span class="status-badge status-info">
              {{ showAllResults ? (kpiResults.detailedResults?.length || 0) : Math.min(10, kpiResults.sampleDetailedResults?.length || 0) }} 
              of {{ kpiResults.detailedResults?.length || 0 }} flights
            </span>
            <button 
              v-if="(kpiResults.detailedResults?.length || 0) > 10"
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
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Departure Time</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Predicted Duration</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actual Duration</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time Difference</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Punctuality</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="flight in displayedFlights" :key="flight.planId || flight.flightIndicative">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-600">
                  {{ flight.planId || 'N/A' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ flight.flightIndicative }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDateTime(flight.actualDepartureTime) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDuration(flight.predictedDurationMs) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDuration(flight.actualDurationMs) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium" :class="getDifferenceColor(flight.timeDifferenceMinutes)">
                  {{ formatDifference(flight.timeDifferenceMinutes) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center space-x-2">
                    <span class="status-badge" :class="getPunctualityStatus(flight.timeDifferenceMinutes)">
                      {{ getPunctualityLabel(flight.timeDifferenceMinutes) }}
                    </span>
                    <div class="flex space-x-1">
                      <span v-if="flight.within3Min" class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">±3</span>
                      <span v-if="flight.within5Min" class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">±5</span>
                      <span v-if="flight.within15Min" class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">±15</span>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- Pagination for large datasets -->
        <div v-if="showAllResults && (kpiResults.detailedResults?.length || 0) > 50" class="px-6 py-3 bg-gray-50 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-700">
              Showing {{ ((currentPage - 1) * pageSize) + 1 }} to {{ Math.min(currentPage * pageSize, kpiResults.detailedResults?.length || 0) }} 
              of {{ kpiResults.detailedResults?.length || 0 }} results
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
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No Analysis Results</h3>
        <p class="mt-1 text-sm text-gray-500">Run the KPI analysis to see punctuality metrics and flight performance data.</p>
        <div class="mt-6">
          <button @click="runKPIAnalysis" :disabled="loading.kpis" class="btn-primary">
            <svg v-if="loading.kpis" class="spinner mr-2" viewBox="0 0 24 24"></svg>
            <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            {{ loading.kpis ? 'Running Analysis (this may take several minutes)...' : 'Start KPI Analysis' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading Overlay for KPI Analysis -->
    <div v-if="loading.kpis" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-8 max-w-md mx-4">
        <div class="text-center">
          <div class="spinner w-12 h-12 mx-auto mb-4"></div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Running Punctuality Analysis</h3>
          <p class="text-sm text-gray-600 mb-4">
            This analysis compares predicted flight times with actual execution times. 
            Please wait, this may take several minutes to complete.
          </p>
          <div class="text-xs text-gray-500">
            Analyzing flight data and calculating ICAO KPI14 metrics...
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, inject, nextTick, computed, onMounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import { punctualityAPI, apiUtils } from '../services/api'
import analysisStore from '../utils/analysisStore.js'

Chart.register(...registerables)

export default {
  name: 'Analysis',
  setup() {
    const showToast = inject('showToast')
    const setGlobalLoading = inject('setGlobalLoading')
    
    const loading = ref({
      kpis: false
    })
    
    const kpiResults = ref(null)
    const punctualityChart = ref(null)
    const histogramChart = ref(null)
    let punctualityChartInstance = null
    let histogramChartInstance = null

    // Load existing analysis results on component mount
    const loadExistingResults = () => {
      const existingResults = analysisStore.getPunctualityAnalysis()
      if (existingResults) {
        kpiResults.value = existingResults
        showToast('Loaded previous punctuality analysis results', 'info')
        
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
      if (!kpiResults.value) return []
      
      if (showAllResults.value) {
        const allFlights = kpiResults.value.detailedResults || []
        const startIndex = (currentPage.value - 1) * pageSize.value
        const endIndex = startIndex + pageSize.value
        return allFlights.slice(startIndex, endIndex)
      } else {
        return (kpiResults.value.sampleDetailedResults || []).slice(0, 10)
      }
    })

    const totalPages = computed(() => {
      if (!kpiResults.value?.detailedResults) return 1
      return Math.ceil(kpiResults.value.detailedResults.length / pageSize.value)
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

    const formatMinutes = (minutes) => {
      if (!minutes) return 'N/A'
      const hours = Math.floor(minutes / 60)
      const mins = Math.round(minutes % 60)
      return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
    }

    const formatDuration = (durationMs) => {
      if (!durationMs) return 'N/A'
      const minutes = Math.round(durationMs / 60000)
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
    }

    const formatDateTime = (timestamp) => {
      if (!timestamp) return 'N/A'
      const date = new Date(timestamp)
      return date.toLocaleString('en-US', {
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        timeZoneName: 'short'
      })
    }

    const formatDifference = (diff) => {
      if (diff === null || diff === undefined) return 'N/A'
      const sign = diff >= 0 ? '+' : ''
      return `${sign}${diff.toFixed(1)}min`
    }

    const getDifferenceColor = (diff) => {
      if (Math.abs(diff) <= 3) return 'text-green-600'
      if (Math.abs(diff) <= 5) return 'text-yellow-600'
      if (Math.abs(diff) <= 15) return 'text-orange-600'
      return 'text-red-600'
    }

    const getPunctualityStatus = (diff) => {
      if (Math.abs(diff) <= 3) return 'status-success'
      if (Math.abs(diff) <= 5) return 'status-warning'
      if (Math.abs(diff) <= 15) return 'status-info'
      return 'status-error'
    }

    const getPunctualityLabel = (diff) => {
      if (Math.abs(diff) <= 3) return 'Excellent'
      if (Math.abs(diff) <= 5) return 'Good'
      if (Math.abs(diff) <= 15) return 'Acceptable'
      return 'Poor'
    }

    // KPI Analysis function

    const runKPIAnalysis = async () => {
      loading.value.kpis = true
      try {
        showToast('Starting punctuality analysis - this may take several minutes...', 'info')
        
        const response = await punctualityAPI.calculateKPIs()
        kpiResults.value = response.data
        
        // Save results to persistent store
        analysisStore.setPunctualityAnalysis(response.data)
        
        showToast(`Analysis completed! ${response.data.totalAnalyzed} flights analyzed`, 'success')
        
        // Initialize charts after getting results
        await nextTick()
        initCharts()
        
      } catch (error) {
        showToast(`Analysis failed: ${apiUtils.formatError(error)}`, 'error')
        console.error('KPI Analysis error:', error)
      } finally {
        loading.value.kpis = false
      }
    }

    // Chart initialization
    const initCharts = async () => {
      try {
        if (!kpiResults.value) {
          console.log('No KPI results available for charts')
          return
        }
        
        console.log('Initializing punctuality charts')
        
        await nextTick()
        
        // Destroy existing charts
        if (punctualityChartInstance) {
          punctualityChartInstance.destroy()
        }
        if (histogramChartInstance) {
          histogramChartInstance.destroy()
        }
        
        // Check if chart elements are available
        if (!punctualityChart.value || !histogramChart.value) {
          console.warn('Chart canvas elements not available yet')
          return
        }
        
        // Punctuality Distribution Doughnut Chart
        if (punctualityChart.value) {
          punctualityChartInstance = new Chart(punctualityChart.value, {
          type: 'doughnut',
          data: {
            labels: ['±3 min', '±5 min', '±15 min', '>15 min'],
            datasets: [{
              data: [
                kpiResults.value.within3MinCount || 0,
                (kpiResults.value.within5MinCount || 0) - (kpiResults.value.within3MinCount || 0),
                (kpiResults.value.within15MinCount || 0) - (kpiResults.value.within5MinCount || 0),
                (kpiResults.value.totalAnalyzed || 0) - (kpiResults.value.within15MinCount || 0)
              ],
              backgroundColor: [
                'rgb(34, 197, 94)',   // Green for ±3 min
                'rgb(251, 191, 36)',  // Yellow for ±5 min  
                'rgb(59, 130, 246)',  // Blue for ±15 min
                'rgb(239, 68, 68)'    // Red for >15 min
              ],
              borderWidth: 2,
              borderColor: '#ffffff'
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'bottom'
              },
              tooltip: {
                callbacks: {
                  label: function(context) {
                    const total = context.dataset.data.reduce((a, b) => a + b, 0)
                    const percentage = ((context.parsed / total) * 100).toFixed(1)
                    return `${context.label}: ${context.parsed} flights (${percentage}%)`
                  }
                }
              }
            }
          }
        })
      }

      // Time Difference Histogram
      if (histogramChart.value && kpiResults.value.detailedResults) {
        // Use a sample of detailed results for histogram (max 1000 flights for performance)
        const allFlights = kpiResults.value.detailedResults
        const sampleSize = Math.min(1000, allFlights.length)
        const sampleFlights = allFlights.slice(0, sampleSize)
        const timeDifferences = sampleFlights.map(flight => flight.timeDifferenceMinutes)
        const histogramData = createHistogramData(timeDifferences)
        
        histogramChartInstance = new Chart(histogramChart.value, {
          type: 'bar',
          data: {
            labels: histogramData.labels,
            datasets: [{
              label: 'Number of Flights',
              data: histogramData.data,
              backgroundColor: 'rgba(14, 165, 233, 0.6)',
              borderColor: 'rgb(14, 165, 233)',
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
                  text: 'Absolute Time Difference (minutes)'
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
      
      console.log('Punctuality charts initialized successfully')
    } catch (error) {
      console.error('Error initializing punctuality charts:', error)
      showToast('Error initializing charts. Please try refreshing the page.', 'error')
    }
    }

    const createHistogramData = (timeDifferences) => {
      // Use absolute values for time differences to focus on magnitude of deviation
      const absoluteDifferences = timeDifferences.map(diff => Math.abs(diff))
      const bins = [0, 1, 3, 5, 10, 15, 20, 30]
      const labels = []
      const data = []
      
      for (let i = 0; i < bins.length - 1; i++) {
        const min = bins[i]
        const max = bins[i + 1]
        labels.push(`${min}-${max} min`)
        
        const count = absoluteDifferences.filter(diff => diff >= min && diff < max).length
        data.push(count)
      }
      
      // Add a final bin for >30 minutes
      labels.push('30+ min')
      const count30Plus = absoluteDifferences.filter(diff => diff >= 30).length
      data.push(count30Plus)
      
      return { labels, data }
    }

    // Clear results function
    const clearResults = () => {
      kpiResults.value = null
      analysisStore.clearPunctualityAnalysis()
      
      // Destroy existing charts
      if (punctualityChartInstance) {
        punctualityChartInstance.destroy()
        punctualityChartInstance = null
      }
      if (histogramChartInstance) {
        histogramChartInstance.destroy()
        histogramChartInstance = null
      }
      
      showToast('Analysis results cleared', 'info')
    }

    // Load existing results on component mount
    onMounted(() => {
      loadExistingResults()
    })

    return {
      loading,
      kpiResults,
      punctualityChart,
      histogramChart,
      showAllResults,
      currentPage,
      displayedFlights,
      totalPages,
      toggleShowAllResults,
      formatNumber,
      formatMinutes,
      formatDuration,
      formatDateTime,
      formatDifference,
      getDifferenceColor,
      getPunctualityStatus,
      getPunctualityLabel,
      runKPIAnalysis,
      clearResults,
      initCharts,
      analysisStore
    }
  }
}
</script>
