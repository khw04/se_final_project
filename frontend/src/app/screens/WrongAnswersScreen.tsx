import { useState } from 'react'

import { relativeKo } from '../api/calendarApi'
import { quizApi } from '../api/quizApi'
import { subjectById } from '../api/subjectApi'
import { Icon } from '../components/Icon'
import type { WrongAnswerNote } from '../types'
import { useApi } from '../useApi'

type Filter = 'all' | 'mcq' | 'short' | 'ox'
type VisibleWrongAnswer = WrongAnswerNote & { question: NonNullable<WrongAnswerNote['question']> }

export function WrongAnswersScreen({ onOpenQuiz }: { onOpenQuiz?: (quizId: number) => void }) {
  const [filter, setFilter] = useState<Filter>('all')
  const { data: items, loading } = useApi(() => quizApi.getWrongAnswers({ type: filter }), [filter])
  const [openExplanations, setOpenExplanations] = useState<Record<number, boolean>>({})
  const [retrying, setRetrying] = useState(false)
  const [message, setMessage] = useState('')

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
            <em>{retrying ? '재시험 생성 중...' : `반복 오답 ${summary.repeated}문제로 자동 출제`}</em>
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
              const subject = subjectById(question.subjectId)
              const typeLabel = question.type === 'mcq' ? '객관식' : question.type === 'short' ? '단답형' : 'OX'
              return (
                <li key={w.questionId} className={w.missCount >= 2 ? 'is-repeat' : ''}>
                  <div className="wa-list__meta">
                    <span className="tag">{subject?.name}</span>
                    <span className={`tag tag--${question.type === 'ox' ? 'warning' : 'accent'}`}>{typeLabel}</span>
                    {w.missCount >= 2 ? <span className="tag tag--warning">{w.missCount}회 오답</span> : null}
                  </div>
                  <p className="wa-list__q">{question.text}</p>
                  <p className="wa-list__sub">
                    관련 개념: <strong>{w.concept}</strong> · 최근 시도: {relativeKo(w.lastMissedAt)}
                  </p>
                  <div className="wa-list__actions">
                    <button type="button" className="surface__title-action" onClick={() => void retryWeakQuiz()} disabled={retrying}>
                      다시 풀기
                    </button>
                    <button
                      type="button"
                      className="surface__title-action"
                      onClick={() => setOpenExplanations((prev) => ({ ...prev, [w.questionId]: !prev[w.questionId] }))}
                    >
                      {openExplanations[w.questionId] ? '해설 닫기' : '해설 보기'}
                    </button>
                  </div>
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
