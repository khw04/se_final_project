import type { SearchHits } from '../types'

import { delay } from './dateUtils'
import { MOCK_EVENTS, MOCK_NOTES, MOCK_QUIZZES } from './mockData'

export const statsApi = {
  async search(q: string): Promise<SearchHits> {
    await delay(100)
    const needle = q.toLowerCase()
    return {
      notes: MOCK_NOTES.filter((n) => (n.title + n.content + n.preview).toLowerCase().includes(needle)),
      quizzes: MOCK_QUIZZES.filter((qz) => qz.title.toLowerCase().includes(needle)),
      events: MOCK_EVENTS.filter((e) => e.title.toLowerCase().includes(needle)),
    }
  },
}

export const search = statsApi.search
