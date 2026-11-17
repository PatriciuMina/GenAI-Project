import type { MyRegistration } from '../../api/types';

export interface SplitRegistrations {
  upcoming: MyRegistration[];
  past: MyRegistration[];
}

// An event has ended once it is COMPLETED or CANCELLED (statuses change only through the
// organizer, see EventLifecycle), so a confirmed registration of any other event is upcoming.
export function isUpcoming(registration: MyRegistration): boolean {
  return (
    registration.status === 'CONFIRMED' &&
    registration.eventStatus !== 'COMPLETED' &&
    registration.eventStatus !== 'CANCELLED'
  );
}

export function splitRegistrations(registrations: MyRegistration[]): SplitRegistrations {
  const byStart = (a: MyRegistration, b: MyRegistration) =>
    Date.parse(a.eventStartAt) - Date.parse(b.eventStartAt);
  const upcoming = registrations.filter(isUpcoming).sort(byStart);
  const past = registrations.filter((r) => !isUpcoming(r)).sort((a, b) => byStart(b, a));
  return { upcoming, past };
}

export type RegistrationChip = 'Confirmed' | 'Cancelled' | 'Checked in' | 'Event cancelled';

export function registrationChip(registration: MyRegistration): RegistrationChip {
  if (registration.eventStatus === 'CANCELLED') return 'Event cancelled';
  if (registration.status === 'CANCELLED') return 'Cancelled';
  if (registration.checkedInAt !== null) return 'Checked in';
  return 'Confirmed';
}
