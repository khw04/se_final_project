# Pokemo Backend

Pokemo Spring Boot 백엔드 REST API입니다.

## 실행 명령

```cmd
mvnw.cmd test
mvnw.cmd spring-boot:run
```

기본 실행은 `local` profile을 사용하므로 MySQL 없이 H2 인메모리 DB로 빠르게 테스트할 수 있습니다.
로컬 MySQL과 연결해서 실행하려면 `SPRING_PROFILES_ACTIVE=dev`를 설정합니다.

## 주요 엔드포인트

- Actuator health: `http://localhost:8080/actuator/health`
- API health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Profiles

- `local`: H2 인메모리 DB 기반 빠른 로컬 테스트
- `dev`: 로컬 MySQL 기본값 + 환경변수 override
- `prod`: 배포 환경변수 기반 운영 설정

## 환경변수 파일 위치

루트 디렉터리의 `.env.example`을 복사해서 실제 `.env`를 만듭니다.

```cmd
copy ..\.env.example ..\.env
```

파일 위치 기준:

- 샘플 파일: `/.env.example`
- 실제 배포/로컬 값 파일: `/.env`
- 백엔드 단독 실행 참고 샘플: `/backend/.env.example`

`.env.example`은 GitHub에 올리는 파일이고, 실제 값은 비워둡니다.
`.env`는 실제 DB 비밀번호, JWT secret, Gemini API key, OAuth secret, SMTP 비밀번호, VAPID private key를 넣는 파일이며 GitHub에 올리지 않습니다.

## Gemini API

Gemini API key는 백엔드 실행 환경에만 설정합니다.
프론트엔드 `.env`나 React 코드에는 절대 넣지 않습니다.

Docker Compose 배포에서는 루트 `.env`에 아래처럼 실제 값을 넣습니다.

```env
COMPOSE_PROJECT_NAME=pokemo
GEMINI_API_KEY=실제_API_KEY
GEMINI_MODEL=gemini-2.0-flash
```

`docker-compose.prod.yml`은 루트 `.env` 값을 읽어서 backend 컨테이너의 환경변수로 전달합니다.

## 배포 환경변수 점검

제출/시연용 Docker Compose 실행 전에는 루트 `.env.example`을 복사한 뒤 아래 값을 필요한 만큼 채웁니다.

- `JWT_SECRET`: 운영/시연 환경마다 긴 임의 문자열 사용
- `GEMINI_API_KEY`, `GEMINI_MODEL`, `GEMINI_TIMEOUT_SECONDS`: AI 요약/퀴즈/추천 서버 사이드 호출
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`: 소셜 로그인 사용 시 설정
- `SMTP_ENABLED`, `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`: 이메일 인증/비밀번호 재설정 메일 발송
- `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`, `VAPID_SUBJECT`: 웹 푸시 구독 유지용 키. 비워두면 서버 재시작 후 기존 구독이 무효화됩니다.

현재 프론트엔드 인증 세션은 `localStorage['pokemo.auth']`에 저장합니다. 운영 수준 보안에서는 HTTPS 적용, HttpOnly Cookie 또는 BFF 패턴, Refresh Token rotation, XSS/CSRF 점검을 별도 보강 대상으로 둡니다.

프로젝트 경로에 한글이나 공백이 있으면 Docker Compose가 프로젝트명을 자동 생성하지 못할 수 있습니다.
이때는 루트 `.env`에 `COMPOSE_PROJECT_NAME=pokemo`를 넣거나 아래처럼 실행합니다.

```cmd
docker compose -p pokemo up --build
```
