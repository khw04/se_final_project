import type { Subject, Tag } from '../types'

import { apiFetch } from './client'

let subjectCache: Subject[] = []
let tagCache: Tag[] = []

type CreateSubjectInput = {
  name: string
  color: string
}

export const subjectApi = {
  async getSubjects(): Promise<Subject[]> {
    const data = await apiFetch<Subject[]>('/api/subjects')
    subjectCache = data
    return data
  },

  async createSubject(input: CreateSubjectInput): Promise<Subject> {
    const subject = await apiFetch<Subject>('/api/subjects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    })
    subjectCache = [...subjectCache, subject]
    return subject
  },

  async getTags(): Promise<Tag[]> {
    const data = await apiFetch<Tag[]>('/api/tags')
    tagCache = data
    return data
  },

  subjectById(id: number): Subject | undefined {
    return subjectCache.find((s) => s.id === id)
  },

  tagById(id: number): Tag | undefined {
    return tagCache.find((t) => t.id === id)
  },
}

export const createSubject = subjectApi.createSubject
export const getSubjects = subjectApi.getSubjects
export const getTags = subjectApi.getTags
export const subjectById = subjectApi.subjectById
export const tagById = subjectApi.tagById
