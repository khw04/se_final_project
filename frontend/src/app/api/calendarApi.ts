import type { CalendarEvent } from '../types'

import { calcDDay as calcDDayValue, delay, relativeKo as relativeKoValue, TODAY } from './dateUtils'
import { MOCK_EVENTS } from './mockData'

export type EventRange = { from?: string; to?: string }

export const calendarApi = {
  async getEvents(range?: EventRange): Promise<CalendarEvent[]> {
    await delay(80)
    let out = [...MOCK_EVENTS]
    if (range?.from) {
      const from = range.from
      out = out.filter((e) => e.startAt >= from)
    }
    if (range?.to) {
      const to = range.to
      out = out.filter((e) => e.startAt <= to)
    }
    return out
      .map((e) => ({ ...e, dDay: calcDDayValue(e.startAt) }))
      .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())
  },

  today(): string {
    return TODAY
  },

  relativeKo: relativeKoValue,
  calcDDay: calcDDayValue,
}

export const getEvents = calendarApi.getEvents
export const calcDDay = calendarApi.calcDDay
export const relativeKo = calendarApi.relativeKo
export const today = calendarApi.today
