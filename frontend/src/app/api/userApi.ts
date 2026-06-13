import type { Role, UserAccount } from '../types'

import { apiFetch } from './client'

type MeResponse = {
  id?: number
  email: string
  role: string
  emailVerified?: boolean
  createdAt?: string
}

function toRole(role: string): Role {
  return role === 'ADMIN' ? 'ADMIN' : 'USER'
}

export const userApi = {
  async getMe(): Promise<UserAccount> {
    const me = await apiFetch<MeResponse>('/auth/me')
    return {
      id: me.id ?? 0,
      email: me.email,
      role: toRole(me.role),
      emailVerified: me.emailVerified ?? false,
      createdAt: me.createdAt ?? '',
    }
  },
}

export const getMe = userApi.getMe
