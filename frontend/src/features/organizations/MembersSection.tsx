import { Chip, Paper, Stack, Typography } from '@mui/material';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { useMembers } from './queries';

const dateFormat = new Intl.DateTimeFormat('en-GB', { dateStyle: 'long' });

interface MembersSectionProps {
  orgId: number;
  enabled: boolean;
}

export function MembersSection({ orgId, enabled }: MembersSectionProps) {
  const { data: members, isPending, isError, error, refetch } = useMembers(orgId, enabled);

  if (!enabled) return null;

  return (
    <Paper component="section" variant="outlined" sx={{ p: { xs: 2, sm: 4 } }}>
      <Typography component="h2" variant="h5" gutterBottom>
        Members
      </Typography>
      {isPending && <LoadingState label="Loading members…" />}
      {isError && <ErrorAlert error={error} onRetry={() => void refetch()} />}
      {members?.length === 0 && <Typography>No members yet</Typography>}
      {members && members.length > 0 && (
        <Stack component="ul" spacing={1} sx={{ listStyle: 'none', m: 0, p: 0 }}>
          {members.map((member) => (
            <Stack
              component="li"
              key={member.membershipId}
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1}
              sx={{ alignItems: { sm: 'center' }, py: 1 }}
            >
              <Typography sx={{ flexGrow: 1 }}>{member.fullName}</Typography>
              <Chip label={member.role} size="small" />
              <Typography variant="body2" color="text.secondary">
                Joined {dateFormat.format(new Date(member.joinedAt))}
              </Typography>
            </Stack>
          ))}
        </Stack>
      )}
    </Paper>
  );
}
