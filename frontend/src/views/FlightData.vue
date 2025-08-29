<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Flight Data Management</h1>
          <p class="mt-1 text-sm text-gray-600">
            Process and store flight data in our database for analysis, data cleaning, and processing workflows
          </p>
        </div>
      </div>
    </div>

    <!-- Database Statistics -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-medium text-gray-900">Database Overview</h2>
        <button 
          @click="loadDatabaseStats" 
          :disabled="loading.stats"
          class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-aviation-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <svg v-if="loading.stats" class="animate-spin -ml-1 mr-2 h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <svg v-else class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          {{ loading.stats ? 'Refreshing...' : 'Refresh' }}
        </button>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-gray-50 rounded-lg p-4">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">Real Flights</p>
              <p class="text-2xl font-bold text-gray-900">{{ databaseStats.totalFlights || 0 }}</p>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 rounded-lg p-4">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">Predicted Flights</p>
              <p class="text-2xl font-bold text-gray-900">{{ databaseStats.totalPredictedFlights || 0 }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Guided Data Processing Workflow -->
    <div class="bg-white shadow rounded-lg p-6">
      <div class="mb-6">
        <h2 class="text-xl font-bold text-gray-900 mb-2">Data Processing Workflow</h2>
        <p class="text-sm text-gray-600">
          Follow these steps to retrieve and store flight data in our database for analysis, data cleaning, and processing. 
          Each step prepares the data for comprehensive flight performance analysis.
        </p>
      </div>

      <div class="space-y-6">
        <!-- Step 1: Retrieve Real Flight Data -->
        <div class="border rounded-lg p-6" :class="getStepBorderClass(1)">
          <div class="flex items-start space-x-4">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium" 
                   :class="getStepIndicatorClass(1)">
                1
              </div>
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <h3 class="text-lg font-medium text-gray-900">Retrieve Real Flight Data</h3>
                  <p class="text-sm text-gray-600 mt-1">
                    Extract actual flight tracking data from Oracle database for analysis baseline.
                  </p>
                </div>
                <button 
                  @click="processStep1" 
                  :disabled="loading.step1"
                  class="btn-primary"
                  :class="{ 'opacity-50 cursor-not-allowed': loading.step1 }"
                >
                  <svg v-if="loading.step1" class="spinner mr-2" viewBox="0 0 24 24"></svg>
                  <svg v-else-if="workflowState.step1.completed" class="w-4 h-4 mr-2 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                  <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                  {{ workflowState.step1.completed ? 'Completed' : (loading.step1 ? 'Processing...' : 'Start Processing') }}
                </button>
              </div>

              <!-- Date and Time Selection -->
              <div class="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <h4 class="text-sm font-medium text-blue-800 mb-3">Data Selection Parameters</h4>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <!-- Date Selection -->
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Date</label>
                    <input 
                      type="date" 
                      v-model="step1Config.date"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-aviation-500 focus:border-aviation-500 text-sm"
                      :disabled="loading.step1"
                    />
                  </div>
                  
                  <!-- Start Time Selection -->
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Start Time (Optional)</label>
                    <input 
                      type="time" 
                      v-model="step1Config.startTime"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-aviation-500 focus:border-aviation-500 text-sm"
                      :disabled="loading.step1"
                    />
                  </div>
                  
                  <!-- End Time Selection -->
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">End Time (Optional)</label>
                    <input 
                      type="time" 
                      v-model="step1Config.endTime"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-aviation-500 focus:border-aviation-500 text-sm"
                      :disabled="loading.step1"
                    />
                  </div>
                </div>
                
                <!-- Time Range Info -->
                <div class="mt-3 text-sm text-blue-700">
                  <div class="flex items-center">
                    <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                    </svg>
                    <span v-if="!step1Config.startTime && !step1Config.endTime">
                      Will process entire day (00:00 - 23:59)
                    </span>
                    <span v-else-if="step1Config.startTime && step1Config.endTime">
                      Will process time range: {{ step1Config.startTime }} - {{ step1Config.endTime }}
                    </span>
                    <span v-else class="text-yellow-700">
                      Please specify both start and end times, or leave both empty for full day
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- Warning Alert -->
              <div class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                <div class="flex">
                  <svg class="w-5 h-5 text-yellow-400 mr-3 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                  </svg>
                  <div>
                    <h4 class="text-sm font-medium text-yellow-800">Long Processing Time Expected</h4>
                    <p class="text-sm text-yellow-700 mt-1">
                      This step processes real-time data and may take <strong>more than 24 hours</strong> to complete. 
                      The system will extract and process all flight tracking data from the Oracle database.
                    </p>
                  </div>
                </div>
              </div>

              <!-- Step 1 Results -->
              <div v-if="workflowState.step1.result" class="mt-4 p-4 bg-gray-50 rounded-lg">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                  <div>
                    <p class="text-lg font-bold text-aviation-600">{{ workflowState.step1.result.totalFlightsExtracted || 0 }}</p>
                    <p class="text-xs text-gray-500">Flights Extracted</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-green-600">{{ workflowState.step1.result.totalFlightsProcessed || 0 }}</p>
                    <p class="text-xs text-gray-500">Flights Processed</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-purple-600">{{ workflowState.step1.result.totalTrackingPoints || 0 }}</p>
                    <p class="text-xs text-gray-500">Tracking Points</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-gray-600">{{ formatDuration(workflowState.step1.result.processingTimeMs) }}</p>
                    <p class="text-xs text-gray-500">Processing Time</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 2: Synchronize Predicted Data -->
        <div class="border rounded-lg p-6" :class="getStepBorderClass(2)">
          <div class="flex items-start space-x-4">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium" 
                   :class="getStepIndicatorClass(2)">
                2
              </div>
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <div>
                  <h3 class="text-lg font-medium text-gray-900">Synchronize Predicted Flight Data</h3>
                  <p class="text-sm text-gray-600 mt-1">
                    Retrieve predicted flight routes and timing data that correspond to the processed real flights.
                  </p>
                </div>
                <button 
                  @click="processStep2" 
                  :disabled="loading.step2"
                  class="btn-primary"
                  :class="{ 'opacity-50 cursor-not-allowed': loading.step2 }"
                >
                  <svg v-if="loading.step2" class="spinner mr-2" viewBox="0 0 24 24"></svg>
                  <svg v-else-if="workflowState.step2.completed" class="w-4 h-4 mr-2 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                  <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
                  </svg>
                  {{ workflowState.step2.completed ? 'Completed' : (loading.step2 ? 'Synchronizing...' : 'Start Sync') }}
                </button>
              </div>

              <!-- Step 2 Results -->
              <div v-if="workflowState.step2.result" class="mt-4 p-4 bg-gray-50 rounded-lg">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                  <div>
                    <p class="text-lg font-bold text-blue-600">{{ workflowState.step2.result.totalRequested || 0 }}</p>
                    <p class="text-xs text-gray-500">Requested</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-green-600">{{ workflowState.step2.result.totalProcessed || 0 }}</p>
                    <p class="text-xs text-gray-500">Processed</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-yellow-600">{{ workflowState.step2.result.totalNotFound || 0 }}</p>
                    <p class="text-xs text-gray-500">Not Found</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-gray-600">{{ formatDuration(workflowState.step2.result.processingTimeMs) }}</p>
                    <p class="text-xs text-gray-500">Processing Time</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 3: Densify Predicted Data -->
        <div class="border rounded-lg p-6" :class="getStepBorderClass(3)">
          <div class="flex items-start space-x-4">
            <div class="flex-shrink-0">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium" 
                   :class="getStepIndicatorClass(3)">
                3
              </div>
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <div>
                  <h3 class="text-lg font-medium text-gray-900">Densify Predicted Trajectories</h3>
                  <p class="text-sm text-gray-600 mt-1">
                    Generate additional trajectory points to match the density of real flight data for accurate trajectory analysis.
                  </p>
                  
                  <!-- Warning for independent execution -->
                  <div class="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                    <div class="flex">
                      <div class="flex-shrink-0">
                        <svg class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                          <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                        </svg>
                      </div>
                      <div class="ml-3">
                        <p class="text-sm text-yellow-800">
                          <strong>Important:</strong> If running this step independently, ensure that the data is not already densified. 
                          If it is, please run <strong>Step 2</strong> first to undensify the data, then run Step 3 to densify again.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
                <button 
                  @click="processStep3" 
                  :disabled="loading.step3"
                  class="btn-primary"
                  :class="{ 'opacity-50 cursor-not-allowed': loading.step3 }"
                >
                  <svg v-if="loading.step3" class="spinner mr-2" viewBox="0 0 24 24"></svg>
                  <svg v-else-if="workflowState.step3.completed" class="w-4 h-4 mr-2 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                  <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                  {{ workflowState.step3.completed ? 'Completed' : (loading.step3 ? 'Densifying...' : 'Start Densification') }}
                </button>
              </div>

              <!-- Step 3 Results -->
              <div v-if="workflowState.step3.result" class="mt-4 p-4 bg-gray-50 rounded-lg">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                  <div>
                    <p class="text-lg font-bold text-blue-600">{{ workflowState.step3.result.totalRequested || 0 }}</p>
                    <p class="text-xs text-gray-500">Flights Processed</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-green-600">{{ workflowState.step3.result.totalProcessed || 0 }}</p>
                    <p class="text-xs text-gray-500">Successfully Densified</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-purple-600">{{ workflowState.step3.result.summary?.totalDensifiedElements || 0 }}</p>
                    <p class="text-xs text-gray-500">Points Generated</p>
                  </div>
                  <div>
                    <p class="text-lg font-bold text-gray-600">{{ formatDuration(workflowState.step3.result.processingTimeMs) }}</p>
                    <p class="text-xs text-gray-500">Processing Time</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Workflow Completion Status -->
        <div v-if="isWorkflowComplete" class="border-2 border-green-200 bg-green-50 rounded-lg p-6">
          <div class="flex items-center">
            <svg class="w-8 h-8 text-green-600 mr-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <div>
              <h3 class="text-lg font-medium text-green-800">Workflow Complete!</h3>
              <p class="text-sm text-green-700 mt-1">
                All data processing steps have been completed successfully. You can now run punctuality and trajectory accuracy analyses.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Processing Results -->
    <div v-if="processingResult" class="card">
      <div class="card-header">
        <h3 class="text-lg font-medium text-gray-900">Latest Processing Result</h3>
        <span class="status-badge" :class="processingResult.status === 'SUCCESS' ? 'status-success' : 'status-error'">
          {{ processingResult.status }}
        </span>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="text-center">
          <p class="text-2xl font-bold text-aviation-600">{{ processingResult.totalFlightsExtracted || 0 }}</p>
          <p class="text-sm text-gray-500">Flights Extracted</p>
        </div>
        <div class="text-center">
          <p class="text-2xl font-bold text-green-600">{{ processingResult.totalFlightsProcessed || 0 }}</p>
          <p class="text-sm text-gray-500">Flights Processed</p>
        </div>
        <div class="text-center">
          <p class="text-2xl font-bold text-purple-600">{{ processingResult.totalTrackingPoints || 0 }}</p>
          <p class="text-sm text-gray-500">Tracking Points</p>
        </div>
        <div class="text-center">
          <p class="text-2xl font-bold text-gray-600">{{ processingResult.processingTimeMs || 0 }}ms</p>
          <p class="text-sm text-gray-500">Processing Time</p>
        </div>
      </div>
      <div class="mt-4 p-3 bg-gray-50 rounded-lg">
        <p class="text-sm text-gray-700">{{ processingResult.message }}</p>
      </div>
    </div>

    <!-- Recent Processing History -->
    <div class="card">
      <div class="card-header">
        <h3 class="text-lg font-medium text-gray-900">Processing History</h3>
        <div class="flex items-center space-x-4">
          <span class="text-sm text-gray-500">Last 15 operations</span>
          <button 
            @click="refreshProcessingHistory" 
            :disabled="loading.history"
            class="text-sm text-aviation-600 hover:text-aviation-700 disabled:opacity-50"
          >
            <span v-if="loading.history">Refreshing...</span>
            <span v-else>🔄 Refresh</span>
          </button>
        </div>
      </div>
      <div class="overflow-x-auto">
        <div v-if="loading.history" class="flex justify-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-aviation-600"></div>
        </div>
        <div v-else-if="processingHistory.length === 0" class="text-center py-8 text-gray-500">
          No processing history available
        </div>
        <table v-else class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Timestamp
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Operation
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Records
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Duration
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Details
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="operation in processingHistory" :key="operation.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatHistoryTimestamp(operation.timestamp) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <div class="flex items-center">
                  <span class="mr-2">{{ getOperationIcon(operation.operation) }}</span>
                  {{ getOperationDisplayName(operation.operation) }}
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="status-badge" :class="getStatusBadgeClass(operation.status)">
                  {{ formatStatus(operation.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <div v-if="operation.recordsProcessed !== null">
                  {{ operation.recordsProcessed }}
                  <span v-if="operation.recordsWithErrors > 0" class="text-red-600 text-xs">
                    ({{ operation.recordsWithErrors }} errors)
                  </span>
                </div>
                <span v-else class="text-gray-400">-</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatDuration(operation.durationMs) }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate" :title="operation.details || operation.errorMessage">
                {{ operation.details || operation.errorMessage || '-' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, inject, computed } from 'vue'
import { flightAPI, flightSearchAPI, predictedFlightAPI, trajectoryDensificationAPI, processingHistoryAPI, apiUtils } from '../services/api'

export default {
  name: 'FlightData',
  setup() {
    const showToast = inject('showToast')
    const setGlobalLoading = inject('setGlobalLoading')
    
    const loading = ref({
      process: false,
      duplicates: false,
      cleanup: false,
      step1: false,
      step2: false,
      step3: false,
      history: false,
      stats: false
    })

    // Initial loading state for page blocking
    const initialLoading = ref(true)

    // Immediate 2-second timeout
    setTimeout(() => {
      console.log('2-second timeout - completing loading')
      initialLoading.value = false
    }, 2000)

    // Additional safety nets
    setTimeout(() => {
      if (initialLoading.value) {
        console.warn('5-second fallback timeout - forcing loading to complete')
        initialLoading.value = false
      }
    }, 5000)

    setTimeout(() => {
      if (initialLoading.value) {
        console.error('10-second emergency timeout - forcing loading to complete')
        initialLoading.value = false
      }
    }, 10000)

    const processingResult = ref(null)
    const duplicateAnalysis = ref({})
    const processingHistory = ref([])
    
    // Database statistics
    const databaseStats = ref({
      totalFlights: 0,
      totalPredictedFlights: 0
    })

    // Workflow state management
    const workflowState = ref({
      step1: {
        completed: false,
        result: null
      },
      step2: {
        completed: false,
        result: null
      },
      step3: {
        completed: false,
        result: null
      }
    })

    // Step 1 configuration for date and time selection
    const step1Config = ref({
      date: '2025-07-11', // Default to the current hardcoded date
      startTime: '', // Empty means no time filtering
      endTime: ''    // Empty means no time filtering
    })

    // Computed properties for workflow
    const isWorkflowComplete = computed(() => {
      return workflowState.value.step1.completed && 
             workflowState.value.step2.completed && 
             workflowState.value.step3.completed
    })

    // Utility functions
    const formatNumber = (num) => {
      if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
      if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
      return num?.toLocaleString() || '0'
    }



    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleString()
    }

    // localStorage utility functions
    const isCacheValid = () => {
      const lastUpdated = localStorage.getItem(STORAGE_KEYS.LAST_UPDATED)
      if (!lastUpdated) return false
      
      const timeDiff = Date.now() - parseInt(lastUpdated)
      return timeDiff < CACHE_EXPIRATION_MS
    }

    const getStatusBadgeClass = (status) => {
      switch (status.toLowerCase()) {
        case 'success': return 'status-success'
        case 'error': return 'status-error'
        case 'warning': return 'status-warning'
        default: return 'status-info'
      }
    }

    // API functions
    const processOracleData = async () => {
      loading.value.process = true
      setGlobalLoading(true, 'Processing Oracle data...')
      try {
        const response = await flightAPI.processOracleData()
        processingResult.value = response.data
        
        // Add to processing history
        processingHistory.value.unshift({
          id: Date.now(),
          timestamp: Date.now(),
          operation: 'Oracle Data Processing',
          status: response.data.status,
          flightsProcessed: response.data.totalFlightsProcessed,
          duration: response.data.processingTimeMs
        })
        
        // Keep only last 10 entries
        if (processingHistory.value.length > 10) {
          processingHistory.value = processingHistory.value.slice(0, 10)
        }
        
        showToast('Oracle data processed successfully', 'success')
      } catch (error) {
        showToast(apiUtils.formatError(error), 'error')
      } finally {
        loading.value.process = false
        setGlobalLoading(false)
      }
    }

    const fetchPlanIds = async () => {
      loading.value.planIds = true
      try {
        const response = await flightAPI.getPlanIds()
        planIds.value = response.data.planIds || []
        showToast(`Loaded ${planIds.value.length} plan IDs`, 'success')
      } catch (error) {
        showToast(apiUtils.formatError(error), 'error')
      } finally {
        loading.value.planIds = false
      }
    }

    const analyzeDuplicates = async () => {
      loading.value.duplicates = true
      try {
        const response = await flightAPI.analyzeDuplicates()
        duplicateAnalysis.value = response.data
        showToast('Duplicate analysis completed', 'success')
      } catch (error) {
        showToast(apiUtils.formatError(error), 'error')
      } finally {
        loading.value.duplicates = false
      }
    }

    const cleanupDuplicates = async () => {
      loading.value.cleanup = true
      setGlobalLoading(true, 'Cleaning up duplicates...')
      try {
        const response = await flightAPI.cleanupDuplicates()
        showToast('Duplicates cleaned up successfully', 'success')
        await analyzeDuplicates()
      } catch (error) {
        showToast(apiUtils.formatError(error), 'error')
      } finally {
        loading.value.cleanup = false
        setGlobalLoading(false)
      }
    }

    // Load database statistics
    const loadDatabaseStats = async () => {
      loading.value.stats = true
      try {
        // Load flight statistics from flight-search endpoint (has both real and predicted)
        const flightStatsResponse = await flightSearchAPI.getStats()
        databaseStats.value.totalFlights = flightStatsResponse.data.totalRealFlights
        databaseStats.value.totalPredictedFlights = flightStatsResponse.data.totalPredictedFlights
        
        console.log('Database statistics loaded:', databaseStats.value)
        showToast('Database statistics refreshed successfully', 'success')
      } catch (error) {
        console.error('Failed to load database statistics:', error)
        showToast('Failed to load database statistics: ' + apiUtils.formatError(error), 'error')
      } finally {
        loading.value.stats = false
      }
    }

    // Load processing history from API
    const loadProcessingHistory = async () => {
      loading.value.history = true
      try {
        const response = await processingHistoryAPI.getRecent(15)
        processingHistory.value = response.data
        console.log('Processing history loaded:', response.data.length, 'entries')
      } catch (error) {
        console.error('Failed to load processing history:', error)
        // Fallback to empty array if API fails
        processingHistory.value = []
        showToast('Failed to load processing history: ' + apiUtils.formatError(error), 'warning')
      } finally {
        loading.value.history = false
      }
    }

    // Refresh processing history
    const refreshProcessingHistory = async () => {
      await loadProcessingHistory()
      showToast('Processing history refreshed', 'success')
    }

    // Format processing history data for display
    const formatHistoryTimestamp = (timestamp) => {
      if (!timestamp) return '-'
      
      // Parse the timestamp - handle both ISO strings and Date objects
      let date
      if (typeof timestamp === 'string') {
        // If the timestamp doesn't end with 'Z', assume it's UTC and add 'Z'
        const isoString = timestamp.endsWith('Z') ? timestamp : timestamp + 'Z'
        date = new Date(isoString)
      } else {
        date = new Date(timestamp)
      }
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        console.warn('Invalid timestamp:', timestamp)
        return 'Invalid date'
      }
      
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      
      // If the difference calculation seems wrong, just show the actual time
      if (diffMs < 0 || diffMins > 10080) { // More than a week seems wrong
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
      
      if (diffMins < 1) return 'Just now'
      if (diffMins < 60) return `${diffMins}m ago`
      if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`
      
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }

    const formatDuration = (durationMs) => {
      if (!durationMs) return '-'
      if (durationMs < 1000) return `${durationMs}ms`
      if (durationMs < 60000) return `${(durationMs / 1000).toFixed(1)}s`
      
      const minutes = Math.floor(durationMs / 60000)
      const seconds = Math.floor((durationMs % 60000) / 1000)
      return `${minutes}m ${seconds}s`
    }

    const formatStatus = (status) => {
      const statusMap = {
        'SUCCESS': 'Success',
        'PARTIAL_SUCCESS': 'Partial Success',
        'FAILURE': 'Failed',
        'IN_PROGRESS': 'In Progress'
      }
      return statusMap[status] || status
    }

    const getOperationDisplayName = (operation) => {
      const operationMap = {
        'PROCESS_REAL_DATA': 'Process Real Data',
        'SYNC_PREDICTED_DATA': 'Sync Predicted Data',
        'DENSIFY_PREDICTED_DATA': 'Densify Trajectories'
      }
      return operationMap[operation] || operation
    }

    const getOperationIcon = (operation) => {
      const iconMap = {
        'PROCESS_REAL_DATA': '📊',
        'SYNC_PREDICTED_DATA': '🔄',
        'DENSIFY_PREDICTED_DATA': '📈'
      }
      return iconMap[operation] || '⚙️'
    }

    // Workflow methods
    const getStepBorderClass = (step) => {
      const stepState = workflowState.value[`step${step}`]
      if (stepState.completed) return 'border-green-200 bg-green-50'
      if (loading.value[`step${step}`]) return 'border-blue-200 bg-blue-50'
      return 'border-gray-200'
    }

    const getStepIndicatorClass = (step) => {
      const stepState = workflowState.value[`step${step}`]
      if (stepState.completed) return 'bg-green-600 text-white'
      if (loading.value[`step${step}`]) return 'bg-blue-600 text-white'
      return 'bg-aviation-600 text-white'
    }

    const processStep1 = async () => {
      loading.value.step1 = true
      try {
        // Validate time range if provided
        if ((step1Config.value.startTime && !step1Config.value.endTime) || 
            (!step1Config.value.startTime && step1Config.value.endTime)) {
          showToast('Please specify both start and end times, or leave both empty for full day processing', 'error')
          return
        }

        // Build processing message
        let processingMessage = `Starting real flight data processing for ${step1Config.value.date}`
        if (step1Config.value.startTime && step1Config.value.endTime) {
          processingMessage += ` from ${step1Config.value.startTime} to ${step1Config.value.endTime}`
        } else {
          processingMessage += ' (entire day)'
        }
        processingMessage += ' - this may take more than 24 hours...'
        
        showToast(processingMessage, 'info')
        
        // Call API with date and time parameters
        const response = await flightAPI.processOracleData(
          step1Config.value.date,
          step1Config.value.startTime || null,
          step1Config.value.endTime || null
        )
        
        workflowState.value.step1.result = response.data
        workflowState.value.step1.completed = true
        
        // Build success message
        let successMessage = `Step 1 completed! Processed ${response.data.totalFlightsProcessed} flights`
        if (step1Config.value.startTime && step1Config.value.endTime) {
          successMessage += ` for time range ${step1Config.value.startTime}-${step1Config.value.endTime}`
        }
        
        showToast(successMessage, 'success')
        
        // Refresh processing history and database stats to show the new operation
        setTimeout(() => {
          loadDatabaseStats()
          loadProcessingHistory()
        }, 1000)
        
      } catch (error) {
        showToast(`Step 1 failed: ${apiUtils.formatError(error)}`, 'error')
        // Refresh processing history to show the failed operation
        setTimeout(() => {
          loadProcessingHistory()
        }, 1000)
      } finally {
        loading.value.step1 = false
      }
    }

    const processStep2 = async () => {
      loading.value.step2 = true
      try {
        showToast('Starting predicted flight data synchronization...', 'info')
        const response = await predictedFlightAPI.autoSync()
        
        workflowState.value.step2.result = response.data
        workflowState.value.step2.completed = true
        
        showToast(`Step 2 completed! Synchronized ${response.data.totalProcessed} predicted flights`, 'success')
        
        // Refresh processing history and database stats to show the new operation
        setTimeout(() => {
          loadDatabaseStats()
          loadProcessingHistory()
        }, 1000)
        
      } catch (error) {
        showToast(`Step 2 failed: ${apiUtils.formatError(error)}`, 'error')
        // Refresh processing history to show the failed operation
        setTimeout(() => {
          loadProcessingHistory()
        }, 1000)
      } finally {
        loading.value.step2 = false
      }
    }

    const processStep3 = async () => {
      loading.value.step3 = true
      try {
        showToast('Starting trajectory densification...', 'info')
        const response = await trajectoryDensificationAPI.autoSync()
        
        workflowState.value.step3.result = response.data
        workflowState.value.step3.completed = true
        
        showToast(`Step 3 completed! Densified ${response.data.totalProcessed} flight trajectories`, 'success')
        
        // Refresh processing history and database stats to show the new operation
        setTimeout(() => {
          loadDatabaseStats()
          loadProcessingHistory()
        }, 1000)
        
      } catch (error) {
        showToast(`Step 3 failed: ${apiUtils.formatError(error)}`, 'error')
        // Refresh processing history to show the failed operation
        setTimeout(() => {
          loadProcessingHistory()
        }, 1000)
      } finally {
        loading.value.step3 = false
      }
    }

    const forceCompleteLoading = () => {
      console.log('User forced loading completion')
      console.log('Current initialLoading value:', initialLoading.value)
      
      // Force set to false multiple ways
      initialLoading.value = false
      
      // Use nextTick to ensure reactivity
      setTimeout(() => {
        initialLoading.value = false
        console.log('After timeout, initialLoading value:', initialLoading.value)
      }, 0)
      
      showToast('Loading completed manually', 'info')
    }

    const refreshData = async () => {
      initialLoading.value = true
      
      try {
        showToast('Data refreshed successfully', 'success')
      } catch (error) {
        showToast('Error refreshing data: ' + error.message, 'error')
      } finally {
        initialLoading.value = false
      }
    }

    onMounted(async () => {
      console.log('FlightData component mounted - starting simple loading')
      
      // Set a maximum 3-second timeout for the whole process
      const timeout = setTimeout(() => {
        console.log('3-second timeout reached - completing loading')
        initialLoading.value = false
      }, 3000)
      
      try {
        // Wait a moment for the component to initialize
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        console.log('Basic loading completed')
        
      } catch (error) {
        console.error('Loading error:', error)
      } finally {
        clearTimeout(timeout)
        console.log('Force setting initialLoading to false')
        initialLoading.value = false
        
        // Load database statistics and processing history after loading completes
        setTimeout(() => {
          loadDatabaseStats()
          loadProcessingHistory()
        }, 100)
      }
    })

    // Auto-refresh processing history every 30 seconds
    let refreshInterval = null
    
    onMounted(() => {
      // Set up auto-refresh for processing history
      refreshInterval = setInterval(() => {
        if (!loading.value.history) {
          loadProcessingHistory()
        }
      }, 30000) // Refresh every 30 seconds
    })
    
    onUnmounted(() => {
      // Clean up interval when component is destroyed
      if (refreshInterval) {
        clearInterval(refreshInterval)
      }
    })

    return {
      loading,
      initialLoading,
      processingResult,
      duplicateAnalysis,
      processingHistory,
      databaseStats,
      workflowState,
      step1Config,
      isWorkflowComplete,
      formatNumber,
      formatDuration,
      formatTime,
      getStatusBadgeClass,
      getStepBorderClass,
      getStepIndicatorClass,
      processStep1,
      processStep2,
      processStep3,
      forceCompleteLoading,
      refreshData,
      processOracleData,
      analyzeDuplicates,
      cleanupDuplicates,
      // Processing History functions
      loadDatabaseStats,
      loadProcessingHistory,
      refreshProcessingHistory,
      formatHistoryTimestamp,
      formatStatus,
      getOperationDisplayName,
      getOperationIcon
    }
  }
}
</script>
