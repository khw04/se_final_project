import type { Subject, Tag } from '../types'

import { apiFetch } from './client'

let subjectCache: Subject[] = []
let tagCache: Tag[] = []

type CreateSubjectInput = {
  name: string
  color: string
}

type UpdateSubjectInput = CreateSubjectInput

type CreateTagInput = {
  name: string
}

export const subjectApi = {
  async getSubjects(): Promise<Subject[]> {
    const data = await apiFetch<Subject[]>('/subjects')
    subjectCache = data
    return data
  },

  getCachedSubjects(): Subject[] {
    return subjectCache
  },

  async getTags(): Promise<Tag[]> {
    const data = await apiFetch<Tag[]>('/tags')
    tagCache = data
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

  async createTag(input: CreateTagInput): Promise<Tag> {
    const tag = await apiFetch<Tag>('/tags', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input),
    })
    tagCache = [...tagCache.filter((item) => item.id !== tag.id), tag]
    return tag
  },

  async deleteTag(id: number): Promise<void> {
    await apiFetch<void>(`/tags/${id}`, { method: 'DELETE' })
    tagCache = tagCache.filter((tag) => tag.id !== id)
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

  tagById(id: number): Tag | undefined {
    return tagCache.find((t) => t.id === id)
  },
}

export const createTag = subjectApi.createTag
export const createSubject = subjectApi.createSubject
export const deleteTag = subjectApi.deleteTag
export const getTags = subjectApi.getTags
export const getSubjects = subjectApi.getSubjects
export const getCachedSubjects = subjectApi.getCachedSubjects
export const subjectById = subjectApi.subjectById
export const tagById = subjectApi.tagById
