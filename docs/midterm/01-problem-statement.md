# 1. Problem statement and project scope

Part **B.1** of the mid-term deliverable. Index: [README](README.md).

---

## 1.1 Problem

**Ticket:** `docs: write problem statement` · **Points:** 2

**What to write**
Describe the problem from the users' point of view, not the solution. How do student
organizations at UBB organize events today, and what goes wrong (scattered information,
manual registration lists, overbooking, no attendance record, …)? Why does it matter?
Base every claim on the interviews or on your own experience as UBB students, and say
which one it is. End with one or two sentences on what the system will change.

**Where to find the information**
- `docs/course/course-requirements.md` (R1–R6: what the course expects the system to cover)
- `CLAUDE.md` → "Project purpose" (one-paragraph summary and workflows W1–W5)
- `06-ux-research.md` → findings table (once interviews are done)

**Format**
Prose, 150–300 words. Optionally a short bullet list of 3–5 pain points.

> **Example – replace:**
> *Pain point:* "Registrations are collected in a Google Form, so organizers find out only
> after the deadline that more people signed up than the room holds." (source: I-02)

**Done when**
- [ ] The problem is described without naming technologies or features.
- [ ] Each pain point names its source (interview ID or team experience).
- [ ] 150–300 words.

> TODO (team): write the problem here.

---

## 1.2 Stakeholders

**Ticket:** `docs: list stakeholders` · **Points:** 1

**What to write**
List everyone who is affected by or has an interest in the system: people who use it
(students, organization members, organizers, presidents, platform admin) and people who
do not use it directly (e.g. the university, sponsors, course staff). For each, write
what they want from the system. Keep it consistent with the actors in `03-actors-roles.md`
(an actor is a stakeholder who uses the system).

**Where to find the information**
- `backend/src/main/java/ro/ubb/events/user/entity/GlobalRole.java` (STUDENT, ADMIN)
- `backend/src/main/java/ro/ubb/events/organization/entity/OrganizationRole.java` (PRESIDENT, ORGANIZER, MEMBER)
- `docs/course/course-requirements.md` (R2, R5)
- `06-ux-research.md` (who you interviewed)

**Format**
Table: `Stakeholder | Uses the system? | Main interest / goal`. 5–8 rows.

The roles below are the ones in the code (global role `STUDENT` or `ADMIN` in `GlobalRole`, organization role `MEMBER`, `ORGANIZER` or `PRESIDENT` in `OrganizationRole`), plus the visitor who is not logged in. Each goal follows from what the course statement expects the system to cover (R2, R3, R5, R6) and from what the role can do in the application today or in the agreed plan. These goals are the team's design assumptions: no interviews have been done yet, so they are not yet confirmed by users (see `06-ux-research.md` when it is written).

| Stakeholder | Uses the system? | Main interest / goal |
|-------------|------------------|----------------------|
| Visitor (not logged in) | Yes | Create an account and log in, to reach the events of student organizations |
| Student (participant, global role STUDENT) | Yes | Find events that suit them and the information needed to decide (date, place, free places, deadline), get a guaranteed place without overbooking, see and, in the plan, cancel their registrations, and read announcements about the events they registered for (R3, R6) |
| Organization member (MEMBER) | Yes | Belong to an organization, see who else is in it, and follow the announcements of its events (R2) |
| Organizer (ORGANIZER) | Yes | Prepare and run the organization's events: create, edit and publish events, share the preparation work in tasks, see who registered, record who attended and announce changes to participants (R1, R5) |
| President (PRESIDENT) | Yes | Everything an organizer does, and keep the organization's member list up to date by adding and removing members (R2) |
| Platform administrator (ADMIN) | Yes | Set up student organizations on the platform and appoint each one's president, so that organizations can start using the system (R2) |
| Course staff (instructors and graders) | No, they review the result | Check that the project covers R1 to R16 and that the use of GenAI is documented and visibly overseen by the students (R13, R14, R15) |

**Done when**
- [ ] Every actor from `03-actors-roles.md` appears as a stakeholder.
- [ ] At least one stakeholder who does not use the system directly is listed.
- [ ] Each row has a concrete goal (not "use the app").

**Open items for the team**
- `03-actors-roles.md` is not written yet, so the first box has to be checked against it once it exists; every value of `GlobalRole` and `OrganizationRole`, and the visitor, is already in the table.
- No other people or bodies are named in any repository document (for example the university or its faculties, or sponsors). Add them only if the interviews show that they matter.
- The goals of the students, members, organizers and presidents should be confirmed or corrected with the interviews.

---

## 1.3 Scope (in scope)

**Ticket:** `docs: define project scope` · **Points:** 1

**What to write**
List what the system will do by the end of the project, grouped by workflow W1–W5 plus
accounts/admin. One line per capability, no implementation details. Mark what is already
part of the mid-term MVP. This list should match the functional requirement groups in
`02-requirements.md`.

**Where to find the information**
- `CLAUDE.md` → workflow table W1–W5
- `docs/course/course-requirements.md` (R1–R6, R10)
- `docs/data-model.md` (entities and the "Rules enforced in the service layer")
- `frontend/src/routes/paths.ts` (pages the UI will have)

**Format**
Bullet list grouped by workflow, 10–20 bullets in total; tag MVP items with **(MVP)**.

By the end of the project the system will let users do the following. **(MVP)** marks what is in the mid-term plan agreed by the team (the mid-term tickets in `docs/planning/midterm-tickets.md`); the rest is planned for the final delivery (its "Deferred to final" list). The groups are the functional requirement groups of `02-requirements.md`.

**Accounts and administration (FR-AUTH, FR-ADM)**
- Visitors create an account and log in; every new account is a student **(MVP)**
- Platform administrators create student organizations and appoint each one's president **(MVP)**

**W1 Organizations and membership**
- Any logged-in user browses the organizations and sees their details, and sees their own organizations and roles **(MVP)**
- A president (or an administrator) adds members to an organization and removes them, and members see who belongs to their organization **(MVP)**
- A president changes a member's role, a member leaves an organization and a president edits the organization's details (final delivery)

**W2 Event creation and lifecycle**
- Organizers create and edit events with title, description, date and time, place, category, capacity and an optional registration deadline **(MVP)**
- Organizers move an event through its lifecycle by publishing it, closing registration, starting it, completing it or cancelling it **(MVP)**

**W3 Event discovery and search**
- Students list upcoming events, filter them by category, organization and date, and search them by title **(MVP)**
- Students open an event and see what they need to decide: date, place, description, free places, registration deadline and status **(MVP)**
- Organizers see all events of their organization, drafts included **(MVP)**

**W4 Participant registration**
- Students register for a published event until it is full, without the event ever being overbooked **(MVP)**
- Students see their registrations, cancel one and register again **(MVP)**

**W5 Event organization**
- Organizers create tasks for an event, assign them to members of the organization and follow their status **(MVP)**
- Organizers see who registered and record who attended **(MVP)**
- Organizers post announcements that participants and members read in the application **(MVP)**
- Organizers edit and delete tasks and announcements (final delivery)

**Done when**
- [ ] Every workflow W1–W5 has at least one bullet.
- [ ] MVP items are marked and agreed with the team.
- [ ] Matches the FR groups in `02-requirements.md`.

**Open items for the team**
- The "(MVP)" tags follow the mid-term plan, not what is already built: parts of W2 (status changes), W3, W4 (cancelling) and W5 (check-in) are still planned. `02-requirements.md` gives the status of each requirement.
- The team should confirm the list before submission. The out-of-scope list (section 1.4) must not contradict it.

---

## 1.4 Out of scope

**Ticket:** `docs: define out of scope` · **Points:** 1

**What to write**
List what the system deliberately will **not** do, and one short reason for each. This
protects the team from scope creep and shows conscious decisions. Include things that
were decided in the data model and ADRs (e.g. no waitlist, no university SSO) and things
users may expect but we skip (e.g. payments, email notifications). If an item may come
later, say "not in this project" or "later, if time allows".

**Where to find the information**
- `docs/data-model.md` → "Design decisions" (D1 no waitlist, D2 free-text location, D4 no hard deletes)
- `docs/architecture/ADR-003-jwt-auth-spring-security.md` (no external IdP/SSO, no refresh tokens)
- `docs/architecture/ADR-001-...md` (no microservices)
- `06-ux-research.md` → assumptions table (features users asked for that we skip)

**Format**
Table: `Out of scope | Reason | Source`. 5–10 rows.

> **Example – replace:**
>
> | Out of scope | Reason | Source |
> |--------------|--------|--------|
> | Waitlist for full events | Keeps W4 simple for the MVP | data-model.md D1 |

**Done when**
- [ ] Each item has a reason and a source.
- [ ] No item contradicts the in-scope list or `02-requirements.md`.

> TODO (team): out-of-scope table here.
