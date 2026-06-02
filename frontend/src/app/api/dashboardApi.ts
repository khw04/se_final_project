import type { DashboardPayload } from '../types'

import { calcDDay, delay } from './dateUtils'
import {
  MOCK_ACCURACY_TREND,
  MOCK_EVENTS,
  MOCK_PRIORITY,
  MOCK_SUBJECT_PROGRESS,
  MOCK_USERS,
  MOCK_WEEKLY_STUDY,
} from './mockData'

export const dashboardApi = {
  async getDashboard(): Promise<DashboardPayload> {
    await delay(120)
    const upcomingExams = [...MOCK_EVENTS]
      .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())
      .filter((e) => e.dDay >= 0)
      .slice(0, 4)
      .map((e) => ({ ...e, dDay: calcDDay(e.startAt) }))
    return {
      user: MOCK_USERS[0],
      upcomingExams,
      weeklyStudy: MOCK_WEEKLY_STUDY,
      subjectProgress: MOCK_SUBJECT_PROGRESS,
      accuracyTrend: MOCK_ACCURACY_TREND,
      recommendation: MOCK_PRIORITY,
    }
  },
}

export const getDashboard = dashboardApi.getDashboard
