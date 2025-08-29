/**
 * Analysis Store Utility
 * 
 * Provides persistent storage for analysis results across page navigation
 * using sessionStorage to maintain data during the browser session.
 */

const STORAGE_KEYS = {
  TRAJECTORY_ANALYSIS: 'aviation_trajectory_analysis',
  PUNCTUALITY_ANALYSIS: 'aviation_punctuality_analysis',
  ANALYSIS_TIMESTAMPS: 'aviation_analysis_timestamps'
}

class AnalysisStore {
  constructor() {
    this.listeners = new Map()
  }

  // Generic storage methods
  _setItem(key, data) {
    try {
      const item = {
        data,
        timestamp: Date.now(),
        version: '1.0'
      }
      sessionStorage.setItem(key, JSON.stringify(item))
      this._updateTimestamp(key)
      this._notifyListeners(key, data)
    } catch (error) {
      console.warn('Failed to save analysis data:', error)
    }
  }

  _getItem(key) {
    try {
      const item = sessionStorage.getItem(key)
      if (!item) return null
      
      const parsed = JSON.parse(item)
      
      // Check if data is too old (optional: expire after 1 hour)
      const oneHour = 60 * 60 * 1000
      if (Date.now() - parsed.timestamp > oneHour) {
        this._removeItem(key)
        return null
      }
      
      return parsed.data
    } catch (error) {
      console.warn('Failed to load analysis data:', error)
      return null
    }
  }

  _removeItem(key) {
    try {
      sessionStorage.removeItem(key)
      this._updateTimestamp(key, null)
      this._notifyListeners(key, null)
    } catch (error) {
      console.warn('Failed to remove analysis data:', error)
    }
  }

  _updateTimestamp(key, timestamp = Date.now()) {
    try {
      const timestamps = JSON.parse(sessionStorage.getItem(STORAGE_KEYS.ANALYSIS_TIMESTAMPS) || '{}')
      if (timestamp === null) {
        delete timestamps[key]
      } else {
        timestamps[key] = timestamp
      }
      sessionStorage.setItem(STORAGE_KEYS.ANALYSIS_TIMESTAMPS, JSON.stringify(timestamps))
    } catch (error) {
      console.warn('Failed to update timestamp:', error)
    }
  }

  // Trajectory Analysis Methods
  setTrajectoryAnalysis(results) {
    this._setItem(STORAGE_KEYS.TRAJECTORY_ANALYSIS, results)
  }

  getTrajectoryAnalysis() {
    return this._getItem(STORAGE_KEYS.TRAJECTORY_ANALYSIS)
  }

  clearTrajectoryAnalysis() {
    this._removeItem(STORAGE_KEYS.TRAJECTORY_ANALYSIS)
  }

  hasTrajectoryAnalysis() {
    return this.getTrajectoryAnalysis() !== null
  }

  // Punctuality Analysis Methods
  setPunctualityAnalysis(results) {
    this._setItem(STORAGE_KEYS.PUNCTUALITY_ANALYSIS, results)
  }

  getPunctualityAnalysis() {
    return this._getItem(STORAGE_KEYS.PUNCTUALITY_ANALYSIS)
  }

  clearPunctualityAnalysis() {
    this._removeItem(STORAGE_KEYS.PUNCTUALITY_ANALYSIS)
  }

  hasPunctualityAnalysis() {
    return this.getPunctualityAnalysis() !== null
  }

  // Utility Methods
  clearAllAnalysis() {
    this.clearTrajectoryAnalysis()
    this.clearPunctualityAnalysis()
  }

  getAnalysisTimestamps() {
    try {
      return JSON.parse(sessionStorage.getItem(STORAGE_KEYS.ANALYSIS_TIMESTAMPS) || '{}')
    } catch (error) {
      return {}
    }
  }

  getAnalysisAge(type) {
    const timestamps = this.getAnalysisTimestamps()
    const key = type === 'trajectory' ? STORAGE_KEYS.TRAJECTORY_ANALYSIS : STORAGE_KEYS.PUNCTUALITY_ANALYSIS
    const timestamp = timestamps[key]
    
    if (!timestamp) return null
    
    return Date.now() - timestamp
  }

  // Event Listeners for reactive updates
  addListener(key, callback) {
    if (!this.listeners.has(key)) {
      this.listeners.set(key, new Set())
    }
    this.listeners.get(key).add(callback)
  }

  removeListener(key, callback) {
    if (this.listeners.has(key)) {
      this.listeners.get(key).delete(callback)
    }
  }

  _notifyListeners(key, data) {
    if (this.listeners.has(key)) {
      this.listeners.get(key).forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.warn('Listener callback error:', error)
        }
      })
    }
  }

  // Storage info
  getStorageInfo() {
    const trajectoryData = this.getTrajectoryAnalysis()
    const punctualityData = this.getPunctualityAnalysis()
    const timestamps = this.getAnalysisTimestamps()
    
    return {
      hasTrajectoryAnalysis: !!trajectoryData,
      hasPunctualityAnalysis: !!punctualityData,
      trajectoryFlights: trajectoryData?.totalAnalyzedFlights || 0,
      punctualityFlights: punctualityData?.totalMatchedFlights || 0,
      trajectoryAge: this.getAnalysisAge('trajectory'),
      punctualityAge: this.getAnalysisAge('punctuality'),
      timestamps
    }
  }
}

// Create singleton instance
const analysisStore = new AnalysisStore()

export default analysisStore
