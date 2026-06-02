import type { Quiz, WrongAnswerNote } from '../types'

import { apiFetch } from './client'

export type WrongAnswerQuery = { type?: 'all' | 'mcq' | 'short' | 'ox' }

export type CreateQuestionRequest = {
  type: 'MCQ' | 'SHORT' | 'OX'
  text: string
  choices?: string[]
  correctIndex?: number
  correctText?: string
  correctBool?: boolean
  explanation: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  subjectId: number
  conceptTags?: string[]
}

export type CreateQuizRequest = {
  title: string
  subjectId: number
  questions: CreateQuestionRequest[]
}

export type SubmitAnswerRequest = {
  questionId: number
  userAnswer: string
  timeSpentSec: number
}

export const quizApi = {
  async listQuizzes(): Promise<Quiz[]> {
    return apiFetch<Quiz[]>('/api/quizzes')
  },

  async getQuiz(id: number): Promise<Quiz> {
    return apiFetch<Quiz>(`/api/quizzes/${id}`)
  },

  async createQuiz(request: CreateQuizRequest): Promise<Quiz> {
    return apiFetch<Quiz>('/api/quizzes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  },

  async submitAttempt(quizId: number, answers: SubmitAnswerRequest[]) {
    return apiFetch(`/api/quizzes/${quizId}/attempts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ answers }),
    })
  },

  async getWrongAnswers(query: WrongAnswerQuery = {}): Promise<WrongAnswerNote[]> {
    const type = query.type ?? 'all'
    return apiFetch<WrongAnswerNote[]>(`/api/wrong-answers?type=${type}`)
  },

  async retryWeakTypes(): Promise<Quiz> {
    return apiFetch<Quiz>('/api/wrong-answers/retry', { method: 'POST' })
  },
}

export const getQuiz = (id: number) => quizApi.getQuiz(id)
export const getWrongAnswers = (query?: WrongAnswerQuery) => quizApi.getWrongAnswers(query)
export const retryWeakTypes = () => quizApi.retryWeakTypes()
