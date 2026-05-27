import { apiHealthPlaceholder } from '../lib/apiHealth'

export function ApiHealthCard() {
  return (
    <aside className="api-card" aria-labelledby="api-health-title">
      <div className="api-card__eyebrow">API 연동</div>
      <h2 id="api-health-title">상태 확인 준비 중</h2>
      <p>{apiHealthPlaceholder.description}</p>
      <dl>
        <div>
          <dt>예상 엔드포인트</dt>
          <dd>{apiHealthPlaceholder.endpoint}</dd>
        </div>
        <div>
          <dt>현재 상태</dt>
          <dd>{apiHealthPlaceholder.statusLabel}</dd>
        </div>
      </dl>
    </aside>
  )
}
