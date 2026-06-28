# Hridaya Creations

Monorepo for **Hridaya Creations**, a personalized gifting e‑commerce platform.

```
.
├── frontend/        # React (Create React App) web client — deployed on Netlify
├── backend/         # Spring Boot 3 + PostgreSQL REST API — deployable on Render
├── netlify.toml     # Netlify build config (base = frontend)
└── render.yaml      # Render blueprint (Postgres + backend service)
```

## Frontend (`frontend/`)
Create React App client.
```bash
cd frontend
npm install
npm start          # dev server
npm run build      # production build -> frontend/build
```
**Deployment:** Netlify, configured by [`netlify.toml`](netlify.toml) with `base = "frontend"`.

## Backend (`backend/`)
Spring Boot 3 / Java 21 REST API with JWT auth, PostgreSQL, Swagger, Docker.
See [backend/README.md](backend/README.md) for full documentation.
```bash
cd backend
./mvnw spring-boot:run     # requires JDK 21
```
**Deployment:** Live on Render (Docker) — **https://hridaya-creations-backend.onrender.com**
- Swagger UI: https://hridaya-creations-backend.onrender.com/swagger-ui.html
- Health: https://hridaya-creations-backend.onrender.com/actuator/health
- Seeded admin: `admin@ridhayacreations.com` / `Admin@12345`
- DB: shares a PostgreSQL instance, with all tables isolated under a dedicated `hridaya` schema.
- Config: [`render.yaml`](render.yaml). A monorepo build filter limits backend rebuilds to `backend/**`.

> Note: the free Render web service sleeps after ~15 min idle; the first request
> after a cold start can take ~30–60s.

## Connecting the two
Point the frontend's API base URL at the deployed backend
(`https://hridaya-creations-backend.onrender.com/api/v1`), and set the backend's
`CORS_ALLOWED_ORIGINS` to the Netlify site URL.
