# GenAI Usage Log

Every GenAI-assisted task is recorded here (newest at the bottom). The human who ran the
task fills in **How it was verified** and **Human changes** after reviewing the output.

| Date | Person | Tool/Model | Purpose | Prompt summary | What was generated | How it was verified | Human changes |
|------|--------|------------|---------|----------------|--------------------|---------------------|---------------|
| 2026-11-06 | Patriciu Mina | Claude Code (Claude Opus 5.5) | Project setup / foundations (no app code) | Add safety rules + project context to CLAUDE.md; create .gitignore, ADR-001..005, usage log, docker-compose (PostgreSQL 16) and .env.example | `CLAUDE.md`, `.gitignore` (merged), `docs/architecture/ADR-001..ADR-005`, `docs/genai/usage-log.md`, `docs/genai/{evidence,sessions}/.gitkeep`, `docker-compose.yml`, `.env.example` | Reviewed plan before approval; read CLAUDE.md and ADRs; `docker compose config -q` valid; Postgres 16 container healthy and answered `select version()`; confirmed `.env` is git-ignored | Approved plan; after review accepted base package `ro.ubb.events` and `/api/v1` API prefix; set local DB password in `.env` |
