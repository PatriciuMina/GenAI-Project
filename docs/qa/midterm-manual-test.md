# Mid-term manual test (T-99)

| Field | Value |
|-------|-------|
| Date | 2026-11-17 |
| Commit of `main` | `7f00227` (T-25 merged; T-44 merged afterwards as `3bdb0ca`, frontend empty states only) |
| Setup | Fresh database: `docker compose -p genai-qa up --build -d --wait` (own volume, the normal dev volume is untouched), dev seed users, password `password` |
| Part 1: API | Run by `scripts/midterm-api-check.py` (Python, standard library), results below |
| Part 2: browser | **To do by a person**: browser, version and results to be filled in below |

Every step was run against the real stack (nginx on :3000, Spring Boot on :8080, PostgreSQL 16).
Each run creates its own organization and events with a time stamp in the name, so it can be repeated.

## How to repeat

```bash
docker compose -p genai-qa up --build -d --wait   # fresh stack with its own database
python3 scripts/midterm-api-check.py /tmp/qa-results.json
docker compose -p genai-qa down -v                # removes only the QA stack and its volume
```

The script prints `N pass, M fail` and every failed step. It takes about 1–2 minutes because it waits
for a short test event to end before completing it.

## Part 1: API results (57 of 57 passed)

| # | Workflow | Step | Account | Expected | Actual | Result |
|---|----------|------|---------|----------|--------|--------|
| 1 | W1 | Admin creates an organization with andrei.toma as president | admin | 201 | 201 | pass |
| 2 | W1 | A student cannot create an organization | ioana.rusu | 403 | 403 FORBIDDEN | pass |
| 3 | W1 | President adds a MEMBER by email | andrei.toma | 201 | 201 | pass |
| 4 | W1 | President adds an ORGANIZER by email | andrei.toma | 201 | 201 | pass |
| 5 | W1 | Adding the same person twice is rejected | andrei.toma | 409 | 409 ALREADY_MEMBER | pass |
| 6 | W1 | A member sees the members list (3 people) | ioana.rusu | 200 | 200 | pass |
| 7 | W1 | An outsider cannot see the members list | maria.dobre | 403 | 403 FORBIDDEN | pass |
| 8 | W1 | President removes the member | andrei.toma | 204 | 204 | pass |
| 9 | W1 | Members list has 2 people after the removal | andrei.toma | 200 | 200 | pass |
| 10 | W2 | Organizer creates a draft event | elena.marin | 201 | 201 | pass |
| 11 | W2 | A non-member cannot create an event | maria.dobre | 403 | 403 FORBIDDEN | pass |
| 12 | W2 | End before start is rejected | elena.marin | 400 | 400 VALIDATION_FAILED | pass |
| 13 | W2 | A draft is hidden from other users | maria.dobre | 404 | 404 NOT_FOUND | pass |
| 14 | W2 | Organizer edits the draft (version goes up) | elena.marin | 200 | 200 | pass |
| 15 | W2 | Edit with an old version is rejected | elena.marin | 409 CONCURRENT_UPDATE | 409 CONCURRENT_UPDATE | pass |
| 16 | W2 | A non-organizer cannot publish | maria.dobre | 403 | 403 FORBIDDEN | pass |
| 17 | W2 | Organizer publishes the event | elena.marin | 200 | 200 | pass |
| 18 | W2 | Start 3 days early is rejected | elena.marin | 409 INVALID_STATUS_TRANSITION | 409 INVALID_STATUS_TRANSITION | pass |
| 19 | W2 | Organizer cancels another event | elena.marin | 200 | 200 | pass |
| 20 | W2 | A member has no ORGANIZER/PRESIDENT role (Organizing menu hidden) | maria.dobre | 200 | 200 | pass |
| 21 | W3 | Text search finds the published event | maria.dobre | 200 | 200 | pass |
| 22 | W3 | A DRAFT never appears in the search | elena.marin | 200 | 200 | pass |
| 23 | W3 | A CANCELLED event does not appear in the search | maria.dobre | 200 | 200 | pass |
| 24 | W3 | Category filter excludes the WORKSHOP event | maria.dobre | 200 | 200 | pass |
| 25 | W3 | Unknown category is rejected | maria.dobre | 400 INVALID_PARAMETER | 400 INVALID_PARAMETER | pass |
| 26 | W3 | Details show 10 free places | maria.dobre | 200 | 200 | pass |
| 27 | W4 | Student registers | maria.dobre | 201 | 201 | pass |
| 28 | W4 | The registration is in My registrations | maria.dobre | 200 | 200 | pass |
| 29 | W4 | Registering twice is rejected | maria.dobre | 409 ALREADY_REGISTERED | 409 ALREADY_REGISTERED | pass |
| 30 | W4 | Student cancels | maria.dobre | 204 | 204 | pass |
| 31 | W4 | Student registers again | maria.dobre | 201 | 201 | pass |
| 32 | W4 | Two rows kept: one CANCELLED, one CONFIRMED | elena.marin | 200 | 200 | pass |
| 33 | W4 | Two students take the last place at the same time: one 201, one 409 | ioana.rusu + andrei.toma | 201 + 409 | 201 + 409 | pass |
| 34 | W4 | Full event is rejected | maria.dobre | 409 EVENT_FULL | 409 EVENT_FULL | pass |
| 35 | W4 | Closed registration is rejected | maria.dobre | 409 REGISTRATION_CLOSED | 409 REGISTRATION_CLOSED | pass |
| 36 | W5 | Organizer creates a task with an assignee (starts TODO) | elena.marin | 201 | 201 | pass |
| 37 | W5 | Organizer changes the task status | elena.marin | 200 | 200 | pass |
| 38 | W5 | Assignee outside the organization is rejected | elena.marin | 400 | 400 ASSIGNEE_NOT_IN_ORGANIZATION | pass |
| 39 | W5 | A participant cannot see the tasks | maria.dobre | 403 | 403 FORBIDDEN | pass |
| 40 | W5 | Organizer posts an announcement | elena.marin | 201 | 201 | pass |
| 41 | W5 | A registered participant reads it | maria.dobre | 200 | 200 | pass |
| 42 | W5 | An unrelated user cannot read it | radu.pop | 403 | 403 FORBIDDEN | pass |
| 43 | W5 | Check-in while the event is PUBLISHED is rejected | elena.marin | 409 CHECK_IN_NOT_ALLOWED | 409 CHECK_IN_NOT_ALLOWED | pass |
| 44 | W2 | Organizer starts the event (within one hour of the start) | elena.marin | 200 | 200 | pass |
| 45 | W5 | Check-in at an ONGOING event | elena.marin | 200 | 200 | pass |
| 46 | W5 | Second check-in is rejected | elena.marin | 409 ALREADY_CHECKED_IN | 409 ALREADY_CHECKED_IN | pass |
| 47 | W4 | A checked-in participant cannot cancel | maria.dobre | 409 CANCEL_NOT_ALLOWED | 409 CANCEL_NOT_ALLOWED | pass |
| 48 | W2 | Complete before the end is rejected | elena.marin | 409 INVALID_STATUS_TRANSITION | 409 INVALID_STATUS_TRANSITION | pass |
| 49 | W2 | Organizer completes the event after its end | elena.marin | 200 | 200 | pass |
| 50 | W5 | No announcement on a COMPLETED event | elena.marin | 409 ANNOUNCEMENT_NOT_ALLOWED | 409 ANNOUNCEMENT_NOT_ALLOWED | pass |
| 51 | Errors | Request without a token | - | 401 | 401 UNAUTHORIZED | pass |
| 52 | Errors | Request with an invalid token | - | 401 | 401 UNAUTHORIZED | pass |
| 53 | Errors | Unknown event | maria.dobre | 404 | 404 NOT_FOUND | pass |
| 54 | Frontend | nginx serves the app at / | - | 200 | 200 | pass |
| 55 | Frontend | nginx serves the app at /events | - | 200 | 200 | pass |
| 56 | Frontend | nginx serves the app at /my-registrations | - | 200 | 200 | pass |
| 57 | Frontend | nginx serves the app at /organizing | - | 200 | 200 | pass |

No step failed, so no bug issue was opened.

## Part 2: browser checks (to do by a person)

Open http://localhost:3000 on the fresh stack. Fill in the browser and the result of each step;
open a **Bug** issue for every failure and link it in the last column.

Browser: ______ (name and version)

| # | Workflow | Step | Account | Expected | Result |
|---|----------|------|---------|----------|--------|
| B1 | W1 | Admin page: create an organization with a president | admin@ubbevents.ro | Success message and link to the new organization | |
| B2 | W1 | Organization page: president adds and removes a member | ana.popescu | List updates without reload | |
| B3 | W1 | Same page as an outsider | andrei.toma | No member management controls | |
| B4 | W2 | Organizing → New event → create, edit, Publish | elena.marin | Status changes shown on the organizer event page | |
| B5 | W2 | A member does not see "Organizing" in the menu | ioana.rusu | Menu item missing | |
| B6 | W3 | Events: search text, category and date filters; Clear filters | maria.dobre | Results match; URL keeps the filters; empty state with "Clear filters" | |
| B7 | W3 | Event details | maria.dobre | Free places, deadline, status, announcements | |
| B8 | W4 | Register, then My registrations, Cancel, register again | maria.dobre | Row moves between Upcoming and Past and cancelled | |
| B9 | W4 | Two browsers take the last place of a 1-place event | two students | One succeeds, the other sees "Sorry, this event is full." | |
| B10 | W4 | My registrations as andrei.toma (seed) | andrei.toma | Workshop under Upcoming, Board Games Evening under Past and cancelled | |
| B11 | W5 | Tasks panel: create, assign, change status | elena.marin | Table updates | |
| B12 | W5 | Participants panel: check in at an ONGOING event (seed "Shakespeare Reading Marathon") | radu.pop | Counter updates; button disappears | |
| B13 | W5 | Post an announcement, read it as a participant | elena.marin / maria.dobre | Shown newest first on the event page | |
| B14 | Errors | Remove the token in DevTools (Application → Local storage) and reload | any | Redirect to the login page | |
| B15 | Errors | Open /admin as a student | maria.dobre | "You do not have access to this page." | |
| B16 | Layout | Events, event details and My registrations at 375 px width (DevTools device mode) | any | No horizontal scrolling, buttons reachable | |

