# Frontend Mock 가이드

> 백엔드 없이 로그인 이후 앱 화면(대시보드, 캘린더, 노트, 퀴즈, 오답노트, AI 추천, 공지사항)을 미리 확인하기 위한 mock 흐름 문서입니다. 실제 API 연결 시 무엇만 바꾸면 되는지도 정리합니다.

## TL;DR

```bash
cd frontend
npm install
npm run mock      # 백엔드 없이 모든 화면 시연
```

- 로그인 화면이 먼저 나타나고 입력란은 `student@pokemo.dev` / `mock-demo-password`로 prefill 되어 있음
- **"로그인"** 버튼 클릭 → fetch 호출 없이 즉시 mock 세션 발급 → 대시보드 진입
- 사이드바로 7개 화면 전부 둘러볼 수 있음
- 이메일을 `admin@pokemo.dev`로 바꿔 입력하면 ADMIN role로 로그인되어 공지사항 작성 UI까지 확인 가능

## 화면 구성

| 화면 | 파일 | 비고 |
|---|---|---|
| 로그인/홈 | `src/pages/HomePage.tsx` + `components/AuthCard.tsx` | **수정 없음** — 기존 그대로 |
| 대시보드 | `src/app/screens/DashboardScreen.tsx` | D-Day 스트립, 주간 막대, 과목별 진척, AI 추천, SVG 정답률 추이, 빠른 이동 |
| 캘린더 | `src/app/screens/CalendarScreen.tsx` | 월간 그리드(주간 반복 일정 펼침) + 다가오는 일정 + 알림 배너 |
| 노트 | `src/app/screens/NotesScreen.tsx` | 과목·태그 필터, 검색, Markdown split 에디터, 자체 파서, 자동 저장 상태 |
| 퀴즈 | `src/app/screens/QuizScreen.tsx` | 객관식/OX/단답형 탭, 점수 사이드, 오답 즉시 누적 |
| 오답노트 | `src/app/screens/WrongAnswersScreen.tsx` | 총·반복·취약 1순위 통계, 유형 필터, 다시 풀기/해설 보기 |
| AI 추천 | `src/app/screens/RecommendScreen.tsx` | 우선순위 레일, 취약 개념, 시험 임박 과목, 노트 요약(★AI) |
| 공지사항 | `src/app/screens/NoticesScreen.tsx` | USER 보기 / ADMIN 작성 분기 |

각 화면은 단 한 줄로 데이터를 가져옵니다:

```tsx
const { data, loading } = useApi(() => pokemoApi.getDashboard(), [])
```

## Mock 데이터 계층

모든 mock 데이터는 단일 모듈 `src/app/pokemoApi.ts`에 모여 있습니다.

```
src/app/
├── types.ts                 모든 DTO 타입 정의 (UserAccount, Note, Quiz, ...)
├── pokemoApi.ts             Promise-shaped mock API (delay 포함)
├── useApi.ts                useApi(fn, deps) → { data, loading, error, refetch }
└── useAuthSession.ts        localStorage 폴링 기반 세션 훅
```

`pokemoApi`의 모든 함수는 **백엔드 REST 엔드포인트와 1:1 매칭**되도록 설계되어 있습니다.

| Mock 함수 | 백엔드 REST 후보 | 매핑 기능 |
|---|---|---|
| `getDashboard()` | `GET /api/dashboard` | F-34 |
| `getEvents({from,to})` | `GET /api/calendar/events?from=&to=` | F-09 |
| `getSubjects()` | `GET /api/subjects` | F-09 |
| `getNotes(query)` | `GET /api/notes?subjectId=&tagIds=&q=` | F-15·16·17 |
| `getNote(id)` | `GET /api/notes/:id` | F-13 |
| `patchNote(id, partial)` | `PATCH /api/notes/:id` | F-18 자동 저장 |
| `getTags()` | `GET /api/tags` | F-16 |
| `getAttachments(ids)` | `GET /api/notes/:id/attachments` | F-14 |
| `getQuiz(id)` | `GET /api/quiz/:id` | F-19~21 |
| `getWrongAnswers({type})` | `GET /api/quiz/wrong-answers` | F-22 |
| `retryWeakTypes()` | `POST /api/quiz/retry-weak-types` | F-26 ★AI |
| `getRecommend()` | `GET /api/recommend` | F-28·29·30 |
| `summarizeNote(noteId)` | `POST /api/recommend/summary` | F-27 ★AI |
| `getNotices()` | `GET /api/notices` | F-08 |
| `createNotice(payload)` | `POST /api/admin/notices` | F-08 ADMIN |

모든 함수는 `Promise<T>` 반환 + 의도적 `delay(80~150ms)` 포함이라 **실제 비동기 UX 그대로** 렌더링됩니다 (로딩 스피너, 스켈레톤 모두 동작).

### 인증 mock

`src/lib/authApi.ts`의 `loginUser`/`registerUser`/`getCurrentUser`에는 환경변수 분기가 추가되어 있습니다:

```ts
const mockAuthMode = import.meta.env.VITE_POKEMO_MOCK_SESSION === 'true'

export function loginUser(credentials) {
  if (mockAuthMode) {
    return Promise.resolve(buildMockSession(credentials.email))
  }
  return requestJson<AuthSession>('/api/auth/login', { ... })
}
```

- prod/dev 모드(`mockAuthMode=false`)에서는 평소대로 백엔드에 fetch
- mock 모드(`mockAuthMode=true`)에서는 fetch 대신 mock 세션을 즉시 resolve
- `localStorage['pokemo.auth']` 저장 흐름·키 구조는 **무수정**

## 실행 모드

### 1. `npm run mock` — 백엔드 없이 시연

`.env.mock` 파일을 통해 `VITE_POKEMO_MOCK_SESSION=true`가 주입되어:

- `AuthCard` 입력란 prefill
- `loginUser` / `registerUser` / `getCurrentUser`가 백엔드 없이 mock 응답 반환
- 화면들은 `pokemoApi` mock 데이터만 사용

### 2. `npm run dev` — 정상 개발 (백엔드 필요)

`.env.mock`이 로드되지 않아 `mockAuthMode=false`:

- `AuthCard`는 빈 입력란
- 모든 인증 호출은 실제 `http://localhost:8080/api/auth/*`로 fetch
- 백엔드를 같이 띄워야 로그인 가능 (`cd backend && ./mvnw.cmd spring-boot:run`)

### 3. `npm run build` — 운영 빌드

`tsc -b && vite build`. `.env.mock`은 로드되지 않음. mock 분기는 정의되지 않은 환경변수로 false 처리되어 운영에 영향 없음.

### 4. `npm run test`

`vitest --run`. 인증 API 클라이언트 + apiHealth 테스트 6건 전부 통과.

## 백엔드 연결 시 작업 절차

화면 코드는 **한 줄도 수정할 필요가 없습니다.** 두 가지만 바꾸면 됩니다.

### Step 1. `pokemoApi.ts` 함수 본문 교체

```ts
// 현재
async getDashboard(): Promise<DashboardPayload> {
  await delay(120)
  return { user, upcomingExams, ... }
}

// 백엔드 연결 후
async getDashboard(): Promise<DashboardPayload> {
  const res = await fetch('/api/dashboard', { headers: authHeaders() })
  if (!res.ok) throw new ApiError(await res.json())
  return res.json()
}
```

각 함수 시그니처와 반환 타입은 그대로 유지 → 화면은 변경 없이 작동.

### Step 2. `authApi.ts` mock 분기 제거 (선택)

`npm run mock`을 더 이상 안 쓴다면 `mockAuthMode` 관련 코드를 지워도 됩니다. 다만 시연용으로 그대로 두는 것을 권장합니다.

## 파일 변경 요약 (이번 작업)

### 신규 (`src/app/` 하위)

- `types.ts` — DTO 타입
- `pokemoApi.ts` — mock API
- `useApi.ts` — 데이터 페치 훅
- `useAuthSession.ts` — 세션 폴링 훅
- `AppShell.tsx` + `AppShell.css` — 로그인 후 레이아웃
- `components/Icon.tsx` — Lucide 스타일 인라인 SVG
- `components/AppHeader.tsx` — 상단 헤더 (브랜드 + 알림 + 사용자 pill)
- `components/Sidebar.tsx` — 좌측 카드형 사이드바
- `screens/{Dashboard,Calendar,Notes,Quiz,WrongAnswers,Recommend,Notices}Screen.tsx`

### 신규 (frontend 루트)

- `.env.mock` — mock 모드 환경변수
- `Frontend_mock.md` — 본 문서

### 수정

- `src/App.tsx` — `useAuthSession()`로 세션 분기 (HomePage ↔ AppShell)
- `src/components/AuthCard.tsx` — mock 모드일 때 입력란 prefill 한 줄
- `src/lib/authApi.ts` — mock 모드일 때 `login`/`register`/`me`가 fetch 대신 mock 즉시 resolve
- `src/lib/apiHealth.test.ts` — 기존 한글화 이후 깨져 있던 expectation 정상화
- `package.json` — `"mock": "vite --mode mock"` 스크립트 추가

### 손대지 않은 핵심 흐름

- 백엔드 코드 전체
- `localStorage['pokemo.auth']` 키와 직렬화 구조
- `HomePage.tsx` / `HomePage.css` (로그아웃 상태 랜딩)

## 디자인 시스템 출처

본 화면들은 Claude Design 핸드오프 번들(`pokemo-design-system`)을 기준으로 포팅했습니다. 핵심 토큰은 `src/styles/tokens.css`(기존)에 그대로 보존되어 있고, 추가 컴포넌트 스타일은 `src/app/AppShell.css`에 모여 있습니다.

- 색상: 네이비/블루/테라코타 + 페이퍼 화이트 (변경 금지, 토큰만 사용)
- 폰트: Pretendard (4단 무게)
- 라디우스: 12 / 16 / 24px + pill
- 그림자: `--shadow-card` 단일 elevation
- 모션: `160ms ease` 단일 토큰, hover `translateY(-2px)`만

새 컴포넌트를 추가할 때는 색상/스페이싱/폰트를 직접 입력하지 말고 **반드시 `tokens.css`의 CSS 변수만** 사용해 주세요.

## 자주 묻는 질문

**Q. mock 모드에서 admin 화면도 볼 수 있나요?**
A. 네, 로그인 입력란을 `admin@pokemo.dev`로 바꿔 입력 후 로그인하면 ADMIN role이 부여되어 공지사항 화면에 "새 공지 작성" 컴포저가 노출됩니다.

**Q. 노트를 수정하면 어디에 저장되나요?**
A. mock 모드에서는 모듈 내 메모리에만 반영됩니다. 페이지 새로고침하면 초기 상태로 리셋됩니다.

**Q. 자동 저장 indicator(저장됨/저장 중)는 실제로 작동하나요?**
A. 네. textarea 입력 후 2초 debounce → `pokemoApi.patchNote(id, { content })` 호출 → 상태 전환까지 모두 정상 동작합니다.

**Q. mock 모드와 dev 모드 동시 사용 가능한가요?**
A. 한 번에 하나만 실행하세요. 둘 다 5173 포트를 사용하므로 충돌합니다.

**Q. `.env.mock` 파일을 수정해도 되나요?**
A. 가능하지만 현재는 변수 하나뿐이라 굳이 바꿀 일이 없습니다. 새 환경변수가 필요하면 prod 영향 없는지 확인 후 추가해 주세요.
