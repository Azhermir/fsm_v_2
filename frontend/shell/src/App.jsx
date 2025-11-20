import './App.css'

function App() {
  return (
    <div className="shell-container">
      <header className="shell-header">
        <h1>Field Service Management</h1>
        <nav>
          <a href="#task-management">Task Management</a>
        </nav>
      </header>
      <main className="shell-content">
        <iframe
          src="http://localhost:5001"
          title="Task Management"
          className="micro-frontend-iframe"
        />
      </main>
    </div>
  )
}

export default App
