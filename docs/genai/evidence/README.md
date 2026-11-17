# Evidence of human oversight

Index of places in the repository where the human review of GenAI output is visible.
Used by section 7 of the mid-term report (`docs/midterm/07-genai-report.md`, R14).
Each line points to a pull request, a commit or a log row that anyone can open.

## Session transcripts

Full sessions with the open-decision tables and the person's answers:
[`../sessions/`](../sessions/) (`P1-foundations.txt` … `P10-team-rules.txt`).

| What the human did | Where to see it |
|--------------------|-----------------|
| Answered design decisions D1–D12 before the data model was generated (e.g. D4: no hard delete of events with registrations) | `P3-data-model.txt`; `docs/data-model.md` "Design decisions" |
| Cut the mid-term scope and set one `no-genai` ticket per member | `P8-ticket-plan.txt` (D12) |
| Found that most `good-first-ticket` tickets were blocked and changed the rule | `P9-board-issues.txt` ("Problem found by the human") |
| Denied a shell command that read `.env` and used `rm` | `P6-docker-ci.txt` |

## Pull requests and commits

| What happened | Evidence |
|---------------|----------|
| After a manual browser check, the error layout of the organizer status actions was changed (T-15) | PR [#162](https://github.com/PatriciuMina/GenAI-Project/pull/162), commit "feat: fixed the error button" |
| The person reported a GitHub Mermaid parse error in workflow W1 and the labels were fixed | PR [#180](https://github.com/PatriciuMina/GenAI-Project/pull/180); usage-log row "W1 Organizations and membership workflow" |
| Concurrency test checked by removing the row lock on purpose (test then fails with overbooking) | PR [#150](https://github.com/PatriciuMina/GenAI-Project/pull/150); usage-log row T-35 |
| Problem statement rewritten after the person supplied the facts and named their source | PR [#152](https://github.com/PatriciuMina/GenAI-Project/pull/152); usage-log row T-62 |
| ER diagram review script fixed after it reported 22 false differences | PR [#139](https://github.com/PatriciuMina/GenAI-Project/pull/139); usage-log row T-50 |
| Registration rules test follows the code (DRAFT → 404), not the ticket text; difference explained | PR [#186](https://github.com/PatriciuMina/GenAI-Project/pull/186) |
| First draft of workflow W3 corrected (error code, R codes) before the PR | PR [#187](https://github.com/PatriciuMina/GenAI-Project/pull/187); usage-log row T-85 |
| Workflow W5 merged with the interview criterion left open instead of invented | PR [#189](https://github.com/PatriciuMina/GenAI-Project/pull/189); issue [#95](https://github.com/PatriciuMina/GenAI-Project/issues/95) stays open |

## Adding evidence

Put screenshots here (`YYYY-MM-DD-short-name.png`) and add a line to the table above.
No personal data and no secrets in screenshots.
