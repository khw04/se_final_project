import { ApiHealthCard } from '../components/ApiHealthCard'
import { AuthCard } from '../components/AuthCard'
import './HomePage.css'

export function HomePage() {
  const springRings = Array.from({ length: 8 }, (_, index) => index)

  return (
    <section className="home-page" aria-labelledby="home-title">
      <div className="hero-panel">
        <div className="hero-panel__spring" aria-hidden="true">
          {springRings.map((ring) => (
            <span key={ring} />
          ))}
        </div>
        <p className="hero-panel__eyebrow">AI 학습 관리 플랫폼</p>
        <h1 id="home-title">
          <span>학습 흐름을 한곳에,</span>
          <span className="hero-panel__title-brand">Pokemo</span>
        </h1>
        <p className="hero-panel__copy">
          Pokemo는 학습 계획, 노트, 퀴즈, 인사이트 흐름을 하나로 연결하는
          AI 기반 학습 관리 플랫폼입니다. 백엔드 서비스가 준비되는 대로
          주요 학습 기능을 이 화면에 이어 붙일 예정입니다.
        </p>
      </div>
      <div className="home-page__sidebar">
        <AuthCard />
        <ApiHealthCard />
      </div>
    </section>
  )
}
