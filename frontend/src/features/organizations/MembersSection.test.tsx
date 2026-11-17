import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as organizationsApi from '../../api/organizations';
import { apiError, withQueryClient } from '../../test/utils';
import { MembersSection } from './MembersSection';

vi.mock('../../api/organizations');

describe('MembersSection', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders members with their roles and joined dates', async () => {
    vi.mocked(organizationsApi.getMembers).mockResolvedValue([
      {
        membershipId: 1,
        userId: 2,
        fullName: 'Ioana Rusu',
        email: 'ioana.rusu@stud.ubbcluj.ro',
        role: 'MEMBER',
        joinedAt: '2026-11-06T10:15:30Z',
      },
    ]);

    render(withQueryClient(<MembersSection orgId={1} enabled canManage={false} />));

    expect(await screen.findByText('Ioana Rusu')).toBeInTheDocument();
    expect(screen.getByText('MEMBER')).toBeInTheDocument();
    expect(screen.getByText(/joined 6 november 2026/i)).toBeInTheDocument();
  });

  it('stays hidden and does not fetch for a non-member', () => {
    render(withQueryClient(<MembersSection orgId={1} enabled={false} canManage={false} />));

    expect(screen.queryByRole('heading', { name: 'Members' })).not.toBeInTheDocument();
    expect(organizationsApi.getMembers).not.toHaveBeenCalled();
  });

  it('submits an email and role, then clears the form', async () => {
    const user = userEvent.setup();
    vi.mocked(organizationsApi.getMembers)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          membershipId: 9,
          userId: 10,
          fullName: 'Andrei Toma',
          email: 'andrei.toma@stud.ubbcluj.ro',
          role: 'MEMBER',
          joinedAt: '2026-11-06T10:15:30Z',
        },
      ]);
    vi.mocked(organizationsApi.addMember).mockResolvedValue({
      membershipId: 9,
      userId: 10,
      fullName: 'Andrei Toma',
      email: 'andrei.toma@stud.ubbcluj.ro',
      role: 'MEMBER',
      joinedAt: '2026-11-06T10:15:30Z',
    });

    render(withQueryClient(<MembersSection orgId={1} enabled canManage />));
    await user.type(
      await screen.findByRole('textbox', { name: 'Email' }),
      'andrei.toma@stud.ubbcluj.ro',
    );
    await user.click(screen.getByRole('button', { name: 'Add member' }));

    await waitFor(() =>
      expect(organizationsApi.addMember).toHaveBeenCalledWith(1, {
        email: 'andrei.toma@stud.ubbcluj.ro',
        role: 'MEMBER',
      }),
    );
    expect(screen.getByRole('textbox', { name: 'Email' })).toHaveValue('');
    expect(await screen.findByText('Andrei Toma')).toBeInTheDocument();
  });

  it('shows a readable backend error next to the add form', async () => {
    const user = userEvent.setup();
    vi.mocked(organizationsApi.getMembers).mockResolvedValue([]);
    vi.mocked(organizationsApi.addMember).mockRejectedValue(
      apiError(404, 'USER_NOT_FOUND', 'No user with this email exists'),
    );

    render(withQueryClient(<MembersSection orgId={1} enabled canManage />));
    await user.type(await screen.findByRole('textbox', { name: 'Email' }), 'missing@example.com');
    await user.click(screen.getByRole('button', { name: 'Add member' }));

    expect(await screen.findByText('No account was found with that email.')).toBeInTheDocument();
  });

  it('shows the duplicate-member backend error next to the add form', async () => {
    const user = userEvent.setup();
    vi.mocked(organizationsApi.getMembers).mockResolvedValue([]);
    vi.mocked(organizationsApi.addMember).mockRejectedValue(
      apiError(409, 'ALREADY_MEMBER', 'This user is already a member'),
    );

    render(withQueryClient(<MembersSection orgId={1} enabled canManage />));
    await user.type(await screen.findByRole('textbox', { name: 'Email' }), 'already@example.com');
    await user.click(screen.getByRole('button', { name: 'Add member' }));

    expect(
      await screen.findByText('This user is already a member of the organization.'),
    ).toBeInTheDocument();
  });

  it('hides management controls when the viewer cannot manage members', async () => {
    vi.mocked(organizationsApi.getMembers).mockResolvedValue([
      {
        membershipId: 1,
        userId: 2,
        fullName: 'President',
        email: 'president@example.com',
        role: 'PRESIDENT',
        joinedAt: '2026-11-06T10:15:30Z',
      },
      {
        membershipId: 2,
        userId: 3,
        fullName: 'Member',
        email: 'member@example.com',
        role: 'MEMBER',
        joinedAt: '2026-11-06T10:15:30Z',
      },
    ]);
    render(withQueryClient(<MembersSection orgId={1} enabled canManage={false} />));

    expect(await screen.findByText('Member')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Remove' })).not.toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Add member' })).not.toBeInTheDocument();
  });
});
