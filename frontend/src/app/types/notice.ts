export type Notice = {
  id: number
  title: string
  body: string
  tag: '공지' | '점검' | '약관' | '베타' | '런칭'
  author: string
  pinned: boolean
  viewCount: number
  createdAt: string
  updatedAt: string
}
