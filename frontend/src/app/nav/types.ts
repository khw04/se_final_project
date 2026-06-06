import type { ReactNode } from 'react'

import type { AuthSession } from '../../lib/authApi'

export type NavRenderContext = {
  session: AuthSession
  navigate: (viewId: string, options?: NavOptions) => void
  options?: NavOptions
}

export type NavOptions = {
  noteId?: number
}

export type NavEntry = {
  id: string
  label: string
  section: string
  icon: string
  badge?: number
  render: (context: NavRenderContext) => ReactNode
}

export type NavSection = {
  label: string
  entries: NavEntry[]
}
