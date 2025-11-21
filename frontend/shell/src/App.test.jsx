import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import App from './App'

describe('App', () => {
  it('renders the app header', () => {
    render(<App />)
    expect(screen.getByText('Field Service Management')).toBeInTheDocument()
  })

  it('renders the task management iframe', () => {
    render(<App />)
    const iframe = screen.getByTitle('Task Management')
    expect(iframe).toBeInTheDocument()
    expect(iframe).toHaveAttribute('src', 'http://localhost:3001')
  })
})
