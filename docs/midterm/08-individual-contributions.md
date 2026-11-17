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

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | |
| Owned workflow | |
| Implemented functionality | |
| Documentation produced | |
| Tests written | |
| Architectural / design decisions contributed | |
| GenAI-related work | |
| Others | |

> TODO (team): Mina Patriciu fills this section.

---

## Andrei Gota

**Ticket:** `docs: individual contribution – Andrei Gota` · **Points:** 1

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | |
| Owned workflow | |
| Implemented functionality | |
| Documentation produced | |
| Tests written | |
| Architectural / design decisions contributed | |
| GenAI-related work | |
| Others | |

> TODO (team): Andrei Gota fills this section.

---

## Jakab Gergo

**Ticket:** `docs: individual contribution – Jakab Gergo` · **Points:** 1

| Field | Contribution |
|-------|--------------|
| Owned artifact(s) | |
| Owned workflow | |
| Implemented functionality | |
| Documentation produced | |
| Tests written | |
| Architectural / design decisions contributed | |
| GenAI-related work | |
| Others | |

> TODO (team): Jakab Gergo fills this section.

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
