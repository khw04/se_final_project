import type { Attachment, Note } from '../types'

import { apiBaseUrl, apiFetch } from './client'

export type NoteQuery = { subjectId?: number; tagIds?: number[]; q?: string }

type BackendNote = Omit<Note, 'tagIds'> & { tagIds?: number[] }

type BackendAttachment = {
  id: number
  noteId: number
  name: string
  mimeType: string
  size: number
  url: string
}

function toAttachment(a: BackendAttachment): Attachment {
  const urlBase = apiBaseUrl.endsWith('/api') && a.url.startsWith('/api/')
    ? apiBaseUrl.slice(0, -4)
    : apiBaseUrl
  return {
    id: a.id,
    name: a.name,
    mimeType: a.mimeType,
    size: a.size,
    url: `${urlBase}${a.url}`,
    uploadedAt: '',
  }
}

function toNote(note: BackendNote): Note {
  return {
    ...note,
    tagIds: note.tagIds ?? [],
  }
}

export const noteApi = {
  async getAttachments(ids: number[]): Promise<Attachment[]> {
    if (!ids.length) return []
    const results = await Promise.all(
      ids.map((id) => apiFetch<BackendAttachment>(`/attachments/${id}/meta`).catch(() => null))
    )
    return results.filter((a): a is BackendAttachment => a !== null).map(toAttachment)
  },

  async getNote(id: number): Promise<Note> {
    return apiFetch<BackendNote>(`/notes/${id}`).then(toNote)
  },

  async getNotes(query: NoteQuery = {}): Promise<Note[]> {
    const params = new URLSearchParams()
    if (query.subjectId) params.set('subjectId', String(query.subjectId))
    if (query.tagIds?.length) {
      query.tagIds.forEach((tagId) => {
        params.append('tagIds', String(tagId))
      })
    }
    if (query.q) params.set('q', query.q)
    const q = params.toString() ? `?${params.toString()}` : ''
    return apiFetch<BackendNote[]>(`/notes${q}`).then((notes) => notes.map(toNote))
  },

  async patchNote(id: number, partial: Partial<Pick<Note, 'content' | 'subjectId' | 'title'>>): Promise<Note> {
    return apiFetch<BackendNote>(`/notes/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(partial),
    }).then(toNote)
  },

  async createNote(title: string, subjectId?: number): Promise<Note> {
    return apiFetch<BackendNote>('/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, subjectId: subjectId ?? null, content: '' }),
    }).then(toNote)
  },

  async addTagToNote(noteId: number, tagId: number): Promise<Note> {
    return apiFetch<BackendNote>(`/notes/${noteId}/tags/${tagId}`, { method: 'POST' }).then(toNote)
  },

  async removeTagFromNote(noteId: number, tagId: number): Promise<Note> {
    return apiFetch<BackendNote>(`/notes/${noteId}/tags/${tagId}`, { method: 'DELETE' }).then(toNote)
  },

  async deleteNote(id: number): Promise<void> {
    await apiFetch<void>(`/notes/${id}`, { method: 'DELETE' })
  },

  async uploadAttachment(noteId: number, file: File): Promise<Attachment> {
    const formData = new FormData()
    formData.append('file', file)
    const data = await apiFetch<BackendAttachment>(`/notes/${noteId}/attachments`, {
      method: 'POST',
      body: formData,
    })
    return toAttachment(data)
  },

  async deleteAttachment(id: number): Promise<void> {
    await apiFetch<void>(`/attachments/${id}`, { method: 'DELETE' })
  },
}

export const getAttachments = noteApi.getAttachments
export const getNote = noteApi.getNote
export const getNotes = noteApi.getNotes
export const patchNote = noteApi.patchNote
export const addTagToNote = noteApi.addTagToNote
export const removeTagFromNote = noteApi.removeTagFromNote
