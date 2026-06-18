# Pokemo 최종 Smoke Test 체크리스트

## 목적

제출/시연 직전 운영 도메인에서 주요 기능이 정상 동작하는지 빠르게 확인하기 위한 체크리스트다.

## 운영 기본 확인

| 항목 | 명령 또는 절차 | 기대 결과 |
| --- | --- | --- |
| API health | `curl -i https://pokemo.duckdns.org/api/health` | `200 OK` |
| Actuator health | `curl -i https://pokemo.duckdns.org/actuator/health` | `200 OK`, status `UP` |
| 공지 목록 | `curl -i https://pokemo.duckdns.org/api/notices` | `200 OK`, JSON 배열 |
| 프론트 접속 | 브라우저에서 `https://pokemo.duckdns.org` 접속 | 로그인 화면 렌더링 |
| HTTPS | 브라우저 주소창 자물쇠 확인 | HTTPS 인증서 정상 |

## 주요 기능 수동 확인

| 기능 | 절차 | 기대 결과 |
| --- | --- | --- |
| 로그인 | 기존 계정으로 로그인 | 대시보드 진입 |
| 관리자 공지 | ADMIN 계정으로 공지 작성/수정/삭제 | 목록/상세에 반영 |
| 과목 | 과목 목록 조회 및 선택 | 화면에서 과목명 정상 표시 |
| 노트 | 노트 목록 진입, 노트 작성/수정 | 노트 목록/편집 화면 표시, 자동 저장 동작 |
| 노트 이미지 | 이미지 첨부 업로드 | S3 첨부 URL로 표시 |
| 캘린더 | 일정 등록/삭제 | 월간 캘린더에 반영 |
| 퀴즈 | 퀴즈 목록/풀이 | 채점 및 기록 저장 |
| 오답노트 | 오답노트 화면 진입 | 오답 목록 및 배지 표시 |
| AI 요약 | 노트 선택 후 요약 실행 | 결과 또는 AI 오류 안내 표시 |
| AI 추천 | 추천 화면 진입 | 추천 카드 또는 데이터 없음 안내 표시 |
| 통계 | 대시보드/통계 화면 진입 | 주간 공부 시간, 성취도, 정답률 표시 |
| 로그아웃 | 로그아웃 버튼 클릭 | 로그인 화면으로 복귀 |

## 일반 API 3초 목표 확인

운영 서버에서 다음 명령으로 주요 일반 API의 응답 시간을 확인한다. AI API는 외부 모델 호출이 포함되어 3초 목표에서 제외하고 별도 timeout 정책으로 관리한다.

```bash
curl -o /dev/null -s -w 'health %{http_code} %{time_total}s\n' https://pokemo.duckdns.org/api/health
curl -o /dev/null -s -w 'notices %{http_code} %{time_total}s\n' https://pokemo.duckdns.org/api/notices
curl -o /dev/null -s -w 'actuator %{http_code} %{time_total}s\n' https://pokemo.duckdns.org/actuator/health
```

2026-06-18 로컬에서 운영 도메인 기준으로 확인한 공개 일반 API 응답 시간:

| API | 결과 |
| --- | --- |
| `GET /api/health` | `200`, `2.918505s` |
| `GET /api/notices` | `200`, `1.013108s` |
| `GET /actuator/health` | 도메인 경유 `502`, 프록시/운영 점검 대상으로 기록 |

로그인 필요 API는 브라우저 수동 확인 또는 Access Token을 이용한 curl로 확인한다.

## 판정 기준

- 핵심 화면 로그인/노트/공지/캘린더/대시보드가 정상 표시되면 MVP 시연 가능으로 본다.
- 일반 공개 API 응답이 3초 이내이면 일반 API 응답 목표를 만족한 것으로 기록한다.
- AI 기능은 외부 Gemini 상태에 영향을 받을 수 있으므로 실패 시 graceful degradation 안내가 표시되는지 확인한다.
- 300~1,000명 동시 사용자 검증은 별도 k6/JMeter/Gatling 기반 부하테스트가 필요하므로 후속 과제로 둔다.
