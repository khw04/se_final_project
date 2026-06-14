export type ViewId = string

export type Role = 'USER' | 'ADMIN'

export type UserAccount = {
  id: number
  email: string
  role: Role
  emailVerified: boolean
  provider?: 'google' | 'kakao'
  createdAt: string
}

export type Subject = {
  id: number
  name: string
  color: string
}

export type Tag = {
  id: number
  name: string
}

export type Attachment = {
  id: number
  name: string
  mimeType: string
  size: number
  url: string
  uploadedAt: string
}
