import { describe, expect, it } from 'vitest';
import {
  categoryLabel,
  placesText,
  registrationDeadlineText,
  statusLabel,
} from './eventFormat';

describe('registrationDeadlineText', () => {
  it('says registration is open until the start when there is no deadline', () => {
    expect(registrationDeadlineText(null)).toBe('Open until the event starts');
  });

  it('shows the deadline date when there is one', () => {
    // midday UTC is the same calendar day in every time zone from UTC-11 to UTC+11
    expect(registrationDeadlineText('2026-12-09T12:00:00Z')).toMatch(/^Until 9 December 2026/);
  });
});

describe('placesText', () => {
  it('shows the free places out of the capacity', () => {
    expect(placesText({ freePlaces: 28, capacity: 40 })).toBe('28 of 40 places left');
  });

  it('shows "Full" when no place is left', () => {
    expect(placesText({ freePlaces: 0, capacity: 3 })).toBe('Full');
  });
});

describe('labels', () => {
  it('makes readable status and category names', () => {
    expect(statusLabel('REGISTRATION_CLOSED')).toBe('Registration closed');
    expect(statusLabel('CANCELLED')).toBe('Cancelled');
    expect(categoryLabel('WORKSHOP')).toBe('Workshop');
    expect(categoryLabel('VOLUNTEERING')).toBe('Volunteering');
  });
});
