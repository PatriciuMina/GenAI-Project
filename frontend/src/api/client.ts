// The single axios instance used for every backend call. Feature code never calls axios directly:
// add a typed function in src/api/<resource>.ts instead (see organizations.ts).
import axios from 'axios';
import { getStoredToken } from './tokenStorage';

/** From VITE_API_BASE_URL; defaults to "/api/v1", which the Vite dev server proxies to :8080. */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach "Authorization: Bearer <token>" to every request when we have a token.
apiClient.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

type UnauthorizedHandler = () => void;
let unauthorizedHandler: UnauthorizedHandler | null = null;

/**
 * Registers what to do when an authenticated request comes back with 401 (token expired or
 * invalid). AuthProvider registers "log out and go to the login page". Returns an unregister fn.
 */
export function setUnauthorizedHandler(handler: UnauthorizedHandler): () => void {
  unauthorizedHandler = handler;
  return () => {
    if (unauthorizedHandler === handler) unauthorizedHandler = null;
  };
}

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    // Only react to 401s of requests that carried a token. A 401 from POST /auth/login means
    // "Invalid credentials" and must be shown on the form instead.
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      error.config?.headers?.Authorization
    ) {
      unauthorizedHandler?.();
    }
    return Promise.reject(error);
  },
);
