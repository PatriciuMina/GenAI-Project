# ADR-002: REST API documented with OpenAPI

- **Status:** Accepted
- **Date:** 2026-11-06

## Context
The React frontend and the Spring Boot backend are developed by different teammates in
parallel. They need a clear, discoverable contract that can be tested manually and used
to generate or check TypeScript types.

## Decision
Expose a JSON REST API under `/api/v1`, with resource-oriented URLs, standard HTTP
methods and status codes, and a consistent error body. Generate the OpenAPI 3 spec from
code with **springdoc-openapi**; Swagger UI is served at `/swagger-ui.html`, spec at
`/v3/api-docs`.

## Alternatives considered
- **GraphQL:** flexible queries, but more learning overhead and tooling for the team.
- **Hand-written OpenAPI (contract-first):** strong contract, but slower iteration and
  risk of drift from the implementation.
- **No formal spec:** fastest initially, but frontend/backend mismatches surface late.

## Consequences
- + Live, always-up-to-date documentation; easy manual testing via Swagger UI.
- + Spec can be used to generate frontend types later.
- − Annotations add some noise to controllers/DTOs.
- − API versioning (`/v1`) must be respected when making breaking changes.
