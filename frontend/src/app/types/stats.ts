import type { CalendarEvent } from './calendar'
import type { UserAccount } from './common'

export type WeakConcept = {
  concept: string
  subjectId: number | null
  missCount: number
  totalAttempts: number
  relatedKeywords: string[]
}

export type UpcomingSubject = {
  subjectId: number
  dDay: number
  accuracy: number | null
  priorityScore: number
}

export type PriorityRecommendation = {
  rank: number
  subjectId: number
  reason: string
  dDay?: number
  accuracy: number | null
  tone: 'urgent' | 'warning' | 'normal'
}

export type WeeklyStudyPoint = {
  weekday: number
  weekdayLabel: string
  studyMinutes: number
  studySeconds?: number
}

export type SubjectProgress = {
  subjectId: number
  accuracy: number
  attempted: number
  total: number
}

export type AccuracyTrendPoint = {
  attemptedAt: string
  accuracy: number
}

export type DashboardPayload = {
  user: UserAccount
  upcomingExams: CalendarEvent[]
  weeklyStudy: WeeklyStudyPoint[]
  subjectProgress: SubjectProgress[]
  accuracyTrend: AccuracyTrendPoint[]
  recommendation: PriorityRecommendation[]
}
