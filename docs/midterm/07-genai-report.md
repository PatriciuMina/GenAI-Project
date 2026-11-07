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

> **Example – replace:**
>
> | # | AI proposal / default | Human decision | Reason | Effect | Evidence |
> |---|-----------------------|----------------|--------|--------|----------|
> | 1 | <what the AI proposed> | Events with registrations are never hard-deleted; cancel instead | Keep participation history | `fk_registrations_event` set to RESTRICT | usage-log 2026-11-06 Data model, `P3-data-model.txt` |

**Done when**
- [ ] Each example states the AI proposal and the human decision separately.
- [ ] Each example has evidence (usage-log row + session file or PR).
- [ ] Examples come from more than one team member, if possible.

> TODO (team): human decisions table here.
