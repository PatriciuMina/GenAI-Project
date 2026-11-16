# 4. Use cases

Part **B.4** of the mid-term deliverable. Index: [README](README.md).

**ID blocks** (so writers do not collide): AUTH/ADM `UC-01–09`, W1 `UC-10–19`,
W2 `UC-20–29`, W3 `UC-30–39`, W4 `UC-40–49`, W5 `UC-50–59`. Never renumber; mark dropped
use cases as `dropped` in the list.

**Where to find the information (all use cases)**
- `02-requirements.md` (FR IDs to link), `03-actors-roles.md` (actors, permissions)
- `docs/data-model.md` → constraints and "Rules enforced in the service layer" (these are your alternative flows)
- Backend feature packages under `backend/src/main/java/ro/ubb/events/` and their tests
- `frontend/src/routes/paths.ts` and `frontend/src/features/*` (pages involved)
- `docs/course/course-requirements.md` (R1–R6, R10, R15)
- `06-ux-research.md` (real user steps)

---

## 4.1 Use case template

Copy this block for every use case. Length guideline: 15–30 lines per use case.

```markdown
### UC-NN: <verb + object>

| Field | Value |
|-------|-------|
| Workflow | W? |
| Primary actor | |
| Linked FR | FR-W?-NN, … |
| Preconditions | |
| Postconditions (success) | |

**Main flow**
1. …

**Alternative / error flows**
- 3a. <condition>: <what happens>

**Status:** implemented / partial / planned
```

> **Example – replace:**
>
> ### UC-41: Register for an event
>
> | Field | Value |
> |-------|-------|
> | Workflow | W4 |
> | Primary actor | Student |
> | Linked FR | FR-W4-01, FR-W4-02 |
> | Preconditions | Student is logged in; event "Intro to Spring Boot Workshop" is PUBLISHED and the deadline has not passed |
> | Postconditions (success) | A CONFIRMED registration exists; free places decrease by one |
>
> **Main flow**
> 1. Student opens the event details page.
> 2. Student presses "Register".
> 3. System checks the registration window and capacity, then saves the registration.
> 4. System shows "You are registered".
>
> **Alternative / error flows**
> - 3a. Event is full: system rejects the request and shows "No places left".
> - 3b. Student already has a confirmed registration: system shows "Already registered".
>
> **Status:** planned

---

## 4.2 Use case list

**Ticket:** `docs: agree use case list` · **Points:** 1

**What to write**
The titles below are a **proposal** derived from the data model and workflows. As a team,
add, remove or merge titles before writing them, fill the Actor column and keep this list
in sync with the detailed sections. Each detailed section (4.3–4.8) is its own ticket.

**Format**
Table: `ID | Title | Workflow | Primary actor | Linked FR | Status`.

**Done when**
- [ ] The team agreed on the list (titles, owners).
- [ ] Every FR in `02-requirements.md` is linked from at least one use case.

| ID | Title | Workflow | Primary actor | Linked FR | Status |
|----|-------|----------|---------------|-----------|--------|
| UC-01 | Create an account | AUTH | Visitor | FR-AUTH-01, FR-AUTH-02, FR-ADM-01, FR-AUTH-06 | implemented |
| UC-02 | Log in | AUTH | Visitor | FR-AUTH-03, FR-AUTH-04, FR-AUTH-06, FR-AUTH-07, FR-AUTH-08 | implemented |
| UC-03 | Log out | AUTH | Student / Administrator | FR-AUTH-07 | implemented |
| UC-04 | View my account | AUTH | Student / Administrator | FR-AUTH-05 | implemented |
| UC-05 | Create an organization and appoint its president | ADM | Administrator | FR-ADM-02, FR-ADM-04, FR-W1-03 | implemented |
| UC-10 | Browse organizations | W1 | Student | FR-W1-01, FR-W1-02 | implemented |
| UC-11 | View organization memberships and member list | W1 | President / Administrator | FR-ADM-03, FR-W1-04, FR-W1-05, FR-W1-06, FR-W1-07 | partial |
| UC-12 | Change a member's role | W1 | President | FR-W1-07, FR-W1-08 | planned |
| UC-13 | Leave an organization / remove a member | W1 | Member / President | FR-W1-06, FR-W1-09 | partial |
| UC-14 | Edit organization details | W1 | President | FR-W1-10 | planned |
| UC-20 | Create a draft event | W2 | Organizer | FR-W2-01, FR-W2-02, FR-W2-04 | implemented |
| UC-21 | Edit an event | W2 | Organizer | FR-W2-03, FR-W2-04 | implemented |
| UC-22 | Publish an event | W2 | Organizer | FR-W2-05 | planned |
| UC-23 | Close registration / start / complete an event | W2 | Organizer | FR-W2-06, FR-W2-07, FR-W2-08, FR-W2-10 | planned |
| UC-24 | Cancel an event | W2 | Organizer | FR-W2-09 | planned |
| UC-30 | Browse upcoming events | W3 | Student | FR-W3-01, FR-W3-02, FR-W3-03, FR-W3-06, FR-W3-07 | planned |
| UC-31 | Search and filter events | W3 | Student | FR-W3-02, FR-W3-03, FR-W3-06, FR-W3-07 | planned |
| UC-32 | View event details | W3 | Student | FR-W2-04, FR-W3-04, FR-W3-05, FR-W3-08 | partial |
| UC-40 | View my registrations | W4 | Student | FR-W4-07 | partial |
| UC-41 | Register for an event | W4 | Student | FR-W4-01, FR-W4-02, FR-W4-03, FR-W4-04, FR-W4-08 | partial |
| UC-42 | Cancel my registration | W4 | Student | FR-W4-05, FR-W4-06 | planned |
| UC-50 | Manage organizer tasks | W5 | Organizer | FR-W5-02, FR-W5-03, FR-W5-04, FR-W5-05 | partial |
| UC-51 | View the participant list | W5 | Organizer | FR-W5-01 | partial |
| UC-52 | Check in a participant | W5 | Organizer | FR-W5-07 | planned |
| UC-53 | Post and read announcements | W5 | Organizer / Student | FR-W5-08, FR-W5-09, FR-W5-10 | partial |
| UC-54 | Read announcements of my events | W5 | Student / Organizer | FR-W5-09 | partial |

---

## 4.3 Use case overview diagram

**Ticket:** `docs: use case overview diagram` · **Points:** 1

**What to write**
One diagram showing actors on the left and use cases grouped by workflow. Mermaid has no
UML use-case diagram, so use a `flowchart LR` with one `subgraph` per workflow. Draw it
after the list in 4.2 is agreed. If it becomes too big, make one diagram per workflow.

**Format**
Mermaid `flowchart LR`; actors as `((Actor))`, use cases as `([UC-NN Title])`.

> **Example – replace:**
>
> ```mermaid
> flowchart LR
>     student((Student))
>     subgraph W4[W4 Registration]
>         uc41([UC-41 Register for an event])
>     end
>     student --> uc41
> ```

**Done when**
- [ ] Every use case from 4.2 and every actor from `03-actors-roles.md` appears.
- [ ] Renders in the GitHub preview.

> TODO (team): overview diagram here.

---

## 4.4 Accounts and admin use cases (UC-01–09)

**Ticket:** `docs: use cases AUTH/ADM` · **Points:** 1

**What to write**
Detailed use cases for the AUTH/ADM rows of 4.2, using the template. Login and register
already exist: describe the real behaviour, including the error flows (wrong password →
401, email already used → 409).
Extra sources: `auth/` package, `backend/src/test/java/ro/ubb/events/auth/*`, `frontend/src/features/auth/`.

**Done when**
- [ ] One section per UC-01–09 in the list, using the template.
- [ ] Error flows match the API responses in Swagger.

> TODO (team): UC-01 … here.

---

## 4.5 W1 use cases (UC-10–19)

**Ticket:** `docs: use cases W1` · **Points:** 2

**What to write**
Detailed use cases for W1 using the template. Alternative flows must cover permission
denials (403) and the "one membership per user per organization" rule.
Extra sources: `organization/` package, `OrgPermissionService`, `docs/data-model.md` memberships.

**Done when**
- [ ] One section per W1 use case in the list.
- [ ] Every alternative flow names the rule or check that causes it.

### UC-10: Browse organizations

| Field | Value |
|-------|-------|
| Workflow | W1 |
| Primary actor | Student |
| Linked FR | FR-W1-01, FR-W1-02 |
| Preconditions | User is logged in; at least one organization exists |
| Postconditions (success) | User sees the organization list sorted by name and can choose an organization to inspect |

**Main flow**
1. Logged-in student opens the Organizations page.
2. System loads all organizations sorted by name.
3. System shows the organizations with their public details and a link to the organization page.
4. Student chooses an organization to review.

**Alternative / error flows**
- 3a. The user is not logged in: `SecurityConfig` requires a valid JWT, so the request is rejected with 401 instead of showing the list.
- 3b. No organizations exist yet: the system shows an empty-state message rather than an error.

**Status:** implemented

### UC-11: View organization memberships and member list

| Field | Value |
|-------|-------|
| Workflow | W1 |
| Primary actor | President / Administrator |
| Linked FR | FR-ADM-03, FR-W1-04, FR-W1-05, FR-W1-06, FR-W1-07 |
| Preconditions | User is logged in; the organization exists; the caller is a member of that organization or a global ADMIN |
| Postconditions (success) | User sees all memberships in the organization, ordered by role and then by full name |

**Main flow**
1. President or administrator opens the organization details page and selects the members view.
2. System validates that the caller is allowed to view this organization’s members.
3. System reads the memberships for the organization, sorts them by PRESIDENT, ORGANIZER, MEMBER and then by full name, and displays the list.
4. The page shows each member’s name, email, role and joined date.

**Alternative / error flows**
- 3a. Caller is neither a member of the organization nor a global ADMIN: `@PreAuthorize("hasRole('ADMIN') or @orgPermissions.isMember(principal.id, #orgId)")` denies access and the request returns 403.
- 3b. The organization id does not exist: `MembershipService.findByOrganizationId` throws `NotFoundException`; admins see 404, while non-members are blocked earlier by the 403 rule.
- 3c. A president tries to add a user who already belongs to the organization: the request is rejected with 409 `ALREADY_MEMBER` because `uq_memberships_user_organization` allows at most one membership row per `user_id + organization_id`.

**Status:** partial

### UC-12: Change a member's role

| Field | Value |
|-------|-------|
| Workflow | W1 |
| Primary actor | President |
| Linked FR | FR-W1-07, FR-W1-08 |
| Preconditions | User is the president of the organization; the target member exists in the same organization |
| Postconditions (success) | The target membership keeps the same identity but changes to the new organization role |

**Main flow**
1. President opens the organization members list and selects a member.
2. President chooses a new role for that member.
3. System validates that the caller is the organization president and that the target membership belongs to the same organization.
4. System updates the membership role and shows the new role in the list.

**Alternative / error flows**
- 3a. A non-president tries to change a role: the endpoint is protected by the president-only check, so the request is rejected with 403.
- 3b. The target membership belongs to a different organization, or the membership id is unknown: the service rejects the action because the member must belong to the organization being edited.
- 3c. The feature is not implemented yet in the current `main` branch: this use case remains `planned` and no role-update endpoint is available.

**Status:** planned

### UC-13: Leave an organization / remove a member

| Field | Value |
|-------|-------|
| Workflow | W1 |
| Primary actor | Member / President |
| Linked FR | FR-W1-06, FR-W1-09 |
| Preconditions | Caller is a member of the organization, or the president/admin is removing another member |
| Postconditions (success) | The caller’s membership is removed, or the selected member is deleted from the organization |

**Main flow**
1. Member chooses to leave the organization, or president chooses to remove a member from the organization.
2. System checks that the request is permitted for the caller.
3. System removes the membership record and confirms the result.
4. Tasks assigned to that membership become unassigned instead of being deleted, because the database uses `fk_event_tasks_assignee` with `SET NULL`.

**Alternative / error flows**
- 3a. An organizer or other non-president tries to remove someone: `@PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")` fails and the API returns 403.
- 3b. The president (the organization leader) is the target of the removal: `MembershipService.removeMember` throws 409 `CANNOT_REMOVE_PRESIDENT` because an organization always keeps its president.
- 3c. The target membership does not exist or belongs to another organization: `MembershipService.removeMember` throws 404 because the membership must belong to the requested `orgId`.
- 3d. Leaving is deferred to the final delivery: the app does not yet expose a member self-service "leave organization" flow, so this action remains `planned` even though the president/admin remove-member API is already implemented.

**Status:** planned

### UC-14: Edit organization details

| Field | Value |
|-------|-------|
| Workflow | W1 |
| Primary actor | President |
| Linked FR | FR-W1-10 |
| Preconditions | User is the president of the organization; the organization exists |
| Postconditions (success) | The organization name, description or contact information is updated and the new values are visible to users |

**Main flow**
1. President opens the organization details page and selects “Edit organization”.
2. System validates the current user is the president of that organization.
3. President changes the allowed organization fields and saves the form.
4. System persists the updated organization and refreshes the organization details.

**Alternative / error flows**
- 3a. A non-president attempts to edit the organization: the organization edit route is protected by the president-only check, so the request is denied with 403.
- 3b. The organization name is already taken, ignoring case: the create/update flow rejects the request because `ux_organizations_name_lower` enforces unique organization names.
- 3c. The endpoint is not implemented in the current codebase, so the behavior remains `planned` and is deferred to the final delivery.

**Status:** planned

---

## 4.6 W2 and W3 use cases (UC-20–39)

**Ticket:** `docs: use cases W2 + W3` · **Points:** 3

**What to write**
Detailed use cases for event lifecycle and discovery. Lifecycle use cases must match the
state machine in `05-workflows.md` (W2) and say which status the event has before and after.
Extra sources: `event/` package, `EventStatus`, data-model.md lifecycle and W3 indexes.
(Split into two tickets if two people own W2 and W3.)

**Done when**
- [ ] One section per W2/W3 use case in the list.
- [ ] Pre/postconditions state the event status.
- [ ] Visibility rules (who sees DRAFT events) are in the W3 flows.

### UC-20: Create a draft event

| Field | Value |
|-------|-------|
| Workflow | W2 |
| Primary actor | Organizer |
| Linked FR | FR-W2-01, FR-W2-02, FR-W2-04 |
| Preconditions | Organizer is allowed to manage an existing organization; the event form is complete enough to pass validation |
| Postconditions (success) | A new event row exists with status `DRAFT`; the organizer can open it, while non-organizers cannot see it |

**Main flow**
1. Organizer opens the organization’s event creation form and submits the event details.
2. System checks that the caller is an organizer of the selected organization.
3. System validates the event fields: title, location, category, start/end dates, capacity, optional registration deadline, and any cross-field rules.
4. System inserts the event with status `DRAFT` and returns the created event details.
5. System shows the organizer the new event, and the event remains hidden from other users until it is published.

**Alternative / error flows**
- 3a. Caller is not an organizer for that organization: the service denies access with 403 and the event is not created.
- 3b. One or more fields fail validation: the API responds with 400 `VALIDATION_FAILED` and names the invalid field(s).
- 3c. The organizer tries to create a draft for a non-existent organization: the request resolves to 404 because the organization must exist.

**Status:** implemented

### UC-21: Edit an event

| Field | Value |
|-------|-------|
| Workflow | W2 |
| Primary actor | Organizer |
| Linked FR | FR-W2-03, FR-W2-04 |
| Preconditions | Event exists; caller is the organizer of the event’s organization; event status is `DRAFT` or `PUBLISHED` |
| Postconditions (success) | The event keeps the same identity but its editable fields are updated; the event status remains `DRAFT` or `PUBLISHED` |

**Main flow**
1. Organizer opens an event they manage and selects “Edit”.
2. System verifies the organizer still owns the event’s organization and that the event is editable.
3. Organizer changes the allowed fields and submits the update together with the version they last read.
4. System checks that the new capacity is not below the number of confirmed registrations and validates the same field rules as creation.
5. System saves the event and returns the updated event payload.

**Alternative / error flows**
- 3a. Event status is `REGISTRATION_CLOSED`, `ONGOING`, `COMPLETED` or `CANCELLED`: the service rejects the edit with 409 `EVENT_NOT_EDITABLE`.
- 3b. Another organizer changed the event since the current page was loaded: the service rejects the update with 409 `CONCURRENT_UPDATE`.
- 3c. The new capacity is lower than the confirmed registrations: the service returns a validation error instead of reducing capacity below the booking count.

**Status:** implemented

### UC-22: Publish an event

| Field | Value |
|-------|-------|
| Workflow | W2 |
| Primary actor | Organizer |
| Linked FR | FR-W2-05 |
| Preconditions | Event exists; event status is `DRAFT`; caller is an organizer of the organization; `startAt` is in the future |
| Postconditions (success) | The event status changes from `DRAFT` to `PUBLISHED`; the event becomes visible to logged-in users and can enter its registration window |

**Main flow**
1. Organizer opens a draft event and chooses “Publish”.
2. System loads the event and confirms the user is an organizer of the organizing organization.
3. System checks the allowed transition from `DRAFT` to `PUBLISHED` and verifies that `startAt` is in the future.
4. System updates the event status to `PUBLISHED` and returns the updated event response.
5. The frontend shows the new published status and the event is now discoverable by users who are allowed to view public event information.

**Alternative / error flows**
- 3a. The event has already started or is not in the future: `EventLifecycle.rejectionReason` rejects the change with 409 `INVALID_STATUS_TRANSITION` and the reason `startAt must be in the future`.
- 3b. The caller is not the organizer: the API denies access with 403.
- 3c. The event is not currently `DRAFT`: the lifecycle rejects the transition because `DRAFT -> PUBLISHED` is the only valid publish path.

**Status:** implemented

### UC-23: Close registration / start / complete an event

| Field | Value |
|-------|-------|
| Workflow | W2 |
| Primary actor | Organizer |
| Linked FR | FR-W2-06, FR-W2-07, FR-W2-08, FR-W2-10 |
| Preconditions | Event exists; caller is organizer of the event’s organization; current status is one of the allowed predecessor states for the requested transition |
| Postconditions (success) | The event changes to the next status in the W2 lifecycle: `PUBLISHED -> REGISTRATION_CLOSED -> ONGOING -> COMPLETED` |

**Main flow**
1. Organizer opens the event details page for an event they manage.
2. Organizer selects a manual lifecycle action such as “Close registration”, “Start event”, or “Complete event”.
3. System reads the event row and validates that the caller is an organizer of the organization that owns the event.
4. System applies the transition using the lifecycle rules from `EventLifecycle`: `PUBLISHED -> REGISTRATION_CLOSED` is allowed immediately, `PUBLISHED or REGISTRATION_CLOSED -> ONGOING` is allowed only within one hour before `startAt`, and `ONGOING -> COMPLETED` is allowed only after `endAt`.
5. System persists the new status and shows the updated status in the organizer UI.

**Alternative / error flows**
- 3a. The requested transition is not allowed by the lifecycle: the service throws 409 `INVALID_STATUS_TRANSITION`.
- 3b. The event is still `DRAFT`: the organizer cannot skip directly to `ONGOING` or `COMPLETED`; the transition must follow the allowed path.
- 3c. The event is already `COMPLETED` or `CANCELLED`: no later lifecycle action is accepted because terminal states are not valid transition destinations.
- 3d. The organizer tries to start too early: the check rejects the action unless `now >= startAt - 1h`.
- 3e. The organizer tries to complete before the event end: the service blocks the action until `endAt` has been reached.

**Status:** implemented

### UC-24: Cancel an event

| Field | Value |
|-------|-------|
| Workflow | W2 |
| Primary actor | Organizer |
| Linked FR | FR-W2-09 |
| Preconditions | Event exists; caller is an organizer of the organization; current event status is not `COMPLETED` and not already `CANCELLED` |
| Postconditions (success) | The event status changes from its current non-terminal value to `CANCELLED`; registrations remain as historical data rather than being deleted |

**Main flow**
1. Organizer opens the event they manage and chooses “Cancel”.
2. System verifies the caller is an organizer of the event’s organization and that the event is cancellable.
3. System checks the lifecycle rule: `CANCELLED` is allowed from any non-terminal status except `COMPLETED`, and an event with registrations remains in the database as a cancelled event instead of being deleted.
4. System updates the status to `CANCELLED` and keeps the registration history intact.
5. System shows the cancelled status to the organizer and to participants who can still access the event details.

**Alternative / error flows**
- 3a. Event is already `COMPLETED`: lifecycle rule rejects the action because a completed event cannot be cancelled.
- 3b. Event is already `CANCELLED`: the same-status transition is rejected with 409 `INVALID_STATUS_TRANSITION`.
- 3c. The organizer is not allowed to manage this event: the request is denied with 403.
- 3d. The event has registrations: the service keeps them as history, but it never hard-deletes the event row or the registration records.

**Status:** implemented

### UC-30: Browse upcoming events

| Field | Value |
|-------|-------|
| Workflow | W3 |
| Primary actor | Student |
| Linked FR | FR-W3-01, FR-W3-07 |
| Preconditions | User is logged in; the page opens with the default search parameters and the list is loaded from the event search endpoint |
| Postconditions (success) | The user sees a page of upcoming events sorted from the earliest start time; only visible statuses appear, and each event card shows the minimum decision-making information |

**Main flow**
1. Logged-in student opens the Events page and the frontend calls the search endpoint without custom filters.
2. System reads every event whose status is `PUBLISHED`, `REGISTRATION_CLOSED` or `ONGOING`, whose `endAt` is not in the past, and whose title matches the current query if one exists.
3. System sorts the results by `startAt` ascending and paginates them, capping the page size at 50.
4. System shows event cards with the event title, organization, date/time, location, category and the current place information.
5. Student can choose an event to open its details page or refine the list with filters.

**Alternative / error flows**
- 3a. The list contains only visible statuses: `DRAFT` events are excluded, and `COMPLETED` or `CANCELLED` events are not returned by the default search.
- 3b. The user passes invalid paging or date parameters: the service rejects the request with 400 `INVALID_PARAMETER` and the UI shows the backend error.
- 3c. No event matches the current query or filters: the page shows the empty-state “No events match your filters” message instead of an error.

**Status:** planned

### UC-31: Search and filter events

| Field | Value |
|-------|-------|
| Workflow | W3 |
| Primary actor | Student |
| Linked FR | FR-W3-02, FR-W3-03, FR-W3-07 |
| Preconditions | User is logged in; the events list is loaded or the user is applying filters in the page query string |
| Postconditions (success) | The results list matches the combined criteria (search text, category, organization, date window) and the page preserves the filters in the URL |

**Main flow**
1. Student enters text in the search box or chooses filters for category, organization, from-date or to-date.
2. Frontend calls `GET /api/v1/events` with the selected values.
3. System validates the filters: dates must be in order, `category` must be one of the enum values, and paging parameters must be valid.
4. System narrows the visible events to those that match the text search by title (case-insensitive) and the other filters.
5. System returns the filtered results sorted by start time and updates the page with the matching cards.

**Alternative / error flows**
- 3a. Search text is blank: the query is treated as an unconstrained search and all valid visible events remain eligible.
- 3b. The category is not one of the allowed event categories: the backend rejects the request with 400 `INVALID_PARAMETER`.
- 3c. `from` is after `to`: the backend rejects the request with 400 `INVALID_PARAMETER` before running the search.
- 3d. The user filters to an organization with no visible results: the system shows an empty state rather than a server failure.

**Status:** planned

### UC-32: View event details

| Field | Value |
|-------|-------|
| Workflow | W3 |
| Primary actor | Student |
| Linked FR | FR-W2-04, FR-W3-04, FR-W3-05, FR-W3-08 |
| Preconditions | User is logged in; the event exists; the user is allowed to see its status and details |
| Postconditions (success) | User sees the event details record and the key decision-making fields needed to decide whether to participate |

**Main flow**
1. Student opens an event from the list or navigates directly to its details route by id.
2. System looks up the event and checks whether the user is allowed to see it.
3. For a `DRAFT` event, only organizers of that event’s organization may open it; for everyone else the event behaves as if it does not exist and returns 404.
4. For `PUBLISHED`, `REGISTRATION_CLOSED` and `ONGOING` events, any logged-in user can open the details page and see the event metadata plus confirmed registrations and free places.
5. For `COMPLETED` and `CANCELLED` events, any logged-in user can open the event and see its status, but the event is not returned in the default search list.
6. System displays the event’s title, organization, category, times, location, description, capacity, registration deadline, confirmed count, free places and current status.

**Alternative / error flows**
- 3a. Requested event id does not exist: `GET /api/v1/events/{id}` returns 404.
- 3b. Caller is not an organizer and the event is `DRAFT`: the service hides the existence of the event and returns 404, even though the row exists in the database.
- 3c. Event is not visible in list results but can still be opened as a completed/cancelled record: the details page may display the status while the search list filters it out.
- 3d. The user is not logged in: the event route is protected by the authenticated-user requirement, so the request is rejected before the details page loads.

**Status:** partial

---

## 4.7 W4 use cases (UC-40–49)

**Ticket:** `docs: use cases W4` · **Points:** 2

**What to write**
Detailed use cases for registration. Alternative flows must cover: full event, deadline
passed, event not PUBLISHED, already registered, cancel then register again.
Extra sources: `registration/` package, data-model.md registrations + service rules 1–2.

**Done when**
- [ ] One section per W4 use case in the list.
- [ ] All five alternative flows above appear.

### UC-40: View my registrations

| Field | Value |
|-------|-------|
| Workflow | W4 |
| Primary actor | Student |
| Linked FR | FR-W4-07 |
| Preconditions | Student is logged in; zero or more registrations exist for the account |
| Postconditions (success) | Student sees only their own `CONFIRMED` and `CANCELLED` registrations, ordered newest first |

**Main flow**
1. Student opens the "My registrations" page.
2. System reads the registrations attached to the authenticated user.
3. System keeps only `CONFIRMED` and `CANCELLED` rows, sorts them newest first, and shows the event title, start time, location and status for each row.
4. If the student has no registrations, the list is empty and the page shows an empty state.

**Alternative / error flows**
- 3a. There are no registrations yet: the system shows an empty list instead of throwing an error.
- 3b. The user is not logged in: the request is rejected before the page loads because the route requires an authenticated session.

**Status:** partial

### UC-41: Register for an event

| Field | Value |
|-------|-------|
| Workflow | W4 |
| Primary actor | Student |
| Linked FR | FR-W4-01, FR-W4-02, FR-W4-03, FR-W4-04, FR-W4-08 |
| Preconditions | Student is logged in; the event exists and is visible; the event status is `PUBLISHED`; the registration window is still open |
| Postconditions (success) | A new `CONFIRMED` registration exists for the user and the event's free places drop by one |

**Main flow**
1. Student opens a visible event page and presses "Register".
2. System locks the event row and checks the event status and registration window.
3. System verifies the user has no existing `CONFIRMED` registration for that event.
4. System compares the current confirmed count with the event's capacity.
5. System creates a new `CONFIRMED` registration and shows the success state (`You are registered`).

**Alternative / error flows**
- 3a. The event is not `PUBLISHED` or the registration deadline has passed (or the start time has passed when there is no deadline): the API rejects the request with 409 `REGISTRATION_CLOSED`.
- 3b. The event is full: the API rejects the request with 409 `EVENT_FULL` and the UI shows the event as full.
- 3c. The student already has a `CONFIRMED` registration for the same event: the API rejects the request with 409 `ALREADY_REGISTERED`.
- 3d. The event does not exist or is a draft: it is treated as 404, so no registration is created.
- 3e. The student previously cancelled a registration for this event and registers again while the event still accepts registrations: the system creates a new `CONFIRMED` row and keeps the earlier `CANCELLED` row as history.

**Status:** partial

### UC-42: Cancel my registration

| Field | Value |
|-------|-------|
| Workflow | W4 |
| Primary actor | Student |
| Linked FR | FR-W4-05, FR-W4-06 |
| Preconditions | Student is logged in; they already hold a `CONFIRMED` registration; the event is still before the start time and is `PUBLISHED` or `REGISTRATION_CLOSED` |
| Postconditions (success) | The registration becomes `CANCELLED` in history, the place is freed again, and a later valid registration can be created |

**Main flow**
1. Student opens the registration they want to cancel from "My registrations" or the event page.
2. System checks that the registration belongs to the logged-in user and is still `CONFIRMED`.
3. System verifies that the event is `PUBLISHED` or `REGISTRATION_CLOSED` and has not started yet.
4. System marks the registration as `CANCELLED` without deleting the row and frees the place again.

**Alternative / error flows**
- 3a. The event already started, or the event is `ONGOING`, `COMPLETED` or `CANCELLED`: cancellation is not allowed and the request is rejected.
- 3b. There is no confirmed registration for the event: the service responds with 404 `NOT_REGISTERED`.
- 3c. The registration was already checked in: cancellation is rejected with 409 `CANCEL_NOT_ALLOWED`.
- 3d. After a cancellation, the student may register again while the event is still open and the duplicate/capacity rules allow it; the new registration is stored as a new `CONFIRMED` row and the earlier `CANCELLED` row remains in history.

**Status:** planned

---

## 4.8 W5 use cases (UC-50–59)

**Ticket:** `docs: use cases W5` · **Points:** 2

**What to write**
Detailed use cases for organizer tasks, participant list, check-in and announcements.
Alternative flows: assignee not in the organization, check-in of a cancelled registration,
non-organizer tries an organizer action.
Extra sources: `organizing/` package, `Registration.checkedInAt`, data-model.md rules 3 and 5.

**Done when**
- [ ] One section per W5 use case in the list.
- [ ] Every use case is linked to at least one FR-W5 ID.

### UC-50: Manage organizer tasks

| Field | Value |
|-------|-------|
| Workflow | W5 |
| Primary actor | Organizer |
| Linked FR | FR-W5-02, FR-W5-03, FR-W5-04, FR-W5-05 |
| Preconditions | Caller is an ORGANIZER or PRESIDENT of the event’s organization; the event exists |
| Postconditions (success) | The event has a task list ordered by due date, any new task starts as `TODO`, and any valid assignee belongs to the event’s organization |

**Main flow**
1. Organizer opens the event’s organizing page and starts the Tasks panel.
2. System validates that the caller is an organizer of the event’s organization.
3. System loads the event’s tasks sorted by due date, with tasks without a due date last, and shows each task’s title, status, due date and assignee.
4. Organizer creates a new task with a title, optional description, optional due date and an optional assignee membership.
5. System checks the assignee membership belongs to the same organization, creates the task with status `TODO`, and saves it.

**Alternative / error flows**
- 3a. Caller is not an organizer or president of the event’s organization: the service rejects the request with 403 `FORBIDDEN`.
- 4a. The assignee membership belongs to another organization: the task creation request is rejected with 400 `ASSIGNEE_NOT_IN_ORGANIZATION`.
- 4b. The title is empty or longer than 200 characters: the request is rejected by validation before the task is saved.
- 5a. The task update flow for status/assignee changes is not fully complete in the web UI on `main` even though the backend update endpoint exists; this is why the use case remains `partial` rather than fully implemented.

**Status:** partial

### UC-51: View the participant list

| Field | Value |
|-------|-------|
| Workflow | W5 |
| Primary actor | Organizer |
| Linked FR | FR-W5-01 |
| Preconditions | Caller is an ORGANIZER or PRESIDENT of the event’s organization; the event exists |
| Postconditions (success) | Organizer sees the event participants, with each row showing registration status, registration time and check-in time, sorted by full name |

**Main flow**
1. Organizer opens the Participants panel for an event.
2. System verifies that the caller is allowed to view the event’s participant data.
3. System loads the registration rows for the event, sorted by participant full name.
4. System shows each participant’s name, email, registration status (`CONFIRMED` or `CANCELLED`), `registeredAt` and `checkedInAt` values.
5. Organizer optionally filters the list by registration status before reviewing attendance.

**Alternative / error flows**
- 2a. Caller is not an organizer of this event’s organization: the endpoint rejects the request with 403 `FORBIDDEN` because email and attendance data are personal data.
- 3a. The event id is unknown: the service throws `NotFoundException`, so no participant list is shown.
- 5a. No registrations exist yet: the system shows an empty list because the query returns zero rows instead of an error.

**Status:** partial

### UC-52: Check in a participant

| Field | Value |
|-------|-------|
| Workflow | W5 |
| Primary actor | Organizer |
| Linked FR | FR-W5-07 |
| Preconditions | Caller is an ORGANIZER or PRESIDENT of the event’s organization; the event exists and is `REGISTRATION_CLOSED` or `ONGOING`; the target registration belongs to the same event |
| Postconditions (success) | The participant’s registration is marked as checked in and `checkedInAt` is set to the current time |

**Main flow**
1. Organizer selects a participant from the participant list and clicks “Check in”.
2. System checks that the caller is an organizer of the event’s organization.
3. System finds the registration for the selected event and confirms it belongs to that event.
4. System verifies the event status is `REGISTRATION_CLOSED` or `ONGOING` and that the registration is `CONFIRMED` and has not been checked in yet.
5. System sets `checkedInAt = now` and returns the updated participant record.

**Alternative / error flows**
- 3a. The registration id belongs to another event or does not exist: the API responds with 404, so the organizer cannot inspect or mutate another event’s registration.
- 4a. The event is `DRAFT`, `PUBLISHED`, `COMPLETED` or `CANCELLED`: the check-in request is rejected with 409 `CHECK_IN_NOT_ALLOWED`.
- 4b. The registration status is `CANCELLED`: the request is rejected with 409 `CHECK_IN_NOT_ALLOWED` because `checked_in_at` is only valid for `CONFIRMED` registrations (`ck_registrations_checkin_confirmed`).
- 4c. The participant is already checked in: the request is rejected with 409 `ALREADY_CHECKED_IN` and no second check-in is recorded.
- 2a. A non-organizer tries to check in: the service rejects the request with 403 `FORBIDDEN`.

**Status:** planned

### UC-53: Post and read announcements

| Field | Value |
|-------|-------|
| Workflow | W5 |
| Primary actor | Organizer |
| Linked FR | FR-W5-08, FR-W5-09, FR-W5-10 |
| Preconditions | Caller is logged in; the event exists; organizer is allowed to post, readers are event members or have a registration for the event |
| Postconditions (success) | New announcements are stored for the event with the posting organizer as author, and readers see the newest announcements first |

**Main flow**
1. Organizer opens the event’s announcements section and submits a title and body.
2. System checks that the caller is an organizer or president of the event’s organization.
3. System rejects posting when the event is `DRAFT` or `COMPLETED`; otherwise it saves the announcement with the current user as author.
4. Event members and participants load the announcements for the event in reverse chronological order.
5. The page shows only the event’s valid announcements and hides them from unrelated users.

**Alternative / error flows**
- 2a. A non-organizer tries to create an announcement: the request is rejected with 403 `FORBIDDEN`.
- 3a. The event is `DRAFT` or `COMPLETED`: the request is rejected with 409 `ANNOUNCEMENT_NOT_ALLOWED`.
- 4a. A user is neither a member of the event’s organization nor a participant with a `CONFIRMED` or `CANCELLED` registration: the request is rejected with 403 `FORBIDDEN`.
- 4b. The event id does not exist: the system returns 404 instead of exposing a non-existent announcement list.
- 5a. The announcement edit/delete flow (`FR-W5-10`) is deferred to the final delivery; the MVP includes creation and reading but not announcement editing or deletion.

**Status:** partial

### UC-54: Read announcements of my events

| Field | Value |
|-------|-------|
| Workflow | W5 |
| Primary actor | Student / Organizer |
| Linked FR | FR-W5-09 |
| Preconditions | User is logged in; the event exists; the user is either a member of the event’s organization or has a `CONFIRMED` or `CANCELLED` registration for the event |
| Postconditions (success) | User sees the event’s announcements ordered newest first and can understand the latest organizer updates |

**Main flow**
1. User opens an event page or the organizing view for one of their events.
2. System checks whether the user is allowed to read the event’s announcements.
3. System loads the announcements for the event and sorts them by creation time descending.
4. System displays the announcements in the event page, showing title, author and message body.

**Alternative / error flows**
- 2a. User is not a member of the event’s organization and does not have a registration for that event: the service returns 403 `FORBIDDEN`.
- 3a. The event has no announcements yet: the system shows an empty list rather than an error.
- 3b. The event does not exist: the service returns 404, so no announcement data is displayed.

**Status:** partial

---
