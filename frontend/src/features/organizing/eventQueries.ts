import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { changeEventStatus, createEvent, getEvent, updateEvent } from '../../api/events';
import type { EventRequest, EventStatus, UpdateEventRequest } from '../../api/types';

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

export function useChangeEventStatus(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (status: EventStatus) => changeEventStatus(id, status),
    onSuccess: (event) => {
      queryClient.setQueryData(eventKeys.detail(event.id), event);
      void queryClient.invalidateQueries({ queryKey: ['organizations'] });
    },
  });
}
