import './App.css'

const TASK_MANAGEMENT_URL = import.meta.env.VITE_TASK_MANAGEMENT_URL || 'http://localhost:3001'
const ANALYTICS_URL = import.meta.env.VITE_ANALYTICS_URL || 'http://localhost:3002'

function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>Field Service Management</h1>
      </header>
      <main className="app-main">
        <iframe
          src={TASK_MANAGEMENT_URL}
          title="Task Management"
          className="micro-frontend"
        />
        <iframe
          src={ANALYTICS_URL}
          title="Analytics Dashboard"
          className="micro-frontend"
        />
      </main>
    </div>
  )
}

export default App
