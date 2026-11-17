import { useState } from 'react';
import { Link as RouterLink } from 'react-router';
import {
  Alert,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Link,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import { parseApiError } from '../../api/errors';
import type { MyRegistration } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { EmptyState } from '../../components/EmptyState';
import { paths } from '../../routes/paths';
import { formatDateTime } from '../events/eventFormat';
import { isCancellationAllowed } from '../events/registrationRules';
import { useCancelRegistration, useMyRegistrations } from './queries';
import { registrationChip, splitRegistrations, type RegistrationChip } from './registrationSplit';

const chipColor: Record<RegistrationChip, 'success' | 'default' | 'info' | 'warning'> = {
  Confirmed: 'success',
  'Checked in': 'info',
  Cancelled: 'default',
  'Event cancelled': 'warning',
};

function RegistrationRow({ registration }: { registration: MyRegistration }) {
  const cancel = useCancelRegistration(registration.eventId);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const chip = registrationChip(registration);
  const canCancel =
    registration.status === 'CONFIRMED' &&
    registration.checkedInAt === null &&
    isCancellationAllowed({
      status: registration.eventStatus,
      startAt: registration.eventStartAt,
      registrationDeadline: null,
    });

  return (
    <Paper variant="outlined" sx={{ p: 2 }} component="li" aria-label={registration.eventTitle}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={1.5}
        sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
      >
        <Stack spacing={0.5}>
          <Link component={RouterLink} to={paths.event(registration.eventId)} variant="subtitle1">
            {registration.eventTitle}
          </Link>
          <Typography color="text.secondary" variant="body2">
            {formatDateTime(registration.eventStartAt)} · {registration.eventLocation}
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <Chip label={chip} color={chipColor[chip]} size="small" />
          {canCancel && (
            <Button
              size="small"
              color="error"
              variant="outlined"
              disabled={cancel.isPending}
              onClick={() => setConfirmOpen(true)}
            >
              {cancel.isPending ? 'Cancelling…' : 'Cancel'}
            </Button>
          )}
        </Stack>
      </Stack>
      {cancel.isError && (
        <Alert severity="error" sx={{ mt: 1.5 }}>
          {parseApiError(cancel.error).message}
        </Alert>
      )}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Cancel your registration?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You give up your place at “{registration.eventTitle}”.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Keep my place</Button>
          <Button
            color="error"
            onClick={() => {
              setConfirmOpen(false);
              cancel.mutate();
            }}
          >
            Yes, cancel registration
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}

function Section({
  title,
  registrations,
  emptyText,
}: {
  title: string;
  registrations: MyRegistration[];
  emptyText: string;
}) {
  return (
    <Stack spacing={1.5} component="section" aria-label={title}>
      <Typography component="h2" variant="h6">
        {title}
      </Typography>
      {registrations.length === 0 ? (
        <EmptyState message={emptyText} compact />
      ) : (
        <Stack spacing={1.5} component="ul" sx={{ listStyle: 'none', p: 0, m: 0 }}>
          {registrations.map((registration) => (
            <RegistrationRow key={registration.id} registration={registration} />
          ))}
        </Stack>
      )}
    </Stack>
  );
}

export function MyRegistrationsPage() {
  const { data, isPending, isError, error, refetch } = useMyRegistrations();

  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h4">
        My registrations
      </Typography>
      {isPending ? (
        <LoadingState label="Loading your registrations…" />
      ) : isError ? (
        <ErrorAlert error={error} onRetry={() => void refetch()} />
      ) : data.length === 0 ? (
        <EmptyState
          message="You have not registered for any event yet."
          icon={<EventBusyIcon />}
          action={{ label: 'Browse events', to: paths.events }}
        />
      ) : (
        <RegistrationSections registrations={data} />
      )}
    </Stack>
  );
}

function RegistrationSections({ registrations }: { registrations: MyRegistration[] }) {
  const { upcoming, past } = splitRegistrations(registrations);
  return (
    <>
      <Section title="Upcoming" registrations={upcoming} emptyText="No upcoming events." />
      <Section
        title="Past and cancelled"
        registrations={past}
        emptyText="No past or cancelled registrations."
      />
    </>
  );
}
