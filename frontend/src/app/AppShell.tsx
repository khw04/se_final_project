import { useState } from 'react'

import { type AuthSession, clearAuthSession } from '../lib/authApi'

import { AppHeader } from './components/AppHeader'
import { Sidebar } from './components/Sidebar'
import { findNavEntry } from './nav/registry'

import './AppShell.css'

type AppShellProps = {
  session: AuthSession
}

export function AppShell({ session }: AppShellProps) {
  const [view, setView] = useState<string>('dashboard')
  const activeEntry = findNavEntry(view)

  function handleLogout() {
    clearAuthSession()
    window.dispatchEvent(new Event('storage'))
  }

  const inner = activeEntry.render({ session, navigate: setView })

  return (
    <div className="app-shell">
      <AppHeader session={session} onHome={() => setView('dashboard')} hasUnreadNotifications />
      <div className="app-layout">
        <Sidebar session={session} activeView={activeEntry.id} onSelect={setView} onLogout={handleLogout} />
        <main className="app-main">{inner}</main>
      </div>
    </div>
  )
}
