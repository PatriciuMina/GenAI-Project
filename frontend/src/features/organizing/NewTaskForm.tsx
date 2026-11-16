import { useState, type FormEvent } from 'react';
import { Alert, Button, MenuItem, Stack, TextField } from '@mui/material';
import { parseApiError } from '../../api/errors';
import type { CreateTaskRequest, Member } from '../../api/types';

function toUtc(value: string): string | null {
  return value ? new Date(value).toISOString() : null;
}

export function NewTaskForm({
  members,
  disabled = false,
  onSubmit,
}: {
  members: Member[];
  disabled?: boolean;
  onSubmit: (body: CreateTaskRequest) => Promise<unknown>;
}) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [dueAt, setDueAt] = useState('');
  const [assignee, setAssignee] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!title.trim()) {
      setError('Title is required');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({
        title: title.trim(),
        description: description.trim() || null,
        dueAt: toUtc(dueAt),
        assigneeMembershipId: assignee ? Number(assignee) : null,
      });
      setTitle('');
      setDescription('');
      setDueAt('');
      setAssignee('');
    } catch (submissionError) {
      setError(parseApiError(submissionError).message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Stack component="form" aria-label="New task form" onSubmit={submit} spacing={2}>
      {error && <Alert severity="error">{error}</Alert>}
      <TextField label="Title" required value={title} onChange={(e) => setTitle(e.target.value)} />
      <TextField label="Description" multiline minRows={2} value={description} onChange={(e) => setDescription(e.target.value)} />
      <TextField label="Due date" type="datetime-local" InputLabelProps={{ shrink: true }} value={dueAt} onChange={(e) => setDueAt(e.target.value)} />
      <TextField select label="Assignee" value={assignee} onChange={(e) => setAssignee(e.target.value)}>
        <MenuItem value="">Unassigned</MenuItem>
        {members.map((member) => (
          <MenuItem key={member.membershipId} value={member.membershipId}>{member.fullName}</MenuItem>
        ))}
      </TextField>
      <Button type="submit" variant="contained" disabled={disabled || submitting}>
        {submitting ? 'Creating…' : 'Create task'}
      </Button>
    </Stack>
  );
}
