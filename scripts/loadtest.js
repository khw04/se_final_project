// Pokemo 부하 테스트 스크립트 (k6)
//
// 목적: 일반 API 응답 3초 이내 목표와 300~1,000명 초기 사용자 규모의 동시 부하를
//       운영/스테이징 환경에서 재현 검증한다.
//
// 사용법:
//   1) k6 설치: https://k6.io/docs/get-started/installation/
//   2) 공개 엔드포인트만 측정:
//        k6 run -e BASE_URL=https://pokemo.duckdns.org scripts/loadtest.js
//   3) 인증 포함 측정(로그인 토큰 주입):
//        k6 run -e BASE_URL=https://pokemo.duckdns.org -e TOKEN=<accessToken> scripts/loadtest.js
//   4) 동시 사용자 수 조정:
//        k6 run -e BASE_URL=... -e VUS=300 -e DURATION=2m scripts/loadtest.js
//
// 주의: 운영 RDS/EC2에 직접 부하를 가하므로 사전 합의된 시간대에만 실행한다.

import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'
const TOKEN = __ENV.TOKEN || ''
const VUS = Number(__ENV.VUS || 300)
const DURATION = __ENV.DURATION || '1m'

export const options = {
  scenarios: {
    ramping_users: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: Math.ceil(VUS / 3) },
        { duration: '30s', target: VUS },
        { duration: DURATION, target: VUS },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // 요구사항: 일반 API 응답 3초 이내. p95 기준으로 검증한다.
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.01'],
  },
}

const authHeaders = TOKEN ? { headers: { Authorization: `Bearer ${TOKEN}` } } : {}

export default function () {
  // 공개 엔드포인트 (인증 불필요)
  const health = http.get(`${BASE_URL}/api/health`)
  check(health, { 'health 200': (r) => r.status === 200 })

  const notices = http.get(`${BASE_URL}/api/notices`)
  check(notices, { 'notices 200': (r) => r.status === 200 })

  // 인증 토큰이 주어지면 로그인 사용자 기준 일반 API도 함께 측정
  if (TOKEN) {
    const subjects = http.get(`${BASE_URL}/api/subjects`, authHeaders)
    check(subjects, { 'subjects 200': (r) => r.status === 200 })

    const notes = http.get(`${BASE_URL}/api/notes?page=0&size=20`, authHeaders)
    check(notes, { 'notes 200': (r) => r.status === 200 })
  }

  sleep(1)
}
