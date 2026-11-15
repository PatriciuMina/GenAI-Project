import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createEvent, getEvent, updateEvent } from '../../api/events';
import type { EventRequest, UpdateEventRequest } from '../../api/types';

export const eventKeys = {
  detail: (id: number) => ['events', id] as const,
};

export function useEvent(id: number) {
  return useQuery({
    queryKey: eventKeys.detail(id),
    queryFn: () => getEvent(id),
    enabled: Number.isInteger(id) && id > 0,
  });
}

export function useCreateEvent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ orgId, body }: { orgId: number; body: EventRequest }) =>
      createEvent(orgId, body),
    onSuccess: (event) => queryClient.setQueryData(eventKeys.detail(event.id), event),
  });
}

export function useUpdateEvent(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: UpdateEventRequest) => updateEvent(id, body),
    onSuccess: (event) => queryClient.setQueryData(eventKeys.detail(event.id), event),
  });
}
