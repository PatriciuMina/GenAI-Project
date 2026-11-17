# 5. Main application workflows

Part **B.5** of the mid-term deliverable. Index: [README](README.md).

A *workflow* is the end-to-end path through the system for one goal, across UI, API and
database. Use cases (`04-use-cases.md`) describe single interactions; here you connect them.

**How to write each workflow section (same for W1–W5)**
- **Step-by-step description:** numbered steps "who does what, what the system does",
  naming the page (`frontend/src/routes/paths.ts`) and the endpoint (`/api/v1/...`, or
  "planned") at each step. 6–12 steps.
- **Diagram:** one Mermaid diagram, either `sequenceDiagram` (User → Frontend → API → DB)
  or `flowchart TD` (decisions and branches). Keep it to ~15 nodes/messages.
- **Business rules:** bullet list of the rules that apply, with their source.
- **Traceability line:** `R: … · FR: … · UC: … · Code: <packages> · Tests: <test classes or "planned">`.
- **Owner:** the teammate who owns the workflow (see `08-individual-contributions.md`).

**Common sources:** `CLAUDE.md` (workflow table, backend organization), `docs/data-model.md`,
`docs/architecture/ADR-001…`, `docs/course/course-requirements.md`, `02-requirements.md`,
`04-use-cases.md`, `06-ux-research.md` → "Main product flows".

**Format example (applies to all sections):** See the completed W1 workflow in
[§5.1](#51-w1-organizations-and-membership).

---

## 5.1 W1 Organizations and membership

**Ticket:** `docs: workflow W1` · **Points:** 2

**What to write**
From an organization being created (by an admin) to members joining, roles being assigned
and members leaving. Show how an organization role later unlocks W2/W5 actions.

**Where to find the information**
- `backend/.../organization/` (incl. `OrgPermissionService`), `frontend/src/features/organizations/`
- `docs/data-model.md` → organizations, memberships · `03-actors-roles.md`

**Format:** see top of file.

**Done when**
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] Implemented steps (list/view organizations) are distinguished from planned ones.

1. **Implemented — create:** An administrator opens **Admin** (`/admin`), enters an organization name and an existing account's email as its president, then submits. The frontend calls `POST /api/v1/organizations`.
2. **Implemented — appoint president:** The backend creates the organization and its `PRESIDENT` membership together in one transaction; the account must already exist.
3. **Implemented — list organizations:** A logged-in user opens **Organizations** (`/organizations`). The frontend calls `GET /api/v1/organizations` and displays the organizations sorted by name.
4. **Implemented — view organization:** The user opens an organization's details (`/organizations/{id}`); the frontend calls `GET /api/v1/organizations/{orgId}`. A logged-in member or an administrator can also see its members through `GET /api/v1/organizations/{orgId}/members`.
5. **Implemented — add members and assign initial roles:** The organization's president or an administrator submits an existing account's email and role (`MEMBER` or `ORGANIZER`) from the members section on `/organizations/{id}`. The frontend calls `POST /api/v1/organizations/{orgId}/members`; a student cannot join themself in the MVP.
6. **Implemented — see membership:** The added user sees the organization and their role in the app; the frontend loads `GET /api/v1/users/me/memberships`.
7. **Implemented — W2 access:** An `ORGANIZER` or `PRESIDENT` can create an event from
   `/organizing/events/new` with `POST /api/v1/organizations/{orgId}/events`; it starts as
   `DRAFT`. They can edit a DRAFT or PUBLISHED event from `/organizing/events/{id}/edit`
   with `PUT /api/v1/events/{id}`.
8. **Implemented in the backend — W5 access:** An `ORGANIZER` or `PRESIDENT` can list
   participants (`GET /api/v1/events/{eventId}/registrations`), create/list/update tasks
   (`POST /api/v1/events/{eventId}/tasks`, `GET /api/v1/events/{eventId}/tasks`,
   `PATCH /api/v1/events/{eventId}/tasks/{taskId}`), post announcements
   (`POST /api/v1/events/{eventId}/announcements`) and check in a participant
   (`POST /api/v1/events/{eventId}/registrations/{registrationId}/check-in`). The W5 web
   screens are planned; the organizing route is `/organizing/events/{id}`.
9. **Implemented — remove members; planned — leave or change role:** A president or administrator removes a non-president from `/organizations/{id}` with `DELETE /api/v1/organizations/{orgId}/members/{membershipId}`. A member leaving themself and changing a membership between `MEMBER` and `ORGANIZER` are planned; how a president leaves or is replaced is still open.

```mermaid
sequenceDiagram
    actor A as Administrator
    actor P as President
    actor M as Member or Organizer
    participant FE as Frontend
    participant API as Backend API
    participant DB as PostgreSQL
    A->>FE: Enter organization details and an existing president's email
    FE->>API: POST /api/v1/organizations
    API->>DB: Create organization and PRESIDENT membership in one transaction
    DB-->>API: Commit
    API-->>FE: 201 Created
    M->>FE: Browse organizations and open one
    FE->>API: GET /api/v1/organizations, then GET /api/v1/organizations/{id}
    API->>DB: Read organization list and details
    DB-->>API: Organization data
    API-->>FE: 200 OK
    P->>FE: Add an existing account as MEMBER or ORGANIZER
    FE->>API: POST /api/v1/organizations/{orgId}/members
    API->>DB: Insert membership after permission, account and duplicate checks
    DB-->>API: Membership saved
    API-->>FE: 201 Created
    M->>FE: View own organizations and roles
    FE->>API: GET /api/v1/users/me/memberships
    API->>DB: Read caller's memberships
    DB-->>API: Memberships and roles
    API-->>FE: 200 OK
    alt Role is ORGANIZER or PRESIDENT
        M->>FE: Create a W2 event or use a W5 organizer action
        FE->>API: POST /api/v1/organizations/{orgId}/events or a W5 endpoint
        API->>API: Check isOrganizer for the event's organization
        API->>DB: Create event/task or read/update W5 data
        DB-->>API: Result
        API-->>FE: Success
    else Role is MEMBER
        M->>FE: Try a W2/W5 organizer-only action
        FE->>API: W2/W5 request
        API-->>FE: 403 Forbidden
    end
    opt President or ADMIN removes a non-president member
        P->>FE: Remove member
        FE->>API: DELETE /api/v1/organizations/{orgId}/members/{membershipId}
        API->>DB: Delete membership and leave assigned tasks unassigned
        API-->>FE: 204 No Content
    end
    Note over M,API: Self-leave and role changes are planned
```

**Business rules**
- Only a global `ADMIN` can create an organization. The president is an existing account, and the organization plus its president membership are created together; organization names are unique ignoring case. (`OrganizationController`, `OrganizationService`; `docs/data-model.md`, organizations; FR-W1-03)
- Only the organization's president or a global `ADMIN` can add or remove members. New memberships can be `MEMBER` or `ORGANIZER`; the president is appointed at creation, not through the add-member endpoint. Students do not self-join in the MVP. (`OrganizationController`, `MembershipService`; FR-W1-05, FR-W1-06)
- A user has at most one membership per organization. Roles belong to a specific organization and form the hierarchy `PRESIDENT ⊃ ORGANIZER ⊃ MEMBER`; a global `ADMIN` has no automatic organization role or organizer rights. (`uq_memberships_user_organization`; `OrgPermissionService`; `docs/data-model.md`, organization_memberships)
- `ORGANIZER` and `PRESIDENT` satisfy `isOrganizer` for W2 event creation and W5 organizer endpoints; a plain `MEMBER` does not. W5 APIs exist on `main`, while their web screens are planned. An assigned task's member must belong to the event's organization. (`OrgPermissionService`, `EventController`, `TaskService`, `RegistrationService`, `AnnouncementService`; `docs/data-model.md`, service rules 3 and 5)
- The president cannot be removed. Removing another member makes tasks assigned to that membership unassigned; member self-leave and role changes are planned. (`MembershipService`; `fk_event_tasks_assignee`; FR-W1-06, FR-W1-08, FR-W1-09)

`R: R1, R2, R5 · FR: FR-ADM-02, FR-ADM-03, FR-W1-01, FR-W1-02, FR-W1-03, FR-W1-04, FR-W1-05, FR-W1-06, FR-W1-07, FR-W1-08, FR-W1-09, FR-W2-01, FR-W2-03, FR-W5-01, FR-W5-02, FR-W5-03, FR-W5-04, FR-W5-05, FR-W5-07, FR-W5-08, FR-W5-09 · UC: UC-05, UC-10, UC-11, UC-12, UC-13, UC-20, UC-21, UC-50, UC-51, UC-52, UC-53, UC-54 · Code: organization, event, organizing, registration · Tests: OrganizationCreateControllerIntegrationTest, OrganizationControllerIntegrationTest, OrganizationAddMemberControllerIntegrationTest, OrganizationRemoveMemberControllerIntegrationTest, MyMembershipsControllerIntegrationTest, OrgPermissionServiceTest, OrgPermissionPreAuthorizeIntegrationTest, EventControllerIntegrationTest, TaskControllerIntegrationTest, TaskUpdateControllerIntegrationTest, RegistrationListControllerIntegrationTest, RegistrationCheckInControllerIntegrationTest, RegistrationServiceCheckInTest, AnnouncementControllerIntegrationTest; FR-W1-08/09 planned`

---

## 5.2 W2 Event creation and lifecycle

**Ticket:** `docs: workflow W2 + event state machine` · **Points:** 3

**What to write**
From an organizer creating a draft to the event being completed or cancelled: who triggers
each transition, which checks apply (dates, required fields, permissions), and what happens
to registrations when an event is cancelled.

**Where to find the information**
- `backend/.../event/` (`Event`, `EventStatus`, `EventCategory`)
- `docs/data-model.md` → events constraints, D3, D4, D11 (optimistic locking), service rule 4

**Format:** see top of file, plus the state machine below.

### Event status state machine

**What to write**
A state diagram of `EventStatus`. The states and the allowed transitions are already
decided: copy them from the **source of truth**, do not invent new ones:
- `backend/src/main/java/ro/ubb/events/event/entity/EventStatus.java` (the states)
- `docs/data-model.md` → "Status lifecycle (W2)" (the transitions; CANCELLED reachable from any status except COMPLETED)

Label every transition with the trigger (user action or time-based) and the actor. If the
team wants to change the lifecycle, update data-model.md and the code first (via a PR),
then this diagram.

**Format:** Mermaid `stateDiagram-v2`.

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Organizer or president (manual: submits valid event, system sets DRAFT)
    DRAFT --> PUBLISHED: Organizer or president (manual: Publish, startAt is in the future)
    PUBLISHED --> REGISTRATION_CLOSED: Organizer or president (manual: Close registration)
    PUBLISHED --> ONGOING: Organizer or president (manual: Start, at or after startAt minus 1 hour)
    REGISTRATION_CLOSED --> ONGOING: Organizer or president (manual: Start, at or after startAt minus 1 hour)
    ONGOING --> COMPLETED: Organizer or president (manual: Complete, at or after endAt)
    DRAFT --> CANCELLED: Organizer or president (manual: Cancel)
    PUBLISHED --> CANCELLED: Organizer or president (manual: Cancel)
    REGISTRATION_CLOSED --> CANCELLED: Organizer or president (manual: Cancel)
    ONGOING --> CANCELLED: Organizer or president (manual: Cancel)
```

**Done when**
- [x] W2 steps, diagram, business rules and traceability line are present.
- [x] All six `EventStatus` values and all allowed transitions appear, matching data-model.md.
- [x] Each transition names its trigger and actor (manual or automatic).

**Checked against the code (T-49, 2026-11-17).** The diagram was compared with
`EventStatus` (6 states), `EventLifecycle.rejectionReason` (allowed transitions and date
rules), `EventService.changeStatus` (organizer/president check, 409 on rejection), the
transition matrix in `EventLifecycleTest` and the buttons in the frontend `StatusActions`
(Publish, Close registration, Start, Complete, Cancel). All ten transitions, their
conditions and actors match; there is no automatic (time-based) transition. The only
difference was in `docs/data-model.md`, which showed a strict linear chain without
`PUBLISHED → ONGOING` and without the date conditions; it now has the same transition table.

### Sequence diagram: create a draft, publish, rejected transition

Creating a draft and changing its status are implemented on `main` (`POST /api/v1/organizations/{orgId}/events` and `POST /api/v1/events/{id}/status`). The diagram shows an allowed publish action and a rejected transition; lifecycle violations return 409 `INVALID_STATUS_TRANSITION`.

```mermaid
sequenceDiagram
    actor O as Organizer
    participant FE as Frontend
    participant API as Backend API
    participant DB as PostgreSQL
    O->>FE: Fill in the event form and submit
    FE->>API: POST /api/v1/organizations/{orgId}/events
    API->>API: check organizer of orgId, validate the fields
    API->>DB: insert event with status DRAFT
    API-->>FE: 201 Created (event, status DRAFT)
    O->>FE: Press "Publish"
    FE->>API: POST /api/v1/events/{id}/status (PUBLISHED)
    API->>DB: read the event
    alt event is DRAFT and its start is in the future
        API->>DB: set status PUBLISHED
        API-->>FE: 200 OK (event, status PUBLISHED)
    else transition not allowed, for example the event is already PUBLISHED
        API-->>FE: 409 INVALID_STATUS_TRANSITION
    end
    FE-->>O: Show the new status or the error message
```

1. **Implemented — open the organizer event list:** An `ORGANIZER` or `PRESIDENT` opens **Organizing** (`/organizing`) and selects an organization. The frontend loads its events with `GET /api/v1/organizations/{orgId}/events`; organizers see drafts as well as events in other statuses.
2. **Implemented — create a draft:** From `/organizing/events/new`, the organizer submits the event form to `POST /api/v1/organizations/{orgId}/events`. The API requires a title, location, category, start and end times, and positive capacity; description and registration deadline are optional. The start must be in the future, the end after the start, and a supplied registration deadline no later than the start. The API creates the event as `DRAFT` and returns `201 Created`.
3. **Implemented — view and edit:** The organizer opens `/organizing/events/{id}` and can edit from `/organizing/events/{id}/edit` using `PUT /api/v1/events/{id}`. Only `DRAFT` and `PUBLISHED` events are editable; the request includes the last-read version, applies the creation field rules, and cannot lower capacity below confirmed registrations. A stale version returns `409 CONCURRENT_UPDATE`.
4. **Implemented — publish:** The organizer selects **Publish**, which sends `POST /api/v1/events/{id}/status` with `PUBLISHED`. Only `DRAFT → PUBLISHED` is allowed, and `startAt` must still be in the future. A successful request returns the event with status `PUBLISHED`.
5. **Implemented — close registration:** The organizer selects **Close registration** to change `PUBLISHED → REGISTRATION_CLOSED` through the same status endpoint. This transition has no additional date check.
6. **Implemented — start:** The organizer selects **Start** to change either `PUBLISHED` or `REGISTRATION_CLOSED` to `ONGOING`. The request is accepted only from one hour before `startAt` onward.
7. **Implemented — complete:** Once `endAt` has passed, the organizer selects **Complete** to change `ONGOING → COMPLETED`. The service rejects completion before the end time.
8. **Implemented — cancel:** The organizer can select **Cancel** from `DRAFT`, `PUBLISHED`, `REGISTRATION_CLOSED`, or `ONGOING`; the service changes the status to `CANCELLED`. It does not delete the event or change/delete its registration records; registrations remain as history and the event is shown as cancelled.
9. **Implemented — handle rejected actions:** Every status request checks that the caller is an `ORGANIZER` or `PRESIDENT` of the event's organization. A caller without that role receives `403 FORBIDDEN`; an unknown event returns 404; a disallowed transition or unmet lifecycle date condition returns `409 INVALID_STATUS_TRANSITION`. The frontend displays the result or error on the organizer event page.

**Business rules**
- An event is created only by an `ORGANIZER` or `PRESIDENT` of its organization and always starts as `DRAFT`; a global `ADMIN` has no event-management rights unless they also have an organizer role in that organization. (`EventController`, `EventService`, `OrgPermissionService`; FR-W2-01)
- Required fields are title (max 200 characters), location (max 255), category, `startAt`, `endAt`, and positive capacity. `startAt` must be future; `endAt > startAt`; optional `registrationDeadline <= startAt`. Description and registration deadline may be null. Invalid values return 400 `VALIDATION_FAILED`. (FR-W2-02; `EventRequest`, `EventDatesValidator`, D3, events constraints)
- Only `DRAFT` and `PUBLISHED` events can be edited. Updates carry the last-read version (D11), and capacity cannot be reduced below confirmed registrations. (FR-W2-03; `EventService.update`, D11)
- A draft is visible only to organizers of its organization; other authenticated users receive 404 when requesting it. Publishing makes it visible beyond the organizing team. (FR-W2-04; `EventService.getById`)
- The allowed transitions are `DRAFT → PUBLISHED`, `PUBLISHED → REGISTRATION_CLOSED`, `PUBLISHED → ONGOING`, `REGISTRATION_CLOSED → ONGOING`, `ONGOING → COMPLETED`, and `DRAFT | PUBLISHED | REGISTRATION_CLOSED | ONGOING → CANCELLED`. Publishing requires a future start; starting is allowed from one hour before the start; completing requires the end time to have passed. Every transition is manual—an organizer invokes the status action; no scheduler advances statuses. Any other transition, including a same-status request or a transition out of `COMPLETED` or `CANCELLED`, returns 409 `INVALID_STATUS_TRANSITION`. (FR-W2-05–10; `EventLifecycle`, `EventService.changeStatus`, data-model service rule 4)
- Cancellation retains the event row and leaves all registration records and their statuses unchanged. Events with registrations must not be hard-deleted, preserving registration history. (`EventService.changeStatus`; FR-W2-09; D4, `fk_registrations_event`)

`R: R1, R4 · FR: FR-W2-01, FR-W2-02, FR-W2-03, FR-W2-04, FR-W2-05, FR-W2-06, FR-W2-07, FR-W2-08, FR-W2-09, FR-W2-10 · UC: UC-20, UC-21, UC-22, UC-23, UC-24 · Code: event, organization, registration, frontend organizing · Tests: EventControllerIntegrationTest, EventLifecycleTest, EventRequestValidationTest`

---

## 5.3 W3 Event discovery and search

**Ticket:** `docs: workflow W3` · **Points:** 1

**What to write**
From a student opening the events page to deciding whether to register: listing, filtering,
searching, opening details. Show which events are visible (statuses) and what information
supports the decision.

**Where to find the information**
- `backend/.../event/repository/EventRepository.java`, `docs/data-model.md` → W3 indexes
- `frontend/src/features/events/` · `06-ux-research.md` (how students find events today)

**Format:** see top of file (a `flowchart TD` usually fits best).

**Done when**
- [x] Steps, diagram, business rules and traceability line are present.
- [x] The hand-off to W4 ("Register" button) is shown.

```mermaid
flowchart TD
    A[Student logs in and opens Events /events] --> B[GET /api/v1/events?page=0&size=20]
    B --> C{Any visible upcoming event?}
    C -- no --> D[No events match your filters]
    C -- yes --> E[Cards: title, organization, date, location, category, places left / Full / Registration closed]
    D --> F[Change text, category, organization or date filters]
    E --> F
    F -->|filters saved in the address, page reset to 1| B
    E --> G[Open an event /events/:id]
    G --> H[GET /api/v1/events/id]
    H --> I{DRAFT and not an organizer, or unknown id?}
    I -- yes --> J[404: Event not found]
    I -- no --> K[Details: description, start and end, location, deadline, free places, status, announcements]
    K --> L{Registration open and places left?}
    L -- yes --> M[Register button: hand-off to W4]
    L -- no --> N[Full / Registration closed: no button]
```

1. **Implemented — open the events list:** A logged-in user selects **Events** (`/events`). The frontend calls `GET /api/v1/events?page=0&size=20`. All event endpoints require a login (JWT); a visitor is sent to `/login`.
2. **Implemented — the system chooses what is visible:** The backend returns only events with status `PUBLISHED`, `REGISTRATION_CLOSED` or `ONGOING` that have not ended yet (`endAt` ≥ now), sorted by start time, earliest first. `DRAFT`, `COMPLETED` and `CANCELLED` events never appear in this list.
3. **Implemented — read the cards:** Each event is a card with title, organization, date and time, location, category and its availability: "x of y places left", "Full", or "Registration closed" for a `REGISTRATION_CLOSED` event. The free places are computed from the confirmed registrations.
4. **Implemented — search by text:** The user types in **Search events**. The frontend sends `q`; an event matches when its title contains the text, ignoring case (the description is not searched).
5. **Implemented — filter:** The user narrows the list by **Category** (one of the eight fixed categories), **Organization**, and a **From / To** date range on the start date. Filters can be combined. They are kept in the address (`?q=…&category=…&organizationId=…&from=…&to=…&page=…`), so the same list can be shared as a link; changing a filter goes back to page 1.
6. **Implemented — page through results and empty result:** With more than 20 results a pagination bar is shown. If nothing matches, the page shows "No events match your filters".
7. **Implemented — (alternative) events of one organization:** From an organization's page, `GET /api/v1/organizations/{orgId}/events` lists that organization's events by start time. A non-organizer does not see drafts; an organizer of that organization also sees its drafts.
8. **Implemented — open the details:** The user selects a card and goes to `/events/{id}`. The frontend calls `GET /api/v1/events/{id}`, which returns the description, start and end, location, category, registration deadline, status, capacity, confirmed count and free places, with a link to the organization. Announcements of the event are shown below (W5).
9. **Implemented — hidden or unknown event:** For a `DRAFT` requested by someone who is not an organizer of its organization, and for an unknown id, the API answers 404 and the page shows "Event not found", so drafts do not leak.
10. **Implemented — decide and hand off to W4:** If the event is `PUBLISHED`, its deadline (or start, without a deadline) has not passed and places are left, the details page shows **Register**; this starts W4 (§5.4). A student who is already registered sees **Cancel registration** instead (also W4). Otherwise the page shows the reason ("Full" or "Registration is closed") instead of the button.

**Business rules**
- Only logged-in users can list or open events; there is no public event page in the MVP. (`SecurityConfig`; FR-W3-01)
- The list shows only `PUBLISHED`, `REGISTRATION_CLOSED` and `ONGOING` events that have not ended, sorted by `startAt` ascending. (`EventService.search`; FR-W3-01, FR-W3-05)
- Text search matches the title only, case-insensitive; `%` and `_` typed by the user are treated as normal characters. (`EventService.search`; FR-W3-03)
- Filters: `category` must be one of the fixed categories and `from` must not be after `to`, otherwise 400 `INVALID_PARAMETER`; `from`/`to` compare against the start time. (`EventController`, `EventService.search`; FR-W3-02; data-model D3)
- Paging: `page` starts at 0, the default size is 20 and the size is capped at 50; a negative page or a size below 1 returns 400 `INVALID_PARAMETER`. (`EventService.search`; FR-W3-02)
- A draft is visible only to organizers of its organization: it is never in the search and `GET /events/{id}` returns 404 to everyone else. (`EventService.getById`, `EventService.findByOrganization`; FR-W2-04, FR-W3-05)
- Free places = capacity − confirmed registrations; cancelled registrations do not count. (`EventMapper`, `RegistrationService.countConfirmedByEventId`; FR-W3-04)
- The frontend only decides which button to show (`registrationRules.ts`); the backend checks every rule again when the student registers (W4). (FR-W3-08, FR-W4-02)

`R: R1, R3, R4, R6, R10 · FR: FR-W2-04, FR-W3-01, FR-W3-02, FR-W3-03, FR-W3-04, FR-W3-05, FR-W3-06, FR-W3-07, FR-W3-08 · UC: UC-30, UC-31, UC-32 · Code: event (EventController, EventService, EventRepository), registration (confirmed count), frontend features/events · Tests: EventSearchControllerIntegrationTest, EventControllerIntegrationTest, PermissionMatrixIntegrationTest, EventsPage.test.tsx, EventDetailsPage.test.tsx, EventCard.test.tsx, eventFormat.test.ts, registrationRules.test.ts`

---

## 5.4 W4 Participant registration

**Ticket:** `docs: workflow W4` · **Points:** 2

**What to write**
Register, see my registrations, cancel and register again. Explain how the capacity limit
is guaranteed when two students register for the last place at the same time.

**Where to find the information**
- `backend/.../registration/`, `frontend/src/features/registrations/`
- `docs/data-model.md` → registrations constraints, service rules 1–2, D1 (no waitlist)

**Format:** see top of file (a `sequenceDiagram` with an `alt` block for "full" fits best).

**Done when**
- [x] Steps, diagram, business rules and traceability line are present.
- [x] The concurrency/capacity rule and the rejection paths are shown.

### Sequence diagram: register with a capacity check, and cancel

Registering (`POST /api/v1/events/{id}/registrations`, T-21) and cancelling (`DELETE /api/v1/events/{id}/registrations/me`, T-22) are implemented on `main`. The event row is locked first, so two students taking the last place are handled one after the other (`docs/data-model.md`, rules 1 and 2).

```mermaid
sequenceDiagram
    actor S as Student
    participant FE as Frontend
    participant API as Backend API
    participant DB as PostgreSQL
    S->>FE: Click "Register"
    FE->>API: POST /api/v1/events/{id}/registrations
    API->>DB: lock the event row (SELECT ... FOR UPDATE)
    alt event not found or still a DRAFT
        API-->>FE: 404 NOT_FOUND
    else event is not PUBLISHED or the deadline has passed
        API-->>FE: 409 REGISTRATION_CLOSED
    else the student already has a CONFIRMED registration
        API-->>FE: 409 ALREADY_REGISTERED
    else CONFIRMED registrations equal the capacity
        API-->>FE: 409 EVENT_FULL
    else a place is free
        API->>DB: insert registration with status CONFIRMED
        API-->>FE: 201 Created
    end
    FE-->>S: "You are registered" or the error message
    S->>FE: Click "Cancel registration" and confirm
    FE->>API: DELETE /api/v1/events/{id}/registrations/me
    alt no CONFIRMED registration of the caller
        API-->>FE: 404 NOT_REGISTERED
    else event started, not open, or already checked in
        API-->>FE: 409 CANCEL_NOT_ALLOWED
    else allowed
        API->>DB: set the registration status to CANCELLED (row kept)
        API-->>FE: 204 No Content
    end
```

1. **Implemented — start from the event details:** A logged-in student opens an event (`/events/{id}`, end of W3). The **registration area** (`RegistrationButton`) loads the student's registrations (`GET /api/v1/users/me/registrations`) to know whether they are already registered, and shows one of: **Register**, "You are registered" with **Cancel registration**, "Full", or "Registration is closed".
2. **Implemented — register:** The student selects **Register**. The frontend sends `POST /api/v1/events/{id}/registrations` (no body; the user comes from the JWT).
3. **Implemented — lock the event:** In one transaction the backend reads the event with a row lock (`SELECT … FOR UPDATE`, `EventRepository.findByIdForUpdate`). A second request for the same event waits at this point until the first one has committed or rolled back.
4. **Implemented — check the rules in order:** unknown event or `DRAFT` → 404; event not `PUBLISHED`, or now after the registration deadline (or after the start when there is no deadline) → 409 `REGISTRATION_CLOSED`; the student already has a `CONFIRMED` registration → 409 `ALREADY_REGISTERED`; confirmed registrations already equal the capacity → 409 `EVENT_FULL`.
5. **Implemented — save:** Otherwise a new `CONFIRMED` registration is inserted and the API returns `201 Created`. The page shows "You are registered for this event." and the free places on the page drop by one.
6. **Implemented — rejected registration:** For any 409 the page shows the friendly message for the error code (T-45, `errorMessages.ts`), for example that the event is full, and the student stays on the page.
7. **Implemented — the last place, two students at once:** Two students press **Register** for the last place at the same moment. Because of the row lock in step 3, the requests run one after the other: the first sees one free place and is saved; the second then counts the new confirmed row and gets 409 `EVENT_FULL`. The capacity is never exceeded. As a second safety net, the unique index `ux_registrations_confirmed_once` stops the same student from having two confirmed rows (mapped to `ALREADY_REGISTERED`).
8. **Planned (T-25) — see my registrations:** The API already returns the student's registrations, `CONFIRMED` and `CANCELLED`, newest first, with event title, start, location and status (`GET /api/v1/users/me/registrations`). The **My registrations** page (`/my-registrations`) that lists them as *Upcoming* and *Past and cancelled* is still a placeholder.
9. **Implemented — cancel:** On the event page the student selects **Cancel registration** and confirms in a dialog. The frontend sends `DELETE /api/v1/events/{id}/registrations/me`. If the event is `PUBLISHED` or `REGISTRATION_CLOSED`, has not started, and the registration is not checked in, the registration becomes `CANCELLED` (`204 No Content`) and the place is free again. Otherwise 409 `CANCEL_NOT_ALLOWED`; without a confirmed registration 404 `NOT_REGISTERED`.
10. **Implemented — register again:** After cancelling, the **Register** button is shown again while the rules in step 4 allow it. Registering again creates a new `CONFIRMED` row; the old `CANCELLED` row stays as history.

**Business rules**
- Only a logged-in user can register, and only for themselves. (`RegistrationController`; FR-W4-01)
- Registration is open only while the event is `PUBLISHED` and before the registration deadline, or before the start if there is no deadline; a `DRAFT` answers 404 so it does not leak. (`RegistrationService.register`; FR-W4-02, FR-W2-04)
- At most one `CONFIRMED` registration per student and event: checked by the service and enforced by the partial unique index `ux_registrations_confirmed_once`. (FR-W4-03; `docs/data-model.md`, registrations)
- Confirmed registrations never exceed the capacity, also under concurrent requests, because the event row is locked for the whole transaction. There is no waitlist: a full event rejects with `EVENT_FULL`. (`EventRepository.findByIdForUpdate`, `RegistrationService.register`; FR-W4-04; data-model service rule 1, D1; test `RegistrationConcurrencyTest`)
- Cancelling is allowed only for the caller's own `CONFIRMED`, not checked-in registration, while the event is `PUBLISHED` or `REGISTRATION_CLOSED` and has not started. The row is kept as `CANCELLED`, never deleted. (`RegistrationService.cancelMyRegistration`; FR-W4-05; data-model D4)
- Registering again after cancelling creates a new row. (FR-W4-06; `ux_registrations_confirmed_once` applies only to `CONFIRMED` rows)
- A user sees only their own registrations. (`RegistrationService.listMine`; FR-W4-07)
- Every rejection uses the standard error body with a stable code (`EVENT_FULL`, `REGISTRATION_CLOSED`, `ALREADY_REGISTERED`, `CANCEL_NOT_ALLOWED`, `NOT_REGISTERED`); the web app turns the code into a friendly message. (`GlobalExceptionHandler`, `frontend/src/api/errorMessages.ts`; FR-W4-08)

`R: R3, R4, R6, R10 · FR: FR-W2-04, FR-W4-01, FR-W4-02, FR-W4-03, FR-W4-04, FR-W4-05, FR-W4-06, FR-W4-07, FR-W4-08 · UC: UC-40, UC-41, UC-42 · Code: registration (RegistrationController, RegistrationService, RegistrationRepository), event (EventRepository.findByIdForUpdate), frontend features/events (RegistrationButton), features/registrations · Tests: RegistrationRulesTest, RegistrationConcurrencyTest, RegistrationRegisterControllerIntegrationTest, RegistrationCancelControllerIntegrationTest, MyRegistrationsControllerIntegrationTest, RegistrationServiceRegisterTest, RegistrationServiceCancelTest, RegistrationButton.test.tsx, registrationRules.test.ts`

---

## 5.5 W5 Event organization

**Ticket:** `docs: workflow W5` · **Points:** 2

**What to write**
What organizers do between publishing and completing an event: split work into tasks and
assign them, follow the participant list, post announcements, check in participants at
the door. Show how this connects to the event status (e.g. check-in while ONGOING).

**Where to find the information**
- `backend/.../organizing/` (`EventTask`, `TaskStatus`, `Announcement`), `Registration.checkedInAt`
- `docs/data-model.md` → event_tasks, announcements, service rules 3 and 5
- `frontend/src/features/organizing/` · `06-ux-research.md` (organizer interviews)

**Format:** see top of file. Optionally a small `stateDiagram-v2` for `TaskStatus`.

**Done when**
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] Tasks, announcements and check-in all appear.
- [ ] At least one step is justified by an interview finding (R5).

> TODO (team): W5 workflow here.
