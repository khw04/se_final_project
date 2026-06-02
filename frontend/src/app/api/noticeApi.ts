import type { Notice, NoticeDraft } from '../types'

import { apiFetch } from './client'

const jsonHeaders = { 'Content-Type': 'application/json' }

export const noticeApi = {
  getNotices(): Promise<Notice[]> {
    return apiFetch<Notice[]>('/api/notices')
  },

  getNotice(id: number): Promise<Notice> {
    return apiFetch<Notice>(`/api/notices/${id}`)
  },

  createNotice(payload: NoticeDraft): Promise<Notice> {
    return apiFetch<Notice>('/api/notices', {
      method: 'POST',
      headers: jsonHeaders,
      body: JSON.stringify(payload),
    })
  },

  updateNotice(id: number, payload: NoticeDraft): Promise<Notice> {
    return apiFetch<Notice>(`/api/notices/${id}`, {
      method: 'PUT',
      headers: jsonHeaders,
      body: JSON.stringify(payload),
    })
  },

  deleteNotice(id: number): Promise<void> {
    return apiFetch<void>(`/api/notices/${id}`, { method: 'DELETE' })
  },
}

export const getNotices = noticeApi.getNotices
export const getNotice = noticeApi.getNotice
export const createNotice = noticeApi.createNotice
export const updateNotice = noticeApi.updateNotice
export const deleteNotice = noticeApi.deleteNotice
