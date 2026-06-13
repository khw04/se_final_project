import { afterEach, describe, expect, it, vi } from 'vitest'

import { apiHealthEndpoint, fetchApiHealth } from './apiHealth'

describe('apiHealthEndpoint', () => {
  it('points at the backend /health endpoint', () => {
    expect(apiHealthEndpoint).toMatch(/\/health$/)
  })
})

describe('fetchApiHealth', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('returns ok state when the backend responds UP', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ status: 'UP', timestamp: '2026-06-10T00:00:00Z' }),
      }),
    )

    const result = await fetchApiHealth()

    expect(result.state).toBe('ok')
    expect(result.status).toBe('UP')
    expect(result.checkedAt).toBe('2026-06-10T00:00:00Z')
  })

  it('throws when the backend responds with an error status', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 503, json: async () => ({}) }))

    await expect(fetchApiHealth()).rejects.toThrow()
  })
})
