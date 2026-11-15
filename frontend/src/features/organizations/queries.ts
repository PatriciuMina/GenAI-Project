// TanStack Query hooks for organizations. Pages use these hooks, never the API functions directly.
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createOrganization,
  addMember,
  getMembers,
  getOrganization,
  getOrganizations,
  removeMember,
} from '../../api/organizations';
import { getMyMemberships } from '../../api/users';

/** Query keys: one place, so invalidation after a mutation is easy (queryClient.invalidateQueries). */
export const organizationKeys = {
  all: ['organizations'] as const,
  detail: (id: number) => ['organizations', id] as const,
  members: (id: number) => ['organizations', id, 'members'] as const,
};

export const myMembershipsKey = ['me', 'memberships'] as const;

export function useMyMemberships() {
  return useQuery({ queryKey: myMembershipsKey, queryFn: getMyMemberships });
}

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

export function useMembers(orgId: number, enabled: boolean) {
  return useQuery({
    queryKey: organizationKeys.members(orgId),
    queryFn: () => getMembers(orgId),
    enabled: enabled && Number.isInteger(orgId) && orgId > 0,
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

export function useAddMember(orgId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: Parameters<typeof addMember>[1]) => addMember(orgId, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: organizationKeys.members(orgId) }),
  });
}

export function useRemoveMember(orgId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (membershipId: number) => removeMember(orgId, membershipId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: organizationKeys.members(orgId) }),
  });
}
