import { apiClient } from './client';
import type { Announcement } from './types';

/** Newest first. 403 when the user is neither a member of the organization nor registered for the event. */
export async function getAnnouncements(eventId: number): Promise<Announcement[]> {
  const { data } = await apiClient.get<Announcement[]>(`/events/${eventId}/announcements`);
  return data;
}
