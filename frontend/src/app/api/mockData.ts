import type {
  AccuracyTrendPoint,
  Attachment,
  CalendarEvent,
  Note,
  NoteSummary,
  Notice,
  PriorityRecommendation,
  Question,
  Quiz,
  Subject,
  SubjectProgress,
  Tag,
  UpcomingSubject,
  WeakConcept,
  WeeklyStudyPoint,
  WrongAnswerNote,
} from '../types'

import { calcDDay } from './dateUtils'

export const MOCK_SUBJECTS: Subject[] = [
  { id: 1, name: '미적분', color: '#243f6b' },
  { id: 2, name: '자료구조', color: '#4673b3' },
  { id: 3, name: '영어 회화', color: '#5e8b7e' },
  { id: 4, name: '운영체제', color: '#c85a4f' },
  { id: 5, name: '데이터베이스', color: '#7b6ca8' },
]

export const MOCK_TAGS: Tag[] = [
  { id: 1, name: '핵심' },
  { id: 2, name: '복습필요' },
  { id: 3, name: '시험범위' },
  { id: 4, name: '예제' },
  { id: 5, name: '용어정리' },
]

const RAW_EVENTS: Omit<CalendarEvent, 'dDay'>[] = [
  { id: 101, title: '중간고사', subjectId: 1, startAt: '2026-06-07T00:00:00Z', allDay: true, type: 'exam', reminder: '1d' },
  { id: 102, title: '과제 #4 마감', subjectId: 2, startAt: '2026-06-13T14:59:00Z', allDay: false, type: 'assignment', reminder: '1h' },
  { id: 103, title: '발표', subjectId: 3, startAt: '2026-06-18T05:00:00Z', allDay: false, type: 'assignment', reminder: '1d' },
  {
    id: 104,
    title: '주간 퀴즈',
    subjectId: 4,
    startAt: '2026-06-25T05:00:00Z',
    allDay: false,
    type: 'exam',
    recurrence: { freq: 'weekly', interval: 1, byweekday: [4], endMode: 'until', endUntil: '2026-08-31' },
    reminder: '1h',
  },
  {
    id: 105,
    title: '강의',
    subjectId: 1,
    startAt: '2026-06-04T05:00:00Z',
    allDay: false,
    type: 'lecture',
    recurrence: { freq: 'weekly', interval: 1, byweekday: [1, 3], endMode: 'never' },
  },
  {
    id: 106,
    title: '강의',
    subjectId: 2,
    startAt: '2026-06-04T07:00:00Z',
    allDay: false,
    type: 'lecture',
    recurrence: { freq: 'weekly', interval: 1, byweekday: [2, 4], endMode: 'never' },
  },
  { id: 107, title: '쪽지시험', subjectId: 4, startAt: '2026-06-06T05:00:00Z', allDay: false, type: 'exam', reminder: '1h' },
]

export const MOCK_EVENTS: CalendarEvent[] = RAW_EVENTS.map((e) => ({ ...e, dDay: calcDDay(e.startAt) }))

export const MOCK_ATTACHMENTS: Attachment[] = [
  {
    id: 201,
    name: 'limit-graph.svg',
    mimeType: 'image/svg+xml',
    size: 1842,
    url:
      'data:image/svg+xml;utf8,' +
      encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 100">' +
          '<rect width="160" height="100" fill="#e8eef8"/>' +
          '<g stroke="#243f6b" stroke-width="2" fill="none">' +
          '<path d="M10 90 L150 90"/><path d="M20 10 L20 90"/>' +
          '<path d="M20 70 Q60 70 80 50 T140 20"/>' +
          '<circle cx="80" cy="50" r="3" fill="#c85a4f"/></g></svg>',
      ),
    uploadedAt: '2026-05-25T09:00:00Z',
  },
]

const NOTE_BODY_LIMITS = `# 극한의 정의

함수 \`f(x)\`의 \`x → a\` 에서의 극한이 \`L\` 이라는 것은,
**임의의 ε > 0** 에 대해 어떤 *δ > 0* 이 존재하여
다음을 만족하는 것을 의미한다:

> 0 < |x − a| < δ  ⇒  |f(x) − L| < ε
>
> — Karl Weierstrass

---

## 핵심 정리

| 개념 | 기호 | 한 줄 정의 |
| --- | --- | --- |
| 좌극한 | \`lim_{x→a⁻}\` | a 보다 작은 쪽에서의 극한 |
| 우극한 | \`lim_{x→a⁺}\` | a 보다 큰 쪽에서의 극한 |
| 극한 존재 | \`lim_{x→a}\` | 좌·우 극한이 일치할 때 |

## 예제

- \`lim_{x→2} (3x − 1) = 5\` — 다항함수는 대입으로 충분
- \`lim_{x→0} sin(x)/x = 1\` — 샌드위치 정리
- \`lim_{n→∞} (1 + 1/n)^n = e\` — 자연상수의 정의

## 자주 틀리는 포인트

1. 좌극한과 우극한이 일치해야 극한이 존재
2. f(a) 와 극한값은 ~~항상 같다~~ **무관할 수 있음** (연속과의 차이)
3. 정의역 외부의 점에서도 극한은 정의될 수 있다
`

export const MOCK_NOTES: Note[] = [
  {
    id: 301,
    title: '극한의 정의',
    subjectId: 1,
    content: NOTE_BODY_LIMITS,
    tagIds: [1, 3],
    attachmentIds: [201],
    preview: '함수 f(x)의 x → a 에서의 극한이 L 이라는 것은, 임의의 ε > 0 에 대해...',
    updatedAt: '2026-06-04T02:00:00Z',
    createdAt: '2026-05-25T09:00:00Z',
  },
  {
    id: 302,
    title: '연속함수의 성질',
    subjectId: 1,
    content: '## 연속함수\n\n중간값 정리, 최대·최소 정리.',
    tagIds: [2],
    attachmentIds: [],
    preview: '중간값 정리, 최대·최소 정리. 닫힌 구간에서 연속인 함수는...',
    updatedAt: '2026-06-03T11:00:00Z',
    createdAt: '2026-05-26T09:00:00Z',
  },
  {
    id: 303,
    title: '해시 충돌 해결',
    subjectId: 2,
    content: '체이닝(separate chaining)과 오픈 어드레싱.',
    tagIds: [4, 1],
    attachmentIds: [],
    preview: '체이닝(separate chaining)과 오픈 어드레싱. 부하율(load factor) α 가...',
    updatedAt: '2026-06-01T02:00:00Z',
    createdAt: '2026-05-12T09:00:00Z',
  },
  {
    id: 304,
    title: 'Phrasal verbs',
    subjectId: 3,
    content: 'look up / look after / look into.',
    tagIds: [5],
    attachmentIds: [],
    preview: 'look up / look after / look into. 동사 + 부사 또는 동사 + 전치사 결합으로...',
    updatedAt: '2026-05-30T05:00:00Z',
    createdAt: '2026-04-29T09:00:00Z',
  },
  {
    id: 305,
    title: '페이지 교체',
    subjectId: 4,
    content: 'FIFO / LRU / Clock 알고리즘.',
    tagIds: [3],
    attachmentIds: [],
    preview: 'FIFO / LRU / Clock 알고리즘. 메모리 프레임이 가득 찼을 때...',
    updatedAt: '2026-05-28T07:00:00Z',
    createdAt: '2026-04-30T09:00:00Z',
  },
]

export const MOCK_QUESTIONS: Question[] = [
  {
    id: 401,
    type: 'mcq',
    text: '다음 중 함수의 극한에 대한 설명으로 옳은 것은?',
    choices: [
      '함수값 f(a) 와 항상 같다.',
      '좌극한과 우극한이 일치하면 존재한다.',
      '연속이 아닌 점에서는 정의될 수 없다.',
      'ε-δ 정의로만 증명할 수 있다.',
    ],
    correctIndex: 1,
    explanation:
      '좌극한과 우극한이 같을 때 극한이 존재합니다. 함수값과 극한값은 일치하지 않을 수 있고, ε-δ 외에도 다양한 정의/판정법이 있습니다.',
    difficulty: 'medium',
    subjectId: 1,
    conceptTags: ['극한의 정의', '좌·우극한'],
  },
  {
    id: 402,
    type: 'ox',
    text: '함수가 한 점에서 연속이면 그 점에서 항상 미분가능하다.',
    correctBool: false,
    explanation: '연속이지만 미분가능하지 않은 함수가 존재합니다 (예: |x| at x=0).',
    difficulty: 'easy',
    subjectId: 1,
    conceptTags: ['연속', '미분가능성'],
  },
  {
    id: 403,
    type: 'short',
    text: 'lim_{x→0} sin(x)/x 의 값은? (소수 첫째 자리까지)',
    correctText: '1.0',
    explanation: '모범 답안: 1.0. 샌드위치 정리로 증명할 수 있습니다.',
    difficulty: 'medium',
    subjectId: 1,
    conceptTags: ['대표 극한', '샌드위치 정리'],
  },
  {
    id: 404,
    type: 'mcq',
    text: '해시 충돌 해결 기법이 아닌 것은?',
    choices: ['Separate chaining', 'Open addressing', 'Linear probing', 'Quicksort'],
    correctIndex: 3,
    explanation: 'Quicksort 는 정렬 알고리즘입니다.',
    difficulty: 'easy',
    subjectId: 2,
    conceptTags: ['해시 충돌'],
  },
]

export const MOCK_QUIZZES: Quiz[] = [
  {
    id: 501,
    title: '미적분 — 극한 단원',
    subjectId: 1,
    questionIds: [401, 402, 403],
    generatedFromNoteId: 301,
    generatedBy: 'ai-gpt',
    createdAt: '2026-06-03T09:00:00Z',
  },
  {
    id: 502,
    title: '자료구조 빠른 점검',
    subjectId: 2,
    questionIds: [404],
    generatedBy: 'manual',
    createdAt: '2026-05-20T09:00:00Z',
  },
]

export const MOCK_WRONG: WrongAnswerNote[] = [
  {
    questionId: 401,
    missCount: 3,
    lastAttemptId: 601,
    lastMissedAt: '2026-06-03T10:00:00Z',
    concept: 'ε-δ 정의',
    question: MOCK_QUESTIONS[0],
  },
  {
    questionId: 403,
    missCount: 1,
    lastAttemptId: 601,
    lastMissedAt: '2026-06-02T07:00:00Z',
    concept: '대표 극한',
    question: MOCK_QUESTIONS[2],
  },
  {
    questionId: 402,
    missCount: 2,
    lastAttemptId: 601,
    lastMissedAt: '2026-06-01T07:00:00Z',
    concept: '연속 vs 미분',
    question: MOCK_QUESTIONS[1],
  },
  {
    questionId: 404,
    missCount: 4,
    lastAttemptId: 601,
    lastMissedAt: '2026-05-31T07:00:00Z',
    concept: '해시 충돌',
    question: MOCK_QUESTIONS[3],
  },
]

export const MOCK_PRIORITY: PriorityRecommendation[] = [
  { rank: 1, subjectId: 1, reason: 'D-3 시험 임박 + 정답률 62%', dDay: 3, accuracy: 62, tone: 'urgent' },
  { rank: 2, subjectId: 4, reason: '과목 평균 대비 18%p 낮음', dDay: 21, accuracy: 55, tone: 'warning' },
  { rank: 3, subjectId: 2, reason: 'D-9 과제 마감 임박', dDay: 9, accuracy: 84, tone: 'normal' },
]

export const MOCK_WEAK_CONCEPTS: WeakConcept[] = [
  { concept: '극한의 정의', subjectId: 1, missCount: 4, totalAttempts: 5, relatedKeywords: ['ε-δ 정의'] },
  { concept: '해시 충돌', subjectId: 2, missCount: 3, totalAttempts: 6, relatedKeywords: ['체이닝', '오픈 어드레싱'] },
  { concept: '페이지 교체', subjectId: 4, missCount: 5, totalAttempts: 7, relatedKeywords: ['LRU', 'FIFO'] },
]

export const MOCK_UPCOMING_SUBJECTS: UpcomingSubject[] = [
  { subjectId: 1, dDay: 3, accuracy: 62, priorityScore: 92 },
  { subjectId: 2, dDay: 9, accuracy: 84, priorityScore: 58 },
  { subjectId: 3, dDay: 14, accuracy: 71, priorityScore: 46 },
  { subjectId: 4, dDay: 21, accuracy: 55, priorityScore: 38 },
]

export const MOCK_SUMMARY: NoteSummary = {
  noteId: 301,
  sourceCharCount: 1840,
  summaryCharCount: 232,
  generatedAt: '2026-06-04T03:10:00Z',
  bullets: [
    '함수 f(x)의 x → a 극한 L은 ε-δ 정의에 의해, 임의의 ε > 0 에 대해 적절한 δ > 0 가 존재함을 의미한다.',
    '좌극한·우극한이 같아야 극한이 존재하며, 극한값은 f(a)와 별개의 개념이다 (연속과 구별).',
    '대표 예: lim sin(x)/x = 1, lim (1 + 1/n)^n = e. 자주 틀리는 포인트는 정의역 외부에서의 극한.',
  ],
}

export const MOCK_WEEKLY_STUDY: WeeklyStudyPoint[] = [
  { weekday: 1, weekdayLabel: '월', studyMinutes: 150 },
  { weekday: 2, weekdayLabel: '화', studyMinutes: 192 },
  { weekday: 3, weekdayLabel: '수', studyMinutes: 108 },
  { weekday: 4, weekdayLabel: '목', studyMinutes: 246 },
  { weekday: 5, weekdayLabel: '금', studyMinutes: 156 },
  { weekday: 6, weekdayLabel: '토', studyMinutes: 324 },
  { weekday: 0, weekdayLabel: '일', studyMinutes: 228 },
]

export const MOCK_SUBJECT_PROGRESS: SubjectProgress[] = [
  { subjectId: 1, accuracy: 62, attempted: 38, total: 60 },
  { subjectId: 2, accuracy: 84, attempted: 42, total: 50 },
  { subjectId: 3, accuracy: 71, attempted: 21, total: 30 },
  { subjectId: 4, accuracy: 55, attempted: 22, total: 40 },
]

export const MOCK_ACCURACY_TREND: AccuracyTrendPoint[] = [
  { attemptedAt: '2026-05-08T09:00:00Z', accuracy: 62 },
  { attemptedAt: '2026-05-11T09:00:00Z', accuracy: 68 },
  { attemptedAt: '2026-05-14T09:00:00Z', accuracy: 65 },
  { attemptedAt: '2026-05-17T09:00:00Z', accuracy: 71 },
  { attemptedAt: '2026-05-20T09:00:00Z', accuracy: 74 },
  { attemptedAt: '2026-05-22T09:00:00Z', accuracy: 73 },
  { attemptedAt: '2026-05-24T09:00:00Z', accuracy: 78 },
  { attemptedAt: '2026-05-26T09:00:00Z', accuracy: 76 },
  { attemptedAt: '2026-05-28T09:00:00Z', accuracy: 81 },
  { attemptedAt: '2026-05-30T09:00:00Z', accuracy: 79 },
  { attemptedAt: '2026-06-01T09:00:00Z', accuracy: 82 },
  { attemptedAt: '2026-06-03T09:00:00Z', accuracy: 78 },
]

export const MOCK_NOTICES: Notice[] = [
  {
    id: 5,
    title: '6월 정기 점검 안내',
    body: '6월 12일 02:00 ~ 04:00 점검이 진행됩니다.',
    tag: '공지',
    author: '운영팀',
    pinned: true,
    viewCount: 412,
    createdAt: '2026-05-26T01:00:00Z',
    updatedAt: '2026-05-26T01:00:00Z',
  },
  {
    id: 4,
    title: 'AI 퀴즈 생성 무료 베타 종료 예정 안내',
    body: '6월 30일까지 모든 사용자에게 무료로 제공됩니다.',
    tag: '베타',
    author: '운영팀',
    pinned: false,
    viewCount: 681,
    createdAt: '2026-05-22T01:00:00Z',
    updatedAt: '2026-05-22T01:00:00Z',
  },
  {
    id: 3,
    title: '카카오 OAuth 점검으로 인한 일시 중단',
    body: '카카오 OAuth 로그인이 일시 중단됩니다.',
    tag: '점검',
    author: '운영팀',
    pinned: false,
    viewCount: 232,
    createdAt: '2026-05-18T01:00:00Z',
    updatedAt: '2026-05-18T01:00:00Z',
  },
  {
    id: 2,
    title: '약관 개정 안내 (2026-06-01 시행)',
    body: '약관이 일부 개정됩니다.',
    tag: '약관',
    author: '운영팀',
    pinned: false,
    viewCount: 1024,
    createdAt: '2026-05-10T01:00:00Z',
    updatedAt: '2026-05-10T01:00:00Z',
  },
  {
    id: 1,
    title: 'Pokemo 정식 출시',
    body: 'Pokemo 정식 출시를 환영합니다.',
    tag: '런칭',
    author: '운영팀',
    pinned: false,
    viewCount: 3502,
    createdAt: '2026-04-15T01:00:00Z',
    updatedAt: '2026-04-15T01:00:00Z',
  },
]
