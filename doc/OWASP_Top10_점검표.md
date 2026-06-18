# Pokemo OWASP Top 10 간단 점검표

## 점검 범위

본 문서는 Pokemo MVP 제출/시연 범위에서 OWASP Top 10 항목을 기준으로 현재 적용된 방어와 남은 보강점을 정리한다. 정식 모의해킹이나 ASVS 수준 검증은 수행하지 않았으며, 코드/설정/수동 smoke 확인 기반의 간단 점검표다.

| OWASP 항목 | 현재 대응 | 상태 | 후속 보강 |
| --- | --- | --- | --- |
| A01 Broken Access Control | JWT role claim, Spring Security `USER`/`ADMIN` 권한 분리, `/api/admin/**` 및 공지 쓰기 API ADMIN 제한, 사용자별 노트/일정/퀴즈 접근 검증, 과목 사용자별 분리 및 공부 세션 과목 소유권 검증 | 대응 | 관리자 작업 감사 로그 강화 |
| A02 Cryptographic Failures | BCrypt 비밀번호 해시, 비밀번호 해시 `Credential` 분리 저장, HTTPS 적용, 운영 secret 환경변수 관리, RDS `VERIFY_IDENTITY` 기본화 + CA truststore 절차 | 부분 대응 | RDS at-rest 암호화 활성화 확인, 필드 단위 암호화 검토 |
| A03 Injection | Spring Data JPA Repository 중심 데이터 접근, 노트 검색 파라미터 바인딩 회귀 테스트 | 대응 | 복잡한 native query 추가 시 보안 리뷰 필요 |
| A04 Insecure Design | 3-Tier 구조, 인증/AI/스토리지/알림 계층 분리, 비밀번호 자격 증명 `Credential` 분리, 과목 사용자별 분리, 후속 과제 문서화 | 부분 대응 | 위협 모델링, `User`/`Member`/`Admin` 상속 계층은 enum 기반으로 단순화(의도적 생략) |
| A05 Security Misconfiguration | CORS 허용 origin 환경변수화, CSP/Referrer-Policy/Permissions-Policy/X-Frame-Options/HSTS 설정, Swagger 경로 인지 | 부분 대응 | 운영 Swagger 접근 제한 검토, CSP `unsafe-inline` 제거 검토 |
| A06 Vulnerable and Outdated Components | Maven/npm 기반 의존성 관리, CI backend test/frontend lint/build | 부분 대응 | 정기 `npm audit`, Maven dependency check, Dependabot 도입 |
| A07 Identification and Authentication Failures | 이메일 인증 로그인 차단, Access/Refresh Token 분리, Refresh Token rotation/revoke, bcrypt, 단기 Access Token TTL | 부분 대응 | (운영 전환 시) localStorage 대신 HttpOnly Cookie/BFF |
| A08 Software and Data Integrity Failures | GitHub Actions CI/CD, EC2 pull/rebuild/restart 자동화, `.env` 미커밋 정책 | 부분 대응 | 배포 artifact 서명, branch protection, secret rotation 강화 |
| A09 Security Logging and Monitoring Failures | 로그인 실패, 미인증 로그인 차단, JWT 검증 실패 로그 기록, 감사 로그 이메일 PII 마스킹 | 부분 대응 | 관리자 작업 감사 로그, 알림/모니터링 대시보드 추가 |
| A10 Server-Side Request Forgery | 외부 호출은 Gemini/OAuth/SMTP/S3 SDK 중심이며 사용자 입력 URL fetch 기능 없음 | 대응 | 사용자 제공 URL 처리 기능 추가 시 allowlist 검토 |

## 현재 보안상 주요 한계

- 브라우저 토큰 저장은 MVP 범위에서 `localStorage` 기반을 유지하기로 결정했다. header 기반 stateless 인증이라 CSRF 위험은 구조적으로 낮고, CSP/단기 TTL/Refresh rotation으로 XSS 토큰 탈취 위험을 완화한다. 운영 상용 전환 시에만 HttpOnly Cookie/BFF로 개선한다.
- CSRF는 현재 Authorization header 기반 stateless API 구조에 맞춰 비활성화한 것이며, 쿠키 인증으로 전환할 경우에만 CSRF 정책을 새로 설계한다.
- CSP는 Swagger/UI 호환을 위해 `script-src 'unsafe-inline'`, `style-src 'unsafe-inline'`을 허용한다.
- 300~1,000명 규모 부하 검증은 운영 단건 응답 시간 확인 수준이며 정식 부하 테스트는 후속 작업이다.

## 제출 기준 결론

Pokemo MVP는 인증, 권한, 비밀번호 해시, HTTPS, CORS, 보안 헤더, SQL Injection 기본 방어, XSS 기본 방어, Refresh Token rotation 등 핵심 보안 통제를 적용했다. 다만 운영 상용 서비스 수준의 토큰 저장 구조, CSRF, 계정/과목 도메인 세분화, 부하 테스트, 모니터링은 후속 보강 범위로 남긴다.
