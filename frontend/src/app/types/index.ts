import type { CalendarEvent } from './calendar'
import type { Note } from './note'
import type { Quiz } from './quiz'

export * from './ai'
export * from './calendar'
export * from './common'
export * from './note'
export * from './notice'
export * from './quiz'
export * from './stats'

export type SearchHits = {
  notes: Note[]
  quizzes: Quiz[]
  events: CalendarEvent[]
}
