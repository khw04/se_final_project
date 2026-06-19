import type { NavEntry } from '../../nav/types'

import { DashboardScreen } from './DashboardScreen'

export const dashboardNavEntry: NavEntry = {
  id: 'dashboard',
  label: '대시보드',
  section: '학습',
  icon: 'dashboard',
  render: ({ session, navigate, studyTimer }) => (
    <DashboardScreen
      session={session}
      onJumpTo={navigate}
      onOpenSubjectNotes={(subjectId) => navigate('notes', { subjectId })}
      studyTimer={studyTimer}
    />
  ),
}
