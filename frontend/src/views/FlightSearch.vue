<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Flight Search & Management</h1>
        <p class="mt-2 text-gray-600">Search, view, and manage real and predicted flight data</p>
      </div>

      <!-- Search Section -->
      <div class="bg-white rounded-xl shadow-lg border border-gray-200 p-8 mb-8">
        <div class="flex items-center mb-6">
          <div class="flex items-center justify-center w-10 h-10 bg-aviation-100 rounded-lg mr-4">
            <svg class="w-6 h-6 text-aviation-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-900">Search Flights</h2>
            <p class="text-sm text-gray-600">Find flights by ID, flight number, or airport codes</p>
          </div>
        </div>
        
        <!-- Search Fields -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          <!-- Plan ID Search -->
          <div class="relative">
            <label class="block text-sm font-semibold text-gray-700 mb-3">
              <div class="flex items-center">
                <svg class="w-4 h-4 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                Plan ID
              </div>
            </label>
            <input
              type="text"
              v-model="searchQueries.planId"
              @input="handleSearch('planId')"
              placeholder="e.g., 17879345"
              class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-aviation-500 focus:border-aviation-500 transition-all duration-200 hover:border-gray-300"
            />
          </div>
          
          <!-- Indicative Search -->
          <div class="relative">
            <label class="block text-sm font-semibold text-gray-700 mb-3">
              <div class="flex items-center">
                <svg class="w-4 h-4 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                </svg>
                Flight Number
              </div>
            </label>
            <input
              type="text"
              v-model="searchQueries.indicative"
              @input="handleSearch('indicative')"
              placeholder="e.g., TAM3886"
              class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-aviation-500 focus:border-aviation-500 transition-all duration-200 hover:border-gray-300"
            />
          </div>

          <!-- Origin Airport Search -->
          <div class="relative">
            <label class="block text-sm font-semibold text-gray-700 mb-3">
              <div class="flex items-center">
                <svg class="w-4 h-4 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                Origin Airport
              </div>
            </label>
            <input
              type="text"
              v-model="searchQueries.origin"
              @input="handleSearch('origin')"
              placeholder="e.g., SBGR"
              class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-aviation-500 focus:border-aviation-500 transition-all duration-200 hover:border-gray-300"
            />
          </div>

          <!-- Destination Airport Search -->
          <div class="relative">
            <label class="block text-sm font-semibold text-gray-700 mb-3">
              <div class="flex items-center">
                <svg class="w-4 h-4 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                Destination Airport
              </div>
            </label>
            <input
              type="text"
              v-model="searchQueries.destination"
              @input="handleSearch('destination')"
              placeholder="e.g., SBCG"
              class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-aviation-500 focus:border-aviation-500 transition-all duration-200 hover:border-gray-300"
            />
          </div>
        </div>

        <!-- Search Actions -->
        <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pt-6 border-t border-gray-100">
          <div class="flex flex-wrap items-center gap-3">
            <button
              @click="clearSearch"
              class="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border-2 border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-all duration-200"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Clear All
            </button>
            <button
              @click="exportResults"
              :disabled="!hasResults"
              class="inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-aviation-600 border-2 border-aviation-600 rounded-lg hover:bg-aviation-700 hover:border-aviation-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-aviation-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Export Results
            </button>
          </div>
          
          <!-- Bulk Actions -->
          <div class="flex items-center gap-3" v-if="selectedFlights.length > 0">
            <div class="flex items-center px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg">
              <svg class="w-4 h-4 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span class="text-sm font-medium text-blue-800">{{ selectedFlights.length }} selected</span>
            </div>
            <button
              @click="confirmBulkDelete(false)"
              class="inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-red-600 border-2 border-red-600 rounded-lg hover:bg-red-700 hover:border-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-all duration-200"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Delete Selected
            </button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="bg-white rounded-xl shadow-lg border border-gray-200 p-12">
        <div class="text-center">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-aviation-100 rounded-full mb-4">
            <svg class="animate-spin w-8 h-8 text-aviation-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">Searching flights...</h3>
          <p class="text-gray-600">Please wait while we search through the flight database</p>
        </div>
      </div>

      <!-- Results Section -->
      <div v-if="hasResults && !loading" class="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
        <div class="px-8 py-6 bg-gradient-to-r from-aviation-50 to-blue-50 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <div class="flex items-center justify-center w-10 h-10 bg-aviation-100 rounded-lg mr-4">
                <svg class="w-6 h-6 text-aviation-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
              <div>
                <h3 class="text-xl font-bold text-gray-900">
                  Search Results
                </h3>
                <p class="text-sm text-gray-600">{{ searchResults.totalReal }} flights found</p>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <div class="px-3 py-1 bg-green-100 text-green-800 text-sm font-medium rounded-full">
                {{ searchResults.totalReal }} flights
              </div>
            </div>
          </div>
        </div>
        <div class="p-8">
          <div v-if="searchResults.realFlights.length === 0" class="text-center py-12">
            <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <h3 class="text-lg font-medium text-gray-900 mb-2">No flights found</h3>
            <p class="text-gray-500">Try adjusting your search criteria</p>
          </div>
          <div v-else class="space-y-3">
            <div 
              v-for="flight in searchResults.realFlights" 
              :key="flight.planId"
              class="group relative bg-white border border-gray-200 rounded-lg p-4 hover:border-aviation-300 hover:shadow-md transition-all duration-200"
            >
              <!-- Compact Flight Header -->
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center space-x-3">
                  <input 
                    type="checkbox" 
                    :checked="isFlightSelected(flight, 'real')"
                    @change="handleFlightSelection(flight, 'real', $event.target.checked)"
                    class="h-4 w-4 text-aviation-600 focus:ring-aviation-500 border-gray-300 rounded"
                  />
                  <div class="flex items-center space-x-3">
                    <div class="flex items-center justify-center w-8 h-8 bg-aviation-100 rounded-lg">
                      <svg class="w-4 h-4 text-aviation-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                      </svg>
                    </div>
                    <div>
                      <h4 class="font-bold text-gray-900">{{ flight.indicative }}</h4>
                      <p class="text-xs text-gray-500">ID: {{ flight.planId }}</p>
                    </div>
                  </div>
                </div>
                
                <!-- Status and Actions -->
                <div class="flex items-center space-x-2">
                  <div :class="flight.finished ? 'bg-gray-100 text-gray-600' : 'bg-green-100 text-green-700'" 
                       class="px-2 py-1 rounded-md text-xs font-medium">
                    {{ flight.finished ? 'Done' : 'Active' }}
                  </div>
                  <button 
                    @click="viewFlightDetails(flight, 'real')"
                    class="p-2 text-aviation-600 hover:bg-aviation-50 rounded-lg transition-colors"
                    title="View Details"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  </button>
                  <button 
                    @click="deleteFlight(flight, 'real')"
                    class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Delete Flight"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>

              <!-- Compact Route Display -->
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center space-x-3 flex-1">
                  <div class="text-center">
                    <div class="text-sm font-bold text-gray-900">{{ flight.startPointIndicative }}</div>
                    <div class="text-xs text-gray-500">Origin</div>
                  </div>
                  <div class="flex items-center flex-1 px-2">
                    <div class="w-2 h-2 bg-aviation-400 rounded-full"></div>
                    <div class="flex-1 h-px bg-aviation-300 mx-2"></div>
                    <svg class="w-4 h-4 text-aviation-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                    </svg>
                    <div class="flex-1 h-px bg-aviation-300 mx-2"></div>
                    <div class="w-2 h-2 bg-aviation-600 rounded-full"></div>
                  </div>
                  <div class="text-center">
                    <div class="text-sm font-bold text-gray-900">{{ flight.endPointIndicative }}</div>
                    <div class="text-xs text-gray-500">Destination</div>
                  </div>
                </div>
                
                <!-- Quick Stats -->
                <div class="flex items-center space-x-4 ml-6">
                  <div class="text-center">
                    <div class="text-xs text-gray-500">Aircraft</div>
                    <div class="text-sm font-medium text-gray-900">{{ flight.aircraftType }}</div>
                  </div>
                  <div class="text-center">
                    <div class="text-xs text-gray-500">Points</div>
                    <div class="text-sm font-medium text-green-600">{{ flight.totalTrackingPoints || 0 }}</div>
                  </div>
                </div>
              </div>

              <!-- Compact Time Info -->
              <div class="flex items-center justify-between text-xs text-gray-500 pt-2 border-t border-gray-100">
                <div v-if="flight.flightPlanDate" class="flex items-center space-x-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span>Plan: {{ formatDate(flight.flightPlanDate) }}</span>
                </div>
                <div v-if="flight.currentDateTimeOfArrival" class="flex items-center space-x-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>Arrival: {{ formatDate(flight.currentDateTimeOfArrival) }}</span>
                </div>
                <div v-if="flight.cruiseLevel" class="flex items-center space-x-1">
                  <span>FL{{ flight.cruiseLevel }} @ {{ flight.cruiseSpeed }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- No Results -->
      <div v-if="!hasResults && !loading && hasSearched" class="bg-white rounded-xl shadow-lg border border-gray-200 p-12">
        <div class="text-center">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
            <svg class="w-8 h-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">No flights found</h3>
          <p class="text-gray-600 mb-6">We couldn't find any flights matching your search criteria</p>
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 text-left max-w-md mx-auto">
            <h4 class="text-sm font-semibold text-blue-900 mb-2">Search Tips:</h4>
            <ul class="text-sm text-blue-800 space-y-1">
              <li>• Try partial matches (e.g., "TAM" for all TAM flights)</li>
              <li>• Check airport codes (e.g., "SBGR", "SBCG")</li>
              <li>• Verify flight numbers and plan IDs</li>
              <li>• Use different search fields</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Flight Details Modal -->
      <FlightDetailsModal
        v-if="showDetailsModal"
        :flightData="selectedFlightDetails"
        @close="closeDetailsModal"
      />
    </div>
  </div>
</template>

<script>
import { ref, computed, inject } from 'vue'
import { flightSearchAPI } from '../services/api'
import FlightDetailsModal from '../components/FlightDetailsModal.vue'

export default {
  name: 'FlightSearch',
  components: {
    FlightDetailsModal
  },
  setup() {
    const showToast = inject('showToast')
    
    // Reactive data
    const loading = ref(false)
    const hasSearched = ref(false)
    const searchQueries = ref({
      planId: '',
      indicative: '',
      origin: '',
      destination: ''
    })
    
    const searchResults = ref({
      realFlights: [],
      predictedFlights: [],
      totalReal: 0,
      totalPredicted: 0,
      searchType: '',
      query: ''
    })
    
    const selectedFlights = ref([])
    const showDetailsModal = ref(false)
    const selectedFlightDetails = ref(null)
    
    // Search timeout for debouncing
    let searchTimeout = null
    
    // Computed properties
    const hasResults = computed(() => {
      return searchResults.value.totalReal > 0
    })
    
    // Search methods
    const handleSearch = (searchType) => {
      // Clear previous timeout
      if (searchTimeout) {
        clearTimeout(searchTimeout)
      }
      
      // Debounce search
      searchTimeout = setTimeout(() => {
        performSearch(searchType)
      }, 300)
    }
    
    const performSearch = async (searchType) => {
      const query = searchQueries.value[searchType]
      
      if (!query || query.length < 2) {
        clearResults()
        return
      }
      
      loading.value = true
      hasSearched.value = true
      
      try {
        let response
        
        if (searchType === 'planId') {
          response = await flightSearchAPI.searchByPlanId(query)
        } else if (searchType === 'indicative') {
          response = await flightSearchAPI.searchByIndicative(query)
        } else if (searchType === 'origin') {
          response = await flightSearchAPI.searchByOrigin(query)
        } else if (searchType === 'destination') {
          response = await flightSearchAPI.searchByDestination(query)
        }
        
        searchResults.value = response.data
        selectedFlights.value = [] // Clear selections
        
      } catch (error) {
        showToast(`Search failed: ${error.message}`, 'error')
        clearResults()
      } finally {
        loading.value = false
      }
    }
    
    const clearSearch = () => {
      searchQueries.value = {
        planId: '',
        indicative: '',
        origin: '',
        destination: ''
      }
      clearResults()
    }
    
    const clearResults = () => {
      searchResults.value = {
        realFlights: [],
        predictedFlights: [],
        totalReal: 0,
        totalPredicted: 0,
        searchType: '',
        query: ''
      }
      selectedFlights.value = []
      hasSearched.value = false
    }
    
    // Flight selection methods
    const isFlightSelected = (flight, type) => {
      const flightId = type === 'real' ? flight.planId : flight.instanceId
      return selectedFlights.value.some(f => f.id === flightId && f.type === type)
    }
    
    const handleFlightSelection = (flight, type, selected) => {
      const flightId = type === 'real' ? flight.planId : flight.instanceId
      const flightData = { ...flight, type, id: flightId }
      
      if (selected) {
        selectedFlights.value.push(flightData)
      } else {
        selectedFlights.value = selectedFlights.value.filter(f => 
          !(f.id === flightId && f.type === type)
        )
      }
    }
    
    // Flight details methods
    const viewFlightDetails = async (flight, type) => {
      try {
        const planId = type === 'real' ? flight.planId : flight.instanceId
        const response = await flightSearchAPI.getFlightDetails(planId)
        
        selectedFlightDetails.value = {
          indicative: flight.indicative,
          realFlight: response.data.realFlight,
          predictedFlight: response.data.predictedFlight
        }
        showDetailsModal.value = true
        
      } catch (error) {
        showToast(`Failed to load flight details: ${error.message}`, 'error')
      }
    }
    
    const closeDetailsModal = () => {
      showDetailsModal.value = false
      selectedFlightDetails.value = null
    }
    
    // Delete methods
    const deleteFlight = async (flight, type) => {
      if (!confirm(`Are you sure you want to delete this ${type} flight?`)) {
        return
      }
      
      try {
        let response
        
        if (type === 'real') {
          response = await flightSearchAPI.deleteRealFlight(flight.planId, false)
        } else {
          response = await flightSearchAPI.deletePredictedFlight(flight.instanceId, false)
        }
        
        showToast(response.data.message, 'success')
        
        // Refresh search results
        if (hasSearched.value) {
          const currentSearchType = Object.keys(searchQueries.value).find(key => searchQueries.value[key])
          if (currentSearchType) {
            performSearch(currentSearchType)
          }
        }
        
      } catch (error) {
        showToast(`Delete failed: ${error.message}`, 'error')
      }
    }
    
    const confirmBulkDelete = async (deleteMatching) => {
      if (!confirm(`Are you sure you want to delete ${selectedFlights.value.length} selected flights?`)) {
        return
      }
      
      try {
        const realFlightIds = selectedFlights.value
          .filter(f => f.type === 'real')
          .map(f => f.planId)
        
        const predictedFlightIds = selectedFlights.value
          .filter(f => f.type === 'predicted')
          .map(f => f.instanceId)
        
        const response = await flightSearchAPI.bulkDelete(realFlightIds, predictedFlightIds, deleteMatching)
        
        showToast(response.data.message, 'success')
        
        // Clear selections and refresh results
        selectedFlights.value = []
        if (hasSearched.value) {
          const currentSearchType = Object.keys(searchQueries.value).find(key => searchQueries.value[key])
          if (currentSearchType) {
            performSearch(currentSearchType)
          }
        }
        
      } catch (error) {
        showToast(`Bulk delete failed: ${error.message}`, 'error')
      }
    }
    
    // Export methods
    const exportResults = () => {
      try {
        const data = {
          searchQuery: searchResults.value.query,
          searchType: searchResults.value.searchType,
          timestamp: new Date().toISOString(),
          realFlights: searchResults.value.realFlights,
          predictedFlights: searchResults.value.predictedFlights,
          summary: {
            totalReal: searchResults.value.totalReal,
            totalPredicted: searchResults.value.totalPredicted
          }
        }
        
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `flight-search-results-${Date.now()}.json`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        showToast('Search results exported successfully', 'success')
      } catch (error) {
        showToast(`Export failed: ${error.message}`, 'error')
      }
    }
    
    // Utility methods
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      try {
        return new Date(dateString).toLocaleString()
      } catch (e) {
        return dateString
      }
    }
    
    return {
      // Reactive data
      loading,
      hasSearched,
      searchQueries,
      searchResults,
      selectedFlights,
      showDetailsModal,
      selectedFlightDetails,
      
      // Computed
      hasResults,
      
      // Methods
      handleSearch,
      clearSearch,
      isFlightSelected,
      handleFlightSelection,
      viewFlightDetails,
      closeDetailsModal,
      deleteFlight,
      confirmBulkDelete,
      exportResults,
      formatDate
    }
  }
}
</script>
