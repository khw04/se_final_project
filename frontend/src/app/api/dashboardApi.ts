import type { DashboardPayload } from '../types'

import { calendarApi } from './calendarApi'
import { statsApi } from './statsApi'
import { userApi } from './userApi'

export const dashboardApi = {
  // AI 추천(/recommend)은 무거운 집계라 대시보드 핵심 데이터와 분리해 별도로 로드한다.
  // (DashboardScreen에서 독립 useApi로 호출)
  async getDashboard(): Promise<DashboardPayload> {
    const today = new Date()
    const to = new Date(today)
    to.setDate(today.getDate() + 30)
    const fromStr = today.toISOString().slice(0, 10)
    const toStr = to.toISOString().slice(0, 10)
    const [user, events, weeklyStudy, subjectProgress, accuracyTrend] = await Promise.all([
      userApi.getMe(),
      calendarApi.getEvents({ from: fromStr, to: toStr }),
      statsApi.getWeeklyStudy(),
      statsApi.getSubjectProgress(),
      statsApi.getAccuracyTrend(),
    ])
    const upcomingExams = events
      .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())
      .filter((event) => event.dDay >= 0)
      .slice(0, 4)
    return {
      user,
      upcomingExams,
      weeklyStudy,
      subjectProgress,
      accuracyTrend,
    }
  },
}

export const getDashboard = dashboardApi.getDashboard
