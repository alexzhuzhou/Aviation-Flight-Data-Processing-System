import { createRouter, createWebHistory } from 'vue-router'
import Overview from '../views/Overview.vue'
import FlightData from '../views/FlightData.vue'
import FlightSearch from '../views/FlightSearch.vue'
import Analysis from '../views/Analysis.vue'
import Trajectory from '../views/Trajectory.vue'

const routes = [
  {
    path: '/',
    name: 'Overview',
    component: Overview,
    meta: { title: 'Dashboard Overview' }
  },
  {
    path: '/flights',
    name: 'FlightData',
    component: FlightData,
    meta: { title: 'Flight Data Management' }
  },
  {
    path: '/search',
    name: 'FlightSearch',
    component: FlightSearch,
    meta: { title: 'Flight Search & Management' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: Analysis,
    meta: { title: 'Flight Analysis' }
  },
  {
    path: '/trajectory',
    name: 'Trajectory',
    component: Trajectory,
    meta: { title: 'Trajectory Analysis' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Update page title based on route
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - Aviation Dashboard` : 'Aviation Dashboard'
  next()
})

export default router
