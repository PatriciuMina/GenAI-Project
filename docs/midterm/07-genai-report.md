# 7. GenAI declaration

Part **D** of the mid-term deliverable. Index: [README](README.md).

Course link: R13 (GenAI-assisted development) and R14 (human oversight), see
`docs/course/course-requirements.md`.

**Build this report from the evidence, not from memory:**
- [`docs/genai/usage-log.md`](../genai/usage-log.md): one row per GenAI task (tool/model,
  purpose, prompt summary, what was generated, how verified, human changes). This is the
  main source; every claim below should point to a row in it.
- [`docs/genai/sessions/`](../genai/sessions/): full session transcripts (`P1-foundations.txt`
  … `P6-docker-ci.txt`, plus later ones). Use them for concrete examples and quotes.
- [`docs/genai/evidence/`](../genai/evidence/): screenshots and other proof (add files here
  when you cite something that is not in a transcript).
- Git history (`git log`, merged PRs) to confirm which files a task touched.

If a teammate used GenAI but did not log it, add the row to `usage-log.md` first, then cite it.

---

## 7.1 Tools and models used

**Ticket:** `docs: GenAI tools and purposes` · **Points:** 1

**What to write**
List every GenAI tool and model the team used (including chat tools used for small tasks)
and, for each, what it was used for and by whom. Use the exact tool/model names from the
usage log. Say briefly why that tool was chosen for that purpose.

**Where to find the information**
- `docs/genai/usage-log.md` → "Tool/Model", "Person", "Purpose" columns
- `CONTRIBUTING.md` §6 "Using GenAI tools" · `CLAUDE.md` (project context file given to the AI)

**Format**
Table: `Tool / model | Used by | Purpose(s) | Why this tool`. One row per tool/model.

> **Example – replace:**
>
> | Tool / model | Used by | Purpose(s) | Why this tool |
> |--------------|---------|------------|---------------|
> | Claude Code (Claude Opus 5.5) | Patriciu Mina | Project foundations, backend skeleton, data model, auth, frontend scaffold, Docker/CI | Works directly in the repository and follows `CLAUDE.md` rules |

**Done when**
- [ ] Every distinct Tool/Model value in `usage-log.md` appears.
- [ ] Every team member who used GenAI is listed.

> TODO (team): tools table here.

---

## 7.2 Artifacts generated or modified with GenAI

**Ticket:** `docs: GenAI artifacts list` · **Points:** 2

**What to write**
Which files or parts of the system were generated or changed with GenAI, and how much of
each was AI output vs. human work. Group by area (docs, backend, database, frontend,
infrastructure). Be honest: "fully generated, reviewed" and "human-written, AI suggested
one function" are both fine, but must be distinguished.

**Where to find the information**
- `docs/genai/usage-log.md` → "What was generated" column
- `git log --stat` / PR descriptions for the exact files

**Format**
Table: `Area | Artifact (file/package) | Generated / modified / assisted | Usage-log row (date + purpose) | Session file`. 8–20 rows.

> **Example – replace:**
>
> | Area | Artifact | Generated / modified / assisted | Usage-log row | Session |
> |------|----------|---------------------------------|---------------|---------|
> | Database | `V1__initial_schema.sql` | Generated after human design decisions D1–D12 | 2026-11-06 Data model | `P3-data-model.txt` |

**Done when**
- [ ] Every usage-log row is represented by at least one artifact.
- [ ] Each artifact links to its usage-log row and, where it exists, a session file.

> TODO (team): artifacts table here.

---

## 7.3 How we verified AI-generated code and documentation

**Ticket:** `docs: GenAI verification process` · **Points:** 2

**What to write**
Describe the general verification process (planning first and answering open decisions,
reviewing diffs, running tests and CI, manual checks in Swagger/UI/psql, PR review by a
teammate), then give 3–5 concrete examples with the actual check and its result. Mention
cases where verification found a problem.

**Where to find the information**
- `docs/genai/usage-log.md` → "How it was verified" column
- `.github/workflows/ci.yml`, `CONTRIBUTING.md` §4 Definition of Done and §6
- `docs/genai/sessions/*` (look for test runs and corrections)

**Format**
Short prose (100–200 words) for the process, then a table:
`Artifact | Verification done | Result | Evidence`.

> **Example – replace:**
>
> | Artifact | Verification done | Result | Evidence |
> |----------|-------------------|--------|----------|
> | Data model (V1 + seed) | `./mvnw verify`; applied seed twice and compared row counts in psql | 16 tests passed; seed idempotent | usage-log 2026-11-06, `P3-data-model.txt` |

**Done when**
- [ ] The process covers automated (tests/CI) and manual checks.
- [ ] At least 3 concrete examples with evidence links.

> TODO (team): verification section here.

---

## 7.4 Human decisions that materially changed the AI output

**Ticket:** `docs: GenAI human decisions` · **Points:** 2

**What to write**
The most important part for R14. Give 3–6 examples where a human decision changed,
corrected or rejected what the AI proposed or would have done: what the AI proposed,
what the human decided, why, and what changed in the result. Only cases with a material
effect (design, behaviour, security, scope), not wording fixes.

**Where to find the information**
- `docs/genai/usage-log.md` → "Human changes" column
- `docs/genai/sessions/*` (open-decision tables and the human's answers, e.g. D1–D12 in P3, auth decisions in P4)
- `docs/data-model.md` → "Design decisions" · `docs/architecture/ADR-003…` → "Implementation notes"

**Format**
Table: `# | AI proposal / default | Human decision | Reason | Effect on the result | Evidence`. 3–6 rows.

All five examples below come from the saved sessions and the "Human changes" column of `docs/genai/usage-log.md`; the quoted proposals are taken from the transcripts.

| # | AI proposal / default | Human decision | Reason | Effect on the result | Evidence |
|---|-----------------------|----------------|--------|----------------------|----------|
| 1 | Deleting an event cascades to its registrations, tasks and announcements. | Events that have registrations are never hard-deleted; they are cancelled with a status instead. | Keep the participation history. | The foreign key from registrations to events is `RESTRICT` (`fk_registrations_event`) and the lifecycle uses `CANCELLED`; a DRAFT without registrations can still be deleted. Recorded as decision D4 in `docs/data-model.md`. | Usage log 2026-11-06, data model; `P3-data-model.txt` (D4) |
| 2 | Plan with 4 `no-genai` tickets and one manual-test ticket per workflow, about 95 tickets and 190 points, with the full workflow scope for the mid-term. | Exactly 5 `no-genai` tickets, one per team member, each a feature with real business logic its author can explain; the scope was cut: changing roles, leaving and editing an organization, editing and deleting tasks and announcements, per-workflow QA and three of the sequence diagrams moved to "Deferred to final". About 99 small tickets were kept after the count was shown. | Give every member one piece of work to write and explain alone, and keep the mid-term scope small. | `docs/planning/midterm-tickets.md` has the `no-genai` tickets T-06, T-17, T-22, T-25 and T-29 and a "Deferred to final" list; this is why those tickets must be written without GenAI. | Usage log 2026-11-07, mid-term ticket planning; `P8-ticket-plan.txt` (D12) |
| 3 | Label `good-first-ticket` on the 1 to 2 point tickets. | The person found that about 75% of them were blocked by other tickets, and set a new rule: 1 to 2 points **and** no dependencies. | A ticket that waits for another is not one a newcomer can start right away. | The label was removed from 57 tickets and kept on 29 in the plan, in the creation script and on the existing issues. | Usage log 2026-11-07, fix the good-first-ticket rule; `P9-board-issues.txt` ("Problem found by the human") |
| 4 | A long combined shell command that copied and read `.env` and used `rm`, to write the Docker files. | The person denied it and instructed: write each file with the normal file edit tool, never read, print, copy or back up `.env`, and do not delete anything the task did not create. | `.env` holds the real secrets, and a command that reads it or deletes files is not something to run unreviewed. | Claude wrote the files with the file tools and said it would not touch `.env`; the secrets stay in the git-ignored `.env`. | Usage log 2026-11-07, Dockerized full stack, CI, README, CONTRIBUTING, templates; `P6-docker-ci.txt` |
| 5 | Thirteen pre-filled assumptions to validate in the interviews, including one about a 2-hour login without refresh. | Removed the 2-hour login assumption and added three about users: the interface is English only, only organizers and presidents create events, and students find events mainly through social media and word of mouth. | The login lifetime is too technical to ask users, while the added assumptions are about real user behaviour. | The assumptions list for the interviews in `06-ux-research.md` changed; interview results must come from real interviews. | Usage log 2026-11-07, mid-term documentation drafts; `P7-midterm-docs.txt` |

**Done when**
- [ ] Each example states the AI proposal and the human decision separately.
- [ ] Each example has evidence (usage-log row + session file or PR).
- [ ] Examples come from more than one team member, if possible.

**Open items for the team**
- All five examples are from Patriciu Mina's sessions, the only ones with saved transcripts. The log rows of Andrei Gota, Portik Márk-Krisztián and Vlad-Ștefan Uleia record a review and no change to the generated code, so they give no example of a decision that changed the AI output. If they made such decisions (rejected a proposal, changed the design, caught a bug), they should add them to the log, and then to this table, to meet the last "Done when" box.
- Example 2 describes the plan as the transcript shows it; confirm the wording with the person who made the decisions.
