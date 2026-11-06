// TypeScript mirrors of the backend DTOs (Java records). Keep them in sync with the backend
// (Swagger UI at http://localhost:8080/swagger-ui.html shows the exact shapes).

/** Application-wide role (backend `GlobalRole`). */
export type GlobalRole = 'STUDENT' | 'ADMIN';

/** Role inside one organization (backend `OrganizationRole`). Not exposed per user yet. */
export type OrganizationRole = 'PRESIDENT' | 'ORGANIZER' | 'MEMBER';

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
