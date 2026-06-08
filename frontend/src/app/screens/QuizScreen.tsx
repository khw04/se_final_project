import { useEffect, useState } from 'react'

import { aiApi } from '../api/aiApi'
import { noteApi } from '../api/noteApi'
import { quizApi } from '../api/quizApi'
import { subjectById } from '../api/subjectApi'
import { Icon } from '../components/Icon'
import type { Answer, Question, QuestionType } from '../types'
import { useApi } from '../useApi'

type Picked = string | number | boolean | null

export function QuizScreen({ quizId }: { quizId?: number }) {
  const { data: quizList, loading: listLoading, refetch: refetchQuizList } = useApi(() => quizApi.listQuizzes(), [])
  const [activeQuizId, setActiveQuizId] = useState<number | null>(quizId ?? null)
  const [generating, setGenerating] = useState(false)
  const [message, setMessage] = useState('')
  const firstQuizId = quizList?.[0]?.id ?? null
  const effectiveQuizId = activeQuizId ?? firstQuizId

  const { data: quiz, loading: quizLoading } = useApi(
    () => (effectiveQuizId != null ? quizApi.getQuiz(effectiveQuizId) : Promise.resolve(null)),
    [effectiveQuizId],
  )

  const [type, setType] = useState<QuestionType>('mcq')
  const [step, setStep] = useState<number>(0)
  const [picked, setPicked] = useState<Picked>(null)
  const [revealed, setRevealed] = useState<boolean>(false)
  const [answers, setAnswers] = useState<Answer[]>([])
  const [submitLoading, setSubmitLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  useEffect(() => {
    if (quizId != null) {
      queueMicrotask(() => setActiveQuizId(quizId))
    }
  }, [quizId])

  useEffect(() => {
    queueMicrotask(() => {
      setType('mcq')
      setStep(0)
      setPicked(null)
      setRevealed(false)
      setAnswers([])
      setSubmitted(false)
    })
  }, [effectiveQuizId])

  useEffect(() => {
    const firstType = quiz?.questions?.[0]?.type
    if (firstType) {
      queueMicrotask(() => setType(firstType))
    }
  }, [quiz?.id, quiz?.questions])

  async function generateNewQuiz() {
    setGenerating(true)
    setMessage('')
    try {
      const notes = await noteApi.getNotes()
      const noteId = quiz?.generatedFromNoteId ?? notes[0]?.id
      if (noteId == null) {
        setMessage('퀴즈를 만들 노트가 없습니다. 먼저 노트를 작성해주세요.')
        return
      }
      const nextQuiz = await aiApi.generateQuiz(noteId)
      setActiveQuizId(nextQuiz.id)
      refetchQuizList()
      setMessage(`새 퀴즈를 생성했습니다: ${nextQuiz.title}`)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '새 퀴즈 생성에 실패했습니다.')
    } finally {
      setGenerating(false)
    }
  }

  const loading = listLoading || (effectiveQuizId != null && quizLoading)

  if (loading) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">퀴즈</p>
          <h1 className="screen__heading">로딩 중…</h1>
        </header>
        <div className="surface" style={{ height: 400, opacity: 0.4 }} />
      </div>
    )
  }

  if (!quiz || !quiz.questions || quiz.questions.length === 0) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">퀴즈</p>
          <h1 className="screen__heading">퀴즈 없음</h1>
          <p className="screen__lede">아직 생성된 퀴즈가 없습니다. AI 퀴즈 생성 기능으로 노트 기반 퀴즈를 만들어보세요.</p>
          <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
            <button type="button" className="surface__title-action" onClick={() => void generateNewQuiz()} disabled={generating}>
              <Icon name="sparkle" size={14} style={{ marginRight: 6 }} />
              {generating ? '퀴즈 생성 중...' : '새 퀴즈 생성'}
            </button>
          </div>
          {message ? <p className="muted-note" style={{ marginTop: 12 }}>{message}</p> : null}
        </header>
      </div>
    )
  }

  const filtered = quiz.questions.filter((q) => q.type === type)
  const total = quiz.questions.length
  const current = filtered[step % Math.max(filtered.length, 1)] ?? null
  const subject = subjectById(quiz.subjectId)
  const allAnswered = answers.length >= total

  function isCorrect(q: Question, value: Picked): boolean {
    if (q.type === 'mcq') return value === q.correctIndex
    if (q.type === 'ox') return value === q.correctBool
    if (q.type === 'short') return String(value ?? '').trim() === (q.correctText ?? '')
    return false
  }

  function submit() {
    if (picked == null || !current) return
    setAnswers((prev) => [
      ...prev,
      {
        questionId: current.id,
        userAnswer: picked as string | number | boolean,
        correct: isCorrect(current, picked),
        timeSpentSec: 0,
      },
    ])
    setRevealed(true)
  }

  function next() {
    setRevealed(false)
    setPicked(null)
    setStep((s) => s + 1)
  }

  function changeType(t: QuestionType) {
    setType(t)
    setStep(0)
    setPicked(null)
    setRevealed(false)
  }

  async function handleSubmit() {
    if (!quiz?.id) return
    setSubmitLoading(true)
    try {
      await quizApi.submitAttempt(
        quiz.id,
        answers.map((a) => ({
          questionId: a.questionId,
          userAnswer: String(a.userAnswer),
          timeSpentSec: a.timeSpentSec,
        })),
      )
      window.dispatchEvent(new Event('pokemo:wrong-answers-updated'))
      setSubmitted(true)
    } finally {
      setSubmitLoading(false)
    }
  }

  const stepDisplay = answers.length + 1
  const score = answers.filter((a) => a.correct).length
  const wrongCount = answers.filter((a) => !a.correct).length

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">퀴즈</p>
        <h1 className="screen__heading">{quiz.title}</h1>
        <p className="screen__lede">
          {subject?.name} —{' '}
          {quiz.generatedBy === 'ai-gpt' || quiz.generatedBy === 'ai-gemini'
            ? 'AI가 노트 내용을 바탕으로 자동 생성한 '
            : '직접 출제한 '}
          문제입니다. 풀이를 마치면 오답이 자동으로 오답노트로 이동합니다.
        </p>
        <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
          <button type="button" className="surface__title-action" onClick={() => void generateNewQuiz()} disabled={generating}>
            <Icon name="sparkle" size={14} style={{ marginRight: 6 }} />
            {generating ? '퀴즈 생성 중...' : '새 퀴즈 생성'}
          </button>
        </div>
        {message ? <p className="muted-note" style={{ marginTop: 12 }}>{message}</p> : null}
      </header>

      <div className="quiz-layout">
        <section className="surface">
          <div className="quiz-tabs" role="tablist">
            <button type="button" role="tab" aria-selected={type === 'mcq'} onClick={() => changeType('mcq')}>
              객관식
            </button>
            <button type="button" role="tab" aria-selected={type === 'ox'} onClick={() => changeType('ox')}>
              OX
            </button>
            <button type="button" role="tab" aria-selected={type === 'short'} onClick={() => changeType('short')}>
              단답형
            </button>
          </div>

          <div className="quiz-progress">
            <span className="quiz-progress__label">
              문제 {stepDisplay} / {total}
            </span>
            <div className="quiz-progress__bar">
              <div style={{ width: `${(stepDisplay / total) * 100}%` }} />
            </div>
          </div>

          {current ? (
            <QuestionView
              question={current}
              picked={picked}
              revealed={revealed}
              onPick={(value) => {
                if (!revealed) setPicked(value)
              }}
            />
          ) : (
            <p className="muted-note" style={{ padding: 32, textAlign: 'center' }}>
              이 유형의 문제가 없습니다.
            </p>
          )}

          <footer className="quiz-footer">
            {allAnswered ? (
              submitted ? (
                <p className="muted-note" style={{ textAlign: 'center' }}>✓ 제출 완료! 오답노트에서 결과를 확인하세요.</p>
              ) : (
                <button
                  type="button"
                  className="auth-card__submit quiz-submit"
                  onClick={() => void handleSubmit()}
                  disabled={submitLoading}
                >
                  {submitLoading ? '제출 중…' : '완료하기'}
                </button>
              )
            ) : revealed ? (
              <button type="button" className="auth-card__submit quiz-next" onClick={next}>
                다음 문제
                <Icon name="arrowRight" size={16} style={{ marginLeft: 8 }} />
              </button>
            ) : (
              <button
                type="button"
                className="auth-card__submit quiz-submit"
                onClick={submit}
                disabled={picked == null || !current}
              >
                정답 확인
              </button>
            )}
          </footer>
        </section>

        <aside className="quiz-side">
          <section className="surface">
            <p className="label">이번 회차 점수</p>
            <p className="trend-stats__big" style={{ fontSize: 40, marginTop: 4 }}>
              {answers.length > 0 ? Math.round((score / answers.length) * 100) : 0}
              <span style={{ fontSize: 20, color: 'var(--color-muted)' }}> / 100</span>
            </p>
            <div style={{ display: 'grid', gap: 8, marginTop: 16 }}>
              <div className="quiz-stat">
                <span>정답</span>
                <strong>{score}</strong>
              </div>
              <div className="quiz-stat">
                <span>오답</span>
                <strong style={{ color: 'var(--color-warning)' }}>{wrongCount}</strong>
              </div>
              <div className="quiz-stat">
                <span>건너뜀</span>
                <strong>0</strong>
              </div>
            </div>
          </section>

          <section className="surface">
            <div className="surface__title">
              <h2 style={{ fontSize: 18 }}>오답노트</h2>
              <span className="tag tag--warning">{wrongCount}</span>
            </div>
            {wrongCount === 0 ? (
              <p className="muted-note">아직 오답이 없습니다. 정답 확인을 누르면 자동으로 분류됩니다.</p>
            ) : (
              <ul className="wrong-list">
                {answers
                  .filter((a) => !a.correct)
                  .map((answer) => (
                    <li key={`${answer.questionId}-${String(answer.userAnswer)}`}>
                      <Icon name="x" size={14} style={{ color: 'var(--color-warning)' }} />
                      <div>
                        <p className="wrong-list__title">문제 {answers.indexOf(answer) + 1}</p>
                        <p className="wrong-list__detail">방금 오답 추가됨</p>
                      </div>
                    </li>
                  ))}
              </ul>
            )}
          </section>
        </aside>
      </div>
    </div>
  )
}

type QuestionProps = {
  question: Question
  picked: Picked
  revealed: boolean
  onPick: (value: Picked) => void
}

function QuestionView({ question, picked, revealed, onPick }: QuestionProps) {
  if (question.type === 'mcq') {
    return (
      <div className="question">
        <p className="question__text">{question.text}</p>
        <ul className="choices">
          {(question.choices ?? []).map((choice, i) => {
            const isPicked = picked === i
            const isCorrect = i === question.correctIndex
            const state = revealed
              ? isCorrect
                ? 'correct'
                : isPicked
                  ? 'wrong'
                  : 'idle'
              : isPicked
                ? 'picked'
                : 'idle'
            return (
              <li key={`${question.id}-${choice}`}>
                <button type="button" className={`choice choice--${state}`} onClick={() => onPick(i)}>
                  <span className="choice__index">{i + 1}</span>
                  <span className="choice__body">{choice}</span>
                  {revealed && isCorrect ? <Icon name="check" size={16} className="choice__icon" /> : null}
                  {revealed && isPicked && !isCorrect ? <Icon name="x" size={16} className="choice__icon" /> : null}
                </button>
              </li>
            )
          })}
        </ul>
        {revealed ? <Explanation text={question.explanation} /> : null}
      </div>
    )
  }

  if (question.type === 'ox') {
    return (
      <div className="question">
        <p className="question__text">{question.text}</p>
        <div className="ox-row">
          {[true, false].map((value) => {
            const isPicked = picked === value
            const isCorrect = value === question.correctBool
            const state = revealed
              ? isCorrect
                ? 'correct'
                : isPicked
                  ? 'wrong'
                  : 'idle'
              : isPicked
                ? 'picked'
                : 'idle'
            return (
              <button key={String(value)} type="button" className={`ox ox--${state}`} onClick={() => onPick(value)}>
                {value ? 'O' : 'X'}
              </button>
            )
          })}
        </div>
        {revealed ? <Explanation text={question.explanation} /> : null}
      </div>
    )
  }

  return (
    <div className="question">
      <p className="question__text">{question.text}</p>
      <label style={{ display: 'grid', gap: 8, marginTop: 16 }}>
        <span className="label">정답 입력</span>
        <input
          className="short-input"
          value={(picked as string) ?? ''}
          onChange={(event) => onPick(event.target.value)}
          placeholder="예: 1.0"
        />
      </label>
      {revealed ? (
        <div className="explain">
          <p className="label">해설</p>
          <p>
            모범 답안: <strong>{question.correctText}</strong>. {question.explanation}
          </p>
        </div>
      ) : null}
    </div>
  )
}

function Explanation({ text }: { text: string }) {
  return (
    <div className="explain">
      <p className="label">해설</p>
      <p>{text}</p>
    </div>
  )
}
