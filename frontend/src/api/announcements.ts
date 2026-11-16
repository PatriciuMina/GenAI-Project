import { apiClient } from './client';
import type { Announcement, CreateAnnouncementRequest } from './types';

/** Newest first. 403 when the user is neither a member of the organization nor registered for the event. */
export async function getAnnouncements(eventId: number): Promise<Announcement[]> {
  const { data } = await apiClient.get<Announcement[]>(`/events/${eventId}/announcements`);
  return data;
}

/** Post an announcement to an event. */
export async function postAnnouncement(
  eventId: number,
  request: CreateAnnouncementRequest,
): Promise<Announcement> {
  const { data } = await apiClient.post<Announcement>(`/events/${eventId}/announcements`, request);
  return data;
}
