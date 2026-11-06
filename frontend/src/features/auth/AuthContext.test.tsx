import { act, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as authApi from '../../api/auth';
import { getStoredToken, storeToken } from '../../api/tokenStorage';
import type { User } from '../../api/types';
import * as usersApi from '../../api/users';
import { ProtectedRoute } from '../../routes/ProtectedRoute';
import { apiError, fakeJwt, withQueryClient } from '../../test/utils';
import { AuthProvider } from './AuthContext';
import { useAuth } from './useAuth';

vi.mock('../../api/auth');
vi.mock('../../api/users');

const ana: User = {
  id: 1,
  email: 'ana@ubb.ro',
  fullName: 'Ana Popescu',
  role: 'STUDENT',
  createdAt: '2026-11-01T10:00:00Z',
};

const inOneHour = () => Math.floor(Date.now() / 1000) + 3600;

/** Shows the auth state and offers login/logout buttons. */
function AuthProbe() {
  const { status, user, sessionExpired, login, logout } = useAuth();
  return (
    <div>
      <p>status: {status}</p>
      <p>user: {user?.fullName ?? 'none'}</p>
      <p>expired: {String(sessionExpired)}</p>
      <button onClick={() => void login('ana@ubb.ro', 'password')}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  );
}

function renderWithAuth(ui: React.ReactNode, initialPath = '/') {
  return render(
    withQueryClient(
      <AuthProvider>
        <MemoryRouter initialEntries={[initialPath]}>{ui}</MemoryRouter>
      </AuthProvider>,
    ),
  );
}

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('logs in: stores the token, loads the user; logout clears both', async () => {
    const token = fakeJwt(inOneHour());
    vi.mocked(authApi.login).mockResolvedValue({
      accessToken: token,
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
    });
    vi.mocked(usersApi.getCurrentUser).mockResolvedValue(ana);
    renderWithAuth(<AuthProbe />);

    expect(screen.getByText('status: anonymous')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'login' }));

    expect(await screen.findByText('status: authenticated')).toBeInTheDocument();
    expect(screen.getByText('user: Ana Popescu')).toBeInTheDocument();
    expect(authApi.login).toHaveBeenCalledWith({ email: 'ana@ubb.ro', password: 'password' });
    expect(getStoredToken()).toBe(token);

    await userEvent.click(screen.getByRole('button', { name: 'logout' }));
    expect(screen.getByText('status: anonymous')).toBeInTheDocument();
    expect(screen.getByText('expired: false')).toBeInTheDocument();
    expect(getStoredToken()).toBeNull();
  });

  it('restores the session from a stored, still valid token', async () => {
    storeToken(fakeJwt(inOneHour()));
    vi.mocked(usersApi.getCurrentUser).mockResolvedValue(ana);
    renderWithAuth(<AuthProbe />);

    expect(screen.getByText('status: loading')).toBeInTheDocument();
    expect(await screen.findByText('status: authenticated')).toBeInTheDocument();
  });

  it('drops an expired stored token and flags the session as expired', () => {
    storeToken(fakeJwt(Math.floor(Date.now() / 1000) - 60));
    renderWithAuth(<AuthProbe />);

    expect(screen.getByText('status: anonymous')).toBeInTheDocument();
    expect(screen.getByText('expired: true')).toBeInTheDocument();
    expect(getStoredToken()).toBeNull();
    expect(usersApi.getCurrentUser).not.toHaveBeenCalled();
  });

  it('logs out when the backend answers 401 for the stored token', async () => {
    storeToken(fakeJwt(inOneHour()));
    vi.mocked(usersApi.getCurrentUser).mockRejectedValue(
      apiError(401, 'UNAUTHORIZED', 'Missing, invalid or expired token'),
    );
    renderWithAuth(<AuthProbe />);

    expect(await screen.findByText('status: anonymous')).toBeInTheDocument();
    expect(screen.getByText('expired: true')).toBeInTheDocument();
    expect(getStoredToken()).toBeNull();
  });

  it('logs out automatically when the token expires', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    try {
      storeToken(fakeJwt(Math.floor(Date.now() / 1000) + 5));
      vi.mocked(usersApi.getCurrentUser).mockResolvedValue(ana);
      renderWithAuth(<AuthProbe />);
      expect(await screen.findByText('status: authenticated')).toBeInTheDocument();

      act(() => {
        vi.advanceTimersByTime(6000);
      });

      expect(screen.getByText('status: anonymous')).toBeInTheDocument();
      expect(screen.getByText('expired: true')).toBeInTheDocument();
    } finally {
      vi.useRealTimers();
    }
  });
});

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  const routes = (
    <Routes>
      <Route path="/login" element={<p>login page</p>} />
      <Route element={<ProtectedRoute />}>
        <Route path="/organizations" element={<p>organizations page</p>} />
      </Route>
      <Route
        path="/admin"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <p>admin page</p>
          </ProtectedRoute>
        }
      />
    </Routes>
  );

  it('redirects to /login when not logged in', () => {
    renderWithAuth(routes, '/organizations');
    expect(screen.getByText('login page')).toBeInTheDocument();
  });

  it('renders the page for a logged-in user', async () => {
    storeToken(fakeJwt(inOneHour()));
    vi.mocked(usersApi.getCurrentUser).mockResolvedValue(ana);
    renderWithAuth(routes, '/organizations');
    expect(await screen.findByText('organizations page')).toBeInTheDocument();
  });

  it('blocks a STUDENT from an ADMIN-only page', async () => {
    storeToken(fakeJwt(inOneHour()));
    vi.mocked(usersApi.getCurrentUser).mockResolvedValue(ana);
    renderWithAuth(routes, '/admin');
    await waitFor(() =>
      expect(screen.getByText('You do not have access to this page.')).toBeInTheDocument(),
    );
    expect(screen.queryByText('admin page')).not.toBeInTheDocument();
  });
});
