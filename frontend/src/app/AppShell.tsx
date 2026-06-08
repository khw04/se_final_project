import { useEffect, useState } from 'react'

import { type AuthSession, clearAuthSession } from '../lib/authApi'

import { studyApi } from './api/studyApi'
import { AppHeader } from './components/AppHeader'
import { Sidebar } from './components/Sidebar'
import { findNavEntry } from './nav/registry'
import type { NavOptions, StudyTimerState } from './nav/types'

import './AppShell.css'

type AppShellProps = {
  session: AuthSession
}

type ViewState = {
  id: string
  options?: NavOptions
}

function todayKey() {
  return new Date().toLocaleDateString('en-CA', { timeZone: 'Asia/Seoul' })
}

function todayStartMs() {
  return new Date(`${todayKey()}T00:00:00+09:00`).getTime()
}

export function AppShell({ session }: AppShellProps) {
  const [view, setView] = useState<ViewState>({ id: 'dashboard' })
  const [runningSubjectId, setRunningSubjectId] = useState<number | null>(null)
  const [startedAt, setStartedAt] = useState<Date | null>(null)
  const [now, setNow] = useState(() => new Date())
  const [todayTotalSeconds, setTodayTotalSeconds] = useState(0)
  const [subjectSeconds, setSubjectSeconds] = useState<Record<number, number>>({})
  const [saving, setSaving] = useState(false)
  const [timerMessage, setTimerMessage] = useState<string | null>(null)
  const [activeDay, setActiveDay] = useState(todayKey())
  const activeEntry = findNavEntry(view.id)

  async function refreshStudySummary() {
    const summary = await studyApi.getTodaySummary()
    setTodayTotalSeconds(summary.totalSeconds)
    setSubjectSeconds(Object.fromEntries(summary.subjects.map((item) => [item.subjectId, item.studySeconds])))
  }

  useEffect(() => {
    Promise.resolve()
      .then(refreshStudySummary)
      .catch(() => {
        setTodayTotalSeconds(0)
        setSubjectSeconds({})
      })
  }, [session.email])

  useEffect(() => {
    if (runningSubjectId === null) return undefined
    const timer = window.setInterval(() => setNow(new Date()), 1000)
    return () => window.clearInterval(timer)
  }, [runningSubjectId])

  useEffect(() => {
    const timer = window.setInterval(() => {
      const nextDay = todayKey()
      if (nextDay !== activeDay) {
        setActiveDay(nextDay)
        setTodayTotalSeconds(0)
        setSubjectSeconds({})
        refreshStudySummary().catch(() => undefined)
      }
    }, 30_000)
    return () => window.clearInterval(timer)
  }, [activeDay])

  function elapsedSeconds() {
    if (!startedAt) return 0
    const effectiveStart = Math.max(startedAt.getTime(), todayStartMs())
    return Math.max(0, Math.floor((now.getTime() - effectiveStart) / 1000))
  }

  function displayTotalSeconds() {
    return todayTotalSeconds + elapsedSeconds()
  }

  function displaySubjectSeconds(subjectId: number) {
    return (subjectSeconds[subjectId] ?? 0) + (runningSubjectId === subjectId ? elapsedSeconds() : 0)
  }

  function startStudyTimer(subjectId: number) {
    setRunningSubjectId(subjectId)
    setStartedAt(new Date())
    setNow(new Date())
    setTimerMessage(null)
  }

  async function stopStudyTimer() {
    if (runningSubjectId === null || !startedAt) return
    const subjectId = runningSubjectId
    const endedAt = new Date()
    setSaving(true)
    setTimerMessage(null)
    try {
      await studyApi.createSession({
        subjectId,
        startedAt: startedAt.toISOString(),
        endedAt: endedAt.toISOString(),
      })
      setRunningSubjectId(null)
      setStartedAt(null)
      setNow(new Date())
      await refreshStudySummary()
      setTimerMessage('공부 시간이 주간 그래프에 반영되었습니다.')
    } catch (error) {
      setTimerMessage(error instanceof Error ? error.message : '공부 시간을 저장하지 못했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const studyTimer: StudyTimerState = {
    runningSubjectId,
    startedAt,
    todayTotalSeconds,
    subjectSeconds,
    saving,
    message: timerMessage,
    start: startStudyTimer,
    stop: stopStudyTimer,
    elapsedSeconds,
    displayTotalSeconds,
    displaySubjectSeconds,
    refreshSummary: refreshStudySummary,
  }

  function navigate(viewId: string, options?: NavOptions) {
    setView({ id: viewId, options })
  }

  function handleLogout() {
    clearAuthSession()
    window.dispatchEvent(new Event('storage'))
  }

  const inner = activeEntry.render({ session, navigate, options: view.options, studyTimer })

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
