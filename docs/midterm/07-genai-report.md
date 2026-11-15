# 7. GenAI declaration

Part **D** of the mid-term deliverable. Index: [README](README.md).

Course link: R13 (GenAI-assisted development) and R14 (human oversight), see
`docs/course/course-requirements.md`.

**Build this report from the evidence, not from memory:**
- [`docs/genai/usage-log.md`](../genai/usage-log.md): one row per GenAI task (tool/model,
  purpose, prompt summary, what was generated, how verified, human changes). This is the
  main source; every claim below should point to a row in it.
- [`docs/genai/sessions/`](../genai/sessions/): full session transcripts (`P1-foundations.txt`
  … `P6-docker-ci.txt`, plus later ones). Use them for concrete examples and quotes.
- [`docs/genai/evidence/`](../genai/evidence/): screenshots and other proof (add files here
  when you cite something that is not in a transcript).
- Git history (`git log`, merged PRs) to confirm which files a task touched.

If a teammate used GenAI but did not log it, add the row to `usage-log.md` first, then cite it.

---

## 7.1 Tools and models used

**Ticket:** `docs: GenAI tools and purposes` · **Points:** 1

**What to write**
List every GenAI tool and model the team used (including chat tools used for small tasks)
and, for each, what it was used for and by whom. Use the exact tool/model names from the
usage log. Say briefly why that tool was chosen for that purpose.

**Where to find the information**
- `docs/genai/usage-log.md` → "Tool/Model", "Person", "Purpose" columns
- `CONTRIBUTING.md` §6 "Using GenAI tools" · `CLAUDE.md` (project context file given to the AI)

**Format**
Table: `Tool / model | Used by | Purpose(s) | Why this tool`. One row per tool/model.

Built from `docs/genai/usage-log.md` on `main` as of 2026-11-15 (35 rows, 4 distinct tool/model values, 4 people). Names are exactly as written in the log.

| Tool / model | Used by | Purpose(s) | Why this tool |
|--------------|---------|------------|---------------|
| Claude Code (Claude Opus 5.5) | Patriciu Mina | Project foundations, backend skeleton, data model, authentication and organization permissions, frontend scaffold, Docker and CI, mid-term documentation drafts, ticket planning and the script that creates the issues and board, review and merge rules | Works directly in the repository and is given the project rules in `CLAUDE.md` (for example, session `P10-team-rules.txt` starts by reading it) |
| Claude Code (Claude Opus 5.5) | Andrei Gota | Onboarding (read-only), W1 and W2 backend endpoints (my memberships, organization members list, add and remove member, create organization, create event) and the Admin page for creating an organization | Same workflow as above; the reason for choosing it is not recorded in the log (to be added by Andrei Gota) |
| Claude Code (Claude Sonnet 5.5) | Vlad-Ștefan Uleia | W4 and W5 backend endpoints (participant list, register with capacity limit, my registrations, announcements, organizer tasks) and sections of the mid-term requirements document | Same Claude Code workflow in the repository; the reason for choosing this model is not recorded in the log (to be added by Vlad-Ștefan Uleia) |
| Codex (GPT-6) | Portik Márk-Krisztián | Frontend features (Organizing menu by role, members list and add/remove members, create and edit event form), backend event editing with optimistic locking and the event details endpoint | The reason for choosing it is not recorded in the log (to be added by Portik Márk-Krisztián) |
| Claude Opus 5.5 (claude.ai chat) | Patriciu Mina | One small task: a summary of the course project statement and mid-term deliverables (`docs/course/course-requirements.md`) | A chat tool used for a one-off summary of a document; the reason is not recorded in the log |

**Done when**
- [ ] Every distinct Tool/Model value in `usage-log.md` appears.
- [ ] Every team member who used GenAI is listed.

**Open items for the team**
- The log has entries from four people. If the fifth team member used GenAI, their uses must first be added to `docs/genai/usage-log.md` (CONTRIBUTING §6) and then listed here; if they did not, say so in this section.
- The "Why this tool" cell is filled only where the repository shows a reason. The people named above should add their own reasons.
- Update this table before submission: new log rows may add people, tools or purposes.

---

## 7.2 Artifacts generated or modified with GenAI

**Ticket:** `docs: GenAI artifacts list` · **Points:** 2

**What to write**
Which files or parts of the system were generated or changed with GenAI, and how much of
each was AI output vs. human work. Group by area (docs, backend, database, frontend,
infrastructure). Be honest: "fully generated, reviewed" and "human-written, AI suggested
one function" are both fine, but must be distinguished.

**Where to find the information**
- `docs/genai/usage-log.md` → "What was generated" column
- `git log --stat` / PR descriptions for the exact files

**Format**
Table: `Area | Artifact (file/package) | Generated / modified / assisted | Usage-log row (date + purpose) | Session file`. 8–20 rows.

Built from the "What was generated" and "Human changes" columns of `docs/genai/usage-log.md` on `main` as of 2026-11-15 (35 rows). In every row below the AI produced the artifact. The log records a person's review for all rows except T-02 (Portik Márk-Krisztián), whose "Human changes" cell still says "Pending human review"; where the log records a human change to the result, it is listed in section 7.4. "Generated" means the AI wrote it from the person's prompt; "modified" means the AI changed an existing file. The log does not say how many lines a person edited by hand, so this table does not claim a percentage. Only the sessions of the first rows were saved in `docs/genai/sessions/`; later work has no saved transcript.

| Area | Artifact | Generated / modified / assisted | Usage-log row (date, purpose) | Session |
|------|----------|---------------------------------|-------------------------------|---------|
| Project setup | `CLAUDE.md`, `.gitignore`, `docs/architecture/ADR-001` to `ADR-005`, first `docker-compose.yml` (PostgreSQL), `.env.example`, `docs/genai/usage-log.md` | Generated after the plan was approved by a person | 2026-11-06, project setup / foundations | `P1-foundations.txt` |
| Project setup | `CLAUDE.md`: `mapper` sub-package and the test naming rule | Modified | 2026-11-06, update project conventions in `CLAUDE.md` | `P4-auth-organizations.txt` |
| Backend | Spring Boot skeleton: `pom.xml`, Maven wrapper, `EventsApplication`, `common` (error body, exceptions, global handler, health), `config` (security, OpenAPI), `application*.yml`, `ApplicationSmokeTest` | Generated | 2026-11-06, backend skeleton | `P2-backend.txt` |
| Database | `V1__initial_schema.sql`, entities, enums and repositories of all features, dev seed `R__dev_seed.sql`, `DataModelConstraintsTest`, `docs/data-model.md` | Generated after a person answered design decisions D1 to D12 | 2026-11-06, data model | `P3-data-model.txt` |
| Backend | JWT authentication, `OrgPermissionService`, organizations endpoints (the worked example), their tests, `ADR-003` implementation notes, `docs/dev-guide/how-to-add-a-feature.md` | Generated after a person answered the authentication decisions | 2026-11-06, JWT auth, org permissions, example feature, Swagger auth, dev guide | `P4-auth-organizations.txt` |
| Frontend | `frontend/` scaffold: API client, token storage, login and register, layout and routes, organizations pages, placeholders, tests, `docs/dev-guide/frontend-guide.md` | Generated after a person answered decisions D1 to D4 | 2026-11-07, frontend scaffold | `P5-frontend.txt` |
| Infrastructure | Dockerfiles, nginx config, `docker-compose.yml` with backend and frontend, `.github/workflows/ci.yml`, `README.md`, `CONTRIBUTING.md`, issue and pull request templates | Generated after a person answered decisions D1 to D7 | 2026-11-07, Dockerized full stack, CI, README, CONTRIBUTING, templates | `P6-docker-ci.txt` |
| Documentation | `docs/midterm/` drafts (`README.md`, `01` to `08`, interview notes template): instructions, structure and the interview guide, no content | Generated | 2026-11-07, mid-term documentation drafts | `P7-midterm-docs.txt` |
| Documentation | `docs/course/course-requirements.md` (summary of the course statement) | Generated (summary, not verbatim) | 2026-11-07, course requirements summary | none (claude.ai chat) |
| Planning | `docs/planning/midterm-tickets.md`: 99 tickets with contracts, shared conventions and the "Deferred to final" list | Generated after a person answered the open decisions | 2026-11-07, mid-term ticket planning | `P8-ticket-plan.txt` |
| Infrastructure | `scripts/create-issues.sh` (creates the labels, milestone, issues and board), `docs/planning/README.md` | Generated after a person answered the open decisions | 2026-11-07, script to create the mid-term issues and board | `P9-board-issues.txt` |
| Infrastructure | `scripts/fix-good-first-labels.sh`, the `good-first-ticket` rule in the parser and in `midterm-tickets.md`, `CONTRIBUTING.md` §1 | Generated and modified, for a problem a person found | 2026-11-07, fix the good-first-ticket rule | `P9-board-issues.txt`, `P10-team-rules.txt` |
| Documentation | `CONTRIBUTING.md` §2 (review and merge rules), `CLAUDE.md` Git rule, checklist line in the feature guide | Modified | 2026-11-07, review and merge rules | `P10-team-rules.txt` |
| Backend (W1) | `MembershipService`, `MyMembershipsController`, organization members list, add and remove member, create organization, with their tests | Generated | 2026-11-10 to 2026-11-11, T-01, T-03, T-04, T-05, T-08 (Andrei Gota) | none saved |
| Backend (W2) | Create event (`EventRequest`, date validator, `EventService.create`), edit event with optimistic locking, event details with free places, with their tests | Generated | 2026-11-11, T-10 (Andrei Gota); 2026-11-14, T-11 and T-12 (Portik Márk-Krisztián) | none saved |
| Frontend | Admin page to create an organization; Organizing menu by role; members list with add and remove; create and edit event form, with their tests | Generated | 2026-11-11, T-09 (Andrei Gota); 2026-11-12 and 2026-11-14, T-02, T-06, T-07, T-13 (Portik Márk-Krisztián) | none saved |
| Backend (W4) | Participant list, register with capacity limit and row lock, "my registrations", with their tests | Generated | 2026-11-15, T-28, T-21, T-23 (Vlad-Ștefan Uleia) | none saved |
| Backend (W5) | Announcements (list and post) and organizer tasks (list and create), with their tests | Generated | 2026-11-15, T-30, T-26 (Vlad-Ștefan Uleia) | none saved |
| Documentation | Mid-term requirements sections `FR-AUTH` and `FR-ADM` (2.2), `FR-W1` (2.3) and `FR-W4` (2.6) in `docs/midterm/02-requirements.md` | Generated (tables), checked against the code by a person | 2026-11-15, T-67, T-68 and T-71 (Vlad-Ștefan Uleia) | none saved |
| (no files) | Onboarding summary shown in the chat; nothing in the repository changed except the log row | Assisted | 2026-11-10, onboarding, read-only (Andrei Gota) | none saved |

**Done when**
- [ ] Every usage-log row is represented by at least one artifact.
- [ ] Each artifact links to its usage-log row and, where it exists, a session file.

**Open items for the team**
- The log row for T-02 (Portik Márk-Krisztián) still says "Pending human review"; the person concerned should complete it.
- The table must be extended when new rows are added to the log (the requirements sections and report sections written after 2026-11-15 are not in it).
- If the course expects a share of AI output versus human work for each artifact, the people concerned have to add it: the log records generation, review and changes, not line counts.
- Save session transcripts of the later work in `docs/genai/sessions/` if they should be cited as evidence.

---

## 7.3 How we verified AI-generated code and documentation

**Ticket:** `docs: GenAI verification process` · **Points:** 2

**What to write**
Describe the general verification process (planning first and answering open decisions,
reviewing diffs, running tests and CI, manual checks in Swagger/UI/psql, PR review by a
teammate), then give 3–5 concrete examples with the actual check and its result. Mention
cases where verification found a problem.

**Where to find the information**
- `docs/genai/usage-log.md` → "How it was verified" column
- `.github/workflows/ci.yml`, `CONTRIBUTING.md` §4 Definition of Done and §6
- `docs/genai/sessions/*` (look for test runs and corrections)

**Format**
Short prose (100–200 words) for the process, then a table:
`Artifact | Verification done | Result | Evidence`.

**The process.** In the saved sessions, work starts in plan mode: the AI proposes a plan and lists the open decisions, and a person answers them before any code is written (the decisions are recorded in the usage log and in the sessions). After the AI has written the change, the person reads the diff, then checks it in two ways. Automated: backend `./mvnw verify` (unit tests with Mockito and integration tests against PostgreSQL 16 in Testcontainers) and frontend `npm run lint`, `npm test` and `npm run build`; the same checks run in CI on every pull request (`.github/workflows/ci.yml`). Manual: the person calls the endpoints in Swagger UI or with curl, uses the application in the browser, and looks at the database with psql or runs `docker compose up --build`. Features are meant to be looked at by a teammate before merging (`CONTRIBUTING.md` §2 and §4), and the person records what they checked and changed in the "How it was verified" and "Human changes" columns of `docs/genai/usage-log.md`.

| Artifact | Verification done | Result | Evidence |
|----------|-------------------|--------|----------|
| Data model (V1 migration and dev seed), Patriciu Mina | Design decisions D1 to D12 answered before code; `./mvnw verify`; applied the migration and the seed to PostgreSQL and counted rows in psql; ran the seed twice | 3 smoke and 13 constraint tests passed (16); psql showed 8 users, 2 organizations, 7 events and 14 registrations; the second run changed no row counts, so the seed is idempotent | Usage log 2026-11-06, data model; `P3-data-model.txt` |
| Dockerized stack (Dockerfiles, Compose, CI), Patriciu Mina | `docker compose up --build --wait`; login, organizations, health and Swagger checked through ports 3000 and 8080; `down` and `up` again | **Found a problem:** the first image build had a corrupted dependency jar (checksum different from Maven Central), which failed at start-up with `ClassFormatError`. Fixed by rebuilding without cache and adding `--strict-checksums` to the Dockerfile; afterwards all three services were healthy | Usage log 2026-11-07, Dockerized full stack; `P6-docker-ci.txt` |
| "My memberships" endpoint (T-01), Andrei Gota | `./mvnw verify` (53 tests, 6 new); calls with seed accounts (a president, an organizer, a user with none, no token); the path checked in `/v3/api-docs`; then the person logged in as the president in the browser | Passed; the browser showed the same membership as the API | Usage log 2026-11-10, T-01 |
| Event create and edit form (T-13), Portik Márk-Krisztián | Backend `./mvnw verify` (149 tests); frontend lint, 33 tests and build; rebuilt Docker Compose; browser flow as an organizer: empty form, create, edit (the log says this full verification and browser flow were performed by Codex, and the person approved the plan and the merge) | Passed: required-field errors shown, create returned 201 with status DRAFT, local time sent as UTC, edit returned 200 and the version went from 0 to 1 | Usage log 2026-11-14, T-13 |
| "My registrations" endpoint (T-23), Vlad-Ștefan Uleia | `./mvnw verify`; the endpoint called in Swagger with a seed student and with a user without registrations | **Found a problem:** one new integration test failed because Java time values carry nanoseconds while PostgreSQL stores microseconds. The cause was in the test, which was fixed by truncating to seconds like the existing event tests; afterwards all 214 tests passed and the endpoint returned the 5 seed registrations and an empty list | Usage log 2026-11-15, T-23 |

**Done when**
- [ ] The process covers automated (tests/CI) and manual checks.
- [ ] At least 3 concrete examples with evidence links.

**Open items for the team**
- Only the first rows have a saved session transcript; the others are evidenced by their usage-log row alone. Save the sessions or screenshots for the later examples under `docs/genai/sessions/` or `docs/genai/evidence/` if they should be cited as proof.
- The "looked at by a teammate" step is a team rule; the usage log does not record who reviewed each pull request. Add the pull request numbers if the course expects that evidence.

---

## 7.4 Human decisions that materially changed the AI output

**Ticket:** `docs: GenAI human decisions` · **Points:** 2

**What to write**
The most important part for R14. Give 3–6 examples where a human decision changed,
corrected or rejected what the AI proposed or would have done: what the AI proposed,
what the human decided, why, and what changed in the result. Only cases with a material
effect (design, behaviour, security, scope), not wording fixes.

**Where to find the information**
- `docs/genai/usage-log.md` → "Human changes" column
- `docs/genai/sessions/*` (open-decision tables and the human's answers, e.g. D1–D12 in P3, auth decisions in P4)
- `docs/data-model.md` → "Design decisions" · `docs/architecture/ADR-003…` → "Implementation notes"

**Format**
Table: `# | AI proposal / default | Human decision | Reason | Effect on the result | Evidence`. 3–6 rows.

> **Example – replace:**
>
> | # | AI proposal / default | Human decision | Reason | Effect | Evidence |
> |---|-----------------------|----------------|--------|--------|----------|
> | 1 | <what the AI proposed> | Events with registrations are never hard-deleted; cancel instead | Keep participation history | `fk_registrations_event` set to RESTRICT | usage-log 2026-11-06 Data model, `P3-data-model.txt` |

**Done when**
- [ ] Each example states the AI proposal and the human decision separately.
- [ ] Each example has evidence (usage-log row + session file or PR).
- [ ] Examples come from more than one team member, if possible.

> TODO (team): human decisions table here.
