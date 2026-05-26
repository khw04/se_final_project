# Pokemo Frontend

React SPA scaffold for Pokemo, an AI-based learning management platform.

## Commands

```bash
npm install
npm run dev
npm run build
npm run test
```

## Structure

- `src/App.tsx` contains the application shell and navigation entry point.
- `src/routes.ts` keeps the initial route map ready for a router integration.
- `src/pages/HomePage.tsx` renders the stage-1 landing screen.
- `src/components/ApiHealthCard.tsx` displays the backend health placeholder.
- `src/lib/apiHealth.ts` centralizes the future API health endpoint.
- `src/styles/tokens.css` defines the initial design tokens.

## API Health Placeholder

Set `VITE_API_BASE_URL` when the Spring Boot backend exists. The placeholder
currently expects a future health endpoint at:

```text
${VITE_API_BASE_URL}/actuator/health
```

If the environment variable is not set, the frontend displays
`http://localhost:8080/actuator/health`.
