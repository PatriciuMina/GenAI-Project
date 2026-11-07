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

**Format example (applies to all sections)**

> **Example – replace:**
>
> 1. The student opens **Events** (`/events`) and selects "Intro to Spring Boot Workshop".
> 2. The frontend calls `GET /api/v1/events/{id}` (planned) and shows free places (28 of 30).
>
> ```mermaid
> sequenceDiagram
>     actor S as Student
>     participant FE as Frontend
>     participant API as Backend API
>     participant DB as PostgreSQL
>     S->>FE: Click "Register"
>     FE->>API: POST /api/v1/events/{id}/registrations
>     API->>DB: lock event row, count CONFIRMED, insert
>     API-->>FE: 201 Created
>     FE-->>S: "You are registered"
> ```
>
> `R: R3 · FR: FR-W4-01, FR-W4-02 · UC: UC-41 · Code: registration · Tests: planned`

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

> TODO (team): W1 workflow here.

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

> **Example – replace** (only the first transition shown):
>
> ```mermaid
> stateDiagram-v2
>     [*] --> DRAFT: organizer creates event
>     DRAFT --> PUBLISHED: organizer publishes
> ```

**Done when**
- [ ] W2 steps, diagram, business rules and traceability line are present.
- [ ] All six `EventStatus` values and all allowed transitions appear, matching data-model.md.
- [ ] Each transition names its trigger and actor (manual or automatic).

> TODO (team): W2 workflow and state machine here.

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
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] The hand-off to W4 ("Register" button) is shown.

> TODO (team): W3 workflow here.

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
- [ ] Steps, diagram, business rules and traceability line are present.
- [ ] The concurrency/capacity rule and the rejection paths are shown.

> TODO (team): W4 workflow here.

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
