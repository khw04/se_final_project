import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { subjectApi } from './subjectApi'

const authSession = {
  accessToken: 'access-token',
  refreshToken: 'refresh-token',
  tokenType: 'Bearer',
  email: 'learner@pokemo.test',
  role: 'USER',
}

function mockJsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), {
      headers: { 'Content-Type': 'application/json' },
      status,
    }),
  )
}

describe('subjectApi', () => {
  const fetchMock = vi.fn()

  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
    vi.stubGlobal('window', {
      localStorage: {
        getItem: vi.fn(() => JSON.stringify(authSession)),
        removeItem: vi.fn(),
        setItem: vi.fn(),
      },
      dispatchEvent: vi.fn(),
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('keeps the last fetched subjects available for remounted screens', async () => {
    const subjects = [
      { id: 1, name: '미적분', color: '#EF4444' },
      { id: 2, name: '자료구조', color: '#3B82F6' },
    ]
    fetchMock.mockReturnValueOnce(mockJsonResponse(subjects))

    await expect(subjectApi.getSubjects()).resolves.toEqual(subjects)

    expect(subjectApi.getCachedSubjects()).toEqual(subjects)
  })
})
