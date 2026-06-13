import { useEffect, useState } from 'react'

import { apiHealthEndpoint, fetchApiHealth, type ApiHealthStatus } from '../lib/apiHealth'

type CardState =
  | { phase: 'loading' }
  | { phase: 'ok'; data: ApiHealthStatus }
  | { phase: 'down' }

export function ApiHealthCard() {
  const [state, setState] = useState<CardState>({ phase: 'loading' })

  useEffect(() => {
    let active = true

    fetchApiHealth()
      .then((data) => {
        if (active) setState({ phase: 'ok', data })
      })
      .catch(() => {
        if (active) setState({ phase: 'down' })
      })

    return () => {
      active = false
    }
  }, [])

  const statusLabel =
    state.phase === 'loading'
      ? '상태 확인 중...'
      : state.phase === 'ok'
        ? `정상 (${state.data.status})`
        : '응답 없음'

  return (
    <aside className="api-card" aria-labelledby="api-health-title">
      <div className="api-card__eyebrow">API 연동</div>
      <h2 id="api-health-title">백엔드 상태 확인</h2>
      <p>
        {state.phase === 'down'
          ? '백엔드에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요.'
          : 'Spring Boot 백엔드의 실시간 상태를 확인합니다.'}
      </p>
      <dl>
        <div>
          <dt>엔드포인트</dt>
          <dd>{apiHealthEndpoint}</dd>
        </div>
        <div>
          <dt>현재 상태</dt>
          <dd>{statusLabel}</dd>
        </div>
      </dl>
    </aside>
  )
}
