import type { PushSubscriptionPayload } from '../../lib/push'

import { apiFetch } from './client'

export const pushApi = {
  async getPublicKey(): Promise<string> {
    const response = await apiFetch<{ publicKey: string }>('/push/public-key')
    return response.publicKey
  },

  async subscribe(payload: PushSubscriptionPayload): Promise<void> {
    await apiFetch<null>('/push/subscribe', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  },

  async unsubscribe(endpoint: string): Promise<void> {
    await apiFetch<null>('/push/subscribe', {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ endpoint }),
    })
  },
}
