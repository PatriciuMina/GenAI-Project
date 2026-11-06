# ADR-004: Database schema managed with Flyway migrations

- **Status:** Accepted
- **Date:** 2026-11-06

## Context
Several teammates will change the PostgreSQL schema concurrently. Every developer's local
database, and any demo environment, must be reproducible from the repository.

## Decision
Manage the schema exclusively with **Flyway** SQL migrations in
`backend/src/main/resources/db/migration`, named `V{n}__short_description.sql`.
Hibernate runs with `ddl-auto=validate` so entity/schema mismatches fail fast. Applied
migrations are never edited; changes always go in a new migration.

## Alternatives considered
- **Hibernate `ddl-auto=update`:** zero effort, but non-deterministic, cannot express all
  changes, and unsafe for real data.
- **Liquibase:** equally capable, but XML/YAML changelogs are more verbose; plain SQL is
  easier to review.
- **Manual SQL scripts:** no version tracking, error-prone.

## Consequences
- + Schema is versioned, reviewable in PRs and reproducible everywhere.
- + Plain SQL is easy for humans to verify (important for GenAI oversight).
- − Two teammates creating the same version number causes a conflict; coordinate
  numbers in PRs (rebase and renumber before merge).
- − Seed/demo data must also be handled deliberately (e.g. separate migrations/profile).
