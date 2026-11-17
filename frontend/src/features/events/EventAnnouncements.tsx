import { Paper, Stack, Typography } from '@mui/material';
import CampaignOutlinedIcon from '@mui/icons-material/CampaignOutlined';
import { ErrorAlert } from '../../components/ErrorAlert';
import { EmptyState } from '../../components/EmptyState';
import { formatDateTime } from './eventFormat';
import { useAnnouncements } from './queries';

/**
 * The announcements of an event (W5), newest first. Only members of the organization and users who
 * registered for the event may read them: for anyone else the backend answers 403 and nothing is shown,
 * neither the section nor an error. Other errors are shown with a retry button.
 */
export function EventAnnouncements({ eventId }: { eventId: number }) {
  const { data, isError, error, refetch } = useAnnouncements(eventId);

  if (data === null) return null;
  if (isError) return <ErrorAlert error={error} onRetry={() => void refetch()} />;
  if (data === undefined) return null;

  return (
    <Paper component="section" variant="outlined" sx={{ p: { xs: 2, sm: 4 } }}>
      <Typography component="h2" variant="h5" gutterBottom>
        Announcements
      </Typography>
      {data.length === 0 ? (
        <EmptyState message="No announcements yet." icon={<CampaignOutlinedIcon />} compact plain />
      ) : (
        <Stack component="ul" spacing={3} sx={{ listStyle: 'none', m: 0, p: 0 }}>
          {data.map((announcement) => (
            <li key={announcement.id}>
              <Typography component="h3" variant="h6">
                {announcement.title}
              </Typography>
              <Typography sx={{ whiteSpace: 'pre-line', my: 1 }}>{announcement.body}</Typography>
              <Typography variant="body2" color="text.secondary">
                {announcement.authorName} · {formatDateTime(announcement.createdAt)}
              </Typography>
            </li>
          ))}
        </Stack>
      )}
    </Paper>
  );
}
