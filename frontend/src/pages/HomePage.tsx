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
      </div>
      <div className="home-page__sidebar">
        <AuthCard />
      </div>
    </section>
  )
}
