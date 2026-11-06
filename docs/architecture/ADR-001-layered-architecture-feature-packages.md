# ADR-001: Layered architecture with feature packages

- **Status:** Accepted
- **Date:** 2026-11-06

## Context
Five students work in parallel on five workflows (W1–W5). The backend must be easy to
navigate, reduce merge conflicts, and be understandable for course reviewers. GenAI tools
also produce more consistent code when the structure is predictable.

## Decision
Use a classic layered architecture (controller → service → repository) **inside feature
packages**: `auth`, `user`, `organization`, `event`, `registration`, `organizing`,
`common`, `config`. Each feature has `controller`, `service`, `repository`, `dto`,
`entity` sub-packages. Controllers expose DTOs only; cross-feature calls go through
services.

## Alternatives considered
- **Package-by-layer** (`controllers/`, `services/`, ...): simple, but features are spread
  across the tree and teammates collide in the same folders.
- **Hexagonal / clean architecture:** strong isolation, but too much ceremony for a
  semester-long student project.
- **Microservices:** unnecessary operational complexity for this scope.

## Consequences
- + Each teammate mostly owns one feature package → fewer merge conflicts.
- + Clear, repeatable structure for humans and GenAI prompts.
- − Some cross-feature dependencies (e.g. registration → event) must be managed carefully
  to avoid cycles.
- − Shared concerns must be kept deliberately small in `common`.
