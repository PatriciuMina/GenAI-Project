# ADR-005: Docker Compose for local infrastructure

- **Status:** Accepted
- **Date:** 2026-11-06

## Context
Five developers on different OSes need the same PostgreSQL version and configuration
without manual installation. Credentials must not be committed.

## Decision
Provide a root `docker-compose.yml` with a **PostgreSQL 16** service (healthcheck, named
volume `pgdata`). Credentials and port come from a local `.env` file (git-ignored); a
committed `.env.example` documents the variables. Backend and frontend run natively for
fast dev loops; they may be added to Compose later.

## Alternatives considered
- **Locally installed PostgreSQL:** version drift and setup friction across machines.
- **H2 in-memory database:** easy, but SQL dialect differs from production PostgreSQL.
- **Testcontainers only:** great for tests (may still be used there), but not for
  day-to-day manual development.
- **Kubernetes / cloud DB:** overkill and outside allowed external services.

## Consequences
- + One command (`docker compose up -d`) gives everyone an identical database.
- + Data persists across restarts in the named volume; `docker compose down -v` resets it.
- − Requires Docker Desktop (or equivalent) on every machine.
- − Port 5432 may clash with a local PostgreSQL; change `POSTGRES_PORT` in `.env`.
