import { useEffect, useState } from 'react'

import { pushApi } from '../api/pushApi'
import {
  getExistingSubscription,
  isPushSupported,
  subscribeToPush,
  subscriptionToPayload,
  unsubscribeFromPush,
} from '../../lib/push'
import { Icon } from './Icon'

export function PushNotificationCard() {
  const [supported] = useState(isPushSupported())
  const [subscribed, setSubscribed] = useState(false)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!supported) return

    void getExistingSubscription().then((subscription) => {
      setSubscribed(subscription !== null)
    })
  }, [supported])

  if (!supported) {
    return null
  }

  async function handleEnable() {
    setLoading(true)
    setMessage('')

    try {
      const permission = await Notification.requestPermission()
      if (permission !== 'granted') {
        setMessage('알림 권한이 거부되었습니다. 브라우저 설정에서 알림을 허용해주세요.')
        return
      }

      const publicKey = await pushApi.getPublicKey()
      const subscription = await subscribeToPush(publicKey)
      await pushApi.subscribe(subscriptionToPayload(subscription))
      setSubscribed(true)
      setMessage('일정 알림이 활성화되었습니다.')
    } catch {
      setMessage('알림 설정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setLoading(false)
    }
  }

  async function handleDisable() {
    setLoading(true)
    setMessage('')

    try {
      const subscription = await unsubscribeFromPush()
      if (subscription) {
        await pushApi.unsubscribe(subscription.endpoint)
      }
      setSubscribed(false)
      setMessage('일정 알림이 해제되었습니다.')
    } catch {
      setMessage('알림 해제 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="surface dashboard-card">
      <div className="surface__title">
        <h2>
          <Icon name="bell" size={18} style={{ marginRight: 8 }} />
          일정 알림
        </h2>
        <span className={`tag ${subscribed ? 'tag--accent' : ''}`}>{subscribed ? '켜짐' : '꺼짐'}</span>
      </div>
      <p className="muted-note" style={{ margin: '0 0 12px' }}>
        시험·과제 일정에 설정한 알림 시점이 되면 브라우저 푸시 알림을 보내드립니다.
      </p>
      {subscribed ? (
        <button type="button" disabled={loading} onClick={() => void handleDisable()}>
          {loading ? '처리 중...' : '알림 끄기'}
        </button>
      ) : (
        <button type="button" disabled={loading} onClick={() => void handleEnable()}>
          {loading ? '처리 중...' : '알림 켜기'}
        </button>
      )}
      {message ? <p className="muted-note" style={{ marginTop: 12 }}>{message}</p> : null}
    </section>
  )
}
