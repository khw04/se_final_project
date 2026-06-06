import type { Attachment, Note } from '../types'

import { apiBaseUrl, apiFetch } from './client'

export type NoteQuery = { subjectId?: number; tagIds?: number[]; q?: string }

type BackendNote = Note

type BackendAttachment = {
  id: number
  noteId: number
  name: string
  mimeType: string
  size: number
  url: string
}

function toAttachment(a: BackendAttachment): Attachment {
  return {
    id: a.id,
    name: a.name,
    mimeType: a.mimeType,
    size: a.size,
    url: `${apiBaseUrl}${a.url}`,
    uploadedAt: '',
  }
}

export const noteApi = {
  attachmentById(): Attachment | undefined {
    return undefined
  },

  async getAttachments(ids: number[]): Promise<Attachment[]> {
    if (!ids.length) return []
    const results = await Promise.all(
      ids.map((id) => apiFetch<BackendAttachment>(`/api/attachments/${id}`).catch(() => null))
    )
    return results.filter((a): a is BackendAttachment => a !== null).map(toAttachment)
  },

  async getNote(id: number): Promise<Note> {
    return apiFetch<BackendNote>(`/api/notes/${id}`)
  },

  async getNotes(query: NoteQuery = {}): Promise<Note[]> {
    const params = new URLSearchParams()
    if (query.subjectId) params.set('subjectId', String(query.subjectId))
    if (query.tagIds?.length) {
      query.tagIds.forEach((t) => {
        params.append('tagIds', String(t))
      })
    }
    if (query.q) params.set('q', query.q)
    const q = params.toString() ? `?${params.toString()}` : ''
    return apiFetch<BackendNote[]>(`/api/notes${q}`)
  },

  async patchNote(id: number, partial: Partial<Pick<Note, 'content' | 'subjectId' | 'title'>>): Promise<Note> {
    return apiFetch<BackendNote>(`/api/notes/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(partial),
    })
  },

  async createNote(title: string, subjectId?: number): Promise<Note> {
    return apiFetch<BackendNote>('/api/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, subjectId: subjectId ?? null, content: '' }),
    })
  },

  async addTagToNote(noteId: number, tagId: number): Promise<Note> {
    return apiFetch<BackendNote>(`/api/notes/${noteId}/tags/${tagId}`, { method: 'POST' })
  },

  async removeTagFromNote(noteId: number, tagId: number): Promise<Note> {
    return apiFetch<BackendNote>(`/api/notes/${noteId}/tags/${tagId}`, { method: 'DELETE' })
  },

  async uploadAttachment(noteId: number, file: File): Promise<Attachment> {
    const formData = new FormData()
    formData.append('file', file)
    const data = await apiFetch<BackendAttachment>(`/api/notes/${noteId}/attachments`, {
      method: 'POST',
      body: formData,
    })
    return toAttachment(data)
  },
}

export const attachmentById = noteApi.attachmentById
export const getAttachments = noteApi.getAttachments
export const getNote = noteApi.getNote
export const getNotes = noteApi.getNotes
export const patchNote = noteApi.patchNote
