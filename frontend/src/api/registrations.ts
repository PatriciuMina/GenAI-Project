import { apiClient } from './client';
import type { MyRegistration, Participant, Registration } from './types';

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

/** Registers the logged-in user (409 REGISTRATION_CLOSED / EVENT_FULL / ALREADY_REGISTERED). */
export async function registerForEvent(eventId: number): Promise<Registration> {
  const { data } = await apiClient.post<Registration>(`/events/${eventId}/registrations`);
  return data;
}

/** Cancels the logged-in user's registration (404 NOT_REGISTERED, 409 CANCEL_NOT_ALLOWED). */
export async function cancelMyRegistration(eventId: number): Promise<void> {
  await apiClient.delete(`/events/${eventId}/registrations/me`);
}

/** The logged-in user's registrations (CONFIRMED and CANCELLED), newest first. */
export async function getMyRegistrations(): Promise<MyRegistration[]> {
  const { data } = await apiClient.get<MyRegistration[]>('/users/me/registrations');
  return data;
}
