const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const apiHealthPlaceholder = {
  endpoint: `${apiBaseUrl}/actuator/health`,
  statusLabel: '백엔드 상태 확인 엔드포인트 대기 중',
  description:
    '백엔드 기본 구조가 준비되면 Spring Boot 상태 확인 결과를 이 카드에 표시합니다.',
} as const
