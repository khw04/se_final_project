import type { CalendarEvent } from '../types'

import { apiFetch } from './client'
import { calcDDay as calcDDayValue, relativeKo as relativeKoValue, TODAY } from './dateUtils'

export type EventRange = { from?: string; to?: string }

function toDateTimeFrom(date: string): string {
  return date.includes('T') ? date : `${date}T00:00:00+09:00`
}

function toDateTimeTo(date: string): string {
  return date.includes('T') ? date : `${date}T23:59:59+09:00`
}

export type CreateEventRequest = {
  title: string
  subjectId?: number | null
  startAt: string
  endAt?: string
  allDay: boolean
  type: string
  reminder?: string
}

export const calendarApi = {
  async getEvents(range?: EventRange): Promise<CalendarEvent[]> {
    const params = new URLSearchParams()
    if (range?.from) params.set('from', toDateTimeFrom(range.from))
    if (range?.to) params.set('to', toDateTimeTo(range.to))
    const query = params.toString() ? `?${params.toString()}` : ''
    return apiFetch<CalendarEvent[]>(`/events${query}`)
  },

  async createEvent(req: CreateEventRequest): Promise<CalendarEvent> {
    return apiFetch<CalendarEvent>('/events', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    })
  },

  async deleteEvent(id: number): Promise<void> {
    await apiFetch(`/events/${id}`, { method: 'DELETE' })
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
