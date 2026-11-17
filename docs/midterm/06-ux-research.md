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
- [x] Number and type of participants stated (aim: at least 3 students and 2 organizers).
- [x] Every interview in the log was done with consent.

**Method (status 2026-11-17).** So far **4 real interviews**, all online, conducted and noted
by Patriciu Mina (log in 6.3, notes in [`interviews/`](interviews/)). Participants were found
through the team's personal network and chosen to cover different kinds of users: three
students (a Master's student who works full time, a recent UBB Computer Science graduate,
and a student of another university who has never been to a UBB event) and one organizer
(a study group leader who organizes events for their study group). Students were asked
the student questions of the guide (6.2), the organizer the organizer questions; follow-ups
("Why?", "What happened next?") were added where useful. Before each interview the consent
note above was read and every participant agreed. Notes are anonymised: no names, contact
details or employers.

**Limitations.** The aim of at least two organizers is not met yet (one so far, and not from a
registered student organization); the interviews of the other team members (T-53 to T-56)
will be added to the log, and the findings below will be updated. Findings supported by a
single interview are marked as such.

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
Table: `ID | Participant type | Interviewer | Mode | Notes`.

**Done when**
- [ ] Every row links to an existing notes file.
- [ ] No names, emails or other identifying data in the table or notes.

| ID | Participant type | Interviewer | Mode | Notes |
|----|------------------|-------------|------|-------|
| I-01 | Student, Master's at FSEGA, works full time; former volunteer in a student organization | Patriciu Mina | Online | [I-01](interviews/I-01.md) |
| I-02 | UBB CS graduate, backend developer; never in a student organization | Patriciu Mina | Online | [I-02](interviews/I-02.md) |
| I-03 | Student at another university (medicine), never at a UBB event; team lead in a student organization | Patriciu Mina | Online | [I-03](interviews/I-03.md) |
| I-04 | Organizer: study group leader, CS, 2nd year | Patriciu Mina | Online | [I-04](interviews/I-04.md) |

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

**Done when**
- [x] Every finding cites at least one interview ID.
- [x] Every consequence links to an FR/UC/workflow or says "no change" with a reason.

| ID | Finding | Evidence (interviews) | Consequence for the product |
|----|---------|-----------------------|-----------------------------|
| F-01 | People find events through social media and chat groups, not by searching a university site; the institutional e-mail is not read. | I-01 (LinkedIn, Instagram), I-02 (year's Facebook group: "I just saw posts"), I-03 (a friend on WhatsApp), I-04 ("nobody reads the institutional email") | No change for the MVP: every event has its own page (`/events/{id}`) and the filtered list keeps its filters in the URL, so both can be shared in a chat (FR-W3-07, FR-W3-08). Main product flow 1 in 6.6 starts from a shared link. Confirms A-13. |
| F-02 | When an event is full, people who miss it are lost; organizers improvise a reserve list and move people up when others drop out. | I-01 (masterclass full, "keep me in mind" and never heard back), I-04 (26 people for 20 places: "I made a reserve list… the first three from the reserve moved up") | **Change:** waitlist for the final delivery, FR-W4-09, issue #199. Changes A-01. |
| F-03 | Participants follow events in their calendar, e-mail and chats; a change announced in only one channel does not reach everyone. | I-01 ("a calendar invite, and that was perfect"; change by e-mail), I-02 (Discord server), I-03 (group chat, screenshots, calendar), I-04 (cancellation in the chat, "but two people still came") | **Change:** calendar file and e-mail on change or cancellation for the final delivery, FR-W5-11, issue #200. Changes A-08. In-app announcements (FR-W5-08) stay. |
| F-04 | Registration forms that require a student e-mail exclude graduates and students of other universities, and events do not say who may attend. | I-02 (alumni meetup: "the form wanted a student email, and mine is deactivated"), I-03 ("I assumed it was only for UBB students"; "It didn't say if non-UBB students were allowed") | Keep: our accounts accept any e-mail (A-04). **Change:** the event shows who may attend, FR-W3-09, issue #201 (new assumption A-14). |
| F-05 | Whether to go depends on when and where the event is relative to the person's day (work, hospital, building and room). | I-01 (only evenings or weekends; distance from the office; "It was at 4 PM on a weekday"), I-03 (distance from the hospital), I-04 ("Room code plus building, because people get lost") | No change: cards and details show date, start and end time and the location text (FR-W3-04, FR-W3-07); organizers should write building and room in the location (A-03 kept). Filtering by time of day: open (I-01 only). |
| F-06 | Without a way to cancel, people simply do not show up; organizers need cancellations to free places. | I-01 ("I just didn't go… There was no option to do it"), I-04 (three drop-outs freed places for the reserve list) | No change: cancelling is implemented and frees the place (FR-W4-05, UC-42, "My registrations"). Confirms A-06. |
| F-07 | Check-in is a name list or QR scan, often done by volunteers or helpers when there is a queue; small events record nothing. | I-01 (volunteer with a name list), I-02 ("the organizers asked mentors to scan QR codes too"), I-03 (name list), I-04 ("I just counted people") | No change for the MVP: organizers check participants in from the participant list (FR-W5-07, W5 step 8). Letting more helpers check in stays open (A-07). |
| F-08 | A task shared by two people without one owner causes confusion. *(single interview)* | I-04 ("with two people on the money, it got confusing who had collected from whom") | No change: a task has at most one assignee (FR-W5-03, W5 step 2). Confirms A-09. |
| F-09 | The event description often misses what people need to decide (who comes, who may attend). | I-02 (career fair "didn't say which companies were coming"), I-03 (eligibility not stated) | Partly covered by F-04 (FR-W3-09). The description stays free text (FR-W2-02); no other change. |

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

**Done when**
- [x] Every row has interview evidence or is marked `open`.
- [x] Every `change` row names what was changed (file, FR, issue number).

| # | Assumption (current design) | Source in repo | Interview evidence | Keep / change | Change made |
|---|-----------------------------|----------------|--------------------|---------------|-------------|
| A-01 | No waitlist: when an event is full, new registrations are rejected | `docs/data-model.md` D1, `RegistrationStatus` | I-04 used a reserve list and moved people up; I-01 lost a place and was never contacted (F-02) | change | FR-W4-09 waitlist (priority later), issue #199; D1 in `docs/data-model.md` already allows adding a `WAITLISTED` status by a new migration |
| A-02 | A fixed list of event categories is enough (ACADEMIC, CAREER, CULTURAL, SOCIAL, SPORTS, VOLUNTEERING, WORKSHOP, OTHER) | data-model.md D3, `EventCategory` | No interviewee filtered by category; I-01 filters by time (F-05) | open | — |
| A-03 | Location as free text is enough (no room/building list, no map) | data-model.md D2 | I-01 and I-03 needed the distance from work or hospital and looked the venue up; I-04 writes room code plus building (F-05) | keep | Free text can hold building and room; a map link is not needed for the MVP. Reason: people mostly needed the exact place, which the text gives |
| A-04 | Anyone can create an account with any email (not only UBB addresses) | ADR-003 implementation notes, `RegisterRequest` | I-02 (alumnus) and I-03 (other university) were blocked by forms that required a student e-mail (F-04) | keep | Confirmed by contrast: our accounts accept any e-mail. See A-14 for what changed |
| A-05 | A registration deadline is optional; without one, registration is open until the event starts | data-model.md `ck_events_deadline_before_start` | I-01 missed a form that closed during the working day; I-04 sets a reply deadline and people answer late | keep | The deadline stays optional and is shown on the event page (FR-W3-04). Reason: both organizers and students use deadlines; none asked for a different rule |
| A-06 | A student may cancel and later register again for the same event | data-model.md `ux_registrations_confirmed_once` | I-01 did not cancel because there was no way to, and just did not show up; I-04 needed drop-outs to free places (F-06) | keep | Confirmed; implemented as FR-W4-05 / FR-W4-06 |
| A-07 | Only organizers of the organization check participants in | data-model.md service rule 5 | I-01 and I-03: a volunteer checks a name list; I-02: mentors helped scan QR codes because of a queue; I-04 records nothing (F-07) | open | Challenged by I-02: helpers who are not organizers did check-in. Kept for the MVP (only organizers, FR-W5-07) because check-in data is personal; to discuss with more organizer interviews |
| A-08 | Announcements are shown only in the app (no email or push notifications) | `Announcement` entity, data-model.md | All four rely on calendar, e-mail, Discord or chats; I-04: a cancellation posted in one place did not reach two people (F-03) | change | FR-W5-11 calendar file and e-mail on change/cancellation (priority later), issue #200; in-app announcements stay |
| A-09 | Each organizer task has at most one assignee | `EventTask.assignee` (column `assignee_membership_id`), data-model.md | I-04: a task shared by two people caused confusion (F-08, single interview) | keep | Confirmed; one assignee per task (FR-W5-03) |
| A-10 | Three organization roles (PRESIDENT, ORGANIZER, MEMBER) are enough | `OrganizationRole`, `OrgPermissionService` | I-01: volunteer → coordinator; I-03: volunteer → project team lead; I-04: elected leader, others help | open | Organizations have more levels (coordinator, team lead) than our three roles, but they map to ORGANIZER for what the app does. Not enough evidence to add roles |
| A-11 | An English-only user interface is acceptable | `frontend/src` (all texts in English) | I-01, I-02: English is fine; I-03 prefers Romanian, but says English-line students need English | open | Mixed answers; English stays for the MVP, a Romanian translation is a candidate for the final delivery |
| A-12 | Only organizers and presidents create events; regular members cannot | `OrgPermissionService` (`isOrganizer`), `03-actors-roles.md` | I-04: the group leader creates and announces events | keep | Confirmed for the organizer interviewed; more organizer interviews needed |
| A-13 | Students currently find events mainly through social media and word of mouth | team assumption (problem statement) | I-01 LinkedIn/Instagram, I-02 Facebook group, I-03 WhatsApp, I-04 group chat (F-01) | keep | Confirmed; shareable event links (FR-W3-07, FR-W3-08) support it |
| A-14 *(new)* | Every logged-in user may register for any published event, and nothing more needs to be said about who may attend | `RegistrationService.register` | I-03 did not register because the event "didn't say if non-UBB students were allowed"; I-02 was blocked by a student-only form (F-04) | change | FR-W3-09 "who may attend" shown on the event (priority later), issue #201 |

**Summary.** Of 14 assumptions, 3 were changed by the interviews (A-01 waitlist, A-08
notifications outside the app, A-14 who may attend), 7 were kept with interview support,
and 4 stay open until more organizer interviews are available. The changes are planned for
the final delivery; the mid-term MVP was not changed.

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

**Done when**
- [x] Order is justified by findings (F-IDs).
- [x] Each flow maps to workflows and use cases.

1. **Open a shared event and decide whether to go** (F-01, F-05, F-09: events reach people as links in chats; the decision depends on time, place and details)
   open the link from a chat → log in → event page: date and time, location, description, free places, deadline → decide.
   Implemented by: W3, UC-32
2. **Take a place before the event fills up** (F-02, F-05: places run out; people check time first)
   Events → filter by date or search → event page → Register → "You are registered"; if full or closed, a clear message.
   Implemented by: W3, W4, UC-30, UC-31, UC-41
3. **Cancel when plans change, so the place is freed** (F-06: without cancelling people just do not come)
   My registrations → Upcoming → Cancel → confirm → the row moves to Past and cancelled and the place is free again.
   Implemented by: W4, UC-40, UC-42
4. **Organizer: tell participants about a change** (F-03: changes must reach people)
   Organizing → event → post an announcement; participants read it on the event page. Final delivery: calendar file and e-mail (FR-W5-11).
   Implemented by: W2, W5, UC-22, UC-53, UC-54
5. **Organizer: check people in at the door** (F-07: name list or scan at the entrance)
   Organizing → event → Start → Participants → search the name → Check in.
   Implemented by: W2, W5, UC-23, UC-51, UC-52

---

## 6.7 Personas

**Ticket:** `research: personas` · **Points:** 2

**What to write**
2–3 fictional personas built only from the findings: name, short description, goals,
frustrations, typical scenario, workflows used, and the findings and interviews they are
based on. They map to the actors in `03-actors-roles.md`.

**Done when**
- [x] 2–3 personas, each citing F-IDs and I-IDs.
- [x] Each persona maps to actors in `03-actors-roles.md`.

### Andra – the working Master's student
- **Actor:** Student (participant).
- **Description:** Master's student who works full time; used to volunteer in a student organization.
- **Goals:** find evening or weekend events worth her time; not lose a place because she could not check her phone; have the event in her calendar.
- **Frustrations:** events at 4 PM on a weekday; forms that close during the working day; no way to cancel, so she just does not show up.
- **Typical scenario:** sees an event on LinkedIn, opens the link in the evening, checks the time and place, registers, adds it to her calendar; cancels in "My registrations" when work runs late.
- **Workflows:** W3, W4 (flows 1, 2, 3 in 6.6).
- **Based on:** F-01, F-02, F-03, F-05, F-06 · I-01.

### Matei – the student from outside UBB
- **Actor:** Student (participant without a UBB e-mail).
- **Description:** student of another university or recent graduate, active in his own community; hears about UBB events from friends.
- **Goals:** know quickly whether he may attend and whether it is worth it (who speaks, which companies come).
- **Frustrations:** forms that require a UBB student e-mail; events that do not say who may attend.
- **Typical scenario:** a friend sends a link on WhatsApp; he creates an account with his own e-mail, checks who the event is for and how far it is, then registers or gives up.
- **Workflows:** W3, W4 (flows 1, 2 in 6.6).
- **Based on:** F-01, F-04, F-05, F-09 · I-02, I-03.

### Bianca – the student organizer
- **Actor:** Organizer (or President) of an organization.
- **Description:** organizes events for her colleagues next to her studies; is the contact person, so most of the work lands on her.
- **Goals:** know who comes; handle more interested people than places; reach everyone when something changes; give each task a clear owner.
- **Frustrations:** people who answer after the deadline; a cancellation that did not reach everyone; shared tasks without an owner; counting people by hand.
- **Typical scenario:** creates and publishes the event, splits the work into tasks with one assignee each, posts an announcement when the room changes, checks people in at the door.
- **Workflows:** W2, W5 (flows 4, 5 in 6.6).
- **Based on:** F-02, F-03, F-07, F-08 · I-04 (one organizer so far; to be checked against the other organizer interviews).

