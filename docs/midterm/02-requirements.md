# 2. Requirements

Part **B.2** of the mid-term deliverable. Index: [README](README.md).

**Status values** used in every table below:
- `implemented`: works end-to-end on `main` (backend + frontend where relevant) and is tested;
- `partial`: some layers exist (e.g. entity/repository or read-only endpoint), not the whole requirement;
- `planned`: nothing usable yet.

Check status against the code on `main` on the day you write it, and update it before submission.

---

## 2.1 High-level requirements R1–R16 (course)

**Ticket:** `docs: map course requirements R1–R16` · **Points:** 2

**What to write**
The list below is copied from `docs/course/course-requirements.md` (a team summary of the
official project statement; do not reword it). Fill in the two extra columns: which of our
FR/NFR IDs cover each course requirement, and its overall status. This table is the top of
the traceability chain (R15): R → FR/NFR → UC → code → tests.

**Where to find the information**
- `docs/course/course-requirements.md` (source of the list)
- Sections 2.2–2.8 below (FR/NFR IDs)
- R7–R9: `docs/data-model.md`, `docs/architecture/ADR-001…005`, Swagger UI `/swagger-ui.html`
- R11: `backend/src/test/java/...`, `frontend/src/**/*.test.tsx`, `.github/workflows/ci.yml`
- R12: `docker-compose.yml`, `README.md` §2 · R13–R14: `docs/genai/` · R16: `08-individual-contributions.md`

**Format**
The table below; "Covered by" lists IDs separated by commas.

**Done when**
- [ ] Every R has at least one FR or NFR in "Covered by" (or a written reason why not).
- [ ] Status filled for all 16 rows and checked against `main`.
- [ ] Title and Summary columns unchanged from the course file.

| ID | Title | Summary | Covered by | Status |
|----|-------|---------|------------|--------|
| R1 | Event Management | Authorized users create, modify, publish and manage events (title, description, date/time, location, organizing organization, capacity, registration information). | | |
| R2 | Organization and User Management | Manage student organizations and their members; users have different responsibilities and permissions; authentication, authorization and access control. | | |
| R3 | Participant Registration | Eligible users discover events and register; registration information is managed and business rules (capacity, registration status) are enforced. | | |
| R4 | Event Lifecycle | Support the lifecycle from creation and planning, through publication and registration, to the event itself and post-event activities; workflows and rules defined by the team. | | |
| R5 | Event Organization | Support organizers in preparing and managing events (responsibilities, event information, monitoring registrations, communication, attendance), justified by identified user needs. | | |
| R6 | Search and Information Access | Users find relevant events and access the information needed to decide whether to participate. | | |
| R7 | Data Persistence | Persistent storage; data model represents entities, relationships, constraints and business rules. | | |
| R8 | Enterprise Architecture | Multi-layer system separating presentation, business logic, persistence and external interfaces; decisions and technologies documented and justified. | | |
| R9 | APIs and Integration | Well-defined, documented APIs between components and, where appropriate, external systems. | | |
| R10 | User Interface | Usable interface for the main user categories, matching the documented workflows and use cases; important workflows validated with users. | | |
| R11 | Testing | Automated tests appropriate to the system: unit tests and, where appropriate, other test levels. | | |
| R12 | Deployment | Deployable and runnable outside the development environment; documented deployment approach and demonstration. | | |
| R13 | GenAI-Assisted Development | GenAI used throughout development as an engineering tool, with relevant uses documented. | | |
| R14 | Human Oversight and Engineering Judgment | Visible human-in-the-loop process: context, prompts, evaluation, corrections, rejections, validation. | | |
| R15 | Traceability | Requirements, use cases, workflows, architectural decisions, implementation and tests are connected. | | |
| R16 | Team-Based Development | Developed collaboratively; each student owns specific artifacts and at least one cross-layer workflow. | | |

> TODO (team): fill "Covered by" and "Status".

---

## Functional requirements: how to write them (applies to 2.2–2.7)

- **ID:** `FR-<group>-NN` (`FR-AUTH-01`, `FR-W1-01`, …). Never reuse or renumber an ID; if a
  requirement is dropped, mark it `dropped` in Notes.
- **Wording:** "The system shall …" / "A <role> can …". One testable behaviour per row.
  Include the business rule (limits, who may do it) in the same sentence.
- **R:** the course requirement(s) it serves (e.g. `R3`).
- **Status:** implemented / partial / planned (see top of file).
- **Source:** where the rule comes from: interview (`I-02`), `data-model.md` rule, ADR, team decision.
- Aim for **5–10 rows per workflow**. Use cases in `04-use-cases.md` will link to these IDs.

**Common table format**

| ID | Requirement | Actor(s) | R | Priority (MVP / later) | Status | Source |
|----|-------------|----------|---|------------------------|--------|--------|

---

## 2.2 Accounts and administration (FR-AUTH, FR-ADM)

**Ticket:** `docs: FR for accounts and admin` · **Points:** 1

**What to write**
Requirements for registering, logging in, seeing your own profile, and what the platform
admin can do (e.g. create organizations, assign presidents). Registration and login
already exist; describe them as they behave today (who can register, which role a new
account gets, what happens with a wrong password).

**Where to find the information**
- `backend/.../auth/controller/AuthController.java`, `auth/service/AuthService.java`, `auth/dto/RegisterRequest.java`
- `backend/.../user/controller/UserController.java` (`GET /api/v1/users/me`)
- `docs/architecture/ADR-003-jwt-auth-spring-security.md` → "Implementation notes"
- `backend/src/test/java/ro/ubb/events/auth/*` (the tests show the exact rules)
- `frontend/src/features/auth/`, `frontend/src/features/admin/AdminPage.tsx`

**Format**
Common table (above), 3–8 rows.

> **Example – replace:**
>
> | ID | Requirement | Actor(s) | R | Priority | Status | Source |
> |----|-------------|----------|---|----------|--------|--------|
> | FR-AUTH-01 | A visitor can create an account with email, full name and password; the new account always has the STUDENT role and the email must not already be used (case-insensitive). | Visitor | R2 | MVP | implemented | ADR-003, AuthService |

**Done when**
- [ ] Status of each row checked against the auth tests.
- [ ] Admin rows agreed with the team (what an ADMIN may do is not fully decided yet).

> TODO (team): FR-AUTH / FR-ADM table here.

---

## 2.3 W1 Organizations and membership (FR-W1)

**Ticket:** `docs: FR for W1 organizations` · **Points:** 2

**What to write**
Requirements for viewing organizations, creating/editing them, joining or being added as a
member, changing roles (PRESIDENT / ORGANIZER / MEMBER) and leaving. State who may do each
action. Listing and viewing organizations already exists (read-only).

**Where to find the information**
- `backend/.../organization/` (controller, service, `OrgPermissionService`, entities)
- `docs/data-model.md` → `organization_memberships` constraints (one role per user per org)
- `docs/dev-guide/how-to-add-a-feature.md` (organizations is the worked example)
- `frontend/src/features/organizations/`
- `03-actors-roles.md` (permission matrix)

**Format**
Common table, 5–10 rows.

Status checked against `main` on 2026-11-15.

| ID | Requirement | Actor(s) | R | Priority (MVP / later) | Status | Source |
|----|-------------|----------|---|------------------------|--------|--------|
| FR-W1-01 | Any logged-in user can list all organizations sorted by name and open one organization's details (name, description, contact email); an unknown organization returns 404. | Any logged-in user | R2 | MVP | implemented | `OrganizationController`, `OrganizationControllerIntegrationTest`; frontend `OrganizationsListPage`, `OrganizationDetailsPage` |
| FR-W1-02 | A logged-in user can see the organizations they belong to and their role in each (sorted by organization name); a user with no memberships gets an empty list. The "Organizing" menu entry is shown only to users who are ORGANIZER or PRESIDENT somewhere. | Any logged-in user | R2 | MVP | implemented | `MyMembershipsController`, `MyMembershipsControllerIntegrationTest`, `AppLayout` (T-01, T-02) |
| FR-W1-03 | A global ADMIN can create an organization and appoint an existing account, found by email, as its PRESIDENT. The organization and the president's membership are created together (both or nothing). The name is required, at most 150 characters and unique ignoring case (otherwise 409 `ORGANIZATION_NAME_TAKEN`); an unknown president email returns 404 `USER_NOT_FOUND`. | ADMIN | R2 | MVP | implemented | `OrganizationService.create`, `ux_organizations_name_lower` (`data-model.md`), `OrganizationCreateControllerIntegrationTest`; frontend `AdminPage`, `CreateOrganizationForm` (T-08, T-09) |
| FR-W1-04 | The members of an organization and global ADMINs can see its member list (name, email, role, joined date), ordered PRESIDENT, ORGANIZER, MEMBER and then by name. Anyone else gets 403. | Member, Organizer, President (of that organization), ADMIN | R2 | MVP | implemented | `OrganizationController.members` (`@PreAuthorize`), `OrganizationMembersControllerIntegrationTest`; frontend `MembersSection` (T-03, T-06) |
| FR-W1-05 | The PRESIDENT of an organization (or an ADMIN) can add an existing account, found by email ignoring case, with the role MEMBER or ORGANIZER. PRESIDENT cannot be assigned this way; a user can be in an organization at most once (409 `ALREADY_MEMBER`); an unknown email returns 404 `USER_NOT_FOUND`. Students do not join organizations by themselves in the MVP. | President (of that organization), ADMIN | R2 | MVP | implemented | `MembershipService.addMember`, `AddMemberRequest`, `uq_memberships_user_organization` (`data-model.md`), `OrganizationAddMemberControllerIntegrationTest`; frontend `MembersSection` (T-04, T-07; MVP decision in `docs/planning/midterm-tickets.md`, T-04) |
| FR-W1-06 | The PRESIDENT of an organization (or an ADMIN) can remove a member. The PRESIDENT cannot be removed (409 `CANNOT_REMOVE_PRESIDENT`), and a membership of another organization returns 404. Tasks assigned to the removed member become unassigned. | President (of that organization), ADMIN | R2 | MVP | implemented | `MembershipService.removeMember`, `fk_event_tasks_assignee` (`data-model.md`), `OrganizationRemoveMemberControllerIntegrationTest`; frontend `MembersSection` (T-05, T-07) |
| FR-W1-07 | Organization roles form a hierarchy PRESIDENT ⊃ ORGANIZER ⊃ MEMBER and apply per organization: a student can hold different roles in different organizations. A global ADMIN gets no automatic organization rights, only those an endpoint grants explicitly. | Member, Organizer, President, ADMIN | R2 | MVP | implemented | `OrgPermissionService`, `OrgPermissionServiceTest`, `OrgPermissionPreAuthorizeIntegrationTest` |
| FR-W1-08 | A member's role can be changed between MEMBER and ORGANIZER; the membership row is updated, not duplicated. Who may do it is not decided yet (expected: the PRESIDENT). | To be confirmed (expected: President) | R2 | later | planned | Deferred to the final delivery (`docs/planning/midterm-tickets.md`, "Deferred to final"); `uq_memberships_user_organization` (`data-model.md`) |
| FR-W1-09 | A member can leave an organization. Open question: how the PRESIDENT leaves or is replaced, since a president cannot be removed today (FR-W1-06). | Member, Organizer | R2 | later | planned | Deferred to the final delivery (`docs/planning/midterm-tickets.md`, "Deferred to final") |
| FR-W1-10 | The PRESIDENT can edit the details of their organization (name, description, contact email). | President | R2 | later | planned | Deferred to the final delivery (`docs/planning/midterm-tickets.md`, "Deferred to final") |

**Done when**
- [ ] Every row names who may perform the action.
- [ ] Role changes and leaving an organization are covered.
- [ ] Status checked against `main`.

---

## 2.4 W2 Event creation and lifecycle (FR-W2)

**Ticket:** `docs: FR for W2 event lifecycle` · **Points:** 2

**What to write**
Requirements for creating, editing, publishing, closing registration, starting, completing
and cancelling an event. Include the field rules (capacity > 0, end after start, optional
deadline ≤ start), the allowed status transitions, and who may perform them. Mention that
events with registrations are cancelled, never deleted.

**Where to find the information**
- `backend/.../event/entity/Event.java`, `EventStatus.java`, `EventCategory.java`
- `docs/data-model.md` → `events` constraints, "Status lifecycle", "Rules enforced in the service layer" (rule 4), D3, D4, D11
- `05-workflows.md` → W2 state machine

**Format**
Common table, 6–10 rows.

> **Example – replace:**
> | FR-W2-03 | An organizer of the event's organization can publish a DRAFT event; publishing is rejected if required fields are missing. | Organizer | R1, R4 | MVP | planned | data-model.md lifecycle |

**Done when**
- [ ] Every status transition in `EventStatus` is covered by a requirement.
- [ ] Field validation rules are listed.
- [ ] Consistent with the W2 state machine in `05-workflows.md`.

> TODO (team): FR-W2 table here.

---

## 2.5 W3 Event discovery and search (FR-W3)

**Ticket:** `docs: FR for W3 discovery` · **Points:** 1

**What to write**
Requirements for listing upcoming events, filtering (category, organization, date),
text search, sorting, and what an event details page shows so a student can decide to
participate (free places, deadline, location). State which events are visible to whom
(e.g. DRAFT only for organizers).

**Where to find the information**
- `docs/data-model.md` → W3 indexes on `events` (status/start_at, organization, category)
- `backend/.../event/repository/EventRepository.java`
- `frontend/src/features/events/EventsPage.tsx` (placeholder)
- `06-ux-research.md` → how students look for events today

**Format**
Common table, 4–8 rows.

> **Example – replace:**
> | FR-W3-02 | A student can filter upcoming published events by category. | Student | R6 | MVP | planned | data-model.md D3 |

**Done when**
- [ ] Visibility of each event status is stated.
- [ ] Filters match fields that exist in the data model (or a schema change is noted).

> TODO (team): FR-W3 table here.

---

## 2.6 W4 Participant registration (FR-W4)

**Ticket:** `docs: FR for W4 registration` · **Points:** 2

**What to write**
Requirements for registering, cancelling, re-registering and seeing "my registrations".
Include the business rules: capacity limit without overbooking, registration window
(PUBLISHED and before deadline/start), at most one confirmed registration per user and
event, no waitlist. Say what the user sees when a rule blocks them.

**Where to find the information**
- `backend/.../registration/entity/Registration.java`, `RegistrationStatus.java`
- `docs/data-model.md` → `registrations` constraints, service rules 1–2, D1 (no waitlist)
- `frontend/src/features/registrations/MyRegistrationsPage.tsx` (placeholder)

**Format**
Common table, 5–8 rows.

Status checked against `main` on 2026-11-15. The backend for registering and for "my registrations" exists; cancelling does not exist yet, and the W4 pages of the web app are still placeholders ("Coming soon"), so most rows are `partial` or `planned`.

| ID | Requirement | Actor(s) | R | Priority (MVP / later) | Status | Source |
|----|-------------|----------|---|------------------------|--------|--------|
| FR-W4-01 | A logged-in user can register for a published event; the registration is created as CONFIRMED and the event's free places drop by one. A draft or unknown event returns 404. | Student | R3 | MVP | partial (backend implemented; the Register button is planned) | `RegistrationService.register`, `RegistrationController`, `RegistrationRegisterControllerIntegrationTest` (T-21); button: T-24 |
| FR-W4-02 | The system never confirms more registrations than the event's capacity. When the confirmed registrations already equal the capacity, registration is rejected with 409 `EVENT_FULL`. There is no waitlist. Requests for the same event are processed one after another (the event row is locked), so two students cannot take the last place. | Student | R3 | MVP | partial (rule and lock implemented; the test with simultaneous requests is planned) | `docs/data-model.md` rule 1 and D1, `RegistrationService.register`, `RegistrationServiceRegisterTest`; concurrency test: T-35 |
| FR-W4-03 | Registration is open only while the event is PUBLISHED and the current time is not after the registration deadline, or after the start when the event has no deadline. Otherwise registration is rejected with 409 `REGISTRATION_CLOSED`. | Student | R3, R4 | MVP | implemented | `docs/data-model.md` rule 2, `RegistrationService.register`, `RegistrationServiceRegisterTest`, `RegistrationRegisterControllerIntegrationTest` |
| FR-W4-04 | A user has at most one CONFIRMED registration per event. Registering again while already confirmed is rejected with 409 `ALREADY_REGISTERED`; the database also enforces this, and a violation of that rule gives the same answer. | Student | R3 | MVP | implemented | `ux_registrations_confirmed_once` (`docs/data-model.md`), `RegistrationService.register`, `RegistrationRegisterControllerIntegrationTest` |
| FR-W4-05 | A user can cancel their own CONFIRMED registration while the event is PUBLISHED or REGISTRATION_CLOSED and has not started. The registration becomes CANCELLED (the row is kept as history) and the place is free again. A checked-in registration cannot be cancelled (409 `CANCEL_NOT_ALLOWED`, also for an event that is ongoing, completed, cancelled or already started); without a confirmed registration the answer is 404 `NOT_REGISTERED`. | Student | R3 | MVP | planned | Agreed behaviour of ticket T-22 (#30) in `docs/planning/midterm-tickets.md`; `docs/data-model.md` (cancelled rows stay as history, `ck_registrations_checkin_confirmed`) |
| FR-W4-06 | A user who cancelled can register again while the registration rules (FR-W4-02 to FR-W4-04) allow it; this creates a new CONFIRMED registration and the CANCELLED row stays as history. | Student | R3 | MVP | partial (re-registering works at the API level; cancelling itself is planned, see FR-W4-05) | `docs/data-model.md` (`ux_registrations_confirmed_once`), `RegistrationRegisterControllerIntegrationTest` |
| FR-W4-07 | A logged-in user can see only their own registrations, CONFIRMED and CANCELLED, newest first, each with the event's title, start, location and status; a user with none gets an empty list. | Student | R3, R6 | MVP | partial (backend implemented; the "My registrations" page is a placeholder) | `RegistrationController` (`GET /api/v1/users/me/registrations`), `MyRegistrationsControllerIntegrationTest` (T-23); page: T-25 |
| FR-W4-08 | When a rule blocks a registration, the user gets a clear answer: the API returns 409 with the code `EVENT_FULL`, `REGISTRATION_CLOSED` or `ALREADY_REGISTERED` and a readable message in the standard error body. The web app shows that message and, on the event page, the state of the button: Register, "You are registered" with Cancel, Full, or Registration closed. | Student | R3, R10 | MVP | partial (codes and messages implemented; the event page UI is planned) | Shared conventions and ticket T-24 in `docs/planning/midterm-tickets.md`, `RegistrationService.register`; UI: T-24 |

**Done when**
- [ ] Capacity, window, duplicate and cancel/re-register rules each have a row.
- [ ] The "what the user sees" part is stated for rejected registrations.

**Open questions for the team**
- The course text says "eligible users" can register (R3). The code lets any logged-in user register, including members, organizers and ADMINs, for any published event. Is that the intended rule, or do we want eligibility rules?

---

## 2.7 W5 Event organization (FR-W5)

**Ticket:** `docs: FR for W5 organizing` · **Points:** 2

**What to write**
Requirements for organizer tasks (create, assign to a member, change status), attendance
check-in, viewing the registration list, and announcements to participants. State who may
do each, and rules such as "assignee must be a member of the event's organization" and
"only confirmed registrations can be checked in".

**Where to find the information**
- `backend/.../organizing/entity/EventTask.java`, `TaskStatus.java`, `Announcement.java`
- `backend/.../registration/entity/Registration.java` (`checkedInAt`)
- `docs/data-model.md` → `event_tasks`, `announcements` constraints, service rules 3 and 5
- `frontend/src/features/organizing/OrganizingPage.tsx` (placeholder)
- `06-ux-research.md` → organizer interviews

**Format**
Common table, 5–10 rows.

> **Example – replace:**
> | FR-W5-04 | An organizer can mark a confirmed participant as checked in; the check-in time is stored. | Organizer | R5 | MVP | planned | data-model.md rule 5 |

**Done when**
- [ ] Tasks, check-in, participant list and announcements are each covered.
- [ ] Each row is justified by a user need or a data-model rule (Source column).

> TODO (team): FR-W5 table here.

---

## 2.8 Non-functional requirements (NFR)

**Ticket:** `docs: write non-functional requirements` · **Points:** 2

**What to write**
Quality requirements that are measurable or checkable: security (JWT, BCrypt, 401/403),
API conventions and documentation, data integrity (constraints, migrations), testing and
CI, deployment with Docker, usability/accessibility, performance targets you can actually
check, maintainability (feature packages). Each NFR must say how it is verified.

**Where to find the information**
- `docs/architecture/ADR-001…005`
- `CLAUDE.md` → "Coding conventions", "REST API"
- `.github/workflows/ci.yml`, `CONTRIBUTING.md` §4 Definition of Done
- `backend/src/main/resources/application*.yml`, `docker-compose.yml`

**Format**
Table: `ID | Category | Requirement | How it is verified | R | Status`. 6–12 rows.

> **Example – replace:**
>
> | ID | Category | Requirement | How it is verified | R | Status |
> |----|----------|-------------|--------------------|---|--------|
> | NFR-01 | Security | Every endpoint except health, Swagger, register and login requires a valid JWT; requests without one get 401 with the standard error body. | `AuthControllerIntegrationTest`, `SecurityConfig` | R2, R8 | implemented |

**Done when**
- [ ] Every NFR has a concrete "How it is verified".
- [ ] R7, R8, R9, R11, R12 are each covered by at least one NFR.
- [ ] No vague wording ("fast", "user-friendly") without a measure.

> TODO (team): NFR table here.
