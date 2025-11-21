import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import PieChart from './PieChart'

describe('PieChart', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    
    // Mock canvas context
    HTMLCanvasElement.prototype.getContext = vi.fn(() => ({
      clearRect: vi.fn(),
      beginPath: vi.fn(),
      moveTo: vi.fn(),
      arc: vi.fn(),
      closePath: vi.fn(),
      fill: vi.fn(),
      stroke: vi.fn(),
      fillText: vi.fn(),
      fillStyle: '',
      strokeStyle: '',
      lineWidth: 0,
      font: '',
      textAlign: '',
      textBaseline: ''
    }))
  })

  const mockData = [
    { label: 'CRITICAL', value: 5, color: '#dc3545' },
    { label: 'HIGH', value: 10, color: '#fd7e14' },
    { label: 'MEDIUM', value: 15, color: '#ffc107' },
    { label: 'LOW', value: 20, color: '#28a745' }
  ]

  it('renders canvas element', () => {
    render(<PieChart data={mockData} />)
    const canvas = screen.getByLabelText('Priority distribution pie chart')
    expect(canvas).toBeInTheDocument()
    expect(canvas.tagName).toBe('CANVAS')
  })

  it('renders legend with all items', () => {
    render(<PieChart data={mockData} />)
    expect(screen.getByText('CRITICAL: 5')).toBeInTheDocument()
    expect(screen.getByText('HIGH: 10')).toBeInTheDocument()
    expect(screen.getByText('MEDIUM: 15')).toBeInTheDocument()
    expect(screen.getByText('LOW: 20')).toBeInTheDocument()
  })

  it('renders empty state when data is empty', () => {
    render(<PieChart data={[]} />)
    const canvas = screen.getByLabelText('Priority distribution pie chart')
    expect(canvas).toBeInTheDocument()
  })

  it('renders when data has zero total', () => {
    const zeroData = [
      { label: 'CRITICAL', value: 0, color: '#dc3545' },
      { label: 'HIGH', value: 0, color: '#fd7e14' }
    ]
    render(<PieChart data={zeroData} />)
    expect(screen.getByText('CRITICAL: 0')).toBeInTheDocument()
    expect(screen.getByText('HIGH: 0')).toBeInTheDocument()
  })

  it('handles single data item', () => {
    const singleData = [{ label: 'CRITICAL', value: 100, color: '#dc3545' }]
    render(<PieChart data={singleData} />)
    expect(screen.getByText('CRITICAL: 100')).toBeInTheDocument()
  })

  it('handles null data gracefully', () => {
    render(<PieChart data={null} />)
    const canvas = screen.getByLabelText('Priority distribution pie chart')
    expect(canvas).toBeInTheDocument()
  })

  it('handles undefined data gracefully', () => {
    render(<PieChart data={undefined} />)
    const canvas = screen.getByLabelText('Priority distribution pie chart')
    expect(canvas).toBeInTheDocument()
  })

  it('applies correct colors to legend items', () => {
    const { container } = render(<PieChart data={mockData} />)
    const legendColors = container.querySelectorAll('.legend-color')
    
    expect(legendColors[0]).toHaveStyle({ backgroundColor: '#dc3545' })
    expect(legendColors[1]).toHaveStyle({ backgroundColor: '#fd7e14' })
    expect(legendColors[2]).toHaveStyle({ backgroundColor: '#ffc107' })
    expect(legendColors[3]).toHaveStyle({ backgroundColor: '#28a745' })
  })

  it('updates canvas when data changes', () => {
    const { rerender } = render(<PieChart data={mockData} />)
    expect(screen.getByText('CRITICAL: 5')).toBeInTheDocument()

    const newData = [{ label: 'CRITICAL', value: 25, color: '#dc3545' }]
    rerender(<PieChart data={newData} />)
    expect(screen.getByText('CRITICAL: 25')).toBeInTheDocument()
    expect(screen.queryByText('CRITICAL: 5')).not.toBeInTheDocument()
  })

  it('renders correct number of legend items', () => {
    const { container } = render(<PieChart data={mockData} />)
    const legendItems = container.querySelectorAll('.legend-item')
    expect(legendItems).toHaveLength(4)
  })
})
