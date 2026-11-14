import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { Event, Membership } from '../../api/types';
import { EventForm } from './EventForm';

const memberships: Membership[] = [
  {
    membershipId: 1,
    organizationId: 7,
    organizationName: 'CS Society',
    role: 'ORGANIZER',
    joinedAt: '2026-01-01T00:00:00Z',
  },
];

const event: Event = {
  id: 14,
  organizationId: 7,
  organizationName: 'CS Society',
  title: 'Workshop',
  description: null,
  location: 'Room 2',
  category: 'WORKSHOP',
  startAt: '2030-01-02T10:00:00Z',
  endAt: '2030-01-02T12:00:00Z',
  capacity: 25,
  registrationDeadline: null,
  status: 'DRAFT',
  confirmedCount: 0,
  freePlaces: 25,
  version: 3,
};

describe('EventForm', () => {
  it('validates required fields and does not submit an empty form', async () => {
    const onSubmit = vi.fn();
    render(
      <EventForm
        memberships={[
          ...memberships,
          {
            membershipId: 2,
            organizationId: 8,
            organizationName: 'Theatre Club',
            role: 'PRESIDENT',
            joinedAt: '2026-01-01T00:00:00Z',
          },
        ]}
        onSubmit={onSubmit}
      />,
    );

    await userEvent.click(screen.getByRole('button', { name: 'Create event' }));

    expect(screen.getByText('Organization is required')).toBeInTheDocument();
    expect(screen.getByText('Title is required')).toBeInTheDocument();
    expect(screen.getByText('Location is required')).toBeInTheDocument();
    expect(screen.getByText('Capacity must be greater than 0')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('submits local date fields as UTC ISO strings and includes the edit version', async () => {
    const onSubmit = vi.fn().mockResolvedValue(event);
    render(<EventForm memberships={memberships} event={event} onSubmit={onSubmit} />);

    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }));

    expect(onSubmit).toHaveBeenCalledWith(7, {
      title: 'Workshop',
      description: null,
      location: 'Room 2',
      category: 'WORKSHOP',
      startAt: '2030-01-02T10:00:00.000Z',
      endAt: '2030-01-02T12:00:00.000Z',
      capacity: 25,
      registrationDeadline: null,
      version: 3,
    });
  });
});
