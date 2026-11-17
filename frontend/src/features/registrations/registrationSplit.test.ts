import { describe, expect, it } from 'vitest';
import type { MyRegistration } from '../../api/types';
import { isUpcoming, registrationChip, splitRegistrations } from './registrationSplit';

const DAY = 86_400_000;
const inDays = (days: number) => new Date(Date.now() + days * DAY).toISOString();

function registration(overrides: Partial<MyRegistration> = {}): MyRegistration {
  return {
    id: 1,
    eventId: 1,
    eventTitle: 'Event',
    eventStartAt: inDays(5),
    eventLocation: 'FSEGA',
    eventStatus: 'PUBLISHED',
    status: 'CONFIRMED',
    registeredAt: inDays(-1),
    checkedInAt: null,
    ...overrides,
  };
}

describe('isUpcoming', () => {
  it.each(['PUBLISHED', 'REGISTRATION_CLOSED', 'ONGOING'] as const)(
    'a confirmed registration of a %s event is upcoming',
    (eventStatus) => {
      expect(isUpcoming(registration({ eventStatus }))).toBe(true);
    },
  );

  it.each(['COMPLETED', 'CANCELLED'] as const)(
    'a confirmed registration of a %s event is past',
    (eventStatus) => {
      expect(isUpcoming(registration({ eventStatus }))).toBe(false);
    },
  );

  it('a cancelled registration is past even if the event is still open', () => {
    expect(isUpcoming(registration({ status: 'CANCELLED' }))).toBe(false);
  });
});

describe('splitRegistrations', () => {
  it('sorts upcoming by start (soonest first) and past by start (latest first)', () => {
    const soon = registration({ id: 1, eventStartAt: inDays(2) });
    const later = registration({ id: 2, eventStartAt: inDays(9) });
    const oldDone = registration({ id: 3, eventStartAt: inDays(-20), eventStatus: 'COMPLETED' });
    const recentCancelled = registration({ id: 4, eventStartAt: inDays(3), status: 'CANCELLED' });

    const { upcoming, past } = splitRegistrations([oldDone, later, recentCancelled, soon]);

    expect(upcoming.map((r) => r.id)).toEqual([1, 2]);
    expect(past.map((r) => r.id)).toEqual([4, 3]);
  });

  it('returns two empty lists for no registrations', () => {
    expect(splitRegistrations([])).toEqual({ upcoming: [], past: [] });
  });
});

describe('registrationChip', () => {
  it('shows "Event cancelled" before the registration status', () => {
    expect(registrationChip(registration({ eventStatus: 'CANCELLED' }))).toBe('Event cancelled');
    expect(registrationChip(registration({ eventStatus: 'CANCELLED', status: 'CANCELLED' }))).toBe(
      'Event cancelled',
    );
  });

  it('distinguishes cancelled, checked in and confirmed', () => {
    expect(registrationChip(registration({ status: 'CANCELLED' }))).toBe('Cancelled');
    expect(registrationChip(registration({ checkedInAt: inDays(-1) }))).toBe('Checked in');
    expect(registrationChip(registration())).toBe('Confirmed');
  });
});
