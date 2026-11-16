import { useState, type FormEvent } from 'react';
import { Button, Paper, Stack, TextField, Typography } from '@mui/material';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { formatDateTime } from '../events/eventFormat';
import { useEventAnnouncements, usePostAnnouncement } from './queries';

export function AnnouncementsPanel({ eventId }: { eventId: number }) {
  const announcements = useEventAnnouncements(eventId);
  const post = usePostAnnouncement(eventId);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const titleInvalid = submitted && !title.trim();
  const bodyInvalid = submitted && !body.trim();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitted(true);
    if (!title.trim() || !body.trim()) return;
    try {
      await post.mutateAsync({ title: title.trim(), body: body.trim() });
      setTitle('');
      setBody('');
      setSubmitted(false);
    } catch {
      // The mutation error is rendered below; preserve the form contents for correction/retry.
    }
  }

  return (
    <Paper component="section" variant="outlined" sx={{ p: 2 }}>
      <Stack spacing={2}>
        <Typography component="h2" variant="h6">Announcements</Typography>
        <Stack component="form" spacing={2} onSubmit={(event) => void handleSubmit(event)} noValidate>
          <TextField
            label="Title"
            name="title"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            required
            error={titleInvalid}
            helperText={titleInvalid ? 'Title is required' : ' '}
            inputProps={{ maxLength: 200 }}
          />
          <TextField
            label="Body"
            name="body"
            value={body}
            onChange={(event) => setBody(event.target.value)}
            required
            multiline
            minRows={3}
            error={bodyInvalid}
            helperText={bodyInvalid ? 'Body is required' : ' '}
          />
          {post.isError && <ErrorAlert error={post.error} />}
          <Button type="submit" variant="contained" disabled={post.isPending}>
            {post.isPending ? 'Posting…' : 'Post announcement'}
          </Button>
        </Stack>
        {announcements.isPending && <LoadingState label="Loading announcements…" />}
        {announcements.isError && (
          <ErrorAlert error={announcements.error} onRetry={() => void announcements.refetch()} />
        )}
        {announcements.isSuccess && announcements.data.length === 0 && (
          <Typography color="text.secondary">No announcements yet.</Typography>
        )}
        {announcements.isSuccess && announcements.data.length > 0 && (
          <Stack component="ul" spacing={2} sx={{ listStyle: 'none', m: 0, p: 0 }}>
            {announcements.data.map((announcement) => (
              <li key={announcement.id}>
                <Typography component="h3" variant="subtitle1">{announcement.title}</Typography>
                <Typography sx={{ whiteSpace: 'pre-line' }}>{announcement.body}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {announcement.authorName} · {formatDateTime(announcement.createdAt)}
                </Typography>
              </li>
            ))}
          </Stack>
        )}
      </Stack>
    </Paper>
  );
}
