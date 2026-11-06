## What does this PR do?

<!-- One or two sentences. Link the ticket: "Closes #N" moves it to Done when merged. -->

Closes #

## How to test it

<!-- Steps a reviewer can follow: commands, URLs, accounts, expected result. -->

1.

## Screenshots (UI or diagram changes)

<!-- Optional. Drag images here. -->

## Checklist

- [ ] Branch is named `feature/<issue-number>-short-name`
- [ ] Commits follow Conventional Commits (`feat:`, `fix:`, `docs:`, ...)
- [ ] Tests pass locally (`cd backend && ./mvnw verify` and/or `cd frontend && npm run lint && npm test && npm run build`)
- [ ] New/changed behavior has tests (or N/A for docs/design tickets)
- [ ] Schema changes are a **new** Flyway migration (never edited an applied one), or N/A
- [ ] No secrets committed (only `.env.example` with placeholders)
- [ ] Docs updated if needed (README, `docs/`, Swagger annotations)
- [ ] GenAI used? If yes, row added to `docs/genai/usage-log.md`
- [ ] Ticket moved to **In Review** on the project board
