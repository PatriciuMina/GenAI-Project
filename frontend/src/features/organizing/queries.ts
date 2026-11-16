import { useQueries } from '@tanstack/react-query';
import { getOrganizationEvents } from '../../api/events';
import type { Membership } from '../../api/types';

export const organizingKeys = {
  organizationEvents: (orgId: number) => ['organizations', orgId, 'events'] as const,
};

export function useOrganizationEvents(memberships: Membership[]) {
  const organizerMemberships = memberships.filter(
    ({ role }) => role === 'ORGANIZER' || role === 'PRESIDENT',
  );

  return useQueries({
    queries: organizerMemberships.map(({ organizationId }) => ({
      queryKey: organizingKeys.organizationEvents(organizationId),
      queryFn: () => getOrganizationEvents(organizationId),
    })),
  });
}
