export type QuestionType = 'mcq' | 'short' | 'ox'

export type Question = {
  id: number
  type: QuestionType
  text: string
  choices?: string[]
  correctIndex?: number
  correctText?: string
  correctBool?: boolean
  explanation?: string
  difficulty: 'easy' | 'medium' | 'hard'
  subjectId: number
  conceptTags: string[]
}

export type Quiz = {
  id: number
  title: string
  subjectId: number
  questionIds: number[]
  questions?: Question[]
  generatedFromNoteId?: number
  generatedBy?: 'manual' | 'ai-gpt' | 'ai-gemini'
  createdAt: string
}

export type Answer = {
  questionId: number
  userAnswer: string | number | boolean
  correct: boolean
  timeSpentSec: number
}

export type WrongAnswerNote = {
  questionId: number
  question: Question | null
  missCount: number
  lastAttemptId: number
  quizId?: number
  lastMissedAt: string
  concept: string
}
