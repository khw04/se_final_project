import type { NavEntry } from '../../nav/types'

import { QuizScreen } from '../QuizScreen'

export const quizNavEntry: NavEntry = {
  id: 'quiz',
  label: '퀴즈',
  section: '학습',
  icon: 'brain',
  render: () => <QuizScreen />,
}
