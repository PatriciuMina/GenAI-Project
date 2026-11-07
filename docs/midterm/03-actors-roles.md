# 3. Actors and roles

Part **B.3** of the mid-term deliverable. Index: [README](README.md).

**Source of truth.** Permissions are decided in code, not in this document. When the doc
and the code disagree, the code wins and the doc must be fixed (or an issue opened).
- `backend/src/main/java/ro/ubb/events/config/SecurityConfig.java`: public vs.
  authenticated endpoints; global roles become `ROLE_STUDENT` / `ROLE_ADMIN`.
- `backend/src/main/java/ro/ubb/events/organization/service/OrgPermissionService.java`:
  organization-level checks `isMember`, `isOrganizer`, `isPresident`
  (hierarchy PRESIDENT ⊃ ORGANIZER ⊃ MEMBER; a global ADMIN gets **no** automatic org rights).
- `@PreAuthorize(...)` annotations on controllers/services (search the code for `@PreAuthorize`).

---

## 3.1 Actors

**Ticket:** `docs: describe actors` · **Points:** 1

**What to write**
Describe each kind of user of the system and the external systems it talks to (if any).
Explain the two levels of roles: the **global** role on the account (STUDENT, ADMIN) and the
**organization** role inside one organization (MEMBER, ORGANIZER, PRESIDENT). Note that a
student can have different roles in different organizations, and that a visitor (not
logged in) is also an actor.

**Where to find the information**
- `backend/.../user/entity/GlobalRole.java`, `organization/entity/OrganizationRole.java`
- `docs/data-model.md` → `users`, `organization_memberships`; seed accounts table (real examples of each role)
- `docs/architecture/ADR-003-jwt-auth-spring-security.md` → "Implementation notes" (who can register, ADMIN only from seed)
- `01-problem-statement.md` → stakeholders

**Format**
Table: `Actor | Kind (global / org role / external) | Description | Example seed account`. 6–8 rows.
Optionally a small Mermaid diagram showing the role hierarchy.

> **Example – replace:**
>
> | Actor | Kind | Description | Example seed account |
> |-------|------|-------------|----------------------|
> | Organizer | Org role | Member of an organization who prepares its events | elena.marin@stud.ubbcluj.ro |

**Done when**
- [ ] Both role levels are explained with one sentence each.
- [ ] Every value of `GlobalRole` and `OrganizationRole` appears, plus Visitor.
- [ ] Consistent with stakeholders in `01-problem-statement.md`.

> TODO (team): actors table here.

---

## 3.2 Permission matrix

**Ticket:** `docs: permission matrix` · **Points:** 2

**What to write**
One row per action (grouped by workflow W1–W5 plus accounts/admin), one column per actor.
Mark what each actor may do. "Organizer" etc. always means *of the organization that owns
the event*. Add the **Enforced by** column: the exact check in code (or `planned` if the
endpoint does not exist yet). The actions should match the use cases in `04-use-cases.md`.

**Where to find the information**
- `SecurityConfig.java`, `OrgPermissionService.java`, `@PreAuthorize` annotations (see top of file)
- `backend/src/test/java/ro/ubb/events/organization/OrgPermissionServiceTest.java`,
  `OrgPermissionPreAuthorizeIntegrationTest.java` (show the expected behaviour)
- `docs/dev-guide/how-to-add-a-feature.md` §5 "Security – who may call the endpoint"
- `docs/data-model.md` → "Rules enforced in the service layer" (rule 6)
- `02-requirements.md` (FR IDs)

**Format**
Table. Legend: ✅ allowed · ❌ not allowed · 🔸 only own (e.g. own registration). 15–30 rows.
Columns: `Action | FR | Visitor | Student | Member | Organizer | President | Admin | Enforced by`.

> **Example – replace:**
>
> | Action | FR | Visitor | Student | Member | Organizer | President | Admin | Enforced by |
> |--------|----|---------|---------|--------|-----------|-----------|-------|-------------|
> | List organizations | FR-W1-01 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | `SecurityConfig` (authenticated) |

**Done when**
- [ ] Every row has an FR ID and an "Enforced by" value (code reference or `planned`).
- [ ] Rows marked implemented were checked against the code/tests.
- [ ] ADMIN rights inside organizations are stated explicitly (not assumed).
- [ ] Reviewed by the teammate who owns `SecurityConfig` / `OrgPermissionService`.

> TODO (team): permission matrix here.
