// TanStack Query hooks for the event pages. Pages use these hooks, never the API functions directly.
import { useQuery } from '@tanstack/react-query';
import { getAnnouncements } from '../../api/announcements';
import { errorStatus } from '../../api/errors';

/** Under ['events', id], so invalidating an event also refreshes its announcements. */
export const announcementKeys = {
  list: (eventId: number) => ['events', eventId, 'announcements'] as const,
};

/**
 * The announcements of an event, or `null` when the user may not read them (the backend answers 403).
 * Not being allowed is not an error: the page just shows no announcements section.
 */
export function useAnnouncements(eventId: number) {
  return useQuery({
    queryKey: announcementKeys.list(eventId),
    queryFn: async () => {
      try {
        return await getAnnouncements(eventId);
      } catch (error) {
        if (errorStatus(error) === 403) return null;
        throw error;
      }
    },
    enabled: Number.isInteger(eventId) && eventId > 0,
  });
}
