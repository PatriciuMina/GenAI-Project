# Mid-term tickets

All GitHub issues needed to finish the **mid-term deliverable** (parts A–E in
[`docs/course/course-requirements.md`](../course/course-requirements.md)). Nothing here is
on GitHub yet: a script will create the issues from this file (one `## T-<nn>:` block = one
issue). Work that already exists on `main` is **not** listed (auth, `GET /users/me`,
`GET /organizations[/{id}]`, `OrgPermissionService`, the V1 schema with all entities and
repositories, the dev seed, the frontend auth/layout/organizations pages, Docker, CI, ADRs,
the ER diagram, the mid-term drafts and the interview guide).

## How to use this file

1. Pick a ticket whose **Depends on** tickets are all *Done* (see
   [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2). There are no assignees: assign yourself.
2. Branch `feature/<issue-number>-short-name`, work, open a PR with `Closes #<issue>`.
3. Every ticket is written so that it can be done **without AI tools**: read the linked
   guide, the files listed under *Files/folders to touch* and the existing example code.
4. If you use GenAI anyway, add a row to [`docs/genai/usage-log.md`](../genai/usage-log.md)
   in the same PR (CONTRIBUTING §6).

**Points** follow [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §5 (1 = under 2 h … 5 = several
days). Nothing is bigger than 3 points.

### Labels

| Label | Meaning |
|-------|---------|
| `type:feature` · `type:test` · `type:design` · `type:diagram` · `type:research` · `type:docs` · `type:report` · `type:qa` | Kind of work (exactly one per ticket) |
| `area:backend` · `area:frontend` · `area:docs` | Part of the repository touched |
| `workflow:W1` … `workflow:W5` | Workflow from `CLAUDE.md` (W1 organizations, W2 event lifecycle, W3 discovery, W4 registration, W5 organizing) |
| `good-first-ticket` | 1–2 points, self-contained, a good way to start |
| `no-genai` | Must be done **without GenAI tools**. There are exactly 5 (one per team member): small features with real business logic. The author must be able to explain every rule and line in the PR review and in the individual contribution statement. |

## Shared conventions for all code tickets

Read these once; the tickets refer to them instead of repeating them.

**Backend** (guide: [`docs/dev-guide/how-to-add-a-feature.md`](../dev-guide/how-to-add-a-feature.md))
- Feature packages and layering from `CLAUDE.md`: controller → service → repository;
  DTOs are Java `record`s; entities never leave the service; another feature is used through
  its **service**, never its repository. `@Transactional` on service methods.
- Every endpoint is under `/api/v1`, needs a JWT (SecurityConfig default) and gets
  `@Operation` / `@ApiResponse` annotations so it appears in Swagger.
- Errors: throw `NotFoundException` (404), `ConflictException(code, message)` (409) or
  `new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "...")` (403). Validation errors
  (400 `VALIDATION_FAILED`) come automatically from `@Valid`. The body is always the common
  `ApiError` (`timestamp, status, code, message, fieldErrors`).
- Business error codes used by the tickets (use exactly these strings so the frontend can
  show friendly texts):

  | Code | Status | When |
  |------|--------|------|
  | `USER_NOT_FOUND` | 404 | an email does not belong to any account |
  | `ALREADY_MEMBER` | 409 | user is already a member of the organization |
  | `CANNOT_REMOVE_PRESIDENT` | 409 | trying to remove a PRESIDENT membership |
  | `ORGANIZATION_NAME_TAKEN` | 409 | organization name exists (case-insensitive) |
  | `EVENT_NOT_EDITABLE` | 409 | edit of an event that is not DRAFT/PUBLISHED |
  | `CONCURRENT_UPDATE` | 409 | optimistic-lock conflict on an event (`version` mismatch) |
  | `INVALID_STATUS_TRANSITION` | 409 | status change not allowed by the lifecycle or the dates |
  | `REGISTRATION_CLOSED` | 409 | event not PUBLISHED or deadline/start passed |
  | `EVENT_FULL` | 409 | confirmed registrations already equal capacity |
  | `ALREADY_REGISTERED` | 409 | user already has a CONFIRMED registration |
  | `NOT_REGISTERED` | 404 | user has no CONFIRMED registration to cancel |
  | `CANCEL_NOT_ALLOWED` | 409 | event already started, completed or cancelled |
  | `CHECK_IN_NOT_ALLOWED` | 409 | event status does not allow check-in, or registration cancelled |
  | `ALREADY_CHECKED_IN` | 409 | registration already has `checkedInAt` |
  | `ANNOUNCEMENT_NOT_ALLOWED` | 409 | posting on a DRAFT or COMPLETED event |
  | `ASSIGNEE_NOT_IN_ORGANIZATION` | 400 | task assignee membership belongs to another organization |
- Organization permissions: `@PreAuthorize("@orgPermissions.isOrganizer(principal.id, #orgId)")`
  etc. (guide §5). A global ADMIN gets **no** organization rights unless the ticket says so.
  When only an event id is known, the service looks up the event's organization and checks
  `OrgPermissionService` itself (guide §5, last bullet).
- Event visibility (used by W3 tickets): a **student** sees events with status
  `PUBLISHED`, `REGISTRATION_CLOSED`, `ONGOING`, `COMPLETED`, `CANCELLED` on the details page,
  and the search list shows only `PUBLISHED`, `REGISTRATION_CLOSED`, `ONGOING` events with
  `endAt >= now`. `DRAFT` events are visible only to organizers/presidents of their organization.
- Tests: Mockito unit test for the service (`*ServiceTest`) and a MockMvc + Testcontainers
  integration test (`*ControllerIntegrationTest`, extend `support/IntegrationTestBase`).
  Class names end with `Test`. Run `cd backend && ./mvnw verify`.

**Frontend** (guide: [`docs/dev-guide/frontend-guide.md`](../dev-guide/frontend-guide.md))
- API functions in `src/api/<feature>.ts`, types in `src/api/types.ts` (mirror the Java
  records), TanStack Query hooks in `src/features/<feature>/queries.ts` (§6), URLs in
  `src/routes/paths.ts`, routes in `src/routes/router.tsx` (§5).
- Use `LoadingState`, `ErrorAlert` and MUI components; no hard-coded colours (use the theme).
- Add a Vitest + React Testing Library test for non-trivial components (§8).
  Run `cd frontend && npm run lint && npm test && npm run build`.

**Seed accounts** for manual checks (password `password`, see README §3):
`admin@ubbevents.ro` (ADMIN), `ana.popescu@stud.ubbcluj.ro` (PRESIDENT, CS Society),
`elena.marin@stud.ubbcluj.ro` (ORGANIZER, CS Society), `ioana.rusu@stud.ubbcluj.ro`
(MEMBER, CS Society), `andrei.toma@stud.ubbcluj.ro` (no organization). Seeded events cover
every status (see `docs/data-model.md` → "Dev seed data").

**Docs / research / design tickets** (guide: [`docs/midterm/README.md`](../midterm/README.md))
- Write in the file named in the ticket; replace the `> TODO (team):` marker and the
  `> **Example – replace:**` block; tick the section's **Done when** list in the PR.
- Images go to `docs/assets/`, design notes to `docs/design/`, research notes to
  `docs/research/` or `docs/midterm/interviews/` (CONTRIBUTING §2 "Non-code tickets").
- Mermaid diagrams must render in the GitHub preview.

---

# Feature tickets (`type:feature`)

## W1 Organizations and membership

## T-01: feat: GET /users/me/memberships (my organizations and roles)
- Points: 2
- Labels: type:feature, area:backend, workflow:W1, good-first-ticket
- Depends on: none

**Context**
The frontend cannot know the user's organization roles (see the `TODO(W5)` in
`frontend/src/components/AppLayout.tsx` and `frontend-guide.md` §7). Several pages need
them: the "Organizing" menu, the members section, the organizing page.

**What to do**
Add an endpoint that returns the logged-in user's memberships with the organization name and role.

**Files/folders to touch**
- `backend/.../organization/dto/MembershipResponse.java` (new record)
- `backend/.../organization/mapper/` (new or extended mapper)
- `backend/.../organization/service/MembershipService.java` (new) using `OrganizationMembershipRepository.findByUserId`
- `backend/.../user/controller/UserController.java` (new method) **or** a new `organization/controller/MyMembershipsController.java`; keep the path below
- Tests in `backend/src/test/java/ro/ubb/events/organization/`

**API contract**
`GET /api/v1/users/me/memberships` → `200 OK`
```json
[
  { "membershipId": 3, "organizationId": 1, "organizationName": "UBB Computer Science Society",
    "role": "PRESIDENT", "joinedAt": "2026-11-06T10:15:30Z" }
]
```
`401` without token. A user without memberships gets `[]`.

**Business rules**
- Only the caller's own memberships (user id from `principal`, never from the URL).
- Sorted by organization name.

**Acceptance criteria**
- [ ] Endpoint returns the shape above, visible in Swagger.
- [ ] `ana.popescu` gets one PRESIDENT membership; `andrei.toma` gets `[]`.
- [ ] Service unit test + integration test (200 with data, 200 empty, 401).

**How to check it's done**
`./mvnw verify` passes; in Swagger log in as `ana.popescu@stud.ubbcluj.ro`, *Authorize*, call the endpoint.

**Guide**
`docs/dev-guide/how-to-add-a-feature.md` §1–§7 and §9 (Swagger with a token).

## T-02: feat: show "Organizing" in the menu only to organizers and presidents
- Points: 1
- Labels: type:feature, area:frontend, workflow:W1, workflow:W5, good-first-ticket
- Depends on: T-01

**Context**
`AppLayout.tsx` shows "Organizing" to everyone (`TODO(W5)`), because memberships were not available.

**What to do**
Load `GET /users/me/memberships` and show "Organizing" only if the user has at least one
ORGANIZER or PRESIDENT membership. Remove the TODO comment.

**Files/folders to touch**
- `frontend/src/api/types.ts` (`Membership` type), `frontend/src/api/users.ts` (`getMyMemberships`)
- `frontend/src/features/organizations/queries.ts` (`useMyMemberships` hook, key `['me','memberships']`)
- `frontend/src/components/AppLayout.tsx` (`visibleNavItems`)
- `frontend-guide.md` §7 (update the sentence about the TODO)

**Acceptance criteria**
- [ ] `elena.marin` (ORGANIZER) and `ana.popescu` (PRESIDENT) see "Organizing"; `ioana.rusu` (MEMBER) and `andrei.toma` do not.
- [ ] While memberships load, the item is hidden (no flicker error).
- [ ] Unit test for the nav filtering.

**How to check it's done**
`npm run lint && npm test && npm run build`; log in with the four accounts above.

**Guide**
`docs/dev-guide/frontend-guide.md` §6, §7, §8.

## T-03: feat: GET /organizations/{id}/members (members list)
- Points: 2
- Labels: type:feature, area:backend, workflow:W1, good-first-ticket
- Depends on: none

**Context**
Members and their roles are needed on the organization page (W1) and to assign tasks (W5).

**What to do**
Return the members of one organization, visible to its members and to ADMIN.

**Files/folders to touch**
- `organization/dto/MemberResponse.java`, `organization/service/MembershipService.java`
  (create it if T-01 is not merged yet), `organization/controller/OrganizationController.java`
- Tests in `backend/src/test/java/ro/ubb/events/organization/`

**API contract**
`GET /api/v1/organizations/{orgId}/members` → `200 OK`
```json
[
  { "membershipId": 3, "userId": 2, "fullName": "Ana Popescu",
    "email": "ana.popescu@stud.ubbcluj.ro", "role": "PRESIDENT", "joinedAt": "2026-11-06T10:15:30Z" }
]
```
`401` no token · `403` not a member and not ADMIN · `404` organization does not exist.

**Business rules**
- `@PreAuthorize("hasRole('ADMIN') or @orgPermissions.isMember(principal.id, #orgId)")`.
- Sorted: PRESIDENT, ORGANIZER, MEMBER, then by full name.

**Acceptance criteria**
- [ ] `ioana.rusu` sees CS Society members; `andrei.toma` gets 403; admin gets 200.
- [ ] Unknown org id gives 404 for admin.
- [ ] Unit + integration tests for 200/403/404/401.

**How to check it's done**
`./mvnw verify`; Swagger with the accounts above.

**Guide**
`how-to-add-a-feature.md` §5 (Level 3 permissions), §6, §7.

## T-04: feat: POST /organizations/{id}/members (president adds a member by email)
- Points: 2
- Labels: type:feature, area:backend, workflow:W1, good-first-ticket
- Depends on: T-03

**Context**
Decision for the MVP: students do not join by themselves; the president (or an admin) adds
an existing account by email and chooses its role.

**Files/folders to touch**
- `organization/dto/AddMemberRequest.java`, `MembershipService`, `OrganizationController`
- Use `UserService.findByEmail` (cross-feature through the service)

**API contract**
`POST /api/v1/organizations/{orgId}/members`
```json
{ "email": "andrei.toma@stud.ubbcluj.ro", "role": "MEMBER" }
```
→ `201 Created` with a `MemberResponse` (see T-03) and `Location` header.
Errors: `400 VALIDATION_FAILED` (blank/invalid email, missing role) · `403` · `404 USER_NOT_FOUND`
· `409 ALREADY_MEMBER`.

**Business rules**
- `@PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")`.
- `role` may be `MEMBER` or `ORGANIZER`; `PRESIDENT` is rejected with 400 (presidents are set by the admin, T-08).
- Email lookup is case-insensitive. One membership per user per org (`uq_memberships_user_organization`).

**Acceptance criteria**
- [ ] `ana.popescu` adds `andrei.toma` to CS Society → 201; adding again → 409 `ALREADY_MEMBER`.
- [ ] `elena.marin` (ORGANIZER) → 403. Unknown email → 404 `USER_NOT_FOUND`.
- [ ] Unit + integration tests for the cases above.

**How to check it's done**
`./mvnw verify`; Swagger, then `GET .../members` shows the new member.

**Guide**
`how-to-add-a-feature.md` §1–§7.

## T-05: feat: DELETE /organizations/{id}/members/{membershipId} (remove a member)
- Points: 1
- Labels: type:feature, area:backend, workflow:W1, good-first-ticket
- Depends on: T-03

**Context**
The president must be able to remove members. Tasks assigned to the removed membership
become unassigned automatically (`fk_event_tasks_assignee` ON DELETE SET NULL).

**API contract**
`DELETE /api/v1/organizations/{orgId}/members/{membershipId}` → `204 No Content`.
Errors: `403` · `404` (membership not found **or** belongs to another organization) ·
`409 CANNOT_REMOVE_PRESIDENT`.

**Business rules**
- `hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)`.
- A PRESIDENT membership cannot be removed (an organization always keeps its president).

**Files/folders to touch**
`MembershipService`, `OrganizationController`, tests.

**Acceptance criteria**
- [ ] President removes a MEMBER → 204, member disappears from the list.
- [ ] Removing the president → 409; membership id of another org → 404; ORGANIZER caller → 403.
- [ ] Tests for these cases.

**How to check it's done**
`./mvnw verify`; Swagger as `ana.popescu`.

**Guide**
`how-to-add-a-feature.md` §4–§7.

## T-06: feat: members list on the organization details page
- Points: 2
- Labels: type:feature, area:frontend, workflow:W1, good-first-ticket, no-genai
- Depends on: T-01, T-03

**Context**
`OrganizationDetailsPage.tsx` has `TODO(W1): members list`. This is a `no-genai` ticket:
write it yourself and be ready to explain how the page decides what to show.

**What to do**
Show a "Members" section (name, role chip, joined date) under the organization details.
Show it only if the user is a member of this organization (from `useMyMemberships`, T-01)
or is ADMIN; otherwise show nothing (do not call the endpoint, it would return 403).

**Files/folders to touch**
- `frontend/src/api/types.ts` (`Member`), `frontend/src/api/organizations.ts` (`getMembers`)
- `frontend/src/features/organizations/queries.ts` (`useMembers(orgId, enabled)`)
- `frontend/src/features/organizations/MembersSection.tsx` (new) + test
- `OrganizationDetailsPage.tsx` (render the section, update the TODO)

**Acceptance criteria**
- [ ] `ioana.rusu` sees CS Society members with roles; `andrei.toma` sees no section and no 403 error.
- [ ] Loading and error states use `LoadingState` / `ErrorAlert`; empty list shows "No members yet".
- [ ] Component test: renders members; hidden for non-members.

**How to check it's done**
`npm run lint && npm test && npm run build`; browser check with both accounts.

**Guide**
`frontend-guide.md` §5, §6, §8.

## T-07: feat: add and remove members (president UI)
- Points: 2
- Labels: type:feature, area:frontend, workflow:W1, good-first-ticket
- Depends on: T-04, T-05, T-06

**What to do**
In the Members section, presidents (and ADMIN) get an "Add member" form (email + role
MEMBER/ORGANIZER) and a "Remove" button per non-president member with a confirm dialog.
After success, invalidate the members query.

**Files/folders to touch**
`src/api/organizations.ts` (`addMember`, `removeMember`), `queries.ts` (mutations),
`MembersSection.tsx` (or `AddMemberForm.tsx`), tests.

**Acceptance criteria**
- [ ] `ana.popescu` adds `andrei.toma` and removes him again; the list updates without reload.
- [ ] `USER_NOT_FOUND` / `ALREADY_MEMBER` show a readable message next to the form.
- [ ] Organizers and members do not see the form or the buttons.
- [ ] Test for the form (submit + error message).

**How to check it's done**
Lint/test/build; browser check as `ana.popescu` and `elena.marin`.

**Guide**
`frontend-guide.md` §6 (mutations, "Showing backend errors"), §8.

## T-08: feat: POST /organizations (admin creates an organization and its president)
- Points: 3
- Labels: type:feature, area:backend, workflow:W1
- Depends on: none

**Context**
Today organizations exist only in the seed. The platform admin must create them and appoint
the first president.

**Files/folders to touch**
`organization/dto/CreateOrganizationRequest.java`, `OrganizationService` (create),
`OrganizationController`, `UserService.findByEmail` for the president, tests.

**API contract**
`POST /api/v1/organizations`
```json
{ "name": "UBB Robotics Club", "description": "Robots and coffee",
  "contactEmail": "robotics@ubbcluj.ro", "presidentEmail": "andrei.toma@stud.ubbcluj.ro" }
```
→ `201 Created`, `Location: /api/v1/organizations/3`, body = `OrganizationResponse`.
Errors: `400 VALIDATION_FAILED` (blank name or longer than 150 characters, invalid emails) · `403` (not ADMIN)
· `404 USER_NOT_FOUND` (president email) · `409 ORGANIZATION_NAME_TAKEN`.

**Business rules**
- `@PreAuthorize("hasRole('ADMIN')")`.
- Organization + PRESIDENT membership are created in **one transaction** (both or nothing).
- Name unique ignoring case (`ux_organizations_name_lower`): check first and also map the
  constraint violation to 409.

**Acceptance criteria**
- [ ] Admin creates an org; `GET /organizations` lists it; the president sees it in `/users/me/memberships`.
- [ ] Duplicate name in another case ("ubb theatre club") → 409; student caller → 403.
- [ ] Unit tests (happy path, duplicate, unknown president) + integration test.

**How to check it's done**
`./mvnw verify`; Swagger as `admin@ubbevents.ro`.

**Guide**
`how-to-add-a-feature.md` §1–§8.

## T-09: feat: Admin page – create organization form
- Points: 2
- Labels: type:feature, area:frontend, workflow:W1, good-first-ticket
- Depends on: T-08

**Context**
`frontend/src/features/admin/AdminPage.tsx` is a "Coming soon" placeholder (`TODO(W1)`);
the route is already ADMIN-only.

**What to do**
Replace the placeholder with a form: name, description, contact email, president email.
On success show a success message with a link to the new organization and reset the form.

**Files/folders to touch**
`src/api/organizations.ts` (`createOrganization`), `src/api/types.ts`, `features/admin/AdminPage.tsx`
(+ `CreateOrganizationForm.tsx`), test.

**Acceptance criteria**
- [ ] Admin creates "UBB Robotics Club" and can open it from the success link.
- [ ] Field errors (400) appear under the fields; 404/409 messages appear above the form.
- [ ] Required-field validation on the client; component test.

**How to check it's done**
Lint/test/build; browser as `admin@ubbevents.ro`.

**Guide**
`frontend-guide.md` §5, §6, §8 (see `LoginForm.tsx` for a form example).

## W2 Event creation and lifecycle

## T-10: feat: POST /organizations/{orgId}/events (create a draft event)
- Points: 3
- Labels: type:feature, area:backend, workflow:W2
- Depends on: none

**Context**
The `event` package has only the entity and repository. This ticket adds the first service
and controller of the package, plus the shared `EventResponse` DTO used by later W2/W3 tickets.

**What to do**
Organizers (ORGANIZER or PRESIDENT of the organization) create an event, which always starts as `DRAFT`.

**Files/folders to touch**
- `event/dto/EventRequest.java` (create/edit body), `event/dto/EventResponse.java`
- `event/mapper/EventMapper.java`, `event/service/EventService.java`, `event/controller/EventController.java`
- `OrganizationService` (get the organization entity or 404), `UserService` (creator)
- Tests in `backend/src/test/java/ro/ubb/events/event/`

**API contract**
`POST /api/v1/organizations/{orgId}/events`
```json
{ "title": "Intro to Docker", "description": "Hands-on workshop", "location": "FSEGA, room 2",
  "category": "WORKSHOP", "startAt": "2026-12-10T16:00:00Z", "endAt": "2026-12-10T18:00:00Z",
  "capacity": 40, "registrationDeadline": "2026-12-09T22:00:00Z" }
```
→ `201 Created`, `Location: /api/v1/events/12`
```json
{ "id": 12, "organizationId": 1, "organizationName": "UBB Computer Science Society",
  "title": "Intro to Docker", "description": "Hands-on workshop", "location": "FSEGA, room 2",
  "category": "WORKSHOP", "startAt": "2026-12-10T16:00:00Z", "endAt": "2026-12-10T18:00:00Z",
  "capacity": 40, "registrationDeadline": "2026-12-09T22:00:00Z", "status": "DRAFT",
  "confirmedCount": 0, "freePlaces": 40, "version": 0 }
```
Errors: `400 VALIDATION_FAILED` · `403` (not organizer of `orgId`) · `404` (organization).

**Business rules**
- `@PreAuthorize("@orgPermissions.isOrganizer(principal.id, #orgId)")` (ADMIN: no rights).
- Validation: title 1–200, location 1–255, category one of `EventCategory`, `capacity > 0`,
  `endAt > startAt`, `registrationDeadline` optional and `<= startAt`, `startAt` in the future.
  Cross-field rules return 400 with a `fieldErrors` entry (e.g. `endAt`).
- `createdBy` = caller, `status` = `DRAFT`.

**Acceptance criteria**
- [ ] `elena.marin` creates an event in CS Society → 201, status DRAFT.
- [ ] `ioana.rusu` (MEMBER) → 403; `elena.marin` in Theatre Club → 403.
- [ ] `endAt` before `startAt`, capacity 0, deadline after start → 400 with field errors.
- [ ] Unit tests for the validation rules + integration test.

**How to check it's done**
`./mvnw verify`; Swagger as `elena.marin@stud.ubbcluj.ro`; the event is in the DB (`docker compose exec postgres psql ...`).

**Guide**
`how-to-add-a-feature.md` (whole guide, organizations is the worked example); `docs/data-model.md` → events.

## T-11: feat: PUT /events/{id} (edit an event)
- Points: 2
- Labels: type:feature, area:backend, workflow:W2, good-first-ticket
- Depends on: T-10

**What to do**
Organizers edit an event while it is `DRAFT` or `PUBLISHED`. Use the `version` field for
optimistic locking (D11) so two organizers cannot overwrite each other.

**API contract**
`PUT /api/v1/events/{id}` with the same body as T-10 **plus** `"version": 0` → `200 OK` with `EventResponse` (version incremented).
Errors: `400` · `403` · `404` · `409 EVENT_NOT_EDITABLE` · `409 CONCURRENT_UPDATE` (version differs or
`ObjectOptimisticLockingFailureException`).

**Business rules**
- Permission: organizer of the event's organization (checked in the service, event id only).
- Same validation as T-10. `capacity` cannot go below the current number of CONFIRMED
  registrations (400, field `capacity`).
- Status and organization cannot be changed here.

**Files/folders to touch**
`EventService`, `EventController`, `EventRequest` (or `UpdateEventRequest` with `version`),
`GlobalExceptionHandler` only if you map the optimistic-lock exception there; tests.

**Acceptance criteria**
- [ ] Edit of a DRAFT and of a PUBLISHED event works; a COMPLETED event → 409 `EVENT_NOT_EDITABLE`.
- [ ] Sending an old `version` → 409 `CONCURRENT_UPDATE`.
- [ ] Capacity below confirmed count → 400.
- [ ] Tests for each rule.

**How to check it's done**
`./mvnw verify`; in Swagger send the same PUT twice with the old version (second → 409).

**Guide**
`how-to-add-a-feature.md` §3–§7; `docs/data-model.md` D11.

## T-12: feat: POST /events/{id}/status (lifecycle transitions and cancel)
- Points: 3
- Labels: type:feature, area:backend, workflow:W2
- Depends on: T-10

**Context**
Lifecycle from `docs/data-model.md`: `DRAFT → PUBLISHED → REGISTRATION_CLOSED → ONGOING →
COMPLETED`; `CANCELLED` from any status except `COMPLETED`. Decision: transitions are
**manual** (organizer presses a button); there is no scheduler.

**API contract**
`POST /api/v1/events/{id}/status`
```json
{ "status": "PUBLISHED" }
```
→ `200 OK` with `EventResponse`. Errors: `400` (unknown status) · `403` · `404` ·
`409 INVALID_STATUS_TRANSITION` (message says which transition and why).

**Business rules** (put the transition table in one place, e.g. `EventStatus.canMoveTo(...)` or a small `EventLifecycle` class)

| From | To | Extra check |
|------|----|-------------|
| DRAFT | PUBLISHED | `startAt` in the future |
| PUBLISHED | REGISTRATION_CLOSED | none |
| PUBLISHED, REGISTRATION_CLOSED | ONGOING | `now >= startAt - 1 hour` |
| ONGOING | COMPLETED | `now >= endAt` |
| any except COMPLETED, CANCELLED | CANCELLED | none |

- Everything else (e.g. DRAFT → ONGOING, COMPLETED → CANCELLED, X → X, back to DRAFT) → 409.
- Permission: organizer of the event's organization.
- Cancelling keeps the registrations as they are (history, D4); participants see the status.

**Files/folders to touch**
`event/entity/EventStatus.java` or new `event/service/EventLifecycle.java`, `EventService`,
`EventController`, `event/dto/ChangeStatusRequest.java`, tests (basic ones here; the full
matrix is T-37).

**Acceptance criteria**
- [ ] All allowed transitions in the table work for an organizer.
- [ ] A few disallowed ones return 409 with code `INVALID_STATUS_TRANSITION`.
- [ ] MEMBER caller → 403.

**How to check it's done**
`./mvnw verify`; Swagger: publish "Spring Hackathon 2027" (seed DRAFT) as `elena.marin`.

**Guide**
`how-to-add-a-feature.md` §3–§7; `docs/data-model.md` → "Status lifecycle (W2)", rule 4.

## T-13: feat: create/edit event form
- Points: 3
- Labels: type:feature, area:frontend, workflow:W2
- Depends on: T-10, T-11, T-18

**What to do**
A form page for organizers to create an event for one of their organizations and to edit it.
Fields: organization (select from `useMyMemberships`, ORGANIZER/PRESIDENT only; fixed on edit),
title, description, location, category (select), start, end, capacity, optional deadline.
Send dates as ISO UTC strings; show them in local time.

**Files/folders to touch**
- `src/routes/paths.ts`: `organizingNewEvent: '/organizing/events/new'`, `organizingEditEvent: (id) => '/organizing/events/${id}/edit'`
- `src/routes/router.tsx`, `src/api/events.ts` (new: `createEvent`, `updateEvent`, `getEvent`), `src/api/types.ts` (`Event`, `EventCategory`, `EventStatus`, `EventRequest`)
- `src/features/organizing/EventForm.tsx`, `EventFormPage.tsx` + test

**Acceptance criteria**
- [ ] Create → redirects to the organizer event page (T-15) or the organizing list if T-15 is not merged.
- [ ] Edit pre-fills the fields and sends `version`; 409 `CONCURRENT_UPDATE` shows "Someone else changed this event, reload".
- [ ] Client validation for required fields, end after start, capacity > 0; server field errors shown under fields.
- [ ] Component test for validation and submit.

**How to check it's done**
Lint/test/build; as `elena.marin`, create an event and edit it.

**Guide**
`frontend-guide.md` §5, §6, §8.

## T-14: feat: Organizing page – events of my organizations
- Points: 2
- Labels: type:feature, area:frontend, workflow:W2, good-first-ticket
- Depends on: T-01, T-17

**Context**
`features/organizing/OrganizingPage.tsx` is a placeholder (`TODO(W5)`). It becomes the
organizer's home: their organizations and all their events, drafts included.

**What to do**
For each membership with role ORGANIZER/PRESIDENT, list the organization's events
(`GET /organizations/{id}/events`) in a table: title, start, status chip, confirmed/capacity.
Button "New event" (T-13 route), row click → organizer event page (T-15 route).
Users without organizer roles see "You are not an organizer in any organization".

**Files/folders to touch**
`src/api/events.ts` (`getOrganizationEvents`), `features/organizing/queries.ts`,
`OrganizingPage.tsx`, `src/routes/paths.ts` (`organizingEvent: (id) => '/organizing/events/${id}'`), test.

**Acceptance criteria**
- [ ] `elena.marin` sees the 4 CS Society seed events including the DRAFT.
- [ ] Empty and loading states shown; test for the "not an organizer" text.

**How to check it's done**
Lint/test/build; browser as `elena.marin` and `andrei.toma`.

**Guide**
`frontend-guide.md` §5, §6, §10 (placeholder pages).

## T-15: feat: organizer event page with status actions
- Points: 2
- Labels: type:feature, area:frontend, workflow:W2, good-first-ticket
- Depends on: T-12, T-14, T-18

**What to do**
Page `/organizing/events/:id` showing the event (from `GET /events/{id}`), an "Edit" button
(DRAFT/PUBLISHED only) and buttons for the transitions allowed from the current status
(Publish, Close registration, Start, Complete, Cancel with confirm dialog). The page has
empty placeholder areas (tabs or sections) for Tasks, Participants and Announcements that
T-31, T-32, T-33 fill.

**Files/folders to touch**
`src/api/events.ts` (`changeEventStatus`), `features/organizing/OrganizerEventPage.tsx`,
`features/organizing/StatusActions.tsx` + test, router.

**Acceptance criteria**
- [ ] Only allowed buttons are shown for each status (same table as T-12).
- [ ] 409 `INVALID_STATUS_TRANSITION` shows the backend message (e.g. "cannot complete before end").
- [ ] After a change the page and the organizing list refresh.
- [ ] Test: correct buttons for DRAFT and ONGOING.

**How to check it's done**
Lint/test/build; as `elena.marin` publish the seed DRAFT event.

**Guide**
`frontend-guide.md` §5, §6, §8.

## W3 Event discovery and search

## T-16: feat: GET /events (search with filters and paging)
- Points: 3
- Labels: type:feature, area:backend, workflow:W3
- Depends on: T-10

**Context**
Students need to find upcoming events. Indexes for this already exist
(`ix_events_status_start_at`, `ix_events_category`, `ix_events_organization_id`).
Depends on T-10 because it reuses `EventResponse`, `EventMapper` and `EventService`.

**What to do**
A paged search over visible events, plus a reusable `PageResponse` DTO in `common`.

**Files/folders to touch**
- `common/dto/PageResponse.java` (new generic record: `content, page, size, totalElements, totalPages`)
- `event/repository/EventRepository.java` (JPA `Specification` or `@Query` with optional parameters;
  extend `JpaSpecificationExecutor<Event>` if you use specifications)
- `EventService.search(...)`, `EventController`, tests

**API contract**
`GET /api/v1/events?q=spring&category=WORKSHOP&organizationId=1&from=2026-11-01T00:00:00Z&to=2026-12-31T23:59:59Z&page=0&size=20`
(all parameters optional) → `200 OK`
```json
{ "content": [ { "id": 2, "title": "Intro to Spring Boot Workshop", "organizationId": 1,
    "organizationName": "UBB Computer Science Society", "category": "WORKSHOP",
    "location": "FMI, room 2/I", "startAt": "2026-11-20T16:00:00Z", "endAt": "2026-11-20T18:00:00Z",
    "capacity": 30, "registrationDeadline": "2026-11-19T22:00:00Z", "status": "PUBLISHED",
    "confirmedCount": 2, "freePlaces": 28, "version": 0 } ],
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```
`400 INVALID_PARAMETER` for a wrong category or date.

**Business rules**
- Only status `PUBLISHED`, `REGISTRATION_CLOSED`, `ONGOING` and `endAt >= now` (see header "Event visibility"). Never DRAFT or CANCELLED.
- `q`: case-insensitive "contains" on the title. `from`/`to`: filter on `startAt`.
- Sorted by `startAt` ascending; `size` max 50 (larger → 50).
- `confirmedCount` comes from `RegistrationRepository.countByEventIdAndStatus` through a small
  method in a registration service (cross-feature rule). Keep it simple; N+1 is acceptable for size ≤ 50.

**Acceptance criteria**
- [ ] With the seed: default call returns the 2 PUBLISHED, the REGISTRATION_CLOSED and the ONGOING events; no DRAFT/COMPLETED/CANCELLED.
- [ ] Each filter works alone and combined; paging fields are correct.
- [ ] Integration test with seeded test data for filters and visibility.

**How to check it's done**
`./mvnw verify`; Swagger with several filter combinations.

**Guide**
`how-to-add-a-feature.md` §1–§7; `docs/data-model.md` → events indexes.

## T-17: feat: GET /organizations/{id}/events (events of one organization)
- Points: 2
- Labels: type:feature, area:backend, workflow:W3, good-first-ticket, no-genai
- Depends on: T-10

**Context**
Organizers need all events of their organization, including drafts (organizing page, T-14);
other users may see the organization's public events. This is a `no-genai` ticket: write it
yourself and be ready to explain the visibility rule.

**API contract**
`GET /api/v1/organizations/{orgId}/events` → `200 OK`, a JSON array of `EventResponse` (shape in T-10), sorted by `startAt` ascending.
`404` if the organization does not exist.

**Business rules**
- Caller is ORGANIZER/PRESIDENT of `orgId` (`OrgPermissionService.isOrganizer`) → **all** statuses.
- Anyone else → all statuses **except DRAFT**.
- Uses the existing `EventRepository.findByOrganizationIdOrderByStartAtAsc`.

**Files/folders to touch**
`EventService`, `OrganizationController` **or** `EventController` (path must stay as above), tests.

**Acceptance criteria**
- [ ] `elena.marin` gets 4 CS Society events incl. "Spring Hackathon 2027" (DRAFT).
- [ ] `andrei.toma` gets the same list without the DRAFT.
- [ ] Unknown org → 404. Unit test for both visibility branches + integration test.

**How to check it's done**
`./mvnw verify`; Swagger as both accounts.

**Guide**
`how-to-add-a-feature.md` §3–§7.

## T-18: feat: GET /events/{id} (event details with free places)
- Points: 2
- Labels: type:feature, area:backend, workflow:W3, good-first-ticket
- Depends on: T-10

**What to do**
Return one event with `confirmedCount` and `freePlaces` so a student can decide to register.
Add a small public method in a registration-side service (e.g.
`registration/service/RegistrationQueryService.countConfirmed(eventId)`) that the event
service calls; W4 tickets extend that package.

**API contract**
`GET /api/v1/events/{id}` → `200 OK` with `EventResponse` (shape in T-10; `freePlaces = capacity - confirmedCount`).
`404` if the event does not exist **or** is DRAFT and the caller is not an organizer of its organization (do not reveal drafts).

**Files/folders to touch**
`EventService`, `EventController`, `registration/service/...`, tests.

**Acceptance criteria**
- [ ] "Intro to Spring Boot Workshop" shows `confirmedCount` 2, `freePlaces` 28.
- [ ] Seed DRAFT: 200 for `elena.marin`, 404 for `andrei.toma`.
- [ ] CANCELLED and COMPLETED events are returned (status visible).
- [ ] Unit + integration tests.

**How to check it's done**
`./mvnw verify`; Swagger.

**Guide**
`how-to-add-a-feature.md` §3–§7.

## T-19: feat: events list with filters
- Points: 3
- Labels: type:feature, area:frontend, workflow:W3
- Depends on: T-16

**Context**
`features/events/EventsPage.tsx` is a placeholder (`TODO(W2)`/`TODO(W3)`).

**What to do**
Replace it with a list of event cards (title, organization, date/time, location, category,
"x places left" or "Full"/"Registration closed") and filters: search text, category, organization
(from `useOrganizations`), date range. Keep filters in the URL query string so a link can be
shared. Pagination with MUI `Pagination`. Card click → `/events/:id` (T-20).

**Files/folders to touch**
`src/api/events.ts` (`searchEvents`), `src/api/types.ts` (`PageResponse<T>`),
`features/events/queries.ts`, `EventsPage.tsx`, `EventCard.tsx`, `EventFilters.tsx`, tests;
`frontend-guide.md` §3/§10 (events no longer "Coming soon").

**Acceptance criteria**
- [ ] Default list shows the seed's visible events in date order.
- [ ] Each filter changes the request; filters survive a page reload.
- [ ] Empty result shows "No events match your filters".
- [ ] Tests for `EventCard` (full / places left) and the filters.

**How to check it's done**
Lint/test/build; browser as any student.

**Guide**
`frontend-guide.md` §5, §6, §8, §10.

## T-20: feat: event details page
- Points: 2
- Labels: type:feature, area:frontend, workflow:W3, good-first-ticket
- Depends on: T-18

**What to do**
New page `/events/:id` with everything needed to decide: title, organization (link),
category, start/end, location, description, capacity, free places, registration deadline
("open until the event starts" when null), status. Leave a clearly marked area for the
Register button (T-24) and announcements (T-34). 404 → "Event not found".

**Files/folders to touch**
`src/routes/paths.ts` (`event: (id) => '/events/${id}'`), router, `src/api/events.ts` (`getEvent`),
`features/events/EventDetailsPage.tsx` + test (follow `OrganizationDetailsPage.tsx`).

**Acceptance criteria**
- [ ] Seed events show correct free places and deadline text.
- [ ] Unknown id and DRAFT (for a student) show "Event not found".
- [ ] Component test for the deadline text and the not-found state.

**How to check it's done**
Lint/test/build; open a few seed events.

**Guide**
`frontend-guide.md` §5, §6, §8.

## W4 Participant registration

## T-21: feat: POST /events/{id}/registrations (register with capacity limit)
- Points: 3
- Labels: type:feature, area:backend, workflow:W4
- Depends on: T-18

**Context**
The central business rule of W4: never more CONFIRMED registrations than `capacity`, even
when two students take the last place at the same moment (`docs/data-model.md` rules 1–2).
No waitlist (D1).

**What to do**
Create `registration/service/RegistrationService`, `registration/controller/RegistrationController`
and DTOs. Add a locking read of the event to the event side, e.g.
`EventRepository.findByIdForUpdate` with `@Lock(LockModeType.PESSIMISTIC_WRITE)` exposed through
an `EventService` method (registration must not use `EventRepository` directly).

**API contract**
`POST /api/v1/events/{eventId}/registrations` (no body) → `201 Created`
```json
{ "id": 31, "eventId": 3, "eventTitle": "Improv Night", "status": "CONFIRMED",
  "registeredAt": "2026-11-08T09:12:00Z", "checkedInAt": null }
```
Errors: `404` (event not found or DRAFT) · `409 REGISTRATION_CLOSED` · `409 EVENT_FULL` · `409 ALREADY_REGISTERED`.

**Business rules** (all inside one `@Transactional` method, in this order)
1. Lock the event row (`PESSIMISTIC_WRITE`).
2. Event status must be `PUBLISHED` and `now <= coalesce(registrationDeadline, startAt)`, else `REGISTRATION_CLOSED`.
3. Caller has no CONFIRMED registration for the event, else `ALREADY_REGISTERED`
   (also map a violation of `ux_registrations_confirmed_once` to this code).
4. `countByEventIdAndStatus(CONFIRMED) < capacity`, else `EVENT_FULL`.
5. Insert a new CONFIRMED row (a previous CANCELLED row stays as history).

**Files/folders to touch**
`registration/{controller,service,dto,mapper}`, `event/repository/EventRepository.java`,
`EventService`, tests (basic cases here; concurrency is T-35, full rule suite T-36).

**Acceptance criteria**
- [ ] `elena.marin` registers for "Intro to Spring Boot Workshop" → 201; `ioana.rusu` (already registered there) → 409 `ALREADY_REGISTERED`.
- [ ] "Tech Career Fair Prep" (REGISTRATION_CLOSED, full) → 409 `REGISTRATION_CLOSED`.
- [ ] A PUBLISHED event with capacity reached → 409 `EVENT_FULL` (create one in the test).
- [ ] Unit tests for each rule + one integration test.

**How to check it's done**
`./mvnw verify`; Swagger as `elena.marin`, register for "Improv Night"; `freePlaces` in `GET /events/{id}` drops by one.

**Guide**
`how-to-add-a-feature.md` §1–§7; `docs/data-model.md` → registrations, rules 1–2.

## T-22: feat: DELETE /events/{id}/registrations/me (cancel my registration)
- Points: 2
- Labels: type:feature, area:backend, workflow:W4, good-first-ticket, no-genai
- Depends on: T-21

**Context**
A student who cannot come frees the place. The row is not deleted; its status becomes
CANCELLED so history stays, and the student may register again later (new row).
This is a `no-genai` ticket: write it yourself and be ready to explain each rule.

**API contract**
`DELETE /api/v1/events/{eventId}/registrations/me` → `204 No Content`.
Errors: `404 NOT_REGISTERED` (no CONFIRMED registration of the caller) · `409 CANCEL_NOT_ALLOWED`.

**Business rules**
- Only the caller's own CONFIRMED registration (`findByEventIdAndUserIdAndStatus`).
- Allowed while the event is `PUBLISHED` or `REGISTRATION_CLOSED` and `now < startAt`;
  otherwise `CANCEL_NOT_ALLOWED` (ONGOING, COMPLETED, CANCELLED, or already started).
- A registration that is checked in cannot be cancelled (`CANCEL_NOT_ALLOWED`).
- After cancelling, `POST .../registrations` works again if places and window allow (T-21).

**Files/folders to touch**
`RegistrationService`, `RegistrationController`, `Registration` entity (a `cancel()` method), tests.

**Acceptance criteria**
- [ ] `andrei.toma` cancels "Intro to Spring Boot Workshop" → 204; free places +1; registering again → 201.
- [ ] Second cancel → 404 `NOT_REGISTERED`; ONGOING event → 409 `CANCEL_NOT_ALLOWED`.
- [ ] Unit tests for every rule + integration test.

**How to check it's done**
`./mvnw verify`; Swagger as `andrei.toma`.

**Guide**
`how-to-add-a-feature.md` §3–§7; `docs/data-model.md` → `ux_registrations_confirmed_once`.

## T-23: feat: GET /users/me/registrations (my registrations)
- Points: 2
- Labels: type:feature, area:backend, workflow:W4, good-first-ticket
- Depends on: T-18

**API contract**
`GET /api/v1/users/me/registrations` → `200 OK`
```json
[ { "id": 7, "eventId": 2, "eventTitle": "Intro to Spring Boot Workshop",
    "eventStartAt": "2026-11-14T16:00:00Z", "eventLocation": "Room 2/I", "eventStatus": "PUBLISHED",
    "status": "CONFIRMED", "registeredAt": "2026-11-06T10:00:00Z", "checkedInAt": null } ]
```

**Business rules**
- Only the caller's registrations, newest first (`findByUserIdOrderByRegisteredAtDesc`), CONFIRMED and CANCELLED.
- Event fields come through `EventService` (cross-feature through the service).

**Files/folders to touch**
`registration/dto/MyRegistrationResponse.java`, `RegistrationService`, controller (path above), tests.

**Acceptance criteria**
- [ ] `andrei.toma` sees his 5 seed registrations incl. the CANCELLED one.
- [ ] A user without registrations gets `[]`. Unit + integration tests.

**How to check it's done**
`./mvnw verify`; Swagger.

**Guide**
`how-to-add-a-feature.md` §1–§7.

## T-24: feat: Register / Cancel button on the event details page
- Points: 2
- Labels: type:feature, area:frontend, workflow:W4, good-first-ticket
- Depends on: T-20, T-21, T-22, T-23

**What to do**
On `/events/:id` show one of: "Register" (window open, places left), "You are registered"
+ "Cancel registration" (with confirm), "Full", "Registration closed". Whether the user is
registered comes from `useMyRegistrations` (T-23). After a change invalidate the event and
my-registrations queries.

**Files/folders to touch**
`src/api/registrations.ts` (new), `features/registrations/queries.ts`,
`features/events/RegistrationButton.tsx` + test, `EventDetailsPage.tsx`.

**Acceptance criteria**
- [ ] Registering updates free places on the page without reload.
- [ ] `EVENT_FULL`, `REGISTRATION_CLOSED`, `ALREADY_REGISTERED` show readable messages.
- [ ] Test: correct state for registered / full / closed.

**How to check it's done**
Lint/test/build; as `elena.marin` register and cancel for "Improv Night".

**Guide**
`frontend-guide.md` §6 (mutations, errors), §8.

## T-25: feat: My registrations page
- Points: 2
- Labels: type:feature, area:frontend, workflow:W4, good-first-ticket, no-genai
- Depends on: T-22, T-23

**Context**
`features/registrations/MyRegistrationsPage.tsx` is a placeholder (`TODO(W4)`). This is a
`no-genai` ticket: write it yourself and be ready to explain how upcoming/past are split.

**What to do**
Two sections: **Upcoming** (CONFIRMED, event not COMPLETED/CANCELLED and not ended) and
**Past and cancelled** (the rest). Each row: event title (link to `/events/:id`), date,
location, status chip (Confirmed / Cancelled / Checked in / Event cancelled). Upcoming rows
have "Cancel" (T-22, with confirm).

**Files/folders to touch**
`src/api/registrations.ts`, `features/registrations/queries.ts`, `MyRegistrationsPage.tsx`, test.

**Acceptance criteria**
- [ ] `andrei.toma` sees "Intro to Spring Boot Workshop" under Upcoming and the cancelled "Board Games Evening" under Past.
- [ ] Cancel works and moves the row; empty state "You have not registered for any event yet" with a link to Events.
- [ ] Test for the upcoming/past split.

**How to check it's done**
Lint/test/build; browser as `andrei.toma`.

**Guide**
`frontend-guide.md` §5, §6, §8, §10.

## W5 Event organization

## T-26: feat: GET/POST /events/{id}/tasks (list and create organizer tasks)
- Points: 2
- Labels: type:feature, area:backend, workflow:W5, good-first-ticket
- Depends on: T-03, T-18

**Context**
Organizers split preparation work into tasks (`organizing/entity/EventTask.java`). The
assignee is an organization **membership**, not a user (`assignee_membership_id`).

**API contract**
`GET /api/v1/events/{eventId}/tasks` → `200 OK`
```json
[ { "id": 1, "title": "Book the room", "description": "Confirm Room 2/I with the faculty secretariat.",
    "status": "DONE", "dueAt": "2026-11-09T12:00:00Z",
    "assignee": { "membershipId": 4, "fullName": "Elena Marin" }, "createdAt": "2026-11-06T10:00:00Z" } ]
```
`POST /api/v1/events/{eventId}/tasks`
```json
{ "title": "Order snacks", "description": "For 30 people", "dueAt": "2026-11-13T12:00:00Z", "assigneeMembershipId": 4 }
```
→ `201 Created` with the task. Errors: `400 VALIDATION_FAILED` · `400 ASSIGNEE_NOT_IN_ORGANIZATION` · `403` · `404`.

**Business rules**
- Both endpoints: caller must be ORGANIZER/PRESIDENT of the event's organization (service check).
- `title` 1–200; `assigneeMembershipId` and `dueAt` optional.
- The assignee membership must belong to the event's organization (data-model rule 3);
  look it up through `MembershipService` (T-03).
- New tasks start as `TODO`. List sorted by `dueAt` (nulls last) — existing `findByEventIdOrderByDueAtAsc`.

**Files/folders to touch**
`organizing/{controller,service,dto,mapper}` (new), tests.

**Acceptance criteria**
- [ ] `elena.marin` lists the 3 seed tasks of "Intro to Spring Boot Workshop" and creates one.
- [ ] Assignee from Theatre Club → 400 `ASSIGNEE_NOT_IN_ORGANIZATION`; MEMBER caller → 403.
- [ ] Unit + integration tests.

**How to check it's done**
`./mvnw verify`; Swagger.

**Guide**
`how-to-add-a-feature.md` §1–§7; `docs/data-model.md` → event_tasks, rule 3.

## T-27: feat: PATCH /events/{id}/tasks/{taskId} (assign and change status)
- Points: 2
- Labels: type:feature, area:backend, workflow:W5, good-first-ticket
- Depends on: T-26

**What to do**
Change a task's assignee and/or status. Editing title/description/due date and deleting
tasks are deferred to the final delivery.

**API contract**
`PATCH /api/v1/events/{eventId}/tasks/{taskId}`
```json
{ "status": "IN_PROGRESS", "assigneeMembershipId": 4 }
```
All fields optional: `status`, `assigneeMembershipId`, and `"unassign": true` to remove the assignee
(`unassign` together with `assigneeMembershipId` → 400). → `200 OK` with the task.
Errors: `400` (unknown status, `ASSIGNEE_NOT_IN_ORGANIZATION`) · `403` · `404` (task not in this event).

**Business rules**
- Organizer of the event's organization (service check).
- Any status change between `TODO`, `IN_PROGRESS`, `DONE` is allowed (also back to TODO).
- Same assignee rule as T-26.

**Files/folders to touch**
`organizing` service/controller/dto, tests.

**Acceptance criteria**
- [ ] Status and assignee can be changed separately and together.
- [ ] Task id of another event → 404; MEMBER → 403.
- [ ] Unit + integration tests.

**How to check it's done**
`./mvnw verify`; Swagger as `elena.marin`.

**Guide**
`how-to-add-a-feature.md` §3–§7.

## T-28: feat: GET /events/{id}/registrations (participant list for organizers)
- Points: 2
- Labels: type:feature, area:backend, workflow:W5, good-first-ticket
- Depends on: T-21

**API contract**
`GET /api/v1/events/{eventId}/registrations` → `200 OK`
```json
[ { "registrationId": 9, "userId": 7, "fullName": "Andrei Toma", "email": "andrei.toma@stud.ubbcluj.ro",
    "status": "CONFIRMED", "registeredAt": "2026-11-05T10:00:00Z", "checkedInAt": null } ]
```
Optional `?status=CONFIRMED` filter. Errors: `403` · `404`.

**Business rules**
- Only ORGANIZER/PRESIDENT of the event's organization (participants' emails are personal data).
- Sorted by full name.

**Files/folders to touch**
`RegistrationService`, `RegistrationController`, `registration/dto/ParticipantResponse.java`, tests.

**Acceptance criteria**
- [ ] `elena.marin` sees 3 participants of "Tech Career Fair Prep"; `ioana.rusu` → 403.
- [ ] Status filter works. Unit + integration tests.

**How to check it's done**
`./mvnw verify`; Swagger.

**Guide**
`how-to-add-a-feature.md` §3–§7.

## T-29: feat: POST /events/{id}/registrations/{registrationId}/check-in
- Points: 2
- Labels: type:feature, area:backend, workflow:W5, good-first-ticket, no-genai
- Depends on: T-28

**Context**
Organizers record attendance at the door (`Registration.checkedInAt`, data-model rule 5;
the database also enforces `ck_registrations_checkin_confirmed`). This is a `no-genai`
ticket: write it yourself and be ready to explain each rule.

**API contract**
`POST /api/v1/events/{eventId}/registrations/{registrationId}/check-in` (no body) → `200 OK`
with the `ParticipantResponse` from T-28 (`checkedInAt` set).
Errors: `403` · `404` (registration not found or belongs to another event) ·
`409 CHECK_IN_NOT_ALLOWED` · `409 ALREADY_CHECKED_IN`.

**Business rules**
- Caller is ORGANIZER/PRESIDENT of the event's organization.
- Event status must be `REGISTRATION_CLOSED` or `ONGOING`, else `CHECK_IN_NOT_ALLOWED`.
- Registration must be `CONFIRMED` (cancelled → `CHECK_IN_NOT_ALLOWED`).
- Already checked in → `ALREADY_CHECKED_IN` (no undo in the MVP).
- `checkedInAt = now`.

**Files/folders to touch**
`RegistrationService`, `RegistrationController`, `Registration` (a `checkIn(Instant)` method), tests.

**Acceptance criteria**
- [ ] Shakespeare Reading Marathon (ONGOING, Theatre Club): `radu.pop` checks in `maria.dobre` → 200; again → 409 `ALREADY_CHECKED_IN`.
- [ ] PUBLISHED event → 409 `CHECK_IN_NOT_ALLOWED`; cancelled registration → 409; `elena.marin` (other org) → 403.
- [ ] Unit tests for every rule + integration test.

**How to check it's done**
`./mvnw verify`; Swagger as `radu.pop@stud.ubbcluj.ro`.

**Guide**
`how-to-add-a-feature.md` §3–§7; `docs/data-model.md` rule 5.

## T-30: feat: GET/POST /events/{id}/announcements
- Points: 2
- Labels: type:feature, area:backend, workflow:W5, good-first-ticket
- Depends on: T-18

**Context**
Announcements are shown only in the app (assumption A-08). Editing/deleting is deferred to the final delivery.

**API contract**
`GET /api/v1/events/{eventId}/announcements` → `200 OK`, newest first
```json
[ { "id": 1, "title": "Bring your laptop", "body": "Please install JDK 21 and an IDE before the workshop.",
    "authorName": "Elena Marin", "createdAt": "2026-11-06T10:00:00Z" } ]
```
`POST /api/v1/events/{eventId}/announcements` `{ "title": "Room changed", "body": "We moved to room 5/I." }`
→ `201 Created`. Errors: `400` · `403` · `404` · `409 ANNOUNCEMENT_NOT_ALLOWED`.

**Business rules**
- POST: ORGANIZER/PRESIDENT of the event's organization; author = caller; not allowed on DRAFT or COMPLETED events → 409 `ANNOUNCEMENT_NOT_ALLOWED`.
- GET: any member of the organization **or** a user with a CONFIRMED or CANCELLED registration for the event; others → 403.
- title 1–200, body not blank.

**Files/folders to touch**
`organizing` controller/service/dto/mapper, tests.

**Acceptance criteria**
- [ ] `andrei.toma` (registered) reads the seed announcement of "Intro to Spring Boot Workshop"; a user without relation → 403.
- [ ] `elena.marin` posts → 201; `ioana.rusu` posts → 403.
- [ ] Unit + integration tests.

**How to check it's done**
`./mvnw verify`; Swagger.

**Guide**
`how-to-add-a-feature.md` §1–§7; `docs/data-model.md` → announcements.

## T-31: feat: tasks panel on the organizer event page
- Points: 3
- Labels: type:feature, area:frontend, workflow:W5
- Depends on: T-03, T-15, T-26, T-27

**What to do**
In the Tasks area of `/organizing/events/:id`: table of tasks (title, assignee, due date,
status), a "New task" form (title, description, due date, assignee select from
`GET /organizations/{id}/members`), a status select and an assignee select per row.

**Files/folders to touch**
`src/api/tasks.ts`, `src/api/types.ts`, `features/organizing/queries.ts`,
`features/organizing/TasksPanel.tsx`, `NewTaskForm.tsx`, tests.

**Acceptance criteria**
- [ ] Seed tasks of "Intro to Spring Boot Workshop" appear with assignee and status.
- [ ] Creating, assigning and changing status update the table without reload.
- [ ] Empty state "No tasks yet"; tests for the form and the status change.

**How to check it's done**
Lint/test/build; browser as `elena.marin`.

**Guide**
`frontend-guide.md` §6, §8.

## T-32: feat: participants and check-in panel
- Points: 2
- Labels: type:feature, area:frontend, workflow:W5, good-first-ticket
- Depends on: T-15, T-28, T-29

**What to do**
In the Participants area: count "x / capacity confirmed, y checked in", a search box by
name, a table (name, email, status, checked-in time) and a "Check in" button on confirmed,
not-yet-checked-in rows, enabled only when the event is REGISTRATION_CLOSED or ONGOING.

**Files/folders to touch**
`src/api/registrations.ts`, `features/organizing/ParticipantsPanel.tsx`, queries, test.

**Acceptance criteria**
- [ ] As `radu.pop` on "Shakespeare Reading Marathon": check in `maria.dobre`; counts update.
- [ ] Buttons disabled on a PUBLISHED event with a tooltip explaining why.
- [ ] Test for the button states and the search filter.

**How to check it's done**
Lint/test/build; browser.

**Guide**
`frontend-guide.md` §6, §8.

## T-33: feat: post an announcement (organizer)
- Points: 1
- Labels: type:feature, area:frontend, workflow:W5, good-first-ticket
- Depends on: T-15, T-30

**What to do**
In the Announcements area of the organizer event page: list of announcements and a form
(title, body). After posting, the list refreshes and the form clears.

**Files/folders to touch**
`src/api/announcements.ts`, `features/organizing/AnnouncementsPanel.tsx`, queries, test.

**Acceptance criteria**
- [ ] `elena.marin` posts an announcement and sees it at the top.
- [ ] Empty fields are rejected on the client; test for the form.

**How to check it's done**
Lint/test/build; browser.

**Guide**
`frontend-guide.md` §6, §8.

## T-34: feat: show announcements on the event details page
- Points: 1
- Labels: type:feature, area:frontend, workflow:W5, good-first-ticket
- Depends on: T-20, T-30

**What to do**
On `/events/:id`, show an "Announcements" list (title, body, author, date) when the user is
allowed to read them. If the backend answers 403, show nothing (not an error).

**Files/folders to touch**
`src/api/announcements.ts`, `features/events/EventAnnouncements.tsx`, `EventDetailsPage.tsx`, test.

**Acceptance criteria**
- [ ] `andrei.toma` sees "Bring your laptop" on "Intro to Spring Boot Workshop".
- [ ] A user without access sees no section and no error; test for both cases.

**How to check it's done**
Lint/test/build; browser.

**Guide**
`frontend-guide.md` §6, §8.

# Test tickets (`type:test`)

Feature tickets already include basic tests. These tickets add dedicated suites for the
most important business rules (R11), so they can be shown in the mid-term as evidence.

## T-35: test: capacity limit under concurrent registrations
- Points: 3
- Labels: type:test, area:backend, workflow:W4
- Depends on: T-21

**Context**
Rule 1 in `docs/data-model.md`: two students registering for the last place at the same
time must not both succeed. T-21 locks the event row; this test proves it works against a
real PostgreSQL (Testcontainers).

**What to do**
Integration test that creates an event with capacity 1 (or N) and N+k users, then fires
registrations in parallel (`ExecutorService` + `CountDownLatch` so they start together), each in
its own transaction (call the service or the HTTP endpoint, not a single test transaction).

**Files/folders to touch**
`backend/src/test/java/ro/ubb/events/registration/RegistrationConcurrencyTest.java`, using `support/IntegrationTestBase`.

**Acceptance criteria**
- [ ] With capacity 1 and 10 parallel attempts: exactly 1 succeeds, 9 get `EVENT_FULL`, DB count of CONFIRMED = 1.
- [ ] Same with capacity 5 and 20 attempts.
- [ ] Test is stable: run it 5 times locally (`./mvnw -Dtest=RegistrationConcurrencyTest test`) without failure.
- [ ] A short comment explains why it would fail without the lock.

**How to check it's done**
`./mvnw verify` green locally and in CI.

**Guide**
`how-to-add-a-feature.md` §7 (integration tests), §"Running the tests".

## T-36: test: registration window, duplicates and cancel/re-register
- Points: 2
- Labels: type:test, area:backend, workflow:W4, good-first-ticket
- Depends on: T-21, T-22

**What to do**
One test class covering every W4 rule with clear test names (one rule per test):
DRAFT / REGISTRATION_CLOSED / ONGOING / CANCELLED event → `REGISTRATION_CLOSED`; deadline
passed; no deadline but start passed; already registered → `ALREADY_REGISTERED`; cancel then
register again → new CONFIRMED row and the old one stays CANCELLED; cancel after start →
`CANCEL_NOT_ALLOWED`; cancel when not registered → `NOT_REGISTERED`.

**Files/folders to touch**
`backend/src/test/java/ro/ubb/events/registration/RegistrationRulesTest.java` (Mockito, fixed `Clock` if the service uses one) and/or an integration test.

**Acceptance criteria**
- [ ] Every rule above has its own test with a descriptive name (`@DisplayName`).
- [ ] Re-register test checks both rows in the DB.

**How to check it's done**
`./mvnw verify`.

**Guide**
`how-to-add-a-feature.md` §6, §7.

## T-37: test: event status transition matrix
- Points: 2
- Labels: type:test, area:backend, workflow:W2, good-first-ticket
- Depends on: T-12

**What to do**
A `@ParameterizedTest` over all 6×6 pairs of `EventStatus` that asserts allowed vs. rejected
exactly as in the table in T-12, plus the date checks (publish with past start, start too
early, complete before `endAt`).

**Files/folders to touch**
`backend/src/test/java/ro/ubb/events/event/EventLifecycleTest.java`.

**Acceptance criteria**
- [ ] 36 combinations covered; expected results listed in one readable table (`@CsvSource`).
- [ ] Date rules covered with a fixed clock or relative dates.
- [ ] The test table matches the state machine in `docs/midterm/05-workflows.md` §5.2 (if written).

**How to check it's done**
`./mvnw verify`.

**Guide**
`how-to-add-a-feature.md` §6; `docs/data-model.md` → "Status lifecycle (W2)".

## T-38: test: permission matrix integration tests
- Points: 3
- Labels: type:test, area:backend, workflow:W1, workflow:W2, workflow:W5
- Depends on: T-04, T-05, T-08, T-10, T-12, T-26, T-29, T-30

**Context**
R2 asks for access control. This suite checks every write endpoint against each actor, using
real tokens (like `OrgPermissionPreAuthorizeIntegrationTest`).

**What to do**
For each endpoint below, assert the status for: no token (401), outsider student, MEMBER,
ORGANIZER, PRESIDENT, ADMIN. Endpoints: add/remove member, create organization, create event,
change status, create task, check-in, post announcement.

**Files/folders to touch**
`backend/src/test/java/ro/ubb/events/security/PermissionMatrixIntegrationTest.java` (new package in tests only).

**Acceptance criteria**
- [ ] One parameterized test per endpoint; expected results match the permission matrix in `docs/midterm/03-actors-roles.md` §3.2 (or the feature tickets if §3.2 is not written yet).
- [ ] ADMIN gets 403 on organization-level actions except add/remove member and create organization.

**How to check it's done**
`./mvnw verify`; link the test class in the "Enforced by" column of the permission matrix.

**Guide**
`how-to-add-a-feature.md` §5, §7.

# Design tickets (`type:design`)

## T-39: design: logo
- Points: 2
- Labels: type:design, area:frontend, area:docs, good-first-ticket
- Depends on: none

**Context**
`frontend/src/components/Logo.tsx` and `frontend/public/logo-placeholder.svg` are placeholders.

**What to do**
Design a simple logo (icon + "UBB Events" wordmark) that works at 32 px and on the primary colour.
Do not use the official UBB logo (trademark). Export SVG; put the source file (Figma link,
Inkscape SVG, …) next to it.

**Files/folders to touch**
`docs/design/logo.md` (idea, variants, usage), `docs/assets/logo/`, `frontend/public/logo.svg`,
`frontend/index.html` (favicon), `Logo.tsx`.

**Acceptance criteria**
- [ ] Logo visible in the app bar, drawer, login page and as favicon.
- [ ] Readable at 32 px; light and dark (on primary) variants.
- [ ] `docs/design/logo.md` explains the choice.

**How to check it's done**
`npm run build`; open the app and check the four places.

**Guide**
`frontend-guide.md` §9 "Theme and logo"; `CONTRIBUTING.md` §2 "Non-code tickets".

## T-40: design: colour palette and theme.ts
- Points: 2
- Labels: type:design, area:frontend, area:docs, good-first-ticket
- Depends on: none

**What to do**
Choose primary, secondary, background, and status colours (success/warning/error/info) and
event-status chip colours. Check text contrast (WCAG AA, 4.5:1) with a contrast checker.
Update only the values in `frontend/src/theme.ts` (components use `theme.palette.*`).

**Files/folders to touch**
`frontend/src/theme.ts`, `docs/design/palette.md` (swatches, hex codes, contrast results).

**Acceptance criteria**
- [ ] All palette colours documented with contrast ratios; all pass AA for normal text.
- [ ] No hard-coded colours added elsewhere; lint/test/build pass.

**How to check it's done**
Run the app and look at login, organizations and a form page.

**Guide**
`frontend-guide.md` §9.

## T-41: design: wireframes of the student screens
- Points: 2
- Labels: type:design, area:docs, workflow:W3, workflow:W4, good-first-ticket
- Depends on: none

**What to do**
Low-fidelity wireframes (paper photo, Excalidraw, Figma, …) for: events list with filters,
event details with Register button and announcements, My registrations. Desktop and mobile
for the events list. Note which information comes from which API field (T-16, T-18, T-23).

**Files/folders to touch**
`docs/design/wireframes.md` (section "Student screens"), images in `docs/assets/wireframes/`.

**Acceptance criteria**
- [ ] 3 screens (+ mobile list) with short notes.
- [ ] Each screen lists its use cases (UC-30–32, UC-40–42 from `04-use-cases.md`).

**How to check it's done**
Images render in the GitHub preview of `wireframes.md`.

**Guide**
`CONTRIBUTING.md` §2 "Non-code tickets".

## T-42: design: wireframes of the organizer screens
- Points: 2
- Labels: type:design, area:docs, workflow:W2, workflow:W5, good-first-ticket
- Depends on: none

**What to do**
Wireframes for: Organizing page (events of my organizations), create/edit event form,
organizer event page with status actions and the Tasks / Participants (check-in) /
Announcements areas. Think about check-in on a phone at the door.

**Files/folders to touch**
`docs/design/wireframes.md` (section "Organizer screens"), `docs/assets/wireframes/`.

**Acceptance criteria**
- [ ] 4 screens incl. a mobile check-in view, with notes and linked UCs (UC-20–24, UC-50–54).

**How to check it's done**
GitHub preview renders the images.

**Guide**
`CONTRIBUTING.md` §2.

## T-43: design: wireframes of organization and admin screens
- Points: 1
- Labels: type:design, area:docs, workflow:W1, good-first-ticket
- Depends on: none

**What to do**
Wireframes for the organization details page with the Members section (add/remove for
presidents) and the admin "Create organization" form.

**Files/folders to touch**
`docs/design/wireframes.md` (section "Organizations and admin"), `docs/assets/wireframes/`.

**Acceptance criteria**
- [ ] 2 screens with notes and linked UCs (UC-04, UC-10–13).

**How to check it's done**
GitHub preview renders the images.

**Guide**
`CONTRIBUTING.md` §2.

## T-44: design: UI polish – empty states and loading
- Points: 2
- Labels: type:design, area:frontend, good-first-ticket
- Depends on: T-06, T-14, T-19, T-25

**What to do**
Go through every page and make empty and loading states consistent: one reusable
`EmptyState` component (icon, text, optional action button) in `src/components`, skeletons or
`LoadingState` for lists, no layout jumps.

**Files/folders to touch**
`frontend/src/components/EmptyState.tsx` (+ test), pages under `src/features/*`.

**Acceptance criteria**
- [ ] Every list page uses `EmptyState` with a helpful text and, where useful, an action (e.g. "Browse events").
- [ ] Loading states look the same everywhere.
- [ ] Screenshots before/after in the PR.

**How to check it's done**
Lint/test/build; browse as a user with no data (`andrei.toma` for organizing) and with data.

**Guide**
`frontend-guide.md` §3, §6.

## T-45: design: UI polish – friendly error messages
- Points: 2
- Labels: type:design, area:frontend, good-first-ticket
- Depends on: T-15, T-24

**What to do**
Map the backend business error codes (table in the header of this file) to short,
friendly texts in one place (e.g. `src/api/errorMessages.ts`) and use it in `ErrorAlert` and
the forms. Unknown codes fall back to the backend message.

**Files/folders to touch**
`frontend/src/api/errors.ts` / new `errorMessages.ts` (+ test), `components/ErrorAlert.tsx`.

**Acceptance criteria**
- [ ] Every code in the header table has a text; unit test for the mapping and the fallback.
- [ ] Register on a full event shows e.g. "Sorry, this event is full." instead of the raw message.

**How to check it's done**
Lint/test/build; trigger `EVENT_FULL` and `INVALID_STATUS_TRANSITION` in the browser.

**Guide**
`frontend-guide.md` §6 "Showing backend errors".

# Diagram tickets (`type:diagram`)

## T-46: docs: use case overview diagram
- Points: 1
- Labels: type:diagram, area:docs, good-first-ticket
- Depends on: T-76

**Context**
Section 4.3 of `docs/midterm/04-use-cases.md` (this ticket replaces the docs ticket of that section).

**What to do**
Draw the Mermaid `flowchart LR` described in §4.3: actors as `((Actor))`, use cases as
`([UC-NN Title])`, one `subgraph` per workflow, after the list in §4.2 is agreed.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` §4.3.

**Acceptance criteria**
- [ ] §4.3 "Done when" boxes ticked (every UC and actor appears; renders on GitHub).

**How to check it's done**
Open the file on GitHub (PR "Files changed" → "View file") and check the diagram renders.

**Guide**
`docs/midterm/README.md`; `docs/midterm/04-use-cases.md` §4.3.

## T-47: docs: sequence diagram W2 (create, publish, transitions)
- Points: 2
- Labels: type:diagram, area:docs, workflow:W2, good-first-ticket
- Depends on: none

**What to do**
Mermaid `sequenceDiagram` Organizer → Frontend → API → DB for: create draft
(`POST /organizations/{orgId}/events`), publish (`POST /events/{id}/status`), and a rejected
transition (`alt` block with 409 `INVALID_STATUS_TRANSITION`). Use the endpoints from T-10–T-12.
It is embedded in `05-workflows.md` §5.2 (the W2 docs ticket T-84 links to it).

**Files/folders to touch**
`docs/midterm/05-workflows.md` §5.2 (diagram block only).

**Acceptance criteria**
- [ ] ≤ 15 messages, renders on GitHub, endpoint names match the tickets/Swagger.

**How to check it's done**
GitHub preview.

**Guide**
`docs/midterm/05-workflows.md` (format example at the top).

## T-48: docs: sequence diagram W4 (registration with capacity check)
- Points: 2
- Labels: type:diagram, area:docs, workflow:W4, good-first-ticket
- Depends on: none

**What to do**
Mermaid `sequenceDiagram` for registering: lock event row, check window, check duplicate,
count CONFIRMED, insert; `alt` blocks for `REGISTRATION_CLOSED`, `EVENT_FULL`,
`ALREADY_REGISTERED`; and the cancel flow. Embedded in `05-workflows.md` §5.4.

**Files/folders to touch**
`docs/midterm/05-workflows.md` §5.4 (diagram block only).

**Acceptance criteria**
- [ ] Shows the lock and all three rejection paths; renders on GitHub.

**How to check it's done**
GitHub preview.

**Guide**
`docs/midterm/05-workflows.md`; `docs/data-model.md` rules 1–2.

## T-49: docs: review the event state machine against the code
- Points: 1
- Labels: type:diagram, area:docs, workflow:W2, good-first-ticket
- Depends on: T-12, T-84

**What to do**
Compare the `stateDiagram-v2` in `05-workflows.md` §5.2 with the transition table
implemented in T-12 (and the matrix test T-37 if merged) and with `docs/data-model.md`
"Status lifecycle". Fix any difference in the docs, or open a bug (bug template) if the code is wrong.

**Files/folders to touch**
`docs/midterm/05-workflows.md` §5.2, `docs/data-model.md` (only if outdated).

**Acceptance criteria**
- [ ] Every transition in code appears in the diagram with trigger and actor, and vice versa.
- [ ] The PR description lists what was checked and what changed.

**How to check it's done**
Reviewer checks the PR list against `EventStatus` / the lifecycle class.

**Guide**
`docs/midterm/README.md` ("Rules for all sections").

## T-50: docs: review the ER diagram against migrations and entities
- Points: 1
- Labels: type:diagram, area:docs, good-first-ticket
- Depends on: T-30

**What to do**
Check the Mermaid ER diagram in `docs/data-model.md` against all Flyway migrations
(`V1__…` and any newer ones) and the JPA entities: tables, columns, nullability, relations,
cardinalities. Fix the diagram if needed; if nothing changed, say so in the PR.

**Files/folders to touch**
`docs/data-model.md`.

**Acceptance criteria**
- [ ] Every table/column in the migrations is in the diagram; relations have the right cardinality.
- [ ] The PR lists the checks done.

**How to check it's done**
GitHub preview renders the diagram.

**Guide**
`docs/data-model.md`; `CONTRIBUTING.md` §2.

# Research tickets (`type:research`)

## T-51: research: review the interview guide
- Points: 1
- Labels: type:research, area:docs, good-first-ticket
- Depends on: none

**Context**
`docs/midterm/06-ux-research.md` §6.2 already has 12 student and 12 organizer questions. The
team must agree on them **before** the first interview.

**What to do**
Read the questions with the team (meeting or PR comments). Keep them open and about past
behaviour, remove leading questions, check that every assumption A-01–A-13 in §6.5 is covered
by at least one question (add a "covers A-NN" note per question). Fix the consent note in §6.1 if needed.

**Files/folders to touch**
`docs/midterm/06-ux-research.md` §6.2 (and the consent note in §6.1).

**Acceptance criteria**
- [ ] §6.2 "Done when" boxes ticked.
- [ ] At least 3 teammates approved the PR or commented in it.

**How to check it's done**
Each A-NN appears next to at least one question.

**Guide**
`docs/midterm/README.md`; `docs/midterm/06-ux-research.md` §6.2.

## T-52: research: Conduct 2 user interviews (#1)
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-51

**Context**
Part C requires interviews with real users (R5, R10). Each of the 5 interview tickets is one
teammate's pair of interviews; together they give 10 interviews (target in §6.1: at least 3
students and 2 organizers).

**What to do**
1. Find **1 student** and **1 event organizer** (member of a student organization who
   organized at least one event). Do not interview teammates.
2. Read the consent note (§6.1) and continue only with a "yes".
3. Use the agreed guide (§6.2), 20–30 minutes, take written notes.
4. Within 24 h, write the anonymised notes with the template from `interviews/README.md`
   as `docs/midterm/interviews/I-01.md` and `I-02.md` (IDs reserved for this ticket).
5. Add the two rows to the interview log table in `06-ux-research.md` §6.3.

**Files/folders to touch**
`docs/midterm/interviews/I-01.md`, `I-02.md`, `docs/midterm/06-ux-research.md` §6.3.

**Acceptance criteria**
- [ ] Two interviews done with consent (one student, one organizer).
- [ ] Notes contain no names, emails or other identifying data.
- [ ] Notes list which assumptions (A-NN) the answers touch.
- [ ] Two log rows link to the notes files.

**How to check it's done**
Reviewer reads the notes and checks for personal data; links in §6.3 open the files.

**Guide**
`docs/midterm/interviews/README.md`; `docs/midterm/06-ux-research.md` §6.1–§6.3.

## T-53: research: Conduct 2 user interviews (#2)
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-51

**Context**
Part C requires interviews with real users (R5, R10). Each of the 5 interview tickets is one
teammate's pair of interviews; together they give 10 interviews (target in §6.1: at least 3
students and 2 organizers).

**What to do**
1. Find **1 student** and **1 event organizer** (member of a student organization who
   organized at least one event). Do not interview teammates.
2. Read the consent note (§6.1) and continue only with a "yes".
3. Use the agreed guide (§6.2), 20–30 minutes, take written notes.
4. Within 24 h, write the anonymised notes with the template from `interviews/README.md`
   as `docs/midterm/interviews/I-03.md` and `I-04.md` (IDs reserved for this ticket).
5. Add the two rows to the interview log table in `06-ux-research.md` §6.3.

**Files/folders to touch**
`docs/midterm/interviews/I-03.md`, `I-04.md`, `docs/midterm/06-ux-research.md` §6.3.

**Acceptance criteria**
- [ ] Two interviews done with consent (one student, one organizer).
- [ ] Notes contain no names, emails or other identifying data.
- [ ] Notes list which assumptions (A-NN) the answers touch.
- [ ] Two log rows link to the notes files.

**How to check it's done**
Reviewer reads the notes and checks for personal data; links in §6.3 open the files.

**Guide**
`docs/midterm/interviews/README.md`; `docs/midterm/06-ux-research.md` §6.1–§6.3.

## T-54: research: Conduct 2 user interviews (#3)
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-51

**Context**
Part C requires interviews with real users (R5, R10). Each of the 5 interview tickets is one
teammate's pair of interviews; together they give 10 interviews (target in §6.1: at least 3
students and 2 organizers).

**What to do**
1. Find **1 student** and **1 event organizer** (member of a student organization who
   organized at least one event). Do not interview teammates.
2. Read the consent note (§6.1) and continue only with a "yes".
3. Use the agreed guide (§6.2), 20–30 minutes, take written notes.
4. Within 24 h, write the anonymised notes with the template from `interviews/README.md`
   as `docs/midterm/interviews/I-05.md` and `I-06.md` (IDs reserved for this ticket).
5. Add the two rows to the interview log table in `06-ux-research.md` §6.3.

**Files/folders to touch**
`docs/midterm/interviews/I-05.md`, `I-06.md`, `docs/midterm/06-ux-research.md` §6.3.

**Acceptance criteria**
- [ ] Two interviews done with consent (one student, one organizer).
- [ ] Notes contain no names, emails or other identifying data.
- [ ] Notes list which assumptions (A-NN) the answers touch.
- [ ] Two log rows link to the notes files.

**How to check it's done**
Reviewer reads the notes and checks for personal data; links in §6.3 open the files.

**Guide**
`docs/midterm/interviews/README.md`; `docs/midterm/06-ux-research.md` §6.1–§6.3.

## T-55: research: Conduct 2 user interviews (#4)
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-51

**Context**
Part C requires interviews with real users (R5, R10). Each of the 5 interview tickets is one
teammate's pair of interviews; together they give 10 interviews (target in §6.1: at least 3
students and 2 organizers).

**What to do**
1. Find **1 student** and **1 event organizer** (member of a student organization who
   organized at least one event). Do not interview teammates.
2. Read the consent note (§6.1) and continue only with a "yes".
3. Use the agreed guide (§6.2), 20–30 minutes, take written notes.
4. Within 24 h, write the anonymised notes with the template from `interviews/README.md`
   as `docs/midterm/interviews/I-07.md` and `I-08.md` (IDs reserved for this ticket).
5. Add the two rows to the interview log table in `06-ux-research.md` §6.3.

**Files/folders to touch**
`docs/midterm/interviews/I-07.md`, `I-08.md`, `docs/midterm/06-ux-research.md` §6.3.

**Acceptance criteria**
- [ ] Two interviews done with consent (one student, one organizer).
- [ ] Notes contain no names, emails or other identifying data.
- [ ] Notes list which assumptions (A-NN) the answers touch.
- [ ] Two log rows link to the notes files.

**How to check it's done**
Reviewer reads the notes and checks for personal data; links in §6.3 open the files.

**Guide**
`docs/midterm/interviews/README.md`; `docs/midterm/06-ux-research.md` §6.1–§6.3.

## T-56: research: Conduct 2 user interviews (#5)
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-51

**Context**
Part C requires interviews with real users (R5, R10). Each of the 5 interview tickets is one
teammate's pair of interviews; together they give 10 interviews (target in §6.1: at least 3
students and 2 organizers).

**What to do**
1. Find **1 student** and **1 event organizer** (member of a student organization who
   organized at least one event). Do not interview teammates.
2. Read the consent note (§6.1) and continue only with a "yes".
3. Use the agreed guide (§6.2), 20–30 minutes, take written notes.
4. Within 24 h, write the anonymised notes with the template from `interviews/README.md`
   as `docs/midterm/interviews/I-09.md` and `I-10.md` (IDs reserved for this ticket).
5. Add the two rows to the interview log table in `06-ux-research.md` §6.3.

**Files/folders to touch**
`docs/midterm/interviews/I-09.md`, `I-10.md`, `docs/midterm/06-ux-research.md` §6.3.

**Acceptance criteria**
- [ ] Two interviews done with consent (one student, one organizer).
- [ ] Notes contain no names, emails or other identifying data.
- [ ] Notes list which assumptions (A-NN) the answers touch.
- [ ] Two log rows link to the notes files.

**How to check it's done**
Reviewer reads the notes and checks for personal data; links in §6.3 open the files.

**Guide**
`docs/midterm/interviews/README.md`; `docs/midterm/06-ux-research.md` §6.1–§6.3.

## T-57: docs: research method + consent
- Points: 1
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-52, T-53, T-54, T-55, T-56

**What to do**
Write §6.1 of `06-ux-research.md`: who was interviewed (types, how many, how they were
found), when, how long, in person or online, who took notes; confirm consent for every interview.

**Files/folders to touch**
`docs/midterm/06-ux-research.md` §6.1.

**Acceptance criteria**
- [ ] §6.1 "Done when" boxes ticked; 100–200 words.

**How to check it's done**
Numbers match the interview log in §6.3.

**Guide**
`docs/midterm/README.md`; `06-ux-research.md` §6.1.

## T-58: docs: synthesise interview findings
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-52, T-53, T-54, T-55, T-56

**What to do**
Read all `interviews/I-*.md` and write the findings table in §6.4: 5–10 findings, each with
evidence (interview IDs, short anonymised quotes) and the consequence for the product
(FR / UC / workflow step, or "no change" with a reason). Best done as a short team
session (affinity mapping on sticky notes), then written down by the ticket owner.

**Files/folders to touch**
`docs/midterm/06-ux-research.md` §6.4.

**Acceptance criteria**
- [ ] §6.4 "Done when" boxes ticked.
- [ ] Findings supported by a single interview are marked as such.

**How to check it's done**
Every quote can be found in a notes file.

**Guide**
`docs/midterm/06-ux-research.md` §6.4.

## T-59: docs: assumptions we changed
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-58

**What to do**
Fill the assumptions table in §6.5 (A-01–A-13, plus new rows): interview evidence,
keep / change / open, and for `change` what was changed (file, FR, issue number). If a change
needs code, open a feature ticket (feature template) and link it; it can be done for the final delivery.

**Files/folders to touch**
`docs/midterm/06-ux-research.md` §6.5; issues for changes.

**Acceptance criteria**
- [ ] §6.5 "Done when" boxes ticked.
- [ ] At least one assumption that was challenged is discussed honestly (even if kept, say why).

**How to check it's done**
Every `change` row has a link that exists.

**Guide**
`docs/midterm/06-ux-research.md` §6.5.

## T-60: research: personas
- Points: 2
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-58

**What to do**
Add a new section **6.7 Personas** to `06-ux-research.md` with 2–3 personas built only from
the findings (e.g. a student participant, an organizer, a president). Per persona: name
(fictional), short description, goals, frustrations, typical scenario, which workflows they use,
and the findings (F-NN) and interviews (I-NN) they are based on. No stock photos of real people.

**Files/folders to touch**
`docs/midterm/06-ux-research.md` (new §6.7, with a "Done when" list like the other sections);
add it to the index table in `docs/midterm/README.md`.

**Acceptance criteria**
- [ ] 2–3 personas, each citing F-IDs and I-IDs.
- [ ] Each persona maps to actors in `03-actors-roles.md`.

**How to check it's done**
Reviewer checks every persona trait against a finding.

**Guide**
`docs/midterm/README.md`.

## T-61: docs: main product flows from research
- Points: 1
- Labels: type:research, area:docs, good-first-ticket
- Depends on: T-58

**What to do**
Write §6.6: the 3–5 most important user flows ordered by importance according to the
findings, each with steps and `Implemented by: W?, UC-…`.

**Files/folders to touch**
`docs/midterm/06-ux-research.md` §6.6.

**Acceptance criteria**
- [ ] §6.6 "Done when" boxes ticked.

**How to check it's done**
Each flow's UCs exist in `04-use-cases.md` §4.2.

**Guide**
`docs/midterm/06-ux-research.md` §6.6.

# Documentation tickets (`type:docs`)

Part **B** of the mid-term deliverable. One ticket per `**Ticket:**` marker in `docs/midterm/01–05`, with the title and points given there. Section 4.3 is covered by the diagram ticket T-46; section 4.6 is split into W2 and W3 as the file allows.

## T-62: docs: write problem statement
- Points: 2
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **1.1 Problem** of [`docs/midterm/01-problem-statement.md`](../midterm/01-problem-statement.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Describe the problem from the users' point of view, not the solution. How do student
organizations at UBB organize events today, and what goes wrong (scattered information,
manual registration lists, overbooking, no attendance record, …)? Why does it matter?
Base every claim on the interviews or on your own experience as UBB students, and say
which one it is. End with one or two sentences on what the system will change.

You can start from team experience as UBB students (say so as the source); add interview IDs once T-58 is merged (checked again in T-88).
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/01-problem-statement.md` → section 1.1

**Acceptance criteria**
- [ ] The problem is described without naming technologies or features.
- [ ] Each pain point names its source (interview ID or team experience).
- [ ] 150–300 words.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-63: docs: list stakeholders
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **1.2 Stakeholders** of [`docs/midterm/01-problem-statement.md`](../midterm/01-problem-statement.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
List everyone who is affected by or has an interest in the system: people who use it
(students, organization members, organizers, presidents, platform admin) and people who
do not use it directly (e.g. the university, sponsors, course staff). For each, write
what they want from the system. Keep it consistent with the actors in `03-actors-roles.md`
(an actor is a stakeholder who uses the system).
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/01-problem-statement.md` → section 1.2

**Acceptance criteria**
- [ ] Every actor from `03-actors-roles.md` appears as a stakeholder.
- [ ] At least one stakeholder who does not use the system directly is listed.
- [ ] Each row has a concrete goal (not "use the app").
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-64: docs: define project scope
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **1.3 Scope (in scope)** of [`docs/midterm/01-problem-statement.md`](../midterm/01-problem-statement.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
List what the system will do by the end of the project, grouped by workflow W1–W5 plus
accounts/admin. One line per capability, no implementation details. Mark what is already
part of the mid-term MVP. This list should match the functional requirement groups in
`02-requirements.md`.

Mark as MVP what the mid-term tickets in `docs/planning/midterm-tickets.md` deliver; items in its "Deferred to final" list are in scope but not MVP.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/01-problem-statement.md` → section 1.3

**Acceptance criteria**
- [ ] Every workflow W1–W5 has at least one bullet.
- [ ] MVP items are marked and agreed with the team.
- [ ] Matches the FR groups in `02-requirements.md`.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-65: docs: define out of scope
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-64

**Context**
Section **1.4 Out of scope** of [`docs/midterm/01-problem-statement.md`](../midterm/01-problem-statement.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
List what the system deliberately will **not** do, and one short reason for each. This
protects the team from scope creep and shows conscious decisions. Include things that
were decided in the data model and ADRs (e.g. no waitlist, no university SSO) and things
users may expect but we skip (e.g. payments, email notifications). If an item may come
later, say "not in this project" or "later, if time allows".
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/01-problem-statement.md` → section 1.4

**Acceptance criteria**
- [ ] Each item has a reason and a source.
- [ ] No item contradicts the in-scope list or `02-requirements.md`.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-66: docs: map course requirements R1–R16
- Points: 2
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-67, T-68, T-69, T-70, T-71, T-72, T-73

**Context**
Section **2.1 High-level requirements R1–R16 (course)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
The list below is copied from `docs/course/course-requirements.md` (a team summary of the
official project statement; do not reword it). Fill in the two extra columns: which of our
FR/NFR IDs cover each course requirement, and its overall status. This table is the top of
the traceability chain (R15): R → FR/NFR → UC → code → tests.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.1

**Acceptance criteria**
- [ ] Every R has at least one FR or NFR in "Covered by" (or a written reason why not).
- [ ] Status filled for all 16 rows and checked against `main`.
- [ ] Title and Summary columns unchanged from the course file.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-67: docs: FR for accounts and admin
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **2.2 Accounts and administration (FR-AUTH, FR-ADM)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Requirements for registering, logging in, seeing your own profile, and what the platform
admin can do (e.g. create organizations, assign presidents). Registration and login
already exist; describe them as they behave today (who can register, which role a new
account gets, what happens with a wrong password).
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.2

**Acceptance criteria**
- [ ] Status of each row checked against the auth tests.
- [ ] Admin rows agreed with the team (what an ADMIN may do is not fully decided yet).
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-68: docs: FR for W1 organizations
- Points: 2
- Labels: type:docs, area:docs, workflow:W1, good-first-ticket
- Depends on: none

**Context**
Section **2.3 W1 Organizations and membership (FR-W1)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Requirements for viewing organizations, creating/editing them, joining or being added as a
member, changing roles (PRESIDENT / ORGANIZER / MEMBER) and leaving. State who may do each
action. Listing and viewing organizations already exists (read-only).

The API contracts and business rules of the W1 feature tickets in `docs/planning/midterm-tickets.md` are the agreed behaviour; requirements not yet on `main` are `planned`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.3

**Acceptance criteria**
- [ ] Every row names who may perform the action.
- [ ] Role changes and leaving an organization are covered.
- [ ] Status checked against `main`.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-69: docs: FR for W2 event lifecycle
- Points: 2
- Labels: type:docs, area:docs, workflow:W2, good-first-ticket
- Depends on: none

**Context**
Section **2.4 W2 Event creation and lifecycle (FR-W2)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Requirements for creating, editing, publishing, closing registration, starting, completing
and cancelling an event. Include the field rules (capacity > 0, end after start, optional
deadline ≤ start), the allowed status transitions, and who may perform them. Mention that
events with registrations are cancelled, never deleted.

The API contracts and business rules of the W2 feature tickets in `docs/planning/midterm-tickets.md` are the agreed behaviour; requirements not yet on `main` are `planned`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.4

**Acceptance criteria**
- [ ] Every status transition in `EventStatus` is covered by a requirement.
- [ ] Field validation rules are listed.
- [ ] Consistent with the W2 state machine in `05-workflows.md`.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-70: docs: FR for W3 discovery
- Points: 1
- Labels: type:docs, area:docs, workflow:W3, good-first-ticket
- Depends on: none

**Context**
Section **2.5 W3 Event discovery and search (FR-W3)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Requirements for listing upcoming events, filtering (category, organization, date),
text search, sorting, and what an event details page shows so a student can decide to
participate (free places, deadline, location). State which events are visible to whom
(e.g. DRAFT only for organizers).

The API contracts and business rules of the W3 feature tickets in `docs/planning/midterm-tickets.md` are the agreed behaviour; requirements not yet on `main` are `planned`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.5

**Acceptance criteria**
- [ ] Visibility of each event status is stated.
- [ ] Filters match fields that exist in the data model (or a schema change is noted).
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-71: docs: FR for W4 registration
- Points: 2
- Labels: type:docs, area:docs, workflow:W4, good-first-ticket
- Depends on: none

**Context**
Section **2.6 W4 Participant registration (FR-W4)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Requirements for registering, cancelling, re-registering and seeing "my registrations".
Include the business rules: capacity limit without overbooking, registration window
(PUBLISHED and before deadline/start), at most one confirmed registration per user and
event, no waitlist. Say what the user sees when a rule blocks them.

The API contracts and business rules of the W4 feature tickets in `docs/planning/midterm-tickets.md` are the agreed behaviour; requirements not yet on `main` are `planned`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.6

**Acceptance criteria**
- [ ] Capacity, window, duplicate and cancel/re-register rules each have a row.
- [ ] The "what the user sees" part is stated for rejected registrations.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-72: docs: FR for W5 organizing
- Points: 2
- Labels: type:docs, area:docs, workflow:W5, good-first-ticket
- Depends on: none

**Context**
Section **2.7 W5 Event organization (FR-W5)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Requirements for organizer tasks (create, assign to a member, change status), attendance
check-in, viewing the registration list, and announcements to participants. State who may
do each, and rules such as "assignee must be a member of the event's organization" and
"only confirmed registrations can be checked in".

The API contracts and business rules of the W5 feature tickets in `docs/planning/midterm-tickets.md` are the agreed behaviour; requirements not yet on `main` are `planned`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.7

**Acceptance criteria**
- [ ] Tasks, check-in, participant list and announcements are each covered.
- [ ] Each row is justified by a user need or a data-model rule (Source column).
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-73: docs: write non-functional requirements
- Points: 2
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **2.8 Non-functional requirements (NFR)** of [`docs/midterm/02-requirements.md`](../midterm/02-requirements.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Quality requirements that are measurable or checkable: security (JWT, BCrypt, 401/403),
API conventions and documentation, data integrity (constraints, migrations), testing and
CI, deployment with Docker, usability/accessibility, performance targets you can actually
check, maintainability (feature packages). Each NFR must say how it is verified.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/02-requirements.md` → section 2.8

**Acceptance criteria**
- [ ] Every NFR has a concrete "How it is verified".
- [ ] R7, R8, R9, R11, R12 are each covered by at least one NFR.
- [ ] No vague wording ("fast", "user-friendly") without a measure.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-74: docs: describe actors
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-63

**Context**
Section **3.1 Actors** of [`docs/midterm/03-actors-roles.md`](../midterm/03-actors-roles.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Describe each kind of user of the system and the external systems it talks to (if any).
Explain the two levels of roles: the **global** role on the account (STUDENT, ADMIN) and the
**organization** role inside one organization (MEMBER, ORGANIZER, PRESIDENT). Note that a
student can have different roles in different organizations, and that a visitor (not
logged in) is also an actor.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/03-actors-roles.md` → section 3.1

**Acceptance criteria**
- [ ] Both role levels are explained with one sentence each.
- [ ] Every value of `GlobalRole` and `OrganizationRole` appears, plus Visitor.
- [ ] Consistent with stakeholders in `01-problem-statement.md`.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-75: docs: permission matrix
- Points: 2
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-67, T-68, T-69, T-70, T-71, T-72, T-74

**Context**
Section **3.2 Permission matrix** of [`docs/midterm/03-actors-roles.md`](../midterm/03-actors-roles.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
One row per action (grouped by workflow W1–W5 plus accounts/admin), one column per actor.
Mark what each actor may do. "Organizer" etc. always means *of the organization that owns
the event*. Add the **Enforced by** column: the exact check in code (or `planned` if the
endpoint does not exist yet). The actions should match the use cases in `04-use-cases.md`.

For planned endpoints, use the permission rules written in the feature tickets and put `planned` in *Enforced by*; T-38 later adds the test class name.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/03-actors-roles.md` → section 3.2

**Acceptance criteria**
- [ ] Every row has an FR ID and an "Enforced by" value (code reference or `planned`).
- [ ] Rows marked implemented were checked against the code/tests.
- [ ] ADMIN rights inside organizations are stated explicitly (not assumed).
- [ ] Reviewed by the teammate who owns `SecurityConfig` / `OrgPermissionService`.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-76: docs: agree use case list
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-75

**Context**
Section **4.2 Use case list** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
The titles below are a **proposal** derived from the data model and workflows. As a team,
add, remove or merge titles before writing them, fill the Actor column and keep this list
in sync with the detailed sections. Each detailed section (4.3–4.8) is its own ticket.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.2

**Acceptance criteria**
- [ ] The team agreed on the list (titles, owners).
- [ ] Every FR in `02-requirements.md` is linked from at least one use case.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-77: docs: use cases AUTH/ADM
- Points: 1
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-76

**Context**
Section **4.4 Accounts and admin use cases (UC-01–09)** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Detailed use cases for the AUTH/ADM rows of 4.2, using the template. Login and register
already exist: describe the real behaviour, including the error flows (wrong password →
401, email already used → 409).
Extra sources: `auth/` package, `backend/src/test/java/ro/ubb/events/auth/*`, `frontend/src/features/auth/`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.4

**Acceptance criteria**
- [ ] One section per UC-01–09 in the list, using the template.
- [ ] Error flows match the API responses in Swagger.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-78: docs: use cases W1
- Points: 2
- Labels: type:docs, area:docs, workflow:W1, good-first-ticket
- Depends on: T-76

**Context**
Section **4.5 W1 use cases (UC-10–19)** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Detailed use cases for W1 using the template. Alternative flows must cover permission
denials (403) and the "one membership per user per organization" rule.
Extra sources: `organization/` package, `OrgPermissionService`, `docs/data-model.md` memberships.

Use cases for actions deferred to the final delivery (change role, leave, edit organization) stay in the list with status `planned`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.5

**Acceptance criteria**
- [ ] One section per W1 use case in the list.
- [ ] Every alternative flow names the rule or check that causes it.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-79: docs: use cases W2
- Points: 2
- Labels: type:docs, area:docs, workflow:W2, good-first-ticket
- Depends on: T-76

**Context**
Section **4.6 W2 and W3 use cases (UC-20–39)** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Detailed use cases for event lifecycle and discovery. Lifecycle use cases must match the
state machine in `05-workflows.md` (W2) and say which status the event has before and after.
Extra sources: `event/` package, `EventStatus`, data-model.md lifecycle and W3 indexes.

**This ticket covers only the W2 use cases (UC-20–29).** W3 is T-80.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.6

**Acceptance criteria**
- [ ] One section per W2 use case in the list (UC-20–29).
- [ ] Pre/postconditions state the event status, consistent with the W2 state machine.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-80: docs: use cases W3
- Points: 1
- Labels: type:docs, area:docs, workflow:W3, good-first-ticket
- Depends on: T-76

**Context**
Section **4.6 W2 and W3 use cases (UC-20–39)** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Detailed use cases for event lifecycle and discovery. Lifecycle use cases must match the
state machine in `05-workflows.md` (W2) and say which status the event has before and after.
Extra sources: `event/` package, `EventStatus`, data-model.md lifecycle and W3 indexes.

**This ticket covers only the W3 use cases (UC-30–39).** W2 is T-79.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.6

**Acceptance criteria**
- [ ] One section per W3 use case in the list (UC-30–39).
- [ ] Visibility rules (who sees DRAFT events) are in the W3 flows.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-81: docs: use cases W4
- Points: 2
- Labels: type:docs, area:docs, workflow:W4, good-first-ticket
- Depends on: T-76

**Context**
Section **4.7 W4 use cases (UC-40–49)** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Detailed use cases for registration. Alternative flows must cover: full event, deadline
passed, event not PUBLISHED, already registered, cancel then register again.
Extra sources: `registration/` package, data-model.md registrations + service rules 1–2.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.7

**Acceptance criteria**
- [ ] One section per W4 use case in the list.
- [ ] All five alternative flows above appear.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-82: docs: use cases W5
- Points: 2
- Labels: type:docs, area:docs, workflow:W5, good-first-ticket
- Depends on: T-76

**Context**
Section **4.8 W5 use cases (UC-50–59)** of [`docs/midterm/04-use-cases.md`](../midterm/04-use-cases.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Detailed use cases for organizer tasks, participant list, check-in and announcements.
Alternative flows: assignee not in the organization, check-in of a cancelled registration,
non-organizer tries an organizer action.
Extra sources: `organizing/` package, `Registration.checkedInAt`, data-model.md rules 3 and 5.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/04-use-cases.md` → section 4.8

**Acceptance criteria**
- [ ] One section per W5 use case in the list.
- [ ] Every use case is linked to at least one FR-W5 ID.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-83: docs: workflow W1
- Points: 2
- Labels: type:docs, area:docs, workflow:W1, good-first-ticket
- Depends on: T-78

**Context**
Section **5.1 W1 Organizations and membership** of [`docs/midterm/05-workflows.md`](../midterm/05-workflows.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
From an organization being created (by an admin) to members joining, roles being assigned
and members leaving. Show how an organization role later unlocks W2/W5 actions.

Draw the diagram yourself as a simple Mermaid `flowchart TD` (there is no separate diagram ticket for this workflow).
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/05-workflows.md` → section 5.1

**Acceptance criteria**
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] Implemented steps (list/view organizations) are distinguished from planned ones.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-84: docs: workflow W2 + event state machine
- Points: 3
- Labels: type:docs, area:docs, workflow:W2
- Depends on: T-79, T-47

**Context**
Section **5.2 W2 Event creation and lifecycle** of [`docs/midterm/05-workflows.md`](../midterm/05-workflows.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
From an organizer creating a draft to the event being completed or cancelled: who triggers
each transition, which checks apply (dates, required fields, permissions), and what happens
to registrations when an event is cancelled.

The sequence diagram comes from T-47 (already in §5.2): do not draw another one; write the steps, rules, traceability line and the `stateDiagram-v2`.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/05-workflows.md` → section 5.2

**Acceptance criteria**
- [ ] W2 steps, diagram, business rules and traceability line are present.
- [ ] All six `EventStatus` values and all allowed transitions appear, matching data-model.md.
- [ ] Each transition names its trigger and actor (manual or automatic).
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-85: docs: workflow W3
- Points: 1
- Labels: type:docs, area:docs, workflow:W3, good-first-ticket
- Depends on: T-80

**Context**
Section **5.3 W3 Event discovery and search** of [`docs/midterm/05-workflows.md`](../midterm/05-workflows.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
From a student opening the events page to deciding whether to register: listing, filtering,
searching, opening details. Show which events are visible (statuses) and what information
supports the decision.

Draw the diagram yourself as a simple Mermaid `flowchart TD` (there is no separate diagram ticket for this workflow).
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/05-workflows.md` → section 5.3

**Acceptance criteria**
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] The hand-off to W4 ("Register" button) is shown.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-86: docs: workflow W4
- Points: 2
- Labels: type:docs, area:docs, workflow:W4, good-first-ticket
- Depends on: T-81, T-48

**Context**
Section **5.4 W4 Participant registration** of [`docs/midterm/05-workflows.md`](../midterm/05-workflows.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Register, see my registrations, cancel and register again. Explain how the capacity limit
is guaranteed when two students register for the last place at the same time.

The sequence diagram comes from T-48 (already in §5.4): do not draw another one.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/05-workflows.md` → section 5.4

**Acceptance criteria**
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] The concurrency/capacity rule and the rejection paths are shown.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-87: docs: workflow W5
- Points: 2
- Labels: type:docs, area:docs, workflow:W5, good-first-ticket
- Depends on: T-82, T-58

**Context**
Section **5.5 W5 Event organization** of [`docs/midterm/05-workflows.md`](../midterm/05-workflows.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
What organizers do between publishing and completing an event: split work into tasks and
assign them, follow the participant list, post announcements, check in participants at
the door. Show how this connects to the event status (e.g. check-in while ONGOING).

Draw the diagram yourself as a simple Mermaid `flowchart TD` (there is no separate diagram ticket for this workflow).
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/05-workflows.md` → section 5.5

**Acceptance criteria**
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] Tasks, announcements and check-in all appear.
- [ ] At least one step is justified by an interview finding (R5).
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-88: docs: final review of the mid-term docs
- Points: 3
- Labels: type:docs, area:docs
- Depends on: T-46, T-49, T-50, T-57, T-58, T-59, T-60, T-61, T-62, T-63, T-64, T-65, T-66, T-67, T-68, T-69, T-70, T-71, T-72, T-73, T-74, T-75, T-76, T-77, T-78, T-79, T-80, T-81, T-82, T-83, T-84, T-85, T-86, T-87, T-90, T-91, T-92, T-93

**Context**
Before submission, the documentation must be consistent and must match the code on `main`
(`docs/midterm/README.md` → "Rules for all sections").

**What to do**
Read `docs/midterm/01–07` end to end (best with a second teammate) and fix:
1. **Status** columns (FR, NFR, UC, R1–R16, permission matrix) re-checked against `main` on the review day.
2. **IDs and links**: every UC links FRs, every FR is used by a UC, R → FR/NFR → UC → code → tests chain (R15) complete; no dangling `F-`, `I-`, `A-` references.
3. Every section's *Done when* boxes are really met; no `> TODO (team):` marker or `Example – replace` block left.
4. Consistent terms (actor names, status names) and English spelling.
5. Remove the "What to write / Where to find / Format" instruction blocks (the team agreed the files are final).
6. Problem statement (T-62) cites interview IDs where possible.

**Files/folders to touch**
`docs/midterm/*.md` (small fixes only; open an issue for anything larger).

**Acceptance criteria**
- [ ] `grep -rn "TODO (team)\|Example – replace" docs/midterm` returns nothing.
- [ ] Statuses checked against `main`, date of the check written in `docs/midterm/README.md`.
- [ ] All Mermaid diagrams render on GitHub; all relative links work.
- [ ] A second teammate approved the PR.

**How to check it's done**
Run the grep above; click every link in the GitHub preview of `docs/midterm/README.md`.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md).

## T-89: docs: assemble the mid-term document and export it to PDF
- Points: 2
- Labels: type:docs, area:docs, good-first-ticket
- Depends on: T-88, T-94, T-95, T-96, T-97, T-98

**What to do**
Produce one PDF with parts A–E in the order of `docs/midterm/README.md`: title page (project,
team, course, date, repository link), table of contents, part A (short summary of the code
state: what runs, how to start it, screenshots of the main pages, link to the repo/tag),
B (01–05), C (06 + list of interview notes), D (07), E (08). Mermaid diagrams must appear as
images: export them (GitHub preview screenshot, `mermaid.live`, or `npx @mermaid-js/mermaid-cli`).
Possible tool: `pandoc` + a Mermaid filter, or copy into a Google Doc / Word and export.
Write the exact steps in a short `docs/midterm/EXPORT.md` so the final delivery can repeat them.

**Files/folders to touch**
`docs/midterm/EXPORT.md`, `docs/midterm/midterm-<team>.pdf` (or attach to the release; agree in the PR), screenshots in `docs/assets/midterm/`.

**Acceptance criteria**
- [ ] PDF contains A–E, a table of contents and readable diagrams/tables.
- [ ] Every page number in the TOC is right; links to the repository work.
- [ ] `EXPORT.md` lets another teammate regenerate the PDF.

**How to check it's done**
Another teammate opens the PDF and compares it with `docs/midterm/README.md`'s index.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md); `docs/course/course-requirements.md` (A–E).

# Report tickets (`type:report`)

Parts **D** (GenAI declaration) and **E** (individual contributions).

## T-90: docs: GenAI tools and purposes
- Points: 1
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **7.1 Tools and models used** of [`docs/midterm/07-genai-report.md`](../midterm/07-genai-report.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
List every GenAI tool and model the team used (including chat tools used for small tasks)
and, for each, what it was used for and by whom. Use the exact tool/model names from the
usage log. Say briefly why that tool was chosen for that purpose.

Before writing, ask every teammate to add missing rows to `docs/genai/usage-log.md` (one row per GenAI task). Do this ticket late, after most feature tickets are merged, so later AI use is included; re-check in T-88.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/07-genai-report.md` → section 7.1

**Acceptance criteria**
- [ ] Every distinct Tool/Model value in `usage-log.md` appears.
- [ ] Every team member who used GenAI is listed.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-91: docs: GenAI artifacts list
- Points: 2
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **7.2 Artifacts generated or modified with GenAI** of [`docs/midterm/07-genai-report.md`](../midterm/07-genai-report.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Which files or parts of the system were generated or changed with GenAI, and how much of
each was AI output vs. human work. Group by area (docs, backend, database, frontend,
infrastructure). Be honest: "fully generated, reviewed" and "human-written, AI suggested
one function" are both fine, but must be distinguished.

Before writing, ask every teammate to add missing rows to `docs/genai/usage-log.md` (one row per GenAI task). Do this ticket late, after most feature tickets are merged, so later AI use is included; re-check in T-88.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/07-genai-report.md` → section 7.2

**Acceptance criteria**
- [ ] Every usage-log row is represented by at least one artifact.
- [ ] Each artifact links to its usage-log row and, where it exists, a session file.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-92: docs: GenAI verification process
- Points: 2
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **7.3 How we verified AI-generated code and documentation** of [`docs/midterm/07-genai-report.md`](../midterm/07-genai-report.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
Describe the general verification process (planning first and answering open decisions,
reviewing diffs, running tests and CI, manual checks in Swagger/UI/psql, PR review by a
teammate), then give 3–5 concrete examples with the actual check and its result. Mention
cases where verification found a problem.

Before writing, ask every teammate to add missing rows to `docs/genai/usage-log.md` (one row per GenAI task). Do this ticket late, after most feature tickets are merged, so later AI use is included; re-check in T-88.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/07-genai-report.md` → section 7.3

**Acceptance criteria**
- [ ] The process covers automated (tests/CI) and manual checks.
- [ ] At least 3 concrete examples with evidence links.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-93: docs: GenAI human decisions
- Points: 2
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Section **7.4 Human decisions that materially changed the AI output** of [`docs/midterm/07-genai-report.md`](../midterm/07-genai-report.md). The section already contains the instructions, sources, format and an example; this ticket is about writing the real content.

**What to do**
The most important part for R14. Give 3–6 examples where a human decision changed,
corrected or rejected what the AI proposed or would have done: what the AI proposed,
what the human decided, why, and what changed in the result. Only cases with a material
effect (design, behaviour, security, scope), not wording fixes.

Before writing, ask every teammate to add missing rows to `docs/genai/usage-log.md` (one row per GenAI task). Do this ticket late, after most feature tickets are merged, so later AI use is included; re-check in T-88.
Follow the section's *Where to find the information* and *Format* exactly, replace the `> TODO (team):` marker and the `> **Example – replace:**` block.

**Files/folders to touch**
`docs/midterm/07-genai-report.md` → section 7.4

**Acceptance criteria**
- [ ] Each example states the AI proposal and the human decision separately.
- [ ] Each example has evidence (usage-log row + session file or PR).
- [ ] Examples come from more than one team member, if possible.
- [ ] Facts checked against the repository on `main` (no invented content); IDs follow the scheme in `docs/midterm/README.md`.

**How to check it's done**
The reviewer ticks the section's *Done when* boxes in the PR review (`docs/midterm/README.md` → "How to work on a section", step 6); Markdown tables and Mermaid render in the GitHub preview.

**Guide**
[`docs/midterm/README.md`](../midterm/README.md), [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2.

## T-94: docs: Individual contribution statement #1
- Points: 1
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Part E: each student declares their own contribution (R16). There are 5 identical tickets;
each teammate takes exactly one and fills **their own** section of
[`docs/midterm/08-individual-contributions.md`](../midterm/08-individual-contributions.md).

**What to do**
Fill every row of your table (owned artifacts, owned cross-layer workflow, implemented
functionality, documentation, tests, design decisions, GenAI-related work, others) with
checkable references: issue numbers, PR numbers, file paths, test classes, usage-log rows.
Mention your `no-genai` ticket. Write "none yet" instead of leaving a cell empty. Do it after
your last mid-term PR is merged.

**Files/folders to touch**
`docs/midterm/08-individual-contributions.md` (only your section).

**Acceptance criteria**
- [ ] Every field is filled (or "none yet").
- [ ] Every item has a reference (file, PR, issue or usage-log row).
- [ ] The owned workflow is cross-layer (UI + API + DB) and matches `05-workflows.md`.
- [ ] Another teammate reviewed the section in the PR.

**How to check it's done**
`gh pr list --repo PatriciuMina/GenAI-Project --author <your-login> --state merged` matches the PRs listed.

**Guide**
`docs/midterm/08-individual-contributions.md` (top of file), `docs/midterm/README.md`.

## T-95: docs: Individual contribution statement #2
- Points: 1
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Part E: each student declares their own contribution (R16). There are 5 identical tickets;
each teammate takes exactly one and fills **their own** section of
[`docs/midterm/08-individual-contributions.md`](../midterm/08-individual-contributions.md).

**What to do**
Fill every row of your table (owned artifacts, owned cross-layer workflow, implemented
functionality, documentation, tests, design decisions, GenAI-related work, others) with
checkable references: issue numbers, PR numbers, file paths, test classes, usage-log rows.
Mention your `no-genai` ticket. Write "none yet" instead of leaving a cell empty. Do it after
your last mid-term PR is merged.

**Files/folders to touch**
`docs/midterm/08-individual-contributions.md` (only your section).

**Acceptance criteria**
- [ ] Every field is filled (or "none yet").
- [ ] Every item has a reference (file, PR, issue or usage-log row).
- [ ] The owned workflow is cross-layer (UI + API + DB) and matches `05-workflows.md`.
- [ ] Another teammate reviewed the section in the PR.

**How to check it's done**
`gh pr list --repo PatriciuMina/GenAI-Project --author <your-login> --state merged` matches the PRs listed.

**Guide**
`docs/midterm/08-individual-contributions.md` (top of file), `docs/midterm/README.md`.

## T-96: docs: Individual contribution statement #3
- Points: 1
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Part E: each student declares their own contribution (R16). There are 5 identical tickets;
each teammate takes exactly one and fills **their own** section of
[`docs/midterm/08-individual-contributions.md`](../midterm/08-individual-contributions.md).

**What to do**
Fill every row of your table (owned artifacts, owned cross-layer workflow, implemented
functionality, documentation, tests, design decisions, GenAI-related work, others) with
checkable references: issue numbers, PR numbers, file paths, test classes, usage-log rows.
Mention your `no-genai` ticket. Write "none yet" instead of leaving a cell empty. Do it after
your last mid-term PR is merged.

**Files/folders to touch**
`docs/midterm/08-individual-contributions.md` (only your section).

**Acceptance criteria**
- [ ] Every field is filled (or "none yet").
- [ ] Every item has a reference (file, PR, issue or usage-log row).
- [ ] The owned workflow is cross-layer (UI + API + DB) and matches `05-workflows.md`.
- [ ] Another teammate reviewed the section in the PR.

**How to check it's done**
`gh pr list --repo PatriciuMina/GenAI-Project --author <your-login> --state merged` matches the PRs listed.

**Guide**
`docs/midterm/08-individual-contributions.md` (top of file), `docs/midterm/README.md`.

## T-97: docs: Individual contribution statement #4
- Points: 1
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Part E: each student declares their own contribution (R16). There are 5 identical tickets;
each teammate takes exactly one and fills **their own** section of
[`docs/midterm/08-individual-contributions.md`](../midterm/08-individual-contributions.md).

**What to do**
Fill every row of your table (owned artifacts, owned cross-layer workflow, implemented
functionality, documentation, tests, design decisions, GenAI-related work, others) with
checkable references: issue numbers, PR numbers, file paths, test classes, usage-log rows.
Mention your `no-genai` ticket. Write "none yet" instead of leaving a cell empty. Do it after
your last mid-term PR is merged.

**Files/folders to touch**
`docs/midterm/08-individual-contributions.md` (only your section).

**Acceptance criteria**
- [ ] Every field is filled (or "none yet").
- [ ] Every item has a reference (file, PR, issue or usage-log row).
- [ ] The owned workflow is cross-layer (UI + API + DB) and matches `05-workflows.md`.
- [ ] Another teammate reviewed the section in the PR.

**How to check it's done**
`gh pr list --repo PatriciuMina/GenAI-Project --author <your-login> --state merged` matches the PRs listed.

**Guide**
`docs/midterm/08-individual-contributions.md` (top of file), `docs/midterm/README.md`.

## T-98: docs: Individual contribution statement #5
- Points: 1
- Labels: type:report, area:docs, good-first-ticket
- Depends on: none

**Context**
Part E: each student declares their own contribution (R16). There are 5 identical tickets;
each teammate takes exactly one and fills **their own** section of
[`docs/midterm/08-individual-contributions.md`](../midterm/08-individual-contributions.md).

**What to do**
Fill every row of your table (owned artifacts, owned cross-layer workflow, implemented
functionality, documentation, tests, design decisions, GenAI-related work, others) with
checkable references: issue numbers, PR numbers, file paths, test classes, usage-log rows.
Mention your `no-genai` ticket. Write "none yet" instead of leaving a cell empty. Do it after
your last mid-term PR is merged.

**Files/folders to touch**
`docs/midterm/08-individual-contributions.md` (only your section).

**Acceptance criteria**
- [ ] Every field is filled (or "none yet").
- [ ] Every item has a reference (file, PR, issue or usage-log row).
- [ ] The owned workflow is cross-layer (UI + API + DB) and matches `05-workflows.md`.
- [ ] Another teammate reviewed the section in the PR.

**How to check it's done**
`gh pr list --repo PatriciuMina/GenAI-Project --author <your-login> --state merged` matches the PRs listed.

**Guide**
`docs/midterm/08-individual-contributions.md` (top of file), `docs/midterm/README.md`.

# QA tickets (`type:qa`)

## T-99: qa: manual test of all workflows W1–W5
- Points: 3
- Labels: type:qa, area:frontend, area:backend, workflow:W1, workflow:W2, workflow:W3, workflow:W4, workflow:W5
- Depends on: T-02, T-07, T-09, T-13, T-15, T-19, T-24, T-25, T-31, T-32, T-33, T-34

**Context**
Before the mid-term, one person tests the whole application like a user, on a clean
Docker setup, and reports every problem as a bug.

**What to do**
1. Fresh start: `docker compose down -v && docker compose up --build`, open http://localhost:3000.
2. Write a short test script in `docs/qa/midterm-manual-test.md` (table: step, account, expected, result) and run it:
   - **W1:** admin creates an organization with a president; president adds and removes a member; member sees the members list; outsider does not.
   - **W2:** organizer creates a draft, edits it, publishes it, closes registration, starts, completes; cancels another event; a member cannot see the Organizing menu.
   - **W3:** student searches and filters events; DRAFT events never appear; details show free places.
   - **W4:** register, see it in My registrations, cancel, register again; full event and closed registration show friendly messages; two browsers register for the last place.
   - **W5:** create and assign tasks, change status; participant list; check-in at an ONGOING event; post an announcement and read it as a participant.
   - Error handling: expired/missing token redirects to login; 403 pages; mobile width (375 px).
3. For every failure, open an issue with the **Bug** template (steps, expected, actual,
   screenshots) and link it in the result column.

**Files/folders to touch**
`docs/qa/midterm-manual-test.md`; bug issues on GitHub (bug template).

**Acceptance criteria**
- [ ] Every workflow W1–W5 has at least 3 steps executed with a recorded result.
- [ ] Every failed step links to a bug issue.
- [ ] Test date, commit hash of `main` and browser are written at the top.

**How to check it's done**
A teammate repeats 3 random steps and gets the same result.

**Guide**
`README.md` §2–§3 (Docker, seed accounts); `CONTRIBUTING.md` §7 (opening issues); `.github/ISSUE_TEMPLATE/bug.yml`.

# Deferred to final

Agreed with the team to keep the mid-term scope small. These are **not** mid-term tickets;
create them for the final delivery.

- **W1:** change a member's role; a member leaves an organization; the president edits organization details.
- **W5:** edit task title/description/due date and delete tasks (mid-term: create, assign, change status only); edit and delete announcements.
- **QA:** one manual-test ticket per workflow (mid-term: T-99 covers all workflows).
- **Diagrams:** separate sequence diagrams for W1, W3 and W5 (mid-term: simple Mermaid flows inside the 05 docs tickets).
- **UI polish** beyond T-44 and T-45 (e.g. accessibility audit, dark mode, animations).
- Already out of scope by design decisions (see `docs/data-model.md`): waitlist (D1), automatic time-based status transitions (manual transitions agreed for the MVP).

# Summary

| ID | Title | Points | Labels | Depends on |
|----|-------|--------|--------|------------|
| T-01 | feat: GET /users/me/memberships (my organizations and roles) | 2 | type:feature, area:backend, workflow:W1, good-first-ticket | none |
| T-02 | feat: show "Organizing" in the menu only to organizers and presidents | 1 | type:feature, area:frontend, workflow:W1, workflow:W5, good-first-ticket | T-01 |
| T-03 | feat: GET /organizations/{id}/members (members list) | 2 | type:feature, area:backend, workflow:W1, good-first-ticket | none |
| T-04 | feat: POST /organizations/{id}/members (president adds a member by email) | 2 | type:feature, area:backend, workflow:W1, good-first-ticket | T-03 |
| T-05 | feat: DELETE /organizations/{id}/members/{membershipId} (remove a member) | 1 | type:feature, area:backend, workflow:W1, good-first-ticket | T-03 |
| T-06 | feat: members list on the organization details page | 2 | type:feature, area:frontend, workflow:W1, good-first-ticket, no-genai | T-01, T-03 |
| T-07 | feat: add and remove members (president UI) | 2 | type:feature, area:frontend, workflow:W1, good-first-ticket | T-04, T-05, T-06 |
| T-08 | feat: POST /organizations (admin creates an organization and its president) | 3 | type:feature, area:backend, workflow:W1 | none |
| T-09 | feat: Admin page – create organization form | 2 | type:feature, area:frontend, workflow:W1, good-first-ticket | T-08 |
| T-10 | feat: POST /organizations/{orgId}/events (create a draft event) | 3 | type:feature, area:backend, workflow:W2 | none |
| T-11 | feat: PUT /events/{id} (edit an event) | 2 | type:feature, area:backend, workflow:W2, good-first-ticket | T-10 |
| T-12 | feat: POST /events/{id}/status (lifecycle transitions and cancel) | 3 | type:feature, area:backend, workflow:W2 | T-10 |
| T-13 | feat: create/edit event form | 3 | type:feature, area:frontend, workflow:W2 | T-10, T-11, T-18 |
| T-14 | feat: Organizing page – events of my organizations | 2 | type:feature, area:frontend, workflow:W2, good-first-ticket | T-01, T-17 |
| T-15 | feat: organizer event page with status actions | 2 | type:feature, area:frontend, workflow:W2, good-first-ticket | T-12, T-14, T-18 |
| T-16 | feat: GET /events (search with filters and paging) | 3 | type:feature, area:backend, workflow:W3 | T-10 |
| T-17 | feat: GET /organizations/{id}/events (events of one organization) | 2 | type:feature, area:backend, workflow:W3, good-first-ticket, no-genai | T-10 |
| T-18 | feat: GET /events/{id} (event details with free places) | 2 | type:feature, area:backend, workflow:W3, good-first-ticket | T-10 |
| T-19 | feat: events list with filters | 3 | type:feature, area:frontend, workflow:W3 | T-16 |
| T-20 | feat: event details page | 2 | type:feature, area:frontend, workflow:W3, good-first-ticket | T-18 |
| T-21 | feat: POST /events/{id}/registrations (register with capacity limit) | 3 | type:feature, area:backend, workflow:W4 | T-18 |
| T-22 | feat: DELETE /events/{id}/registrations/me (cancel my registration) | 2 | type:feature, area:backend, workflow:W4, good-first-ticket, no-genai | T-21 |
| T-23 | feat: GET /users/me/registrations (my registrations) | 2 | type:feature, area:backend, workflow:W4, good-first-ticket | T-18 |
| T-24 | feat: Register / Cancel button on the event details page | 2 | type:feature, area:frontend, workflow:W4, good-first-ticket | T-20, T-21, T-22, T-23 |
| T-25 | feat: My registrations page | 2 | type:feature, area:frontend, workflow:W4, good-first-ticket, no-genai | T-22, T-23 |
| T-26 | feat: GET/POST /events/{id}/tasks (list and create organizer tasks) | 2 | type:feature, area:backend, workflow:W5, good-first-ticket | T-03, T-18 |
| T-27 | feat: PATCH /events/{id}/tasks/{taskId} (assign and change status) | 2 | type:feature, area:backend, workflow:W5, good-first-ticket | T-26 |
| T-28 | feat: GET /events/{id}/registrations (participant list for organizers) | 2 | type:feature, area:backend, workflow:W5, good-first-ticket | T-21 |
| T-29 | feat: POST /events/{id}/registrations/{registrationId}/check-in | 2 | type:feature, area:backend, workflow:W5, good-first-ticket, no-genai | T-28 |
| T-30 | feat: GET/POST /events/{id}/announcements | 2 | type:feature, area:backend, workflow:W5, good-first-ticket | T-18 |
| T-31 | feat: tasks panel on the organizer event page | 3 | type:feature, area:frontend, workflow:W5 | T-03, T-15, T-26, T-27 |
| T-32 | feat: participants and check-in panel | 2 | type:feature, area:frontend, workflow:W5, good-first-ticket | T-15, T-28, T-29 |
| T-33 | feat: post an announcement (organizer) | 1 | type:feature, area:frontend, workflow:W5, good-first-ticket | T-15, T-30 |
| T-34 | feat: show announcements on the event details page | 1 | type:feature, area:frontend, workflow:W5, good-first-ticket | T-20, T-30 |
| T-35 | test: capacity limit under concurrent registrations | 3 | type:test, area:backend, workflow:W4 | T-21 |
| T-36 | test: registration window, duplicates and cancel/re-register | 2 | type:test, area:backend, workflow:W4, good-first-ticket | T-21, T-22 |
| T-37 | test: event status transition matrix | 2 | type:test, area:backend, workflow:W2, good-first-ticket | T-12 |
| T-38 | test: permission matrix integration tests | 3 | type:test, area:backend, workflow:W1, workflow:W2, workflow:W5 | T-04, T-05, T-08, T-10, T-12, T-26, T-29, T-30 |
| T-39 | design: logo | 2 | type:design, area:frontend, area:docs, good-first-ticket | none |
| T-40 | design: colour palette and theme.ts | 2 | type:design, area:frontend, area:docs, good-first-ticket | none |
| T-41 | design: wireframes of the student screens | 2 | type:design, area:docs, workflow:W3, workflow:W4, good-first-ticket | none |
| T-42 | design: wireframes of the organizer screens | 2 | type:design, area:docs, workflow:W2, workflow:W5, good-first-ticket | none |
| T-43 | design: wireframes of organization and admin screens | 1 | type:design, area:docs, workflow:W1, good-first-ticket | none |
| T-44 | design: UI polish – empty states and loading | 2 | type:design, area:frontend, good-first-ticket | T-06, T-14, T-19, T-25 |
| T-45 | design: UI polish – friendly error messages | 2 | type:design, area:frontend, good-first-ticket | T-15, T-24 |
| T-46 | docs: use case overview diagram | 1 | type:diagram, area:docs, good-first-ticket | T-76 |
| T-47 | docs: sequence diagram W2 (create, publish, transitions) | 2 | type:diagram, area:docs, workflow:W2, good-first-ticket | none |
| T-48 | docs: sequence diagram W4 (registration with capacity check) | 2 | type:diagram, area:docs, workflow:W4, good-first-ticket | none |
| T-49 | docs: review the event state machine against the code | 1 | type:diagram, area:docs, workflow:W2, good-first-ticket | T-12, T-84 |
| T-50 | docs: review the ER diagram against migrations and entities | 1 | type:diagram, area:docs, good-first-ticket | T-30 |
| T-51 | research: review the interview guide | 1 | type:research, area:docs, good-first-ticket | none |
| T-52 | research: Conduct 2 user interviews (#1) | 2 | type:research, area:docs, good-first-ticket | T-51 |
| T-53 | research: Conduct 2 user interviews (#2) | 2 | type:research, area:docs, good-first-ticket | T-51 |
| T-54 | research: Conduct 2 user interviews (#3) | 2 | type:research, area:docs, good-first-ticket | T-51 |
| T-55 | research: Conduct 2 user interviews (#4) | 2 | type:research, area:docs, good-first-ticket | T-51 |
| T-56 | research: Conduct 2 user interviews (#5) | 2 | type:research, area:docs, good-first-ticket | T-51 |
| T-57 | docs: research method + consent | 1 | type:research, area:docs, good-first-ticket | T-52, T-53, T-54, T-55, T-56 |
| T-58 | docs: synthesise interview findings | 2 | type:research, area:docs, good-first-ticket | T-52, T-53, T-54, T-55, T-56 |
| T-59 | docs: assumptions we changed | 2 | type:research, area:docs, good-first-ticket | T-58 |
| T-60 | research: personas | 2 | type:research, area:docs, good-first-ticket | T-58 |
| T-61 | docs: main product flows from research | 1 | type:research, area:docs, good-first-ticket | T-58 |
| T-62 | docs: write problem statement | 2 | type:docs, area:docs, good-first-ticket | none |
| T-63 | docs: list stakeholders | 1 | type:docs, area:docs, good-first-ticket | none |
| T-64 | docs: define project scope | 1 | type:docs, area:docs, good-first-ticket | none |
| T-65 | docs: define out of scope | 1 | type:docs, area:docs, good-first-ticket | T-64 |
| T-66 | docs: map course requirements R1–R16 | 2 | type:docs, area:docs, good-first-ticket | T-67, T-68, T-69, T-70, T-71, T-72, T-73 |
| T-67 | docs: FR for accounts and admin | 1 | type:docs, area:docs, good-first-ticket | none |
| T-68 | docs: FR for W1 organizations | 2 | type:docs, area:docs, workflow:W1, good-first-ticket | none |
| T-69 | docs: FR for W2 event lifecycle | 2 | type:docs, area:docs, workflow:W2, good-first-ticket | none |
| T-70 | docs: FR for W3 discovery | 1 | type:docs, area:docs, workflow:W3, good-first-ticket | none |
| T-71 | docs: FR for W4 registration | 2 | type:docs, area:docs, workflow:W4, good-first-ticket | none |
| T-72 | docs: FR for W5 organizing | 2 | type:docs, area:docs, workflow:W5, good-first-ticket | none |
| T-73 | docs: write non-functional requirements | 2 | type:docs, area:docs, good-first-ticket | none |
| T-74 | docs: describe actors | 1 | type:docs, area:docs, good-first-ticket | T-63 |
| T-75 | docs: permission matrix | 2 | type:docs, area:docs, good-first-ticket | T-67, T-68, T-69, T-70, T-71, T-72, T-74 |
| T-76 | docs: agree use case list | 1 | type:docs, area:docs, good-first-ticket | T-75 |
| T-77 | docs: use cases AUTH/ADM | 1 | type:docs, area:docs, good-first-ticket | T-76 |
| T-78 | docs: use cases W1 | 2 | type:docs, area:docs, workflow:W1, good-first-ticket | T-76 |
| T-79 | docs: use cases W2 | 2 | type:docs, area:docs, workflow:W2, good-first-ticket | T-76 |
| T-80 | docs: use cases W3 | 1 | type:docs, area:docs, workflow:W3, good-first-ticket | T-76 |
| T-81 | docs: use cases W4 | 2 | type:docs, area:docs, workflow:W4, good-first-ticket | T-76 |
| T-82 | docs: use cases W5 | 2 | type:docs, area:docs, workflow:W5, good-first-ticket | T-76 |
| T-83 | docs: workflow W1 | 2 | type:docs, area:docs, workflow:W1, good-first-ticket | T-78 |
| T-84 | docs: workflow W2 + event state machine | 3 | type:docs, area:docs, workflow:W2 | T-79, T-47 |
| T-85 | docs: workflow W3 | 1 | type:docs, area:docs, workflow:W3, good-first-ticket | T-80 |
| T-86 | docs: workflow W4 | 2 | type:docs, area:docs, workflow:W4, good-first-ticket | T-81, T-48 |
| T-87 | docs: workflow W5 | 2 | type:docs, area:docs, workflow:W5, good-first-ticket | T-82, T-58 |
| T-88 | docs: final review of the mid-term docs | 3 | type:docs, area:docs | T-46, T-49, T-50, T-57, T-58, T-59, T-60, T-61, T-62, T-63, T-64, T-65, T-66, T-67, T-68, T-69, T-70, T-71, T-72, T-73, T-74, T-75, T-76, T-77, T-78, T-79, T-80, T-81, T-82, T-83, T-84, T-85, T-86, T-87, T-90, T-91, T-92, T-93 |
| T-89 | docs: assemble the mid-term document and export it to PDF | 2 | type:docs, area:docs, good-first-ticket | T-88, T-94, T-95, T-96, T-97, T-98 |
| T-90 | docs: GenAI tools and purposes | 1 | type:report, area:docs, good-first-ticket | none |
| T-91 | docs: GenAI artifacts list | 2 | type:report, area:docs, good-first-ticket | none |
| T-92 | docs: GenAI verification process | 2 | type:report, area:docs, good-first-ticket | none |
| T-93 | docs: GenAI human decisions | 2 | type:report, area:docs, good-first-ticket | none |
| T-94 | docs: Individual contribution statement #1 | 1 | type:report, area:docs, good-first-ticket | none |
| T-95 | docs: Individual contribution statement #2 | 1 | type:report, area:docs, good-first-ticket | none |
| T-96 | docs: Individual contribution statement #3 | 1 | type:report, area:docs, good-first-ticket | none |
| T-97 | docs: Individual contribution statement #4 | 1 | type:report, area:docs, good-first-ticket | none |
| T-98 | docs: Individual contribution statement #5 | 1 | type:report, area:docs, good-first-ticket | none |
| T-99 | qa: manual test of all workflows W1–W5 | 3 | type:qa, area:frontend, area:backend, workflow:W1, workflow:W2, workflow:W3, workflow:W4, workflow:W5 | T-02, T-07, T-09, T-13, T-15, T-19, T-24, T-25, T-31, T-32, T-33, T-34 |

## Totals

| Category | Tickets | Points | 1–2 point tickets |
|----------|---------|--------|-------------------|
| `type:feature` | 34 | 72 | 26 |
| `type:test` | 4 | 10 | 2 |
| `type:design` | 7 | 13 | 7 |
| `type:diagram` | 5 | 7 | 5 |
| `type:research` | 11 | 19 | 11 |
| `type:docs` | 28 | 48 | 26 |
| `type:report` | 9 | 12 | 9 |
| `type:qa` | 1 | 3 | 0 |
| **Overall** | **99** | **184** | **86** |

About 37 points per team member (5 people). `no-genai` tickets: T-06, T-17, T-22, T-25, T-29.
