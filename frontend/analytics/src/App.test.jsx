import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import App from './App'

// Mock the Dashboard component
vi.mock('./components/Dashboard', () => ({
  default: () => <div data-testid="dashboard-mock">Dashboard</div>
}))

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders without crashing', () => {
    render(<App />)
    expect(screen.getByTestId('dashboard-mock')).toBeInTheDocument()
  })

  it('renders Dashboard component', () => {
    render(<App />)
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })
})
