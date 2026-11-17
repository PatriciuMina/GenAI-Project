# Mid-term: finish plan

Status checked on GitHub and `main` (commit `0a49db7`) on **2026-11-17**.
Follow the steps in order. Each step lists its tickets, a suggested owner and when it is done.
Owners are suggestions: the team decides.

---

## 1. Where we are

**Tickets:** 99 planned → **74 closed, 25 open**. CI on `main` is green. The repo is public.

| Deliverable part | State | What is missing |
|------------------|-------|-----------------|
| A. Code | Almost done. W1–W5 backend and most screens work | "My registrations" page is still a placeholder (T-25); a few tests (T-36), UI polish (T-44), manual QA of all workflows (T-99) |
| B. Docs 1–4 (problem, requirements, actors, use cases) | Written | Status columns are out of date (checked on 2026-11-15; W3 pages, cancel and check-in now exist). `03-actors-roles.md` still has an "Example – replace" block |
| B. Docs 5 (workflows) | W1 and W2 done | W3, W4, W5 (T-85, T-86, T-87); state machine check against code (T-49) |
| C. UX research | **Not started** (only the interview guide is done) | Interviews, method, findings, assumptions, personas, product flows (T-52 … T-61) |
| D. GenAI report | Written on 2026-11-15 | Does not cover the work from 11-16 and 11-17 (Copilot/ChatGPT use for docs and design, later Codex tickets, T-22/T-24). One "reason not recorded" placeholder. `docs/genai/evidence/` is empty |
| E. Individual contributions | **Not started** | All 5 sections are TODO (T-94 … T-98) |

### Who did what (from merged PRs and closed issues)

| Person | GitHub | Main work |
|--------|--------|-----------|
| Mina Patriciu | PatriciuMina | Project foundation: backend skeleton, data model (Flyway V1, entities, seed, ER diagram), JWT auth and org permissions, frontend skeleton, Docker full stack, CI, README, CONTRIBUTING, issue/PR templates, mid-term doc drafts and interview guide, course requirements summary, the 99-ticket plan, board/issue scripts and team rules (PRs #1–#8, #108, #109). Open: T-25 (#33) |
| Andrei Gota | GotaSeptimiuAndrei | W1 membership API (T-01, T-03, T-04, T-05), admin creates organization API and page (T-08, T-09), create draft event (T-10), cancel registration API and Register/Cancel button (T-22, T-24). Assigned: interviews T-52 (#60) |
| Portik Márk-Krisztián | MARKUCI14 | Most W2/W3 backend and frontend: Organizing menu (T-02), members UI (T-06, T-07), edit event, status transitions, event details, search, org events (T-11, T-12, T-16, T-17, T-18), event form, events list, organizing pages (T-13, T-14, T-15, T-19), W5 panels: tasks, participants/check-in, announcements (T-31, T-32, T-33), tests (T-37, T-38), wireframes (T-41, T-42, T-43) |
| Jakab Gergő | JakabGergo | Logo and colour palette (T-39, T-40), friendly error messages (T-45), permission matrix (T-75), use case list, diagram and all use cases (T-46, T-76 … T-82), workflows W1 and W2 (T-83, T-84) |
| Vlad-Ștefan Uleia | uleiavlad27 | W4/W5 backend: register with capacity, my registrations, tasks, participants, check-in, announcements (T-21, T-23, T-26 … T-30), concurrency test (T-35), event details page and announcements on it (T-20, T-34); docs: problem statement, stakeholders, scope, out of scope, R1–R16 map, all FR and NFR, actors, sequence diagrams, ER review, interview guide review, whole GenAI report (T-47, T-48, T-50, T-51, T-62 … T-74, T-90 … T-93) |

---

## 2. Steps

### Step 1 – Team decision: interviews (today, 10 min)

The course asks for "interviews with real users". The team plans to simulate some.

- **Rule:** every simulated interview is **labelled as simulated** in its note (`I-NN.md`,
  field "Mode: simulated") and in 6.1 Method. Never present a simulated interview as real.
  Presenting invented interviews as real ones would be a false statement in a graded report.
- **Recommended:** each person does at least **one short real interview** (10–15 min, a
  colleague or a friend in a student organization, with the consent note from 6.1). Mixed
  real + simulated is fine if it is labelled.
- Decide who does which (T-52 … T-56: 2 interviews per person, aim ≥ 3 students and ≥ 2 organizers in total).
- Check first if a colleague already wrote interview notes on a branch (only `interviews/README.md` is on `main`).

**Done when:** the team agreed in the group chat; each person knows which 2 interviews are theirs.

### Step 2 – Code: "My registrations" page (T-25, #33) – Mina

- Ticket is labelled **`no-genai`**: write it yourself; AI may only be used to explain, not to write the code.
- Dependencies are done (T-22 cancel API, T-23 my registrations API).
- Acceptance: `andrei.toma` sees the workshop under *Upcoming* and the cancelled "Board Games Evening" under *Past and cancelled*; Cancel with confirm; empty state; test for the split.

**Done when:** PR merged, CI green, checked in the browser as `andrei.toma`.

### Step 3 – Code: remaining tests and polish (parallel with Step 2)

| Ticket | What | Suggested owner |
|--------|------|-----------------|
| T-36 (#44) | Tests: registration window, duplicates, cancel/re-register | Vlad or Andrei |
| T-44 (#52) | UI polish: empty states and loading | Portik |
| T-49 (#57) | Check the event state machine in 5.2 against `EventStatus` / transition code | Jakab |

**Done when:** all three merged.

### Step 4 – Docs: workflows W3, W4, W5 (T-85, T-86, T-87)

Same format as W1/W2 in `05-workflows.md` (steps, actors, diagram). W4 already has the sequence diagram.
Suggested owner: Jakab (wrote W1/W2), or split with Andrei (W4) and Portik (W3/W5, he built the screens).

**Done when:** no `TODO (team)` left in `05-workflows.md`, Mermaid renders on GitHub.

### Step 5 – Interviews (T-52 … T-56) – everyone

- One file per interview: `docs/midterm/interviews/I-01.md` … `I-10.md` (template in `interviews/README.md`).
- Short is fine: 5–8 questions from the guide (6.2), answers in 1–3 lines each, 1–2 quotes, link to assumptions A-01 … A-13.
- No personal data. Mark mode `real` or `simulated`.
- Fill the interview log in 6.3.

**Done when:** ≥ 5 notes merged (target 10), log 6.3 filled.

### Step 6 – Research synthesis (T-57 … T-61) – after Step 5

| Ticket | Section | Suggested owner |
|--------|---------|-----------------|
| T-57 (#65) | 6.1 Method + consent (state how many real vs simulated) | Andrei |
| T-58 (#66) | 6.4 Findings table F-01 … (each cites I-NN) | Vlad |
| T-59 (#67) | 6.5 Assumptions A-01 … A-13: keep / change / open | Mina |
| T-60 (#68) | Personas (2–3, from the interviews) | Portik |
| T-61 (#69) | 6.6 Main product flows, ordered by findings | Jakab |

At least one assumption should end as `change` with a real follow-up (new FR, new ticket for the
final delivery, or doc change). This is the evidence the course asks for in part C.

**Done when:** no `TODO (team)` left in `06-ux-research.md`.

### Step 7 – Docs: bring status up to date (new ticket, 1–2 pts)

Code changed after the docs were written (2026-11-15). Check against `main`:

- `02-requirements.md`: Status column of FR-W3, FR-W4, FR-W5 (events page, details page, cancel, check-in, tasks panel, organizer screens now exist); remove the "placeholder" notes.
- `04-use-cases.md` 4.2: UC-22, UC-23, UC-24, UC-30, UC-31, UC-42, UC-52 are now implemented; check UC-40 after Step 2.
- `03-actors-roles.md`: remove the leftover "Example – replace" block.
- Add consequences of the interviews (Step 6) as new FRs where an assumption changed.

**Done when:** every status matches `main`; no example blocks left.

### Step 8 – GenAI report up to date (new ticket, 2 pts) – Vlad (wrote it) + everyone

- 7.1: add Jakab's GitHub Copilot and ChatGPT use, Portik fills "why this tool" for Codex.
- 7.2–7.4: add artifacts and verifications from 2026-11-16/17 (usage log rows 65–93), and from Steps 2–7.
- Add 2–3 more human-decision examples (e.g. interview results that changed an assumption; T-25 written without AI).
- `docs/genai/evidence/`: add a few screenshots/links (PR review comments, failing then passing tests, a corrected AI output). The course stresses **visible human oversight**.
- Everyone: check your own rows in `docs/genai/usage-log.md` are complete ("human changes" column).

**Done when:** every usage-log row is reflected in 7.2; every tool in 7.1 has a reason.

### Step 9 – Manual QA of all workflows (T-99, #107) – Mina

`docker compose up --build`, then go through W1–W5 with the seed users (admin, president, organizer, student).
Write the result as a short checklist (pass/fail per use case) in the PR; open bug tickets for failures.

**Done when:** all W1–W5 main flows pass or have a bug ticket.

### Step 10 – Individual contribution statements (T-94 … T-98) – each person

In `08-individual-contributions.md`, own section only: owned artifacts, workflow, features, docs,
tests, design decisions, GenAI work. Use the "Who did what" table above and your PRs as the source.

**Done when:** 5 sections filled, each person approved their own text.

### Step 11 – Final review (T-88, #96) – two people who did not write the section

Read 01–08 end to end: consistent IDs (FR/UC/F/A), links work, no TODO, no "Example – replace",
statuses match `main`, no personal data in interviews.

### Step 12 – Assemble the mid-term document (T-89, #97) – last step

Merge 01–08 into one document (PDF), with the repo link on the cover. Submit A–E.

---

## 3. Order and dependencies

```
Step 1 ──► Step 5 ──► Step 6 ──┐
Step 2, 3 (code) ──► Step 9 ───┤
Step 4 (workflows) ────────────┼──► Step 7 ──► Step 8 ──► Step 10 ──► Step 11 ──► Step 12
```

Steps 2, 3, 4 and 5 can run in parallel.
