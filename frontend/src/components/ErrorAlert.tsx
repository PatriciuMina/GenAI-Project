import { Alert, Button } from '@mui/material';
import { parseApiError } from '../api/errors';

interface ErrorAlertProps {
  error: unknown;
  /** Shows a "Try again" button (e.g. pass `refetch` from useQuery). */
  onRetry?: () => void;
}

/** Shows the readable message of a backend error (ApiError JSON) or a network error. */
export function ErrorAlert({ error, onRetry }: ErrorAlertProps) {
  const { message } = parseApiError(error);
  return (
    <Alert
      severity="error"
      action={
        onRetry && (
          <Button color="inherit" size="small" onClick={onRetry}>
            Try again
          </Button>
        )
      }
    >
      {message}
    </Alert>
  );
}
