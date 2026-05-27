import { useState } from 'react'

import { clearAuthSession, type AuthSession } from '../lib/authApi'

import { AppHeader } from './components/AppHeader'
import { Sidebar } from './components/Sidebar'
import { CalendarScreen } from './screens/CalendarScreen'
import { DashboardScreen } from './screens/DashboardScreen'
import { NotesScreen } from './screens/NotesScreen'
import { NoticesScreen } from './screens/NoticesScreen'
import { QuizScreen } from './screens/QuizScreen'
import { RecommendScreen } from './screens/RecommendScreen'
import { WrongAnswersScreen } from './screens/WrongAnswersScreen'
import type { ViewId } from './types'

import './AppShell.css'

type AppShellProps = {
  session: AuthSession
}

export function AppShell({ session }: AppShellProps) {
  const [view, setView] = useState<ViewId>('dashboard')

  function handleLogout() {
    clearAuthSession()
    window.dispatchEvent(new Event('storage'))
  }

  let inner
  if (view === 'calendar') inner = <CalendarScreen />
  else if (view === 'notes') inner = <NotesScreen />
  else if (view === 'quiz') inner = <QuizScreen />
  else if (view === 'wrong') inner = <WrongAnswersScreen />
  else if (view === 'recommend') inner = <RecommendScreen />
  else if (view === 'notices') inner = <NoticesScreen session={session} />
  else inner = <DashboardScreen session={session} onJumpTo={setView} />

  return (
    <div className="app-shell">
      <AppHeader session={session} onHome={() => setView('dashboard')} hasUnreadNotifications />
      <div className="app-layout">
        <Sidebar session={session} activeView={view} onSelect={setView} onLogout={handleLogout} />
        <main className="app-main">{inner}</main>
      </div>
    </div>
  )
}
