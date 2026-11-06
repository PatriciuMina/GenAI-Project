# Frontend guide

This guide explains how the React frontend in `/frontend` is built and how to extend it.
You do not need any AI tool: everything here is plain React + TypeScript.

Read first: `CLAUDE.md` (rules) and `docs/dev-guide/how-to-add-a-feature.md` (backend side).

---

## 1. Run it

Requirements: **Node 24 LTS** (`frontend/.nvmrc`; Node 22.12+ also works) and the backend running.

```bash
docker compose up -d postgres                         # only PostgreSQL
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # API on :8080 + seed
cd frontend && npm install && npm run dev             # UI on http://localhost:5173
```

Log in with a seed user, e.g. `ana.popescu@stud.ubbcluj.ro` / `password`
(admin: `admin@ubbevents.ro` / `password`).

**Why no CORS setup?** The browser only talks to `http://localhost:5173`. Vite forwards every
request starting with `/api` to `http://localhost:8080` (`server.proxy` in `vite.config.ts`).

**Environment:** `VITE_API_BASE_URL` (default `/api/v1`). To change it locally, copy
`frontend/.env.example` to `frontend/.env.local` (git-ignored). Only `VITE_*` variables reach
the browser, so never put secrets there.

## 2. Commands

| Command | What it does |
|---------|--------------|
| `npm run dev` | Dev server with hot reload on :5173 |
| `npm run lint` | ESLint (TypeScript, React hooks, no `any`); fails on any warning |
| `npm run format` / `format:check` | Prettier: fix / only check formatting |
| `npm test` | Vitest + React Testing Library, single run |
| `npm run test:watch` | Tests in watch mode while you code |
| `npm run build` | Type-check (`tsc -b`) + production build into `dist/` |

Before opening a PR, run `npm run lint && npm test && npm run build`. All three must pass.

## 3. Folder structure

```
frontend/src
├── main.tsx, App.tsx      entry point; providers: MUI theme → TanStack Query → Auth → Router
├── theme.ts               MUI theme (PLACEHOLDER colors)
├── api/                   THE ONLY place that talks HTTP
│   ├── client.ts          axios instance: base URL, adds "Bearer <token>", reacts to 401
│   ├── tokenStorage.ts    JWT in localStorage + reading its expiry
│   ├── errors.ts          parseApiError(): backend error JSON → readable message + field errors
│   ├── types.ts           TypeScript copies of the backend DTOs
│   ├── queryClient.ts     TanStack Query defaults (no retry on 4xx)
│   └── auth.ts, users.ts, organizations.ts   one typed function per endpoint
├── components/            shared UI: AppLayout (top bar + nav), Logo, ComingSoon, ErrorAlert, ...
├── routes/                paths.ts (all URLs), router.tsx (route table), ProtectedRoute.tsx
├── features/              one folder per feature (same split as the backend)
│   ├── auth/              AuthContext, useAuth, LoginForm, LoginPage, RegisterPage
│   ├── organizations/     queries.ts (hooks), list + details pages        (W1)
│   ├── events/            Coming soon                                     (W2, W3)
│   ├── registrations/     Coming soon                                     (W4)
│   ├── organizing/        Coming soon                                     (W5)
│   └── admin/             Coming soon, ADMIN only
└── test/                  setup.ts + helpers (apiError, fakeJwt, withQueryClient)
```

Rules:
- Pages never import axios. They use a hook from `features/<x>/queries.ts`, which calls a
  function from `src/api/<resource>.ts`.
- Use `paths.*` from `routes/paths.ts` instead of writing URLs as strings.
- TypeScript is `strict`. Don't use `any`; if you really must, add
  `// eslint-disable-next-line @typescript-eslint/no-explicit-any -- <reason>`.
- Tests live next to the code: `Something.tsx` → `Something.test.tsx`.

## 4. How authentication works

1. `LoginPage` → `useAuth().login(email, password)` → `POST /auth/login` returns `accessToken`.
2. The token is saved in `localStorage` (`tokenStorage.ts`), then `GET /users/me` loads the user
   (name, role). The login response itself has no user info.
3. Every request made through `apiClient` gets `Authorization: Bearer <token>`.
4. The session ends automatically when:
   - the token's `exp` time is reached (a timer in `AuthContext`), or
   - any authenticated request gets **401** (interceptor in `client.ts`), or
   - the page is reloaded with an already expired token.
   The user is then sent to `/login` with "Your session has expired" and, after logging in,
   comes back to the page they were on.
5. `logout()` (button in the top bar) removes the token and clears all cached query data.

In any component:

```tsx
const { status, user, logout } = useAuth(); // status: 'loading' | 'authenticated' | 'anonymous'
if (user?.role === 'ADMIN') { /* ... */ }
```

## 5. How to add a page

Example: an "Event details" page at `/events/:id`.

1. **Create the component** in its feature folder, `src/features/events/EventDetailsPage.tsx`:

   ```tsx
   import { Typography } from '@mui/material';
   import { useParams } from 'react-router';

   export function EventDetailsPage() {
     const { id } = useParams();
     return <Typography variant="h4">Event {id}</Typography>;
   }
   ```

2. **Add the URL** to `src/routes/paths.ts`:

   ```ts
   event: (id: number | string) => `/events/${id}`,
   ```

3. **Register the route** in `src/routes/router.tsx`, inside the `AppLayout` children (so it
   gets the top bar and requires login):

   ```tsx
   { path: '/events/:id', element: <EventDetailsPage /> },
   ```

4. **Optional: add it to the navigation** in `NAV_ITEMS` in `src/components/AppLayout.tsx`
   (only for top-level pages; set `roles: ['ADMIN']` to show it to admins only).

5. Link to it with `<Button component={RouterLink} to={paths.event(42)}>` (import
   `Link as RouterLink` from `react-router`).

## 6. How to call an API with TanStack Query

Follow `features/organizations/` exactly.

**Step 1: type the DTO** in `src/api/types.ts`, copying the backend record (check Swagger UI at
http://localhost:8080/swagger-ui.html):

```ts
export interface EventSummary {
  id: number;
  title: string;
  startsAt: string; // ISO date-time from the backend
}
```

**Step 2: one function per endpoint** in `src/api/events.ts`. Paths are relative to
`/api/v1`:

```ts
import { apiClient } from './client';
import type { EventSummary } from './types';

export async function getEvents(): Promise<EventSummary[]> {
  const { data } = await apiClient.get<EventSummary[]>('/events');
  return data;
}

export async function createEvent(body: CreateEventRequest): Promise<EventSummary> {
  const { data } = await apiClient.post<EventSummary>('/events', body);
  return data;
}
```

**Step 3: hooks** in `src/features/events/queries.ts`:

```ts
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createEvent, getEvents } from '../../api/events';

export const eventKeys = { all: ['events'] as const };

/** Read data: useQuery caches it by key and gives you isPending / isError / data. */
export function useEvents() {
  return useQuery({ queryKey: eventKeys.all, queryFn: getEvents });
}

/** Change data: useMutation, then invalidate the list so it reloads. */
export function useCreateEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createEvent,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: eventKeys.all }),
  });
}
```

**Step 4: use it in the page**, with the shared loading and error components:

```tsx
const { data: events, isPending, isError, error, refetch } = useEvents();

{isPending && <LoadingState />}
{isError && <ErrorAlert error={error} onRetry={() => void refetch()} />}
{events?.map((e) => <div key={e.id}>{e.title}</div>)}
```

For a mutation in a form:

```tsx
const createEvent = useCreateEvent();
const parsed = createEvent.error ? parseApiError(createEvent.error) : null;
// parsed.message → show in an <Alert>; parsed.fieldErrors.title → helperText of the "title" field
createEvent.mutate(values, { onSuccess: (created) => navigate(paths.event(created.id)) });
```

### Showing backend errors

Every backend error has the same JSON (`ApiError`):
`{ status, code, message, fieldErrors: [{ field, message }] }`.
`parseApiError(error)` from `src/api/errors.ts` turns it into:

- `message`: the backend message (e.g. "Invalid credentials", "Organization 999 not found").
  For validation errors it says "Please correct the highlighted fields."
- `fieldErrors`: `{ email: 'must be a well-formed email address', ... }`. The keys are the
  backend DTO field names, so give your form fields the same names.
- `status` and `code` if you need to react differently (e.g. `status === 404` → "not found" view).

`<ErrorAlert error={error} />` shows the message for you. See `LoginForm.tsx` and
`RegisterPage.tsx` for per-field errors.

## 7. How to protect a route

- **Login required:** put the route inside the `AppLayout` children in `router.tsx`. They are
  already wrapped in `<ProtectedRoute />`, which sends anonymous users to `/login`.
- **Role required** (global role `STUDENT` / `ADMIN`): wrap the element:

  ```tsx
  { path: paths.admin, element: (
      <ProtectedRoute roles={['ADMIN']}>
        <AdminPage />
      </ProtectedRoute>
  ) },
  ```

  Other users see "You do not have access to this page."
- **Hide a nav item** by role: set `roles` on the item in `NAV_ITEMS` (`AppLayout.tsx`).
- The client-side checks are only for UX. **The backend must still enforce permissions**
  (`@PreAuthorize`, `OrgPermissionService`). Organization roles (PRESIDENT/ORGANIZER/MEMBER) are
  not available on the client yet. See the `TODO(W5)` in `AppLayout.tsx`.

## 8. Tests

Vitest runs in a fake browser (jsdom). React Testing Library finds elements the way a user
would (label, role, text). Examples:

- `features/auth/LoginForm.test.tsx`: component test (typing, validation, backend errors).
- `features/auth/AuthContext.test.tsx`: login/logout, expired token, 401, `ProtectedRoute`.
- `api/client.test.ts`: Bearer header and 401 handling.

Mock the API module instead of the network:

```ts
import * as eventsApi from '../../api/events';
vi.mock('../../api/events');

vi.mocked(eventsApi.getEvents).mockResolvedValue([{ id: 1, title: 'Hackathon', startsAt: '...' }]);
vi.mocked(eventsApi.getEvents).mockRejectedValue(apiError(404, 'NOT_FOUND', 'Event not found'));
```

Wrap components that use queries with `withQueryClient(...)` from `src/test/utils.tsx`, and
components that use links/routes with `<MemoryRouter>`.

## 9. Theme and logo

`src/theme.ts` and `src/components/Logo.tsx` (plus `public/logo-placeholder.svg`, the browser
tab icon) are **placeholders**. Change the palette in `theme.ts` only; components read colors
from the theme (`color="primary"`, `sx={{ bgcolor: 'primary.main' }}`), so never hard-code hex
values in components.

## 10. Placeholder pages

`Events`, `My registrations`, `Organizing` and `Admin` show "Coming soon". Each file has a
`TODO(Wn)` comment naming its workflow. To find them all, run `grep -rn "TODO(W" frontend/src`.
