import type { Event } from '../../api/types';

// Client-side copies of the backend registration rules (RegistrationService), used only to decide
// which button to show. The backend still checks everything and answers 409 if the page is outdated.

type EventTimes = Pick<Event, 'status' | 'startAt' | 'registrationDeadline'>;

/** PUBLISHED, and the registration deadline (or the start, if there is no deadline) not passed. */
export function isRegistrationOpen(event: EventTimes, now = Date.now()): boolean {
  const closesAt = Date.parse(event.registrationDeadline ?? event.startAt);
  return event.status === 'PUBLISHED' && now <= closesAt;
}

/** PUBLISHED or REGISTRATION_CLOSED, and the event has not started yet. */
export function isCancellationAllowed(event: EventTimes, now = Date.now()): boolean {
  const active = event.status === 'PUBLISHED' || event.status === 'REGISTRATION_CLOSED';
  return active && now < Date.parse(event.startAt);
}
