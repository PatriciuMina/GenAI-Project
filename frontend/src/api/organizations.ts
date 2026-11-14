import { apiClient } from './client';
import type { CreateOrganizationRequest, Member, Organization } from './types';

export async function getOrganizations(): Promise<Organization[]> {
  const { data } = await apiClient.get<Organization[]>('/organizations');
  return data;
}

export async function getOrganization(id: number): Promise<Organization> {
  const { data } = await apiClient.get<Organization>(`/organizations/${id}`);
  return data;
}

export async function getMembers(orgId: number): Promise<Member[]> {
  const { data } = await apiClient.get<Member[]>(`/organizations/${orgId}/members`);
  return data;
}

/** ADMIN only. Creates the organization and makes `presidentEmail` its PRESIDENT. */
export async function createOrganization(body: CreateOrganizationRequest): Promise<Organization> {
  const { data } = await apiClient.post<Organization>('/organizations', body);
  return data;
}
