import { aiApi } from './api/aiApi'
import { calendarApi } from './api/calendarApi'
import { dashboardApi } from './api/dashboardApi'
import { noteApi } from './api/noteApi'
import { noticeApi } from './api/noticeApi'
import { quizApi } from './api/quizApi'
import { statsApi } from './api/statsApi'
import { subjectApi } from './api/subjectApi'
import { userApi } from './api/userApi'

export type { EventRange } from './api/calendarApi'
export type { NoteQuery } from './api/noteApi'
export type { WrongAnswerQuery } from './api/quizApi'

export const pokemoApi = {
  ...userApi,
  ...noticeApi,
  ...calendarApi,
  ...subjectApi,
  ...noteApi,
  ...quizApi,
  ...aiApi,
  ...dashboardApi,
  ...statsApi,
}

export const subjectById = subjectApi.subjectById
export const tagById = subjectApi.tagById
export const attachmentById = noteApi.attachmentById
export const today = calendarApi.today
export const relativeKo = calendarApi.relativeKo
export const calcDDay = calendarApi.calcDDay
