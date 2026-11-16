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
