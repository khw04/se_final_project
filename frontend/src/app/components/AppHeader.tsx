import type { AuthSession } from '../../lib/authApi'

import { Icon } from './Icon'

type AppHeaderProps = {
  session: AuthSession | null
  onHome: () => void
  onOpenProfile?: () => void
  onOpenNotifications?: () => void
  hasUnreadNotifications?: boolean
}

export function AppHeader({
  session,
  onHome,
  onOpenProfile,
  onOpenNotifications,
  hasUnreadNotifications,
}: AppHeaderProps) {
  return (
    <header className="app-header">
      <button type="button" className="brand-mark" onClick={onHome} aria-label="Pokemo 홈">
        Pokemo
      </button>
      <nav className="app-nav-cluster" aria-label="주요 영역">
        {session ? (
          <>
            <button type="button" className="icon-btn" aria-label="알림" onClick={onOpenNotifications}>
              <Icon name="bell" size={16} />
              {hasUnreadNotifications ? <span className="icon-btn__dot" /> : null}
            </button>
            <button type="button" className="user-pill" onClick={onOpenProfile} aria-label="사용자 메뉴">
              <span className="user-pill__avatar">{(session.email[0] || 'U').toUpperCase()}</span>
              <span>{session.email.split('@')[0]}</span>
            </button>
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
