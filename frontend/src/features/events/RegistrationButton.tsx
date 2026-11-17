import { useState, type ReactNode } from 'react';
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
} from '@mui/material';
import { parseApiError } from '../../api/errors';
import type { Event } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import {
  useCancelRegistration,
  useMyRegistrations,
  useRegisterForEvent,
} from '../registrations/queries';
import { isCancellationAllowed, isRegistrationOpen } from './registrationRules';

interface RegistrationButtonProps {
  event: Event;
}

/**
 * The W4 action on the event details page (T-24). Shows one of: "You are registered" + "Cancel
 * registration" (with a confirmation), "Registration is closed", "Full", or a "Register" button.
 * Whether the user is registered comes from their own registrations. Backend errors (EVENT_FULL,
 * REGISTRATION_CLOSED, ALREADY_REGISTERED, ...) are shown with their readable message.
 */
export function RegistrationButton({ event }: RegistrationButtonProps) {
  const myRegistrations = useMyRegistrations();
  const register = useRegisterForEvent(event.id);
  const cancel = useCancelRegistration(event.id);
  const [confirmOpen, setConfirmOpen] = useState(false);

  if (myRegistrations.isPending) {
    return (
      <Button variant="contained" disabled>
        Checking your registration…
      </Button>
    );
  }
  if (myRegistrations.isError) {
    return (
      <ErrorAlert error={myRegistrations.error} onRetry={() => void myRegistrations.refetch()} />
    );
  }

  const myRegistration = myRegistrations.data.find(
    (r) => r.eventId === event.id && r.status === 'CONFIRMED',
  );
  const failure = register.error ?? cancel.error;

  const startRegistration = () => {
    cancel.reset();
    register.mutate();
  };
  const confirmCancellation = () => {
    setConfirmOpen(false);
    register.reset();
    cancel.mutate();
  };

  let state: ReactNode;
  if (myRegistration) {
    const checkedIn = myRegistration.checkedInAt !== null;
    state = (
      <Stack spacing={1.5} sx={{ alignItems: 'flex-start' }}>
        <Alert severity="success" role="status">
          {checkedIn ? 'You are registered and checked in.' : 'You are registered for this event.'}
        </Alert>
        {!checkedIn && isCancellationAllowed(event) && (
          <Button
            variant="outlined"
            color="error"
            disabled={cancel.isPending}
            onClick={() => setConfirmOpen(true)}
          >
            {cancel.isPending ? 'Cancelling…' : 'Cancel registration'}
          </Button>
        )}
      </Stack>
    );
  } else if (!isRegistrationOpen(event)) {
    state = (
      <Alert severity="info" role="status">
        Registration is closed for this event.
      </Alert>
    );
  } else if (event.freePlaces <= 0) {
    state = (
      <Alert severity="warning" role="status">
        This event is full: all places are taken.
      </Alert>
    );
  } else {
    state = (
      <Button variant="contained" disabled={register.isPending} onClick={startRegistration}>
        {register.isPending ? 'Registering…' : 'Register'}
      </Button>
    );
  }

  return (
    <Stack spacing={1.5} sx={{ alignItems: 'flex-start' }}>
      {failure && <Alert severity="error">{parseApiError(failure).message}</Alert>}
      {state}
      <Dialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        aria-labelledby="cancel-registration-title"
      >
        <DialogTitle id="cancel-registration-title">Cancel your registration?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You give up your place at “{event.title}”. You can register again later if registration
            is still open and places are left.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Keep my place</Button>
          <Button color="error" onClick={confirmCancellation}>
            Yes, cancel registration
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
