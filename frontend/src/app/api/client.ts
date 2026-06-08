export const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/+$/, '')

const authStorageKey = 'pokemo.auth'

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

function readAccessToken() {
  try {
    const storedSession = window.localStorage.getItem(authStorageKey)

    if (!storedSession) {
      return null
    }

    const session: unknown = JSON.parse(storedSession)

    if (!session || typeof session !== 'object') {
      return null
    }

    const accessToken = (session as Record<string, unknown>).accessToken

    return typeof accessToken === 'string' && accessToken ? accessToken : null
  } catch {
    return null
  }
}

function buildUrl(path: string) {
  if (/^https?:\/\//i.test(path)) {
    return path
  }

  return `${apiBaseUrl}${path.startsWith('/') ? path : `/${path}`}`
}

export async function apiFetch<TResponse = unknown>(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers)
  const accessToken = readAccessToken()

  if (accessToken && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(buildUrl(path), {
    ...init,
    headers,
  })
  const body = await parseResponseBody(response)

  if (!response.ok) {
    throw new ApiError(getErrorMessage(body, response.status), response.status, body)
  }

  return body as TResponse
}
