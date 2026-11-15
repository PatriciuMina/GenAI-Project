import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as announcementsApi from '../../api/announcements';
import { apiError, createTestQueryClient, withQueryClient } from '../../test/utils';
import { EventAnnouncements } from './EventAnnouncements';

vi.mock('../../api/announcements');

describe('EventAnnouncements', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows the announcements with title, text, author and date', async () => {
    vi.mocked(announcementsApi.getAnnouncements).mockResolvedValue([
      {
        id: 1,
        title: 'Bring your laptop',
        body: 'Please install JDK 21 and an IDE before the workshop.',
        authorName: 'Elena Marin',
        // midday UTC is the same calendar day in every time zone from UTC-11 to UTC+11
        createdAt: '2026-11-06T12:00:00Z',
      },
    ]);

    render(withQueryClient(<EventAnnouncements eventId={2} />));

    expect(await screen.findByRole('heading', { name: 'Announcements' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Bring your laptop' })).toBeInTheDocument();
    expect(
      screen.getByText('Please install JDK 21 and an IDE before the workshop.'),
    ).toBeInTheDocument();
    expect(screen.getByText(/Elena Marin · 6 November 2026/)).toBeInTheDocument();
    expect(announcementsApi.getAnnouncements).toHaveBeenCalledWith(2);
  });

  it('shows no section and no error when the user may not read them (403)', async () => {
    vi.mocked(announcementsApi.getAnnouncements).mockRejectedValue(
      apiError(403, 'FORBIDDEN', 'Only members and registered participants can read announcements'),
    );
    const client = createTestQueryClient();

    const { container } = render(withQueryClient(<EventAnnouncements eventId={2} />, client));

    await waitFor(() => expect(announcementsApi.getAnnouncements).toHaveBeenCalledWith(2));
    await waitFor(() => expect(client.isFetching()).toBe(0));
    expect(screen.queryByRole('heading', { name: 'Announcements' })).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(container).toBeEmptyDOMElement();
  });

  it('says so when the event has no announcements yet', async () => {
    vi.mocked(announcementsApi.getAnnouncements).mockResolvedValue([]);

    render(withQueryClient(<EventAnnouncements eventId={2} />));

    expect(await screen.findByText('No announcements yet.')).toBeInTheDocument();
  });

  it('shows the backend message with a retry button for other errors', async () => {
    vi.mocked(announcementsApi.getAnnouncements).mockRejectedValue(
      apiError(500, 'INTERNAL_ERROR', 'Something went wrong'),
    );

    render(withQueryClient(<EventAnnouncements eventId={2} />));

    expect(await screen.findByText('Something went wrong')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });
});
