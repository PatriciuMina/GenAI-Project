// TanStack Query hooks for registrations (W4). Pages use these hooks, never the API functions directly.
import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query';
import {
  cancelMyRegistration,
  getMyRegistrations,
  registerForEvent,
} from '../../api/registrations';
import { eventKeys } from '../organizing/eventQueries';

export const registrationKeys = {
  mine: ['registrations', 'me'] as const,
};

/** The logged-in user's registrations (CONFIRMED and CANCELLED), newest first. */
export function useMyRegistrations() {
  return useQuery({ queryKey: registrationKeys.mine, queryFn: getMyRegistrations });
}

/**
 * After registering or cancelling, reload the event (free places) and my registrations. Also after a
 * failed attempt: an EVENT_FULL or REGISTRATION_CLOSED answer means the page shows outdated data.
 * Returning the promise keeps the mutation pending until the new data is there (no flicker).
 */
function reloadEventAndMyRegistrations(queryClient: QueryClient, eventId: number) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: eventKeys.detail(eventId), exact: true }),
    queryClient.invalidateQueries({ queryKey: registrationKeys.mine }),
  ]);
}

export function useRegisterForEvent(eventId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => registerForEvent(eventId),
    onSettled: () => reloadEventAndMyRegistrations(queryClient, eventId),
  });
}

export function useCancelRegistration(eventId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => cancelMyRegistration(eventId),
    onSettled: () => reloadEventAndMyRegistrations(queryClient, eventId),
  });
}
