import { MenuItem, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material';
import AssignmentOutlinedIcon from '@mui/icons-material/AssignmentOutlined';
import type { Event } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { EmptyState } from '../../components/EmptyState';
import { formatDateTime } from '../events/eventFormat';
import { useCreateEventTask, useEventTasks, useTaskAssignees, useUpdateEventTask } from './queries';
import { NewTaskForm } from './NewTaskForm';

const statuses = ['TODO', 'IN_PROGRESS', 'DONE'] as const;
const statusLabels = { TODO: 'To do', IN_PROGRESS: 'In progress', DONE: 'Done' };

export function TasksPanel({ event }: { event: Pick<Event, 'id' | 'organizationId'> }) {
  const tasks = useEventTasks(event.id);
  const members = useTaskAssignees(event.organizationId);
  const createTask = useCreateEventTask(event.id);
  const updateTask = useUpdateEventTask(event.id);

  return (
    <Stack spacing={3}>
      <Typography component="h2" variant="h6">Tasks</Typography>
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack spacing={2}>
          <Typography component="h3" variant="subtitle1">New task</Typography>
          {members.isPending && <LoadingState label="Loading organization members…" />}
          {members.isError && <ErrorAlert error={members.error} onRetry={() => void members.refetch()} />}
          <NewTaskForm
            members={members.data ?? []}
            disabled={members.isPending || members.isError || createTask.isPending}
            onSubmit={(body) => createTask.mutateAsync(body)}
          />
        </Stack>
      </Paper>
      {createTask.isError && <ErrorAlert error={createTask.error} />}
      {tasks.isPending && <LoadingState label="Loading tasks…" />}
      {tasks.isError && <ErrorAlert error={tasks.error} onRetry={() => void tasks.refetch()} />}
      {tasks.data?.length === 0 && (
        <EmptyState message="No tasks yet" icon={<AssignmentOutlinedIcon />} compact />
      )}
      {tasks.data && tasks.data.length > 0 && (
        <Table aria-label="Event tasks">
          <TableHead><TableRow><TableCell>Title</TableCell><TableCell>Assignee</TableCell><TableCell>Due date</TableCell><TableCell>Status</TableCell></TableRow></TableHead>
          <TableBody>
            {tasks.data.map((task) => (
              <TableRow key={task.id}>
                <TableCell>{task.title}</TableCell>
                <TableCell>
                  <TextField select size="small" label={`Assignee for ${task.title}`} value={task.assignee?.membershipId ?? ''} disabled={updateTask.isPending || members.isPending || members.isError} onChange={(e) => updateTask.mutate({ taskId: task.id, body: e.target.value ? { assigneeMembershipId: Number(e.target.value) } : { unassign: true } })}>
                    <MenuItem value="">Unassigned</MenuItem>
                    {(members.data ?? []).map((member) => <MenuItem key={member.membershipId} value={member.membershipId}>{member.fullName}</MenuItem>)}
                  </TextField>
                </TableCell>
                <TableCell>{task.dueAt ? formatDateTime(task.dueAt) : '—'}</TableCell>
                <TableCell>
                  <TextField select size="small" label={`Status for ${task.title}`} value={task.status} disabled={updateTask.isPending} onChange={(e) => updateTask.mutate({ taskId: task.id, body: { status: e.target.value as (typeof statuses)[number] } })}>
                    {statuses.map((status) => <MenuItem key={status} value={status}>{statusLabels[status]}</MenuItem>)}
                  </TextField>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {updateTask.isError && <ErrorAlert error={updateTask.error} />}
    </Stack>
  );
}
