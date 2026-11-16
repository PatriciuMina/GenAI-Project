import { useState } from 'react';
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Stack } from '@mui/material';
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

export function StatusActions({ eventId, status }: { eventId: number; status: EventStatus }) {
  const mutation = useChangeEventStatus(eventId);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const available = actions[status] ?? [];
  const canCancel = status !== 'COMPLETED' && status !== 'CANCELLED';

  return (
    <Stack spacing={1} alignItems="flex-start">
      <Stack direction="row" spacing={1} flexWrap="wrap">
        {available.map(({ label, next }) => (
          <Button key={next} variant="contained" disabled={mutation.isPending}
            onClick={() => mutation.mutate(next)}>{label}</Button>
        ))}
        {canCancel && <Button color="error" disabled={mutation.isPending}
          onClick={() => setCancelDialogOpen(true)}>Cancel</Button>}
      </Stack>
      {mutation.isError && <ErrorAlert error={mutation.error} />}
      {mutation.isPending && <Alert severity="info">Updating event status…</Alert>}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)}>
        <DialogTitle>Cancel event?</DialogTitle>
        <DialogContent><DialogContentText>Are you sure you want to cancel this event?</DialogContentText></DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>Keep event</Button>
          <Button color="error" onClick={() => { setCancelDialogOpen(false); mutation.mutate('CANCELLED'); }}>Cancel event</Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
