import type { NavEntry } from '../../nav/types'

import { NotesScreen } from '../NotesScreen'

export const noteNavEntry: NavEntry = {
  id: 'notes',
  label: '노트',
  section: '학습',
  icon: 'book',
  render: ({ options }) => <NotesScreen key={options?.noteId ?? 'all'} initialNoteId={options?.noteId} />,
}
