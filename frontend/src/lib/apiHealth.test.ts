import { describe, expect, it } from 'vitest'

import { apiHealthPlaceholder } from './apiHealth'

describe('apiHealthPlaceholder', () => {
  it('documents the expected backend health endpoint', () => {
    expect(apiHealthPlaceholder.endpoint).toContain('/actuator/health')
    expect(apiHealthPlaceholder.statusLabel).toBe('Waiting for backend health endpoint')
  })
})
