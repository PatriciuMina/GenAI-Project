import { Card, CardActionArea, CardContent, Grid, Paper, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { paths } from '../../routes/paths';
import { useOrganizations } from './queries';

export function OrganizationsListPage() {
  const { data: organizations, isPending, isError, error, refetch } = useOrganizations();

  return (
    <>
      <Typography component="h1" variant="h4" sx={{ mb: 3 }}>
        Organizations
      </Typography>

      {isPending && <LoadingState label="Loading organizations…" />}
      {isError && <ErrorAlert error={error} onRetry={() => void refetch()} />}

      {organizations && organizations.length === 0 && (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">No organizations yet.</Typography>
        </Paper>
      )}

      {organizations && organizations.length > 0 && (
        <Grid container spacing={2}>
          {organizations.map((org) => (
            <Grid key={org.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <Card variant="outlined" sx={{ height: '100%' }}>
                <CardActionArea
                  component={RouterLink}
                  to={paths.organization(org.id)}
                  sx={{ height: '100%', alignItems: 'flex-start' }}
                >
                  <CardContent>
                    <Typography variant="h6" component="h2" gutterBottom>
                      {org.name}
                    </Typography>
                    <Typography
                      variant="body2"
                      color="text.secondary"
                      sx={{
                        display: '-webkit-box',
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                      }}
                    >
                      {org.description || 'No description.'}
                    </Typography>
                  </CardContent>
                </CardActionArea>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </>
  );
}
