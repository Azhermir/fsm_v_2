import { useState } from 'react'
import CreateTaskForm from './components/CreateTaskForm'
import TaskList from './components/TaskList'
import TechnicianMap from './components/TechnicianMap'
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
        <button
          className={`nav-tab ${activeView === 'map' ? 'active' : ''}`}
          onClick={() => setActiveView('map')}
        >
          Technician Map
        </button>
      </nav>
      <div className="content">
        {activeView === 'list' && <TaskList />}
        {activeView === 'create' && <CreateTaskForm />}
        {activeView === 'map' && <TechnicianMap />}
      </div>
    </div>
  )
}

export default App
