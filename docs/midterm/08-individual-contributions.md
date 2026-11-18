# 8. Individual contribution declaration

Part **E** of the mid-term deliverable. Index: [README](README.md).

Course link: R16 ("each student owns specific artifacts and at least one cross-layer
workflow"), see `docs/course/course-requirements.md`.

**What to write (each student fills only their own section)**
Fill every field with concrete, checkable items: file paths, PR numbers, issue numbers,
test class names. Write "none yet" rather than leaving a field empty. Do not claim work that
another teammate did; for shared work, say what *your* part was.

**Where to find the information**
- GitHub: your merged PRs and closed issues on the project board
  (`gh pr list --repo PatriciuMina/GenAI-Project --author <your-login> --state merged`)
- `git log --author="<your name>" --stat`
- `docs/genai/usage-log.md` (rows with your name) and `docs/genai/sessions/`
- `docs/architecture/ADR-*`, `docs/data-model.md` → design decisions (which ones you proposed)
- `05-workflows.md` (workflow owners)

**Format**
The table in each section; each cell a short list. Total per student ≈ ½ page.

> **Example – replace** (format only, one row):
>
> | Field | Contribution |
> |-------|--------------|
> | Tests written | `OrganizationServiceTest` (3 tests), PR #4 |

**Done when** (per student)
- [ ] Every field is filled (or "none yet").
- [ ] Every item has a reference (file, PR, issue or usage-log row).
- [ ] The owned workflow is cross-layer (UI + API + DB) and matches `05-workflows.md`.
- [ ] Another teammate reviewed the section in the PR.

---

## Mina Patriciu

**Ticket:** `docs: individual contribution – Mina Patriciu` · **Points:** 1

GitHub: `PatriciuMina`. Merged PRs: #1–#8, #108, #109, #184–#189, #191, #193–#196, #198,
#202–#205, #210 (`gh pr list --repo PatriciuMina/GenAI-Project --author PatriciuMina --state merged`).
Author of all 99 mid-term issues (#9–#107) and of the project board.

**Summary.** I set up the project from an empty repository: architecture, data model,
authentication, the first backend and frontend feature, Docker, CI and the team process. I
then wrote the guides with worked examples, the documentation skeleton and the 99 tickets
the team used to build the mid-term version. In the last phase I finished the remaining
non-interview tickets, interviewed three users and wrote the user research, and turned the
documentation into its final form.

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | **Foundation:** `CLAUDE.md` (project rules and AI context), ADR-001 to ADR-005, `docs/genai/usage-log.md`, first `docker-compose.yml` (commit `7baec2e`). **Backend skeleton:** Maven project, `common` (error body, exceptions, global handler, health), Swagger, security config (PR #1). **Data model:** `V1__initial_schema.sql` (7 tables with all constraints), entities, enums and repositories of every feature, dev seed `R__dev_seed.sql` (8 users, 2 organizations, 7 events in every status, registrations, tasks, announcements), `docs/data-model.md` with ER diagram and decisions D1–D12 (PR #3). **Infrastructure:** Dockerfiles, nginx, full-stack `docker-compose.yml`, `.github/workflows/ci.yml`, `README.md` with run instructions and seed accounts (PR #6). **Team process:** `CONTRIBUTING.md`, the PR template and five issue templates (PR #6, #108, #109). **Planning:** `docs/planning/midterm-tickets.md`, `scripts/create-issues.sh`, `scripts/fix-good-first-labels.sh` (PR #8, #108, #109). **User research:** interviews I-01 to I-03 and `06-ux-research.md` §6.1, 6.3–6.7 (PR #198, #202, #210). **QA:** `docs/qa/midterm-manual-test.md`, `scripts/midterm-api-check.py` (PR #196) |
| Owned workflow | **W4 Participant registration – "My registrations"**: page `MyRegistrationsPage` + `registrationSplit.ts` (UI, T-25, PR #194) on `GET /api/v1/users/me/registrations` and `DELETE /api/v1/events/{id}/registrations/me` (API by Vlad-Ștefan Uleia and Andrei Gota) and the `registrations` table with its constraints (DB, PR #3); W4 rules test (T-36, PR #186) and workflow §5.4 (T-86, PR #188). **Cross-layer base of W1:** organizations and memberships tables (PR #3), `OrganizationController`, `OrganizationService`, `OrgPermissionService` (PR #4), `OrganizationsListPage`, `OrganizationDetailsPage` (PR #5); member management on top of it was built by Andrei Gota (API) and Portik Márk-Krisztián (UI) |
| Implemented functionality | Health endpoint, Swagger UI, consistent error body (PR #1); register / login with JWT, `JwtService`, `GET /users/me`, organization permissions (`OrgPermissionService`, `@PreAuthorize`), organizations list and details as the reference feature (PR #4); React app: typed API client, token storage, `AuthContext`, login and register pages, protected and role-based routes, layout and menu, organizations pages (PR #5); Docker full stack with health checks and CI for backend and frontend (PR #6); "My registrations" page with cancel (T-25, PR #194); shared `EmptyState` on all list pages and "Clear filters" on Events (T-44, PR #195) |
| Documentation produced | **Guides with worked examples for the team:** `docs/dev-guide/how-to-add-a-feature.md`, a 10-step walkthrough of a backend feature using the organizations feature (DTO, mapper, service, controller, security, unit test, integration test, DB changes, Swagger with a token, PR checklist) (PR #4); `docs/dev-guide/frontend-guide.md` (PR #5). **Ticket plan:** 99 tickets / 184 points, each with context, what to do, files to touch, acceptance criteria, how to check and the guide to read; shared conventions (layering, error codes, event visibility), labels, `no-genai` tickets and a "Deferred to final" list (PR #8, #108). **Mid-term documentation:** the skeleton of `docs/midterm/` with instructions and examples per section, the interview guide and 13 assumptions (PR #7); workflows W3–W5 (PR #187–#189); state machine check (PR #185); status update of 02–04 (PR #191); GenAI report update and evidence index (PR #193); **user research**: method, findings, assumptions (3 changed, each with a requirement and a ticket for the final delivery: #199, #200, #201), product flows and personas (PR #202, #210); **final version of 01–07**: drafting instructions removed, problem statement rewritten from the interviews (PR #203, #204) |
| Tests written | `ApplicationSmokeTest`, `DataModelConstraintsTest` (PR #1, #3); `AuthControllerIntegrationTest`, `AuthServiceTest`, `OrgPermissionServiceTest`, `OrgPermissionPreAuthorizeIntegrationTest`, `OrganizationControllerIntegrationTest`, `OrganizationServiceTest` and the Testcontainers base class `IntegrationTestBase` used by all integration tests (PR #4); `client.test.ts`, `AuthContext.test.tsx`, `LoginForm.test.tsx` (PR #5); `RegistrationRulesTest` (PR #186); `MyRegistrationsPage.test.tsx`, `registrationSplit.test.ts` (PR #194); `EmptyState.test.tsx`, "Clear filters" tests (PR #195); API run of W1–W5, 57 steps, all passed (PR #196) |
| Architectural / design decisions contributed | Feature packages and layering (ADR-001), REST + OpenAPI (ADR-002), JWT with Spring Security (ADR-003), Flyway (ADR-004), Docker Compose (ADR-005); data model decisions D1–D12, e.g. D1 no waitlist, D4 events with registrations are cancelled and never deleted, D11 optimistic locking on events (`P3-data-model.txt`); frontend decisions D1–D4 (`P5-frontend.txt`); the business error codes and event visibility rules shared by all tickets; the mid-term scope cut and one `no-genai` ticket per member (`P8-ticket-plan.txt`, D12); the `good-first-ticket` rule (`P9-board-issues.txt`); equal review and merge rules (`CONTRIBUTING.md` §2, PR #109); after the interviews: waitlist, calendar/e-mail notifications and "who may attend" moved into the final-delivery scope (`06-ux-research.md` §6.5) |
| GenAI-related work | Done with Claude Code (Claude Opus 5.5), plus one summary with Claude Opus 5.5 in claude.ai. Set up the GenAI process for the team: the usage log, `CLAUDE.md` as the shared AI context, CONTRIBUTING §6 rules; worked in plan mode and answered the open decisions before code was generated; the first ten sessions are saved as evidence in `docs/genai/sessions/` (`P1` … `P10`); every task has a usage-log row. The interviews were conducted and written by me; the AI only put my notes into the template. **`no-genai` ticket:** T-25 was mine; I removed the label before the work started and the page was generated with Claude Code (comment on issue #33, report §7.5), so I have no work written without GenAI |
| Others | Created the GitHub labels, milestone, the 99 issues and the project board with a script (PR #108, #109) and fixed the `good-first-ticket` labels on the existing issues; wrote the team rules for picking, reviewing and merging tickets; mid-term status analysis and record of how it was finished (PR #184, #204); ran the API part of the manual test on a fresh stack (PR #196) |
---

## Andrei Gota

**Ticket:** `docs: individual contribution – Andrei Gota` · **Points:** 1

GitHub: `GotaSeptimiuAndrei`. Merged PRs, all merged by me: #110–#117, #179, #181, #208
(`gh pr list --repo PatriciuMina/GenAI-Project --author GotaSeptimiuAndrei --state merged`).
Issues done: #9 (T-01), #11 (T-03), #12 (T-04), #13 (T-05), #16 (T-08), #17 (T-09), #18 (T-10),
#30 (T-22), #32 (T-24), #62 (T-54), #106 (T-98).

**Summary.** I built most of the W1 membership API (my organizations, the members list, adding
and removing members), the admin flow that creates an organization with its president (API and
admin page), the first service and controller of the `event` package (create a draft event) and
the W4 cancel path: the cancel API, my `no-genai` ticket, and the register / cancel button on the
event page. For the user research I collected the answers of two students (I-05, I-06).

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | **Backend `organization`:** `MembershipService`, `MembershipMapper`, `MyMembershipsController`, the DTOs `MembershipResponse`, `MemberResponse`, `AddMemberRequest`, `CreateOrganizationRequest`, the members, add-member, remove-member and create endpoints in `OrganizationController`, `OrganizationService.create` and `getEntityById` (PR #110–#113, #115). **Backend `event`, first version:** `EventRequest` with `@ValidEventDates` / `EventDatesValidator`, `EventResponse`, `EventMapper`, `EventService.create`, `EventController`, later extended by teammates; `UserService.getEntityById` (PR #114). **Backend `registration`:** `Registration.cancel()`, `RegistrationService.cancelMyRegistration`, `RegistrationController.cancelMine` (PR #179). **Frontend:** `features/admin/AdminPage.tsx`, `CreateOrganizationForm.tsx` (PR #116); `features/events/RegistrationButton.tsx`, `registrationRules.ts`, `features/registrations/queries.ts` and the register / cancel / my-registrations calls in `src/api/registrations.ts` (PR #181). **Research:** `docs/midterm/interviews/I-05.md`, `I-06.md` (PR #208) |
| Owned workflow | **W1 – "an admin creates an organization and its president"**, end to end (`05-workflows.md` §5.1 steps 1–2): the `/admin` page with `CreateOrganizationForm` (UI, T-09, PR #116) → `POST /api/v1/organizations` (API, T-08, PR #115) → the `organizations` row and the `PRESIDENT` row in `organization_memberships`, saved in one transaction (DB). I also built the W1 membership API that the member screens use (§5.1 steps 5, 6, 9: T-01, T-03, T-04, T-05, PR #110–#113); those screens are by Portik Márk-Krisztián. **W4 – "cancel and register again"**, end to end (§5.4 steps 1, 9, 10): `RegistrationButton` on `/events/{id}` (UI, T-24, PR #181) → `DELETE /api/v1/events/{id}/registrations/me` (API, T-22, PR #179) → the registration row set to `CANCELLED` and kept as history (DB). The register API itself is by Vlad-Ștefan Uleia (#127) |
| Implemented functionality | `GET /api/v1/users/me/memberships` (T-01, #110); `GET /api/v1/organizations/{orgId}/members` (T-03, #111); `POST /api/v1/organizations/{orgId}/members` (T-04, #112); `DELETE /api/v1/organizations/{orgId}/members/{membershipId}` (T-05, #113); `POST /api/v1/organizations/{orgId}/events` (T-10, #114); `POST /api/v1/organizations` (T-08, #115); admin page form "Create an organization" with a link to the new organization (T-09, #116); `DELETE /api/v1/events/{id}/registrations/me` (T-22, #179); the registration area on the event details page: Register, "You are registered" with Cancel registration and a confirmation dialog, "full" and "registration closed" (T-24, #181) |
| Documentation produced | Swagger / OpenAPI annotations (`@Operation`, `@ApiResponse` with the error codes) on all endpoints above; interview notes I-05 and I-06, their rows in the interview log and the I-05/I-06 paragraph of the method (`06-ux-research.md` §6.1, §6.3, PR #208); a "How to test it" section with seed accounts in the PR descriptions (#110–#116, #181); my rows in `docs/genai/usage-log.md`; this section (T-98) |
| Tests written | `MembershipServiceTest` (15 tests, PR #110–#113); `MyMembershipsControllerIntegrationTest` (3, #110); `OrganizationMembersControllerIntegrationTest` (7, #111); `OrganizationAddMemberControllerIntegrationTest` (12, #112); `OrganizationRemoveMemberControllerIntegrationTest` (9, #113); `EventRequestValidationTest` (12), and the first `EventServiceTest` (2) and `EventControllerIntegrationTest` (12), later extended by teammates (#114); `OrganizationServiceTest` (+5 for `create`) and `OrganizationCreateControllerIntegrationTest` (10, #115); `CreateOrganizationForm.test.tsx` (7) and `AdminPage.test.tsx` (1, #116); `RegistrationServiceCancelTest` (5) and `RegistrationCancelControllerIntegrationTest` (8, #179, without GenAI); `RegistrationButton.test.tsx` (14), `registrationRules.test.ts` (15) and one more `EventDetailsPage.test.tsx` test (#181) |
| Architectural / design decisions contributed | Choices in my PRs, made in Claude Code sessions and reviewed and merged by me (the reasons are in each PR description): `/users/me/memberships` lives in the `organization` package, so `user` does not depend on `organization` (#110); `@EntityGraph` on the membership queries, so a list needs no extra query per row (#110, #111); an unknown organization gives 404 only to an ADMIN and 403 to everyone else, so organization ids cannot be probed (#111); the role in `AddMemberRequest` is a validated string, so `PRESIDENT` gets a field error on `role` (#112); a membership can only be removed through its own organization, and the president can never be removed (#113); organization and president saved in one transaction, and a race on a unique index mapped to the same 409 as the normal check (#115, also #112); the cross-field date rules as one class-level constraint `@ValidEventDates` that reports on `endAt` / `registrationDeadline` (#114); `EventMapper.toResponse(event, confirmedCount)` gets the count from the caller, so the `event` package does not read registrations (#114); entities of another feature only through `getEntityById` service methods (#114); the event page reloads the event and my registrations also after a failed registration, so an `EVENT_FULL` answer updates it at once (#181). No ADR written by me |
| GenAI-related work | Claude Code (Claude Opus 5.5) for everything except T-22: T-01, T-03, T-04, T-05, T-08, T-09, T-10, T-24, putting the I-05/I-06 answers into the notes template, and this section. Each has a row in `docs/genai/usage-log.md` (rows with my name, 2026-11-10 to 2026-11-18). For every ticket I chose the ticket, Claude Code implemented and checked it (`./mvnw verify` or `npm test`, plus a run against the dev seed data) and I reviewed and merged all my PRs myself. From T-03 on, the usage-log rows, including the verification and human-changes columns, were written by Claude Code at my request from what happened in the session; I wrote the human columns of my onboarding and T-01 rows myself (PR #110, incl. my own check of T-01 in the browser as `ana.popescu`) and updated the T-09 row after the admin page was clicked through in a browser (PR #117). The interview answers were collected by me; the AI only put them into the template and anonymised them. **`no-genai` ticket: T-22** (#30, PR #179), written without GenAI tools: no usage-log row, "GenAI used?" left unticked in the PR |
| Others | Fixed the backend Docker build on Windows checkouts: `backend/Dockerfile` strips CRLF line endings from `mvnw` (PR #110); made a slow frontend test reliable: `CreateOrganizationForm.test.tsx` pastes instead of typing (4.4 s → 0.7 s; it had timed out at 5 s when the whole suite ran, PR #181); the interviews were first opened as I-01/I-02 (PR #207, closed) and renumbered to I-05/I-06 after the ID collision with #198 |

---

## Jakab Gergő

**Ticket:** `docs: individual contribution – Jakab Gergő` · **Points:** 1

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | W2 lifecycle workflow documentation in `docs/midterm/05-workflows.md` §5.2 (PR #182); W1 workflow documentation in §5.1 (PR #180); use cases in `docs/midterm/04-use-cases.md` §§4.2, 4.4–4.8 and the overview diagram (PRs #167–#172, #178); permission matrix in `docs/midterm/03-actors-roles.md` (PR #165); palette and logo artifacts in `docs/design/palette.md`, `docs/design/logo.md`, `docs/assets/logo/`, and `frontend/` (PRs #159, #161). |
| Owned workflow | **W2 Event creation and lifecycle — documentation ownership.** I owned documenting and checking the end-to-end workflow against the code. The flow in `docs/midterm/05-workflows.md` §5.2 (PR #182) traces organizer actions in the UI (`/organizing/events/new`, `/organizing/events/{id}/edit`, and status actions) through the create/update/status API requests to event rows in the database, including the lifecycle rules. |
| Implemented functionality | Friendly business-error messages in `frontend/src/api/errorMessages.ts`, applied through `frontend/src/api/errors.ts` to forms and alerts; unknown error codes retain the backend message (PR #183). |
| Documentation produced | `docs/midterm/03-actors-roles.md` permission matrix (#165); `docs/midterm/04-use-cases.md` use-case list, AUTH/ADM and W1–W5 cases, and overview diagram (#167–#172, #178); `docs/midterm/05-workflows.md` W1 and W2 (#180, #182); palette and logo guidance (`docs/design/palette.md`, `docs/design/logo.md`, #159, #161). |
| Tests written | `frontend/src/api/errorMessages.test.ts` (16 business-code mappings and unknown-code fallback); updated friendly-message expectations in `CreateOrganizationForm.test.tsx`, `RegistrationButton.test.tsx`, and `MembersSection.test.tsx` (PR #183). |
| Architectural / design decisions contributed | Retain primary indigo `#3949ab`, use status colors with text labels and documented WCAG AA contrast ratios (`docs/design/palette.md`, PR #159); use an original calendar/checkmark logo in the existing primary color (`docs/design/logo.md`, PR #161); document role boundaries in the permission matrix (#165) and manual W2 state transitions/date guards checked against `EventLifecycle` and `EventService` (`05-workflows.md` §5.2, #182). No ADR authored (`docs/architecture/ADR-*`). |
| GenAI-related work | GenAI use and verification are recorded in `docs/genai/usage-log.md` rows (T-39/T-40 design proposals, actors/use-case/W1/W2 documentation, and friendly error messages); |
| Others | GitHub's merged-PR search for `JakabGergo` returned 13 PRs: #159, #161, #165, #167–#172, #178, #180, #182, and #183. |

---

## Portik Márk-Krisztián

**Ticket:** `docs: individual contribution – Portik Márk-Krisztián` · **Points:** 1

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | Membership and event frontend features (`frontend/src/components/AppLayout.tsx`, `frontend/src/features/organizations/`, `frontend/src/features/events/`, `frontend/src/features/organizing/`; PRs #118, #119, #121, #123, #156, #160, #162–#166); event lifecycle and search backend/tests (PRs #120, #154–#155, #157, #173–#174); student, organizer, organization and admin wireframes (`docs/design/wireframes.md`, `docs/assets/wireframes/`, PRs #175–#177). |
| Owned workflow | **W1 Organizations and membership**, UI contribution across the cross-layer flow in `05-workflows.md` §5.1: role-gated Organizing navigation and member list/add-remove screens (#118, #119, #121) call the membership APIs (`/users/me/memberships`, `/organizations/{id}/members`); those APIs read and update organization membership records in the database (T-01, T-03–T-05). The API/database work was shared with teammates. |
| Implemented functionality | Membership-aware navigation (T-02, PR #118); organization member list (T-06, PR #119); add/remove member UI (T-07, PR #121); event create/edit and lifecycle UI (T-13, #123; T-15, #162); event search and list (T-16, #155; T-19, #156); organizing page (T-14, #160), task/participant/announcement panels (T-31–T-33, #163, #164, #166). |
| Documentation produced | Frontend guide membership-navigation notes (T-02, PR #118); screen design notes and eight wireframes in `docs/design/wireframes.md` and `docs/assets/wireframes/` (T-41–T-43, PRs #175–#177); GenAI activity recorded in `docs/genai/usage-log.md` (Portik rows dated 2026-11-12–2026-11-16). |
| Tests written | `AppLayout.test.ts` (T-02, #118); `MembersSection.test.tsx` (T-06, #119); event form/list, organizing page, status actions and panel tests (T-13–T-16, T-19, T-31–T-33; PRs #123, #156, #160, #162–#166); `EventLifecycleTest` (T-37, #173) and `PermissionMatrixIntegrationTest` (T-38, #174). |
| Architectural / design decisions contributed | Membership navigation stays hidden while memberships load to prevent a role flicker (`AppLayout.test.ts`, T-02, #118); the member list is available to organization members and ADMIN, matching the API permission contract (T-06, #119); event updates use optimistic versioning and reject capacity below confirmed registrations (T-11, #120; D11 in `docs/data-model.md`); wireframes trace screen elements to use cases (T-41–T-43, #175–#177). |
| GenAI-related work | Codex (GPT-6) use and verification are recorded in `docs/genai/usage-log.md` rows dated 2026-11-12–2026-11-16. **No-genai ticket:** T-06 (PR #119) carried the `no-genai` label; the usage log records Codex assistance for that work, so it was not completed in compliance with that label. |
| Others | GitHub confirms 20 merged PRs by `MARKUCI14` in this repository; the latest is [#177](https://github.com/PatriciuMina/GenAI-Project/pull/177) (T-43). Commit history was checked per PR, including [T-06 PR #119](https://github.com/PatriciuMina/GenAI-Project/pull/119/commits) (`2f53697`) and [T-43 PR #177](https://github.com/PatriciuMina/GenAI-Project/pull/177/commits) (`3310cc3`). |

---

## Vlad-Stefan Uleia

**Ticket:** `docs: individual contribution – Vlad-Stefan Uleia` · **Points:** 1

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | `docs/midterm/02-requirements.md` (FR tables 2.1 to 2.8, PRs #130 to #136, #147); `docs/midterm/01-problem-statement.md` (1.1 to 1.4, PRs #152, #140, #141, #146); `docs/midterm/03-actors-roles.md` §3.1 (#148); `docs/midterm/07-genai-report.md` §7.1 to 7.4 (#142 to #145); backend packages `registration` (register, participant list, check-in) and `organizing` (announcements, tasks) |
| Owned workflow | **W5 event organization**, the backend (API + database) part: participant list (T-28, #125), announcements (T-30, #126), tasks (T-26, #129; T-27, #149) and check-in (T-29, #158), plus the UI that shows announcements on the event page (T-34, #153). The organizer screens that use these endpoints (organizer page, tasks, participants and check-in panel, announcement form) were built by Portik Márk-Krisztián (#160, #162 to #164, #166), not by me. The workflow description is `05-workflows.md` §5.5 |
| Implemented functionality | `GET /api/v1/events/{id}/registrations` (#125); `GET/POST /api/v1/events/{id}/announcements` (#126); `POST /api/v1/events/{id}/registrations` with the capacity limit (#127); `GET /api/v1/users/me/registrations` (#128); `GET/POST /api/v1/events/{id}/tasks` (#129); `PATCH /api/v1/events/{id}/tasks/{taskId}` (#149); `POST /api/v1/events/{id}/registrations/{registrationId}/check-in` (#158); event details page `/events/:id` (`EventDetailsPage.tsx`, #151); announcements on that page (`EventAnnouncements.tsx`, #153). Not mine: cancelling a registration (#179) and the register/cancel button (#181) by Gota Andrei; the My registrations page (#194) by Mina Patriciu |
| Documentation produced | Problem statement §1.1 to 1.4 (#152, #140, #141, #146); requirements §2.1 to 2.8: FR for accounts, W1 to W5, NFR and the R1 to R16 mapping (#130 to #136, #147); actors §3.1 (#148); sequence diagrams W2 and W4 in `05-workflows.md` (#137, #138); review of the ER diagram in `docs/data-model.md` against the migrations and entities (#139); GenAI report §7.1 to 7.4 (#142 to #145); review of the interview guide (#124) |
| Tests written | `RegistrationServiceTest`, `RegistrationListControllerIntegrationTest` (#125); `AnnouncementServiceTest`, `AnnouncementControllerIntegrationTest` (#126); `RegistrationServiceRegisterTest`, `RegistrationRegisterControllerIntegrationTest` (#127); `RegistrationServiceMineTest`, `MyRegistrationsControllerIntegrationTest` (#128); `TaskServiceTest`, `TaskControllerIntegrationTest` (#129); `TaskServiceUpdateTest`, `TaskUpdateControllerIntegrationTest` (#149); `RegistrationConcurrencyTest` (#150); `RegistrationServiceCheckInTest`, `RegistrationCheckInControllerIntegrationTest` (#158); frontend `eventFormat.test.ts`, `EventDetailsPage.test.tsx` (#151) and `EventAnnouncements.test.tsx` (#153) |
| Architectural / design decisions contributed | Capacity is enforced by a row lock on the event (`EventService.getEntityByIdForUpdate`) inside the registration transaction, and `RegistrationConcurrencyTest` shows it prevents overbooking (#127, #150); `EventService` is injected lazily into `RegistrationService` to avoid a constructor cycle (#127); check-in rule order: event 404, then 403, then registration 404, then the 409 rules; a registration of another event is 404 so ids do not leak (#158); the participant list and check-in are restricted to organizers because they expose personal data (#125). No ADR was written by me |
| GenAI-related work | Claude Code (Claude Sonnet 5.5) was used for all items above; each is a row in `docs/genai/usage-log.md` (rows dated 2026-11-15 and 2026-11-16 with my name), with the verification done and what I changed. GenAI report §7.1 to 7.4 (#142 to #145). **`no-genai` ticket:** T-29 check-in (#37, PR #158) is labelled `no-genai`, but I generated it with Claude Code; I confirmed this when asked, and the usage-log row says so. GitHub Copilot was used once to review the interview guide (usage-log row of 2026-11-14, #124) |
| Others | Opened and merged 31 PRs (`gh pr list --repo PatriciuMina/GenAI-Project --author uleiavlad27 --state merged`): #124 to #153 and #158 |
