import type { DashboardPayload } from '../types'

import { aiApi } from './aiApi'
import { calendarApi } from './calendarApi'
import { statsApi } from './statsApi'
import { userApi } from './userApi'

export const dashboardApi = {
  async getDashboard(): Promise<DashboardPayload> {
    const today = new Date()
    const to = new Date(today)
    to.setDate(today.getDate() + 30)
    const fromStr = today.toISOString().slice(0, 10)
    const toStr = to.toISOString().slice(0, 10)
    const [user, events, weeklyStudy, subjectProgress, accuracyTrend, recommend] = await Promise.all([
      userApi.getMe(),
      calendarApi.getEvents({ from: fromStr, to: toStr }),
      statsApi.getWeeklyStudy(),
      statsApi.getSubjectProgress(),
      statsApi.getAccuracyTrend(),
      aiApi.getRecommend(),
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
      recommendation: recommend.priorities,
    }
  },
}

export const getDashboard = dashboardApi.getDashboard
