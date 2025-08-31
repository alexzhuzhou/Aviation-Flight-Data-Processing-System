<template>
  <!-- Modal Overlay -->
  <div class="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center p-4 z-50" @click="closeModal">
    <!-- Modal Container - Much Larger -->
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-[95vw] h-[95vh] flex flex-col overflow-hidden" @click.stop>
      <!-- Modal Header -->
      <div class="flex items-center justify-between p-6 border-b border-gray-200 bg-gradient-to-r from-aviation-50 to-blue-50 flex-shrink-0">
        <div class="flex items-center space-x-4">
          <div class="flex items-center justify-center w-12 h-12 bg-aviation-100 rounded-xl">
            <svg class="w-6 h-6 text-aviation-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </div>
          <div>
            <h2 class="text-2xl font-bold text-gray-900">Flight Details Comparison</h2>
            <p class="text-sm text-gray-600">{{ flightData.indicative || 'Unknown' }} - Real vs Predicted Data Analysis</p>
          </div>
        </div>
        <div class="flex items-center space-x-3">
          <button 
            @click="exportData"
            class="inline-flex items-center px-4 py-2 text-sm font-medium text-aviation-700 bg-aviation-100 border border-aviation-200 rounded-lg hover:bg-aviation-200 transition-colors"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            Export
          </button>
          <button 
            @click="closeModal"
            class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Modal Content - Full Height with Proper Scrolling -->
      <div class="flex-1 flex overflow-hidden">
        <div class="w-full grid grid-cols-1 lg:grid-cols-2 gap-0">
          
          <!-- Real Flight Data Column -->
          <div class="bg-green-50 border-r border-gray-200 flex flex-col">
            <div class="p-6 border-b border-green-200 bg-green-100 flex-shrink-0">
              <div class="flex items-center space-x-3">
                <div class="flex items-center justify-center w-10 h-10 bg-green-200 rounded-lg">
                  <svg class="w-5 h-5 text-green-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <h4 class="text-xl font-bold text-green-800">Real Flight Data</h4>
                  <p class="text-sm text-green-600">Actual flight execution data</p>
                </div>
              </div>
            </div>
            
            <div class="flex-1 overflow-hidden flex flex-col" style="max-height: calc(100vh - 200px);">
              <div v-if="flightData.realFlight" class="flex flex-col h-full">
                
                <!-- Tab Navigation -->
                <div class="flex border-b border-green-200 bg-white mx-6 mt-6 rounded-t-lg overflow-hidden">
                  <button 
                    @click="activeRealTab = 'overview'"
                    :class="activeRealTab === 'overview' ? 'bg-green-100 text-green-800 border-green-300' : 'bg-gray-50 text-gray-600 hover:bg-gray-100'"
                    class="flex-1 px-4 py-3 text-sm font-medium border-b-2 transition-colors"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Overview
                  </button>
                  <button 
                    @click="activeRealTab = 'tracking'"
                    :class="activeRealTab === 'tracking' ? 'bg-green-100 text-green-800 border-green-300' : 'bg-gray-50 text-gray-600 hover:bg-gray-100'"
                    class="flex-1 px-4 py-3 text-sm font-medium border-b-2 transition-colors"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    </svg>
                    Tracking Points ({{ flightData.realFlight.trackingPoints?.length || 0 }})
                  </button>
                </div>

                <!-- Tab Content -->
                <div class="flex-1 overflow-y-auto px-6 pb-12">
                  
                  <!-- Overview Tab -->
                  <div v-if="activeRealTab === 'overview'" class="space-y-6 pt-6 pb-8">
                
                <!-- Basic Information Card -->
                <div class="bg-white rounded-lg border border-green-200 p-4">
                  <h5 class="text-lg font-semibold text-green-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Basic Information
                  </h5>
                  <div class="grid grid-cols-2 gap-4">
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Plan ID</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.planId }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Indicative</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.indicative }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Aircraft Type</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.aircraftType }}</p>
                      </div>
                    </div>
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Track ID</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.trackId }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Airline</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.airline || 'N/A' }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Status</label>
                        <p class="text-sm font-semibold" :class="flightData.realFlight.finished ? 'text-gray-600' : 'text-green-600'">
                          {{ flightData.realFlight.finished ? 'Completed' : 'Active' }}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Route Information Card -->
                <div class="bg-white rounded-lg border border-green-200 p-4">
                  <h5 class="text-lg font-semibold text-green-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    Route Information
                  </h5>
                  <div class="space-y-4">
                    <div class="flex items-center justify-between">
                      <div class="text-center">
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Origin</label>
                        <p class="text-lg font-bold text-gray-900">{{ flightData.realFlight.startPointIndicative }}</p>
                      </div>
                      <div class="flex items-center px-4">
                        <div class="w-3 h-3 bg-green-400 rounded-full"></div>
                        <div class="flex-1 h-0.5 bg-green-300 mx-2"></div>
                        <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                        </svg>
                        <div class="flex-1 h-0.5 bg-green-300 mx-2"></div>
                        <div class="w-3 h-3 bg-green-600 rounded-full"></div>
                      </div>
                      <div class="text-center">
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Destination</label>
                        <p class="text-lg font-bold text-gray-900">{{ flightData.realFlight.endPointIndicative }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Flight Parameters Card -->
                <div class="bg-white rounded-lg border border-green-200 p-4">
                  <h5 class="text-lg font-semibold text-green-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                    Flight Parameters
                  </h5>
                  <div class="grid grid-cols-2 gap-4">
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Cruise Level</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.cruiseLevel ? `FL${flightData.realFlight.cruiseLevel}` : 'N/A' }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Cruise Speed</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.cruiseSpeed || 'N/A' }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">SSR Code</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.ssrCode || 'N/A' }}</p>
                      </div>
                    </div>
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Tracking Points</label>
                        <p class="text-sm font-semibold text-green-600">{{ flightData.realFlight.totalTrackingPoints || 0 }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Flight Rules</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.flightRules || 'N/A' }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">EET Minutes</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.realFlight.eetMinute || 'N/A' }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Timing Information Card -->
                <div class="bg-white rounded-lg border border-green-200 p-4">
                  <h5 class="text-lg font-semibold text-green-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Timing Information
                  </h5>
                  <div class="space-y-3">
                    <div>
                      <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Flight Plan Date</label>
                      <p class="text-sm font-semibold text-gray-900">{{ formatDate(flightData.realFlight.flightPlanDate) }}</p>
                    </div>
                    <div>
                      <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">EOBT (Estimated Off-Block Time)</label>
                      <p class="text-sm font-semibold text-gray-900">{{ formatDate(flightData.realFlight.eobt) }}</p>
                    </div>
                    <div>
                      <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">ETA (Estimated Time of Arrival)</label>
                      <p class="text-sm font-semibold text-gray-900">{{ formatDate(flightData.realFlight.eta) }}</p>
                    </div>
                    <div>
                      <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Current Arrival Time</label>
                      <p class="text-sm font-semibold text-gray-900">{{ formatDate(flightData.realFlight.currentDateTimeOfArrival) }}</p>
                    </div>
                  </div>
                </div>

                  </div>

                  <!-- Tracking Points Tab -->
                  <div v-if="activeRealTab === 'tracking'" class="pt-6 pb-8">
                    <div v-if="flightData.realFlight.trackingPoints && flightData.realFlight.trackingPoints.length > 0">
                      <div class="bg-white rounded-lg border border-green-200 p-4">
                        <div class="flex items-center justify-between mb-4">
                          <h5 class="text-lg font-semibold text-green-800 flex items-center">
                            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                            </svg>
                            Flight Tracking Points ({{ flightData.realFlight.trackingPoints.length }})
                          </h5>
                          <button 
                            @click="toggleTrackingPoints" 
                            class="text-xs text-green-600 hover:text-green-800 px-3 py-1 rounded border border-green-200 bg-green-50"
                          >
                            {{ showAllTrackingPoints ? 'Show Less' : 'Show All' }}
                          </button>
                        </div>
                        <div class="h-[calc(100vh-280px)] min-h-96 max-h-[700px] overflow-y-auto border border-gray-200 rounded-lg bg-gray-50">
                          <div class="p-2">
                            <div 
                              v-for="(point, index) in displayedTrackingPoints" 
                              :key="index"
                              class="p-3 border border-gray-200 bg-white rounded mb-2 last:mb-0"
                            >
                              <div class="grid grid-cols-3 gap-3 text-xs">
                                <div>
                                  <label class="text-gray-500 font-medium block mb-1">Coordinates</label>
                                  <p class="font-mono text-gray-900">{{ formatCoordinate(point.latitude) }}</p>
                                  <p class="font-mono text-gray-900">{{ formatCoordinate(point.longitude) }}</p>
                                </div>
                                <div>
                                  <label class="text-gray-500 font-medium block mb-1">Altitude/Speed</label>
                                  <p class="text-gray-900">FL{{ point.flightLevel }}</p>
                                  <p class="text-gray-900">{{ point.speed }} kts</p>
                                </div>
                                <div>
                                  <label class="text-gray-500 font-medium block mb-1">Time/Source</label>
                                  <p class="text-gray-900">{{ formatTimestamp(point.timestamp) }}</p>
                                  <p class="text-gray-600 text-xs">{{ point.detectorSource }}</p>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-else class="flex items-center justify-center h-64">
                      <div class="text-center">
                        <svg class="w-12 h-12 text-gray-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        </svg>
                        <p class="text-gray-500 font-medium">No tracking points available</p>
                      </div>
                    </div>
                  </div>

                </div>

              </div>
              <div v-else class="flex items-center justify-center h-full">
                <div class="text-center">
                  <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.29-1.009-5.824-2.562M15 6.306a7.962 7.962 0 00-6 0m6 0a7.962 7.962 0 011.999 5.714 7.962 7.962 0 01-1.999 5.714" />
                  </svg>
                  <p class="text-gray-500 font-medium">No real flight data available</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Predicted Flight Data Column -->
          <div class="bg-blue-50 flex flex-col">
            <div class="p-6 border-b border-blue-200 bg-blue-100 flex-shrink-0">
              <div class="flex items-center space-x-3">
                <div class="flex items-center justify-center w-10 h-10 bg-blue-200 rounded-lg">
                  <svg class="w-5 h-5 text-blue-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                  </svg>
                </div>
                <div>
                  <h4 class="text-xl font-bold text-blue-800">Predicted Flight Data</h4>
                  <p class="text-sm text-blue-600">Predicted route and trajectory data</p>
                </div>
              </div>
            </div>
            
            <div class="flex-1 overflow-hidden flex flex-col" style="max-height: calc(100vh - 200px);">
              <div v-if="flightData.predictedFlight" class="flex flex-col h-full">
                
                <!-- Tab Navigation -->
                <div class="flex border-b border-blue-200 bg-white mx-6 mt-6 rounded-t-lg overflow-hidden">
                  <button 
                    @click="activePredictedTab = 'overview'"
                    :class="activePredictedTab === 'overview' ? 'bg-blue-100 text-blue-800 border-blue-300' : 'bg-gray-50 text-gray-600 hover:bg-gray-100'"
                    class="flex-1 px-4 py-3 text-sm font-medium border-b-2 transition-colors"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Overview
                  </button>
                  <button 
                    @click="activePredictedTab = 'route'"
                    :class="activePredictedTab === 'route' ? 'bg-blue-100 text-blue-800 border-blue-300' : 'bg-gray-50 text-gray-600 hover:bg-gray-100'"
                    class="flex-1 px-4 py-3 text-sm font-medium border-b-2 transition-colors"
                  >
                    <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                    </svg>
                    Route Elements ({{ flightData.predictedFlight.routeElements?.length || 0 }})
                  </button>
                </div>

                <!-- Tab Content -->
                <div class="flex-1 overflow-y-auto px-6 pb-12">
                  
                  <!-- Overview Tab -->
                  <div v-if="activePredictedTab === 'overview'" class="space-y-6 pt-6 pb-8">
                
                <!-- Basic Information Card -->
                <div class="bg-white rounded-lg border border-blue-200 p-4">
                  <h5 class="text-lg font-semibold text-blue-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Basic Information
                  </h5>
                  <div class="grid grid-cols-2 gap-4">
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Instance ID</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.instanceId }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Indicative</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.indicative }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Aircraft Type</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.aircraftType || 'N/A' }}</p>
                      </div>
                    </div>
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Route ID</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.routeId }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Airline</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.airline || 'N/A' }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Status</label>
                        <p class="text-sm font-semibold text-blue-600">Predicted</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Route Information Card -->
                <div class="bg-white rounded-lg border border-blue-200 p-4">
                  <h5 class="text-lg font-semibold text-blue-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    Route Information
                  </h5>
                  <div class="space-y-4">
                    <div class="flex items-center justify-between">
                      <div class="text-center">
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Origin</label>
                        <p class="text-lg font-bold text-gray-900">{{ flightData.predictedFlight.startPointIndicative }}</p>
                      </div>
                      <div class="flex items-center px-4">
                        <div class="w-3 h-3 bg-blue-400 rounded-full"></div>
                        <div class="flex-1 h-0.5 bg-blue-300 mx-2"></div>
                        <svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                        </svg>
                        <div class="flex-1 h-0.5 bg-blue-300 mx-2"></div>
                        <div class="w-3 h-3 bg-blue-600 rounded-full"></div>
                      </div>
                      <div class="text-center">
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Destination</label>
                        <p class="text-lg font-bold text-gray-900">{{ flightData.predictedFlight.endPointIndicative }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Flight Parameters Card -->
                <div class="bg-white rounded-lg border border-blue-200 p-4">
                  <h5 class="text-lg font-semibold text-blue-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                    Flight Parameters
                  </h5>
                  <div class="grid grid-cols-2 gap-4">
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Cruise Level</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.cruiseLevel ? `FL${flightData.predictedFlight.cruiseLevel}` : 'N/A' }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Cruise Speed</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.cruiseSpeed || 'N/A' }}</p>
                      </div>
                    </div>
                    <div class="space-y-3">
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Route Elements</label>
                        <p class="text-sm font-semibold text-blue-600">{{ flightData.predictedFlight.totalRouteElements || 0 }}</p>
                      </div>
                      <div>
                        <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Time Window</label>
                        <p class="text-sm font-semibold text-gray-900">{{ flightData.predictedFlight.time || 'N/A' }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Timing Information Card -->
                <div class="bg-white rounded-lg border border-blue-200 p-4">
                  <h5 class="text-lg font-semibold text-blue-800 mb-4 flex items-center">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Timing Information
                  </h5>
                  <div class="space-y-3">
                    <div>
                      <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Flight Plan Date</label>
                      <p class="text-sm font-semibold text-gray-900">{{ formatDate(flightData.predictedFlight.flightPlanDate) }}</p>
                    </div>
                    <div>
                      <label class="text-xs font-medium text-gray-500 uppercase tracking-wide">Current Arrival Time</label>
                      <p class="text-sm font-semibold text-gray-900">{{ formatDate(flightData.predictedFlight.currentDateTimeOfArrival) }}</p>
                    </div>
                  </div>
                </div>

                  </div>

                  <!-- Route Elements Tab -->
                  <div v-if="activePredictedTab === 'route'" class="pt-6 pb-8">
                    <div v-if="flightData.predictedFlight.routeElements && flightData.predictedFlight.routeElements.length > 0">
                      <div class="bg-white rounded-lg border border-blue-200 p-4">
                        <div class="flex items-center justify-between mb-4">
                          <h5 class="text-lg font-semibold text-blue-800 flex items-center">
                            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                            </svg>
                            Predicted Route Elements ({{ flightData.predictedFlight.routeElements.length }})
                          </h5>
                          <button 
                            @click="toggleRouteElements" 
                            class="text-xs text-blue-600 hover:text-blue-800 px-3 py-1 rounded border border-blue-200 bg-blue-50"
                          >
                            {{ showAllRouteElements ? 'Show Less' : 'Show All' }}
                          </button>
                        </div>
                        <div class="h-[calc(100vh-280px)] min-h-96 max-h-[700px] overflow-y-auto border border-gray-200 rounded-lg bg-gray-50">
                          <div class="p-2">
                            <div 
                              v-for="(element, index) in displayedRouteElements" 
                              :key="index"
                              class="p-3 border border-gray-200 bg-white rounded mb-2 last:mb-0"
                            >
                              <div class="grid grid-cols-3 gap-3 text-xs">
                                <div>
                                  <label class="text-gray-500 font-medium block mb-1">Waypoint</label>
                                  <p class="font-semibold text-gray-900">{{ element.waypoint || element.indicative || 'N/A' }}</p>
                                  <p class="text-gray-600 text-xs">{{ element.elementType || 'Unknown' }}</p>
                                </div>
                                <div>
                                  <label class="text-gray-500 font-medium block mb-1">Coordinates</label>
                                  <p class="font-mono text-gray-900">{{ formatCoordinate(element.latitude) }}</p>
                                  <p class="font-mono text-gray-900">{{ formatCoordinate(element.longitude) }}</p>
                                </div>
                                <div>
                                  <label class="text-gray-500 font-medium block mb-1">Details</label>
                                  <p class="text-gray-900">{{ element.altitude || element.levelMeters ? `${element.altitude || element.levelMeters}m` : 'N/A' }}</p>
                                  <p class="text-gray-600 text-xs">{{ element.eetMinutes ? `${element.eetMinutes}min` : 'N/A' }}</p>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div v-else class="flex items-center justify-center h-64">
                      <div class="text-center">
                        <svg class="w-12 h-12 text-gray-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                        </svg>
                        <p class="text-gray-500 font-medium">No route elements available</p>
                      </div>
                    </div>
                  </div>

                </div>

              </div>
              <div v-else class="flex items-center justify-center h-full">
                <div class="text-center">
                  <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                  </svg>
                  <p class="text-gray-500 font-medium">No predicted flight data available</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, inject } from 'vue'

export default {
  name: 'FlightDetailsModal',
  props: {
    flightData: {
      type: Object,
      required: true
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const showToast = inject('showToast')
    
    const showAllTrackingPoints = ref(false)
    const showAllRouteElements = ref(false)
    const activeRealTab = ref('overview')
    const activePredictedTab = ref('overview')
    
    const displayedTrackingPoints = computed(() => {
      if (!props.flightData.realFlight?.trackingPoints) return []
      
      const points = props.flightData.realFlight.trackingPoints
      return showAllTrackingPoints.value ? points : points.slice(0, 5)
    })
    
    const displayedRouteElements = computed(() => {
      if (!props.flightData.predictedFlight?.routeElements) return []
      
      const elements = props.flightData.predictedFlight.routeElements
      return showAllRouteElements.value ? elements : elements.slice(0, 5)
    })
    
    const totalDistance = computed(() => {
      if (!props.flightData.predictedFlight?.routeSegments) return 0
      
      return props.flightData.predictedFlight.routeSegments.reduce((total, segment) => {
        return total + (segment.distance || 0)
      }, 0)
    })
    
    const closeModal = () => {
      emit('close')
    }
    
    const toggleTrackingPoints = () => {
      showAllTrackingPoints.value = !showAllTrackingPoints.value
    }
    
    const toggleRouteElements = () => {
      showAllRouteElements.value = !showAllRouteElements.value
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      try {
        return new Date(dateString).toLocaleString()
      } catch (e) {
        return dateString
      }
    }
    
    const formatTimestamp = (timestamp) => {
      if (!timestamp) return 'N/A'
      try {
        return new Date(parseInt(timestamp)).toLocaleString()
      } catch (e) {
        return timestamp
      }
    }
    
    // Convert radians to decimal degrees
    const radiansToDegrees = (radians) => {
      if (radians === null || radians === undefined) return null
      return radians * (180 / Math.PI)
    }
    
    // Format coordinate for display (convert from radians if needed)
    const formatCoordinate = (coordinate) => {
      if (coordinate === null || coordinate === undefined) return 'N/A'
      
      // Check if the coordinate is likely in radians (absolute value < 10)
      // Most coordinates in degrees are between -180 and 180, while radians are between -π and π (~-3.14 to 3.14)
      const absValue = Math.abs(coordinate)
      if (absValue <= Math.PI + 0.1) { // Add small buffer for precision
        // Likely in radians, convert to degrees
        return radiansToDegrees(coordinate).toFixed(6)
      } else {
        // Already in degrees
        return coordinate.toFixed(6)
      }
    }
    
    const exportFlightData = () => {
      try {
        const data = {
          exportedAt: new Date().toISOString(),
          flightIndicative: props.flightData.indicative,
          realFlight: props.flightData.realFlight,
          predictedFlight: props.flightData.predictedFlight
        }
        
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `flight-details-${props.flightData.indicative || 'unknown'}-${Date.now()}.json`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        showToast('Flight data exported successfully', 'success')
      } catch (error) {
        showToast(`Export failed: ${error.message}`, 'error')
      }
    }
    
    return {
      showAllTrackingPoints,
      showAllRouteElements,
      activeRealTab,
      activePredictedTab,
      displayedTrackingPoints,
      displayedRouteElements,
      totalDistance,
      closeModal,
      toggleTrackingPoints,
      toggleRouteElements,
      formatDate,
      formatTimestamp,
      formatCoordinate,
      radiansToDegrees,
      exportFlightData
    }
  }
}
</script>
