import { useState } from 'react'

import { relativeKo } from '../api/calendarApi'
import { quizApi } from '../api/quizApi'
import { subjectApi } from '../api/subjectApi'
import { Icon } from '../components/Icon'
import type { WrongAnswerNote } from '../types'
import { useApi } from '../useApi'

type Filter = 'all' | 'mcq' | 'short' | 'ox'
type VisibleWrongAnswer = WrongAnswerNote & { question: NonNullable<WrongAnswerNote['question']> }
type SolveQuestion = VisibleWrongAnswer['question']
type Picked = string | number | boolean | null

function isAnswerCorrect(question: SolveQuestion, picked: Picked): boolean {
  if (picked == null || picked === '') return false
  if (question.type === 'mcq') return picked === question.correctIndex
  if (question.type === 'ox') return picked === question.correctBool
  return typeof picked === 'string'
    && question.correctText != null
    && picked.trim().toLowerCase() === question.correctText.trim().toLowerCase()
}

export function WrongAnswersScreen({ onOpenQuiz }: { onOpenQuiz?: (quizId: number) => void }) {
  const [filter, setFilter] = useState<Filter>('all')
  const { data: items, loading, refetch } = useApi(() => quizApi.getWrongAnswers({ type: filter }), [filter])
  const { data: subjects } = useApi(() => subjectApi.getSubjects(), [])
  const [openExplanations, setOpenExplanations] = useState<Record<number, boolean>>({})
  const [solvingId, setSolvingId] = useState<number | null>(null)
  const [retrying, setRetrying] = useState(false)
  const [message, setMessage] = useState('')

  async function handleResolved(questionId: number) {
    try {
      await quizApi.resolveWrongAnswer(questionId)
      setSolvingId(null)
      setMessage('정답입니다! 이 문제를 오답노트에서 제거했습니다.')
      refetch()
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '오답노트에서 제거하지 못했습니다.')
    }
  }

  async function retryWeakQuiz() {
    setRetrying(true)
    setMessage('')
    try {
      const quiz = await quizApi.retryWeakTypes()
      onOpenQuiz?.(quiz.id)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '재시험 퀴즈 생성에 실패했습니다.')
    } finally {
      setRetrying(false)
    }
  }

  if (loading || !items) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">오답노트</p>
          <h1 className="screen__heading">반복해서 틀린 문제</h1>
        </header>
        <div className="surface" style={{ height: 400, opacity: 0.4 }} />
      </div>
    )
  }

  const summary = {
    total: items.length,
    repeated: items.filter((i) => i.missCount >= 2).length,
    topConcept: items.slice().sort((a, b) => b.missCount - a.missCount)[0]?.concept ?? '없음',
  }
  const visibleItems = items.filter((item): item is VisibleWrongAnswer => item.question != null)

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">오답노트 · 취약 유형</p>
        <h1 className="screen__heading">반복해서 틀린 문제</h1>
        <p className="screen__lede">
          최근 30일 동안 틀린 문제가 자동으로 모입니다. 2회 이상 틀린 문제는 취약 유형으로 표시되어, 한 번에 모아 재시험을
          시작할 수 있습니다.
        </p>
      </header>

      <div className="wa-summary">
        <div className="surface wa-stat">
          <p className="label">총 오답</p>
          <p className="wa-stat__big">
            {summary.total}
            <span>건</span>
          </p>
        </div>
        <div className="surface wa-stat">
          <p className="label">2회+ 반복</p>
          <p className="wa-stat__big">
            {summary.repeated}
            <span>건</span>
          </p>
        </div>
        <div className="surface wa-stat">
          <p className="label">취약 1순위 개념</p>
          <p className="wa-stat__big wa-stat__big--text">{summary.topConcept}</p>
        </div>
        <button
          type="button"
          className="surface wa-cta"
          onClick={() => void retryWeakQuiz()}
          disabled={retrying || summary.repeated === 0}
        >
          <Icon name="refresh" size={18} />
          <span>
            <strong>취약 유형 재시험</strong>
            <em>{retrying ? 'AI가 새 문제 생성 중...' : `반복 오답 ${summary.repeated}개 기반 AI 새 문제 출제`}</em>
          </span>
        </button>
      </div>
      {message ? <p className="muted-note" style={{ marginTop: -8 }}>{message}</p> : null}

      <section className="surface">
        <div className="surface__title">
          <h2>오답 목록</h2>
          <div className="wa-filter" role="tablist">
            {(
              [
                ['all', '전체'],
                ['mcq', '객관식'],
                ['short', '단답형'],
                ['ox', 'OX'],
              ] as const
            ).map(([id, label]) => (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={filter === id}
                onClick={() => setFilter(id)}
              >
                {label}
              </button>
            ))}
          </div>
        </div>

        {visibleItems.length === 0 ? (
          <p className="muted-note" style={{ textAlign: 'center', padding: 32 }}>
            해당 유형의 오답이 없습니다.
          </p>
        ) : (
          <ul className="wa-list">
            {visibleItems.map((w) => {
              const question = w.question
              const subject = subjects?.find((item) => item.id === question.subjectId)
              const subjectName = subject?.name ?? (question.subjectId == null ? '과목 없음' : `과목 ${question.subjectId}`)
              const typeLabel = question.type === 'mcq' ? '객관식' : question.type === 'short' ? '단답형' : 'OX'
              return (
                <li key={w.questionId} className={w.missCount >= 2 ? 'is-repeat' : ''}>
                  <div className="wa-list__meta">
                    <span className="tag">{subjectName}</span>
                    <span className={`tag tag--${question.type === 'ox' ? 'warning' : 'accent'}`}>{typeLabel}</span>
                    {w.missCount >= 2 ? <span className="tag tag--warning">{w.missCount}회 오답</span> : null}
                  </div>
                  <p className="wa-list__q">{question.text}</p>
                  <p className="wa-list__sub">
                    관련 개념: <strong>{w.concept}</strong> · 최근 시도: {relativeKo(w.lastMissedAt)}
                  </p>
                  <div className="wa-list__actions">
                    <button
                      type="button"
                      className="surface__title-action"
                      onClick={() => setSolvingId((prev) => (prev === w.questionId ? null : w.questionId))}
                    >
                      {solvingId === w.questionId ? '풀이 닫기' : '다시 풀기'}
                    </button>
                    <button
                      type="button"
                      className="surface__title-action"
                      onClick={() => setOpenExplanations((prev) => ({ ...prev, [w.questionId]: !prev[w.questionId] }))}
                    >
                      {openExplanations[w.questionId] ? '해설 닫기' : '해설 보기'}
                    </button>
                  </div>
                  {solvingId === w.questionId ? <InlineSolve question={question} onResolved={() => void handleResolved(w.questionId)} /> : null}
                  {openExplanations[w.questionId] ? (
                    <div className="wa-list__explanation">
                      <p className="label">해설</p>
                      <p>{question.explanation}</p>
                    </div>
                  ) : null}
                </li>
              )
            })}
          </ul>
        )}
      </section>
    </div>
  )
}

function InlineSolve({ question, onResolved }: { question: SolveQuestion; onResolved?: () => void }) {
  const [picked, setPicked] = useState<Picked>(null)
  const [revealed, setRevealed] = useState(false)
  const answered = picked != null && picked !== ''
  const correct = isAnswerCorrect(question, picked)

  return (
    <div className="wa-solve" style={{ marginTop: 12, display: 'grid', gap: 12 }}>
      {question.type === 'mcq' ? (
        <ul className="choices">
          {(question.choices ?? []).map((choice, i) => {
            const state = revealed
              ? i === question.correctIndex ? 'correct' : picked === i ? 'wrong' : 'idle'
              : picked === i ? 'picked' : 'idle'
            return (
              <li key={`${i}-${choice}`}>
                <button type="button" className={`choice choice--${state}`} disabled={revealed} onClick={() => setPicked(i)}>
                  <span className="choice__index">{i + 1}</span>
                  <span className="choice__body">{choice}</span>
                </button>
              </li>
            )
          })}
        </ul>
      ) : question.type === 'ox' ? (
        <div className="ox-row">
          {[true, false].map((value) => {
            const state = revealed
              ? value === question.correctBool ? 'correct' : picked === value ? 'wrong' : 'idle'
              : picked === value ? 'picked' : 'idle'
            return (
              <button key={String(value)} type="button" className={`ox ox--${state}`} disabled={revealed} onClick={() => setPicked(value)}>
                {value ? 'O' : 'X'}
              </button>
            )
          })}
        </div>
      ) : (
        <input
          className="short-input"
          value={(picked as string) ?? ''}
          disabled={revealed}
          onChange={(event) => setPicked(event.target.value)}
          placeholder="정답 입력"
        />
      )}

      {!revealed ? (
        <button
          type="button"
          className="surface__title-action"
          style={{ justifySelf: 'start' }}
          disabled={!answered}
          onClick={() => {
            setRevealed(true)
            if (isAnswerCorrect(question, picked)) onResolved?.()
          }}
        >
          정답 확인
        </button>
      ) : (
        <div style={{ display: 'grid', gap: 6 }}>
          <p style={{ margin: 0, fontWeight: 600, color: correct ? 'var(--color-accent)' : 'var(--color-warning)' }}>
            {correct ? '정답입니다!' : '오답입니다.'}
            {question.type === 'short' && question.correctText ? ` 모범답안: ${question.correctText}` : ''}
          </p>
          <p className="muted-note" style={{ margin: 0 }}>{question.explanation}</p>
          <button
            type="button"
            className="surface__title-action"
            style={{ justifySelf: 'start' }}
            onClick={() => {
              setPicked(null)
              setRevealed(false)
            }}
          >
            다시 시도
          </button>
        </div>
      )}
    </div>
  )
}
