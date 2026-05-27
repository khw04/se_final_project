const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/+$/, '')

const authStorageKey = 'pokemo.auth'

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
  return requestJson<UserResponse>('/api/auth/register', {
    body: JSON.stringify(credentials),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

export function loginUser(credentials: AuthCredentials) {
  return requestJson<AuthSession>('/api/auth/login', {
    body: JSON.stringify(credentials),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })
}

export function getCurrentUser(accessToken: string, tokenType = 'Bearer') {
  return requestJson<UserResponse>('/api/auth/me', {
    headers: {
      Authorization: `${tokenType} ${accessToken}`,
    },
    method: 'GET',
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
