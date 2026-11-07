# Contributing

How our team of 5 works on this project. Read this once before you take your first ticket.
Setup and run instructions are in the [README](README.md); coding rules are in
[`CLAUDE.md`](CLAUDE.md) and the guides under [`docs/dev-guide/`](docs/dev-guide/).

## 1. The project board

All work is tracked as GitHub issues on the project board **UBB Events Board**
(*Projects* tab of the repository). Columns: **Todo → In Progress → In Review → Done**.

Every issue says what it **depends on** (other issue numbers) and has **story points**.

### How to pick a ticket

1. Open a ticket in **Todo** and look at the **Relationships → Blocked by** box in the
   issue sidebar on the right (the `Depends on: T-xx (#nn)` line in the description lists the
   same tickets).
2. Start it **only if every blocking issue is closed** (purple "closed" icon). If one is
   still open, the ticket is not ready yet: pick another one or help finish the blocker.
3. **`good-first-ticket`** marks tickets with 1–2 points and **no dependencies at all**: they
   can be started right away and are the best way to begin. Filter the board or the issue
   list with `label:good-first-ticket`.

## 2. Working on a ticket, step by step

1. **Pick** a ticket in **Todo** whose blockers are all closed (see "How to pick a ticket" above).
   If you are unsure, ask in the team chat before starting.
2. **Assign yourself** to the issue and move it to **In Progress**.
3. **Create a branch** from an up-to-date `main`, named `feature/<issue-number>-short-name`:
   ```bash
   git switch main
   git pull
   git switch -c feature/42-event-search
   ```
   (Use this name for every ticket type, including docs, design and bug fixes.)
4. **Do the work** in small commits (see the commit format below). Run the checks locally
   (section 4).
5. **Open a pull request** into `main`. Fill in the PR template, and put
   **`Closes #42`** in the description. GitHub then closes the issue automatically when the PR
   is merged.
6. Move the ticket to **In Review**.
7. **Review is optional but encouraged.** For **feature tickets** (`type:feature`) and other
   larger changes, ask a teammate to look at the PR before you merge. If you get review
   comments, push more commits to the same branch and reply to each comment.
8. **Merge rule:** you (the author) may merge **as soon as CI is green**: both checks
   *Backend (Maven verify, Java 21)* and *Frontend (lint, test, build, Node 24)*.
   **No approval is required.**
9. Merge with **Squash and merge**, then click **Delete branch**. The issue then closes and
   moves to **Done**.

There is **no single maintainer**: everyone in the team has the same rights. Everyone writes
code, reviews and merges, and every change (also docs) goes through a PR into `main`.

### How to review a PR

Anyone except the author can review, whenever they are asked or want to help.


1. **Read the linked issue** (`Closes #N`) and its **acceptance criteria**. The PR must meet
   all of them, no more and no less.
2. **Check that CI is green**: both the Backend and the Frontend checks. Do not approve a red
   or still-running PR.
3. **Read the diff** (*Files changed*): `CLAUDE.md` conventions, tests for new behaviour,
   no secrets, usage-log row if GenAI was used (see the Definition of Done, section 4).
4. **Run it locally if it changes behaviour** (endpoint, page, migration, Docker/config):
   `gh pr checkout <number>`, then follow the "How to test" steps in the PR description.
5. **Leave comments or approve**: *Review changes* → **Comment** for questions,
   **Request changes** if something must be fixed, **Approve** when everything above is OK.
   Be specific and kind, and say what you checked (e.g. "ran it locally, registration
   returns 409 when full").

### Non-code tickets (diagrams, designs, research, docs)

They are delivered exactly the same way, as a **pull request**:

- Text goes into a Markdown file under `docs/` (e.g. `docs/research/<topic>.md`,
  `docs/design/<topic>.md`).
- Images (PNG/SVG exports, screenshots, mockups) go under `docs/assets/` and are linked from
  the Markdown file. If the diagram tool has a source file (draw.io, PlantUML, …), commit it
  next to the image.

## 3. Commit messages: Conventional Commits

Format: `<type>: <short summary in imperative mood>`

| Type | Use for |
|------|---------|
| `feat:` | new functionality |
| `fix:` | bug fix |
| `docs:` | documentation only |
| `test:` | adding or fixing tests |
| `refactor:` | code change that neither fixes a bug nor adds a feature |
| `chore:` | build, dependencies, config |
| `ci:` | GitHub Actions changes |

Examples:

```
feat: add event search by title and date range
fix: return 409 when registering for a full event
docs: add sequence diagram for registration
```

Keep the summary under ~70 characters. Add a body (after a blank line) if the *why* is not obvious.

## 4. Definition of Done

A ticket is **Done** only when all of these are true:

- [ ] All acceptance criteria of the issue are met.
- [ ] Code follows `CLAUDE.md` (feature packages, DTOs, layering, Flyway migrations, REST conventions).
- [ ] New or changed behavior is covered by tests (service unit tests and/or controller integration tests;
      frontend tests for non-trivial components).
- [ ] Checks pass locally **and** in CI:
      `cd backend && ./mvnw verify` · `cd frontend && npm run lint && npm test && npm run build`.
- [ ] The app still starts with `docker compose up --build`, if you touched Docker, config or migrations.
- [ ] API changes are visible in Swagger; docs/guides are updated if behavior changed.
- [ ] No secrets committed (only placeholders in `.env.example`).
- [ ] If GenAI was used: a row was added to `docs/genai/usage-log.md`.
- [ ] CI (Backend + Frontend) is green, and the author squash-merged the PR into `main` and
      deleted the branch (for feature tickets and larger changes: after a teammate looked at it).

## 5. Story points

| Points | Meaning |
|--------|---------|
| **1** | under 2 hours, trivial |
| **2** | about half a day |
| **3** | about a day |
| **4** | about 2 days, or tricky logic |
| **5** | several days (consider splitting the ticket) |

## 6. Using GenAI tools (ChatGPT, Claude, Copilot, Gemini, …)

This course grades **visible human oversight** of AI-assisted work. Using AI is allowed and
encouraged, but it must be recorded:

- Whenever you use **any** GenAI tool for a ticket (code, tests, docs, diagrams, research …),
  add **one row** to [`docs/genai/usage-log.md`](docs/genai/usage-log.md) **in the same PR**.
- Columns: date, person, tool/model, purpose, prompt summary, what was generated,
  **how you verified it**, and **what you changed by hand**. The last two columns matter most.
- You are responsible for everything you commit: read and understand AI output, run it, and
  test it before opening the PR.
- Optional but useful: save long transcripts under `docs/genai/sessions/` and screenshots
  under `docs/genai/evidence/`.
- Never paste secrets (your `.env`, tokens, passwords) into an AI tool.

## 7. Opening issues

Use **New issue** and pick a template: Feature, Documentation, Design, Research or Bug.
Fill in dependencies and story points so the ticket can go on the board.
