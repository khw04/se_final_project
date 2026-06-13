const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/+$/, '')

export const apiHealthEndpoint = `${apiBaseUrl}/health`

export type ApiHealthStatus = {
  state: 'ok' | 'down'
  status: string
  checkedAt: string
}

export async function fetchApiHealth(): Promise<ApiHealthStatus> {
  const response = await fetch(apiHealthEndpoint, { headers: { Accept: 'application/json' } })

  if (!response.ok) {
    throw new Error(`상태 확인 실패 (status ${response.status})`)
  }

  const body = (await response.json()) as { status?: string; timestamp?: string }

  return {
    state: 'ok',
    status: body.status ?? 'UP',
    checkedAt: body.timestamp ?? new Date().toISOString(),
  }
}
