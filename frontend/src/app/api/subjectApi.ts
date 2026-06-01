import type { Subject, Tag } from '../types'

import { delay } from './dateUtils'
import { MOCK_SUBJECTS, MOCK_TAGS } from './mockData'

export const subjectApi = {
  async getSubjects(): Promise<Subject[]> {
    await delay(40)
    return MOCK_SUBJECTS
  },

  async getTags(): Promise<Tag[]> {
    await delay(30)
    return MOCK_TAGS
  },

  subjectById(id: number): Subject | undefined {
    return MOCK_SUBJECTS.find((s) => s.id === id)
  },

  tagById(id: number): Tag | undefined {
    return MOCK_TAGS.find((t) => t.id === id)
  },
}

export const getSubjects = subjectApi.getSubjects
export const getTags = subjectApi.getTags
export const subjectById = subjectApi.subjectById
export const tagById = subjectApi.tagById
