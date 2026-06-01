import type { UserAccount } from '../types'

import { delay } from './dateUtils'
import { MOCK_USERS } from './mockData'

export const userApi = {
  async getMe(): Promise<UserAccount> {
    await delay(60)
    return MOCK_USERS[0]
  },
}

export const getMe = userApi.getMe
