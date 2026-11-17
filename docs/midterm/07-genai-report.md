# 7. GenAI declaration

Part **D** of the mid-term deliverable. Index: [README](README.md).

Course link: R13 (GenAI-assisted development) and R14 (human oversight), see
`docs/course/course-requirements.md`.

---

## 7.1 Tools and models used

Built from `docs/genai/usage-log.md` on `main` as of 2026-11-17 (93 rows, 8 distinct tool/model values, all 5 team members). Names are exactly as written in the log; rows for the same tool used by different people are kept separate.

| Tool / model | Used by | Purpose(s) | Why this tool |
|--------------|---------|------------|---------------|
| Claude Code (Claude Opus 5.5) | Patriciu Mina | Project foundations, backend skeleton, data model, authentication and organization permissions, frontend scaffold, Docker and CI, mid-term documentation drafts, ticket planning and the issue/board script, review and merge rules; later the status analysis and finish plan, the state machine review (T-49), the registration rules test (T-36), workflows W3–W5 (T-85–T-87), the status update of the requirements (T-100) and this update of the report (T-101) | Works directly in the repository and is given the project rules in `CLAUDE.md` (for example, session `P10-team-rules.txt` starts by reading it) |
| Claude Code (Claude Opus 5.5) | Andrei Gota | Onboarding (read-only), W1 and W2 backend endpoints (my memberships, members list, add and remove member, create organization, create event), the Admin page and the Register / Cancel button (T-24) | Same workflow as above; the reason for choosing it is not recorded in the log (to be added by Andrei Gota) |
| Claude Code (Claude Sonnet 5.5) | Vlad-Ștefan Uleia | W4 and W5 backend (participant list, register with capacity limit, my registrations, announcements, tasks, task changes, check-in), the concurrency test, the event details page and its announcements, and most mid-term document sections (problem statement, scope, requirements, actors, sequence diagrams, ER review, sections 7.1–7.4 of this report) | Same Claude Code workflow in the repository; the reason for choosing this model is not recorded in the log (to be added by Vlad-Ștefan Uleia) |
| GitHub Copilot | Vlad-Ștefan Uleia (logged as "Vlad") | Review of the interview guide | The reason is not recorded in the log |
| Codex (GPT-6) | Portik Márk-Krisztián | Most W2/W3 features and W5 screens: Organizing menu, members UI, edit event, status transitions, event details, search, an organization's events, event form, events list, organizer pages, tasks / participants / announcements panels; transition matrix and permission matrix tests; the three sets of wireframes | The reason for choosing it is not recorded in the log (to be added by Portik Márk-Krisztián) |
| GitHub Copilot (model not specified) | Jakab Gergő | Actors section, use case list, AUTH/ADM use cases, workflows W1 and W2, friendly error messages in the frontend (T-45) | Used inside VS Code (log row "Actors and roles"); the reason is not otherwise recorded |
| GitHub Copilot (MAI-Code-1.1-Flash) | Jakab Gergő | Cleaning the use case file and writing the W1, W4 and W5 use cases | Same tool with an explicitly chosen model; the reason is not recorded |
| ChatGPT (GPT-6) | Jakab Gergő | Colour palette with WCAG contrast check (T-40), logo (T-39), use case overview diagram | A chat tool for design proposals outside the repository; the reason is not recorded |
| Claude Opus 5.5 (claude.ai chat) | Patriciu Mina | One small task: a summary of the course project statement (`docs/course/course-requirements.md`) | A chat tool used for a one-off summary of a document |

---

## 7.2 Artifacts generated or modified with GenAI

Built from the "What was generated" and "Human changes" columns of `docs/genai/usage-log.md` on `main`: the first 35 rows as of 2026-11-15, extended on 2026-11-17 to all 93 rows (last block of the table). In every row below the AI produced the artifact. The log records a person's review for all rows except T-02 (Portik Márk-Krisztián), whose "Human changes" cell still says "Pending human review"; where the log records a human change to the result, it is listed in section 7.4. "Generated" means the AI wrote it from the person's prompt; "modified" means the AI changed an existing file. The log does not say how many lines a person edited by hand, so this table does not claim a percentage. Only the sessions of the first rows were saved in `docs/genai/sessions/`; later work has no saved transcript.

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
| Documentation | Mid-term sections 1.1–1.4 (problem statement, stakeholders, scope, out of scope), 2.1 (R1–R16 map), 2.4, 2.5, 2.7, 2.8 (FR-W2, FR-W3, FR-W5, NFR), 3.1 (actors), sections 7.1–7.4 of this report | Generated, checked against the code and transcripts; for 1.1 the person supplied the facts | 2026-11-15, T-62–T-66, T-69, T-70, T-72–T-74, T-90–T-93 (Vlad-Ștefan Uleia) | none saved |
| Documentation | Interview guide review (questions tagged and mapped to assumptions A-01–A-13) | Assisted | 2026-11-14, interview guide review (Vlad-Ștefan Uleia, GitHub Copilot) | none saved |
| Documentation | W2 and W4 sequence diagrams; ER diagram compared with the migration and entities (no difference found) | Generated / checked by a script | 2026-11-15, T-47, T-48, T-50 (Vlad-Ștefan Uleia) | none saved |
| Backend (W4, W5) | Change a task's status and assignee, check-in endpoint, concurrency test of the capacity limit | Generated | 2026-11-15/16, T-27, T-29, T-35 (Vlad-Ștefan Uleia) | none saved |
| Frontend (W3, W5) | Event details page, announcements on the event details page | Generated | 2026-11-15, T-20, T-34 (Vlad-Ștefan Uleia) | none saved |
| Backend (W2, W3) | Event status transitions (`EventLifecycle`, `POST /events/{id}/status`), paged event search, an organization's events | Generated after the person approved the plan | 2026-11-16, T-12, T-16, T-17 (Portik Márk-Krisztián) | none saved |
| Frontend (W3, W5) | Events list with filters, Organizing page, organizer event page with status actions, tasks / participants and check-in / announcements panels | Generated after the person approved the plan | 2026-11-16, T-19, T-14, T-15, T-31, T-32, T-33 (Portik Márk-Krisztián) | none saved |
| Tests | Event status transition matrix, permission matrix integration tests | Generated | 2026-11-16, T-37, T-38 (Portik Márk-Krisztián) | none saved |
| Design | Student, organizer, organization and admin wireframes (SVG + `docs/design/wireframes.md`) | Generated | 2026-11-16, T-41, T-42, T-43 (Portik Márk-Krisztián) | none saved |
| Design | Colour palette (`theme.ts`, `docs/design/palette.md`) and logo (`docs/design/logo.md`, SVG) | Assisted: proposals from a chat tool, applied and adapted by the person | 2026-11-16, T-40, T-39 (Jakab Gergő, ChatGPT) | none saved |
| Documentation | Actors (3.1), use case list (4.2), use case diagram (4.3), all use cases (4.4–4.8), workflows W1 and W2 (5.1, 5.2) | Generated, reviewed by the person | 2026-11-16/17 (Jakab Gergő, GitHub Copilot and ChatGPT) | none saved |
| Frontend | Friendly messages for backend error codes (`api/errorMessages.ts`) | Generated | 2026-11-17, T-45 (Jakab Gergő) | none saved |
| Frontend (W4) | Register / Cancel button on the event details page | Generated | 2026-11-17, T-24 (Andrei Gota) | none saved |
| Planning, documentation, tests | Record of how the mid-term was finished (`docs/planning/midterm-finish-plan.md`), state machine review (5.2, `data-model.md`), `RegistrationRulesTest`, workflows W3–W5 (5.3–5.5), status update of 02–04, this update of section 7 | Generated, checked against the code; human decisions in 7.4 | 2026-11-17, plan, T-49, T-36, T-85, T-86, T-87, T-100, T-101 (Patriciu Mina) | none saved |

---

## 7.3 How we verified AI-generated code and documentation

**The process.** In the saved sessions, work starts in plan mode: the AI proposes a plan and lists the open decisions, and a person answers them before any code is written (the decisions are recorded in the usage log and in the sessions). After the AI has written the change, the person reads the diff, then checks it in two ways. Automated: backend `./mvnw verify` (unit tests with Mockito and integration tests against PostgreSQL 16 in Testcontainers) and frontend `npm run lint`, `npm test` and `npm run build`; the same checks run in CI on every pull request (`.github/workflows/ci.yml`). Manual: the person calls the endpoints in Swagger UI or with curl, uses the application in the browser, and looks at the database with psql or runs `docker compose up --build`. Features are meant to be looked at by a teammate before merging (`CONTRIBUTING.md` §2 and §4), and the person records what they checked and changed in the "How it was verified" and "Human changes" columns of `docs/genai/usage-log.md`.

| Artifact | Verification done | Result | Evidence |
|----------|-------------------|--------|----------|
| Data model (V1 migration and dev seed), Patriciu Mina | Design decisions D1 to D12 answered before code; `./mvnw verify`; applied the migration and the seed to PostgreSQL and counted rows in psql; ran the seed twice | 3 smoke and 13 constraint tests passed (16); psql showed 8 users, 2 organizations, 7 events and 14 registrations; the second run changed no row counts, so the seed is idempotent | Usage log 2026-11-06, data model; `P3-data-model.txt` |
| Dockerized stack (Dockerfiles, Compose, CI), Patriciu Mina | `docker compose up --build --wait`; login, organizations, health and Swagger checked through ports 3000 and 8080; `down` and `up` again | **Found a problem:** the first image build had a corrupted dependency jar (checksum different from Maven Central), which failed at start-up with `ClassFormatError`. Fixed by rebuilding without cache and adding `--strict-checksums` to the Dockerfile; afterwards all three services were healthy | Usage log 2026-11-07, Dockerized full stack; `P6-docker-ci.txt` |
| "My memberships" endpoint (T-01), Andrei Gota | `./mvnw verify` (53 tests, 6 new); calls with seed accounts (a president, an organizer, a user with none, no token); the path checked in `/v3/api-docs`; then the person logged in as the president in the browser | Passed; the browser showed the same membership as the API | Usage log 2026-11-10, T-01 |
| Event create and edit form (T-13), Portik Márk-Krisztián | Backend `./mvnw verify` (149 tests); frontend lint, 33 tests and build; rebuilt Docker Compose; browser flow as an organizer: empty form, create, edit (the log says this full verification and browser flow were performed by Codex, and the person approved the plan and the merge) | Passed: required-field errors shown, create returned 201 with status DRAFT, local time sent as UTC, edit returned 200 and the version went from 0 to 1 | Usage log 2026-11-14, T-13 |
| Capacity limit under concurrent registrations (T-35), Vlad-Ștefan Uleia | Ran the concurrency test 5 times; then removed the row lock on purpose to see whether the test notices | The test passed 5 of 5 times; without the lock both scenarios failed with overbooking (10 of 10 students registered for 1 place), so the test really guards the rule; the lock was restored | Usage log 2026-11-15, T-35 |
| Organizer event page with status actions (T-15), Portik Márk-Krisztián | Lint, 63 tests, build; manual browser check as an organizer on `/organizing/events/11` | **Found a problem:** after the manual check the person asked for the error layout of the status actions to be changed, then checked and approved the corrected layout; a test case for the error placement was added | Usage log 2026-11-16, T-15 |
| ER diagram review (T-50), Vlad-Ștefan Uleia | A script compared the migration, the diagram and the entities (7 tables, 51 columns, 10 foreign keys) | **Found a problem in the check itself:** the first run reported 22 differences that came from the script's own parsing of `VARCHAR(n)` and primary keys; after fixing the parser there was no difference | Usage log 2026-11-15, T-50 |
| Workflow W1 (5.1), Jakab Gergő | Endpoints and roles checked against the code; GitHub preview of the Mermaid diagram | **Found a problem:** GitHub could not render the diagram (semicolons in labels); the person reported it and the labels were fixed | Usage log 2026-11-17, W1 workflow |
| Registration rules test (T-36) and workflow W3 (5.3), Patriciu Mina | Run by Claude Code and reported in the PRs: `./mvnw verify` (411 tests); the deadline rule removed on purpose; every statement of 5.3 compared with the code | The removed rule made 2 of the new tests fail, so they guard it. **Found a problem:** the first draft of 5.3 named the wrong error code for an unknown category (`VALIDATION_FAILED` instead of `INVALID_PARAMETER`) and incomplete R codes; both were corrected before the PR | Usage log 2026-11-17, T-36, T-85; PR #186, #187 |
| "My registrations" endpoint (T-23), Vlad-Ștefan Uleia | `./mvnw verify`; the endpoint called in Swagger with a seed student and with a user without registrations | **Found a problem:** one new integration test failed because Java time values carry nanoseconds while PostgreSQL stores microseconds. The cause was in the test, which was fixed by truncating to seconds like the existing event tests; afterwards all 214 tests passed and the endpoint returned the 5 seed registrations and an empty list | Usage log 2026-11-15, T-23 |

---

## 7.4 Human decisions that materially changed the AI output

Examples 1–5 come from the saved sessions and the "Human changes" column of `docs/genai/usage-log.md`; the quoted proposals are taken from the transcripts. Examples 6 and 7 were added on 2026-11-17 from the log rows of other team members.

| # | AI proposal / default | Human decision | Reason | Effect on the result | Evidence |
|---|-----------------------|----------------|--------|----------------------|----------|
| 1 | Deleting an event cascades to its registrations, tasks and announcements. | Events that have registrations are never hard-deleted; they are cancelled with a status instead. | Keep the participation history. | The foreign key from registrations to events is `RESTRICT` (`fk_registrations_event`) and the lifecycle uses `CANCELLED`; a DRAFT without registrations can still be deleted. Recorded as decision D4 in `docs/data-model.md`. | Usage log 2026-11-06, data model; `P3-data-model.txt` (D4) |
| 2 | Plan with 4 `no-genai` tickets and one manual-test ticket per workflow, about 95 tickets and 190 points, with the full workflow scope for the mid-term. | Exactly 5 `no-genai` tickets, one per team member, each a feature with real business logic its author can explain; the scope was cut: changing roles, leaving and editing an organization, editing and deleting tasks and announcements, per-workflow QA and three of the sequence diagrams moved to "Deferred to final". About 99 small tickets were kept after the count was shown. | Give every member one piece of work to write and explain alone, and keep the mid-term scope small. | `docs/planning/midterm-tickets.md` has the `no-genai` tickets T-06, T-17, T-22, T-25 and T-29 and a "Deferred to final" list; this is why those tickets must be written without GenAI. | Usage log 2026-11-07, mid-term ticket planning; `P8-ticket-plan.txt` (D12) |
| 3 | Label `good-first-ticket` on the 1 to 2 point tickets. | The person found that about 75% of them were blocked by other tickets, and set a new rule: 1 to 2 points **and** no dependencies. | A ticket that waits for another is not one a newcomer can start right away. | The label was removed from 57 tickets and kept on 29 in the plan, in the creation script and on the existing issues. | Usage log 2026-11-07, fix the good-first-ticket rule; `P9-board-issues.txt` ("Problem found by the human") |
| 4 | A long combined shell command that copied and read `.env` and used `rm`, to write the Docker files. | The person denied it and instructed: write each file with the normal file edit tool, never read, print, copy or back up `.env`, and do not delete anything the task did not create. | `.env` holds the real secrets, and a command that reads it or deletes files is not something to run unreviewed. | Claude wrote the files with the file tools and said it would not touch `.env`; the secrets stay in the git-ignored `.env`. | Usage log 2026-11-07, Dockerized full stack, CI, README, CONTRIBUTING, templates; `P6-docker-ci.txt` |
| 5 | Thirteen pre-filled assumptions to validate in the interviews, including one about a 2-hour login without refresh. | Removed the 2-hour login assumption and added three about users: the interface is English only, only organizers and presidents create events, and students find events mainly through social media and word of mouth. | The login lifetime is too technical to ask users, while the added assumptions are about real user behaviour. | The assumptions list for the interviews in `06-ux-research.md` changed; interview results must come from real interviews. | Usage log 2026-11-07, mid-term documentation drafts; `P7-midterm-docs.txt` |
| 6 | Organizer event page (T-15): the generated layout of the status actions and their error message. | After checking the page in the browser, the person asked for the error layout to be changed, then checked and approved the corrected layout. | The person was not satisfied with how the error appeared in the browser. | The layout was changed and a test case for the status action row and error placement was added before merge. | Usage log 2026-11-16, T-15 (Portik Márk-Krisztián) |
| 7 | Problem statement (1.1): the AI first wrote the students' problems as assumptions, because the person's first answer was not a confirmation. | The person supplied the facts (answers to five questions and what they had learned from a conversation with someone); the text was rewritten to cite "a conversation with one person" as the source and to keep only what that supports. | No invented facts in the report; the source of every claim must be named. | Section 1.1 cites its source instead of presenting assumptions as facts. | Usage log 2026-11-15, T-62 (Vlad-Ștefan Uleia) |

---

## 7.5 How the `no-genai` tickets were done

The plan (`docs/planning/midterm-tickets.md`, decision D12 in `P8-ticket-plan.txt`) gave each
member one ticket to write **without** GenAI, so that each person can explain one feature
alone (NFR-12). The table is taken from the usage log and the pull requests on 2026-11-17.

| Ticket | Owner | What the log / PR shows |
|--------|-------|-------------------------|
| T-06 members list | Portik Márk-Krisztián | Done with Codex (GPT-6), log row 2026-11-14 "T-06" |
| T-17 an organization's events | Portik Márk-Krisztián | Done with Codex (GPT-6), log row 2026-11-16 "T-17 (#25)" |
| T-22 cancel my registration (API) | Andrei Gota | No log row; PR #179 leaves "GenAI used?" unticked |
| T-25 "My registrations" page | Patriciu Mina | The owner removed the `no-genai` label (comment on issue #33, 2026-11-17) and it was done with Claude Code (Claude Opus 5.5), log row 2026-11-17 "T-25 (#33)" |
| T-29 check-in endpoint | Vlad-Ștefan Uleia | Done with Claude Code (Claude Sonnet 5.5); the log row itself notes the `no-genai` label |

So four of the five `no-genai` tickets were done with GenAI and declared in the log; only T-22
was done without it. For T-25 the label was removed before the work started.
The team has to decide how to handle this before submission (for example, each person
explains the code in their individual statement, section 8, or another ticket becomes their
`no-genai` work).

