import type { ReactNode } from 'react';
import { Alert, Container } from '@mui/material';
import { Navigate, Outlet, useLocation } from 'react-router';
import type { GlobalRole } from '../api/types';
import { LoadingState } from '../components/LoadingState';
import type { LoginLocationState } from '../features/auth/LoginPage';
import { useAuth } from '../features/auth/useAuth';
import { paths } from './paths';

interface ProtectedRouteProps {
  /** If set, only users with one of these global roles may enter. */
  roles?: GlobalRole[];
  /** Rendered when allowed; defaults to the nested routes (<Outlet />). */
  children?: ReactNode;
}

/**
 * Guards routes that need a logged-in user. Not logged in (or token expired / 401) -> redirect to
 * /login, remembering the requested page so the user comes back after logging in.
 */
export function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
  const { status, user } = useAuth();
  const location = useLocation();

  if (status === 'loading') {
    return <LoadingState label="Checking your session…" />;
  }
  if (status === 'anonymous' || user === null) {
    const state: LoginLocationState = {
      from: { pathname: location.pathname, search: location.search },
    };
    return <Navigate to={paths.login} replace state={state} />;
  }
  if (roles && !roles.includes(user.role)) {
    return (
      <Container sx={{ py: 4 }}>
        <Alert severity="warning">You do not have access to this page.</Alert>
      </Container>
    );
  }
  return children ?? <Outlet />;
}
