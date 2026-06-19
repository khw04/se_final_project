# Pokemo 프로젝트 문서

설계/요구사항 원본은 `Pokemo_설계문서_v3_5.pdf`, `Pokemo_요구사항_문서_v2.3.pdf`를 참고한다. 이 문서는 보안·개인정보·배포·성능·Smoke Test 점검 내용을 하나로 정리한다.

---

## 1. 보안

### 1.1 적용된 보안 통제

- **비밀번호 저장**: BCrypt 해시. 해시는 사용자 프로필과 분리된 `Credential` 엔티티(`credentials` 테이블)에 저장.
- **인증 구조**: JWT Access Token / Refresh Token 분리. Refresh Token은 DB 저장, 로그아웃 시 revoke, refresh 성공 시 기존 토큰 revoke 후 재발급(rotation).
- **권한 제어**: Spring Security `USER`/`ADMIN` Role 분리. `/api/admin/**` 및 공지 작성·수정·삭제는 ADMIN 제한. 노트/일정/퀴즈/과목은 사용자별 분리, 공부 세션 생성 시 과목 소유권 검증.
- **이메일 인증**: 미인증 계정 로그인 차단.
- **CORS**: 허용 origin을 환경변수로 제한.
- **SQL Injection**: 데이터 접근을 Spring Data JPA Repository 기반으로 처리, 노트 검색 파라미터 바인딩 회귀 테스트.
- **XSS 기본 방어**: React 렌더링 사용, `dangerouslySetInnerHTML` 미사용.
- **보안 헤더**: CSP, Referrer-Policy, Permissions-Policy, X-Frame-Options, HSTS.
- **인증 실패 로그**: 로그인 실패, 미인증 로그인 차단, JWT 검증 실패 기록.
- **로그 PII 마스킹**: 감사 로그의 이메일을 `s***@example.com` 형태로 마스킹(`common/LogMask`), 추적은 내부 `userId`로 수행.
- **DB 전송 보안**: 운영 RDS 연결 `VERIFY_IDENTITY` 기본화 + RDS CA truststore 적용(아래 4.2).
- **비밀값 관리**: Gemini/OAuth/SMTP/JWT secret/VAPID private key는 `.env`·배포 환경변수에만 저장하고 Git에 커밋하지 않음. 프론트엔드는 OAuth public client id만 포함.

### 1.2 인증 모델 정책

- 브라우저 인증 세션은 `localStorage['pokemo.auth']`에 Access/Refresh Token을 저장한다. Authorization Bearer header 기반 stateless API라 브라우저가 인증정보를 자동 첨부하지 않으므로 전통적 CSRF 위험이 구조적으로 낮다.
- 따라서 Spring Security에서 CSRF를 비활성화한 것이 현재 인증 모델에 부합하는 설정이다.
- `localStorage` 방식의 XSS 토큰 탈취 위험은 CSP 보안 헤더, `dangerouslySetInnerHTML` 미사용, Access Token 단기 TTL(기본 30분), Refresh Token rotation/revoke로 완화한다.
- Swagger UI 호환을 위해 CSP `script-src`·`style-src`에 `'unsafe-inline'`을 허용한다.

### 1.3 OWASP Top 10 점검표

| OWASP 항목                                     | 현재 대응                                                                                                                                      | 상태      |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | --------- |
| A01 Broken Access Control                      | JWT role claim,`USER`/`ADMIN` 권한 분리, `/api/admin/**`·공지 쓰기 ADMIN 제한, 사용자별 노트/일정/퀴즈/과목 분리, 공부 세션 소유권 검증 | 대응      |
| A02 Cryptographic Failures                     | BCrypt 해시,`Credential` 분리 저장, HTTPS, secret 환경변수 관리, RDS `VERIFY_IDENTITY` + CA truststore                                     | 부분 대응 |
| A03 Injection                                  | Spring Data JPA Repository 중심, 노트 검색 파라미터 바인딩 회귀 테스트                                                                         | 대응      |
| A04 Insecure Design                            | 3-Tier 구조, 인증/AI/스토리지/알림 계층 분리,`Credential` 분리, 과목 사용자별 분리                                                           | 부분 대응 |
| A05 Security Misconfiguration                  | CORS origin 환경변수화, CSP/Referrer-Policy/Permissions-Policy/X-Frame-Options/HSTS                                                            | 부분 대응 |
| A06 Vulnerable and Outdated Components         | Maven/npm 의존성 관리, CI backend test/frontend lint·build                                                                                    | 부분 대응 |
| A07 Identification and Authentication Failures | 이메일 인증 로그인 차단, Access/Refresh 분리, Refresh rotation/revoke, bcrypt, 단기 Access TTL                                                 | 부분 대응 |
| A08 Software and Data Integrity Failures       | GitHub Actions CI/CD, EC2 pull/rebuild/restart 자동화,`.env` 미커밋 정책                                                                     | 부분 대응 |
| A09 Security Logging and Monitoring Failures   | 로그인 실패/미인증 차단/JWT 검증 실패 로그, 감사 로그 이메일 PII 마스킹                                                                        | 부분 대응 |
| A10 Server-Side Request Forgery                | 외부 호출은 Gemini/OAuth/SMTP/S3 SDK 중심, 사용자 입력 URL fetch 기능 없음                                                                     | 대응      |

---

## 2. 개인정보 처리

### 2.1 수집 항목

| 구분        | 항목                                              | 목적                                            |
| ----------- | ------------------------------------------------- | ----------------------------------------------- |
| 계정        | 이메일                                            | 회원 식별, 로그인, 이메일 인증, 비밀번호 재설정 |
| 인증        | 비밀번호 해시                                     | 이메일/비밀번호 로그인 검증                     |
| OAuth       | provider, provider user id                        | Google/Kakao 로그인 계정 연결                   |
| 학습 데이터 | 과목, 일정, 노트, 퀴즈 풀이 이력, 오답, 공부 시간 | 학습 관리 기능 제공                             |
| 첨부파일    | 노트 이미지 파일                                  | 노트 이미지 첨부 기능 제공                      |
| 알림        | 웹 푸시 구독 endpoint/key                         | 일정 임박 웹 푸시 알림 발송                     |

주민등록번호, 결제 정보, 주소, 전화번호 등 민감하거나 불필요한 개인정보는 수집하지 않는다.

### 2.2 저장 및 보호

- 비밀번호는 원문 저장 없이 BCrypt 해시로 저장하며, `Credential` 엔티티로 분리한다.
- Access/Refresh Token을 분리하고 Refresh Token은 DB에 저장, 로그아웃·재발급 시 revoke한다.
- 운영 통신은 DuckDNS/Caddy 기반 HTTPS, 운영 DB는 AWS RDS MySQL(`VERIFY_IDENTITY` TLS)을 사용한다.
- 첨부파일은 운영 환경에서 EC2 IAM Role 기반 S3에 저장한다.
- 비밀값은 `.env`·배포 환경변수에만 저장한다.

### 2.3 접근 통제

- 일반 API는 JWT 인증 후 접근한다.
- 관리자 API와 공지 작성/수정/삭제는 `ADMIN` Role만 허용한다.
- 공지 조회, health check, Swagger 문서, 첨부파일 GET 등 공개가 필요한 경로만 인증 없이 허용한다.
- 프론트 Nginx `/api/` 프록시에서 `Authorization` 헤더를 백엔드로 전달한다.

### 2.4 데이터 보존/삭제

- 노트, 태그, 일정, 공지 등 주요 사용 데이터는 화면 또는 API에서 삭제할 수 있다.
- 사용자가 생성한 과목, 일정, 노트, 태그, 첨부파일, 퀴즈 데이터는 서비스 기능 제공을 위해 저장한다.

---

## 3. 배포 실행 순서

### 3.1 인프라 권장 시작값

**RDS (MySQL 8.x)**

- DB instance identifier `pokemo-db`, DB name `pokemo`, Master username `pokemo`
- Public access `No`, Region `ap-northeast-2`
- Security group inbound: EC2 보안 그룹에서 오는 MySQL `3306`만 허용
- 최초 배포 `JPA_DDL_AUTO=update` → 기동 성공 후 `validate`

**EC2 (Ubuntu 24.04 LTS / Amazon Linux 2023)**

- Instance type `t3.small` 이상 권장(시연용 `t2.micro` 가능), Storage 20GB+
- Security group inbound: SSH `22`(가능하면 본인 IP), HTTP `80`, HTTPS `443`

### 3.2 EC2 준비 및 `.env`

```bash
docker --version
docker compose version
cp .env.example .env
```

운영 최소 필수값:

```env
COMPOSE_PROJECT_NAME=pokemo
DB_HOST=your-rds-endpoint.ap-northeast-2.rds.amazonaws.com
DB_PORT=3306
DB_NAME=pokemo
DB_USERNAME=pokemo
DB_PASSWORD=change-me
DB_SSL_MODE=VERIFY_IDENTITY
JPA_DDL_AUTO=update
JWT_SECRET=change-this-to-a-long-random-secret-at-least-32-bytes
CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### 3.3 RDS TLS 강화 (VERIFY_IDENTITY)

`VERIFY_IDENTITY`는 전송 암호화에 더해 RDS 서버 인증서를 신뢰된 CA로 검증해 MITM을 방지한다. Amazon RDS CA는 JVM 기본 truststore에 없으므로 CA 번들을 truststore로 만들어 전달한다.

```bash
curl -O https://truststore.pki.rds.amazonaws.com/ap-northeast-2/ap-northeast-2-bundle.pem
keytool -importcert -noprompt -alias rds-ca \
  -file ap-northeast-2-bundle.pem \
  -keystore certs/rds-truststore.jks \
  -storepass changeit
```

- `docker-compose.prod.yml`의 backend `volumes`에서 `- ./certs:/app/certs:ro` 주석을 해제한다.
- `.env`에 설정:

```env
DB_SSL_MODE=VERIFY_IDENTITY
DB_SSL_PARAMS=&trustCertificateKeyStoreUrl=file:/app/certs/rds-truststore.jks&trustCertificateKeyStorePassword=changeit
```

truststore 준비 전에는 임시로 `DB_SSL_MODE=REQUIRED`, `DB_SSL_PARAMS=`로 기동할 수 있다(전송 암호화만 수행). 운영 확정 시 `VERIFY_IDENTITY`로 되돌린다.

### 3.4 운영 compose 실행

```bash
docker compose -f docker-compose.prod.yml --env-file .env config
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

최초 기동이 성공하면 `.env`의 `JPA_DDL_AUTO`를 `validate`로 바꾸고 backend만 재배포한다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build backend
curl http://localhost:8080/api/health
```

### 3.5 RDS 재초기화 (데이터 보존 불필요 시)

기존 데이터를 비우고 새 스키마로 시작할 때만 사용한다. **인스턴스를 삭제하지 말고 스냅샷을 먼저 만든 뒤 DB만 비운다.**

1. AWS 콘솔 `RDS` > `데이터베이스` > 인스턴스 > `작업` > `스냅샷 생성`(`pre-reset-YYYYMMDD`).
2. 백엔드를 내려 연결을 끊는다: `docker compose -f docker-compose.prod.yml --env-file .env down`
3. DB 재생성(비밀번호는 `read -s DB_PASSWORD`로 입력):

```bash
docker run --rm mysql:8.4 mysql \
  -h "$DB_HOST" -P 3306 --ssl-mode=REQUIRED \
  -u "$DB_USERNAME" -p"$DB_PASSWORD" \
  -e "DROP DATABASE IF EXISTS \`pokemo\`; CREATE DATABASE \`pokemo\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

4. `.env`를 `JPA_DDL_AUTO=update`로 1회 기동 → 성공 후 `validate`로 복귀.

> 자동 CD가 켜진 상태에서 준비 없이 main에 merge하면 `validate` 상태로 먼저 실행되어 스키마 검증 실패로 백엔드가 내려갈 수 있다. RDS 초기화와 `.env` 준비를 merge 전에 끝낸다.

### 3.6 HTTPS 적용

`docker-compose.prod.yml`은 호스트 Caddy가 80/443을 받고 프론트 컨테이너를 `127.0.0.1:8080:80`으로 프록시하는 구성을 기준으로 한다.

- DuckDNS 또는 운영 도메인 A 레코드를 EC2 public IP로 설정.
- Caddyfile 예시:

```caddy
pokemo.duckdns.org {
    reverse_proxy 127.0.0.1:8080
}
```

HTTPS 적용 후 `.env`의 `CORS_ALLOWED_ORIGINS`와 OAuth callback URL을 운영 도메인 기준으로 맞춘다.

### 3.7 외부 서비스 운영값

필요한 기능만 실제 값을 채운다: `GEMINI_API_KEY`, `GOOGLE_CLIENT_ID/SECRET`, `KAKAO_CLIENT_ID/SECRET`, `SMTP_*`, `VAPID_*`, `STORAGE_TYPE=s3`/`S3_BUCKET`/`AWS_REGION`.

### 3.8 CD 자동화

`.github/workflows/cd.yml`은 `main` push 후 CI 성공 시 EC2에 SSH 접속해 `origin/main`으로 동기화하고 compose를 재배포한다(`workflow_dispatch` 수동 실행도 가능). GitHub Actions secrets에 등록:

| Secret              | 예시                                       | 설명                      |
| ------------------- | ------------------------------------------ | ------------------------- |
| `EC2_HOST`        | `52.79.233.137`                          | EC2 public IP 또는 도메인 |
| `EC2_USER`        | `ubuntu`                                 | SSH 사용자                |
| `EC2_SSH_KEY`     | `-----BEGIN OPENSSH PRIVATE KEY-----...` | EC2 접속 private key 전체 |
| `EC2_PROJECT_DIR` | `/home/ubuntu/pokemo`                    | EC2 프로젝트 경로         |
| `EC2_SSH_PORT`    | `22`                                     | 선택값, 비우면 22         |

CD 실행 전 EC2 조건: 저장소 clone 상태, 운영 `.env` 존재, Docker/Compose plugin 설치, GitHub Actions runner의 SSH 접속 허용.

---

## 4. 성능 / 부하 검증

요구사항 비기능 목표 두 가지를 검증한다: ① 일반 API 응답 3초 이내, ② 300~1,000명 초기 사용자 처리. 측정값(운영 도메인 실측)과 용량 추정을 구분하며, 부하는 `scripts/loadtest.js`(k6)로 재현한다.

### 4.1 일반 API 응답 3초 이내 (측정)

운영 도메인(`https://pokemo.duckdns.org`) 공개 일반 API 단건 응답 시간:

| 엔드포인트           | 응답 시간 | 비고                                       |
| -------------------- | --------- | ------------------------------------------ |
| `GET /api/health`  | 2.918초   | 최초 호출 cold start 포함, warm 시 수백 ms |
| `GET /api/notices` | 1.013초   | 공지 목록(캐시 제외 경로)                  |

```bash
curl -s -o /dev/null -w "%{time_total}\n" https://pokemo.duckdns.org/api/health
curl -s -o /dev/null -w "%{time_total}\n" https://pokemo.duckdns.org/api/notices
```

### 4.2 300~1,000명 초기 사용자 처리 (추정 + 하니스 실측)

- "300-1,000명"은 가입자 규모로, 동시 활성 사용자는 보통 가입자의 5~15%로 가정한다(1,000명 가입 시 피크 약 50~150명). 읽기 위주·think time으로 사용자당 지속 RPS는 낮아(0.2~1 req/s), 피크 150 RPS 수준을 목표 처리량으로 본다.
- warm 단건 응답이 수백 ms 이내이고 톰캣 기본 워커(200)·HikariCP(10) 구성에 여유가 있으며, 조회 잦은 경로는 Redis 캐시와 페이지네이션/인덱스로 DB 부하를 줄였다.

**권장 검증 절차 (k6)**

```bash
k6 run -e BASE_URL=https://pokemo.duckdns.org -e VUS=300 -e DURATION=1m scripts/loadtest.js
k6 run -e BASE_URL=https://pokemo.duckdns.org -e TOKEN=<accessToken> -e VUS=300 scripts/loadtest.js
```

판정 기준(스크립트 thresholds): `http_req_duration p(95) < 3000ms`, `http_req_failed rate < 1%`.

**로컬 하니스 실측 (2026-06-19, `http://localhost:8080`, H2, 300 VU ramping 2m30s, 30,086 iterations)**

| 지표                        | 측정값                  | 판정 기준 | 통과 |
| --------------------------- | ----------------------- | --------- | ---- |
| `http_req_duration p(95)` | 1.04ms                  | < 3000ms  | ✅   |
| `http_req_failed`         | 0.00%                   | < 1%      | ✅   |
| checks 성공률               | 100.00% (60,172/60,172) | —        | ✅   |

> p95 1.04ms는 루프백 측정값으로 네트워크 RTT가 빠진 수치다. 이 실행은 "하니스 동작 및 임계치 통과 검증"으로만 의미가 있다.

---

## 5. 최종 Smoke Test 체크리스트

제출/시연 직전 운영 도메인에서 주요 기능을 빠르게 확인한다.

### 5.1 운영 기본 확인

| 항목            | 명령/절차                                              | 기대 결과                 |
| --------------- | ------------------------------------------------------ | ------------------------- |
| API health      | `curl -i https://pokemo.duckdns.org/api/health`      | `200 OK`                |
| Actuator health | `curl -i https://pokemo.duckdns.org/actuator/health` | `200 OK`, status `UP` |
| 공지 목록       | `curl -i https://pokemo.duckdns.org/api/notices`     | `200 OK`, JSON 배열     |
| 프론트 접속     | 브라우저 `https://pokemo.duckdns.org`                | 로그인 화면 렌더링        |
| HTTPS           | 주소창 자물쇠 확인                                     | 인증서 정상               |

### 5.2 주요 기능 수동 확인

| 기능        | 절차                               | 기대 결과                           |
| ----------- | ---------------------------------- | ----------------------------------- |
| 로그인      | 기존 계정 로그인                   | 대시보드 진입                       |
| 관리자 공지 | ADMIN 계정으로 공지 작성/수정/삭제 | 목록/상세 반영                      |
| 과목        | 과목 목록 조회/선택                | 과목명 정상 표시                    |
| 노트        | 노트 작성/수정                     | 편집 화면, 자동 저장 동작           |
| 노트 이미지 | 이미지 첨부 업로드                 | S3 첨부 URL 표시                    |
| 캘린더      | 일정 등록/삭제                     | 월간 캘린더 반영                    |
| 퀴즈        | 퀴즈 목록/풀이                     | 채점·기록 저장                     |
| 오답노트    | 오답노트 진입                      | 오답 목록·배지 표시                |
| AI 요약     | 노트 선택 후 요약                  | 결과 또는 AI 오류 안내              |
| AI 추천     | 추천 화면 진입                     | 추천 카드 또는 데이터 없음 안내     |
| 통계        | 대시보드/통계 진입                 | 주간 공부 시간, 성취도, 정답률 표시 |
| 로그아웃    | 로그아웃 클릭                      | 로그인 화면 복귀                    |

### 5.3 응답 시간 확인

AI API는 외부 모델 호출이 포함되어 3초 목표에서 제외하고 별도 timeout 정책으로 관리한다.

```bash
curl -o /dev/null -s -w 'health %{http_code} %{time_total}s\n' https://pokemo.duckdns.org/api/health
curl -o /dev/null -s -w 'notices %{http_code} %{time_total}s\n' https://pokemo.duckdns.org/api/notices
```

판정: 핵심 화면(로그인/노트/공지/캘린더/대시보드)이 정상 표시되고 일반 공개 API 응답이 3초 이내이면 MVP 시연 가능으로 본다. AI 기능은 실패 시 graceful degradation 안내 표시를 확인한다.
