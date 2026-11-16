// TypeScript mirrors of the backend DTOs (Java records). Keep them in sync with the backend
// (Swagger UI at http://localhost:8080/swagger-ui.html shows the exact shapes).

/** Application-wide role (backend `GlobalRole`). */
export type GlobalRole = 'STUDENT' | 'ADMIN';

/** Role inside one organization (backend `OrganizationRole`). */
export type OrganizationRole = 'PRESIDENT' | 'ORGANIZER' | 'MEMBER';

/** Backend `MembershipResponse` (GET /users/me/memberships). */
export interface Membership {
  membershipId: number;
  organizationId: number;
  organizationName: string;
  role: OrganizationRole;
  /** ISO-8601 instant. */
  joinedAt: string;
}

/** Backend `MemberResponse` (GET /organizations/{id}/members). */
export interface Member {
  membershipId: number;
  userId: number;
  fullName: string;
  email: string;
  role: OrganizationRole;
  /** ISO-8601 instant. */
  joinedAt: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

/** Backend `TaskResponse` (GET/POST/PATCH /events/{id}/tasks). */
export interface EventTask {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  dueAt: string | null;
  assignee: { membershipId: number; fullName: string } | null;
  createdAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description: string | null;
  dueAt: string | null;
  assigneeMembershipId: number | null;
}

export interface UpdateTaskRequest {
  status?: TaskStatus;
  assigneeMembershipId?: number;
  unassign?: boolean;
}

/** Body of POST /auth/login. */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Response of POST /auth/login. */
export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
}

/** Body of POST /auth/register. New accounts are always STUDENT. */
export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

/** Backend `UserResponse` (GET /users/me, POST /auth/register). */
export interface User {
  id: number;
  email: string;
  fullName: string;
  role: GlobalRole;
  /** ISO-8601 instant, e.g. "2026-11-06T10:15:30Z". */
  createdAt: string;
}

/** Backend `OrganizationResponse`. */
export interface Organization {
  id: number;
  name: string;
  description: string | null;
  contactEmail: string | null;
  createdAt: string;
}

/** Backend EventCategory. */
export type EventCategory =
  'ACADEMIC' | 'CAREER' | 'CULTURAL' | 'SOCIAL' | 'SPORTS' | 'VOLUNTEERING' | 'WORKSHOP' | 'OTHER';

export type EventStatus =
  'DRAFT' | 'PUBLISHED' | 'REGISTRATION_CLOSED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';

/** EventResponse. */
export interface Event {
  id: number;
  organizationId: number;
  organizationName: string;
  title: string;
  description: string | null;
  location: string;
  category: EventCategory;
  startAt: string;
  endAt: string;
  capacity: number;
  registrationDeadline: string | null;
  status: EventStatus;
  confirmedCount: number;
  freePlaces: number;
  version: number;
}

/** Backend `PageResponse<T>` returned by paged endpoints. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Backend `AnnouncementResponse` (GET /events/{id}/announcements). */
export interface Announcement {
  id: number;
  title: string;
  body: string;
  authorName: string;
  createdAt: string;
}

/** Backend `AnnouncementRequest` (POST /events/{id}/announcements). */
export interface CreateAnnouncementRequest {
  title: string;
  body: string;
}

/** EventRequest / editable fields of UpdateEventRequest. */
export interface EventRequest {
  title: string;
  description: string | null;
  location: string;
  category: EventCategory;
  startAt: string;
  endAt: string;
  capacity: number;
  registrationDeadline: string | null;
}

export interface UpdateEventRequest extends EventRequest {
  version: number;
}

/** Body of POST /organizations (ADMIN only). Optional fields may be null. */
export interface CreateOrganizationRequest {
  name: string;
  description: string | null;
  contactEmail: string | null;
  /** An existing account; becomes the organization's PRESIDENT. */
  presidentEmail: string;
}

/** Backend `FieldErrorDto`. */
export interface ApiFieldError {
  field: string;
  message: string;
}

/** Backend `ApiError`: the JSON body of every error response. */
export interface ApiErrorBody {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  fieldErrors: ApiFieldError[];
}
