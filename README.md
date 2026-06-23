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
**Deployment:** Render blueprint ([`render.yaml`](render.yaml)) provisions PostgreSQL + the
Dockerized service. Swagger UI at `/swagger-ui.html`.

## Connecting the two
Point the frontend's API base URL at the deployed backend URL, and set the backend's
`CORS_ALLOWED_ORIGINS` to the Netlify site URL.
