import type { AuthSession } from '../../../lib/authApi'

import { dashboardApi } from '../../api/dashboardApi'
import { subjectById } from '../../api/subjectApi'
import { Icon } from '../../components/Icon'
import type {
  AccuracyTrendPoint,
  CalendarEvent,
  PriorityRecommendation,
  SubjectProgress,
  ViewId,
  WeeklyStudyPoint,
} from '../../types'
import { useApi } from '../../useApi'

type Props = {
  session: AuthSession
  onJumpTo: (view: ViewId) => void
}

export function DashboardScreen({ session, onJumpTo }: Props) {
  const greeting = session.email.split('@')[0]
  const { data, loading } = useApi(() => dashboardApi.getDashboard(), [])

  if (loading || !data) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">학습 대시보드</p>
          <h1 className="screen__heading">안녕하세요, {greeting}님</h1>
          <p className="screen__lede" style={{ opacity: 0.5 }}>
            학습 데이터를 불러오는 중입니다.
          </p>
        </header>
        <div className="dashboard-grid">
          {[0, 1, 2, 3].map((i) => (
            <div key={i} className="surface" style={{ height: 240, opacity: 0.4 }} />
          ))}
        </div>
      </div>
    )
  }

  const latest = data.accuracyTrend[data.accuracyTrend.length - 1]

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">학습 대시보드</p>
        <h1 className="screen__heading">안녕하세요, {greeting}님</h1>
        <p className="screen__lede">
          이번 주 평균 정답률은 {latest?.accuracy ?? 0}%, 다가오는 일정 {data.upcomingExams.length}건이 있습니다.
          임박한 과목부터 차근차근 살펴보세요.
        </p>
      </header>

      <DDayStrip events={data.upcomingExams} onJumpTo={onJumpTo} />

      <div className="dashboard-grid">
        <WeeklyStudyCard points={data.weeklyStudy} />
        <SubjectProgressCard rows={data.subjectProgress} onAll={() => onJumpTo('recommend')} />
        <AiRecommendCard items={data.recommendation} onAll={() => onJumpTo('recommend')} />
        <QuizTrendCard points={data.accuracyTrend} />
      </div>

      <section className="surface">
        <div className="surface__title">
          <h2>빠른 이동</h2>
        </div>
        <div className="dashboard-shortcuts">
          <button className="dashboard-shortcut" type="button" onClick={() => onJumpTo('notes')}>
            <Icon name="book" size={18} />
            <span>새 노트 시작</span>
          </button>
          <button className="dashboard-shortcut" type="button" onClick={() => onJumpTo('quiz')}>
            <Icon name="brain" size={18} />
            <span>퀴즈 풀기</span>
          </button>
          <button className="dashboard-shortcut" type="button" onClick={() => onJumpTo('wrong')}>
            <Icon name="x" size={18} />
            <span>오답 다시 풀기</span>
          </button>
          <button className="dashboard-shortcut" type="button" onClick={() => onJumpTo('calendar')}>
            <Icon name="calendar" size={18} />
            <span>일정 추가</span>
          </button>
        </div>
      </section>
    </div>
  )
}

function DDayStrip({ events, onJumpTo }: { events: CalendarEvent[]; onJumpTo: (v: ViewId) => void }) {
  if (events.length === 0) {
    return (
      <section className="surface" style={{ padding: 'var(--space-4) var(--space-5)' }}>
        <p style={{ margin: 0, color: 'var(--color-muted)' }}>다가오는 일정이 없습니다.</p>
      </section>
    )
  }

  return (
    <section className="dday-strip" aria-label="D-Day 일정">
      {events.map((ev) => {
        const subject = subjectById(ev.subjectId)
        const urgent = ev.dDay <= 3
        return (
          <button key={ev.id} type="button" className="dday-card" onClick={() => onJumpTo('calendar')}>
            <span className={`dday ${urgent ? 'dday--urgent' : ''}`}>D-{ev.dDay}</span>
            <div>
              <p className="dday-card__subject">{subject?.name}</p>
              <p className="dday-card__title">{ev.title}</p>
            </div>
          </button>
        )
      })}
    </section>
  )
}

function WeeklyStudyCard({ points }: { points: WeeklyStudyPoint[] }) {
  const maxMin = Math.max(360, ...points.map((p) => p.studyMinutes))
  const totalH = (points.reduce((sum, p) => sum + p.studyMinutes, 0) / 60).toFixed(1)

  return (
    <section className="surface dashboard-card">
      <div className="surface__title">
        <h2>주간 공부 시간</h2>
        <span className="tag tag--accent">{totalH}h · 이번 주</span>
      </div>
      <div className="bars">
        {points.map((p) => (
          <div key={p.weekday} className="bar">
            <div className="bar__track">
              <div className="bar__fill" style={{ height: `${(p.studyMinutes / maxMin) * 100}%` }}>
                <span className="bar__label">{(p.studyMinutes / 60).toFixed(1)}h</span>
              </div>
            </div>
            <span className="bar__day">{p.weekdayLabel}</span>
          </div>
        ))}
      </div>
    </section>
  )
}

function SubjectProgressCard({ rows, onAll }: { rows: SubjectProgress[]; onAll: () => void }) {
  return (
    <section className="surface dashboard-card">
      <div className="surface__title">
        <h2>과목별 성취도</h2>
        <button type="button" className="surface__title-action" onClick={onAll}>
          전체 보기
        </button>
      </div>
      <div className="progress-list">
        {rows.map((row) => {
          const subject = subjectById(row.subjectId)
          const tone = row.accuracy >= 70 ? 'accent' : 'warning'
          return (
            <div key={row.subjectId} className="progress-row">
              <span className="progress-row__name">{subject?.name}</span>
              <div className="progress-row__track">
                <div className={`progress-row__fill progress-row__fill--${tone}`} style={{ width: `${row.accuracy}%` }} />
              </div>
              <span className="progress-row__pct">{row.accuracy}%</span>
            </div>
          )
        })}
      </div>
    </section>
  )
}

function AiRecommendCard({ items, onAll }: { items: PriorityRecommendation[]; onAll: () => void }) {
  return (
    <section className="surface dashboard-card">
      <div className="surface__title">
        <h2>
          <Icon name="sparkle" size={20} style={{ marginRight: 8 }} />
          AI 추천
        </h2>
        <button type="button" className="surface__title-action" onClick={onAll}>
          새로고침
        </button>
      </div>
      <ul className="recommend-list">
        {items.map((item) => {
          const subject = subjectById(item.subjectId)
          const kind = item.tone === 'urgent' ? '시험 임박' : item.tone === 'warning' ? '취약 과목' : '학습 추천'
          return (
            <li key={item.rank}>
              <span className={`tag ${item.tone === 'urgent' ? 'tag--warning' : 'tag--accent'}`}>{kind}</span>
              <div>
                <p className="recommend-list__label">{subject?.name}</p>
                <p className="recommend-list__detail">{item.reason}</p>
              </div>
              <button type="button" className="recommend-list__cta" aria-label={`${subject?.name} 학습 시작`}>
                <Icon name="arrowRight" size={16} />
              </button>
            </li>
          )
        })}
      </ul>
    </section>
  )
}

function QuizTrendCard({ points }: { points: AccuracyTrendPoint[] }) {
  if (points.length === 0) return null
  const values = points.map((p) => p.accuracy)
  const max = Math.max(...values, 90)
  const min = Math.min(...values, 50)
  const w = 320
  const h = 120
  const pad = 10
  const stepX = (w - pad * 2) / Math.max(values.length - 1, 1)
  const yFor = (v: number) => h - pad - ((v - min) / Math.max(max - min, 1)) * (h - pad * 2)
  const linePath = values.map((v, i) => `${i === 0 ? 'M' : 'L'} ${pad + i * stepX} ${yFor(v)}`).join(' ')
  const areaPath = `${linePath} L ${pad + (values.length - 1) * stepX} ${h - pad} L ${pad} ${h - pad} Z`

  const latest = values[values.length - 1]
  const avg = Math.round(values.reduce((sum, v) => sum + v, 0) / values.length)
  const best = Math.max(...values)

  return (
    <section className="surface dashboard-card">
      <div className="surface__title">
        <h2>퀴즈 정답률 추이</h2>
        <span className="tag tag--accent">최근 {values.length}회</span>
      </div>
      <svg viewBox={`0 0 ${w} ${h}`} width="100%" height="120" aria-hidden="true">
        <path d={areaPath} fill="var(--color-accent-soft)" opacity={0.5} />
        <path d={linePath} fill="none" stroke="var(--color-accent)" strokeWidth={2.4} strokeLinejoin="round" />
        {values.map((v, i) => (
          <circle
            key={points[i]?.attemptedAt ?? v}
            cx={pad + i * stepX}
            cy={yFor(v)}
            r={i === values.length - 1 ? 4 : 2.5}
            fill={i === values.length - 1 ? 'var(--color-accent)' : '#fff'}
            stroke="var(--color-accent)"
            strokeWidth={1.6}
          />
        ))}
      </svg>
      <div className="trend-stats">
        <div>
          <p className="label">최근</p>
          <p className="trend-stats__big">{latest}%</p>
        </div>
        <div>
          <p className="label">평균</p>
          <p className="trend-stats__big">{avg}%</p>
        </div>
        <div>
          <p className="label">최고</p>
          <p className="trend-stats__big">{best}%</p>
        </div>
      </div>
    </section>
  )
}
