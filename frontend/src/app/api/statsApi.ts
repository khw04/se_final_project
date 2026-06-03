import type { AccuracyTrendPoint, SubjectProgress, WeeklyStudyPoint } from '../types'

import { apiFetch } from './client'

export const statsApi = {
  async getAccuracyTrend(): Promise<AccuracyTrendPoint[]> {
    return apiFetch<AccuracyTrendPoint[]>('/api/stats/trend')
  },

  async getSubjectProgress(): Promise<SubjectProgress[]> {
    return apiFetch<SubjectProgress[]>('/api/stats/progress')
  },

  async getWeeklyStudy(): Promise<WeeklyStudyPoint[]> {
    return apiFetch<WeeklyStudyPoint[]>('/api/stats/weekly')
  },
}

export const getAccuracyTrend = () => statsApi.getAccuracyTrend()
export const getSubjectProgress = () => statsApi.getSubjectProgress()
export const getWeeklyStudy = () => statsApi.getWeeklyStudy()
