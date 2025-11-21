import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import App from './App'

// Mock the child components
vi.mock('./components/TaskList', () => ({
  default: () => <div data-testid="task-list">Task List Component</div>
}))

vi.mock('./components/CreateTaskForm', () => ({
  default: () => <div data-testid="create-task-form">Create Task Form Component</div>
}))

describe('App', () => {
  beforeEach(() => {
    global.fetch = vi.fn()
  })

  it('renders navigation tabs', () => {
    render(<App />)
    
    expect(screen.getByRole('button', { name: /task list/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /create task/i })).toBeInTheDocument()
  })

  it('shows TaskList component by default', () => {
    render(<App />)
    
    expect(screen.getByTestId('task-list')).toBeInTheDocument()
    expect(screen.queryByTestId('create-task-form')).not.toBeInTheDocument()
  })

  it('switches to CreateTaskForm when create task tab is clicked', async () => {
    const user = userEvent.setup()
    render(<App />)
    
    const createTabButton = screen.getByRole('button', { name: /create task/i })
    await user.click(createTabButton)
    
    expect(screen.getByTestId('create-task-form')).toBeInTheDocument()
    expect(screen.queryByTestId('task-list')).not.toBeInTheDocument()
  })

  it('switches back to TaskList when task list tab is clicked', async () => {
    const user = userEvent.setup()
    render(<App />)
    
    // Click create task tab
    await user.click(screen.getByRole('button', { name: /create task/i }))
    expect(screen.getByTestId('create-task-form')).toBeInTheDocument()
    
    // Click task list tab
    await user.click(screen.getByRole('button', { name: /task list/i }))
    expect(screen.getByTestId('task-list')).toBeInTheDocument()
    expect(screen.queryByTestId('create-task-form')).not.toBeInTheDocument()
  })

  it('applies active class to current tab', async () => {
    const user = userEvent.setup()
    render(<App />)
    
    const listTab = screen.getByRole('button', { name: /task list/i })
    const createTab = screen.getByRole('button', { name: /create task/i })
    
    // Initially task list tab should be active
    expect(listTab).toHaveClass('active')
    expect(createTab).not.toHaveClass('active')
    
    // After clicking create task tab
    await user.click(createTab)
    expect(createTab).toHaveClass('active')
    expect(listTab).not.toHaveClass('active')
  })
})
