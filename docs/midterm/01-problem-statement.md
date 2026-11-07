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

> **Example – replace:**
>
> | Stakeholder | Uses the system? | Main interest / goal |
> |-------------|------------------|----------------------|
> | Student (participant) | Yes | Find interesting events and get a guaranteed place |

**Done when**
- [ ] Every actor from `03-actors-roles.md` appears as a stakeholder.
- [ ] At least one stakeholder who does not use the system directly is listed.
- [ ] Each row has a concrete goal (not "use the app").

> TODO (team): stakeholders table here.

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

> **Example – replace:**
> - **W4 Registration:** students register for a published event until it is full **(MVP)**

**Done when**
- [ ] Every workflow W1–W5 has at least one bullet.
- [ ] MVP items are marked and agreed with the team.
- [ ] Matches the FR groups in `02-requirements.md`.

> TODO (team): in-scope list here.

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
