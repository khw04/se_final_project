import { useState } from 'react'

import { calendarApi } from '../api/calendarApi'
import { subjectApi } from '../api/subjectApi'
import { Icon } from '../components/Icon'
import type { CalendarEvent, RecurrenceRule } from '../types'
import { useApi } from '../useApi'

const EVENT_TYPES = [
  { value: 'exam', label: '시험' },
  { value: 'assignment', label: '과제' },
  { value: 'lecture', label: '강의' },
  { value: 'other', label: '기타' },
]

const REMINDER_OPTIONS = [
  { value: '', label: '안 함' },
  { value: '15m', label: '15분 전' },
  { value: '1h', label: '1시간 전' },
  { value: '1d', label: '1일 전' },
]

// byweekday는 표시 로직(getUTCDay)과 동일하게 0=일 ~ 6=토 규칙을 사용한다.
const WEEKDAYS = [
  { value: 1, label: '월' },
  { value: 2, label: '화' },
  { value: 3, label: '수' },
  { value: 4, label: '목' },
  { value: 5, label: '금' },
  { value: 6, label: '토' },
  { value: 0, label: '일' },
]

type RepeatMode = 'none' | 'weekly' | 'monthly'

function pad(n: number) {
  return String(n).padStart(2, '0')
}

export function CalendarScreen() {
  const todayDate = new Date()
  const [year, setYear] = useState(todayDate.getFullYear())
  const [month, setMonth] = useState(todayDate.getMonth() + 1)
  const [showAddForm, setShowAddForm] = useState(false)
  const [formTitle, setFormTitle] = useState('')
  const [formDate, setFormDate] = useState(`${todayDate.getFullYear()}-${pad(todayDate.getMonth() + 1)}-${pad(todayDate.getDate())}`)
  const [formType, setFormType] = useState('other')
  const [formSubjectId, setFormSubjectId] = useState<string>('')
  const [formReminder, setFormReminder] = useState('')
  const [formRepeat, setFormRepeat] = useState<RepeatMode>('none')
  const [formWeekdays, setFormWeekdays] = useState<number[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const daysInMonth = new Date(year, month, 0).getDate()
  const firstWeekday = (new Date(year, month - 1, 1).getDay() + 6) % 7

  const fromStr = `${year}-${pad(month)}-01`
  const toStr = `${year}-${pad(month)}-${pad(daysInMonth)}`

  const { data: events, loading, refetch } = useApi(
    () => calendarApi.getEvents({ from: fromStr, to: toStr }),
    [year, month],
  )
  const { data: subjects } = useApi(() => subjectApi.getSubjects(), [])
  const subjectMap = new Map((subjects ?? []).map((s) => [s.id, s] as const))
  const findSubject = (id: number) => subjectMap.get(id)

  function prevMonth() {
    if (month === 1) { setYear(y => y - 1); setMonth(12) }
    else setMonth(m => m - 1)
  }

  function nextMonth() {
    if (month === 12) { setYear(y => y + 1); setMonth(1) }
    else setMonth(m => m + 1)
  }

  async function handleAddEvent(e: React.FormEvent) {
    e.preventDefault()
    if (!formTitle.trim() || !formDate) return
    setSubmitting(true)
    try {
      const startAt = `${formDate}T09:00:00+09:00`
      let recurrence: RecurrenceRule | undefined
      if (formRepeat === 'weekly') {
        const days = formWeekdays.length > 0 ? [...formWeekdays].sort((a, b) => a - b) : [new Date(startAt).getUTCDay()]
        recurrence = { freq: 'weekly', interval: 1, byweekday: days, endMode: 'never' }
      } else if (formRepeat === 'monthly') {
        recurrence = { freq: 'monthly', interval: 1, endMode: 'never' }
      }
      await calendarApi.createEvent({
        title: formTitle.trim(),
        subjectId: formSubjectId ? Number(formSubjectId) : null,
        startAt,
        allDay: false,
        type: formType,
        reminder: formReminder || undefined,
        recurrence,
      })
      setShowAddForm(false)
      setFormTitle('')
      setFormType('other')
      setFormSubjectId('')
      setFormReminder('')
      setFormRepeat('none')
      setFormWeekdays([])
      refetch()
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeleteEvent(event: CalendarEvent) {
    const ok = window.confirm(`'${event.title}' 일정을 삭제할까요?`)
    if (!ok) return
    setDeletingId(event.id)
    try {
      await calendarApi.deleteEvent(event.id)
      refetch()
    } finally {
      setDeletingId(null)
    }
  }

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
  function addEventToDay(day: number, event: CalendarEvent) {
    if (!dayMap[day]) dayMap[day] = []
    dayMap[day].push(event)
  }

  for (const ev of events) {
    const d = new Date(ev.startAt)
    if (d.getUTCFullYear() === year && d.getUTCMonth() + 1 === month) {
      addEventToDay(d.getUTCDate(), ev)
    }
    if (ev.recurrence?.freq === 'weekly') {
      const start = new Date(ev.startAt)
      for (let day = 1; day <= daysInMonth; day += 1) {
        const candidate = new Date(Date.UTC(year, month - 1, day))
        if (candidate >= start && ev.recurrence.byweekday?.includes(candidate.getUTCDay())) {
          if (candidate.toISOString().slice(0, 10) === ev.startAt.slice(0, 10)) continue
          addEventToDay(day, ev)
        }
      }
    }
  }

  const upcoming = events.filter((e) => e.dDay >= 0).slice(0, 5)

  const todayYear = todayDate.getFullYear()
  const todayMonth = todayDate.getMonth() + 1
  const todayDay = todayDate.getDate()

  type Cell = { blank: true; key: string } | { blank: false; key: number; day: number }
  const cells: Cell[] = []
  for (let i = 0; i < firstWeekday; i += 1) cells.push({ blank: true, key: `b${i}` })
  for (let day = 1; day <= daysInMonth; day += 1) cells.push({ blank: false, key: day, day })

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">학습 일정</p>
        <h1 className="screen__heading">캘린더</h1>
        <p className="screen__lede">과제·시험을 한눈에 확인하고, 반복 일정도 함께 관리합니다.</p>
      </header>

      {showAddForm && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', zIndex: 100, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <form
            onSubmit={handleAddEvent}
            style={{ background: 'var(--color-surface)', borderRadius: 12, padding: 24, width: 360, display: 'flex', flexDirection: 'column', gap: 12 }}
          >
            <h3 style={{ margin: 0 }}>일정 추가</h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <label style={{ fontSize: 13, color: 'var(--color-text)' }}>제목 *</label>
              <input
                className="notes-editor__title"
                style={{ fontSize: 14 }}
                placeholder="일정 제목"
                value={formTitle}
                onChange={(e) => setFormTitle(e.target.value)}
                required
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <label style={{ fontSize: 13, color: 'var(--color-text)' }}>날짜 *</label>
              <input
                type="date"
                style={{ padding: '8px 12px', borderRadius: 8, border: '1px solid var(--color-border)', fontSize: 14 }}
                value={formDate}
                onChange={(e) => setFormDate(e.target.value)}
                required
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <label style={{ fontSize: 13, color: 'var(--color-text)' }}>유형</label>
              <select
                style={{ padding: '8px 12px', borderRadius: 8, border: '1px solid var(--color-border)', fontSize: 14 }}
                value={formType}
                onChange={(e) => setFormType(e.target.value)}
              >
                {EVENT_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>

            {subjects && subjects.length > 0 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <label style={{ fontSize: 13, color: 'var(--color-text)' }}>과목</label>
                <select
                  style={{ padding: '8px 12px', borderRadius: 8, border: '1px solid var(--color-border)', fontSize: 14 }}
                  value={formSubjectId}
                  onChange={(e) => setFormSubjectId(e.target.value)}
                >
                  <option value="">과목 없음</option>
                  {subjects.map((s) => (
                    <option key={s.id} value={s.id}>{s.name}</option>
                  ))}
                </select>
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <label style={{ fontSize: 13, color: 'var(--color-text)' }}>알림 시점</label>
              <select
                style={{ padding: '8px 12px', borderRadius: 8, border: '1px solid var(--color-border)', fontSize: 14 }}
                value={formReminder}
                onChange={(e) => setFormReminder(e.target.value)}
              >
                {REMINDER_OPTIONS.map((r) => (
                  <option key={r.value} value={r.value}>{r.label}</option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <label style={{ fontSize: 13, color: 'var(--color-text)' }}>반복</label>
              <select
                style={{ padding: '8px 12px', borderRadius: 8, border: '1px solid var(--color-border)', fontSize: 14 }}
                value={formRepeat}
                onChange={(e) => setFormRepeat(e.target.value as RepeatMode)}
              >
                <option value="none">반복 안 함</option>
                <option value="weekly">매주</option>
                <option value="monthly">매월</option>
              </select>
            </div>

            {formRepeat === 'weekly' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <label style={{ fontSize: 13, color: 'var(--color-text)' }}>반복 요일 (미선택 시 시작 요일)</label>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                  {WEEKDAYS.map((d) => {
                    const active = formWeekdays.includes(d.value)
                    return (
                      <button
                        key={d.value}
                        type="button"
                        onClick={() =>
                          setFormWeekdays((prev) =>
                            active ? prev.filter((v) => v !== d.value) : [...prev, d.value],
                          )
                        }
                        style={{
                          width: 34,
                          height: 34,
                          borderRadius: 8,
                          border: '1px solid var(--color-border)',
                          background: active ? 'var(--color-accent)' : 'transparent',
                          color: active ? '#fff' : 'var(--color-text)',
                          fontSize: 13,
                          cursor: 'pointer',
                        }}
                      >
                        {d.label}
                      </button>
                    )
                  })}
                </div>
              </div>
            )}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', marginTop: 4 }}>
              <button
                type="button"
                className="surface__title-action"
                onClick={() => setShowAddForm(false)}
              >
                취소
              </button>
              <button
                type="submit"
                className="surface__title-action"
                disabled={submitting}
                style={{ background: 'var(--color-accent)', color: '#fff' }}
              >
                {submitting ? '저장 중...' : '추가'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="calendar-grid">
        <section className="surface">
          <div className="surface__title">
            <h2>{year}년 {month}월</h2>
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="button" className="surface__title-action" onClick={() => setShowAddForm(true)}>
                <Icon name="plus" size={14} style={{ marginRight: 4 }} />
                일정 추가
              </button>
              <button type="button" className="surface__title-action" aria-label="이전 달" onClick={prevMonth}>
                ‹
              </button>
              <button type="button" className="surface__title-action" aria-label="다음 달" onClick={nextMonth}>
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
                <div
                  key={cell.key}
                  className={`cal-cell ${year === todayYear && month === todayMonth && cell.day === todayDay ? 'cal-cell--today' : ''}`}
                >
                  <span className="cal-cell__num">{cell.day}</span>
                  <div className="cal-cell__events">
                    {(dayMap[cell.day] || []).slice(0, 2).map((ev) => {
                      const subject = findSubject(ev.subjectId)
                      const tone = ev.type === 'exam' ? 'warning' : 'accent'
                      return (
                        <span
                          key={`${ev.id}-${cell.day}`}
                          className={`cal-cell__chip cal-cell__chip--${tone}`}
                          title={subject?.name ?? ev.title}
                        >
                          {subject?.name ?? ev.title}
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
          </div>
          {upcoming.length === 0 ? (
            <p style={{ padding: '16px 24px', color: 'var(--color-text)', fontSize: 14 }}>
              다가오는 일정이 없습니다.
            </p>
          ) : (
            <ul className="upcoming-list">
              {upcoming.map((event) => {
                const subject = findSubject(event.subjectId)
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
                        {dateLabel}{subject ? ` · ${subject.name}` : ''}
                        {event.recurrence ? (
                          <span className="tag tag--accent" style={{ marginLeft: 8 }}>
                            {event.recurrence.freq === 'monthly' ? '매월' : event.recurrence.freq === 'weekly' ? '매주' : '반복'}
                          </span>
                        ) : null}
                      </p>
                    </div>
                    <button
                      type="button"
                      className="row-icon upcoming-list__delete"
                      aria-label={`${event.title} 삭제`}
                      disabled={deletingId === event.id}
                      onClick={() => handleDeleteEvent(event)}
                    >
                      <Icon name="x" size={14} />
                    </button>
                  </li>
                )
              })}
            </ul>
          )}

          <div className="reminder-banner">
            <Icon name="bell" size={18} style={{ color: 'var(--color-accent-strong)' }} />
            <div>
              <p style={{ margin: 0, color: 'var(--color-ink)', fontWeight: 700, fontSize: 14 }}>
                웹 푸시 알림이 켜져 있어요
              </p>
              <p style={{ margin: '2px 0 0', color: 'var(--color-text)', fontSize: 13 }}>
                임박한 일정은 알림 설정 시점에 맞춰 알림으로 받습니다.
              </p>
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
