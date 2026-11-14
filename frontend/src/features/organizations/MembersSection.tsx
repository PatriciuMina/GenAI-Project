import { useState, type FormEvent } from 'react';
import { Alert, Button, Chip, MenuItem, Paper, Stack, TextField, Typography } from '@mui/material';
import { parseApiError } from '../../api/errors';
import type { OrganizationRole } from '../../api/types';
import { ErrorAlert } from '../../components/ErrorAlert';
import { LoadingState } from '../../components/LoadingState';
import { useAddMember, useMembers, useRemoveMember } from './queries';

const dateFormat = new Intl.DateTimeFormat('en-GB', { dateStyle: 'long' });

interface MembersSectionProps {
  orgId: number;
  enabled: boolean;
  canManage: boolean;
}

export function MembersSection({ orgId, enabled, canManage }: MembersSectionProps) {
  const { data: members, isPending, isError, error, refetch } = useMembers(orgId, enabled);
  const addMember = useAddMember(orgId);
  const removeMember = useRemoveMember(orgId);
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<Extract<OrganizationRole, 'MEMBER' | 'ORGANIZER'>>('MEMBER');

  const handleAdd = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    addMember.reset();
    try {
      await addMember.mutateAsync({ email: email.trim(), role });
      setEmail('');
      setRole('MEMBER');
    } catch {
      // The mutation error is rendered below using the shared API error parser.
    }
  };

  if (!enabled) return null;

  return (
    <Paper component="section" variant="outlined" sx={{ p: { xs: 2, sm: 4 } }}>
      <Typography component="h2" variant="h5" gutterBottom>
        Members
      </Typography>
      {canManage && (
        <Stack
          component="form"
          aria-label="Add member form"
          onSubmit={handleAdd}
          spacing={2}
          sx={{ mb: 3 }}
        >
          <Typography component="h3" variant="h6">
            Add member
          </Typography>
          {addMember.error && (
            <Alert severity="error">{parseApiError(addMember.error).message}</Alert>
          )}
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <TextField
              label="Email"
              name="email"
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              fullWidth
            />
            <TextField
              label="Role"
              name="role"
              select
              value={role}
              onChange={(event) => setRole(event.target.value as typeof role)}
              sx={{ minWidth: { sm: 180 } }}
            >
              <MenuItem value="MEMBER">Member</MenuItem>
              <MenuItem value="ORGANIZER">Organizer</MenuItem>
            </TextField>
            <Button type="submit" variant="contained" disabled={addMember.isPending}>
              {addMember.isPending ? 'Adding…' : 'Add member'}
            </Button>
          </Stack>
        </Stack>
      )}
      {canManage && removeMember.error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {parseApiError(removeMember.error).message}
        </Alert>
      )}
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
              {canManage && member.role !== 'PRESIDENT' && (
                <Button
                  color="error"
                  disabled={removeMember.isPending}
                  onClick={() => {
                    if (window.confirm(`Remove ${member.fullName} from this organization?`)) {
                      removeMember.mutate(member.membershipId);
                    }
                  }}
                >
                  Remove
                </Button>
              )}
            </Stack>
          ))}
        </Stack>
      )}
    </Paper>
  );
}
