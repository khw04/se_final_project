import type { PriorityRecommendation, UpcomingSubject, WeakConcept } from './stats'

export type NoteSummary = {
  noteId: number
  sourceCharCount: number
  summaryCharCount: number
  bullets: string[]
  generatedAt: string
  fallback?: boolean
}

export type RecommendPayload = {
  priorities: PriorityRecommendation[]
  weakConcepts: WeakConcept[]
  upcomingSubjects: UpcomingSubject[]
}
