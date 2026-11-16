import { apiClient } from './client';
import type { Event, EventRequest, EventStatus, PageResponse, UpdateEventRequest } from './types';

export interface EventSearchParams {
  q?: string;
  category?: Event['category'];
  organizationId?: number;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export async function searchEvents(params: EventSearchParams = {}): Promise<PageResponse<Event>> {
  const { data } = await apiClient.get<PageResponse<Event>>('/events', { params });
  return data;
}

export async function getOrganizationEvents(orgId: number): Promise<Event[]> {
  const { data } = await apiClient.get<Event[]>(`/organizations/${orgId}/events`);
  return data;
}

export async function createEvent(orgId: number, body: EventRequest): Promise<Event> {
  const { data } = await apiClient.post<Event>(`/organizations/${orgId}/events`, body);
  return data;
}

export async function updateEvent(id: number, body: UpdateEventRequest): Promise<Event> {
  const { data } = await apiClient.put<Event>(`/events/${id}`, body);
  return data;
}

export async function getEvent(id: number): Promise<Event> {
  const { data } = await apiClient.get<Event>(`/events/${id}`);
  return data;
}

export async function changeEventStatus(id: number, status: EventStatus): Promise<Event> {
  const { data } = await apiClient.post<Event>(`/events/${id}/status`, { status });
  return data;
}
