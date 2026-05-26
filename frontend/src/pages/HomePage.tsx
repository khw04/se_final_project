import { ApiHealthCard } from '../components/ApiHealthCard'
import './HomePage.css'

export function HomePage() {
  return (
    <section className="home-page" aria-labelledby="home-title">
      <div className="hero-panel">
        <p className="hero-panel__eyebrow">AI learning management platform</p>
        <h1 id="home-title">Study 흐름을 한곳에 모으는 Pokemo</h1>
        <p className="hero-panel__copy">
          Pokemo is the React SPA shell for an AI-based learning management
          platform that will connect study planning, notes, quizzes, and insight
          workflows as the backend services come online.
        </p>
      </div>
      <ApiHealthCard />
    </section>
  )
}
