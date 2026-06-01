import type { NavEntry } from '../../nav/types'

import { DashboardScreen } from './DashboardScreen'

export const dashboardNavEntry: NavEntry = {
  id: 'dashboard',
  label: '대시보드',
  section: '학습',
  icon: 'dashboard',
  render: ({ session, navigate }) => <DashboardScreen session={session} onJumpTo={navigate} />,
}
