import type { NavEntry } from '../../nav/types'

import { WrongAnswersScreen } from '../WrongAnswersScreen'

export const wrongNavEntry: NavEntry = {
  id: 'wrong',
  label: '오답노트',
  section: '학습',
  icon: 'x',
  render: ({ navigate }) => <WrongAnswersScreen onOpenQuiz={(quizId) => navigate('quiz', { quizId })} />,
}
