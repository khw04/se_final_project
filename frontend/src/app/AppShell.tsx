import { useState } from 'react'

import { type AuthSession, clearAuthSession } from '../lib/authApi'

import { AppHeader } from './components/AppHeader'
import { Sidebar } from './components/Sidebar'
import { findNavEntry } from './nav/registry'
import type { NavOptions } from './nav/types'

import './AppShell.css'

type AppShellProps = {
  session: AuthSession
}

type ViewState = {
  id: string
  options?: NavOptions
}

export function AppShell({ session }: AppShellProps) {
  const [view, setView] = useState<ViewState>({ id: 'dashboard' })
  const activeEntry = findNavEntry(view.id)

  function navigate(viewId: string, options?: NavOptions) {
    setView({ id: viewId, options })
  }

  function handleLogout() {
    clearAuthSession()
    window.dispatchEvent(new Event('storage'))
  }

  const inner = activeEntry.render({ session, navigate, options: view.options })

  return (
    <div className="app-shell">
      <AppHeader session={session} onHome={() => navigate('dashboard')} hasUnreadNotifications />
      <div className="app-layout">
        <Sidebar session={session} activeView={activeEntry.id} onSelect={navigate} onLogout={handleLogout} />
        <main className="app-main">{inner}</main>
      </div>
    </div>
  )
}
