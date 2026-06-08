const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/+$/, '')

const authStorageKey = 'pokemo.auth'

const mockAuthMode = import.meta.env.VITE_POKEMO_MOCK_SESSION === 'true'

function buildMockSession(email: string): AuthSession {
  const normalizedEmail = email.trim() || 'student@pokemo.dev'
  const role = normalizedEmail.startsWith('admin') ? 'ADMIN' : 'USER'
  return {
    accessToken: 'mock.access.' + normalizedEmail,
    refreshToken: 'mock.refresh.' + normalizedEmail,
    tokenType: 'Bearer',
    email: normalizedEmail,
    role,
  }
}

export type AuthCredentials = {
  email: string
  password: string
}

export type AuthSession = {
  accessToken: string
  refreshToken: string
  tokenType: string
  email: string
  role: string
}

export type UserResponse = {
  id?: number | string
  email: string
  role: string
}

export class AuthApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'AuthApiError'
    this.status = status
  }
}

async function parseResponseBody(response: Response) {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

function getBackendMessage(body: unknown, status: number) {
  if (typeof body === 'string' && body.trim()) {
    return body
  }

  if (body && typeof body === 'object') {
    const payload = body as Record<string, unknown>
    const message = payload.message ?? payload.error ?? payload.status

    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }

  return `Request failed with status ${status}`
}

async function requestJson<TResponse>(path: string, init: RequestInit) {
  const response = await fetch(`${apiBaseUrl}${path}`, init)
  const body = await parseResponseBody(response)

  if (!response.ok) {
    throw new AuthApiError(getBackendMessage(body, response.status), response.status)
  }

  return body as TResponse
}

export function registerUser(credentials: AuthCredentials) {
  if (mockAuthMode) {
    const mock = buildMockSession(credentials.email)
    return Promise.resolve<UserResponse>({ email: mock.email, role: mock.role })
  }
  return requestJson<UserResponse>('/api/auth/register', {
    body: JSON.stringify(credentials),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

export function loginUser(credentials: AuthCredentials) {
  if (mockAuthMode) {
    return Promise.resolve<AuthSession>(buildMockSession(credentials.email))
  }
  return requestJson<AuthSession>('/api/auth/login', {
    body: JSON.stringify(credentials),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

export function getCurrentUser(accessToken: string, tokenType = 'Bearer') {
  if (mockAuthMode) {
    const session = loadAuthSession()
    return Promise.resolve<UserResponse>({
      email: session?.email ?? 'student@pokemo.dev',
      role: session?.role ?? 'USER',
    })
  }
  return requestJson<UserResponse>('/api/auth/me', {
    headers: {
      Authorization: `${tokenType} ${accessToken}`,
    },
    method: 'GET',
  })
}

export function requestPasswordReset(email: string) {
  if (mockAuthMode) {
    return Promise.resolve<null>(null)
  }
  return requestJson<null>('/api/auth/password/reset-request', {
    body: JSON.stringify({ email }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

export function validateResetToken(token: string) {
  if (mockAuthMode) {
    return Promise.resolve<null>(null)
  }
  return requestJson<null>('/api/auth/password/reset-validate', {
    body: JSON.stringify({ token }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

export function confirmPasswordReset(token: string, newPassword: string) {
  if (mockAuthMode) {
    return Promise.resolve<null>(null)
  }
  return requestJson<null>('/api/auth/password/reset', {
    body: JSON.stringify({ token, newPassword }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

function isAuthSession(value: unknown): value is AuthSession {
  if (!value || typeof value !== 'object') {
    return false
  }

  const session = value as Record<string, unknown>

  return (
    typeof session.accessToken === 'string' &&
    typeof session.refreshToken === 'string' &&
    typeof session.tokenType === 'string' &&
    typeof session.email === 'string' &&
    typeof session.role === 'string'
  )
}

export function loadAuthSession() {
  try {
    const storedSession = window.localStorage.getItem(authStorageKey)

    if (!storedSession) {
      return null
    }

    const parsedSession: unknown = JSON.parse(storedSession)

    return isAuthSession(parsedSession) ? parsedSession : null
  } catch {
    return null
  }
}

export function saveAuthSession(session: AuthSession) {
  window.localStorage.setItem(authStorageKey, JSON.stringify(session))
}

export function clearAuthSession() {
  window.localStorage.removeItem(authStorageKey)
}

export function formatAuthError(error: unknown) {
  if (error instanceof AuthApiError) {
    return `${error.message} (status ${error.status})`
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return 'Something went wrong. Please try again.'
}

export type OAuthProvider = 'google' | 'kakao'

const oauthStateKey = 'pokemo.oauth'

const oauthClientIds: Record<OAuthProvider, string> = {
  google: import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '',
  kakao: import.meta.env.VITE_KAKAO_CLIENT_ID ?? '',
}

function oauthRedirectUri() {
  return import.meta.env.VITE_OAUTH_REDIRECT_URI ?? window.location.origin
}

function randomState() {
  const bytes = new Uint8Array(16)
  window.crypto.getRandomValues(bytes)
  return Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('')
}

function buildAuthorizeUrl(provider: OAuthProvider, state: string, redirectUri: string) {
  if (provider === 'google') {
    const params = new URLSearchParams({
      client_id: oauthClientIds.google,
      redirect_uri: redirectUri,
      response_type: 'code',
      scope: 'openid email profile',
      state,
      prompt: 'select_account',
    })
    return `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`
  }

  const params = new URLSearchParams({
    client_id: oauthClientIds.kakao,
    redirect_uri: redirectUri,
    response_type: 'code',
    scope: 'profile_nickname',
    state,
  })
  return `https://kauth.kakao.com/oauth/authorize?${params.toString()}`
}

export function startOAuthLogin(provider: OAuthProvider) {
  const state = randomState()
  window.sessionStorage.setItem(oauthStateKey, JSON.stringify({ provider, state }))
  window.location.assign(buildAuthorizeUrl(provider, state, oauthRedirectUri()))
}

type OAuthLoginResponse = {
  email: string
  role: string
  accessToken: string
  refreshToken: string
  tokenType: string
}

export function loginWithOAuth(provider: OAuthProvider, code: string, redirectUri: string) {
  return requestJson<OAuthLoginResponse>(`/api/auth/oauth/${provider}`, {
    body: JSON.stringify({ code, redirectUri }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  }).then(
    (response): AuthSession => ({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      tokenType: response.tokenType || 'Bearer',
      email: response.email,
      role: response.role,
    }),
  )
}

export type OAuthCallback = {
  provider: OAuthProvider
  code: string
  redirectUri: string
}

export function consumeOAuthRedirect(): OAuthCallback | null {
  const params = new URLSearchParams(window.location.search)
  const code = params.get('code')
  const state = params.get('state')

  if (!code || !state) {
    return null
  }

  const stored = window.sessionStorage.getItem(oauthStateKey)
  window.sessionStorage.removeItem(oauthStateKey)

  if (!stored) {
    return null
  }

  let parsed: { provider?: OAuthProvider; state?: string }
  try {
    parsed = JSON.parse(stored) as { provider?: OAuthProvider; state?: string }
  } catch {
    return null
  }

  if (!parsed.provider || parsed.state !== state) {
    return null
  }

  return { provider: parsed.provider, code, redirectUri: oauthRedirectUri() }
}

export function clearOAuthQueryParams() {
  const url = new URL(window.location.href)
  url.search = ''
  window.history.replaceState({}, document.title, url.toString())
}
