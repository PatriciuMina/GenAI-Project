import {
  Button,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router';
import type { Event, EventStatus } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { paths } from '../../routes/paths';
import { useMyMemberships } from '../organizations/queries';
import { formatDateTime, statusLabel } from '../events/eventFormat';
import { useOrganizationEvents } from './queries';

const statusColors: Record<EventStatus, 'default' | 'success' | 'warning' | 'info' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  REGISTRATION_CLOSED: 'warning',
  ONGOING: 'info',
  COMPLETED: 'default',
  CANCELLED: 'error',
};

export function OrganizingPage() {
  const membershipsQuery = useMyMemberships();
  const navigate = useNavigate();
  const memberships = membershipsQuery.data ?? [];
  const organizerMemberships = memberships.filter(
    ({ role }) => role === 'ORGANIZER' || role === 'PRESIDENT',
  );
  const eventQueries = useOrganizationEvents(organizerMemberships);
  const events = eventQueries.flatMap((query) => query.data ?? []);
  const hasEventError = eventQueries.some((query) => query.isError);

  return (
    <Stack spacing={2}>
      <Typography component="h1" variant="h4">
        Organizing
      </Typography>
      <Button
        component={RouterLink}
        to={paths.organizingNewEvent}
        variant="contained"
        sx={{ alignSelf: 'flex-start' }}
      >
        New event
      </Button>

      {membershipsQuery.isPending && <LoadingState label="Loading your organizations…" />}
      {membershipsQuery.isError && (
        <ErrorAlert error={membershipsQuery.error} onRetry={() => void membershipsQuery.refetch()} />
      )}
      {membershipsQuery.data && organizerMemberships.length === 0 && (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography>You are not an organizer in any organization</Typography>
        </Paper>
      )}
      {organizerMemberships.length > 0 &&
        eventQueries.some((query) => query.isPending) && (
          <LoadingState label="Loading organization events…" />
        )}
      {eventQueries.map(
        (query, index) =>
          query.isError && (
            <ErrorAlert
              key={organizerMemberships[index].organizationId}
              error={query.error}
              onRetry={() => void query.refetch()}
            />
          ),
      )}
      {organizerMemberships.length > 0 && !eventQueries.some((query) => query.isPending) &&
        !hasEventError && events.length === 0 && (
          <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
            <Typography color="text.secondary">No events in your organizations yet.</Typography>
          </Paper>
        )}
      {events.length > 0 && (
        <TableContainer component={Paper} variant="outlined">
          <Table aria-label="Events in your organizations">
            <TableHead>
              <TableRow>
                <TableCell>Title</TableCell>
                <TableCell>Start</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Confirmed / capacity</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {events.map((event: Event) => (
                <TableRow
                  key={event.id}
                  hover
                  role="link"
                  tabIndex={0}
                  aria-label={event.title}
                  onClick={() => navigate(paths.organizingEvent(event.id))}
                  onKeyDown={(keyboardEvent) => {
                    if (keyboardEvent.key === 'Enter' || keyboardEvent.key === ' ') {
                      keyboardEvent.preventDefault();
                      navigate(paths.organizingEvent(event.id));
                    }
                  }}
                  sx={{ cursor: 'pointer' }}
                >
                  <TableCell>{event.title}</TableCell>
                  <TableCell>{formatDateTime(event.startAt)}</TableCell>
                  <TableCell>
                    <Chip label={statusLabel(event.status)} color={statusColors[event.status]} size="small" />
                  </TableCell>
                  <TableCell>{event.confirmedCount} / {event.capacity}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Stack>
  );
}
