import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import * as organizationsApi from '../../api/organizations';
import { withQueryClient } from '../../test/utils';
import { AdminPage } from './AdminPage';

vi.mock('../../api/organizations');

describe('AdminPage', () => {
  it('creates an organization through the API and links to it', async () => {
    vi.mocked(organizationsApi.createOrganization).mockResolvedValue({
      id: 3,
      name: 'UBB Robotics Club',
      description: null,
      contactEmail: null,
      createdAt: '2026-11-11T12:00:00Z',
    });
    const user = userEvent.setup({ delay: null });
    render(
      withQueryClient(
        <MemoryRouter>
          <AdminPage />
        </MemoryRouter>,
      ),
    );

    expect(screen.getByRole('heading', { name: /create an organization/i })).toBeInTheDocument();
    await user.type(screen.getByLabelText(/^name/i), 'UBB Robotics Club');
    await user.type(screen.getByLabelText(/president email/i), 'andrei.toma@stud.ubbcluj.ro');
    await user.click(screen.getByRole('button', { name: /create organization/i }));

    expect(await screen.findByRole('link', { name: /open ubb robotics club/i })).toHaveAttribute(
      'href',
      '/organizations/3',
    );
    expect(vi.mocked(organizationsApi.createOrganization).mock.calls[0][0]).toEqual({
      name: 'UBB Robotics Club',
      description: null,
      contactEmail: null,
      presidentEmail: 'andrei.toma@stud.ubbcluj.ro',
    });
  });
});
