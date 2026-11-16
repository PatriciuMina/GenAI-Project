import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import type { Event } from '../../api/types';
import * as eventsApi from '../../api/events';
import { withQueryClient } from '../../test/utils';
import { StatusActions } from './StatusActions';

vi.mock('../../api/events');

const event: Event = {
  id: 14, organizationId: 7, organizationName: 'CS Society', title: 'Workshop', description: null,
  location: 'Room 2', category: 'WORKSHOP', startAt: '2030-01-02T10:00:00Z',
  endAt: '2030-01-02T12:00:00Z', capacity: 25, registrationDeadline: null,
  status: 'DRAFT', confirmedCount: 0, freePlaces: 25, version: 1,
};

function renderActions(status: Event['status']) {
  return render(withQueryClient(<StatusActions eventId={event.id} status={status} />));
}

describe('StatusActions', () => {
  it('shows only publish and cancel for DRAFT, with cancel confirmation', async () => {
    renderActions('DRAFT');
    expect(screen.getByRole('button', { name: 'Publish' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Start' })).not.toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.getByText('Are you sure you want to cancel this event?')).toBeInTheDocument();
  });

  it('shows complete and cancel for ONGOING', () => {
    renderActions('ONGOING');
    expect(screen.getByRole('button', { name: 'Complete' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Publish' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Close registration' })).not.toBeInTheDocument();
  });

  it('shows the backend transition message', async () => {
    vi.mocked(eventsApi.changeEventStatus).mockRejectedValue(new Error('cannot complete before end'));
    const { container } = render(
      withQueryClient(
        <MemoryRouter>
          <StatusActions eventId={event.id} status="PUBLISHED" editPath="/organizing/events/14/edit" />
        </MemoryRouter>,
      ),
    );
    const actionRow = screen.getByTestId('status-action-buttons');
    expect(actionRow).toContainElement(screen.getByRole('link', { name: 'Edit' }));
    await userEvent.click(screen.getByRole('button', { name: 'Start' }));
    const errorAlert = await screen.findByRole('alert');
    expect(errorAlert).toHaveTextContent('cannot complete before end');
    expect(actionRow).not.toContainElement(errorAlert);
    expect(container.firstChild).toContainElement(errorAlert);
  });
});
