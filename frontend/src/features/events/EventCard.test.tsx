import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it } from 'vitest';
import type { Event } from '../../api/types';
import { EventCard } from './EventCard';

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

function renderCard(value: Event) {
  return render(
    <MemoryRouter>
      <EventCard event={value} />
    </MemoryRouter>,
  );
}

describe('EventCard', () => {
  it('shows the places left and links to event details', () => {
    renderCard(event);

    expect(screen.getByText('18 of 30 places left')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Spring Workshop/ })).toHaveAttribute(
      'href',
      '/events/7',
    );
  });

  it('shows Full when there are no places left', () => {
    renderCard({ ...event, freePlaces: 0 });

    expect(screen.getByText('Full')).toBeInTheDocument();
  });

  it('shows Registration closed when registration has closed', () => {
    renderCard({ ...event, status: 'REGISTRATION_CLOSED', freePlaces: 0 });

    expect(screen.getByText('Registration closed')).toBeInTheDocument();
    expect(screen.queryByText('Full')).not.toBeInTheDocument();
  });
});
