import { Paper, Stack, Typography } from '@mui/material';
import { useCreateOrganization } from '../organizations/queries';
import { CreateOrganizationForm } from './CreateOrganizationForm';

// Admin tools (W1). Only ADMIN users see this page: the route is guarded with
// <ProtectedRoute roles={['ADMIN']}> in routes/router.tsx, and the backend checks hasRole('ADMIN').
export function AdminPage() {
  const createOrganization = useCreateOrganization();

  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h4">
        Admin
      </Typography>
      <Paper variant="outlined" sx={{ p: { xs: 2, sm: 4 }, maxWidth: 640 }}>
        <Typography component="h2" variant="h6" gutterBottom>
          Create an organization
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          The president must already have an account. They can then add members and organizers.
        </Typography>
        <CreateOrganizationForm onSubmit={createOrganization.mutateAsync} />
      </Paper>
    </Stack>
  );
}
