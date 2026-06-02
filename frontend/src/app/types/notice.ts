export const NOTICE_TAGS = ['공지', '점검', '약관', '베타', '런칭'] as const

export type NoticeTag = (typeof NOTICE_TAGS)[number]

export type Notice = {
  id: number
  title: string
  body: string
  tag: NoticeTag
  author: string
  pinned: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
}

export type NoticeDraft = Pick<Notice, 'title' | 'body' | 'tag' | 'pinned'>
