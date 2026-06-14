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

Set `VITE_API_BASE_URL` when the Spring Boot backend exists. The value should be the API root itself:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

If the environment variable is not set, local development defaults to `http://localhost:8080/api`.
Docker Compose builds the frontend with `VITE_API_BASE_URL=/api`, and `frontend/nginx.conf` proxies `/api/` and `/actuator/` to the backend container.

## OAuth and Demo Environment

The browser only receives public OAuth client IDs:

```env
VITE_GOOGLE_CLIENT_ID=
VITE_KAKAO_CLIENT_ID=
```

Provider secrets, Gemini keys, SMTP passwords, and VAPID private keys must stay in the backend/deploy environment. For frontend-only mock sessions, use `.env.mock` with `VITE_POKEMO_MOCK_SESSION=true`; normal build/dev runs do not load that file automatically.
