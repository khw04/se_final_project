import { useEffect } from 'react'

import { AppShell } from './app/AppShell'
import { useAuthSession } from './app/useAuthSession'
import { HomePage } from './pages/HomePage'
import { appRoutes } from './routes'
import './App.css'

function App() {
  const session = useAuthSession()

  useEffect(() => {
    if (!session) {
      window.scrollTo({ top: 0, left: 0 })
    }
  }, [session])

  if (session) {
    return <AppShell session={session} />
  }

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
