import { Alert, Button, Chip, Divider, Paper, Stack, Typography } from '@mui/material';
import { Link as RouterLink, useParams } from 'react-router';
import { errorStatus } from '../../api/errors';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { paths } from '../../routes/paths';
import { formatDateTime, registrationDeadlineText, statusLabel } from '../events/eventFormat';
import { useEvent } from './eventQueries';
import { StatusActions } from './StatusActions';

export function OrganizerEventPage() {
  const { id: idParam } = useParams();
  const id = Number(idParam);
  const validId = Number.isInteger(id) && id > 0;
  const query = useEvent(id);

  if (!validId || (query.isError && errorStatus(query.error) === 404))
    return <Alert severity="warning">Event not found.</Alert>;
  if (query.isPending) return <LoadingState label="Loading event…" />;
  if (query.isError) return <ErrorAlert error={query.error} onRetry={() => void query.refetch()} />;
  const event = query.data;
  if (!event) return <Alert severity="warning">Event not found.</Alert>;

  return (
    <Stack spacing={3}>
      <Button component={RouterLink} to={paths.organizing}>Organizing</Button>
      <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
        <Typography component="h1" variant="h4">{event.title}</Typography>
        <Chip label={statusLabel(event.status)} />
      </Stack>
      <Typography color="text.secondary">{event.organizationName}</Typography>
      <Typography>{event.description || 'No description.'}</Typography>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography><strong>Location:</strong> {event.location}</Typography>
          <Typography><strong>Starts:</strong> {formatDateTime(event.startAt)}</Typography>
          <Typography><strong>Ends:</strong> {formatDateTime(event.endAt)}</Typography>
          <Typography><strong>Registration:</strong> {registrationDeadlineText(event.registrationDeadline)}</Typography>
          <Typography><strong>Places:</strong> {event.confirmedCount} confirmed · {event.freePlaces} free of {event.capacity}</Typography>
        </Stack>
      </Paper>
      <StatusActions
        eventId={event.id}
        status={event.status}
        editPath={
          event.status === 'DRAFT' || event.status === 'PUBLISHED'
            ? paths.organizingEditEvent(event.id)
            : undefined
        }
      />
      <Divider />
      {['Tasks', 'Participants', 'Announcements'].map((section) => (
        <Paper key={section} variant="outlined" sx={{ p: 2 }}>
          <Typography component="h2" variant="h6">{section}</Typography>
          <Typography color="text.secondary">{section} will appear here.</Typography>
        </Paper>
      ))}
    </Stack>
  );
}
