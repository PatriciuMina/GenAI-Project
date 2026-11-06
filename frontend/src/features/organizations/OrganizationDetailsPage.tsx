import { Alert, Box, Button, Link, Paper, Stack, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { Link as RouterLink, useParams } from 'react-router';
import { errorStatus } from '../../api/errors';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { paths } from '../../routes/paths';
import { useOrganization } from './queries';

const dateFormat = new Intl.DateTimeFormat('en-GB', { dateStyle: 'long' });

export function OrganizationDetailsPage() {
  const { id: idParam } = useParams();
  const id = Number(idParam);
  const validId = Number.isInteger(id) && id > 0;
  const { data: org, isPending, isError, error, refetch } = useOrganization(id);

  const back = (
    <Button component={RouterLink} to={paths.organizations} startIcon={<ArrowBackIcon />}>
      All organizations
    </Button>
  );

  if (!validId || (isError && errorStatus(error) === 404)) {
    return (
      <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
        <Alert severity="warning">Organization not found.</Alert>
        {back}
      </Stack>
    );
  }

  return (
    <Stack spacing={2}>
      <Box>{back}</Box>
      {isPending && <LoadingState label="Loading organization…" />}
      {isError && <ErrorAlert error={error} onRetry={() => void refetch()} />}
      {org && (
        <Paper variant="outlined" sx={{ p: { xs: 2, sm: 4 } }}>
          <Typography component="h1" variant="h4" gutterBottom>
            {org.name}
          </Typography>
          <Typography sx={{ whiteSpace: 'pre-line', mb: 3 }}>
            {org.description || 'No description.'}
          </Typography>
          <Stack spacing={1}>
            <Typography variant="body2">
              <strong>Contact:</strong>{' '}
              {org.contactEmail ? (
                <Link href={`mailto:${org.contactEmail}`}>{org.contactEmail}</Link>
              ) : (
                '—'
              )}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Created {dateFormat.format(new Date(org.createdAt))}
            </Typography>
          </Stack>
          {/* TODO(W1): members list, join/leave, edit for PRESIDENT. */}
        </Paper>
      )}
    </Stack>
  );
}
