import type { Subject } from '../types'

import { apiFetch } from './client'

let subjectCache: Subject[] = []

type CreateSubjectInput = {
  name: string
  color: string
}

type UpdateSubjectInput = CreateSubjectInput

export const subjectApi = {
  async getSubjects(): Promise<Subject[]> {
    const data = await apiFetch<Subject[]>('/subjects')
    subjectCache = data
    return data
  },

  async createSubject(input: CreateSubjectInput): Promise<Subject> {
    const subject = await apiFetch<Subject>('/subjects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    })
    subjectCache = [...subjectCache, subject]
    return subject
  },

  async updateSubject(id: number, input: UpdateSubjectInput): Promise<Subject> {
    const subject = await apiFetch<Subject>(`/subjects/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    })
    subjectCache = subjectCache.map((item) => (item.id === id ? subject : item))
    return subject
  },

  async deleteSubject(id: number): Promise<void> {
    await apiFetch<void>(`/subjects/${id}`, { method: 'DELETE' })
    subjectCache = subjectCache.filter((subject) => subject.id !== id)
  },

  subjectById(id: number): Subject | undefined {
    return subjectCache.find((s) => s.id === id)
  },
}

export const createSubject = subjectApi.createSubject
export const getSubjects = subjectApi.getSubjects
export const subjectById = subjectApi.subjectById
