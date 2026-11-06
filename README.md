# UBB Events

Event management system for student organizations at Babeș-Bolyai University (UBB).
Built by a team of 5 students for a university course on **GenAI-assisted development with
visible human oversight** (every AI-assisted task is logged in
[`docs/genai/usage-log.md`](docs/genai/usage-log.md)).

| ID | Workflow |
|----|----------|
| W1 | Organizations & membership |
| W2 | Event creation & lifecycle |
| W3 | Event discovery & search |
| W4 | Participant registration (capacity limits) |
| W5 | Event organization (organizer tasks, attendance check-in, announcements) |

**Stack:** Java 21 + Spring Boot 3 (REST, JWT, JPA, Flyway) · React + TypeScript (Vite) ·
PostgreSQL 16 · Docker Compose · GitHub Actions.

---

## Prerequisites

| To... | You need |
|-------|----------|
| Run the whole app (easiest) | [Docker Desktop](https://www.docker.com/products/docker-desktop/) (running) and Git |
| Develop the backend | + **Java 21** (e.g. Temurin). Maven is NOT needed: use the wrapper `./mvnw` |
| Develop the frontend | + **Node 24 LTS** (see `frontend/.nvmrc`; with nvm: `nvm use`) |

Windows: run the commands in Git Bash or WSL, or use `mvnw.cmd` instead of `./mvnw`.

## 1. First-time setup

```bash
git clone https://github.com/PatriciuMina/GenAI-Project.git
cd GenAI-Project
cp .env.example .env
```

Open `.env` and set at least:

- `JWT_SECRET`: a random string of 32+ characters. Generate one with `openssl rand -base64 48`.
- `POSTGRES_PASSWORD`: any password (it is only used on your machine).

`.env` is git-ignored. **Never commit it**; only `.env.example` (placeholders) is in Git.

## 2. Run everything with Docker (recommended for trying the app)

```bash
docker compose up --build
```

The first build downloads dependencies and takes a few minutes. When the logs calm down:

| What | URL |
|------|-----|
| **App (frontend)** | <http://localhost:3000> |
| Swagger UI (API docs, try requests) | <http://localhost:8080/swagger-ui.html> |
| Health check | <http://localhost:8080/api/v1/health> → `{"status":"UP", ...}` |
| PostgreSQL | `localhost:5432` (user/password/db from `.env`) |

How it works: `docker-compose.yml` starts three containers: `postgres`, `backend` (Spring Boot)
and `frontend` (nginx serving the built React app). nginx forwards every `/api/...` request to
the backend, so the browser only talks to `http://localhost:3000`. The backend waits until the
database is healthy, and the frontend waits until the backend is healthy.

Useful commands:

```bash
docker compose up --build -d     # same, in the background
docker compose ps                # all three should be "Up ... (healthy)" / "Up"
docker compose logs -f backend   # follow the backend logs
docker compose down              # stop (data is kept)
docker compose down -v           # stop AND delete the database (fresh seed next time)
```

Ports can be changed in `.env` (`FRONTEND_PORT`, `BACKEND_PORT`, `POSTGRES_PORT`).

> The Docker setup is a **local/demo environment**: it always runs the backend with the `dev`
> profile and loads the demo data (see below). A production setup without demo data comes later.

## 3. Seeded test accounts

All accounts have the password **`password`**.

| Email | Role |
|-------|------|
| `admin@ubbevents.ro` | ADMIN |
| `ana.popescu@stud.ubbcluj.ro` | STUDENT, PRESIDENT of *UBB Computer Science Society* |
| `mihai.ionescu@stud.ubbcluj.ro` | STUDENT, PRESIDENT of *UBB Theatre Club* |
| `elena.marin@stud.ubbcluj.ro` | STUDENT, ORGANIZER in *UBB Computer Science Society* |
| `radu.pop@stud.ubbcluj.ro` | STUDENT, ORGANIZER in *UBB Theatre Club* |
| `ioana.rusu@stud.ubbcluj.ro` | STUDENT, MEMBER of *UBB Computer Science Society* |
| `maria.dobre@stud.ubbcluj.ro` | STUDENT, MEMBER of *UBB Theatre Club* |
| `andrei.toma@stud.ubbcluj.ro` | STUDENT, no organization |

Seeded organizations and events are listed in [`docs/data-model.md`](docs/data-model.md#dev-seed-data).

## 4. Run backend and frontend separately (for development)

Use this while you write code: changes are picked up without rebuilding Docker images.
Only the database runs in Docker. If the full Docker stack is running, stop it first
(`docker compose down`), otherwise port 8080 is already taken.

```bash
# terminal 1: database only
docker compose up -d postgres

# terminal 2: backend on http://localhost:8080 (reads the root .env automatically)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# terminal 3: frontend on http://localhost:5173 (proxies /api to :8080)
cd frontend
npm install
npm run dev
```

Always start the backend with the **`dev` profile** on this database. The `dev` profile adds the
demo data, and once that data exists the backend refuses to start without it (Flyway reports
a "missing" migration). If that ever happens, start with `-Dspring-boot.run.profiles=dev`, or
reset the database with `docker compose down -v`.

Checks (the same ones CI runs on every pull request):

```bash
cd backend && ./mvnw verify                                   # needs Docker running (Testcontainers)
cd frontend && npm ci && npm run lint && npm test && npm run build
```

## 5. Documentation

| Topic | Where |
|-------|-------|
| How we work: board, branches, PRs, Definition of Done | [`CONTRIBUTING.md`](CONTRIBUTING.md) |
| Backend: how to add a feature (step by step) | [`docs/dev-guide/how-to-add-a-feature.md`](docs/dev-guide/how-to-add-a-feature.md) |
| Frontend guide | [`docs/dev-guide/frontend-guide.md`](docs/dev-guide/frontend-guide.md) |
| Data model + ER diagram + seed data | [`docs/data-model.md`](docs/data-model.md) |
| Architecture decisions (ADR-001 … ADR-005) | [`docs/architecture/`](docs/architecture/) |
| GenAI usage log and session transcripts | [`docs/genai/`](docs/genai/) |
| Project rules for AI assistants | [`CLAUDE.md`](CLAUDE.md) |
| API reference | Swagger UI at <http://localhost:8080/swagger-ui.html> (when the backend runs) |

## Project layout

```
backend/     Spring Boot app (Maven), feature packages under ro.ubb.events
frontend/    React + TypeScript (Vite); served by nginx in Docker
docs/        architecture (ADRs), dev guides, data model, GenAI evidence
.github/     CI workflow, issue and pull request templates
docker-compose.yml   postgres + backend + frontend
.env.example         template for your local .env
```
