import { useMemo, useState } from 'react';
import {
  Button,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import GroupOutlinedIcon from '@mui/icons-material/GroupOutlined';
import type { EventStatus, Participant } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { EmptyState } from '../../components/EmptyState';
import { useCheckInParticipant, useParticipants } from './registrationQueries';

const CHECK_IN_UNAVAILABLE = 'Check-in is available after registration closes or when the event is ongoing.';
const NO_PARTICIPANTS: Participant[] = [];

function formatCheckedInAt(value: string | null) {
  return value ? new Date(value).toLocaleString() : '—';
}

export function ParticipantsPanel({
  eventId,
  status,
  capacity,
  confirmedCount,
}: {
  eventId: number;
  status: EventStatus;
  capacity: number;
  confirmedCount: number;
}) {
  const [search, setSearch] = useState('');
  const participantsQuery = useParticipants(eventId);
  const checkIn = useCheckInParticipant(eventId);
  const participants = participantsQuery.data ?? NO_PARTICIPANTS;
  const canCheckIn = status === 'REGISTRATION_CLOSED' || status === 'ONGOING';
  const checkedInCount = participants.filter(
    (participant) => participant.status === 'CONFIRMED' && participant.checkedInAt !== null,
  ).length;
  const filteredParticipants = useMemo(() => {
    const normalizedSearch = search.trim().toLocaleLowerCase();
    if (!normalizedSearch) return participants;
    return participants.filter((participant) =>
      participant.fullName.toLocaleLowerCase().includes(normalizedSearch),
    );
  }, [participants, search]);

  if (participantsQuery.isPending) return <LoadingState label="Loading participants…" />;
  if (participantsQuery.isError)
    return <ErrorAlert error={participantsQuery.error} onRetry={() => void participantsQuery.refetch()} />;

  return (
    <Stack spacing={2}>
      <Typography aria-live="polite">
        {confirmedCount} / {capacity} confirmed, {checkedInCount} checked in
      </Typography>
      <TextField
        label="Search participants by name"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        size="small"
      />
      {checkIn.isError && <ErrorAlert error={checkIn.error} />}
      <TableContainer component={Paper} variant="outlined">
        <Table aria-label="Event participants">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Checked-in time</TableCell>
              <TableCell>Action</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredParticipants.map((participant) => {
              const eligible = participant.status === 'CONFIRMED' && participant.checkedInAt === null;
              const isCheckingIn = checkIn.isPending && checkIn.variables === participant.registrationId;
              return (
                <TableRow key={participant.registrationId}>
                  <TableCell>{participant.fullName}</TableCell>
                  <TableCell>{participant.email}</TableCell>
                  <TableCell>{participant.status}</TableCell>
                  <TableCell>{formatCheckedInAt(participant.checkedInAt)}</TableCell>
                  <TableCell>
                    {eligible && (
                      <Tooltip title={canCheckIn ? '' : CHECK_IN_UNAVAILABLE}>
                        <span>
                          <Button
                            size="small"
                            disabled={!canCheckIn || checkIn.isPending}
                            onClick={() => checkIn.mutate(participant.registrationId)}
                          >
                            {isCheckingIn ? 'Checking in…' : 'Check in'}
                          </Button>
                        </span>
                      </Tooltip>
                    )}
                  </TableCell>
                </TableRow>
              );
            })}
            {filteredParticipants.length === 0 && (
              <TableRow>
                <TableCell colSpan={5}>
                  <EmptyState
                    message={
                      participants.length === 0
                        ? 'No participants yet.'
                        : 'No participants match your search.'
                    }
                    icon={<GroupOutlinedIcon />}
                    compact
                    plain
                  />
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
}
