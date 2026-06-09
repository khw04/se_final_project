import { useState } from 'react'

import type { AuthSession } from '../../lib/authApi'

import { Icon } from './Icon'

type AppHeaderProps = {
  session: AuthSession | null
  onHome: () => void
  onOpenProfile?: () => void
  onOpenNotifications?: () => void
  onLogout?: () => void
  hasUnreadNotifications?: boolean
}

export function AppHeader({
  session,
  onHome,
  onOpenProfile,
  onOpenNotifications,
  onLogout,
  hasUnreadNotifications,
}: AppHeaderProps) {
  const [profileOpen, setProfileOpen] = useState(false)
  const [notificationOpen, setNotificationOpen] = useState(false)

  return (
    <header className="app-header">
      <button type="button" className="brand-mark" onClick={onHome} aria-label="Pokemo 홈">
        Pokemo
      </button>
      <nav className="app-nav-cluster" aria-label="주요 영역">
        {session ? (
          <>
            <div className="header-menu-wrap">
              <button
                type="button"
                className="icon-btn"
                aria-label="알림"
                onClick={() => {
                  setNotificationOpen((value) => !value)
                  onOpenNotifications?.()
                }}
              >
              <Icon name="bell" size={16} />
              {hasUnreadNotifications ? <span className="icon-btn__dot" /> : null}
              </button>
              {notificationOpen ? (
                <div className="header-popover">
                  <p className="label">알림</p>
                  <p className="muted-note">새 알림이 있으면 여기에 표시됩니다.</p>
                </div>
              ) : null}
            </div>
            <div className="header-menu-wrap">
              <button
                type="button"
                className="user-pill"
                onClick={() => {
                  setProfileOpen((value) => !value)
                  onOpenProfile?.()
                }}
                aria-label="사용자 메뉴"
              >
                <span className="user-pill__avatar">{(session.email[0] || 'U').toUpperCase()}</span>
                <span>{session.email.split('@')[0]}</span>
              </button>
              {profileOpen ? (
                <div className="header-popover header-popover--profile">
                  <p className="label">계정 정보</p>
                  <strong>{session.email}</strong>
                  <span>{session.role === 'ADMIN' ? '관리자' : '일반 사용자'}</span>
                  <button type="button" className="surface__title-action" onClick={onLogout}>
                    로그아웃
                  </button>
                </div>
              ) : null}
            </div>
          </>
        ) : (
          <span className="user-pill" aria-current="page">
            <span>홈</span>
          </span>
        )}
      </nav>
    </header>
  )
}
