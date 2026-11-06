# Contributing

How our team of 5 works on this project. Read this once before you take your first ticket.
Setup and run instructions are in the [README](README.md); coding rules are in
[`CLAUDE.md`](CLAUDE.md) and the guides under [`docs/dev-guide/`](docs/dev-guide/).

## 1. The project board

All work is tracked as GitHub issues on the project board **GenAI Pro**
(*Projects* tab of the repository). Columns: **Todo → In Progress → In Review → Done**.

Every issue says what it **depends on** (other issue numbers) and has **story points**.

## 2. Working on a ticket, step by step

1. **Pick** a ticket in **Todo** whose dependencies are all **Done**.
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
6. Move the ticket to **In Review** and ask a teammate to review.
7. **Address review comments**: push more commits to the same branch and reply to each
   comment. CI must be green.
8. The **maintainer** approves and merges (squash merge). The issue then moves to **Done**.
   Delete your branch after the merge.

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
- [ ] The PR was reviewed and approved by a teammate, then merged into `main` by the maintainer.

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
