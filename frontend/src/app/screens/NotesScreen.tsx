import { useEffect, useMemo, useRef, useState } from 'react'

import { Icon } from '../components/Icon'
import { pokemoApi } from '../pokemoApi'
import type { Attachment, Subject, Tag } from '../types'
import { useApi } from '../useApi'

type SaveState = 'saved' | 'saving' | 'error'

const ALL_SUBJECTS = 0

export function NotesScreen() {
  const { data: subjects } = useApi(() => pokemoApi.getSubjects(), [])
  const { data: tags } = useApi(() => pokemoApi.getTags(), [])
  const { data: notes } = useApi(() => pokemoApi.getNotes(), [])

  const [activeId, setActiveId] = useState<number>(301)
  const [activeSubjectId, setActiveSubjectId] = useState<number>(1)
  const [query, setQuery] = useState<string>('')

  if (!subjects || !tags || !notes) {
    return (
      <div className="screen notes-screen">
        <header>
          <p className="eyebrow">노트</p>
          <h1 className="screen__heading">학습 노트</h1>
        </header>
        <div className="surface" style={{ height: 540, opacity: 0.4 }} />
      </div>
    )
  }

  const filteredNotes = notes.filter((note) => {
    if (activeSubjectId !== ALL_SUBJECTS && note.subjectId !== activeSubjectId) return false
    if (query) {
      const needle = query.toLowerCase()
      if (
        !note.title.toLowerCase().includes(needle) &&
        !note.preview.toLowerCase().includes(needle) &&
        !note.content.toLowerCase().includes(needle)
      ) {
        return false
      }
    }
    return true
  })

  const activeSubject = subjects.find((s) => s.id === activeSubjectId)

  return (
    <div className="screen notes-screen">
      <header>
        <p className="eyebrow">노트</p>
        <h1 className="screen__heading">학습 노트</h1>
      </header>

      <div className="notes-layout">
        <aside className="surface notes-left">
          <div className="notes-search">
            <Icon name="search" size={16} />
            <input
              placeholder="제목, 내용 검색"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </div>

          <p className="label" style={{ marginTop: 16 }}>
            과목
          </p>
          <div className="notes-subjects">
            <button
              type="button"
              className={activeSubjectId === ALL_SUBJECTS ? 'is-active' : ''}
              onClick={() => setActiveSubjectId(ALL_SUBJECTS)}
            >
              <span>전체</span>
              <span className="notes-subjects__count">{notes.length}</span>
            </button>
            {subjects.map((subject) => {
              const count = notes.filter((n) => n.subjectId === subject.id).length
              return (
                <button
                  key={subject.id}
                  type="button"
                  className={activeSubjectId === subject.id ? 'is-active' : ''}
                  onClick={() => setActiveSubjectId(subject.id)}
                >
                  <span>{subject.name}</span>
                  <span className="notes-subjects__count">{count}</span>
                </button>
              )
            })}
          </div>

          <p className="label" style={{ marginTop: 16 }}>
            태그
          </p>
          <div className="notes-tags">
            {tags.map((tag) => (
              <span key={tag.id} className="tag">
                {tag.name}
              </span>
            ))}
          </div>
        </aside>

        <section className="surface notes-list">
          <div className="surface__title">
            <h2>
              {activeSubject ? `${activeSubject.name} · ${filteredNotes.length}개` : `전체 · ${filteredNotes.length}개`}
            </h2>
            <button type="button" className="surface__title-action">
              <Icon name="plus" size={14} style={{ marginRight: 4 }} />
              새 노트
            </button>
          </div>
          <ul>
            {filteredNotes.map((note) => {
              const subject = subjects.find((s) => s.id === note.subjectId)
              const noteTags = note.tagIds
                .map((id) => tags.find((t) => t.id === id))
                .filter((t): t is Tag => Boolean(t))
              return (
                <li
                  key={note.id}
                  className={note.id === activeId ? 'is-active' : ''}
                  onClick={() => setActiveId(note.id)}
                  style={{ cursor: 'pointer' }}
                >
                  <p className="notes-list__title">{note.title}</p>
                  <p className="notes-list__subject">
                    {subject?.name} · {pokemoApi.relativeKo(note.updatedAt)}
                  </p>
                  <div className="notes-list__tags">
                    {noteTags.map((tag) => (
                      <span key={tag.id} className="tag">
                        {tag.name}
                      </span>
                    ))}
                  </div>
                </li>
              )
            })}
            {filteredNotes.length === 0 ? (
              <li className="muted-note" style={{ padding: 24, textAlign: 'center', border: 0 }}>
                조건에 맞는 노트가 없습니다.
              </li>
            ) : null}
          </ul>
        </section>

        <NoteEditor noteId={activeId} subjects={subjects} tags={tags} />
      </div>
    </div>
  )
}

type NoteEditorProps = {
  noteId: number
  subjects: Subject[]
  tags: Tag[]
}

function NoteEditor({ noteId, subjects, tags }: NoteEditorProps) {
  const { data: note, loading } = useApi(() => pokemoApi.getNote(noteId), [noteId])

  const [body, setBody] = useState<string>('')
  const [title, setTitle] = useState<string>('')
  const [attachments, setAttachments] = useState<Attachment[]>([])
  const [saveState, setSaveState] = useState<SaveState>('saved')

  const dirtyRef = useRef<boolean>(false)

  useEffect(() => {
    if (!note) return
    let alive = true
    dirtyRef.current = false
    Promise.resolve().then(() => {
      if (!alive) return
      setBody(note.content)
      setTitle(note.title)
    })
    pokemoApi.getAttachments(note.attachmentIds).then((nextAttachments) => {
      if (alive) setAttachments(nextAttachments)
    })
    return () => {
      alive = false
    }
  }, [note])

  useEffect(() => {
    if (!note || !dirtyRef.current) return
    setSaveState('saving')
    const timer = window.setTimeout(() => {
      pokemoApi
        .patchNote(note.id, { content: body })
        .then(() => setSaveState('saved'))
        .catch(() => setSaveState('error'))
    }, 2000)
    return () => window.clearTimeout(timer)
  }, [body, note])

  function onBodyChange(next: string) {
    dirtyRef.current = true
    setBody(next)
  }

  if (loading || !note) {
    return <section className="surface notes-editor" style={{ opacity: 0.4 }} />
  }

  const subject = subjects.find((s) => s.id === note.subjectId)
  const noteTags = note.tagIds.map((id) => tags.find((t) => t.id === id)).filter((t): t is Tag => Boolean(t))

  function formatBytes(n: number) {
    if (n < 1024) return `${n} B`
    if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
    return `${(n / 1024 / 1024).toFixed(1)} MB`
  }

  const saveLabel =
    saveState === 'saved' ? '자동 저장됨 · 방금 전' : saveState === 'saving' ? '저장 중...' : '저장 실패'
  const saveIcon = saveState === 'saved' ? 'check' : saveState === 'error' ? 'x' : 'clock'

  return (
    <section className="surface notes-editor">
      <div className="notes-editor__top">
        <input
          className="notes-editor__title"
          value={title}
          onChange={(event) => {
            dirtyRef.current = true
            setTitle(event.target.value)
          }}
        />
        <div className="notes-editor__meta">
          {subject ? <span className="tag tag--accent">{subject.name}</span> : null}
          {noteTags.map((tag) => (
            <span key={tag.id} className="tag">
              {tag.name}
            </span>
          ))}
          <button type="button" className="notes-editor__add-tag" aria-label="태그 추가">
            <Icon name="plus" size={14} />
          </button>
        </div>
      </div>

      <div className="notes-editor__split">
        <div className="notes-editor__ta-wrap">
          <textarea
            className="notes-editor__textarea"
            value={body}
            onChange={(event) => onBodyChange(event.target.value)}
            aria-label="Markdown 본문"
          />
        </div>
        <div className="notes-editor__preview">
          <MarkdownPreview source={body} />
        </div>
      </div>

      {attachments.length > 0 ? (
        <div className="attachments">
          <p className="label" style={{ margin: '0 0 8px' }}>
            첨부 이미지 · {attachments.length}건
          </p>
          <div className="attachments__strip">
            {attachments.map((att) => (
              <div key={att.id} className="attachment">
                <span className="attachment__thumb">
                  <img src={att.url} alt={att.name} />
                </span>
                <div className="attachment__info">
                  <p className="attachment__name" title={att.name}>
                    {att.name}
                  </p>
                  <p className="attachment__meta">{formatBytes(att.size)}</p>
                </div>
                <button type="button" className="attachment__remove" aria-label="첨부 삭제">
                  <Icon name="x" size={12} />
                </button>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      <footer className="notes-editor__footer">
        <span className={`save-status save-status--${saveState}`}>
          <Icon name={saveIcon} size={14} />
          {saveLabel}
        </span>
        <div style={{ display: 'flex', gap: 8 }}>
          <button type="button" className="surface__title-action">
            <Icon name="image" size={14} style={{ marginRight: 4 }} />
            이미지 첨부
          </button>
          <button type="button" className="surface__title-action">
            <Icon name="sparkle" size={14} style={{ marginRight: 4 }} />
            AI 퀴즈 생성
          </button>
        </div>
      </footer>
    </section>
  )
}

function MarkdownPreview({ source }: { source: string }) {
  const html = useMemo(() => renderMarkdown(source), [source])
  return <div className="md" dangerouslySetInnerHTML={{ __html: html }} />
}

function renderMarkdown(source: string): string {
  const escaped = source
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  const lines = escaped.split('\n')
  const out: string[] = []
  let inList = false
  let inOrdered = false
  let inQuote = false
  let inCode = false
  const flushList = () => {
    if (inList) {
      out.push('</ul>')
      inList = false
    }
    if (inOrdered) {
      out.push('</ol>')
      inOrdered = false
    }
  }
  const flushQuote = () => {
    if (inQuote) {
      out.push('</blockquote>')
      inQuote = false
    }
  }

  for (const raw of lines) {
    const line = raw

    if (line.startsWith('```')) {
      flushList()
      flushQuote()
      if (inCode) {
        out.push('</code></pre>')
        inCode = false
      } else {
        out.push('<pre><code>')
        inCode = true
      }
      continue
    }

    if (inCode) {
      out.push(line)
      continue
    }

    if (line.startsWith('# ')) {
      flushList()
      flushQuote()
      out.push(`<h1>${inline(line.slice(2))}</h1>`)
      continue
    }
    if (line.startsWith('## ')) {
      flushList()
      flushQuote()
      out.push(`<h2>${inline(line.slice(3))}</h2>`)
      continue
    }
    if (line.startsWith('### ')) {
      flushList()
      flushQuote()
      out.push(`<h3>${inline(line.slice(4))}</h3>`)
      continue
    }
    if (line.startsWith('> ')) {
      flushList()
      if (!inQuote) {
        out.push('<blockquote>')
        inQuote = true
      }
      out.push(`<p>${inline(line.slice(2))}</p>`)
      continue
    }
    if (line.startsWith('- ')) {
      flushQuote()
      if (inOrdered) {
        out.push('</ol>')
        inOrdered = false
      }
      if (!inList) {
        out.push('<ul>')
        inList = true
      }
      out.push(`<li>${inline(line.slice(2))}</li>`)
      continue
    }
    if (/^\d+\.\s/.test(line)) {
      flushQuote()
      if (inList) {
        out.push('</ul>')
        inList = false
      }
      if (!inOrdered) {
        out.push('<ol>')
        inOrdered = true
      }
      out.push(`<li>${inline(line.replace(/^\d+\.\s/, ''))}</li>`)
      continue
    }
    if (line.startsWith('---')) {
      flushList()
      flushQuote()
      out.push('<hr />')
      continue
    }
    if (line.trim() === '') {
      flushList()
      flushQuote()
      continue
    }

    flushList()
    flushQuote()
    out.push(`<p>${inline(line)}</p>`)
  }

  flushList()
  flushQuote()
  if (inCode) out.push('</code></pre>')

  return out.join('\n')

  function inline(text: string): string {
    return text
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\*([^*]+)\*/g, '<em>$1</em>')
      .replace(/~~([^~]+)~~/g, '<del>$1</del>')
  }
}
