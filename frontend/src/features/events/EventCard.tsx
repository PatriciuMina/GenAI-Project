import { Card, CardActionArea, CardContent, Chip, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router';
import type { Event } from '../../api/types';
import { paths } from '../../routes/paths';
import { categoryLabel, formatDateTime, placesText } from './eventFormat';

export function EventCard({ event }: { event: Event }) {
  const availability =
    event.status === 'REGISTRATION_CLOSED' ? 'Registration closed' : placesText(event);

  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
      <CardActionArea
        component={RouterLink}
        to={paths.event(event.id)}
        aria-label={`${event.title}, ${event.organizationName}`}
        sx={{ height: '100%', alignItems: 'flex-start' }}
      >
        <CardContent sx={{ width: '100%' }}>
          <Stack direction="row" spacing={1} sx={{ mb: 1, flexWrap: 'wrap', rowGap: 1 }}>
            <Chip label={categoryLabel(event.category)} size="small" variant="outlined" />
            <Chip label={availability} size="small" color={availability === 'Full' ? 'warning' : 'default'} />
          </Stack>
          <Typography component="h2" variant="h6" gutterBottom>
            {event.title}
          </Typography>
          <Typography color="text.secondary" variant="body2" gutterBottom>
            {event.organizationName}
          </Typography>
          <Typography variant="body2" gutterBottom>
            {formatDateTime(event.startAt)}
          </Typography>
          <Typography color="text.secondary" variant="body2">
            {event.location}
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
