import type { NavEntry } from '../../nav/types'

import { PasswordResetScreen } from './PasswordResetScreen'

export const passwordResetNavEntry: NavEntry = {
  id: 'password-reset',
  label: '비밀번호 재설정',
  section: '운영',
  icon: 'refresh',
  render: ({ session }) => <PasswordResetScreen session={session} />,
}
