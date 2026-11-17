import { describe, expect, it } from 'vitest';
import { isCancellationAllowed, isRegistrationOpen } from './registrationRules';

const NOW = Date.parse('2026-12-01T12:00:00Z');
const START = '2026-12-10T16:00:00Z';

describe('isRegistrationOpen', () => {
  it('is open for a published event before the deadline', () => {
    expect(
      isRegistrationOpen(
        { status: 'PUBLISHED', startAt: START, registrationDeadline: '2026-12-09T12:00:00Z' },
        NOW,
      ),
    ).toBe(true);
  });

  it('is still open exactly at the deadline', () => {
    expect(
      isRegistrationOpen(
        { status: 'PUBLISHED', startAt: START, registrationDeadline: '2026-12-01T12:00:00Z' },
        NOW,
      ),
    ).toBe(true);
  });

  it('is closed after the deadline even if the event is published', () => {
    expect(
      isRegistrationOpen(
        { status: 'PUBLISHED', startAt: START, registrationDeadline: '2026-11-30T12:00:00Z' },
        NOW,
      ),
    ).toBe(false);
  });

  it('uses the start when there is no deadline', () => {
    const event = { status: 'PUBLISHED' as const, startAt: START, registrationDeadline: null };
    expect(isRegistrationOpen(event, NOW)).toBe(true);
    expect(isRegistrationOpen(event, Date.parse('2026-12-10T16:00:01Z'))).toBe(false);
  });

  it.each(['DRAFT', 'REGISTRATION_CLOSED', 'ONGOING', 'COMPLETED', 'CANCELLED'] as const)(
    'is closed for a %s event',
    (status) => {
      expect(isRegistrationOpen({ status, startAt: START, registrationDeadline: null }, NOW)).toBe(
        false,
      );
    },
  );
});

describe('isCancellationAllowed', () => {
  it.each(['PUBLISHED', 'REGISTRATION_CLOSED'] as const)(
    'is allowed for a %s event that has not started',
    (status) => {
      expect(
        isCancellationAllowed({ status, startAt: START, registrationDeadline: null }, NOW),
      ).toBe(true);
    },
  );

  it('is not allowed once the event has started', () => {
    expect(
      isCancellationAllowed(
        { status: 'PUBLISHED', startAt: START, registrationDeadline: null },
        Date.parse(START),
      ),
    ).toBe(false);
  });

  it.each(['ONGOING', 'COMPLETED', 'CANCELLED'] as const)(
    'is not allowed for a %s event',
    (status) => {
      expect(
        isCancellationAllowed({ status, startAt: START, registrationDeadline: null }, NOW),
      ).toBe(false);
    },
  );
});
