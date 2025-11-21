import { useState } from 'react'
import CreateTaskForm from './components/CreateTaskForm'
import TaskList from './components/TaskList'
import './App.css'

function App() {
  const [activeView, setActiveView] = useState('list')

  return (
    <div className="app">
      <nav className="nav-tabs">
        <button
          className={`nav-tab ${activeView === 'list' ? 'active' : ''}`}
          onClick={() => setActiveView('list')}
        >
          Task List
        </button>
        <button
          className={`nav-tab ${activeView === 'create' ? 'active' : ''}`}
          onClick={() => setActiveView('create')}
        >
          Create Task
        </button>
      </nav>
      <div className="content">
        {activeView === 'list' ? <TaskList /> : <CreateTaskForm />}
      </div>
    </div>
  )
}

export default App
