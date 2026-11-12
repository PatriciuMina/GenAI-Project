import { apiClient } from './client';
import type { Membership, User } from './types';

export async function getCurrentUser(): Promise<User> {
  const { data } = await apiClient.get<User>('/users/me');
  return data;
}

export async function getMyMemberships(): Promise<Membership[]> {
  const { data } = await apiClient.get<Membership[]>('/users/me/memberships');
  return data;
}
