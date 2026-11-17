# Mid-term deliverable

This folder holds the mid-term documentation of the UBB student event management system:
one file per part of the deliverable (A–E), linked by IDs so that requirements, use cases,
workflows, research findings, code and tests can be traced to each other.

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
| **C. UX / user research** | Interviews, findings, changed assumptions, main product flows, personas (6.7) | [`06-ux-research.md`](06-ux-research.md), raw notes in [`interviews/`](interviews/) |
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

## Status

Sections 01–07 are final and were checked against `main` on 2026-11-17 (statuses of
requirements and use cases, workflows W1–W5, user research from interviews I-01 to I-04).
Section 08 is filled by each student for their own part; its instructions stay in the file
until all five statements are written. New interviews go into `interviews/` and the
interview log (6.3), and the findings (6.4) are updated from them.
