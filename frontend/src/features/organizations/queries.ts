// TanStack Query hooks for organizations. Pages use these hooks, never the API functions directly.
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createOrganization, getOrganization, getOrganizations } from '../../api/organizations';

/** Query keys: one place, so invalidation after a mutation is easy (queryClient.invalidateQueries). */
export const organizationKeys = {
  all: ['organizations'] as const,
  detail: (id: number) => ['organizations', id] as const,
};

export function useOrganizations() {
  return useQuery({ queryKey: organizationKeys.all, queryFn: getOrganizations });
}

export function useOrganization(id: number) {
  return useQuery({
    queryKey: organizationKeys.detail(id),
    queryFn: () => getOrganization(id),
    enabled: Number.isInteger(id) && id > 0,
  });
}

/** ADMIN: create an organization; the organizations list is reloaded afterwards. */
export function useCreateOrganization() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createOrganization,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: organizationKeys.all }),
  });
}
