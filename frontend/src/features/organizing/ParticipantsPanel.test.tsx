import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Participant } from '../../api/types';
import * as registrationsApi from '../../api/registrations';
import { withQueryClient } from '../../test/utils';
import { ParticipantsPanel } from './ParticipantsPanel';

vi.mock('../../api/registrations');

const participants: Participant[] = [
  {
    registrationId: 7,
    userId: 21,
    fullName: 'Maria Dobre',
    email: 'maria.dobre@example.com',
    status: 'CONFIRMED',
    registeredAt: '2026-11-01T10:00:00Z',
    checkedInAt: null,
  },
  {
    registrationId: 8,
    userId: 22,
    fullName: 'Mihai Pop',
    email: 'mihai.pop@example.com',
    status: 'CONFIRMED',
    registeredAt: '2026-11-02T10:00:00Z',
    checkedInAt: '2026-11-16T08:00:00Z',
  },
  {
    registrationId: 9,
    userId: 23,
    fullName: 'Ana Ionescu',
    email: 'ana.ionescu@example.com',
    status: 'CANCELLED',
    registeredAt: '2026-11-03T10:00:00Z',
    checkedInAt: null,
  },
];
let participantState = participants;

function renderPanel(status: 'PUBLISHED' | 'REGISTRATION_CLOSED' | 'ONGOING') {
  return render(
    withQueryClient(
      <ParticipantsPanel eventId={14} status={status} capacity={25} confirmedCount={2} />,
    ),
  );
}

describe('ParticipantsPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    participantState = participants;
    vi.mocked(registrationsApi.getParticipants).mockImplementation(async () => participantState);
    vi.mocked(registrationsApi.checkInParticipant).mockImplementation(async (_eventId, registrationId) => {
      participantState = participantState.map((participant) =>
        participant.registrationId === registrationId
          ? { ...participant, checkedInAt: '2026-11-16T09:00:00Z' }
          : participant,
      );
      return participantState.find(({ registrationId: id }) => id === registrationId) ?? {
        ...participants[0],
        registrationId,
      };
    });
  });

  it('allows check-in only for confirmed participants who have not checked in', async () => {
    renderPanel('ONGOING');
    expect(await screen.findByText('2 / 25 confirmed, 1 checked in')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Check in' })).toBeEnabled();
    expect(screen.getAllByRole('button', { name: 'Check in' })).toHaveLength(1);

    await userEvent.click(screen.getByRole('button', { name: 'Check in' }));
    await waitFor(() => expect(registrationsApi.checkInParticipant).toHaveBeenCalledWith(14, 7));
    expect(await screen.findByText('2 / 25 confirmed, 2 checked in')).toBeInTheDocument();
  });

  it('disables check-in on PUBLISHED and explains why', async () => {
    renderPanel('PUBLISHED');
    const button = await screen.findByRole('button', { name: 'Check in' });
    expect(button).toBeDisabled();
    fireEvent.mouseOver(button.parentElement as HTMLElement);
    expect(await screen.findByText(/Check-in is available after registration closes/)).toBeInTheDocument();
  });

  it('filters the participant table by name without regard to case', async () => {
    renderPanel('REGISTRATION_CLOSED');
    await screen.findByText('Maria Dobre');
    await userEvent.type(screen.getByRole('textbox', { name: 'Search participants by name' }), 'mArIa');
    expect(screen.getByText('Maria Dobre')).toBeInTheDocument();
    expect(screen.queryByText('Mihai Pop')).not.toBeInTheDocument();
    expect(screen.queryByText('Ana Ionescu')).not.toBeInTheDocument();
  });
});
