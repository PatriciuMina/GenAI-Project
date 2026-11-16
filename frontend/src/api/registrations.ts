import { apiClient } from './client';
import type { Participant } from './types';

export async function getParticipants(eventId: number): Promise<Participant[]> {
  const { data } = await apiClient.get<Participant[]>(`/events/${eventId}/registrations`);
  return data;
}

export async function checkInParticipant(
  eventId: number,
  registrationId: number,
): Promise<Participant> {
  const { data } = await apiClient.post<Participant>(
    `/events/${eventId}/registrations/${registrationId}/check-in`,
  );
  return data;
}
