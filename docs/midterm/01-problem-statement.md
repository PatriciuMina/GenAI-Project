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

Student organizations at Babeș-Bolyai University run events such as workshops, career fairs and cultural evenings, but each organization handles an event on its own. Students hear about events through the organizations' social media accounts, messaging channels and word of mouth. Organizers collect sign-ups themselves, take attendance with a shared link where people write their names, and share the preparation work through links or inside the announcement. Every step happens in a different place.

**Pain points**
- Information about events is scattered over several channels, and events are sometimes announced late or not at all. *(Source: team experience, and a conversation with one person.)*
- Sign-ups are collected separately for each event, as far as we know with an online form, and counting the places is manual. *(Source: team experience, second-hand. That the counting is manual is an assumption.)*
- Some of the people who are expected do not come. *(Source: a conversation with one person.)*
- Attendance is taken by sharing a link where people write their own names, and the record is kept only sometimes. *(Source: a conversation with one person, and team experience.)*
- Preparation work is shared through links or announcements, so no single place shows who is doing what. *(Source: team experience. The missing overview is an assumption.)*

The system aims to bring the announcement, sign-up, attendance and preparation of an event into one place, so that students can find events and organizers can run them with less manual work.

**Done when**
- [ ] The problem is described without naming technologies or features.
- [ ] Each pain point names its source (interview ID or team experience).
- [ ] 150–300 words.

**Open items for the team**
- "Team experience" here is what one team member knows, partly second-hand. Other members should add their own experience, or correct it.
- "A conversation with one person" is an informal talk, not an interview: the person's role (a student or an organizer) is not recorded here. The interviews (tickets T-52 to T-56) should confirm these points, and the sources should then become interview IDs (`I-NN`).
- The statements still marked "assumption" are what we expect to go wrong; nobody has confirmed them yet.

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

Every item below was decided by the team and is written down in the source named in the last column. Where a source says "for the mid-term" the item may be reconsidered for the final delivery; the capabilities that are only postponed (changing roles, leaving or editing an organization, editing and deleting tasks and announcements) are in the scope list (section 1.3), not here.

| Out of scope | Reason | Source |
|--------------|--------|--------|
| Waitlist for full events | Keeps registration simple: when an event is full, new registrations are rejected. A waitlist could be added later with a new migration | `docs/data-model.md` D1 |
| Login through an external identity provider or a university single sign-on | Heavy to set up and outside the external services the course allows; the application issues its own tokens | `docs/architecture/ADR-003-jwt-auth-spring-security.md` |
| Refresh tokens | An access token lasts 2 hours and the user then logs in again, which keeps authentication simple for the mid-term | ADR-003 "Implementation notes" |
| Splitting the backend into microservices | Unnecessary operational complexity for this scope; the backend is one application organised by feature | `docs/architecture/ADR-001-layered-architecture-feature-packages.md` |
| Automatic, time-based changes of an event's status | An organizer changes the status by pressing a button; there is no scheduler | Ticket T-12 and "Deferred to final" in `docs/planning/midterm-tickets.md` |
| Students joining an organization on their own | For the MVP the president, or an admin, adds an existing account by email and chooses its role | Ticket T-04 ("Decision for the MVP") in `docs/planning/midterm-tickets.md` |
| Email or push notifications to participants | Announcements are shown only inside the application | Ticket T-30 (assumption A-08) in `docs/planning/midterm-tickets.md` |
| Permanently deleting events that have registrations, users or organizations | Keeps the participation history: an event is cancelled instead of deleted | `docs/data-model.md` D4 |
| A separate table or map for locations | A free-text location is enough: no workflow needs a location table, and a table would add joins | `docs/data-model.md` D2 |
| Event categories created by users | The categories are a fixed list; adding one needs a database migration and a code change | `docs/data-model.md` D3 |

**Done when**
- [ ] Each item has a reason and a source.
- [ ] No item contradicts the in-scope list or `02-requirements.md`.

**Open items for the team**
- Payments and ticket sales, and an account profile or password reset, are not mentioned in any repository document, so they are not listed as decisions here. Add them if the team wants them written down as out of scope.
