# Pokemo Frontend

Pokemo의 React SPA 프론트엔드입니다. 로그인 후 대시보드·캘린더·노트·퀴즈·오답노트·통계·AI 추천·공지 화면을 단일 페이지 앱으로 제공합니다.

- React 19 + Vite 8 + TypeScript 6
- 라우터 라이브러리 없이 자체 nav 레지스트리로 화면을 전환하는 구조
- 인증 세션은 `localStorage['pokemo.auth']`에 저장하고, API 호출 시 `Authorization` 헤더를 자동 첨부

## 실행 명령

```bash
npm install
npm run dev      # 개발 서버 (Vite)
npm run build    # 타입 체크(tsc -b) 후 프로덕션 빌드
npm run test     # Vitest 단위 테스트
npm run lint     # ESLint
npm run preview  # 빌드 결과 미리보기
```

## 디렉터리 구조

```
src/
├── main.tsx                앱 진입점 (React 루트 마운트)
├── App.tsx                 세션 유무에 따라 로그인 화면 / AppShell 분기
├── pages/HomePage.tsx      비로그인 랜딩 + 로그인 카드
├── app/
│   ├── AppShell.tsx        로그인 후 레이아웃 (헤더 + 사이드바 + 본문, 학습 타이머 상태 관리)
│   ├── useAuthSession.ts   localStorage 세션 구독 훅
│   ├── nav/
│   │   ├── registry.tsx    화면(nav entry) 등록 목록과 조회 헬퍼
│   │   └── types.ts        NavEntry / NavOptions / StudyTimerState 타입
│   ├── screens/            화면별 컴포넌트 + 화면별 `nav.tsx`(등록 정의)
│   │   ├── dashboard/  calendar/  note/  quiz/  wrong/
│   │   ├── stats/  ai/  notice/  auth/
│   ├── api/                백엔드 REST 호출 클라이언트
│   │   ├── client.ts       fetch 래퍼 (토큰 첨부 + 401 시 refresh 재시도)
│   │   └── *Api.ts         도메인별 API 모듈 (note/quiz/calendar/stats/...)
│   ├── components/         공통 UI (AppHeader, Sidebar, Icon, PushNotificationCard)
│   └── types/              도메인 타입 정의
├── lib/                    인증(authApi)·헬스체크(apiHealth)·웹 푸시(push) 유틸
├── components/             로그인 카드(AuthCard), API 헬스 카드(ApiHealthCard)
└── styles/tokens.css       디자인 토큰
```

새 화면을 추가할 때는 `screens/<화면>/nav.tsx`에 `NavEntry`를 정의하고 `app/nav/registry.tsx`의 `navRegistry` 배열에 등록합니다. `AppShell`이 사이드바 선택에 따라 해당 entry의 `render()`를 호출합니다.

## 백엔드 연동

API 루트는 `VITE_API_BASE_URL`로 지정합니다. 값은 API 루트 자체(예: `.../api`)를 가리킵니다.

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

- 설정하지 않으면 개발 환경 기본값은 `http://localhost:8080/api`입니다. (`app/api/client.ts`)
- Docker Compose 배포에서는 `VITE_API_BASE_URL=/api`로 빌드되고, `frontend/nginx.conf`가 `/api/`·`/actuator/`를 backend 컨테이너로 프록시합니다.

`client.ts`의 `apiFetch`는 저장된 세션이 있으면 `Authorization` 헤더를 자동으로 붙이고, 응답이 `401`이면 `/auth/refresh`로 토큰을 갱신한 뒤 한 번 재시도합니다. 갱신에 실패하면 세션을 비웁니다.

## OAuth 환경변수

브라우저에는 공개 값인 OAuth client id만 전달합니다.

```env
VITE_GOOGLE_CLIENT_ID=
VITE_KAKAO_CLIENT_ID=
```

provider secret, Gemini key, SMTP 비밀번호, VAPID private key 등 비밀값은 절대 프론트엔드에 넣지 않고 백엔드/배포 환경에만 둡니다.

## 인증 세션 처리

- 로그인 성공 시 Access/Refresh Token, 이메일, role을 `localStorage['pokemo.auth']`에 저장합니다.
- `useAuthSession` 훅이 `storage` 이벤트를 구독해, 로그인/로그아웃 시 화면을 즉시 전환합니다.
- 로그아웃은 백엔드 `/auth/logout` 호출 후(실패해도) 로컬 세션을 제거합니다.
