import type { NavEntry } from '../../nav/types'

import { CalendarScreen } from '../CalendarScreen'

export const calendarNavEntry: NavEntry = {
  id: 'calendar',
  label: '캘린더',
  section: '학습',
  icon: 'calendar',
  render: () => <CalendarScreen />,
}
