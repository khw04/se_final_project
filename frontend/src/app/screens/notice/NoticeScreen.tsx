import { useState } from 'react'

import type { AuthSession } from '../../../lib/authApi'

import { ApiError } from '../../api/client'
import { noticeApi } from '../../api/noticeApi'
import { Icon } from '../../components/Icon'
import type { Notice, NoticeDraft } from '../../types'
import { NOTICE_TAGS } from '../../types'
import { useApi } from '../../useApi'

type Mode = 'list' | 'detail' | 'editor'

type Feedback = { tone: 'success' | 'error'; text: string }

const EMPTY_DRAFT: NoticeDraft = { title: '', tag: '공지', body: '', pinned: false }

function warningTag(tag: Notice['tag']) {
  return tag === '점검' || tag === '약관'
}

function describeError(error: unknown) {
  if (error instanceof ApiError) {
    return `${error.message} (오류 ${error.status})`
  }
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '요청을 처리하지 못했습니다.'
}

export function NoticeScreen({ session }: { session: AuthSession }) {
  const isAdmin = session.role === 'ADMIN'

  const { data: notices, loading, refetch } = useApi(() => noticeApi.getNotices(), [])
  const [mode, setMode] = useState<Mode>('list')
  const [selected, setSelected] = useState<Notice | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [draft, setDraft] = useState<NoticeDraft>(EMPTY_DRAFT)
  const [feedback, setFeedback] = useState<Feedback | null>(null)
  const [busy, setBusy] = useState(false)

  function backToList() {
    setMode('list')
    setSelected(null)
    setEditingId(null)
    refetch()
  }

  async function openDetail(id: number) {
    setFeedback(null)
    try {
      const notice = await noticeApi.getNotice(id)
      setSelected(notice)
      setMode('detail')
    } catch (error) {
      setFeedback({ tone: 'error', text: describeError(error) })
    }
  }

  function startCreate() {
    setDraft(EMPTY_DRAFT)
    setEditingId(null)
    setFeedback(null)
    setMode('editor')
  }

  function startEdit(notice: Notice) {
    setDraft({ title: notice.title, tag: notice.tag, body: notice.body, pinned: notice.pinned })
    setEditingId(notice.id)
    setFeedback(null)
    setMode('editor')
  }

  async function submitEditor() {
    if (!draft.title.trim() || !draft.body.trim()) {
      setFeedback({ tone: 'error', text: '제목과 본문을 모두 입력하세요.' })
      return
    }

    setBusy(true)
    try {
      if (editingId === null) {
        await noticeApi.createNotice(draft)
        setFeedback({ tone: 'success', text: '공지가 게시되었습니다.' })
      } else {
        await noticeApi.updateNotice(editingId, draft)
        setFeedback({ tone: 'success', text: '공지가 수정되었습니다.' })
      }
      setMode('list')
      setEditingId(null)
      refetch()
    } catch (error) {
      setFeedback({ tone: 'error', text: describeError(error) })
    } finally {
      setBusy(false)
    }
  }

  async function removeNotice(notice: Notice) {
    if (!window.confirm(`"${notice.title}" 공지를 삭제할까요?`)) {
      return
    }

    setBusy(true)
    try {
      await noticeApi.deleteNotice(notice.id)
      setFeedback({ tone: 'success', text: '공지가 삭제되었습니다.' })
      setMode('list')
      setSelected(null)
      refetch()
    } catch (error) {
      setFeedback({ tone: 'error', text: describeError(error) })
    } finally {
      setBusy(false)
    }
  }

  if (loading || !notices) {
    return (
      <div className="screen">
        <header>
          <p className="eyebrow">공지사항</p>
          <h1 className="screen__heading">공지 / 업데이트</h1>
        </header>
        <div className="surface" style={{ height: 300, opacity: 0.4 }} />
      </div>
    )
  }

  return (
    <div className="screen">
      <header>
        <p className="eyebrow">{isAdmin ? '관리자 · 공지사항' : '공지사항'}</p>
        <h1 className="screen__heading">공지 / 업데이트</h1>
        <p className="screen__lede">
          {isAdmin
            ? '관리자 권한으로 공지를 작성·수정·삭제할 수 있습니다. ADMIN 권한은 JWT role claim으로 검증됩니다.'
            : 'Pokemo 서비스의 점검, 업데이트, 안내를 한곳에서 확인하세요.'}
        </p>
      </header>

      {feedback ? (
        <p
          role="status"
          style={{
            margin: 0,
            padding: '10px 14px',
            borderRadius: 10,
            fontSize: 14,
            background: feedback.tone === 'error' ? 'rgba(239,68,68,0.12)' : 'rgba(34,197,94,0.12)',
            color: feedback.tone === 'error' ? '#b91c1c' : '#15803d',
          }}
        >
          {feedback.text}
        </p>
      ) : null}

      {mode === 'list' && isAdmin ? (
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <button
            type="button"
            className="auth-card__submit"
            style={{ padding: '8px 16px', fontSize: 14, display: 'inline-flex', alignItems: 'center' }}
            onClick={startCreate}
          >
            <Icon name="plus" size={14} style={{ marginRight: 6 }} />새 공지 작성
          </button>
        </div>
      ) : null}

      {mode === 'editor' ? (
        <section className="surface notices-composer">
          <div className="surface__title">
            <h2>{editingId === null ? '새 공지사항' : '공지 수정'}</h2>
            <span className="tag tag--accent">ADMIN</span>
          </div>
          <label style={{ display: 'grid', gap: 6, marginBottom: 12 }}>
            <span className="label">제목</span>
            <input
              className="short-input"
              value={draft.title}
              onChange={(event) => setDraft({ ...draft, title: event.target.value })}
              placeholder="제목을 입력하세요"
            />
          </label>
          <label style={{ display: 'grid', gap: 6, marginBottom: 12 }}>
            <span className="label">태그</span>
            <select
              className="short-input"
              value={draft.tag}
              onChange={(event) => setDraft({ ...draft, tag: event.target.value as Notice['tag'] })}
            >
              {NOTICE_TAGS.map((tag) => (
                <option key={tag} value={tag}>
                  {tag}
                </option>
              ))}
            </select>
          </label>
          <label style={{ display: 'grid', gap: 6 }}>
            <span className="label">본문 (Markdown)</span>
            <textarea
              className="notes-editor__textarea"
              rows={8}
              value={draft.body}
              onChange={(event) => setDraft({ ...draft, body: event.target.value })}
              placeholder="### 내용을 입력하세요"
            />
          </label>
          <footer style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16 }}>
            <label style={{ display: 'inline-flex', alignItems: 'center', gap: 8, color: 'var(--color-text)', fontSize: 14 }}>
              <input
                type="checkbox"
                checked={draft.pinned}
                onChange={(event) => setDraft({ ...draft, pinned: event.target.checked })}
              />{' '}
              상단 고정
            </label>
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="button" className="surface__title-action" onClick={backToList} disabled={busy}>
                취소
              </button>
              <button
                type="button"
                className="auth-card__submit"
                style={{ padding: '8px 16px', fontSize: 14 }}
                onClick={submitEditor}
                disabled={busy}
              >
                {busy ? '처리 중...' : editingId === null ? '게시하기' : '수정 저장'}
              </button>
            </div>
          </footer>
        </section>
      ) : null}

      {mode === 'detail' && selected ? (
        <section className="surface">
          <div className="surface__title">
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <span className={`tag tag--${warningTag(selected.tag) ? 'warning' : 'accent'}`}>{selected.tag}</span>
              <h2 style={{ margin: 0 }}>{selected.title}</h2>
            </div>
            <button type="button" className="surface__title-action" onClick={backToList}>
              목록으로
            </button>
          </div>
          <p style={{ color: 'var(--color-muted)', fontSize: 13, marginTop: 4 }}>
            {selected.author} · {selected.createdAt.slice(0, 10)} · 조회 {selected.viewCount.toLocaleString()}
          </p>
          <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.7, marginTop: 16, color: 'var(--color-ink)' }}>
            {selected.body}
          </div>
          {isAdmin ? (
            <footer style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', marginTop: 20 }}>
              <button type="button" className="surface__title-action" onClick={() => startEdit(selected)} disabled={busy}>
                수정
              </button>
              <button
                type="button"
                className="surface__title-action"
                style={{ color: '#b91c1c' }}
                onClick={() => removeNotice(selected)}
                disabled={busy}
              >
                삭제
              </button>
            </footer>
          ) : null}
        </section>
      ) : null}

      {mode === 'list' ? (
        <section className="surface">
          <div className="surface__title">
            <h2>전체 공지 ({notices.length})</h2>
          </div>

          <table className="notices-table">
            <thead>
              <tr>
                <th style={{ width: 40 }}>#</th>
                <th>제목</th>
                <th style={{ width: 120 }}>작성자</th>
                <th style={{ width: 120 }}>날짜</th>
                <th style={{ width: 80 }}>조회수</th>
                {isAdmin ? <th style={{ width: 110 }} aria-label="관리" /> : null}
              </tr>
            </thead>
            <tbody>
              {notices.map((notice) => (
                <tr key={notice.id} className={notice.pinned ? 'is-pinned' : ''}>
                  <td>{notice.pinned ? <span className="pinned-dot" title="고정" /> : notice.id}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => openDetail(notice.id)}
                      style={{
                        display: 'flex',
                        gap: 8,
                        alignItems: 'center',
                        background: 'none',
                        border: 'none',
                        padding: 0,
                        cursor: 'pointer',
                        textAlign: 'left',
                      }}
                    >
                      <span className={`tag tag--${warningTag(notice.tag) ? 'warning' : 'accent'}`}>{notice.tag}</span>
                      <span style={{ color: 'var(--color-ink)', fontWeight: 700 }}>{notice.title}</span>
                    </button>
                  </td>
                  <td>{notice.author}</td>
                  <td style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }}>{notice.createdAt.slice(0, 10)}</td>
                  <td style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }}>{notice.viewCount.toLocaleString()}</td>
                  {isAdmin ? (
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button type="button" className="row-icon" aria-label="수정" onClick={() => startEdit(notice)}>
                          <Icon name="book" size={16} />
                        </button>
                        <button type="button" className="row-icon" aria-label="삭제" onClick={() => removeNotice(notice)}>
                          <Icon name="x" size={16} />
                        </button>
                      </div>
                    </td>
                  ) : null}
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      ) : null}

      {mode === 'list' && !isAdmin ? (
        <p style={{ textAlign: 'center', color: 'var(--color-muted)', fontSize: 13, marginTop: 12 }}>
          공지사항 작성·수정·삭제는 관리자(ADMIN) 권한이 필요합니다.
        </p>
      ) : null}
    </div>
  )
}
