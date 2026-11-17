import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as registrationsApi from '../../api/registrations';
import type { Event, MyRegistration } from '../../api/types';
import { apiError, withQueryClient } from '../../test/utils';
import { RegistrationButton } from './RegistrationButton';

vi.mock('../../api/registrations');

// Dates relative to now, so the tests do not start failing when a fixed date has passed.
const DAY = 86_400_000;
const inDays = (days: number) => new Date(Date.now() + days * DAY).toISOString();

const openEvent: Event = {
  id: 3,
  organizationId: 2,
  organizationName: 'UBB Theatre Club',
  title: 'Improv Night',
  description: null,
  location: 'Student Culture House',
  category: 'CULTURAL',
  startAt: inDays(10),
  endAt: inDays(10.1),
  capacity: 50,
  registrationDeadline: null,
  status: 'PUBLISHED',
  confirmedCount: 2,
  freePlaces: 48,
  version: 0,
};

function myRegistration(overrides: Partial<MyRegistration> = {}): MyRegistration {
  return {
    id: 31,
    eventId: 3,
    eventTitle: 'Improv Night',
    eventStartAt: openEvent.startAt,
    eventLocation: 'Student Culture House',
    eventStatus: 'PUBLISHED',
    status: 'CONFIRMED',
    registeredAt: inDays(-1),
    checkedInAt: null,
    ...overrides,
  };
}

function renderButton(event: Event) {
  const user = userEvent.setup({ delay: null });
  render(withQueryClient(<RegistrationButton event={event} />));
  return user;
}

describe('RegistrationButton', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('offers "Register" when registration is open and places are left, then shows the new state', async () => {
    vi.mocked(registrationsApi.getMyRegistrations)
      .mockResolvedValueOnce([])
      .mockResolvedValue([myRegistration()]);
    vi.mocked(registrationsApi.registerForEvent).mockResolvedValue({
      id: 31,
      eventId: 3,
      eventTitle: 'Improv Night',
      status: 'CONFIRMED',
      registeredAt: inDays(0),
      checkedInAt: null,
    });
    const user = renderButton(openEvent);

    await user.click(await screen.findByRole('button', { name: 'Register' }));

    expect(registrationsApi.registerForEvent).toHaveBeenCalledWith(3);
    expect(await screen.findByText('You are registered for this event.')).toBeInTheDocument();
    // my registrations were reloaded after registering
    expect(registrationsApi.getMyRegistrations).toHaveBeenCalledTimes(2);
  });

  it('ignores a cancelled earlier registration and offers "Register" again', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([
      myRegistration({ status: 'CANCELLED' }),
    ]);

    renderButton(openEvent);

    expect(await screen.findByRole('button', { name: 'Register' })).toBeEnabled();
  });

  it('shows "You are registered" for a registered user, also when the event is full', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([myRegistration()]);

    renderButton({ ...openEvent, freePlaces: 0, confirmedCount: 50 });

    expect(await screen.findByText('You are registered for this event.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel registration' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Register' })).not.toBeInTheDocument();
  });

  it('cancels the registration after confirmation', async () => {
    vi.mocked(registrationsApi.getMyRegistrations)
      .mockResolvedValueOnce([myRegistration()])
      .mockResolvedValue([myRegistration({ status: 'CANCELLED' })]);
    vi.mocked(registrationsApi.cancelMyRegistration).mockResolvedValue(undefined);
    const user = renderButton(openEvent);

    await user.click(await screen.findByRole('button', { name: 'Cancel registration' }));
    const dialog = screen.getByRole('dialog', { name: 'Cancel your registration?' });
    await user.click(within(dialog).getByRole('button', { name: 'Yes, cancel registration' }));

    expect(registrationsApi.cancelMyRegistration).toHaveBeenCalledWith(3);
    expect(await screen.findByRole('button', { name: 'Register' })).toBeInTheDocument();
  });

  it('keeps the registration when the user does not confirm', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([myRegistration()]);
    const user = renderButton(openEvent);

    await user.click(await screen.findByRole('button', { name: 'Cancel registration' }));
    await user.click(screen.getByRole('button', { name: 'Keep my place' }));

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    expect(registrationsApi.cancelMyRegistration).not.toHaveBeenCalled();
    expect(screen.getByText('You are registered for this event.')).toBeInTheDocument();
  });

  it('does not offer cancellation once the event has started or the user is checked in', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([
      myRegistration({ checkedInAt: inDays(0) }),
    ]);

    renderButton({ ...openEvent, status: 'ONGOING', startAt: inDays(-0.1) });

    expect(await screen.findByText('You are registered and checked in.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Cancel registration' })).not.toBeInTheDocument();
  });

  it('shows "full" and no button when no place is left', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([]);

    renderButton({ ...openEvent, freePlaces: 0, confirmedCount: 50 });

    expect(
      await screen.findByText('This event is full: all places are taken.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it.each([
    ['the status is REGISTRATION_CLOSED', { status: 'REGISTRATION_CLOSED' as const }],
    ['the deadline has passed', { registrationDeadline: inDays(-1) }],
    ['the event was cancelled', { status: 'CANCELLED' as const }],
  ])('shows "registration closed" when %s', async (_case, overrides) => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([]);

    renderButton({ ...openEvent, ...overrides });

    expect(await screen.findByText('Registration is closed for this event.')).toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it.each([
    ['EVENT_FULL', 'All places for this event are taken'],
    ['REGISTRATION_CLOSED', 'Registration for this event is closed'],
    ['ALREADY_REGISTERED', 'You are already registered for this event'],
  ])('shows the readable message of %s', async (code, message) => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([]);
    vi.mocked(registrationsApi.registerForEvent).mockRejectedValue(apiError(409, code, message));
    const user = renderButton(openEvent);

    await user.click(await screen.findByRole('button', { name: 'Register' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(message);
  });

  it('shows an error with a retry button when the registrations cannot be loaded', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockRejectedValue(
      apiError(500, 'INTERNAL_ERROR', 'Something went wrong'),
    );

    renderButton(openEvent);

    expect(await screen.findByText('Something went wrong')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });
});
