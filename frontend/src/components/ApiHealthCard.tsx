import { apiHealthPlaceholder } from '../lib/apiHealth'

export function ApiHealthCard() {
  return (
    <aside className="api-card" aria-labelledby="api-health-title">
      <div className="api-card__eyebrow">API integration</div>
      <h2 id="api-health-title">Health status placeholder</h2>
      <p>{apiHealthPlaceholder.description}</p>
      <dl>
        <div>
          <dt>Expected endpoint</dt>
          <dd>{apiHealthPlaceholder.endpoint}</dd>
        </div>
        <div>
          <dt>Current state</dt>
          <dd>{apiHealthPlaceholder.statusLabel}</dd>
        </div>
      </dl>
    </aside>
  )
}
