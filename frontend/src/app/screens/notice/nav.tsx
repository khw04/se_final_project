import type { NavEntry } from '../../nav/types'

import { NoticeScreen } from './NoticeScreen'

export const noticeNavEntry: NavEntry = {
  id: 'notices',
  label: '공지사항',
  section: '운영',
  icon: 'bell',
  render: ({ session }) => <NoticeScreen session={session} />,
}
