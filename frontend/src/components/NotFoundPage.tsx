import { Alert, Button, Stack } from '@mui/material';
import { Link as RouterLink } from 'react-router';
import { paths } from '../routes/paths';

export function NotFoundPage() {
  return (
    <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
      <Alert severity="warning">This page does not exist.</Alert>
      <Button component={RouterLink} to={paths.organizations}>
        Go to organizations
      </Button>
    </Stack>
  );
}
