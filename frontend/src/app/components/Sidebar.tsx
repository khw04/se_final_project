import { useEffect, useState } from 'react'
import type { AuthSession } from '../../lib/authApi'

import { quizApi } from '../api/quizApi'
import { navSections } from '../nav/registry'

import { Icon } from './Icon'

type SidebarProps = {
  session: AuthSession
  activeView: string
  onSelect: (view: string) => void
  onLogout: () => void
}

export function Sidebar({ session, activeView, onSelect, onLogout }: SidebarProps) {
  const [wrongCount, setWrongCount] = useState(0)

  useEffect(() => {
    let alive = true
    const refresh = () => quizApi
      .getWrongAnswers()
      .then((items) => {
        if (alive) setWrongCount(items.length)
      })
      .catch(() => {
        if (alive) setWrongCount(0)
      })
    refresh()
    window.addEventListener('pokemo:wrong-answers-updated', refresh)
    return () => {
      alive = false
      window.removeEventListener('pokemo:wrong-answers-updated', refresh)
    }
  }, [session.email])

  return (
    <aside className="sidebar" aria-label="Workspace navigation">
      <div className="sidebar__user">
        <p className="sidebar__user-label">{session.role === 'ADMIN' ? '관리자' : '현재 사용자'}</p>
        <p className="sidebar__user-email">{session.email}</p>
      </div>

      {navSections.map((section) => (
        <div key={section.label}>
          <p className="sidebar__section-label">{section.label}</p>
          <nav className="sidebar__nav" aria-label={section.label}>
            {section.entries.map((item) => (
              <button
                key={item.id}
                type="button"
                aria-current={activeView === item.id ? 'page' : undefined}
                onClick={() => onSelect(item.id)}
              >
                <Icon name={item.icon} size={18} />
                <span>{item.label}</span>
                {item.id === 'wrong' && wrongCount > 0 ? <span className="sidebar__nav-badge">{wrongCount}</span> : null}
                {item.id !== 'wrong' && item.badge ? <span className="sidebar__nav-badge">{item.badge}</span> : null}
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
