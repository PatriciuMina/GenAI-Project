import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { getOrganizationEvents } from '../../api/events';
import { getMembers } from '../../api/organizations';
import { createEventTask, getEventTasks, updateEventTask } from '../../api/tasks';
import type { CreateTaskRequest, Membership, UpdateTaskRequest } from '../../api/types';

export const organizingKeys = {
  organizationEvents: (orgId: number) => ['organizations', orgId, 'events'] as const,
  eventTasks: (eventId: number) => ['events', eventId, 'tasks'] as const,
  organizationMembers: (orgId: number) => ['organizations', orgId, 'members'] as const,
};

export function useEventTasks(eventId: number) {
  return useQuery({
    queryKey: organizingKeys.eventTasks(eventId),
    queryFn: () => getEventTasks(eventId),
    enabled: Number.isInteger(eventId) && eventId > 0,
  });
}

export function useTaskAssignees(orgId: number) {
  return useQuery({
    queryKey: organizingKeys.organizationMembers(orgId),
    queryFn: () => getMembers(orgId),
    enabled: Number.isInteger(orgId) && orgId > 0,
  });
}

export function useCreateEventTask(eventId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateTaskRequest) => createEventTask(eventId, body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: organizingKeys.eventTasks(eventId) }),
  });
}

export function useUpdateEventTask(eventId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, body }: { taskId: number; body: UpdateTaskRequest }) =>
      updateEventTask(eventId, taskId, body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: organizingKeys.eventTasks(eventId) }),
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
