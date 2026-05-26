const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const apiHealthPlaceholder = {
  endpoint: `${apiBaseUrl}/actuator/health`,
  statusLabel: 'Waiting for backend health endpoint',
  description:
    'This card is reserved for the Spring Boot health check once the backend scaffold is available.',
} as const
