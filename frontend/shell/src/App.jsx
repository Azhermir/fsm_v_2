import './App.css'

function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>Field Service Management</h1>
      </header>
      <main className="app-main">
        <iframe
          src="http://localhost:3001"
          title="Task Management"
          className="micro-frontend"
        />
        <iframe
          src="http://localhost:3002"
          title="Analytics Dashboard"
          className="micro-frontend"
        />
      </main>
    </div>
  )
}

export default App
