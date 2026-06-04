import type { NoteSummary, Quiz, RecommendPayload } from '../types'

import { apiFetch } from './client'

export const aiApi = {
  async getRecommend(): Promise<RecommendPayload> {
    return apiFetch<RecommendPayload>('/api/recommend')
  },

  async summarizeNote(noteId: number): Promise<NoteSummary> {
    return apiFetch<NoteSummary>('/api/ai/summary', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ noteId }),
    })
  },

  async generateQuiz(noteId: number, count = 5): Promise<Quiz> {
    return apiFetch<Quiz>('/api/quiz/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ noteId, count }),
    })
  },
}

export const getRecommend = aiApi.getRecommend
export const summarizeNote = aiApi.summarizeNote
export const generateQuiz = aiApi.generateQuiz
