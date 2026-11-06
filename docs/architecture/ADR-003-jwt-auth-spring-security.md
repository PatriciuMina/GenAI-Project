# ADR-003: JWT authentication with Spring Security

- **Status:** Accepted
- **Date:** 2026-11-06

## Context
Users (students, organizers, organization admins) need to log in, and endpoints need
role- and membership-based authorization (e.g. only organizers of an organization can
edit its events). The frontend is a separate SPA served from a different origin in dev.

## Decision
Use **Spring Security** with stateless **JWT** bearer tokens. The `auth` feature issues a
signed access token (HS256, secret from the `JWT_SECRET` environment variable) on login;
a filter validates it on every request. Passwords are hashed with BCrypt. Coarse rules go
in `SecurityConfig`; fine-grained checks (membership/role in an organization) in services
or via `@PreAuthorize`.

## Alternatives considered
- **Server-side sessions (cookie + JSESSIONID):** simple, but stateful and needs CSRF and
  CORS-cookie handling for the SPA.
- **OAuth2 / external IdP (Keycloak, university SSO):** realistic for production, but
  heavy setup and outside the course's allowed external services.
- **Basic auth:** unsuitable for a real UI.

## Consequences
- + Stateless backend; simple to call from the SPA and from tests.
- + Industry-standard, well-supported by Spring Security.
- − Tokens cannot be revoked before expiry without extra work (keep expiry short).
- − Secret management matters: never commit `JWT_SECRET`, only `.env.example` placeholder.
- − Token storage on the frontend must be handled carefully (XSS risk).

## Implementation notes (2026-11-06)
Decided by the team before implementation:
- **Library:** Spring Security's built-in OAuth2 Resource Server (Nimbus) with an HMAC
  (HS256) secret; no separate JWT library and no hand-written token filter.
- **Lifetime:** access token valid 2 hours (`JWT_EXPIRATION_MINUTES=120`). **No refresh
  tokens** for the mid-term; the client logs in again.
- **Claims:** `sub` = user id, `email`, `role` (STUDENT/ADMIN → `ROLE_*` authority). Role
  changes take effect at the next login.
- **Registration:** anyone, any email, always `STUDENT`; `ADMIN` accounts only from the seed.
  Password 8–72 characters (72 = BCrypt limit).
- **Login errors:** the same "Invalid credentials" (401) for unknown email and wrong password.
- **Organization roles:** `OrgPermissionService` (bean `orgPermissions`) for `@PreAuthorize`;
  PRESIDENT ⊃ ORGANIZER ⊃ MEMBER; no implicit bypass for global ADMIN.
- **Startup:** the app refuses to start if `JWT_SECRET` is missing or shorter than 32 characters.

How to use it in a feature: `docs/dev-guide/how-to-add-a-feature.md`.
