import { calendarNavEntry } from '../screens/calendar/nav'
import { dashboardNavEntry } from '../screens/dashboard/nav'
import { noteNavEntry } from '../screens/note/nav'
import { noticeNavEntry } from '../screens/notice/nav'
import { quizNavEntry } from '../screens/quiz/nav'
import { recommendNavEntry } from '../screens/recommend/nav'
import { wrongNavEntry } from '../screens/wrong/nav'

import type { NavEntry, NavSection } from './types'

export const navRegistry: NavEntry[] = [
  dashboardNavEntry,
  calendarNavEntry,
  noteNavEntry,
  quizNavEntry,
  wrongNavEntry,
  recommendNavEntry,
  noticeNavEntry,
]

export const fallbackNavEntry = dashboardNavEntry

export const navSections: NavSection[] = navRegistry.reduce<NavSection[]>((sections, entry) => {
  const section = sections.find((item) => item.label === entry.section)

  if (section) {
    section.entries.push(entry)
  } else {
    sections.push({ label: entry.section, entries: [entry] })
  }

  return sections
}, [])

export function findNavEntry(viewId: string) {
  return navRegistry.find((entry) => entry.id === viewId) ?? fallbackNavEntry
}
