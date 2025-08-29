<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Navigation Header -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <!-- Logo/Title -->
            <div class="flex-shrink-0 flex items-center">
              <svg class="h-8 w-8 text-aviation-600 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
              <h1 class="text-xl font-bold text-gray-900">Aviation Flight Dashboard</h1>
            </div>
            
            <!-- Navigation Links -->
            <div class="hidden md:ml-10 md:flex md:space-x-8">
              <router-link 
                v-for="item in navigation" 
                :key="item.name"
                :to="item.href"
                class="nav-link"
                :class="{ 'nav-link-active': $route.path === item.href }"
              >
                {{ item.name }}
              </router-link>
            </div>
          </div>
          
          <!-- System Status -->
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <div 
                class="w-3 h-3 rounded-full"
                :class="systemHealthy ? 'bg-green-400' : 'bg-red-400'"
              ></div>
              <span class="text-sm text-gray-600">
                {{ systemHealthy ? 'System Online' : 'System Issues' }}
              </span>
            </div>
            
            <!-- Refresh Button -->
            <button 
              @click="refreshSystemHealth"
              :disabled="loading"
              class="btn-secondary text-sm"
            >
              <svg v-if="loading" class="spinner mr-2" viewBox="0 0 24 24"></svg>
              <svg v-else class="w-4 h-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Refresh
            </button>
          </div>
        </div>
      </div>
      
      <!-- Mobile Navigation -->
      <div class="md:hidden">
        <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200">
          <router-link 
            v-for="item in navigation" 
            :key="item.name"
            :to="item.href"
            class="mobile-nav-link"
            :class="{ 'mobile-nav-link-active': $route.path === item.href }"
          >
            {{ item.name }}
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <router-view />
    </main>

    <!-- Global Loading Overlay -->
    <div v-if="globalLoading" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 flex items-center space-x-3">
        <div class="spinner w-6 h-6"></div>
        <span class="text-gray-700">{{ loadingMessage }}</span>
      </div>
    </div>

    <!-- Toast Notifications -->
    <div class="fixed top-4 right-4 space-y-2 z-40">
      <div 
        v-for="toast in toasts" 
        :key="toast.id"
        class="toast"
        :class="toastClasses(toast.type)"
      >
        <div class="flex items-center">
          <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
            <path v-if="toast.type === 'success'" fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            <path v-else-if="toast.type === 'error'" fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            <path v-else fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
          </svg>
          <span>{{ toast.message }}</span>
        </div>
        <button @click="removeToast(toast.id)" class="ml-4 text-current opacity-70 hover:opacity-100">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, provide } from 'vue'
import { apiUtils } from './services/api'

export default {
  name: 'App',
  setup() {
    const systemHealthy = ref(true)
    const loading = ref(false)
    const globalLoading = ref(false)
    const loadingMessage = ref('')
    const toasts = ref([])
    
    const navigation = [
      { name: 'Overview', href: '/' },
      { name: 'Flight Data', href: '/flights' },
      { name: 'Flight Search', href: '/search' },
      { name: 'Analysis', href: '/analysis' },
      { name: 'Trajectory', href: '/trajectory' },
    ]

    // Toast management
    let toastId = 0
    const showToast = (message, type = 'info', duration = 5000) => {
      const toast = {
        id: ++toastId,
        message,
        type,
      }
      toasts.value.push(toast)
      
      setTimeout(() => {
        removeToast(toast.id)
      }, duration)
    }

    const removeToast = (id) => {
      const index = toasts.value.findIndex(toast => toast.id === id)
      if (index > -1) {
        toasts.value.splice(index, 1)
      }
    }

    const toastClasses = (type) => {
      const base = 'toast bg-white border-l-4 rounded-lg shadow-lg p-4 max-w-sm'
      switch (type) {
        case 'success': return `${base} border-green-400 text-green-700`
        case 'error': return `${base} border-red-400 text-red-700`
        case 'warning': return `${base} border-yellow-400 text-yellow-700`
        default: return `${base} border-blue-400 text-blue-700`
      }
    }

    // Global loading management
    const setGlobalLoading = (isLoading, message = 'Loading...') => {
      globalLoading.value = isLoading
      loadingMessage.value = message
    }

    // System health check
    const refreshSystemHealth = async () => {
      loading.value = true
      try {
        const health = await apiUtils.checkHealth()
        systemHealthy.value = health
        showToast(
          health ? 'System is healthy' : 'System health check failed',
          health ? 'success' : 'error'
        )
      } catch (error) {
        systemHealthy.value = false
        showToast('Failed to check system health', 'error')
      } finally {
        loading.value = false
      }
    }

    // Initial health check
    onMounted(() => {
      refreshSystemHealth()
    })

    // Provide global functions to child components
    provide('showToast', showToast)
    provide('setGlobalLoading', setGlobalLoading)

    return {
      navigation,
      systemHealthy,
      loading,
      globalLoading,
      loadingMessage,
      toasts,
      refreshSystemHealth,
      removeToast,
      toastClasses,
    }
  }
}
</script>

<style scoped>
.nav-link {
  @apply text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200;
}

.nav-link-active {
  @apply text-aviation-600 bg-aviation-50;
}

.mobile-nav-link {
  @apply text-gray-500 hover:text-gray-700 block px-3 py-2 rounded-md text-base font-medium transition-colors duration-200;
}

.mobile-nav-link-active {
  @apply text-aviation-600 bg-aviation-50;
}

.toast {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
