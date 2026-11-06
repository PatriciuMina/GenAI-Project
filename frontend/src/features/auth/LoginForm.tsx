import { useState, type FormEvent } from 'react';
import { Alert, Box, Button, Stack, TextField } from '@mui/material';
import { parseApiError } from '../../api/errors';

export interface LoginFormValues {
  email: string;
  password: string;
}

interface LoginFormProps {
  /** Called with the values; a rejected promise is shown as an error (backend ApiError JSON). */
  onSubmit: (values: LoginFormValues) => Promise<void>;
}

type FieldErrors = Partial<Record<keyof LoginFormValues, string>>;

function validate(values: LoginFormValues): FieldErrors {
  const errors: FieldErrors = {};
  if (!values.email.trim()) errors.email = 'Email is required';
  if (!values.password) errors.password = 'Password is required';
  return errors;
}

export function LoginForm({ onSubmit }: LoginFormProps) {
  const [values, setValues] = useState<LoginFormValues>({ email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFormError(null);
    const errors = validate(values);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    try {
      await onSubmit({ email: values.email.trim(), password: values.password });
    } catch (error) {
      const parsed = parseApiError(error);
      setFieldErrors(parsed.fieldErrors);
      setFormError(parsed.message);
    } finally {
      setSubmitting(false);
    }
  };

  const update = (field: keyof LoginFormValues) => (e: { target: { value: string } }) =>
    setValues((v) => ({ ...v, [field]: e.target.value }));

  return (
    <Box component="form" noValidate onSubmit={handleSubmit} aria-label="Login form">
      <Stack spacing={2}>
        {formError && <Alert severity="error">{formError}</Alert>}
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          autoFocus
          value={values.email}
          onChange={update('email')}
          error={Boolean(fieldErrors.email)}
          helperText={fieldErrors.email}
          fullWidth
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="current-password"
          value={values.password}
          onChange={update('password')}
          error={Boolean(fieldErrors.password)}
          helperText={fieldErrors.password}
          fullWidth
        />
        <Button type="submit" variant="contained" size="large" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Log in'}
        </Button>
      </Stack>
    </Box>
  );
}
