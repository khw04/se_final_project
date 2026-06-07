import type { DashboardPayload } from '../types'

import { aiApi } from './aiApi'
import { calendarApi } from './calendarApi'
import { statsApi } from './statsApi'

export const dashboardApi = {
  async getDashboard(): Promise<DashboardPayload> {
    const today = new Date()
    const to = new Date(today)
    to.setDate(today.getDate() + 30)
    const fromStr = today.toISOString().slice(0, 10)
    const toStr = to.toISOString().slice(0, 10)
    const [events, weeklyStudy, subjectProgress, accuracyTrend, recommend] = await Promise.all([
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
      user: { id: 0, email: 'user@pokemo.local', role: 'USER', emailVerified: true, createdAt: '' },
      upcomingExams,
      weeklyStudy,
      subjectProgress,
      accuracyTrend,
      recommendation: recommend.priorities,
    }
  },
}

export const getDashboard = dashboardApi.getDashboard
