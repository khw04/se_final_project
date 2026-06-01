import type { Notice } from '../types'

import { delay, iso } from './dateUtils'
import { MOCK_NOTICES } from './mockData'

export const noticeApi = {
  async getNotices(): Promise<Notice[]> {
    await delay(70)
    return [...MOCK_NOTICES].sort((a, b) => Number(b.pinned) - Number(a.pinned) || b.id - a.id)
  },

  async createNotice(payload: Pick<Notice, 'title' | 'body' | 'tag' | 'pinned'>): Promise<Notice> {
    await delay(120)
    const next: Notice = {
      ...payload,
      id: MOCK_NOTICES.length + 10,
      viewCount: 0,
      author: '운영팀',
      createdAt: iso(Date.now()),
      updatedAt: iso(Date.now()),
    }
    MOCK_NOTICES.unshift(next)
    return next
  },
}

export const getNotices = noticeApi.getNotices
export const createNotice = noticeApi.createNotice
