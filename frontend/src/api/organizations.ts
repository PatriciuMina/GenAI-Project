import { apiClient } from './client';
import type { CreateOrganizationRequest, Organization } from './types';

export async function getOrganizations(): Promise<Organization[]> {
  const { data } = await apiClient.get<Organization[]>('/organizations');
  return data;
}

export async function getOrganization(id: number): Promise<Organization> {
  const { data } = await apiClient.get<Organization>(`/organizations/${id}`);
  return data;
}

/** ADMIN only. Creates the organization and makes `presidentEmail` its PRESIDENT. */
export async function createOrganization(body: CreateOrganizationRequest): Promise<Organization> {
  const { data } = await apiClient.post<Organization>('/organizations', body);
  return data;
}
