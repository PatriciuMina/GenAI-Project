# How to add a feature (backend)

This guide walks through the **organizations** example (`GET /api/v1/organizations` and
`GET /api/v1/organizations/{id}`) file by file. Copy the same structure for your own
endpoint. You do not need any AI tool: everything here is plain Spring Boot.

Read first: `CLAUDE.md` (rules), `docs/data-model.md` (tables), ADR-001 (feature packages),
ADR-003 (JWT).

---

## 0. The big picture

```
HTTP request
  │  Authorization: Bearer <token>
  ▼
SecurityConfig ── no/invalid token? ──► 401 JSON
  │
  ▼
Controller  (DTOs in/out, @Valid, @PreAuthorize, OpenAPI annotations)
  │
  ▼
Service     (@Transactional, business rules, throws ApiException subclasses)
  │
  ▼
Repository  (Spring Data JPA interface)  ──►  PostgreSQL (schema via Flyway)
```

Rules (from CLAUDE.md):
- Controller → Service → Repository. **Controllers never call repositories.**
- **Entities never leave the service layer.** Controllers receive and return DTOs (records).
- Another feature's data? Call **its service**, not its repository.
- Errors: throw an exception from `common.exception`; `GlobalExceptionHandler` turns it into the
  standard JSON error body. Never build error responses by hand.

All code for one feature lives in one package, e.g. `ro.ubb.events.organization`:

```
backend/src/main/java/ro/ubb/events/organization/
├── entity/        Organization.java                 (already existed – JPA entity)
├── repository/    OrganizationRepository.java       (already existed – Spring Data)
├── dto/           OrganizationResponse.java         ← step 1
├── mapper/        OrganizationMapper.java           ← step 2
├── service/       OrganizationService.java          ← step 3
│                  OrgPermissionService.java         (role checks, see step 5)
└── controller/    OrganizationController.java       ← step 4

backend/src/test/java/ro/ubb/events/organization/
├── OrganizationServiceTest.java                     ← step 6 (unit, Mockito)
└── OrganizationControllerIntegrationTest.java       ← step 7 (MockMvc + Testcontainers)
```

---

## 1. DTO – `dto/OrganizationResponse.java`

What the API returns. A Java `record` (immutable, no boilerplate):

```java
public record OrganizationResponse(Long id, String name, String description,
                                   String contactEmail, Instant createdAt) { }
```

- Only put fields the client should see (never password hashes, never whole entities).
- For **input** DTOs (POST/PUT bodies) add Bean Validation annotations, e.g. from
  `auth/dto/RegisterRequest.java`:
  ```java
  public record RegisterRequest(
          @NotBlank @Email @Size(max = 255) String email,
          @NotBlank @Size(min = 8, max = 72) String password,
          @NotBlank @Size(max = 150) String fullName) { }
  ```
  and use `@Valid @RequestBody RegisterRequest request` in the controller. Invalid input then
  becomes a **400 `VALIDATION_FAILED`** with `fieldErrors` automatically.

## 2. Mapper – `mapper/OrganizationMapper.java`

Converts entity → DTO. Hand-written on purpose (easy to read and debug):

```java
@Component
public class OrganizationMapper {
    public OrganizationResponse toResponse(Organization o) {
        return new OrganizationResponse(o.getId(), o.getName(), o.getDescription(),
                o.getContactEmail(), o.getCreatedAt());
    }
}
```

Only the service uses the mapper. For a create/update endpoint, the service copies the fields
from the request DTO into the entity itself (or add a `toEntity` method here).

## 3. Service – `service/OrganizationService.java`

Business logic and transactions. Returns DTOs.

```java
@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    // constructor injection only – no @Autowired on fields
    public OrganizationService(OrganizationRepository r, OrganizationMapper m) { ... }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAll() {
        return organizationRepository.findAll(Sort.by("name")).stream()
                .map(organizationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {
        return organizationRepository.findById(id)
                .map(organizationMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Organization " + id + " not found"));
    }
}
```

- `@Transactional` goes on **service** methods (`readOnly = true` for reads).
- Map to DTOs **inside** the transaction (lazy relations are only loadable there).
- Errors – use the classes in `common/exception`:

  | Throw | HTTP | Example |
  |-------|------|---------|
  | `new NotFoundException("Event 7 not found")` | 404 `NOT_FOUND` | unknown id |
  | `new ConflictException("EVENT_FULL", "Event is full")` | 409 | duplicate / capacity reached |
  | `new ApiException(HttpStatus.X, "CODE", "message")` | any | anything else |

## 4. Controller – `controller/OrganizationController.java`

HTTP only: URL, status codes, DTOs, OpenAPI docs. No logic, no repositories.

```java
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Student organizations (W1)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class OrganizationController {

    private final OrganizationService organizationService;
    public OrganizationController(OrganizationService s) { this.organizationService = s; }

    @GetMapping
    @Operation(summary = "List all organizations (sorted by name)")
    @ApiResponse(responseCode = "200", description = "List of organizations")
    public List<OrganizationResponse> findAll() { return organizationService.findAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Get one organization by id")
    @ApiResponse(responseCode = "200", description = "The organization")
    @ApiResponse(responseCode = "404", description = "Organization not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public OrganizationResponse getById(@PathVariable Long id) { return organizationService.getById(id); }
}
```

REST conventions (CLAUDE.md): base path `/api/v1`, plural nouns, `201` on create
(`@ResponseStatus(HttpStatus.CREATED)`), `204` on delete, `400/401/403/404/409` for errors.

Who is calling? Add a parameter:

```java
public UserResponse me(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
    return userService.getById(me.id());   // me.id(), me.email(), me.role()
}
```

(`@Parameter(hidden = true)` keeps it out of Swagger.)

## 5. Security – who may call the endpoint

**Level 1 – logged in (default).** `config/SecurityConfig.java` makes every path require a valid
JWT except health, Swagger, `POST /api/v1/auth/register` and `POST /api/v1/auth/login`.
The organizations endpoints need nothing extra: any logged-in user may read them.
To make a new path public, add it to `PUBLIC_PATHS` / `PUBLIC_POST_PATHS` there (ask the team first).

**Level 2 – global role.** Tokens carry `ROLE_STUDENT` or `ROLE_ADMIN`:

```java
@PreAuthorize("hasRole('ADMIN')")
```

**Level 3 – role inside an organization.** Use the `orgPermissions` bean
(`organization/service/OrgPermissionService.java`). Hierarchy: PRESIDENT ⊃ ORGANIZER ⊃ MEMBER.

```java
// any member of organization {orgId}
@PreAuthorize("@orgPermissions.isMember(principal.id, #orgId)")
// ORGANIZER or PRESIDENT
@PreAuthorize("@orgPermissions.isOrganizer(principal.id, #orgId)")
// PRESIDENT only, or a global admin
@PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")
@PostMapping("/{orgId}/events")
public EventResponse create(@PathVariable Long orgId, @Valid @RequestBody CreateEventRequest req) { ... }
```

- `principal` is the logged-in `AuthenticatedUser`; `#orgId` is the method parameter with that name.
- A global ADMIN is **not** automatically allowed by `orgPermissions`; write `hasRole('ADMIN') or ...`.
- A failed check returns **403 `FORBIDDEN`** (JSON error body); missing token returns **401**.
- If the endpoint only knows an event id, look up its organization in the service and check
  there with `orgPermissions.isOrganizer(...)`, throwing `new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "...")`.

## 6. Unit test for the service – `OrganizationServiceTest.java`

Pure JUnit 5 + Mockito: no Spring, no database, runs in milliseconds.

```java
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock private OrganizationRepository organizationRepository;
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(organizationRepository, new OrganizationMapper());
    }

    @Test
    void getByIdThrowsNotFoundForUnknownId() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Organization 99 not found");
    }
}
```

Pattern: **given** (mock repository answers) → **when** (call the service) → **then** (assert
result or exception). Test every business rule and every error the service can throw.
See also `OrgPermissionServiceTest` and `auth/AuthServiceTest`.

## 7. Integration test for the controller – `OrganizationControllerIntegrationTest.java`

Starts the real application + a real PostgreSQL 16 in Docker (Testcontainers), and sends HTTP
requests with MockMvc. Extend `support/IntegrationTestBase`, which provides:
`mockMvc`, `createUser(email, role)`, `loginAndGetToken(email)`, `tokenForNewStudent()`,
`bearer(token)`, `uniqueEmail(prefix)`, `json(object)`.

```java
class OrganizationControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private OrganizationRepository organizationRepository;
    private Organization organization;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        organization = organizationRepository.save(
                new Organization("Org " + UUID.randomUUID(), "A test organization", "org@test.ro"));
        token = tokenForNewStudent();          // real login through /api/v1/auth/login
    }

    @Test
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void getByUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{id}", 999_999_999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
```

- The database is **shared** by all integration tests and not cleaned between them: always use
  unique names/emails (`UUID`, `uniqueEmail(...)`). Repositories may be used in tests to set up data.
- Test at least: 401 without token, the happy path, 403 for a user without permission (if the
  endpoint uses `@PreAuthorize`), 404, 400 for invalid input, 409 for conflicts.
  `OrgPermissionPreAuthorizeIntegrationTest` shows how to test a 403 with memberships.
- **Naming:** test classes must end in `Test` (e.g. `...IntegrationTest`), otherwise Maven skips them.

### Running the tests

Docker must be running (Testcontainers starts `postgres:16-alpine`).

```bash
cd backend
./mvnw verify                                   # everything; must end with BUILD SUCCESS
./mvnw test -Dtest=OrganizationServiceTest      # one class
./mvnw test -Dtest='Organization*'              # by pattern
```

In IntelliJ: click the green arrow next to the class or method.

## 8. Database changes

Need a new column/table? Add a **new** Flyway file
`backend/src/main/resources/db/migration/V{next}__short_description.sql` (never edit an
existing one), then update the entity. `ddl-auto=validate` fails fast if they disagree.
Demo data for local dev goes in `db/seed-dev/R__dev_seed.sql` (dev profile only).

## 9. Try it in Swagger with a token

1. One-time: in the root `.env` set a real secret (at least 32 characters):
   ```bash
   openssl rand -base64 48      # paste the output as JWT_SECRET=... in .env
   ```
2. Start the database and the API with demo data:
   ```bash
   docker compose up -d postgres
   cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. Open <http://localhost:8080/swagger-ui.html>.
4. **Auth → POST /api/v1/auth/login → Try it out**, body:
   ```json
   { "email": "ana.popescu@stud.ubbcluj.ro", "password": "password" }
   ```
   (all seed users have password `password`; `admin@ubbevents.ro` is ADMIN; see `docs/data-model.md`.)
5. Copy `accessToken` from the response.
6. Click **Authorize** (top right), paste the token (**without** `Bearer `), **Authorize**, **Close**.
7. Call **GET /api/v1/organizations** → 200 with the seed organizations.
   **GET /api/v1/organizations/999** → 404. Click **Logout** in the Authorize dialog and call
   again → 401.

The token is valid for 2 hours (`JWT_EXPIRATION_MINUTES`); after that log in again (no refresh tokens).

With curl:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana.popescu@stud.ubbcluj.ro","password":"password"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
curl -s localhost:8080/api/v1/organizations -H "Authorization: Bearer $TOKEN"
```

## 10. Checklist before opening a PR

- [ ] Files in the right feature package (`controller`, `service`, `dto`, `mapper`, ...).
- [ ] Controller uses only DTOs and services; `@Valid` on request bodies.
- [ ] Correct status codes; errors via `common.exception` classes.
- [ ] Protected correctly (logged-in by default; `@PreAuthorize` for roles).
- [ ] OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponse` incl. errors).
- [ ] Service unit test + controller integration test (401 / 403 / 404 / 400 as relevant).
- [ ] `./mvnw verify` → BUILD SUCCESS.
- [ ] Usage log entry in `docs/genai/usage-log.md` if GenAI was used.
- [ ] Branch `feature/<issue-number>-short-name`, Conventional Commit message, PR with green CI (ask a teammate to look at it: this is a feature, see `CONTRIBUTING.md` §2).
