import { type FormEvent, useEffect, useState } from 'react'

import { PasswordResetScreen } from '../app/screens/auth/PasswordResetScreen'
import type { AuthSession, UserResponse } from '../lib/authApi'

import {
  clearAuthSession,
  clearOAuthQueryParams,
  consumeOAuthRedirect,
  formatAuthError,
  getCurrentUser,
  loadAuthSession,
  loginUser,
  loginWithOAuth,
  registerUser,
  saveAuthSession,
  startOAuthLogin,
} from '../lib/authApi'

type AuthMode = 'login' | 'register'
type MessageTone = 'success' | 'error' | 'muted'

type AuthMessage = {
  tone: MessageTone
  text: string
}

const MOCK_PREFILL = import.meta.env.VITE_POKEMO_MOCK_SESSION === 'true'

export function AuthCard() {
  const [mode, setMode] = useState<AuthMode>('login')
  const [email, setEmail] = useState(MOCK_PREFILL ? 'student@pokemo.dev' : '')
  const [password, setPassword] = useState(MOCK_PREFILL ? 'mock-demo-password' : '')
  const [session, setSession] = useState<AuthSession | null>(() => loadAuthSession())
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null)
  const [message, setMessage] = useState<AuthMessage | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isCheckingUser, setIsCheckingUser] = useState(Boolean(session))
  const [showReset, setShowReset] = useState(false)

  useEffect(() => {
    let isMounted = true

    if (!session?.accessToken) {
      Promise.resolve().then(() => {
        if (!isMounted) {
          return
        }

        setCurrentUser(null)
        setIsCheckingUser(false)
      })

      return () => {
        isMounted = false
      }
    }

    Promise.resolve().then(() => {
      if (isMounted) {
        setIsCheckingUser(true)
      }
    })
    getCurrentUser(session.accessToken, session.tokenType || 'Bearer')
      .then((user) => {
        if (!isMounted) {
          return
        }

        setCurrentUser(user)
        setMessage({ tone: 'success', text: `${user.email} 계정으로 로그인되었습니다.` })
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        clearAuthSession()
        setSession(null)
        setCurrentUser(null)
        setMessage({ tone: 'error', text: `세션 확인에 실패했습니다: ${formatAuthError(error)}` })
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

  useEffect(() => {
    const callback = consumeOAuthRedirect()

    if (!callback) {
      return
    }

    let isMounted = true
    clearOAuthQueryParams()

    Promise.resolve().then(() => {
      if (isMounted) {
        setIsSubmitting(true)
        setMessage({ tone: 'muted', text: '소셜 로그인을 처리하는 중입니다...' })
      }
    })

    loginWithOAuth(callback.provider, callback.code, callback.redirectUri)
      .then((nextSession) => {
        if (!isMounted) {
          return
        }

        saveAuthSession(nextSession)
        setSession(nextSession)
        setCurrentUser({ email: nextSession.email, role: nextSession.role })
        setMessage({ tone: 'success', text: '소셜 로그인되었습니다. 프로필을 확인하는 중입니다.' })
        window.dispatchEvent(new Event('storage'))
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setMessage({ tone: 'error', text: `소셜 로그인에 실패했습니다: ${formatAuthError(error)}` })
      })
      .finally(() => {
        if (isMounted) {
          setIsSubmitting(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [])

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
    setMessage({ tone: 'muted', text: '이 기기에서 로그아웃되었습니다.' })
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
        setMessage({ tone: 'success', text: `${user.email} 계정이 생성되었습니다. 이제 로그인할 수 있습니다.` })
        return
      }

      const nextSession = await loginUser({ email, password })
      saveAuthSession(nextSession)
      setSession(nextSession)
      setCurrentUser({ email: nextSession.email, role: nextSession.role })
      setPassword('')
      setMessage({ tone: 'success', text: '로그인되었습니다. 프로필을 확인하는 중입니다.' })
    } catch (error) {
      setMessage({ tone: 'error', text: formatAuthError(error) })
    } finally {
      setIsSubmitting(false)
    }
  }

  const isLoggedIn = Boolean(session)
  const submitLabel = isSubmitting ? '처리 중...' : mode === 'login' ? '로그인' : '계정 만들기'

  return (
    <aside className="auth-card" aria-labelledby="auth-card-title">
      <div className="auth-card__eyebrow">계정 접속</div>
      <div className="auth-card__heading-row">
        <h2 id="auth-card-title">{mode === 'login' ? 'Pokemo 로그인' : 'Pokemo 계정 만들기'}</h2>
        {isLoggedIn ? (
          <button className="auth-card__ghost-button" type="button" onClick={handleLogout}>
            로그아웃
          </button>
        ) : null}
      </div>

      {session ? (
        <div className="auth-card__session" aria-live="polite">
          <p className="auth-card__session-label">현재 사용자</p>
          <p className="auth-card__session-email">{currentUser?.email ?? session.email}</p>
          <p className="auth-card__session-role">{currentUser?.role ?? session.role}</p>
          {isCheckingUser ? <p className="auth-card__hint">/api/auth/me에서 프로필을 새로 불러오는 중...</p> : null}
        </div>
      ) : showReset ? (
        <PasswordResetScreen onBack={() => setShowReset(false)} />
      ) : (
        <>
          <div className="auth-card__mode-toggle" role="tablist" aria-label="인증 모드">
            <button
              aria-selected={mode === 'login'}
              role="tab"
              type="button"
              onClick={() => switchMode('login')}
            >
              로그인
            </button>
            <button
              aria-selected={mode === 'register'}
              role="tab"
              type="button"
              onClick={() => switchMode('register')}
            >
              회원가입
            </button>
          </div>

          <form className="auth-card__form" onSubmit={handleSubmit}>
            <label>
              <span>이메일</span>
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
              <span>비밀번호</span>
              <input
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                minLength={mode === 'register' ? 8 : 6}
                name="password"
                onChange={(event) => setPassword(event.target.value)}
                placeholder={mode === 'register' ? '비밀번호 8자 이상' : '비밀번호를 입력하세요'}
                required
                type="password"
                value={password}
              />
            </label>
            <button className="auth-card__submit" disabled={isSubmitting} type="submit">
              {submitLabel}
            </button>
          </form>

          <div className="auth-card__social">
            <p className="auth-card__social-divider">
              <span>또는 소셜 계정으로</span>
            </p>
            <button
              className="auth-card__social-button auth-card__social-button--google"
              disabled={isSubmitting}
              onClick={() => startOAuthLogin('google')}
              type="button"
            >
              구글로 로그인
            </button>
            <button
              className="auth-card__social-button auth-card__social-button--kakao"
              disabled={isSubmitting}
              onClick={() => startOAuthLogin('kakao')}
              type="button"
            >
              카카오로 로그인
            </button>
          </div>

          {mode === 'login' ? (
            <button
              type="button"
              className="auth-card__ghost-button"
              style={{ marginTop: 12, alignSelf: 'flex-start' }}
              onClick={() => {
                setShowReset(true)
                setMessage(null)
              }}
            >
              비밀번호를 잊으셨나요?
            </button>
          ) : null}
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
