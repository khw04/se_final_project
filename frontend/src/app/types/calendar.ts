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
