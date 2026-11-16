import { useState } from 'react';
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Stack } from '@mui/material';
import { Link as RouterLink } from 'react-router';
import type { EventStatus } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { useChangeEventStatus } from './eventQueries';

const actions: Partial<Record<EventStatus, { label: string; next: EventStatus }[]>> = {
  DRAFT: [{ label: 'Publish', next: 'PUBLISHED' }],
  PUBLISHED: [
    { label: 'Close registration', next: 'REGISTRATION_CLOSED' },
    { label: 'Start', next: 'ONGOING' },
  ],
  REGISTRATION_CLOSED: [{ label: 'Start', next: 'ONGOING' }],
  ONGOING: [{ label: 'Complete', next: 'COMPLETED' }],
};

export function StatusActions({
  eventId,
  status,
  editPath,
}: {
  eventId: number;
  status: EventStatus;
  editPath?: string;
}) {
  const mutation = useChangeEventStatus(eventId);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const available = actions[status] ?? [];
  const canCancel = status !== 'COMPLETED' && status !== 'CANCELLED';

  return (
    <Stack spacing={1} alignItems="flex-start" sx={{ width: '100%' }}>
      <Stack
        data-testid="status-action-buttons"
        direction="row"
        spacing={1}
        flexWrap="nowrap"
        alignItems="center"
        sx={{ maxWidth: '100%', overflowX: 'auto' }}
      >
        {editPath && (
          <Button component={RouterLink} to={editPath} sx={{ flexShrink: 0 }}>
            Edit
          </Button>
        )}
        {available.map(({ label, next }) => (
          <Button
            key={next}
            variant="contained"
            disabled={mutation.isPending}
            sx={{ flexShrink: 0 }}
            onClick={() => mutation.mutate(next)}
          >
            {label}
          </Button>
        ))}
        {canCancel && (
          <Button
            color="error"
            disabled={mutation.isPending}
            sx={{ flexShrink: 0 }}
            onClick={() => setCancelDialogOpen(true)}
          >
            Cancel
          </Button>
        )}
      </Stack>
      {mutation.isError && <ErrorAlert error={mutation.error} />}
      {mutation.isPending && <Alert severity="info">Updating event status…</Alert>}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)}>
        <DialogTitle>Cancel event?</DialogTitle>
        <DialogContent><DialogContentText>Are you sure you want to cancel this event?</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>Keep event</Button>
          <Button
            color="error"
            onClick={() => {
              setCancelDialogOpen(false);
              mutation.mutate('CANCELLED');
            }}
          >
            Cancel event
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
