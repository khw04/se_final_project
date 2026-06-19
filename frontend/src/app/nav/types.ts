import type { ReactNode } from 'react'

import type { AuthSession } from '../../lib/authApi'

export type StudyTimerState = {
  runningSubjectId: number | null
  startedAt: Date | null
  todayTotalSeconds: number
  subjectSeconds: Record<number, number>
  saving: boolean
  message: string | null
  start: (subjectId: number) => void
  stop: () => Promise<void>
  elapsedSeconds: () => number
  displayTotalSeconds: () => number
  displaySubjectSeconds: (subjectId: number) => number
  refreshSummary: () => Promise<void>
}

export type NavRenderContext = {
  session: AuthSession
  navigate: (viewId: string, options?: NavOptions) => void
  options?: NavOptions
  studyTimer: StudyTimerState
}

export type NavOptions = {
  noteId?: number
  quizId?: number
  subjectId?: number
}

export type NavEntry = {
  id: string
  label: string
  section: string
  icon: string
  badge?: number
  render: (context: NavRenderContext) => ReactNode
}

export type NavSection = {
  label: string
  entries: NavEntry[]
}
