# Pokemo

AI 기반 학습 관리 플랫폼. 과목별 노트 작성, 퀴즈 풀이(수동 + Gemini AI 생성), 오답노트, 학습 시간 타이머, 캘린더 일정, 통계, AI 추천을 한 곳에서 제공합니다.

모노레포 구조로 **Spring Boot REST 백엔드**와 **React SPA 프론트엔드**가 분리되어 있으며, Docker Compose로 함께 배포됩니다.


> **프로젝트 배포 링크:** [배포 URL](https://pokemo.duckdns.org/)

## 기술 스택

| 영역      | 스택                                                                                                                    |
| --------- | ----------------------------------------------------------------------------------------------------------------------- |
| Backend   | Java 21, Spring Boot 3.4.1, Spring Security, JPA(Hibernate), JJWT, springdoc-openapi                                    |
| Frontend  | React 19, Vite 8, TypeScript 6, Vitest 4                                                                                |
| DB / 캐시 | MySQL 8.4 (prod / dev), H2 인메모리 (local 테스트), Redis (prod 캐시)                                                   |
| 외부 연동 | Google Gemini API(AI 퀴즈·요약·추천), Google / Kakao OAuth, AWS S3(첨부 스토리지), SMTP(이메일 인증), Web Push(VAPID) |
| 인프라    | Docker / Docker Compose, Nginx(프론트 서빙·API 프록시), GitHub Actions(CI/CD), AWS EC2 + RDS                           |

## 주요 기능

- **인증** — 이메일/비밀번호 가입·로그인, JWT(Access/Refresh), 이메일 인증, 비밀번호 재설정, Google·Kakao OAuth, 관리자 사용자 관리
- **노트** — 과목별 노트 CRUD, 태그, 버전 이력, 첨부파일(로컬/S3)
- **퀴즈** — 수동 출제 + Gemini AI 자동 생성, 풀이 시도(attempt), 정답 채점, 오답노트
- **학습** — 학습 세션 타이머, 오늘의 학습 요약
- **캘린더** — 일정 이벤트 등록·조회
- **통계** — 정답률 추이, 과목별 진도, 유형별 정확도, 주간 학습량
- **AI 추천** — 우선순위 추천, 취약 개념, 예정 과목 요약 (Gemini)
- **공지 / 알림** — 공지사항, 웹 푸시(VAPID) 구독·발송

## 프로젝트 구조

```
se_final_project/
├── backend/                 Spring Boot REST API (Java 21)
│   └── src/main/java/com/pokemo/
│       ├── auth/            인증·인가, OAuth, JWT
│       ├── note/            노트·태그·첨부·버전
│       ├── quiz/            퀴즈·문항·시도·오답노트
│       ├── study/           학습 세션
│       ├── calendar/        캘린더 이벤트
│       ├── subject/         과목
│       ├── stats/           통계
│       ├── notice/          공지사항
│       ├── notification/    웹 푸시
│       ├── ai/              Gemini 연동
│       └── common/ config/ health/
├── frontend/                React 19 + Vite SPA
│   └── src/app/             화면(screens)·API 클라이언트·nav 등록
├── doc/                     설계/요구사항/보안/배포 산출물 문서
├── docker-compose.prod.yml  배포용 스택(redis + backend + frontend)
├── .env.example             루트 환경변수 템플릿
└── .github/workflows/       CI(ci.yml) / CD(cd.yml)
```

## 로컬 개발

백엔드와 프론트엔드는 독립 빌드 단위입니다. 별도 DB 설치 없이 각각 실행할 수 있습니다.

### Backend (H2 인메모리, 외부 DB 불필요)

```cmd
cd backend
mvnw.cmd spring-boot:run
```

기본 `local` profile은 H2 인메모리 DB를 사용합니다. 로컬 MySQL과 연결하려면 `SPRING_PROFILES_ACTIVE=dev`로 실행합니다.

- API health: `http://localhost:8080/api/health`
- Actuator health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

`VITE_API_BASE_URL`을 설정하지 않으면 기본값 `http://localhost:8080/api`로 백엔드를 호출합니다.

## 환경변수

루트 `.env.example`을 복사해 `.env`를 만들고 실제 값을 채웁니다. `.env`는 `.gitignore`에 등록되어 있어 커밋되지 않습니다.

```cmd
copy .env.example .env
```

| 변수                                                                | 용도                                                |
| ------------------------------------------------------------------- | --------------------------------------------------- |
| `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD` | DB 접속 정보                                        |
| `JWT_SECRET`                                                      | JWT 서명 시크릿 (긴 임의 문자열)                    |
| `GEMINI_API_KEY` `GEMINI_MODEL`                                 | AI 퀴즈·요약·추천 (서버 사이드 전용)              |
| `GOOGLE_CLIENT_ID/SECRET` `KAKAO_CLIENT_ID/SECRET`              | 소셜 로그인                                         |
| `SMTP_*`                                                          | 이메일 인증·비밀번호 재설정 메일 발송              |
| `VAPID_*`                                                         | 웹 푸시 구독 키 (비우면 재시작 시 기존 구독 무효화) |
| `STORAGE_TYPE` `S3_BUCKET` `AWS_REGION`                       | 첨부 스토리지 (`local` 또는 `s3`)               |
| `DB_SSL_MODE` `DB_SSL_PARAMS`                                   | 운영 MySQL/RDS TLS 옵션                             |

> ⚠️ 시크릿(DB 비밀번호, `JWT_SECRET`, `GEMINI_API_KEY`, OAuth client secret 등)은 절대 프론트엔드 코드나 저장소에 넣지 않습니다. 백엔드 실행 환경 또는 루트 `.env`에만 둡니다.

## 배포 (Docker Compose)

배포 스택은 `docker-compose.prod.yml`을 사용합니다. **redis(캐시) + backend(`prod` profile, 외부 RDS/MySQL) + frontend(nginx)** 로 구성됩니다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

- frontend는 `VITE_API_BASE_URL=/api`로 빌드되고, `frontend/nginx.conf`가 `/api/`·`/actuator/`를 backend 컨테이너로 프록시합니다.
- 운영 RDS에 `DB_SSL_MODE=VERIFY_IDENTITY`를 적용하려면 RDS CA truststore를 `./certs`에 마운트하고 `DB_SSL_PARAMS`를 설정합니다. (자세한 내용은 `doc/README.md`의 배포 실행 순서)

> 경로에 한글/공백이 있으면 Docker Compose 프로젝트명 자동 생성이 실패할 수 있습니다. `.env`에 `COMPOSE_PROJECT_NAME=pokemo`를 설정하거나 `-p pokemo` 옵션을 사용하세요.

### CI / CD

- **CI** (`.github/workflows/ci.yml`) — PR 및 main push 시 백엔드 테스트(`mvnw.cmd test`)와 프론트엔드 lint + build 실행.
- **CD** (`.github/workflows/cd.yml`) — main 브랜치 CI 성공 시 EC2에 SSH 접속 → `git reset --hard origin/main` → `docker-compose.prod.yml`로 재빌드 후 health check.

## 테스트

```bash
# Backend (H2 기반, 외부 DB 불필요)
cd backend && mvnw.cmd test

# Frontend
cd frontend && npm run test && npm run lint && npm run build
```

## 문서

| 문서                                  | 내용                                                                 |
| ------------------------------------- | -------------------------------------------------------------------- |
| `doc/README.md`                     | 보안·개인정보·배포 실행 순서·성능 부하 검증·Smoke Test 통합 문서 |
| `doc/Pokemo_설계문서_v3_5.pdf`      | 설계 문서                                                            |
| `doc/Pokemo_요구사항_문서_v2.3.pdf` | 요구사항 명세                                                        |
| `AGENTS.md`                         | AI 에이전트용 프로젝트 가이드                                        |
