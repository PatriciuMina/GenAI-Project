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
> course project. We would like to ask about your experience finding, attending or
> organizing events. The interview takes about 20–30 minutes. We will take written notes
> and will not record audio unless you separately agree. Please do not share your name,
> contact details, student ID or other identifying details; we will leave identifying
> details out of the notes. Notes will be stored under an interview ID in our project
> repository. Anyone with access to that repository may read them, and we may use
> anonymised quotations in our report. You can skip any question or stop at any time. Do
> you agree to participate?"

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
- [x] The team agreed on the final questions before the first interview.
- [x] Every assumption in 6.5 is covered by at least one question.

### Students (participants)
1. Think about the most recent student event you attended. How did you first hear about it, and what did you do next? *(covers A-13)*
2. The last time you looked for a UBB event, where did you look, and how did you narrow down what you wanted? *(covers A-02, A-13)*
3. Recall the last event you considered attending. What did you need to know about its location, and how did you find that out? *(covers A-03)*
4. Walk me through the last time you registered for a student event that required sign-up. What information did the form ask for, and what requirements, if any, did it place on the account or email? Please do not share the actual email address. *(covers A-04)*
5. Tell me about a time you wanted to attend a student event but could not register. What happened, and what did you learn about availability or timing? *(covers A-01, A-05)*
6. Tell me about the last time you changed or cancelled a registration. What happened afterward? If you later wanted to attend, what did you do? *(covers A-06)*
7. After registering for a student event, how did you keep track of the event details and any updates? Tell me about the most recent time. *(covers A-08)*
8. If an event you registered for has changed or been cancelled, tell me about the most recent example. How did you find out, and what did you do next? *(covers A-08)*
9. Tell me about your most recent arrival at a student event. What happened at check-in, if anything? *(covers A-07)*
10. Tell me how you joined your current or most recent student organization and what responsibilities or roles you took on over time. *(covers A-10)*
11. Think about the last university-related app or website you used. What language was it in, and how, if at all, did that affect what you did? *(covers A-11)*
12. Recall a recent event you decided not to attend. What information about the event, if any, affected that decision? *(covers A-02, A-03, A-05)*

### Event organizers (organizers / presidents)
1. Walk me through the most recent event your organization planned, from idea through follow-up. Who was involved at each stage? *(covers A-09, A-12)*
2. For your most recent event, who created and announced it, and how was that responsibility decided? *(covers A-12)*
3. Tell me how participants registered for your most recent event. What steps and requirements did they encounter? *(covers A-04, A-05)*
4. Tell me about the most recent time an event filled up or registration closed before it started. What happened next? *(covers A-01, A-05)*
5. For your last event, how were preparation tasks assigned and followed up? What happened when a task involved more than one person? *(covers A-09)*
6. How did you communicate with participants before your most recent event? What updates did you send and how? *(covers A-08)*
7. Describe what happened when participants arrived at your last event. How did you record attendance, if you did? *(covers A-07)*
8. Tell me about the last event your organization postponed or cancelled. How did you notify people and handle registrations? *(covers A-06, A-08)*
9. Tell me how new members joined or took on responsibilities in your organization most recently. What changed over time? *(covers A-10)*
10. For your latest event, what information did you publish, and how did you decide how to describe its type, venue and registration timing? *(covers A-02, A-03, A-05)*
11. Think about the last event you organized. How did your team divide the work, and what part was hardest to coordinate? *(covers A-09)*
12. How did students first hear about your most recent event, as far as you know? What makes you think that? *(covers A-13)*

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
| I-01 | _to add_ | Student, Master's at FSEGA, works full time; former volunteer in a student organization | Patriciu Mina | _to add_ | [I-01](interviews/I-01.md) |
| I-02 | _to add_ | UBB CS graduate, backend developer; never in a student organization | Patriciu Mina | _to add_ | [I-02](interviews/I-02.md) |
| I-03 | _to add_ | Student at another university (medicine), never at a UBB event; team lead in a student organization | Patriciu Mina | _to add_ | [I-03](interviews/I-03.md) |
| I-04 | _to add_ | Organizer: study group leader, CS, 2nd year | Patriciu Mina | _to add_ | [I-04](interviews/I-04.md) |

> TODO (team): log the remaining interviews here.

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
