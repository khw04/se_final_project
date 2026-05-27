import { useState } from 'react'

import type { AuthSession } from '../../lib/authApi'

import { Icon } from '../components/Icon'
import { pokemoApi } from '../pokemoApi'
import type { Notice } from '../types'
import { useApi } from '../useApi'

type Draft = {
  title: string
  tag: Notice['tag']
  body: string
  pinned: boolean
}

const EMPTY_DRAFT: Draft = { title: '', tag: '공지', body: '', pinned: false }

export function NoticesScreen({ session }: { session: AuthSession }) {
  const isAdmin = session.role === 'ADMIN'
  const [composing, setComposing] = useState(false)
  const [draft, setDraft] = useState<Draft>(EMPTY_DRAFT)
  const { data: notices, loading, refetch } = useApi(() => pokemoApi.getNotices(), [])

  function publish() {
    if (!draft.title.trim()) return
    pokemoApi.createNotice(draft).then(() => {
      setDraft(EMPTY_DRAFT)
      setComposing(false)
      refetch()
    })
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
            ? '관리자 권한으로 모든 공지사항을 작성·수정·삭제할 수 있습니다. ADMIN 역할은 JWT 토큰의 role claim으로 검증됩니다.'
            : 'Pokemo 서비스의 점검, 업데이트, 안내를 한곳에서 확인하세요.'}
        </p>
      </header>

      {isAdmin ? (
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <button
            type="button"
            className="auth-card__submit"
            style={{ padding: '8px 16px', fontSize: 14, display: 'inline-flex', alignItems: 'center' }}
            onClick={() => setComposing((v) => !v)}
          >
            <Icon name="plus" size={14} style={{ marginRight: 6 }} />
            {composing ? '취소' : '새 공지 작성'}
          </button>
        </div>
      ) : null}

      {composing ? (
        <section className="surface notices-composer">
          <div className="surface__title">
            <h2>새 공지사항</h2>
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
              {(['공지', '점검', '약관', '베타', '런칭'] as const).map((tag) => (
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
              rows={6}
              value={draft.body}
              onChange={(event) => setDraft({ ...draft, body: event.target.value })}
              placeholder="### 내용을 입력하세요"
            />
          </label>
          <footer style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 16 }}>
            <label
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 8,
                color: 'var(--color-text)',
                fontSize: 14,
              }}
            >
              <input
                type="checkbox"
                checked={draft.pinned}
                onChange={(event) => setDraft({ ...draft, pinned: event.target.checked })}
              />{' '}
              상단 고정
            </label>
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="button" className="surface__title-action">
                미리보기
              </button>
              <button
                type="button"
                className="auth-card__submit"
                style={{ padding: '8px 16px', fontSize: 14 }}
                onClick={publish}
              >
                게시하기
              </button>
            </div>
          </footer>
        </section>
      ) : null}

      <section className="surface">
        <div className="surface__title">
          <h2>전체 공지 ({notices.length})</h2>
        </div>

        <table className="notices-table">
          <thead>
            <tr>
              <th style={{ width: 40 }}>#</th>
              <th>제목</th>
              <th style={{ width: 100 }}>작성자</th>
              <th style={{ width: 120 }}>날짜</th>
              <th style={{ width: 80 }}>조회수</th>
              {isAdmin ? <th style={{ width: 80 }} aria-label="관리" /> : null}
            </tr>
          </thead>
          <tbody>
            {notices.map((notice) => (
              <tr key={notice.id} className={notice.pinned ? 'is-pinned' : ''}>
                <td>{notice.pinned ? <span className="pinned-dot" title="고정" /> : notice.id}</td>
                <td>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <span
                      className={`tag tag--${notice.tag === '점검' || notice.tag === '약관' ? 'warning' : 'accent'}`}
                    >
                      {notice.tag}
                    </span>
                    <span style={{ color: 'var(--color-ink)', fontWeight: 700 }}>{notice.title}</span>
                  </div>
                </td>
                <td>{notice.author}</td>
                <td style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }}>{notice.createdAt.slice(0, 10)}</td>
                <td style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }}>{notice.viewCount.toLocaleString()}</td>
                {isAdmin ? (
                  <td>
                    <button type="button" className="row-icon" aria-label="더보기">
                      <Icon name="more" size={16} />
                    </button>
                  </td>
                ) : null}
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {!isAdmin ? (
        <p style={{ textAlign: 'center', color: 'var(--color-muted)', fontSize: 13, marginTop: 12 }}>
          공지사항 작성·수정·삭제는 관리자(ADMIN) 권한이 필요합니다.
        </p>
      ) : null}
    </div>
  )
}
