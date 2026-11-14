import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as organizationsApi from '../../api/organizations';
import { withQueryClient } from '../../test/utils';
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

    render(withQueryClient(<MembersSection orgId={1} enabled />));

    expect(await screen.findByText('Ioana Rusu')).toBeInTheDocument();
    expect(screen.getByText('MEMBER')).toBeInTheDocument();
    expect(screen.getByText(/joined 6 november 2026/i)).toBeInTheDocument();
  });

  it('stays hidden and does not fetch for a non-member', () => {
    render(withQueryClient(<MembersSection orgId={1} enabled={false} />));

    expect(screen.queryByRole('heading', { name: 'Members' })).not.toBeInTheDocument();
    expect(organizationsApi.getMembers).not.toHaveBeenCalled();
  });
});
