import { statsApi } from '../../api/statsApi'
import { subjectById } from '../../api/subjectApi'
import type { AccuracyTrendPoint, SubjectProgress, WeeklyStudyPoint } from '../../types'
import { useApi } from '../../useApi'

export function StatsScreen() {
  const { data: weekly, loading: weeklyLoading } = useApi(() => statsApi.getWeeklyStudy(), [])
  const { data: progress, loading: progressLoading } = useApi(() => statsApi.getSubjectProgress(), [])
  const { data: trend, loading: trendLoading } = useApi(() => statsApi.getAccuracyTrend(), [])

  const loading = weeklyLoading || progressLoading || trendLoading

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">통계</p>
        <h1 className="screen__heading">학습 통계</h1>
        <p className="screen__lede">퀴즈 풀이 기록을 바탕으로 학습 현황을 분석합니다.</p>
      </header>

      {loading ? (
        <div className="surface" style={{ height: 300, opacity: 0.4 }} />
      ) : (
        <div style={{ display: 'grid', gap: 20 }}>
          <WeeklyChart data={weekly ?? []} />
          <SubjectProgressChart data={progress ?? []} />
          <AccuracyTrendChart data={trend ?? []} />
        </div>
      )}
    </div>
  )
}

function WeeklyChart({ data }: { data: WeeklyStudyPoint[] }) {
  const max = Math.max(...data.map((d) => d.studyMinutes), 1)
  const chartH = 120
  const barW = 32
  const gap = 12
  const totalW = data.length * (barW + gap)

  return (
    <section className="surface">
      <div className="surface__title">
        <h2>주간 공부시간</h2>
        <span className="tag">분 단위</span>
      </div>
      {data.every((d) => d.studyMinutes === 0) ? (
        <p className="muted-note" style={{ textAlign: 'center', padding: 32 }}>
          아직 기록된 공부시간이 없습니다.
        </p>
      ) : (
        <svg width="100%" viewBox={`0 0 ${totalW} ${chartH + 32}`} style={{ overflow: 'visible' }}>
          {data.map((d, i) => {
            const barH = (d.studyMinutes / max) * chartH
            const x = i * (barW + gap)
            const y = chartH - barH
            return (
              <g key={d.weekday}>
                <rect
                  x={x}
                  y={y}
                  width={barW}
                  height={barH}
                  rx={4}
                  fill="var(--color-accent, #4673b3)"
                  opacity={0.85}
                />
                <text x={x + barW / 2} y={chartH + 16} textAnchor="middle" fontSize={12} fill="var(--color-muted)">
                  {d.weekdayLabel}
                </text>
                {d.studyMinutes > 0 && (
                  <text x={x + barW / 2} y={y - 4} textAnchor="middle" fontSize={10} fill="var(--color-muted)">
                    {d.studyMinutes}
                  </text>
                )}
              </g>
            )
          })}
        </svg>
      )}
    </section>
  )
}

function SubjectProgressChart({ data }: { data: SubjectProgress[] }) {
  return (
    <section className="surface">
      <div className="surface__title">
        <h2>과목별 성취도</h2>
        <span className="tag">정답률</span>
      </div>
      {data.length === 0 ? (
        <p className="muted-note" style={{ textAlign: 'center', padding: 32 }}>
          아직 풀이한 퀴즈가 없습니다.
        </p>
      ) : (
        <div style={{ display: 'grid', gap: 16, marginTop: 8 }}>
          {data.map((s) => {
            const subject = subjectById(s.subjectId)
            const name = subject?.name ?? `과목 ${s.subjectId}`
            const color = subject?.color ?? 'var(--color-accent)'
            return (
              <div key={s.subjectId}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                  <span style={{ fontSize: 14, fontWeight: 600 }}>{name}</span>
                  <span style={{ fontSize: 14, color: 'var(--color-muted)' }}>
                    {s.accuracy}% · {s.attempted}문제
                  </span>
                </div>
                <div style={{ height: 8, background: 'var(--color-surface-alt, #eef2f8)', borderRadius: 4, overflow: 'hidden' }}>
                  <div
                    style={{
                      height: '100%',
                      width: `${s.accuracy}%`,
                      background: color,
                      borderRadius: 4,
                      transition: 'width 0.4s ease',
                    }}
                  />
                </div>
              </div>
            )
          })}
        </div>
      )}
    </section>
  )
}

function AccuracyTrendChart({ data }: { data: AccuracyTrendPoint[] }) {
  const W = 500
  const H = 140
  const pad = { top: 16, right: 16, bottom: 24, left: 32 }
  const innerW = W - pad.left - pad.right
  const innerH = H - pad.top - pad.bottom

  const points = data.slice().reverse()

  function toX(i: number) {
    return pad.left + (points.length <= 1 ? innerW / 2 : (i / (points.length - 1)) * innerW)
  }
  function toY(acc: number) {
    return pad.top + innerH - (acc / 100) * innerH
  }

  const polyline =
    points.length > 1
      ? points.map((p, i) => `${toX(i)},${toY(p.accuracy)}`).join(' ')
      : ''

  return (
    <section className="surface">
      <div className="surface__title">
        <h2>정답률 추이</h2>
        <span className="tag">{data.length}회</span>
      </div>
      {data.length === 0 ? (
        <p className="muted-note" style={{ textAlign: 'center', padding: 32 }}>
          아직 시도 기록이 없습니다.
        </p>
      ) : (
        <svg width="100%" viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="xMidYMid meet">
          {[0, 25, 50, 75, 100].map((v) => (
            <g key={v}>
              <line
                x1={pad.left}
                x2={W - pad.right}
                y1={toY(v)}
                y2={toY(v)}
                stroke="var(--color-border, #e2e8f0)"
                strokeWidth={1}
              />
              <text x={pad.left - 4} y={toY(v) + 4} textAnchor="end" fontSize={10} fill="var(--color-muted)">
                {v}
              </text>
            </g>
          ))}
          {polyline && (
            <polyline points={polyline} fill="none" stroke="var(--color-accent, #4673b3)" strokeWidth={2} />
          )}
          {points.map((p, i) => (
            <circle key={i} cx={toX(i)} cy={toY(p.accuracy)} r={4} fill="var(--color-accent, #4673b3)" />
          ))}
        </svg>
      )}
    </section>
  )
}
