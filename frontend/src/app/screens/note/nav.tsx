import type { NavEntry } from '../../nav/types'

import { NotesScreen } from '../NotesScreen'

export const noteNavEntry: NavEntry = {
  id: 'notes',
  label: '노트',
  section: '학습',
  icon: 'book',
  render: ({ options, navigate }) => (
    <NotesScreen
      key={`${options?.noteId ?? 'all'}-${options?.subjectId ?? 'all'}`}
      initialNoteId={options?.noteId}
      initialSubjectId={options?.subjectId}
      onOpenQuiz={(quizId) => navigate('quiz', { quizId })}
    />
  ),
}
