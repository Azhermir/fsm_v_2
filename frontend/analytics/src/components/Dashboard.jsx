import { useState, useEffect, useCallback } from 'react'
import PieChart from './PieChart'
import './Dashboard.css'

const API_BASE_URL = 'http://localhost:8080'
const REFRESH_INTERVAL = 30000 // 30 seconds

const Dashboard = ({ disableAutoRefresh = false }) => {
  const [metrics, setMetrics] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [dateRange, setDateRange] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  const fetchMetrics = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)

      // Build query params
      const params = new URLSearchParams()
      if (dateRange && dateRange !== 'all') {
        params.append('range', dateRange)
      }
      if (dateRange === 'custom') {
        if (startDate) params.append('startDate', startDate)
        if (endDate) params.append('endDate', endDate)
      }

      const queryString = params.toString()
      const url = `${API_BASE_URL}/api/dashboard/metrics${queryString ? `?${queryString}` : ''}`

      const response = await fetch(url)
      
      if (!response.ok) {
        throw new Error(`Failed to fetch metrics: ${response.statusText}`)
      }

      const data = await response.json()
      setMetrics(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [dateRange, startDate, endDate])

  useEffect(() => {
    fetchMetrics()
  }, [fetchMetrics])

  // Auto-refresh every 30 seconds
  useEffect(() => {
    if (disableAutoRefresh) return

    const interval = setInterval(() => {
      fetchMetrics()
    }, REFRESH_INTERVAL)

    return () => clearInterval(interval)
  }, [fetchMetrics, disableAutoRefresh])

  const handleDateRangeChange = (e) => {
    const value = e.target.value
    setDateRange(value)
    // Reset custom dates when switching away from custom
    if (value !== 'custom') {
      setStartDate('')
      setEndDate('')
    }
  }

  const calculateCompletionPercentage = () => {
    if (!metrics || metrics.totalTasks === 0) return '0.0'
    return ((metrics.completedTasks / metrics.totalTasks) * 100).toFixed(1)
  }

  const getPriorityChartData = () => {
    if (!metrics || !metrics.tasksByPriority) return []

    const priorityColors = {
      CRITICAL: '#dc3545',
      HIGH: '#fd7e14',
      MEDIUM: '#ffc107',
      LOW: '#28a745'
    }

    return Object.entries(metrics.tasksByPriority)
      .map(([priority, count]) => ({
        label: priority,
        value: count,
        color: priorityColors[priority] || '#6c757d'
      }))
      .filter(item => item.value > 0)
  }

  if (loading && !metrics) {
    return <div className="dashboard-loading">Loading dashboard...</div>
  }

  if (error) {
    return (
      <div className="dashboard-error">
        <p>Error loading dashboard: {error}</p>
        <button onClick={fetchMetrics}>Retry</button>
      </div>
    )
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Analytics Dashboard</h1>
        <button 
          className="refresh-button" 
          onClick={fetchMetrics}
          disabled={loading}
        >
          {loading ? 'Loading...' : 'Refresh'}
        </button>
      </header>

      <div className="date-filters">
        <label htmlFor="date-range">Date Range:</label>
        <select 
          id="date-range"
          value={dateRange} 
          onChange={handleDateRangeChange}
        >
          <option value="">All Time</option>
          <option value="today">Today</option>
          <option value="this_week">This Week</option>
          <option value="custom">Custom Range</option>
        </select>

        {dateRange === 'custom' && (
          <div className="custom-date-inputs">
            <div className="date-input-group">
              <label htmlFor="start-date">Start Date:</label>
              <input
                type="date"
                id="start-date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>
            <div className="date-input-group">
              <label htmlFor="end-date">End Date:</label>
              <input
                type="date"
                id="end-date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
            <button onClick={fetchMetrics}>Apply</button>
          </div>
        )}
      </div>

      {metrics && (
        <>
          <div className="kpi-cards">
            <div className="kpi-card">
              <h3>Total Tasks</h3>
              <p className="kpi-value">{metrics.totalTasks}</p>
            </div>
            <div className="kpi-card">
              <h3>Completed</h3>
              <p className="kpi-value">{metrics.completedTasks}</p>
            </div>
            <div className="kpi-card">
              <h3>Completion Rate</h3>
              <p className="kpi-value">{calculateCompletionPercentage()}%</p>
            </div>
            <div className="kpi-card">
              <h3>Avg Completion Time</h3>
              <p className="kpi-value">
                {metrics.averageCompletionTimeMinutes 
                  ? `${metrics.averageCompletionTimeMinutes.toFixed(1)} min` 
                  : 'N/A'}
              </p>
            </div>
          </div>

          <div className="charts-section">
            <div className="chart-card">
              <h2>Priority Distribution</h2>
              <PieChart data={getPriorityChartData()} />
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default Dashboard
