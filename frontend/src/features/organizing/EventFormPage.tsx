import { Alert, Button, Stack, Typography } from '@mui/material';
import { Link as RouterLink, useNavigate, useParams } from 'react-router';
import { errorStatus } from '../../api/errors';
import type { EventRequest, UpdateEventRequest } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { paths } from '../../routes/paths';
import { useMyMemberships } from '../organizations/queries';
import { EventForm } from './EventForm';
import { useCreateEvent, useEvent, useUpdateEvent } from './eventQueries';

export function EventFormPage() {
  const { id: idParam } = useParams();
  const editMode = idParam !== undefined;
  const id = Number(idParam);
  const validId = !editMode || (Number.isInteger(id) && id > 0);
  const navigate = useNavigate();
  const membershipsQuery = useMyMemberships();
  const eventQuery = useEvent(id);
  const createMutation = useCreateEvent();
  const updateMutation = useUpdateEvent(id);
  const eligible =
    membershipsQuery.data?.filter((m) => m.role === 'ORGANIZER' || m.role === 'PRESIDENT') ?? [];

  const back = (
    <Button component={RouterLink} to={paths.organizing}>
      Organizing
    </Button>
  );
  if (!validId || (eventQuery.isError && errorStatus(eventQuery.error) === 404)) {
    return (
      <Stack spacing={2} alignItems="flex-start">
        <Alert severity="warning">Event not found.</Alert>
        {back}
      </Stack>
    );
  }

  const isPending = membershipsQuery.isPending || (editMode && eventQuery.isPending);
  if (isPending)
    return <LoadingState label={editMode ? 'Loading event…' : 'Loading organizations…'} />;

  const queryError = membershipsQuery.error ?? (editMode ? eventQuery.error : null);
  if (queryError)
    return (
      <ErrorAlert
        error={queryError}
        onRetry={() => {
          void membershipsQuery.refetch();
          if (editMode) void eventQuery.refetch();
        }}
      />
    );

  if (editMode && !eventQuery.data) return <Alert severity="warning">Event not found.</Alert>;

  return (
    <Stack spacing={3}>
      {back}
      <Typography component="h1" variant="h4">
        {editMode ? 'Edit event' : 'Create event'}
      </Typography>
      {!editMode && eligible.length === 0 && (
        <Alert severity="info">
          You need an ORGANIZER or PRESIDENT role in an organization to create an event.
        </Alert>
      )}
      <EventForm
        memberships={membershipsQuery.data ?? []}
        event={editMode ? eventQuery.data : undefined}
        onSubmit={async (organizationId, body) => {
          const event = editMode
            ? await updateMutation.mutateAsync(body as UpdateEventRequest)
            : await createMutation.mutateAsync({
                orgId: organizationId,
                body: body as EventRequest,
              });
          navigate(paths.organizing, { replace: true });
          return event;
        }}
      />
    </Stack>
  );
}
