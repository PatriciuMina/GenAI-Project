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

| ID | Title (proposed – adjust) | Workflow | Primary actor | Linked FR | Status |
|----|---------------------------|----------|---------------|-----------|--------|
| UC-01 | Create an account | AUTH | | | |
| UC-02 | Log in | AUTH | | | |
| UC-03 | Log out | AUTH | | | |
| UC-04 | Create an organization and appoint its president | ADM | | | |
| UC-10 | Browse organizations | W1 | | | |
| UC-11 | Join an organization / add a member | W1 | | | |
| UC-12 | Change a member's role | W1 | | | |
| UC-13 | Leave an organization / remove a member | W1 | | | |
| UC-14 | Edit organization details | W1 | | | |
| UC-20 | Create a draft event | W2 | | | |
| UC-21 | Edit an event | W2 | | | |
| UC-22 | Publish an event | W2 | | | |
| UC-23 | Close registration / start / complete an event | W2 | | | |
| UC-24 | Cancel an event | W2 | | | |
| UC-30 | Browse upcoming events | W3 | | | |
| UC-31 | Search and filter events | W3 | | | |
| UC-32 | View event details | W3 | | | |
| UC-40 | View my registrations | W4 | | | |
| UC-41 | Register for an event | W4 | | | |
| UC-42 | Cancel my registration | W4 | | | |
| UC-50 | Manage organizer tasks | W5 | | | |
| UC-51 | View the participant list | W5 | | | |
| UC-52 | Check in a participant | W5 | | | |
| UC-53 | Post an announcement | W5 | | | |
| UC-54 | Read announcements of my events | W5 | | | |

> TODO (team): confirm the list and fill the empty columns.

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

> TODO (team): UC-10 … here.

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

> TODO (team): UC-20 … and UC-30 … here.

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

> TODO (team): UC-40 … here.

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

> TODO (team): UC-50 … here.
