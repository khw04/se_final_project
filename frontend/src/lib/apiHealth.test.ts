import { describe, expect, it } from 'vitest'

import { apiHealthPlaceholder } from './apiHealth'

describe('apiHealthPlaceholder', () => {
  it('documents the expected backend health endpoint', () => {
    expect(apiHealthPlaceholder.endpoint).toContain('/actuator/health')
    expect(apiHealthPlaceholder.statusLabel).toBe('백엔드 상태 확인 엔드포인트 대기 중')
  })
})
