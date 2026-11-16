import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as eventsApi from '../../api/events';
import * as organizationsApi from '../../api/organizations';
import type { Event, Organization, PageResponse } from '../../api/types';
import { withQueryClient } from '../../test/utils';
import { EventsPage } from './EventsPage';

vi.mock('../../api/events', () => ({ searchEvents: vi.fn() }));
vi.mock('../../api/organizations', () => ({ getOrganizations: vi.fn() }));

const event: Event = {
  id: 7,
  organizationId: 3,
  organizationName: 'UBB Computer Science Society',
  title: 'Spring Workshop',
  description: null,
  location: 'Room 2/I',
  category: 'WORKSHOP',
  startAt: '2026-12-01T10:00:00Z',
  endAt: '2026-12-01T12:00:00Z',
  capacity: 30,
  registrationDeadline: null,
  status: 'PUBLISHED',
  confirmedCount: 12,
  freePlaces: 18,
  version: 0,
};

const organization: Organization = {
  id: 3,
  name: 'UBB Computer Science Society',
  description: null,
  contactEmail: null,
  createdAt: '2026-01-01T00:00:00Z',
};

const result: PageResponse<Event> = {
  content: [event],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

function CurrentSearch() {
  const location = useLocation();
  return <output aria-label="Current URL">{location.search}</output>;
}

function renderPage(initialEntry = '/events') {
  return render(
    withQueryClient(
      <MemoryRouter initialEntries={[initialEntry]}>
        <CurrentSearch />
        <Routes>
          <Route path="/events" element={<EventsPage />} />
        </Routes>
      </MemoryRouter>,
    ),
  );
}

describe('EventsPage', () => {
  beforeEach(() => {
    vi.mocked(eventsApi.searchEvents).mockResolvedValue(result);
    vi.mocked(organizationsApi.getOrganizations).mockResolvedValue([organization]);
  });

  it('loads the seed-visible events and reports an empty result', async () => {
    vi.mocked(eventsApi.searchEvents).mockResolvedValue({ ...result, content: [] });
    renderPage();

    expect(await screen.findByText('No events match your filters')).toBeInTheDocument();
    expect(eventsApi.searchEvents).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 20 }),
    );
  });

  it('applies filters to the request and preserves them in the URL', async () => {
    const user = userEvent.setup();
    renderPage('/events?q=workshop');

    const search = screen.getByRole('textbox', { name: 'Search events' });
    await user.clear(search);
    await user.type(search, 'Spring');

    await user.click(screen.getByRole('combobox', { name: 'Category' }));
    await user.click(await screen.findByRole('option', { name: 'Workshop' }));
    await user.click(screen.getByRole('combobox', { name: 'Organization' }));
    await user.click(await screen.findByRole('option', { name: organization.name }));
    await user.type(screen.getByLabelText('From'), '2026-12-01');
    await user.type(screen.getByLabelText('To'), '2026-12-05');

    await waitFor(() => {
      expect(eventsApi.searchEvents).toHaveBeenLastCalledWith(
        expect.objectContaining({
          q: 'Spring',
          category: 'WORKSHOP',
          organizationId: 3,
          from: expect.any(String),
          to: expect.any(String),
          page: 0,
        }),
      );
    });
    expect(screen.getByLabelText('Current URL')).toHaveTextContent(
      'q=Spring&category=WORKSHOP&organizationId=3&from=2026-12-01&to=2026-12-05',
    );
  });

  it('restores filters from the URL on page load', async () => {
    renderPage('/events?category=WORKSHOP&organizationId=3&page=2');

    await waitFor(() => {
      expect(eventsApi.searchEvents).toHaveBeenCalledWith(
        expect.objectContaining({ category: 'WORKSHOP', organizationId: 3, page: 1 }),
      );
    });
    await waitFor(() => {
      expect(screen.getByRole('combobox', { name: 'Category' })).toHaveTextContent('Workshop');
      expect(screen.getByRole('combobox', { name: 'Organization' })).toHaveTextContent(
        organization.name,
      );
    });
  });
});
