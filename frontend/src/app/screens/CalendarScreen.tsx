import { Icon } from '../components/Icon'
import { pokemoApi } from '../pokemoApi'
import type { CalendarEvent } from '../types'
import { useApi } from '../useApi'

const YEAR = 2026
const MONTH = 6
const TODAY_DAY = 4
const DAYS_IN_MONTH = 30
const FIRST_WEEKDAY = 1

export function CalendarScreen() {
  const { data: events, loading } = useApi(
    () => pokemoApi.getEvents({ from: '2026-06-01', to: '2026-06-30' }),
    [],
  )

  if (loading || !events) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">학습 일정</p>
          <h1 className="screen__heading">캘린더</h1>
        </header>
        <div className="surface" style={{ height: 400, opacity: 0.4 }} />
      </div>
    )
  }

  const dayMap: Record<number, CalendarEvent[]> = {}
  for (const ev of events) {
    const d = new Date(ev.startAt)
    if (d.getUTCFullYear() === YEAR && d.getUTCMonth() + 1 === MONTH) {
      const day = d.getUTCDate()
      ;(dayMap[day] ||= []).push(ev)
    }
    if (ev.recurrence?.freq === 'weekly') {
      const start = new Date(ev.startAt)
      for (let day = 1; day <= DAYS_IN_MONTH; day += 1) {
        const candidate = new Date(Date.UTC(YEAR, MONTH - 1, day))
        if (candidate >= start && ev.recurrence.byweekday?.includes(candidate.getUTCDay())) {
          if (candidate.toISOString().slice(0, 10) === ev.startAt.slice(0, 10)) continue
          ;(dayMap[day] ||= []).push(ev)
        }
      }
    }
  }

  const upcoming = events.filter((e) => e.dDay >= 0).slice(0, 5)

  type Cell = { blank: true; key: string } | { blank: false; key: number; day: number }
  const cells: Cell[] = []
  for (let i = 0; i < FIRST_WEEKDAY; i += 1) cells.push({ blank: true, key: `b${i}` })
  for (let day = 1; day <= DAYS_IN_MONTH; day += 1) cells.push({ blank: false, key: day, day })

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">학습 일정</p>
        <h1 className="screen__heading">캘린더</h1>
        <p className="screen__lede">과제·시험을 한눈에 확인하고, 반복 일정도 함께 관리합니다.</p>
      </header>

      <div className="calendar-grid">
        <section className="surface">
          <div className="surface__title">
            <h2>
              {YEAR}년 {MONTH}월
            </h2>
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="button" className="surface__title-action">
                <Icon name="plus" size={14} style={{ marginRight: 4 }} />
                일정 추가
              </button>
              <button type="button" className="surface__title-action" aria-label="이전 달">
                ‹
              </button>
              <button type="button" className="surface__title-action" aria-label="다음 달">
                ›
              </button>
            </div>
          </div>

          <div className="cal-grid">
            <div className="cal-weekday">월</div>
            <div className="cal-weekday">화</div>
            <div className="cal-weekday">수</div>
            <div className="cal-weekday">목</div>
            <div className="cal-weekday">금</div>
            <div className="cal-weekday cal-weekday--sat">토</div>
            <div className="cal-weekday cal-weekday--sun">일</div>

            {cells.map((cell) =>
              cell.blank ? (
                <div key={cell.key} className="cal-cell cal-cell--blank" />
              ) : (
                <div key={cell.key} className={`cal-cell ${cell.day === TODAY_DAY ? 'cal-cell--today' : ''}`}>
                  <span className="cal-cell__num">{cell.day}</span>
                  <div className="cal-cell__events">
                    {(dayMap[cell.day] || []).slice(0, 2).map((ev) => {
                      const subject = pokemoApi.subjectById(ev.subjectId)
                      const tone = ev.type === 'exam' ? 'warning' : 'accent'
                      return (
                        <span
                          key={`${ev.id}-${cell.day}`}
                          className={`cal-cell__chip cal-cell__chip--${tone}`}
                          title={subject?.name}
                        >
                          {subject?.name}
                        </span>
                      )
                    })}
                  </div>
                </div>
              ),
            )}
          </div>
        </section>

        <section className="surface">
          <div className="surface__title">
            <h2>다가오는 일정</h2>
            <button type="button" className="surface__title-action">
              필터
            </button>
          </div>
          <ul className="upcoming-list">
            {upcoming.map((event) => {
              const subject = pokemoApi.subjectById(event.subjectId)
              const tone = event.dDay <= 3 ? 'urgent' : 'normal'
              const dateLabel = new Date(event.startAt).toLocaleDateString('ko-KR', {
                month: 'long',
                day: 'numeric',
              })
              return (
                <li key={event.id}>
                  <span className={`dday ${tone === 'urgent' ? 'dday--urgent' : ''}`}>D-{event.dDay}</span>
                  <div className="upcoming-list__body">
                    <p className="upcoming-list__title">{event.title}</p>
                    <p className="upcoming-list__meta">
                      {dateLabel} · {subject?.name}
                      {event.recurrence ? (
                        <span className="tag tag--accent" style={{ marginLeft: 8 }}>
                          매주
                        </span>
                      ) : null}
                    </p>
                  </div>
                </li>
              )
            })}
          </ul>

          <div className="reminder-banner">
            <Icon name="bell" size={18} style={{ color: 'var(--color-accent-strong)' }} />
            <div>
              <p style={{ margin: 0, color: 'var(--color-ink)', fontWeight: 700, fontSize: 14 }}>
                웹 푸시 알림이 켜져 있어요
              </p>
              <p style={{ margin: '2px 0 0', color: 'var(--color-text)', fontSize: 13 }}>
                임박한 일정은 1시간 전 알림으로 받습니다.
              </p>
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
