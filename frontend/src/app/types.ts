export type ViewId =
  | 'dashboard'
  | 'calendar'
  | 'notes'
  | 'quiz'
  | 'wrong'
  | 'recommend'
  | 'notices'

export type Role = 'USER' | 'ADMIN'

export type UserAccount = {
  id: number
  email: string
  role: Role
  emailVerified: boolean
  provider?: 'google' | 'kakao'
  createdAt: string
}

export type Subject = {
  id: number
  name: string
  color: string
}

export type Tag = {
  id: number
  name: string
}

export type CalendarEventType = 'exam' | 'assignment' | 'lecture' | 'other'

export type RecurrenceRule = {
  freq: 'daily' | 'weekly' | 'monthly' | 'custom'
  interval: number
  byweekday?: number[]
  endMode: 'never' | 'count' | 'until'
  endCount?: number
  endUntil?: string
}

export type CalendarEvent = {
  id: number
  title: string
  subjectId: number
  startAt: string
  endAt?: string
  allDay: boolean
  type: CalendarEventType
  recurrence?: RecurrenceRule
  reminder?: '15m' | '1h' | '1d'
  dDay: number
}

export type Attachment = {
  id: number
  name: string
  mimeType: string
  size: number
  url: string
  uploadedAt: string
}

export type Note = {
  id: number
  title: string
  subjectId: number
  content: string
  tagIds: number[]
  attachmentIds: number[]
  preview: string
  updatedAt: string
  createdAt: string
}

export type QuestionType = 'mcq' | 'short' | 'ox'

export type Question = {
  id: number
  type: QuestionType
  text: string
  choices?: string[]
  correctIndex?: number
  correctText?: string
  correctBool?: boolean
  explanation: string
  difficulty: 'easy' | 'medium' | 'hard'
  subjectId: number
  conceptTags: string[]
}

export type Quiz = {
  id: number
  title: string
  subjectId: number
  questionIds: number[]
  questions?: Question[]
  generatedFromNoteId?: number
  generatedBy?: 'manual' | 'ai-gpt' | 'ai-gemini'
  createdAt: string
}

export type Answer = {
  questionId: number
  userAnswer: string | number | boolean
  correct: boolean
  timeSpentSec: number
}

export type WrongAnswerNote = {
  questionId: number
  question: Question
  missCount: number
  lastAttemptId: number
  lastMissedAt: string
  concept: string
}

export type WeakConcept = {
  concept: string
  subjectId: number
  missCount: number
  totalAttempts: number
  relatedKeywords: string[]
}

export type UpcomingSubject = {
  subjectId: number
  dDay: number
  accuracy: number
  priorityScore: number
}

export type PriorityRecommendation = {
  rank: number
  subjectId: number
  reason: string
  dDay?: number
  accuracy: number
  tone: 'urgent' | 'warning' | 'normal'
}

export type NoteSummary = {
  noteId: number
  sourceCharCount: number
  summaryCharCount: number
  bullets: string[]
  generatedAt: string
  fallback?: boolean
}

export type WeeklyStudyPoint = {
  weekday: number
  weekdayLabel: string
  studyMinutes: number
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

export type Notice = {
  id: number
  title: string
  body: string
  tag: '공지' | '점검' | '약관' | '베타' | '런칭'
  author: string
  pinned: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
}

export type RecommendPayload = {
  priorities: PriorityRecommendation[]
  weakConcepts: WeakConcept[]
  upcomingSubjects: UpcomingSubject[]
}

export type SearchHits = {
  notes: Note[]
  quizzes: Quiz[]
  events: CalendarEvent[]
}
