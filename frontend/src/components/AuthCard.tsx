import { type FormEvent, useEffect, useState } from 'react'

import type { AuthSession, UserResponse } from '../lib/authApi'

import {
  clearAuthSession,
  formatAuthError,
  getCurrentUser,
  loadAuthSession,
  loginUser,
  registerUser,
  saveAuthSession,
} from '../lib/authApi'

type AuthMode = 'login' | 'register'
type MessageTone = 'success' | 'error' | 'muted'

type AuthMessage = {
  tone: MessageTone
  text: string
}

export function AuthCard() {
  const [mode, setMode] = useState<AuthMode>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [session, setSession] = useState<AuthSession | null>(() => loadAuthSession())
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null)
  const [message, setMessage] = useState<AuthMessage | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isCheckingUser, setIsCheckingUser] = useState(Boolean(session))

  useEffect(() => {
    if (!session?.accessToken) {
      setCurrentUser(null)
      setIsCheckingUser(false)
      return
    }

    let isMounted = true

    setIsCheckingUser(true)
    getCurrentUser(session.accessToken, session.tokenType || 'Bearer')
      .then((user) => {
        if (!isMounted) {
          return
        }

        setCurrentUser(user)
        setMessage({ tone: 'success', text: `Signed in as ${user.email}.` })
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        clearAuthSession()
        setSession(null)
        setCurrentUser(null)
        setMessage({ tone: 'error', text: `Session check failed: ${formatAuthError(error)}` })
      })
      .finally(() => {
        if (isMounted) {
          setIsCheckingUser(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [session])

  function switchMode(nextMode: AuthMode) {
    setMode(nextMode)
    setPassword('')
    setMessage(null)
  }

  function handleLogout() {
    clearAuthSession()
    setSession(null)
    setCurrentUser(null)
    setPassword('')
    setMessage({ tone: 'muted', text: 'You have been logged out on this device.' })
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)
    setMessage(null)

    try {
      if (mode === 'register') {
        const user = await registerUser({ email, password })
        setMode('login')
        setPassword('')
        setMessage({ tone: 'success', text: `Account created for ${user.email}. You can log in now.` })
        return
      }

      const nextSession = await loginUser({ email, password })
      saveAuthSession(nextSession)
      setSession(nextSession)
      setCurrentUser({ email: nextSession.email, role: nextSession.role })
      setPassword('')
      setMessage({ tone: 'success', text: 'Login successful. Checking your profile now.' })
    } catch (error) {
      setMessage({ tone: 'error', text: formatAuthError(error) })
    } finally {
      setIsSubmitting(false)
    }
  }

  const isLoggedIn = Boolean(session)
  const submitLabel = isSubmitting ? 'Working...' : mode === 'login' ? 'Log in' : 'Create account'

  return (
    <aside className="auth-card" aria-labelledby="auth-card-title">
      <div className="auth-card__eyebrow">Account access</div>
      <div className="auth-card__heading-row">
        <h2 id="auth-card-title">{mode === 'login' ? 'Log in to Pokemo' : 'Create your Pokemo account'}</h2>
        {isLoggedIn ? (
          <button className="auth-card__ghost-button" type="button" onClick={handleLogout}>
            Log out
          </button>
        ) : null}
      </div>

      {session ? (
        <div className="auth-card__session" aria-live="polite">
          <p className="auth-card__session-label">Current user</p>
          <p className="auth-card__session-email">{currentUser?.email ?? session.email}</p>
          <p className="auth-card__session-role">{currentUser?.role ?? session.role}</p>
          {isCheckingUser ? <p className="auth-card__hint">Refreshing profile from /api/auth/me...</p> : null}
        </div>
      ) : (
        <>
          <div className="auth-card__mode-toggle" role="tablist" aria-label="Authentication mode">
            <button
              aria-selected={mode === 'login'}
              role="tab"
              type="button"
              onClick={() => switchMode('login')}
            >
              Login
            </button>
            <button
              aria-selected={mode === 'register'}
              role="tab"
              type="button"
              onClick={() => switchMode('register')}
            >
              Register
            </button>
          </div>

          <form className="auth-card__form" onSubmit={handleSubmit}>
            <label>
              <span>Email</span>
              <input
                autoComplete="email"
                name="email"
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                required
                type="email"
                value={email}
              />
            </label>
            <label>
              <span>Password</span>
              <input
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                minLength={6}
                name="password"
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Enter your password"
                required
                type="password"
                value={password}
              />
            </label>
            <button className="auth-card__submit" disabled={isSubmitting} type="submit">
              {submitLabel}
            </button>
          </form>
        </>
      )}

      {message ? (
        <p className={`auth-card__message auth-card__message--${message.tone}`} role="status">
          {message.text}
        </p>
      ) : null}
    </aside>
  )
}
