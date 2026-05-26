import { HomePage } from './pages/HomePage'
import { appRoutes } from './routes'
import './App.css'

function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <a className="brand-mark" href={appRoutes.home.path} aria-label="Pokemo home">
          Pokemo
        </a>
        <nav className="app-nav" aria-label="Primary navigation">
          <a href={appRoutes.home.path}>{appRoutes.home.label}</a>
        </nav>
      </header>
      <main>
        <HomePage />
      </main>
    </div>
  )
}

export default App
