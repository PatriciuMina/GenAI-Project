import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { getAnnouncements, postAnnouncement } from '../../api/announcements';
import { getOrganizationEvents } from '../../api/events';
import type { CreateAnnouncementRequest, Membership } from '../../api/types';

export const organizingKeys = {
  organizationEvents: (orgId: number) => ['organizations', orgId, 'events'] as const,
  announcements: (eventId: number) => ['events', eventId, 'announcements'] as const,
};

export function useEventAnnouncements(eventId: number) {
  return useQuery({
    queryKey: organizingKeys.announcements(eventId),
    queryFn: () => getAnnouncements(eventId),
    enabled: Number.isInteger(eventId) && eventId > 0,
  });
}

export function usePostAnnouncement(eventId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateAnnouncementRequest) => postAnnouncement(eventId, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: organizingKeys.announcements(eventId) }),
  });
}

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
