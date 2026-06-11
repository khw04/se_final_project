import type { AccuracyTrendPoint, QuestionTypeAccuracy, SubjectProgress, WeeklyStudyPoint } from '../types'

import { apiFetch } from './client'

export const statsApi = {
  async getAccuracyTrend(): Promise<AccuracyTrendPoint[]> {
    return apiFetch<AccuracyTrendPoint[]>('/stats/trend')
  },

  async getSubjectProgress(): Promise<SubjectProgress[]> {
    return apiFetch<SubjectProgress[]>('/stats/progress')
  },

  async getWeeklyStudy(): Promise<WeeklyStudyPoint[]> {
    return apiFetch<WeeklyStudyPoint[]>('/stats/weekly')
  },

  async getTypeAccuracy(): Promise<QuestionTypeAccuracy[]> {
    return apiFetch<QuestionTypeAccuracy[]>('/stats/type-accuracy')
  },
}

export const getAccuracyTrend = () => statsApi.getAccuracyTrend()
export const getSubjectProgress = () => statsApi.getSubjectProgress()
export const getWeeklyStudy = () => statsApi.getWeeklyStudy()
export const getTypeAccuracy = () => statsApi.getTypeAccuracy()
