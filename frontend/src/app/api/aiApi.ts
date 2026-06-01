import type { NoteSummary, RecommendPayload } from '../types'

import { delay, iso } from './dateUtils'
import { MOCK_PRIORITY, MOCK_SUMMARY, MOCK_UPCOMING_SUBJECTS, MOCK_WEAK_CONCEPTS } from './mockData'

export const aiApi = {
  async getRecommend(): Promise<RecommendPayload> {
    await delay(150)
    return {
      priorities: MOCK_PRIORITY,
      weakConcepts: MOCK_WEAK_CONCEPTS,
      upcomingSubjects: MOCK_UPCOMING_SUBJECTS,
    }
  },

  async summarizeNote(noteId: number): Promise<NoteSummary> {
    await delay(900)
    return { ...MOCK_SUMMARY, noteId, generatedAt: iso(Date.now()) }
  },
}

export const getRecommend = aiApi.getRecommend
export const summarizeNote = aiApi.summarizeNote
