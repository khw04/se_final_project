import { Icon } from '../components/Icon'
import { pokemoApi } from '../pokemoApi'
import type { PriorityRecommendation, UpcomingSubject, WeakConcept } from '../types'
import { useApi } from '../useApi'

export function RecommendScreen() {
  const { data, loading } = useApi(() => pokemoApi.getRecommend(), [])

  if (loading || !data) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">AI 학습 추천</p>
          <h1 className="screen__heading">오늘의 학습 안내</h1>
        </header>
        <div className="surface" style={{ height: 200, opacity: 0.4 }} />
      </div>
    )
  }

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">
          <Icon name="sparkle" size={14} style={{ marginRight: 6, verticalAlign: -2 }} />
          AI 학습 추천
        </p>
        <h1 className="screen__heading">오늘의 학습 안내</h1>
        <p className="screen__lede">
          정답률 데이터와 캘린더 D-Day, 노트 내용을 종합해 AI가 우선순위를 제안합니다. AI 응답에 오류가 있어도 정답률
          데이터는 항상 함께 표시됩니다.
        </p>
      </header>

      <PriorityRail items={data.priorities} />

      <div className="recommend-cols">
        <WeakConceptCard concepts={data.weakConcepts} />
        <UpcomingSubjectCard subjects={data.upcomingSubjects} />
      </div>

      <NoteSummaryCard />
    </div>
  )
}

function PriorityRail({ items }: { items: PriorityRecommendation[] }) {
  return (
    <section className="surface">
      <div className="surface__title">
        <h2>우선 학습 순위</h2>
        <span className="tag tag--accent">D-Day + 정답률 가중</span>
      </div>
      <ol className="priority-rail">
        {items.map((item) => {
          const subject = pokemoApi.subjectById(item.subjectId)
          return (
            <li key={item.rank} className={`priority-row priority-row--${item.tone}`}>
              <span className="priority-row__rank">{item.rank}</span>
              <div>
                <p className="priority-row__subject">{subject?.name}</p>
                <p className="priority-row__reason">{item.reason}</p>
              </div>
              <div className="priority-row__metrics">
                {item.dDay != null ? (
                  <span className={`dday ${item.tone === 'urgent' ? 'dday--urgent' : ''}`}>D-{item.dDay}</span>
                ) : null}
                <span className="priority-row__rate">{item.accuracy}%</span>
              </div>
              <button type="button" className="priority-row__cta" aria-label="학습 시작">
                <Icon name="arrowRight" size={16} />
              </button>
            </li>
          )
        })}
      </ol>
    </section>
  )
}

function WeakConceptCard({ concepts }: { concepts: WeakConcept[] }) {
  return (
    <section className="surface">
      <div className="surface__title">
        <h2>
          <Icon name="sparkle" size={18} style={{ marginRight: 6 }} />
          취약 개념 분석
        </h2>
        <button type="button" className="surface__title-action">
          새로고침
        </button>
      </div>
      <p className="muted-note">
        최근 30일 오답에서 추출한 개념 리스트입니다. AI 응답이 없을 때는 오답 빈도만 표시됩니다.
      </p>
      <ul className="weak-list">
        {concepts.map((concept, i) => {
          const subject = pokemoApi.subjectById(concept.subjectId)
          return (
            <li key={i}>
              <div>
                <p className="weak-list__concept">{concept.concept}</p>
                <p className="weak-list__meta">
                  {subject?.name} · 관련: {concept.relatedKeywords.join(', ')}
                </p>
              </div>
              <span className="weak-list__ratio">
                <strong>{concept.missCount}</strong>
                <span> / {concept.totalAttempts}</span>
              </span>
            </li>
          )
        })}
      </ul>
      <button type="button" className="surface__title-action" style={{ marginTop: 16, width: '100%' }}>
        <Icon name="refresh" size={14} style={{ marginRight: 6 }} />
        취약 개념 위주 퀴즈 생성
      </button>
    </section>
  )
}

function UpcomingSubjectCard({ subjects }: { subjects: UpcomingSubject[] }) {
  return (
    <section className="surface">
      <div className="surface__title">
        <h2>
          <Icon name="calendar" size={18} style={{ marginRight: 6 }} />
          시험 임박 과목
        </h2>
        <span className="tag tag--accent">정답률 + D-Day</span>
      </div>
      <p className="muted-note">시험까지 남은 일수가 짧고 정답률이 낮은 과목을 우선으로 정렬합니다.</p>
      <ul className="upcoming-subjects">
        {subjects.map((subject, i) => {
          const meta = pokemoApi.subjectById(subject.subjectId)
          return (
            <li key={i}>
              <div>
                <p className="upcoming-subjects__name">{meta?.name}</p>
                <p className="upcoming-subjects__meta">
                  D-{subject.dDay} · 정답률 {subject.accuracy}%
                </p>
              </div>
              <div className="urgency-bar">
                <div className="urgency-bar__fill" style={{ width: `${Math.min(100, subject.priorityScore)}%` }} />
              </div>
            </li>
          )
        })}
      </ul>
    </section>
  )
}

function NoteSummaryCard() {
  const { data: summary, loading, refetch } = useApi(() => pokemoApi.summarizeNote(301), [])

  return (
    <section className="surface">
      <div className="surface__title">
        <h2>
          <Icon name="book" size={18} style={{ marginRight: 6 }} />
          학습 내용 요약 (★AI)
        </h2>
        <button type="button" className="surface__title-action" onClick={refetch} disabled={loading}>
          <Icon name="refresh" size={14} style={{ marginRight: 6 }} />
          {loading ? '요약 중...' : '다시 요약'}
        </button>
      </div>
      <div className="summary-block">
        {loading || !summary ? (
          <p className="muted-note">GPT/Gemini API 로 노트를 요약하는 중입니다.</p>
        ) : (
          <>
            <p className="muted-note">
              미적분 · "극한의 정의" 노트 ({summary.sourceCharCount.toLocaleString()}자 →{' '}
              {summary.summaryCharCount}자 요약)
              {summary.fallback ? ' · AI 실패 → 첫 문단 표시' : ''}
            </p>
            <h3 style={{ fontSize: 18, marginTop: 12 }}>핵심 {summary.bullets.length}줄</h3>
            <ul className="summary-list">
              {summary.bullets.map((bullet, i) => (
                <li key={i} dangerouslySetInnerHTML={{ __html: bullet.replace(/`([^`]+)`/g, '<code>$1</code>') }} />
              ))}
            </ul>
            <div className="summary-actions">
              <button type="button" className="surface__title-action">
                <Icon name="brain" size={14} style={{ marginRight: 6 }} />이 요약으로 퀴즈 생성
              </button>
              <button type="button" className="surface__title-action">
                <Icon name="book" size={14} style={{ marginRight: 6 }} />
                원본 노트 열기
              </button>
            </div>
          </>
        )}
      </div>
    </section>
  )
}
