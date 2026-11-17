import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as registrationsApi from '../../api/registrations';
import type { MyRegistration } from '../../api/types';
import { apiError, withQueryClient } from '../../test/utils';
import { MyRegistrationsPage } from './MyRegistrationsPage';

vi.mock('../../api/registrations');

const DAY = 86_400_000;
const inDays = (days: number) => new Date(Date.now() + days * DAY).toISOString();

const workshop: MyRegistration = {
  id: 11,
  eventId: 1,
  eventTitle: 'Intro to Spring Boot Workshop',
  eventStartAt: inDays(7),
  eventLocation: 'FSEGA, room 101',
  eventStatus: 'PUBLISHED',
  status: 'CONFIRMED',
  registeredAt: inDays(-1),
  checkedInAt: null,
};

const boardGames: MyRegistration = {
  id: 12,
  eventId: 2,
  eventTitle: 'Board Games Evening',
  eventStartAt: inDays(4),
  eventLocation: 'Student Culture House',
  eventStatus: 'PUBLISHED',
  status: 'CANCELLED',
  registeredAt: inDays(-2),
  checkedInAt: null,
};

function renderPage() {
  const user = userEvent.setup({ delay: null });
  render(
    withQueryClient(
      <MemoryRouter>
        <MyRegistrationsPage />
      </MemoryRouter>,
    ),
  );
  return user;
}

describe('MyRegistrationsPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('puts a confirmed registration under Upcoming and a cancelled one under Past and cancelled', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([boardGames, workshop]);
    renderPage();

    const upcoming = await screen.findByRole('region', { name: 'Upcoming' });
    const past = screen.getByRole('region', { name: 'Past and cancelled' });

    expect(within(upcoming).getByRole('link', { name: workshop.eventTitle })).toHaveAttribute(
      'href',
      '/events/1',
    );
    expect(within(upcoming).getByText('Confirmed')).toBeInTheDocument();
    expect(within(past).getByRole('link', { name: boardGames.eventTitle })).toBeInTheDocument();
    expect(within(past).getByText('Cancelled')).toBeInTheDocument();
    expect(within(past).queryByRole('button', { name: 'Cancel' })).not.toBeInTheDocument();
  });

  it('cancels after confirmation and moves the row to Past and cancelled', async () => {
    vi.mocked(registrationsApi.getMyRegistrations)
      .mockResolvedValueOnce([workshop])
      .mockResolvedValue([{ ...workshop, status: 'CANCELLED' }]);
    vi.mocked(registrationsApi.cancelMyRegistration).mockResolvedValue();
    const user = renderPage();

    const upcoming = await screen.findByRole('region', { name: 'Upcoming' });
    await user.click(within(upcoming).getByRole('button', { name: 'Cancel' }));
    await user.click(screen.getByRole('button', { name: 'Yes, cancel registration' }));

    expect(registrationsApi.cancelMyRegistration).toHaveBeenCalledWith(1);
    const past = await screen.findByRole('region', { name: 'Past and cancelled' });
    await waitFor(() =>
      expect(within(past).getByRole('link', { name: workshop.eventTitle })).toBeInTheDocument(),
    );
    expect(screen.getByText('No upcoming events.')).toBeInTheDocument();
  });

  it('keeps the place when the dialog is closed with "Keep my place"', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([workshop]);
    const user = renderPage();

    await user.click(await screen.findByRole('button', { name: 'Cancel' }));
    await user.click(screen.getByRole('button', { name: 'Keep my place' }));

    expect(registrationsApi.cancelMyRegistration).not.toHaveBeenCalled();
  });

  it('shows the backend message when cancelling is rejected', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([workshop]);
    vi.mocked(registrationsApi.cancelMyRegistration).mockRejectedValue(
      apiError(409, 'CANCEL_NOT_ALLOWED', 'The event has already started'),
    );
    const user = renderPage();

    await user.click(await screen.findByRole('button', { name: 'Cancel' }));
    await user.click(screen.getByRole('button', { name: 'Yes, cancel registration' }));

    expect(
      await screen.findByText('This registration can no longer be cancelled.'),
    ).toBeInTheDocument();
  });

  it('does not offer Cancel for a checked-in registration', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([
      {
        ...workshop,
        eventStatus: 'ONGOING',
        eventStartAt: inDays(-0.01),
        checkedInAt: inDays(-0.005),
      },
    ]);
    renderPage();

    expect(await screen.findByText('Checked in')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Cancel' })).not.toBeInTheDocument();
  });

  it('shows an empty state with a link to the events page', async () => {
    vi.mocked(registrationsApi.getMyRegistrations).mockResolvedValue([]);
    renderPage();

    expect(
      await screen.findByText('You have not registered for any event yet.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Browse events' })).toHaveAttribute('href', '/events');
  });
});
