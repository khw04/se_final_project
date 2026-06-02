import type { Question, Quiz, WrongAnswerNote } from '../types'

import { delay, iso } from './dateUtils'
import { MOCK_QUESTIONS, MOCK_QUIZZES, MOCK_WRONG } from './mockData'

export type WrongAnswerQuery = { type?: 'all' | 'mcq' | 'short' | 'ox' }

export const quizApi = {
  async getQuiz(id: number): Promise<Quiz> {
    await delay(70)
    const quiz = MOCK_QUIZZES.find((q) => q.id === id)
    if (!quiz) throw new Error('QUIZ_NOT_FOUND')
    const questions = quiz.questionIds
      .map((qid) => MOCK_QUESTIONS.find((qq) => qq.id === qid))
      .filter((q): q is Question => Boolean(q))
    return { ...quiz, questions }
  },

  async getWrongAnswers(query: WrongAnswerQuery = {}): Promise<WrongAnswerNote[]> {
    await delay(80)
    let out = MOCK_WRONG
    if (query.type && query.type !== 'all') out = out.filter((w) => w.question.type === query.type)
    return out
  },

  async retryWeakTypes(): Promise<Quiz> {
    await delay(140)
    const repeated = MOCK_WRONG.filter((w) => w.missCount >= 2)
    return {
      id: Date.now(),
      title: '취약 유형 재시험',
      subjectId: 1,
      questionIds: repeated.map((w) => w.questionId),
      generatedBy: 'ai-gpt',
      createdAt: iso(Date.now()),
    }
  },
}

export const getQuiz = quizApi.getQuiz
export const getWrongAnswers = quizApi.getWrongAnswers
export const retryWeakTypes = quizApi.retryWeakTypes
