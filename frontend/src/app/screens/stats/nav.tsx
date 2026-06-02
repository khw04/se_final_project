import type { NavEntry } from '../../nav/types'

import { StatsScreen } from './StatsScreen'

export const statsNavEntry: NavEntry = {
  id: 'stats',
  label: '통계',
  section: '학습',
  icon: 'sparkle',
  render: () => <StatsScreen />,
}
