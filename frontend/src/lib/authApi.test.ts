import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import type { AuthApiError } from './authApi'

import {
  clearAuthSession,
  getCurrentUser,
  loadAuthSession,
  loginUser,
  registerUser,
  saveAuthSession,
} from './authApi'

const authSession = {
  accessToken: 'access-token',
  refreshToken: 'refresh-token',
  tokenType: 'Bearer',
  email: 'learner@pokemo.test',
  role: 'STUDENT',
}

function mockJsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), {
      headers: { 'Content-Type': 'application/json' },
      status,
    }),
  )
}

describe('authApi', () => {
  const fetchMock = vi.fn()

  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('registers with email and password', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ email: authSession.email, role: authSession.role }), { status: 201 }),
    )

    await expect(registerUser({ email: authSession.email, password: 'secret12' })).resolves.toEqual({
      email: authSession.email,
      role: authSession.role,
    })
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/auth/register', {
      body: JSON.stringify({ email: authSession.email, password: 'secret12' }),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'POST',
    })
  })

  it('logs in and returns the auth session payload', async () => {
    fetchMock.mockReturnValueOnce(mockJsonResponse(authSession))

    await expect(loginUser({ email: authSession.email, password: 'secret12' })).resolves.toEqual(authSession)
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/auth/login', {
      body: JSON.stringify({ email: authSession.email, password: 'secret12' }),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'POST',
    })
  })

  it('loads the current user with the bearer access token', async () => {
    fetchMock.mockReturnValueOnce(mockJsonResponse({ email: authSession.email, role: authSession.role }))

    await expect(getCurrentUser(authSession.accessToken, authSession.tokenType)).resolves.toEqual({
      email: authSession.email,
      role: authSession.role,
    })
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/auth/me', {
      headers: {
        Authorization: 'Bearer access-token',
      },
      method: 'GET',
    })
  })

  it('surfaces backend error messages with status codes', async () => {
    fetchMock.mockReturnValueOnce(mockJsonResponse({ message: 'Invalid credentials' }, 401))

    await expect(loginUser({ email: authSession.email, password: 'bad-password' })).rejects.toMatchObject({
      message: 'Invalid credentials',
      status: 401,
    } satisfies Partial<AuthApiError>)
  })

  it('saves, loads, and clears auth session state', () => {
    const storedValues = new Map<string, string>()
    const localStorage = {
      getItem: vi.fn((key: string) => storedValues.get(key) ?? null),
      removeItem: vi.fn((key: string) => storedValues.delete(key)),
      setItem: vi.fn((key: string, value: string) => storedValues.set(key, value)),
    }

    vi.stubGlobal('window', { localStorage })

    saveAuthSession(authSession)

    expect(loadAuthSession()).toEqual(authSession)

    clearAuthSession()

    expect(loadAuthSession()).toBeNull()
  })
})
