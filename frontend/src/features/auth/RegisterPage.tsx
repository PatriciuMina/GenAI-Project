import { useState, type FormEvent } from 'react';
import { Alert, Box, Button, Link, Stack, TextField, Typography } from '@mui/material';
import { Link as RouterLink, Navigate, useNavigate } from 'react-router';
import { parseApiError } from '../../api/errors';
import type { RegisterRequest } from '../../api/types';
import { paths } from '../../routes/paths';
import { AuthPageLayout } from './AuthPageLayout';
import { useAuth } from './useAuth';

type FieldErrors = Partial<Record<keyof RegisterRequest, string>>;

// Same limits as the backend RegisterRequest; the backend still validates everything.
function validate(values: RegisterRequest): FieldErrors {
  const errors: FieldErrors = {};
  if (!values.fullName.trim()) errors.fullName = 'Full name is required';
  else if (values.fullName.length > 150) errors.fullName = 'At most 150 characters';
  if (!values.email.trim()) errors.email = 'Email is required';
  else if (!/^\S+@\S+\.\S+$/.test(values.email.trim())) errors.email = 'Enter a valid email';
  if (values.password.length < 8) errors.password = 'At least 8 characters';
  else if (values.password.length > 72) errors.password = 'At most 72 characters';
  return errors;
}

export function RegisterPage() {
  const { status, register } = useAuth();
  const navigate = useNavigate();
  const [values, setValues] = useState<RegisterRequest>({ fullName: '', email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (status === 'authenticated') {
    return <Navigate to={paths.organizations} replace />;
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFormError(null);
    const errors = validate(values);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    try {
      await register({ ...values, fullName: values.fullName.trim(), email: values.email.trim() });
      navigate(paths.organizations, { replace: true });
    } catch (error) {
      const parsed = parseApiError(error);
      setFieldErrors(parsed.fieldErrors);
      setFormError(parsed.message);
      setSubmitting(false);
    }
  };

  const update = (field: keyof RegisterRequest) => (e: { target: { value: string } }) =>
    setValues((v) => ({ ...v, [field]: e.target.value }));

  return (
    <AuthPageLayout title="Create an account">
      <Box component="form" noValidate onSubmit={handleSubmit} aria-label="Register form">
        <Stack spacing={2}>
          {formError && <Alert severity="error">{formError}</Alert>}
          <TextField
            label="Full name"
            autoComplete="name"
            autoFocus
            value={values.fullName}
            onChange={update('fullName')}
            error={Boolean(fieldErrors.fullName)}
            helperText={fieldErrors.fullName}
            fullWidth
          />
          <TextField
            label="Email"
            type="email"
            autoComplete="email"
            value={values.email}
            onChange={update('email')}
            error={Boolean(fieldErrors.email)}
            helperText={fieldErrors.email}
            fullWidth
          />
          <TextField
            label="Password"
            type="password"
            autoComplete="new-password"
            value={values.password}
            onChange={update('password')}
            error={Boolean(fieldErrors.password)}
            helperText={fieldErrors.password ?? '8 to 72 characters'}
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={submitting}>
            {submitting ? 'Creating account…' : 'Register'}
          </Button>
        </Stack>
      </Box>
      <Typography variant="body2" sx={{ mt: 3, textAlign: 'center' }}>
        Already have an account?{' '}
        <Link component={RouterLink} to={paths.login}>
          Log in
        </Link>
      </Typography>
    </AuthPageLayout>
  );
}
