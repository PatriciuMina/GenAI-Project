# CLAUDE.md

## SAFETY RULES (apply to every task)

- Work ONLY inside this folder: `~/projects/GenAI-Project`. Never read or modify files
  outside it (except normal tool caches like `~/.m2` and `~/.npm`).
- The ONLY GitHub repository for this project is `PatriciuMina/GenAI-Project`.
  Every `gh` command must include `--repo PatriciuMina/GenAI-Project` (or
  `repos/PatriciuMina/GenAI-Project` for `gh api`), and GitHub Projects must use
  `--owner PatriciuMina`. Never use any other repository, organization or account.
- Never change git config, gh auth, or any global/user settings.
- Do NOT commit or push unless the human explicitly asks. Humans review and commit.
- Do not use any MCP servers, connectors or external services other than GitHub via
  `gh`, Maven Central, npm and Docker Hub.
- Never put secrets in committed files; only `.env.example` (with placeholder values).

## Rules for every future task

1. **Verification hand-off:** at the end of every task, tell the human exactly how to
   verify the work: commands to run, URLs to open, and the expected result.
2. **Usage log:** append an entry to `docs/genai/usage-log.md` (date, person, tool/model,
   purpose, prompt summary, what was generated, how it was verified, human changes).
3. **No secrets:** never commit secrets; only `.env.example` with placeholders. Real
   values live in `.env` (git-ignored).
4. Do not write application code beyond the scope of the current task.

## Project purpose

Enterprise event management system for student organizations at Babeș-Bolyai University
(UBB), built by a team of 5 students for a university course that requires
GenAI-assisted development with **visible human oversight** (see `docs/genai/`).

Main workflows:

| ID | Workflow |
|----|----------|
| W1 | Organizations & membership |
| W2 | Event creation & lifecycle |
| W3 | Event discovery & search |
| W4 | Participant registration (capacity limits) |
| W5 | Event organization (organizer tasks, attendance check-in, announcements) |

## Stack

- **Backend:** Java 21, Spring Boot 3.x, Maven (wrapper `./mvnw`), Spring Security (JWT),
  Spring Data JPA, Bean Validation, Flyway, springdoc-openapi.
- **Frontend:** React + TypeScript, Vite.
- **Database:** PostgreSQL 16 (Docker Compose for local dev).

## Folder layout

```
/backend             Spring Boot application (Maven)
/frontend            React + TypeScript (Vite)
/docs
  /architecture      ADRs (ADR-001 ... )
  /genai             usage-log.md, evidence/, sessions/
/scripts             helper scripts (dev, db, CI)
docker-compose.yml   local infrastructure (PostgreSQL)
.env.example         template for local environment variables
```

## Backend organization (mandatory)

Code is organized by **feature package**, not by technical layer. Base package
(proposed): `ro.ubb.events`.

```
ro.ubb.events
├── auth           login, JWT issuing/validation
├── user
├── organization   W1
├── event          W2, W3
├── registration   W4
├── organizing     W5 (tasks, check-in, announcements)
├── common         shared exceptions, error handler, base classes, utils
└── config         Spring configuration (security, OpenAPI, CORS, ...)
```

Each feature package contains the sub-packages
`controller`, `service`, `repository`, `dto`, `entity` (only those it needs).

Layering rules:
- `controller` → `service` → `repository`. Controllers never call repositories directly.
- Entities never leave the service layer; controllers accept/return DTOs only.
- Cross-feature access goes through the other feature's **service**, not its repository.

## Coding conventions

**Java / Spring**
- Constructor injection only (no field `@Autowired`); prefer `final` fields.
- DTOs as Java `record`s; validate input with Bean Validation (`@Valid`, `@NotBlank`, ...).
- One global `@RestControllerAdvice` in `common` returning a consistent error body.
- Transactions (`@Transactional`) on service methods, not controllers.
- Schema changes only via Flyway migrations: `backend/src/main/resources/db/migration/V{n}__short_description.sql`.
  Never edit an applied migration; add a new one. JPA `ddl-auto` = `validate`.
- Tests: JUnit 5 + Mockito for services; `@SpringBootTest`/`@WebMvcTest` for controllers.

**REST API**
- Base path `/api/v1/...`, plural nouns (`/api/v1/events/{id}/registrations`).
- Correct status codes (201 on create, 204 on delete, 400/401/403/404/409).
- Documented via OpenAPI (springdoc); Swagger UI at `/swagger-ui.html`.

**TypeScript / React**
- `strict` mode; no `any` without justification.
- Organize by feature (`src/features/events/...`), shared UI in `src/components`.
- Single typed API client module; base URL from `VITE_API_BASE_URL`.

**Git**
- Feature branches (`feature/<short-name>`), Conventional Commits (`feat:`, `fix:`, `docs:` ...).
- Every change merged via PR reviewed by at least one human teammate.

## How to run

```bash
cp .env.example .env              # then edit values locally
docker compose up -d              # PostgreSQL 16 on localhost:${POSTGRES_PORT}
docker compose ps                 # postgres should be "healthy"

# once scaffolded:
cd backend && ./mvnw spring-boot:run      # API on http://localhost:8080
cd frontend && npm install && npm run dev # UI on http://localhost:5173
```

## Architecture decisions

See `docs/architecture/`:
- ADR-001 Layered architecture with feature packages
- ADR-002 REST API + OpenAPI
- ADR-003 JWT authentication with Spring Security
- ADR-004 Flyway database migrations
- ADR-005 Docker Compose for local infrastructure
