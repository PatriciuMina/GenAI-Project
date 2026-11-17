# 1. Problem statement and project scope

Part **B.1** of the mid-term deliverable. Index: [README](README.md).

---

## 1.1 Problem

Student organizations at Babeș-Bolyai University run workshops, career fairs, hackathons and
cultural evenings, but each organization, and often each event, is handled on its own.
Students hear about events from LinkedIn, Instagram, Facebook groups and chat groups, while
the institutional e-mail is hardly read. Sign-up happens in a different form, poll or
spreadsheet every time, and attendance, preparation work and changes are managed by hand.

**Pain points** (sources: interviews I-01 to I-04, section 6; findings F-NN in 6.4)
- **Information is scattered.** Events reach people as posts and forwarded links, and
  important details are often missing, such as who may attend or who will be there.
  *(I-01, I-02, I-03, I-04; F-01, F-09)*
- **Registration is manual and sometimes excludes people.** Each event has its own form,
  poll or sheet; some forms accept only a UBB student e-mail, which shuts out graduates
  and students of other universities. *(I-02, I-03, I-04; F-04)*
- **Places run out and the people who missed them are lost.** Organizers keep reserve lists
  by hand; students who could not sign up in time are not contacted again.
  *(I-01, I-04; F-02)*
- **Plans change, but there is no easy way to cancel or to hear about changes.** People
  who cannot come simply do not show up, and a cancellation posted in one place does not
  reach everyone. *(I-01, I-04; F-03, F-06)*
- **Attendance and preparation are informal.** Check-in is a paper list or a head count,
  and shared tasks without a clear owner cause confusion. *(I-01, I-02, I-03, I-04; F-07, F-08)*

This matters because students miss events that would interest them, places stay empty
while others wanted them, and organizers spend their time on lists and messages instead of
on the event. The system brings finding, registering, cancelling, attendance, preparation
and announcements for student-organization events into one place.

---

## 1.2 Stakeholders

The roles below are the ones in the code (global role `STUDENT` or `ADMIN` in `GlobalRole`, organization role `MEMBER`, `ORGANIZER` or `PRESIDENT` in `OrganizationRole`), plus the visitor who is not logged in. Each goal follows from what the course statement expects the system to cover (R2, R3, R5, R6) and from what the role can do in the application today or in the agreed plan. The goals of students and organizers were checked in four interviews (I-01 to I-04); the findings are in `06-ux-research.md` §6.4 and the personas in §6.7.

| Stakeholder | Uses the system? | Main interest / goal |
|-------------|------------------|----------------------|
| Visitor (not logged in) | Yes | Create an account and log in, to reach the events of student organizations |
| Student (participant, global role STUDENT) | Yes | Find events that suit them and the information needed to decide (date, place, free places, deadline), get a guaranteed place without overbooking, see and cancel their registrations, and read announcements about the events they registered for (R3, R6) |
| Organization member (MEMBER) | Yes | Belong to an organization, see who else is in it, and follow the announcements of its events (R2) |
| Organizer (ORGANIZER) | Yes | Prepare and run the organization's events: create, edit and publish events, share the preparation work in tasks, see who registered, record who attended and announce changes to participants (R1, R5) |
| President (PRESIDENT) | Yes | Everything an organizer does, and keep the organization's member list up to date by adding and removing members (R2) |
| Platform administrator (ADMIN) | Yes | Set up student organizations on the platform and appoint each one's president, so that organizations can start using the system (R2) |
| Course staff (instructors and graders) | No, they review the result | Check that the project covers R1 to R16 and that the use of GenAI is documented and visibly overseen by the students (R13, R14, R15) |

---

## 1.3 Scope (in scope)

By the end of the project the system will let users do the following. **(MVP)** marks what is in the mid-term plan agreed by the team (the mid-term tickets in `docs/planning/midterm-tickets.md`); all MVP items are implemented on `main`. The rest is planned for the final delivery (the "Deferred to final" list and the requirements added after the interviews). The groups are the functional requirement groups of `02-requirements.md`.

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
- An event states who may attend, for example UBB students only or everyone (final delivery; added after the interviews, FR-W3-09)
- Organizers see all events of their organization, drafts included **(MVP)**

**W4 Participant registration**
- Students register for a published event until it is full, without the event ever being overbooked **(MVP)**
- Students see their registrations, cancel one and register again **(MVP)**
- Students join a waitlist when an event is full and get the place when someone cancels (final delivery; added after the interviews, FR-W4-09)

**W5 Event organization**
- Organizers create tasks for an event, assign them to members of the organization and follow their status **(MVP)**
- Organizers see who registered and record who attended **(MVP)**
- Organizers post announcements that participants and members read in the application **(MVP)**
- Organizers edit and delete tasks and announcements (final delivery)
- Participants add an event to their calendar and get an e-mail when it changes or is cancelled (final delivery; added after the interviews, FR-W5-11)

---

## 1.4 Out of scope

Every item below was decided by the team and is written down in the source named in the last column. Where a source says "for the mid-term" the item may be reconsidered for the final delivery; the capabilities that are only postponed (changing roles, leaving or editing an organization, editing and deleting tasks and announcements) are in the scope list (section 1.3), not here. Two items that were out of scope before the interviews, a waitlist and e-mail about changes, moved into the scope for the final delivery (`06-ux-research.md` §6.5, A-01 and A-08).

| Out of scope | Reason | Source |
|--------------|--------|--------|
| Login through an external identity provider or a university single sign-on | Heavy to set up and outside the external services the course allows; the application issues its own tokens | `docs/architecture/ADR-003-jwt-auth-spring-security.md` |
| Refresh tokens | An access token lasts 2 hours and the user then logs in again, which keeps authentication simple for the mid-term | ADR-003 "Implementation notes" |
| Splitting the backend into microservices | Unnecessary operational complexity for this scope; the backend is one application organised by feature | `docs/architecture/ADR-001-layered-architecture-feature-packages.md` |
| Automatic, time-based changes of an event's status | An organizer changes the status by pressing a button; there is no scheduler | Ticket T-12 and "Deferred to final" in `docs/planning/midterm-tickets.md` |
| Students joining an organization on their own | For the MVP the president, or an admin, adds an existing account by email and chooses its role | Ticket T-04 ("Decision for the MVP") in `docs/planning/midterm-tickets.md` |
| Push notifications to phones | Announcements in the application, plus a calendar file and e-mail on changes in the final delivery, cover what the interviews asked for (F-03) | `06-ux-research.md` A-08 |
| Permanently deleting events that have registrations, users or organizations | Keeps the participation history: an event is cancelled instead of deleted | `docs/data-model.md` D4 |
| A separate table or map for locations | A free-text location is enough: no workflow needs a location table, and a table would add joins | `docs/data-model.md` D2 |
| Event categories created by users | The categories are a fixed list; adding one needs a database migration and a code change | `docs/data-model.md` D3 |
