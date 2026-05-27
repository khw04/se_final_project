import { useEffect, useState } from 'react'

import { loadAuthSession, type AuthSession } from '../lib/authApi'

const POLL_INTERVAL_MS = 500

export function useAuthSession() {
  const [session, setSession] = useState<AuthSession | null>(() => loadAuthSession())

  useEffect(() => {
    const sync = () => {
      const next = loadAuthSession()
      setSession((prev) => {
        if (prev === next) return prev
        if (prev && next && prev.accessToken === next.accessToken && prev.email === next.email) {
          return prev
        }
        return next
      })
    }

    const interval = window.setInterval(sync, POLL_INTERVAL_MS)
    window.addEventListener('storage', sync)
    window.addEventListener('focus', sync)

    return () => {
      window.clearInterval(interval)
      window.removeEventListener('storage', sync)
      window.removeEventListener('focus', sync)
    }
  }, [])

  return session
}
