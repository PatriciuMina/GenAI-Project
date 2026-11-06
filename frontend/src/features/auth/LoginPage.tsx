import { Alert, Link, Typography } from '@mui/material';
import { Link as RouterLink, Navigate, useLocation, useNavigate } from 'react-router';
import { paths } from '../../routes/paths';
import { AuthPageLayout } from './AuthPageLayout';
import { LoginForm, type LoginFormValues } from './LoginForm';
import { useAuth } from './useAuth';

/** ProtectedRoute puts the page the user wanted into location.state.from. */
export interface LoginLocationState {
  from?: { pathname: string; search?: string };
}

export function LoginPage() {
  const { status, sessionExpired, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as LoginLocationState | null)?.from;
  const target = from ? `${from.pathname}${from.search ?? ''}` : paths.organizations;

  if (status === 'authenticated') {
    return <Navigate to={target} replace />;
  }

  const handleLogin = async ({ email, password }: LoginFormValues) => {
    await login(email, password);
    navigate(target, { replace: true });
  };

  return (
    <AuthPageLayout title="Log in">
      {sessionExpired && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Your session has expired. Please log in again.
        </Alert>
      )}
      <LoginForm onSubmit={handleLogin} />
      <Typography variant="body2" sx={{ mt: 3, textAlign: 'center' }}>
        No account yet?{' '}
        <Link component={RouterLink} to={paths.register}>
          Register
        </Link>
      </Typography>
    </AuthPageLayout>
  );
}
