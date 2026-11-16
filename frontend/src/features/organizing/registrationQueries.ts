import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { checkInParticipant, getParticipants } from '../../api/registrations';

export const participantKeys = {
  event: (eventId: number) => ['events', eventId, 'participants'] as const,
};

export function useParticipants(eventId: number) {
  return useQuery({
    queryKey: participantKeys.event(eventId),
    queryFn: () => getParticipants(eventId),
    enabled: Number.isInteger(eventId) && eventId > 0,
  });
}

export function useCheckInParticipant(eventId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (registrationId: number) => checkInParticipant(eventId, registrationId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: participantKeys.event(eventId) }),
  });
}
