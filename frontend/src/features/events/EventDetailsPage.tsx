import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { Alert, Box, Button, Chip, Link, Paper, Stack, Typography } from '@mui/material';
import { Link as RouterLink, useParams } from 'react-router';
import { errorStatus } from '../../api/errors';
import type { EventStatus } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { paths } from '../../routes/paths';
import { useEvent } from '../organizing/eventQueries';
import { EventAnnouncements } from './EventAnnouncements';
import {
  categoryLabel,
  formatDateTime,
  placesText,
  registrationDeadlineText,
  statusLabel,
} from './eventFormat';

const statusColors: Record<EventStatus, 'default' | 'success' | 'warning' | 'info' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  REGISTRATION_CLOSED: 'warning',
  ONGOING: 'info',
  COMPLETED: 'default',
  CANCELLED: 'error',
};

function Detail({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <Typography variant="body2">
      <strong>{label}:</strong> {children}
    </Typography>
  );
}

/**
 * Everything a student needs to decide whether to take part in an event (W3): when, where, free places,
 * registration deadline and status. A draft that the user may not see, and an unknown event, both show
 * "Event not found" (the backend answers 404 for both).
 */
export function EventDetailsPage() {
  const { id: idParam } = useParams();
  const id = Number(idParam);
  const validId = Number.isInteger(id) && id > 0;
  const { data: event, isPending, isError, error, refetch } = useEvent(id);

  const back = (
    <Button component={RouterLink} to={paths.events} startIcon={<ArrowBackIcon />}>
      All events
    </Button>
  );

  if (!validId || (isError && errorStatus(error) === 404)) {
    return (
      <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
        <Alert severity="warning">Event not found.</Alert>
        {back}
      </Stack>
    );
  }

  return (
    <Stack spacing={2}>
      <Box>{back}</Box>
      {isPending && <LoadingState label="Loading event…" />}
      {isError && <ErrorAlert error={error} onRetry={() => void refetch()} />}
      {event && (
        <Paper variant="outlined" sx={{ p: { xs: 2, sm: 4 } }}>
          <Stack direction="row" spacing={1} sx={{ mb: 1, flexWrap: 'wrap', rowGap: 1 }}>
            <Chip label={statusLabel(event.status)} color={statusColors[event.status]} size="small" />
            <Chip label={categoryLabel(event.category)} size="small" variant="outlined" />
          </Stack>
          <Typography component="h1" variant="h4" gutterBottom>
            {event.title}
          </Typography>
          <Typography sx={{ whiteSpace: 'pre-line', mb: 3 }}>
            {event.description || 'No description.'}
          </Typography>
          <Stack spacing={1}>
            <Detail label="Organized by">
              <Link component={RouterLink} to={paths.organization(event.organizationId)}>
                {event.organizationName}
              </Link>
            </Detail>
            <Detail label="Starts">{formatDateTime(event.startAt)}</Detail>
            <Detail label="Ends">{formatDateTime(event.endAt)}</Detail>
            <Detail label="Location">{event.location}</Detail>
            <Detail label="Places">{placesText(event)}</Detail>
            <Detail label="Capacity">{event.capacity}</Detail>
            <Detail label="Registration">{registrationDeadlineText(event.registrationDeadline)}</Detail>
          </Stack>
        </Paper>
      )}
      {/* TODO(T-24): the Register / Cancel registration button goes here (ticket "Register / Cancel button"). */}
      {event && <EventAnnouncements eventId={event.id} />}
    </Stack>
  );
}
