import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as announcementsApi from '../../api/announcements';
import * as eventsApi from '../../api/events';
import type { Event } from '../../api/types';
import { apiError, withQueryClient } from '../../test/utils';
import { EventDetailsPage } from './EventDetailsPage';

vi.mock('../../api/events');
vi.mock('../../api/announcements');

const event: Event = {
  id: 2,
  organizationId: 1,
  organizationName: 'UBB Computer Science Society',
  title: 'Intro to Spring Boot Workshop',
  description: 'Hands-on workshop: build a REST API.',
  location: 'Room 2/I, Mathematica Building',
  category: 'WORKSHOP',
  startAt: '2026-12-10T16:00:00Z',
  endAt: '2026-12-10T18:00:00Z',
  capacity: 30,
  registrationDeadline: '2026-12-09T12:00:00Z',
  status: 'PUBLISHED',
  confirmedCount: 2,
  freePlaces: 28,
  version: 0,
};

function renderPage(path = '/events/2') {
  return render(
    withQueryClient(
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/events/:id" element={<EventDetailsPage />} />
        </Routes>
      </MemoryRouter>,
    ),
  );
}

describe('EventDetailsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(announcementsApi.getAnnouncements).mockResolvedValue([]);
  });

  it('shows the announcements of the event below the details', async () => {
    vi.mocked(eventsApi.getEvent).mockResolvedValue(event);
    vi.mocked(announcementsApi.getAnnouncements).mockResolvedValue([
      {
        id: 1,
        title: 'Bring your laptop',
        body: 'Please install JDK 21 and an IDE before the workshop.',
        authorName: 'Elena Marin',
        createdAt: '2026-11-06T12:00:00Z',
      },
    ]);

    renderPage();

    expect(await screen.findByRole('heading', { name: 'Bring your laptop' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Announcements' })).toBeInTheDocument();
    expect(announcementsApi.getAnnouncements).toHaveBeenCalledWith(2);
  });

  it('shows no announcements section for a user who may not read them', async () => {
    vi.mocked(eventsApi.getEvent).mockResolvedValue(event);
    vi.mocked(announcementsApi.getAnnouncements).mockRejectedValue(
      apiError(403, 'FORBIDDEN', 'Not allowed'),
    );

    renderPage();

    expect(
      await screen.findByRole('heading', { name: 'Intro to Spring Boot Workshop' }),
    ).toBeInTheDocument();
    await waitFor(() => expect(announcementsApi.getAnnouncements).toHaveBeenCalledWith(2));
    expect(screen.queryByRole('heading', { name: 'Announcements' })).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('shows what a student needs to decide: place, free places, deadline and status', async () => {
    vi.mocked(eventsApi.getEvent).mockResolvedValue(event);

    renderPage();

    expect(
      await screen.findByRole('heading', { name: 'Intro to Spring Boot Workshop' }),
    ).toBeInTheDocument();
    expect(screen.getByText('Published')).toBeInTheDocument();
    expect(screen.getByText('Workshop')).toBeInTheDocument();
    expect(screen.getByText('Room 2/I, Mathematica Building')).toBeInTheDocument();
    expect(screen.getByText('28 of 30 places left')).toBeInTheDocument();
    expect(screen.getByText(/Until 9 December 2026/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'UBB Computer Science Society' })).toHaveAttribute(
      'href',
      '/organizations/1',
    );
    expect(eventsApi.getEvent).toHaveBeenCalledWith(2);
  });

  it('says registration is open until the start when the event has no deadline', async () => {
    vi.mocked(eventsApi.getEvent).mockResolvedValue({ ...event, registrationDeadline: null });

    renderPage();

    expect(await screen.findByText('Open until the event starts')).toBeInTheDocument();
  });

  it('shows "Full" and the closed status for a full event with closed registration', async () => {
    vi.mocked(eventsApi.getEvent).mockResolvedValue({
      ...event,
      status: 'REGISTRATION_CLOSED',
      confirmedCount: 30,
      freePlaces: 0,
    });

    renderPage();

    expect(await screen.findByText('Full')).toBeInTheDocument();
    expect(screen.getByText('Registration closed')).toBeInTheDocument();
  });

  it('shows "Event not found" when the backend answers 404 (unknown event or a hidden draft)', async () => {
    vi.mocked(eventsApi.getEvent).mockRejectedValue(apiError(404, 'NOT_FOUND', 'Event 999 not found'));

    renderPage('/events/999');

    expect(await screen.findByText('Event not found.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /all events/i })).toHaveAttribute('href', '/events');
  });

  it('shows "Event not found" for an id that is not a number, without calling the backend', () => {
    renderPage('/events/abc');

    expect(screen.getByText('Event not found.')).toBeInTheDocument();
    expect(eventsApi.getEvent).not.toHaveBeenCalled();
  });

  it('shows the backend error message with a retry button for other errors', async () => {
    vi.mocked(eventsApi.getEvent).mockRejectedValue(apiError(500, 'INTERNAL_ERROR', 'Something went wrong'));

    renderPage();

    expect(await screen.findByText('Something went wrong')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });
});
