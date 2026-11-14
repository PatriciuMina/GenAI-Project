import { apiClient } from './client';
import type { Event, EventRequest, UpdateEventRequest } from './types';

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
