import type { ReactNode } from 'react'
import { useState } from 'react'

import { aiApi } from '../api/aiApi'
import { noteApi } from '../api/noteApi'
import { quizApi } from '../api/quizApi'
import { subjectById } from '../api/subjectApi'
import { Icon } from '../components/Icon'
import type { Note, PriorityRecommendation, UpcomingSubject, WeakConcept } from '../types'
import { useApi } from '../useApi'

type RecommendScreenProps = {
  onOpenNote?: (noteId: number) => void
  onOpenQuiz?: (quizId: number) => void
}

export function RecommendScreen({ onOpenNote, onOpenQuiz }: RecommendScreenProps) {
  const { data, loading, refetch } = useApi(() => aiApi.getRecommend(), [])
  const [weakGenerating, setWeakGenerating] = useState(false)
  const [weakMessage, setWeakMessage] = useState('')

  async function generateWeakQuiz() {
    setWeakGenerating(true)
    setWeakMessage('')
    try {
      const quiz = await quizApi.retryWeakTypes()
      onOpenQuiz?.(quiz.id)
    } catch (error) {
      setWeakMessage(error instanceof Error ? error.message : '취약 개념 퀴즈 생성에 실패했습니다.')
    } finally {
      setWeakGenerating(false)
    }
  }

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

      <NoteSummaryCard onOpenNote={onOpenNote} onOpenQuiz={onOpenQuiz} />

      <PriorityRail items={data.priorities} />

      <div className="recommend-cols">
        <WeakConceptCard
          concepts={data.weakConcepts}
          generating={weakGenerating}
          message={weakMessage}
          onRefresh={refetch}
          onGenerateQuiz={generateWeakQuiz}
        />
        <UpcomingSubjectCard subjects={data.upcomingSubjects} />
      </div>
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
          const subject = subjectById(item.subjectId)
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
                <span className="priority-row__rate">{item.accuracy == null ? '–' : `${item.accuracy}%`}</span>
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

function WeakConceptCard({
  concepts,
  generating,
  message,
  onRefresh,
  onGenerateQuiz,
}: {
  concepts: WeakConcept[]
  generating: boolean
  message: string
  onRefresh: () => void
  onGenerateQuiz: () => void
}) {
  return (
    <section className="surface">
      <div className="surface__title">
        <h2>
          <Icon name="sparkle" size={18} style={{ marginRight: 6 }} />
          취약 개념 분석
        </h2>
        <button type="button" className="surface__title-action" onClick={onRefresh}>
          새로고침
        </button>
      </div>
      <p className="muted-note">
        최근 30일 오답에서 추출한 개념 리스트입니다. AI 응답이 없을 때는 오답 빈도만 표시됩니다.
      </p>
      <ul className="weak-list">
        {concepts.map((concept, index) => {
          const subject = subjectById(concept.subjectId ?? -1)
          return (
            <li key={`${concept.subjectId ?? 'unknown'}-${concept.concept}-${index}`}>
              <div>
                <p className="weak-list__concept">{concept.concept}</p>
                <p className="weak-list__meta">
                  {subject?.name ?? '과목 미분류'} · 관련: {concept.relatedKeywords.join(', ')}
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
      {message ? <p className="muted-note" style={{ marginTop: 12 }}>{message}</p> : null}
      <button
        type="button"
        className="surface__title-action"
        style={{ marginTop: 16, width: '100%' }}
        onClick={onGenerateQuiz}
        disabled={generating || concepts.length === 0}
      >
        <Icon name="refresh" size={14} style={{ marginRight: 6 }} />
        {generating ? '퀴즈 생성 중...' : '취약 개념 위주 퀴즈 생성'}
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
        {subjects.map((subject) => {
          const meta = subjectById(subject.subjectId)
          return (
            <li key={subject.subjectId}>
              <div>
                <p className="upcoming-subjects__name">{meta?.name}</p>
                <p className="upcoming-subjects__meta">
                  D-{subject.dDay} · 정답률 {subject.accuracy == null ? '–' : `${subject.accuracy}%`}
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

function NoteSummaryCard({ onOpenNote, onOpenQuiz }: { onOpenNote?: (noteId: number) => void; onOpenQuiz?: (quizId: number) => void }) {
  const { data: notes, loading: notesLoading } = useApi(() => noteApi.getNotes(), [])
  const [selectedNoteId, setSelectedNoteId] = useState<number | null>(null)
  const [summary, setSummary] = useState<Awaited<ReturnType<typeof aiApi.summarizeNote>> | null>(null)
  const [loading, setLoading] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [message, setMessage] = useState('')

  const effectiveNoteId = selectedNoteId ?? notes?.[0]?.id ?? null
  const selectedNote = notes?.find((note) => note.id === effectiveNoteId) ?? null

  async function summarize() {
    if (effectiveNoteId === null) return
    setLoading(true)
    setMessage('')
    try {
      setSummary(await aiApi.summarizeNote(effectiveNoteId))
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '요약에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  async function generateQuiz() {
    if (effectiveNoteId === null) return
    setGenerating(true)
    setMessage('')
    try {
      const quiz = await aiApi.generateQuiz(effectiveNoteId)
      setMessage(`AI 퀴즈가 생성되었습니다: ${quiz.title}`)
      onOpenQuiz?.(quiz.id)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '퀴즈 생성에 실패했습니다.')
    } finally {
      setGenerating(false)
    }
  }

  return (
    <section className="surface">
      <div className="surface__title">
        <h2>
          <Icon name="book" size={18} style={{ marginRight: 6 }} />
          학습 내용 요약
        </h2>
        <button type="button" className="surface__title-action" onClick={summarize} disabled={loading || effectiveNoteId === null}>
          <Icon name="refresh" size={14} style={{ marginRight: 6 }} />
          {loading ? '요약 중...' : '요약하기'}
        </button>
      </div>
      <div className="summary-block">
        {notesLoading ? (
          <p className="muted-note">노트 목록을 불러오는 중입니다.</p>
        ) : !notes?.length ? (
          <p className="muted-note">요약할 노트가 없습니다. 먼저 노트를 작성하세요.</p>
        ) : (
          <>
            <NotePicker notes={notes} selectedNoteId={effectiveNoteId} onChange={setSelectedNoteId} />
            {message ? <p className="muted-note" style={{ marginTop: 12 }}>{message}</p> : null}
            {loading ? <p className="muted-note">Gemini API로 노트를 요약하는 중입니다.</p> : null}
            {summary ? (
              <>
                <p className="muted-note">
                  {selectedNote?.title ?? '선택한 노트'} ({summary.sourceCharCount.toLocaleString()}자 →{' '}
                  {summary.summaryCharCount}자 요약)
                  {summary.fallback ? ' · fallback 응답' : ''}
                </p>
                <h3 style={{ fontSize: 18, marginTop: 12 }}>핵심 {summary.bullets.length}줄</h3>
                <ul className="summary-list">
                  {summary.bullets.map((bullet) => (
                    <li key={bullet}>{renderInlineCode(bullet)}</li>
                  ))}
                </ul>
              </>
            ) : null}
            <div className="summary-actions">
              <button type="button" className="surface__title-action" onClick={generateQuiz} disabled={generating || effectiveNoteId === null}>
                <Icon name="brain" size={14} style={{ marginRight: 6 }} />
                {generating ? '퀴즈 생성 중...' : '이 노트로 퀴즈 생성'}
              </button>
              <button
                type="button"
                className="surface__title-action"
                onClick={() => {
                  if (effectiveNoteId !== null) onOpenNote?.(effectiveNoteId)
                }}
                disabled={effectiveNoteId === null || !onOpenNote}
              >
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

function NotePicker({ notes, selectedNoteId, onChange }: { notes: Note[]; selectedNoteId: number | null; onChange: (id: number) => void }) {
  return (
    <label className="label" style={{ display: 'grid', gap: 8, marginBottom: 12 }}>
      요약할 노트
      <select value={selectedNoteId ?? ''} onChange={(event) => onChange(Number(event.target.value))}>
        {notes.map((note) => (
          <option key={note.id} value={note.id}>{note.title}</option>
        ))}
      </select>
    </label>
  )
}

function renderInlineCode(text: string): ReactNode[] {
  return text.split(/(`[^`]+`)/g).map((part) => {
    if (part.startsWith('`') && part.endsWith('`')) {
      return <code key={part}>{part.slice(1, -1)}</code>
    }

    return part
  })
}
