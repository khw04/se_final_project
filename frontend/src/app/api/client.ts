export const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/+$/, '')

const authStorageKey = 'pokemo.auth'

type StoredSession = {
  accessToken: string
  refreshToken: string
  tokenType: string
  email: string
  role: string
}

export class ApiError extends Error {
  status: number
  body: unknown

  constructor(message: string, status: number, body: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
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

function getErrorMessage(body: unknown, status: number) {
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

function readSession() {
  try {
    const storedSession = window.localStorage.getItem(authStorageKey)

    if (!storedSession) {
      return null
    }

    const session: unknown = JSON.parse(storedSession)

    if (!session || typeof session !== 'object') {
      return null
    }

    const candidate = session as Record<string, unknown>

    if (
      typeof candidate.accessToken !== 'string' ||
      typeof candidate.refreshToken !== 'string' ||
      typeof candidate.tokenType !== 'string' ||
      typeof candidate.email !== 'string' ||
      typeof candidate.role !== 'string'
    ) {
      return null
    }

    return candidate as StoredSession
  } catch {
    return null
  }
}

function saveSession(session: StoredSession) {
  window.localStorage.setItem(authStorageKey, JSON.stringify(session))
  window.dispatchEvent(new Event('storage'))
}

function clearSession() {
  window.localStorage.removeItem(authStorageKey)
  window.dispatchEvent(new Event('storage'))
}

function buildUrl(path: string) {
  if (/^https?:\/\//i.test(path)) {
    return path
  }

  return `${apiBaseUrl}${path.startsWith('/') ? path : `/${path}`}`
}

export async function apiFetch<TResponse = unknown>(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers)
  const session = readSession()

  if (session?.accessToken && !headers.has('Authorization')) {
    headers.set('Authorization', `${session.tokenType || 'Bearer'} ${session.accessToken}`)
  }

  let response = await fetch(buildUrl(path), {
    ...init,
    headers,
  })

  if (response.status === 401 && session?.refreshToken && path !== '/auth/refresh') {
    const refreshed = await refreshSession(session)
    if (refreshed) {
      const retryHeaders = new Headers(init.headers)
      retryHeaders.set('Authorization', `${refreshed.tokenType || 'Bearer'} ${refreshed.accessToken}`)
      response = await fetch(buildUrl(path), {
        ...init,
        headers: retryHeaders,
      })
    }
  }

  const body = await parseResponseBody(response)

  if (!response.ok) {
    throw new ApiError(getErrorMessage(body, response.status), response.status, body)
  }

  return body as TResponse
}

async function refreshSession(session: StoredSession) {
  const response = await fetch(buildUrl('/auth/refresh'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken: session.refreshToken }),
  })
  const body = await parseResponseBody(response)

  if (!response.ok || !body || typeof body !== 'object') {
    clearSession()
    return null
  }

  const payload = body as Record<string, unknown>
  if (
    typeof payload.accessToken !== 'string' ||
    typeof payload.refreshToken !== 'string' ||
    typeof payload.tokenType !== 'string'
  ) {
    clearSession()
    return null
  }

  const refreshed: StoredSession = {
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    tokenType: payload.tokenType,
    email: typeof payload.email === 'string' ? payload.email : session.email,
    role: typeof payload.role === 'string' ? payload.role : session.role,
  }
  saveSession(refreshed)
  return refreshed
}
