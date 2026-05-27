import type { AuthSession } from '../../lib/authApi'
import type { ViewId } from '../types'

import { Icon } from './Icon'

type NavItem = {
  id: ViewId
  label: string
  icon: string
  badge?: number
}

type NavSection = {
  label: string
  items: NavItem[]
}

export const NAV_SECTIONS: NavSection[] = [
  {
    label: '학습',
    items: [
      { id: 'dashboard', label: '대시보드', icon: 'dashboard' },
      { id: 'calendar', label: '캘린더', icon: 'calendar' },
      { id: 'notes', label: '노트', icon: 'book' },
      { id: 'quiz', label: '퀴즈', icon: 'brain' },
      { id: 'wrong', label: '오답노트', icon: 'x', badge: 2 },
    ],
  },
  {
    label: '인사이트',
    items: [{ id: 'recommend', label: 'AI 추천', icon: 'sparkle' }],
  },
  {
    label: '운영',
    items: [{ id: 'notices', label: '공지사항', icon: 'bell' }],
  },
]

type SidebarProps = {
  session: AuthSession
  activeView: ViewId
  onSelect: (view: ViewId) => void
  onLogout: () => void
}

export function Sidebar({ session, activeView, onSelect, onLogout }: SidebarProps) {
  return (
    <aside className="sidebar" aria-label="Workspace navigation">
      <div className="sidebar__user">
        <p className="sidebar__user-label">{session.role === 'ADMIN' ? '관리자' : '현재 사용자'}</p>
        <p className="sidebar__user-email">{session.email}</p>
      </div>

      {NAV_SECTIONS.map((section) => (
        <div key={section.label}>
          <p className="sidebar__section-label">{section.label}</p>
          <nav className="sidebar__nav" aria-label={section.label}>
            {section.items.map((item) => (
              <button
                key={item.id}
                type="button"
                aria-current={activeView === item.id ? 'page' : undefined}
                onClick={() => onSelect(item.id)}
              >
                <Icon name={item.icon} size={18} />
                <span>{item.label}</span>
                {item.badge ? <span className="sidebar__nav-badge">{item.badge}</span> : null}
              </button>
            ))}
          </nav>
        </div>
      ))}

      <button type="button" className="sidebar__logout" onClick={onLogout}>
        로그아웃
      </button>
    </aside>
  )
}
