import type { Event, EventCategory, EventStatus } from '../../api/types';

// Display helpers for events, shared by the event pages. Dates are shown in the user's local time zone.

const dateTimeFormat = new Intl.DateTimeFormat('en-GB', { dateStyle: 'long', timeStyle: 'short' });

export function formatDateTime(iso: string): string {
  return dateTimeFormat.format(new Date(iso));
}

/** What to show for the registration deadline; `null` means registration stays open until the start. */
export function registrationDeadlineText(deadline: string | null): string {
  return deadline === null ? 'Open until the event starts' : `Until ${formatDateTime(deadline)}`;
}

/** "28 of 40 places left", or "Full" when no place is left. */
export function placesText(event: Pick<Event, 'freePlaces' | 'capacity'>): string {
  return event.freePlaces <= 0 ? 'Full' : `${event.freePlaces} of ${event.capacity} places left`;
}

const statusLabels: Record<EventStatus, string> = {
  DRAFT: 'Draft',
  PUBLISHED: 'Published',
  REGISTRATION_CLOSED: 'Registration closed',
  ONGOING: 'Ongoing',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

export function statusLabel(status: EventStatus): string {
  return statusLabels[status];
}

/** "WORKSHOP" -> "Workshop". */
export function categoryLabel(category: EventCategory): string {
  return category.charAt(0) + category.slice(1).toLowerCase();
}
