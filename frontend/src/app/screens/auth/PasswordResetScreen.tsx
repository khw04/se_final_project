import { type FormEvent, useState } from 'react'

import type { AuthSession } from '../../../lib/authApi'

import {
  confirmPasswordReset,
  formatAuthError,
  requestPasswordReset,
  validateResetToken,
} from '../../../lib/authApi'

type Step = 'request' | 'reset' | 'done'
type Feedback = { tone: 'success' | 'error' | 'muted'; text: string }

export function PasswordResetScreen({ session, onBack }: { session?: AuthSession; onBack?: () => void }) {
  const [step, setStep] = useState<Step>('request')
  const [email, setEmail] = useState(session?.email ?? '')
  const [token, setToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [feedback, setFeedback] = useState<Feedback | null>(null)
  const [busy, setBusy] = useState(false)

  async function submitRequest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setFeedback(null)
    try {
      await requestPasswordReset(email)
      setStep('reset')
      setFeedback({
        tone: 'success',
        text: '재설정 안내를 전송했습니다. 메일(개발 모드는 서버 콘솔 로그)의 토큰을 입력하세요.',
      })
    } catch (error) {
      setFeedback({ tone: 'error', text: formatAuthError(error) })
    } finally {
      setBusy(false)
    }
  }

  async function checkToken() {
    if (!token.trim()) {
      setFeedback({ tone: 'error', text: '토큰을 입력하세요.' })
      return
    }
    setBusy(true)
    try {
      await validateResetToken(token.trim())
      setFeedback({ tone: 'success', text: '사용 가능한 토큰입니다.' })
    } catch (error) {
      setFeedback({ tone: 'error', text: formatAuthError(error) })
    } finally {
      setBusy(false)
    }
  }

  async function submitReset(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (newPassword.length < 8) {
      setFeedback({ tone: 'error', text: '비밀번호는 8자 이상이어야 합니다.' })
      return
    }
    if (newPassword !== confirmPassword) {
      setFeedback({ tone: 'error', text: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }
    setBusy(true)
    setFeedback(null)
    try {
      await confirmPasswordReset(token.trim(), newPassword)
      setStep('done')
      setFeedback({ tone: 'success', text: '비밀번호가 변경되었습니다. 새 비밀번호로 로그인하세요.' })
    } catch (error) {
      setFeedback({ tone: 'error', text: formatAuthError(error) })
    } finally {
      setBusy(false)
    }
  }

  function restart() {
    setStep('request')
    setToken('')
    setNewPassword('')
    setConfirmPassword('')
    setFeedback(null)
  }

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">계정</p>
        <h1 className="screen__heading">비밀번호 재설정</h1>
        <p className="screen__lede">
          가입한 이메일로 재설정 토큰을 받은 뒤, 토큰과 새 비밀번호를 입력해 변경합니다.
        </p>
        {onBack ? (
          <button type="button" className="surface__title-action" onClick={onBack} style={{ marginTop: 8 }}>
            ← 로그인으로 돌아가기
          </button>
        ) : null}
      </header>

      {feedback ? (
        <p
          role="status"
          style={{
            margin: 0,
            padding: '10px 14px',
            borderRadius: 10,
            fontSize: 14,
            background:
              feedback.tone === 'error'
                ? 'rgba(239,68,68,0.12)'
                : feedback.tone === 'success'
                  ? 'rgba(34,197,94,0.12)'
                  : 'rgba(148,163,184,0.16)',
            color: feedback.tone === 'error' ? '#b91c1c' : feedback.tone === 'success' ? '#15803d' : 'var(--color-muted)',
          }}
        >
          {feedback.text}
        </p>
      ) : null}

      {step === 'request' ? (
        <section className="surface" style={{ maxWidth: 480 }}>
          <div className="surface__title">
            <h2>1단계 · 재설정 요청</h2>
          </div>
          <form onSubmit={submitRequest} style={{ display: 'grid', gap: 12 }}>
            <label style={{ display: 'grid', gap: 6 }}>
              <span className="label">이메일</span>
              <input
                className="short-input"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                required
              />
            </label>
            <button
              type="submit"
              className="auth-card__submit"
              style={{ padding: '10px 16px', fontSize: 14 }}
              disabled={busy}
            >
              {busy ? '전송 중...' : '재설정 토큰 받기'}
            </button>
          </form>
        </section>
      ) : null}

      {step === 'reset' ? (
        <section className="surface" style={{ maxWidth: 480 }}>
          <div className="surface__title">
            <h2>2단계 · 새 비밀번호 설정</h2>
          </div>
          <form onSubmit={submitReset} style={{ display: 'grid', gap: 12 }}>
            <label style={{ display: 'grid', gap: 6 }}>
              <span className="label">재설정 토큰</span>
              <div style={{ display: 'flex', gap: 8 }}>
                <input
                  className="short-input"
                  style={{ flex: 1 }}
                  value={token}
                  onChange={(event) => setToken(event.target.value)}
                  placeholder="메일/콘솔의 토큰"
                  required
                />
                <button
                  type="button"
                  className="surface__title-action"
                  onClick={checkToken}
                  disabled={busy}
                >
                  토큰 확인
                </button>
              </div>
            </label>
            <label style={{ display: 'grid', gap: 6 }}>
              <span className="label">새 비밀번호 (8자 이상)</span>
              <input
                className="short-input"
                type="password"
                autoComplete="new-password"
                minLength={8}
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
                required
              />
            </label>
            <label style={{ display: 'grid', gap: 6 }}>
              <span className="label">새 비밀번호 확인</span>
              <input
                className="short-input"
                type="password"
                autoComplete="new-password"
                minLength={8}
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                required
              />
            </label>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'space-between' }}>
              <button type="button" className="surface__title-action" onClick={restart} disabled={busy}>
                이메일 다시 입력
              </button>
              <button
                type="submit"
                className="auth-card__submit"
                style={{ padding: '10px 16px', fontSize: 14 }}
                disabled={busy}
              >
                {busy ? '변경 중...' : '비밀번호 변경'}
              </button>
            </div>
          </form>
        </section>
      ) : null}

      {step === 'done' ? (
        <section className="surface" style={{ maxWidth: 480 }}>
          <div className="surface__title">
            <h2>완료</h2>
          </div>
          <p style={{ color: 'var(--color-text)', fontSize: 14, lineHeight: 1.7 }}>
            비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 다시 로그인해 주세요.
          </p>
          <button
            type="button"
            className="surface__title-action"
            onClick={restart}
            style={{ marginTop: 12 }}
          >
            처음으로
          </button>
        </section>
      ) : null}
    </div>
  )
}
