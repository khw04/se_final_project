import type { NavEntry } from '../../nav/types'

import { RecommendScreen } from '../RecommendScreen'

export const recommendNavEntry: NavEntry = {
  id: 'recommend',
  label: 'AI 추천',
  section: '인사이트',
  icon: 'sparkle',
  render: ({ navigate }) => <RecommendScreen onOpenNote={(noteId) => navigate('notes', { noteId })} />,
}
