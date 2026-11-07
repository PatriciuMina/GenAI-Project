# Mid-term deliverable

This folder holds the mid-term documentation. The files are **drafts with instructions**:
each section says what to write, where to find the information, which format to use, and
when the section is done. The team writes the content.

Course source: [`docs/course/course-requirements.md`](../course/course-requirements.md)
(R1–R16 and the A–E mid-term parts).

## Index

| Part | Content | Where |
|------|---------|-------|
| **A. Code** | Project structure, dev environment, initial backend + frontend, database + initial data model, main MVP workflows | The repository: [`backend/`](../../backend), [`frontend/`](../../frontend), [`docker-compose.yml`](../../docker-compose.yml), [`README.md`](../../README.md), [`docs/data-model.md`](../data-model.md) |
| **B. Documentation** | 1. Problem statement and scope | [`01-problem-statement.md`](01-problem-statement.md) |
| | 2. High-level requirements | [`02-requirements.md`](02-requirements.md) |
| | 3. Actors and roles | [`03-actors-roles.md`](03-actors-roles.md) |
| | 4. Use cases | [`04-use-cases.md`](04-use-cases.md) |
| | 5. Main application workflows | [`05-workflows.md`](05-workflows.md) |
| **C. UX / user research** | Interviews, findings, changed assumptions, main product flows | [`06-ux-research.md`](06-ux-research.md), raw notes in [`interviews/`](interviews/) |
| **D. GenAI declaration** | Tools/models, purpose, artifacts, verification, human decisions | [`07-genai-report.md`](07-genai-report.md) |
| **E. Individual contributions** | One declaration per student | [`08-individual-contributions.md`](08-individual-contributions.md) |

### ID schemes used across the files

| Kind | Scheme | Example |
|------|--------|---------|
| Course requirement | `R1` … `R16` | R4 Event Lifecycle |
| Functional requirement | `FR-<group>-NN`, group = `AUTH`, `ADM`, `W1` … `W5` | `FR-W4-02` |
| Non-functional requirement | `NFR-NN` | `NFR-03` |
| Use case | `UC-NN`, numbered in blocks: AUTH/ADM 01–09, W1 10–19, W2 20–29, W3 30–39, W4 40–49, W5 50–59 | `UC-41` |
| Interview | `I-NN` | `I-03` |
| Finding | `F-NN` | `F-02` |
| Assumption | `A-NN` | `A-01` |

Always link by ID (e.g. a use case lists `FR-W4-01`, a requirement lists `R3`). This
gives the traceability required by R15.

## How to work on a section

Every section with a **Done when** checklist is meant to be one ticket.

1. **Pick the ticket** on the project board (or open one with the *Documentation*
   template, `.github/ISSUE_TEMPLATE/docs.yml`). Use the suggested ticket title and points
   from the section. Move it to *In progress* and assign yourself.
2. **Create a branch**: `feature/<issue-number>-short-name` (see
   [`CONTRIBUTING.md`](../../CONTRIBUTING.md) §2).
3. **Write in the file.** Replace the `> TODO (team):` marker and the
   `> **Example – replace:**` block with real content. Keep the heading. Delete the
   "What to write / Where to find / Format" instructions only when the whole file is final
   (the team decides that before submission).
4. **Use GenAI if you want**, but record it in [`docs/genai/usage-log.md`](../genai/usage-log.md)
   (CONTRIBUTING §6) and check every fact against the repo.
5. **Open a PR** (`docs: ...` commit messages) and link the issue.
6. **The reviewer** ticks the section's *Done when* boxes in the PR review. If one box is
   not met, request changes. Merge only when all boxes are ticked.

### Rules for all sections
- Do not invent facts. If something is not decided yet, write it as *planned* or as an
  open question.
- Interview results come only from real interviews (see `06-ux-research.md`).
- Check the "Status" of requirements against the code on `main`, not against plans.
- Mermaid diagrams must render in the GitHub preview before you open the PR.
