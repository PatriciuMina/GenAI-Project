import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import * as authApi from '../../api/auth';
import { setUnauthorizedHandler } from '../../api/client';
import { errorStatus } from '../../api/errors';
import {
  clearStoredToken,
  getStoredToken,
  getTokenExpiry,
  isTokenExpired,
  storeToken,
} from '../../api/tokenStorage';
import type { RegisterRequest, User } from '../../api/types';
import { getCurrentUser } from '../../api/users';

/** "loading" = a stored token exists and we are fetching /users/me. */
export type AuthStatus = 'loading' | 'authenticated' | 'anonymous';

export interface AuthContextValue {
  status: AuthStatus;
  user: User | null;
  /** True after an automatic logout (token expired or a 401). The login page shows a notice. */
  sessionExpired: boolean;
  login: (email: string, password: string) => Promise<void>;
  /** Creates the account, then logs in with the same credentials (decision D6). */
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
}

// eslint-disable-next-line react-refresh/only-export-components -- context object is shared with useAuth
export const AuthContext = createContext<AuthContextValue | null>(null);

// setTimeout breaks for delays above ~24.8 days; tokens live 2 h, but stay safe.
const MAX_TIMEOUT_MS = 2_147_483_647;

function readInitialToken(): { token: string | null; expired: boolean } {
  const stored = getStoredToken();
  if (stored !== null && isTokenExpired(stored)) {
    clearStoredToken();
    return { token: null, expired: true };
  }
  return { token: stored, expired: false };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  // Read the stored token once on start-up; an already expired one counts as "session expired".
  const [initial] = useState(readInitialToken);
  const [token, setToken] = useState<string | null>(initial.token);
  const [user, setUser] = useState<User | null>(null);
  const [sessionExpired, setSessionExpired] = useState<boolean>(initial.expired);

  const status: AuthStatus =
    token === null ? 'anonymous' : user === null ? 'loading' : 'authenticated';

  const endSession = useCallback(
    (expired: boolean) => {
      clearStoredToken();
      setToken(null);
      setUser(null);
      setSessionExpired(expired);
      queryClient.clear(); // never show one user's cached data to the next user
    },
    [queryClient],
  );

  const logout = useCallback(() => endSession(false), [endSession]);

  // A 401 on any authenticated request means the token is no longer valid.
  useEffect(() => setUnauthorizedHandler(() => endSession(true)), [endSession]);

  // Load the user for a token we already have (page reload). A login sets the user itself.
  useEffect(() => {
    if (token === null || user !== null) return;
    let cancelled = false;
    getCurrentUser()
      .then((me) => {
        if (!cancelled) setUser(me);
      })
      .catch((error: unknown) => {
        if (cancelled) return;
        // 401 is already handled by the interceptor; anything else (server down): log out quietly.
        endSession(errorStatus(error) === 401);
      });
    return () => {
      cancelled = true;
    };
  }, [token, user, endSession]);

  // Log out exactly when the token expires, even if the user makes no request.
  useEffect(() => {
    if (token === null) return;
    const expiry = getTokenExpiry(token);
    if (expiry === null) return;
    const timer = setTimeout(() => endSession(true), Math.min(expiry - Date.now(), MAX_TIMEOUT_MS));
    return () => clearTimeout(timer);
  }, [token, endSession]);

  const login = useCallback(
    async (email: string, password: string) => {
      const { accessToken } = await authApi.login({ email, password });
      storeToken(accessToken);
      try {
        const me = await getCurrentUser();
        queryClient.clear();
        setUser(me);
        setToken(accessToken);
        setSessionExpired(false);
      } catch (error) {
        clearStoredToken();
        throw error;
      }
    },
    [queryClient],
  );

  const register = useCallback(
    async (request: RegisterRequest) => {
      await authApi.register(request);
      await login(request.email, request.password);
    },
    [login],
  );

  const value = useMemo<AuthContextValue>(
    () => ({ status, user, sessionExpired, login, register, logout }),
    [status, user, sessionExpired, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
