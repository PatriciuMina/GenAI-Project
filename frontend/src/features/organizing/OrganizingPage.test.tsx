import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, useLocation } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Event, Membership } from '../../api/types';
import * as eventsApi from '../../api/events';
import * as usersApi from '../../api/users';
import { withQueryClient } from '../../test/utils';
import { OrganizingPage } from './OrganizingPage';

vi.mock('../../api/events');
vi.mock('../../api/users');

const organizerMembership: Membership = {
  membershipId: 1,
  organizationId: 7,
  organizationName: 'CS Society',
  role: 'ORGANIZER',
  joinedAt: '2026-01-01T00:00:00Z',
};

const event: Event = {
  id: 14,
  organizationId: 7,
  organizationName: 'CS Society',
  title: 'Spring Hackathon',
  description: null,
  location: 'Room 2',
  category: 'WORKSHOP',
  startAt: '2030-01-02T10:00:00Z',
  endAt: '2030-01-02T12:00:00Z',
  capacity: 25,
  registrationDeadline: null,
  status: 'DRAFT',
  confirmedCount: 3,
  freePlaces: 22,
  version: 1,
};

function renderPage() {
  function CurrentPath() {
    return <span>{useLocation().pathname}</span>;
  }

  render(
    withQueryClient(
      <MemoryRouter>
        <CurrentPath />
        <OrganizingPage />
      </MemoryRouter>,
    ),
  );
}

describe('OrganizingPage', () => {
  beforeEach(() => vi.resetAllMocks());

  it('shows the not-an-organizer message for users without organizer roles', async () => {
    vi.mocked(usersApi.getMyMemberships).mockResolvedValue([
      { ...organizerMembership, role: 'MEMBER' },
    ]);
    renderPage();

    expect(await screen.findByText('You are not an organizer in any organization')).toBeInTheDocument();
    expect(eventsApi.getOrganizationEvents).not.toHaveBeenCalled();
  });

  it('shows loading while memberships are being fetched', () => {
    vi.mocked(usersApi.getMyMemberships).mockReturnValue(new Promise(() => {}));
    renderPage();

    expect(screen.getByRole('status')).toHaveTextContent('Loading your organizations…');
  });

  it('shows an empty state when the organizer has no events', async () => {
    vi.mocked(usersApi.getMyMemberships).mockResolvedValue([organizerMembership]);
    vi.mocked(eventsApi.getOrganizationEvents).mockResolvedValue([]);
    renderPage();

    expect(await screen.findByText('No events in your organizations yet.')).toBeInTheDocument();
  });

  it('lists draft events and navigates the row to the organizer event path', async () => {
    vi.mocked(usersApi.getMyMemberships).mockResolvedValue([organizerMembership]);
    vi.mocked(eventsApi.getOrganizationEvents).mockResolvedValue([event]);
    renderPage();

    const row = await screen.findByRole('link', { name: /Spring Hackathon/ });
    expect(within(row).getByText('Draft')).toBeInTheDocument();
    expect(within(row).getByText('3 / 25')).toBeInTheDocument();
    expect(within(row).getByText(/2030/)).toBeInTheDocument();
    await userEvent.click(row);
    expect(screen.getByText('/organizing/events/14')).toBeInTheDocument();
  });
});
