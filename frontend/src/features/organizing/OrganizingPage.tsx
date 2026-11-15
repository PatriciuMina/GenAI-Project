import { Button, Stack } from '@mui/material';
import { Link as RouterLink } from 'react-router';
import { ComingSoon } from '../../components/ComingSoon';
import { paths } from '../../routes/paths';

// TODO(W5): event organization (organizer tasks, attendance check-in, announcements).
// Also gate the "Organizing" navigation item by organization role (see AppLayout NAV_ITEMS).
export function OrganizingPage() {
  return (
    <Stack spacing={2}>
      <Button
        component={RouterLink}
        to={paths.organizingNewEvent}
        variant="contained"
        sx={{ alignSelf: 'flex-start' }}
      >
        New event
      </Button>
      <ComingSoon
        title="Organizing"
        workflow="W5"
        description="Organizer tasks, attendance check-in and announcements."
      />
    </Stack>
  );
}
