import { apiFetch } from './client'

export type StudySessionRequest = {
  subjectId: number
  startedAt: string
  endedAt: string
}

export type StudySessionResponse = {
  id: number
  subjectId: number
  startedAt: string
  endedAt: string
  studySeconds: number
}

export type TodayStudySummary = {
  totalSeconds: number
  subjects: Array<{
    subjectId: number
    studySeconds: number
  }>
}

export const studyApi = {
  async getTodaySummary(): Promise<TodayStudySummary> {
    return apiFetch<TodayStudySummary>('/study-sessions/today')
  },

  async createSession(input: StudySessionRequest): Promise<StudySessionResponse> {
    return apiFetch<StudySessionResponse>('/study-sessions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    })
  },
}

export const createStudySession = studyApi.createSession
