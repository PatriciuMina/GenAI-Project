# 6. UX / user research

Part **C** of the mid-term deliverable. Index: [README](README.md).

Course link: R10 ("important workflows validated with users") and R5 ("justified by
identified user needs"), see `docs/course/course-requirements.md`.

**Rule: no invented results.** Every finding must point to a real interview (`I-NN`) whose
anonymised notes are in [`interviews/`](interviews/). If an interview has not happened,
its row stays empty.

---

## 6.1 Method and consent

**Ticket:** `docs: research method + consent` · **Points:** 1

**What to write**
Who you interviewed (types of people, how many, how you found them), when, how long, in
person or online, and who took notes. Adapt the consent note below if needed and confirm
that every participant agreed to it before the interview.

**Where to find the information**
- This file's interview guide (6.2) · `interviews/README.md` (note template)
- Your own interview schedule

**Format**
Prose, 100–200 words + the consent note.

**Consent note (read to each participant before starting):**
> "We are students at UBB building an event management app for student organizations as a
> course project. We would like to ask you some questions about how you find, attend or
> organize events. The interview takes about 20–30 minutes. We will take written notes but
> will not record audio unless you agree. We will **not** store your name, email or any
> information that identifies you; your answers are stored anonymously as 'Interview I-NN'
> in our project repository and may be quoted anonymously in our report. You can skip any
> question or stop at any time. Do you agree to participate?"

**Done when**
- [ ] Number and type of participants stated (aim: at least 3 students and 2 organizers).
- [ ] Every interview in the log was done with consent.

> TODO (team): method description here.

---

## 6.2 Interview guide

**Ticket:** `docs: review interview guide` · **Points:** 1

**What to write**
The questions below are a starting point. Review them as a team, adjust wording, and keep
them **open** (no yes/no, no leading "Would you like feature X?"). Ask about past behaviour
("Tell me about the last time …") rather than opinions about the future. Ask follow-ups
("Why?", "What happened next?").

**Done when**
- [ ] The team agreed on the final questions before the first interview.
- [ ] Every assumption in 6.5 is covered by at least one question.

### Students (participants)
1. Tell me about the last student event you attended. How did you find out about it?
2. Where do you usually look when you want to know what events are happening at UBB?
3. What information do you need about an event before you decide to go?
4. Describe how you signed up for the last event that required registration. What was easy or annoying?
5. Tell me about a time you wanted to attend an event but could not. What happened?
6. What do you do when you registered for an event but then cannot go?
7. How do you keep track of the events you signed up for?
8. How did you receive updates or changes about an event (time, room, cancellation)? How did that work for you?
9. What happens when you arrive at an event? How is attendance taken, if at all?
10. Are you a member of any student organization? How did you join, and what is your role?
11. In which language(s) do you prefer to use university-related apps and websites, and why?
12. If you could change one thing about how student events work today, what would it be?

### Event organizers (organizers / presidents)
1. Walk me through how your organization planned its most recent event, from idea to the day after.
2. Who in your organization is allowed to create or announce an event? How is that decided?
3. How do you collect registrations today? What tools do you use?
4. Tell me about a time when more people wanted to attend than you had places. What did you do?
5. How do you split the preparation work among members, and how do you follow up on it?
6. How do you communicate with registered participants before the event (reminders, changes)?
7. How do you check who actually attended? What do you do with that information afterwards?
8. Tell me about an event that was cancelled or postponed. How did you handle it?
9. How do new members join your organization, and how do roles change over time?
10. What information about an event (category, location, deadline, …) do you always publish? What is often missing?
11. What is the most time-consuming or frustrating part of organizing an event?
12. Where do students mostly find out about your events today?

---

## 6.3 Interview log

**Ticket:** `research: conduct interviews` · **Points:** 3 (one ticket per interviewer is fine)

**What to write**
One row per interview. Write the anonymised notes in `interviews/I-NN.md` using the
template in `interviews/README.md` within 24 hours of the interview.

**Format**
Table: `ID | Date | Participant type | Interviewer | Duration | Notes`.

> **Example – replace:** (format only, not a real interview)
>
> | ID | Date | Participant type | Interviewer | Duration | Notes |
> |----|------|------------------|-------------|----------|-------|
> | I-01 | YYYY-MM-DD | Student, 2nd year, not in an organization | <team member> | 25 min | [I-01](interviews/I-01.md) |

**Done when**
- [ ] Every row links to an existing notes file.
- [ ] No names, emails or other identifying data in the table or notes.

| ID | Date | Participant type | Interviewer | Duration | Notes |
|----|------|------------------|-------------|----------|-------|

> TODO (team): log interviews here.

---

## 6.4 Findings

**Ticket:** `docs: synthesise interview findings` · **Points:** 2

**What to write**
Group what you heard into findings: patterns that appear in more than one interview, or a
single strong observation (say so). Each finding states what users do or need, the evidence
(interview IDs, short anonymised quotes), and the consequence for the product (new or
changed FR, UC, workflow step, or "no change").

**Where to find the information**
- `interviews/I-*.md` · `02-requirements.md`, `04-use-cases.md` (to link consequences)

**Format**
Table: `ID | Finding | Evidence (interviews) | Consequence for the product`. 5–10 rows.

> **Example – replace:** (format only, not a real finding)
>
> | ID | Finding | Evidence | Consequence |
> |----|---------|----------|-------------|
> | F-01 | <what users do/need> | I-02, I-04: "<short quote>" | <e.g. new FR-W5-07 / no change> |

**Done when**
- [ ] Every finding cites at least one interview ID.
- [ ] Every consequence links to an FR/UC/workflow or says "no change" with a reason.

> TODO (team): findings table here.

---

## 6.5 Assumptions we checked (and changed)

**Ticket:** `docs: assumptions validated by interviews` · **Points:** 2

**What to write**
These are design choices the team made **before** talking to users. For each, fill in what
the interviews showed, decide whether to keep or change it, and (if changed) what was
changed in the docs/code. A changed assumption is the most valuable evidence for the course
(part C: "changes to initial assumptions caused by the interviews"). Add new rows if the
interviews challenged something not listed.

**Format**
The table below. "Keep / change" = `keep`, `change`, or `open` (not enough evidence).

> **Example – replace:** (format only)
>
> | A-01 | … | … | I-03: "<quote>"; I-05 agreed | change | FR-W4-06 added; data-model migration V? |

**Done when**
- [ ] Every row has interview evidence or is marked `open`.
- [ ] Every `change` row names what was changed (file, FR, issue number).

| # | Assumption (current design) | Source in repo | Interview evidence | Keep / change | Change made |
|---|-----------------------------|----------------|--------------------|---------------|-------------|
| A-01 | No waitlist: when an event is full, new registrations are rejected | `docs/data-model.md` D1, `RegistrationStatus` | | | |
| A-02 | A fixed list of event categories is enough (ACADEMIC, CAREER, CULTURAL, SOCIAL, SPORTS, VOLUNTEERING, WORKSHOP, OTHER) | data-model.md D3, `EventCategory` | | | |
| A-03 | Location as free text is enough (no room/building list, no map) | data-model.md D2 | | | |
| A-04 | Anyone can create an account with any email (not only UBB addresses) | ADR-003 implementation notes, `RegisterRequest` | | | |
| A-05 | A registration deadline is optional; without one, registration is open until the event starts | data-model.md `ck_events_deadline_before_start` | | | |
| A-06 | A student may cancel and later register again for the same event | data-model.md `ux_registrations_confirmed_once` | | | |
| A-07 | Only organizers of the organization check participants in | data-model.md service rule 5 | | | |
| A-08 | Announcements are shown only in the app (no email or push notifications) | `Announcement` entity, data-model.md | | | |
| A-09 | Each organizer task has at most one assignee | `EventTask.assignee` (column `assignee_membership_id`), data-model.md | | | |
| A-10 | Three organization roles (PRESIDENT, ORGANIZER, MEMBER) are enough | `OrganizationRole`, `OrgPermissionService` | | | |
| A-11 | An English-only user interface is acceptable | `frontend/src` (all texts in English) | | | |
| A-12 | Only organizers and presidents create events; regular members cannot | `OrgPermissionService` (`isOrganizer`), `03-actors-roles.md` | | | |
| A-13 | Students currently find events mainly through social media and word of mouth | team assumption (problem statement) | | | |

> TODO (team): fill evidence and decisions after the interviews.

---

## 6.6 Main product flows

**Ticket:** `docs: main product flows from research` · **Points:** 1

**What to write**
From the user's perspective (not the system's), list the 3–5 most important flows the
product must support well, ordered by importance according to the interviews. For each:
the user's goal, the steps they expect, and the workflow (W1–W5) that implements it. These
are the flows to validate with users in the UI later (R10).

**Where to find the information**
- 6.4 findings · `05-workflows.md` · `04-use-cases.md`

**Format**
Numbered list; per flow 3–6 short steps and a line `Implemented by: W?, UC-…`.

> **Example – replace:** (format only)
>
> 1. **Find an event for this week and save a place** (most mentioned, F-0?)
>    open events → filter by this week → open details → register.
>    Implemented by: W3, W4, UC-31, UC-41

**Done when**
- [ ] Order is justified by findings (F-IDs).
- [ ] Each flow maps to workflows and use cases.

> TODO (team): main product flows here.
