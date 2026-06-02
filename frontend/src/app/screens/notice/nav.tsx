import type { NavEntry } from '../../nav/types'

import { NoticesScreen } from '../NoticesScreen'

export const noticeNavEntry: NavEntry = {
  id: 'notices',
  label: '공지사항',
  section: '운영',
  icon: 'bell',
  render: ({ session }) => <NoticesScreen session={session} />,
}
