import type { Attachment, Note } from '../types'

import { delay, iso } from './dateUtils'
import { MOCK_ATTACHMENTS, MOCK_NOTES } from './mockData'

export type NoteQuery = { subjectId?: number; tagIds?: number[]; q?: string }


export const noteApi = {
  attachmentById(id: number): Attachment | undefined {
    return MOCK_ATTACHMENTS.find((a) => a.id === id)
  },

  async getAttachments(ids: number[]): Promise<Attachment[]> {
    await delay(20)
    return MOCK_ATTACHMENTS.filter((a) => ids.includes(a.id))
  },

  async getNote(id: number): Promise<Note> {
    await delay(60)
    const note = MOCK_NOTES.find((n) => n.id === id)
    if (!note) throw new Error('NOTE_NOT_FOUND')
    return note
  },

  async getNotes(query: NoteQuery = {}): Promise<Note[]> {
    await delay(80)
    let out = [...MOCK_NOTES]
    if (query.subjectId) out = out.filter((n) => n.subjectId === query.subjectId)
    if (query.tagIds?.length) {
      const tagIds = query.tagIds
      out = out.filter((n) => tagIds.every((t) => n.tagIds.includes(t)))
    }
    if (query.q) {
      const needle = query.q.toLowerCase()
      out = out.filter(
        (n) => n.title.toLowerCase().includes(needle) || n.content.toLowerCase().includes(needle),
      )
    }
    return out.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
  },

  async patchNote(id: number, partial: Partial<Pick<Note, 'content' | 'title'>>): Promise<Note> {
    await delay(100)
    const note = MOCK_NOTES.find((n) => n.id === id)
    if (!note) throw new Error('NOTE_NOT_FOUND')
    Object.assign(note, partial, { updatedAt: iso(Date.now()) })
    return note
  },
}

export const attachmentById = noteApi.attachmentById
export const getAttachments = noteApi.getAttachments
export const getNote = noteApi.getNote
export const getNotes = noteApi.getNotes
export const patchNote = noteApi.patchNote
