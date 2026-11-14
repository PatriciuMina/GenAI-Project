import { describe, expect, it } from 'vitest';
import type { Membership } from '../api/types';
import { visibleNavItems } from './AppLayout';

const memberships: Membership[] = [
  {
    membershipId: 1,
    organizationId: 1,
    organizationName: 'Computer Science Society',
    role: 'ORGANIZER',
    joinedAt: '2026-11-01T10:00:00Z',
  },
];

describe('visibleNavItems', () => {
  it('shows Organizing to presidents and organizers', () => {
    expect(visibleNavItems('STUDENT', memberships).map(({ label }) => label)).toContain('Organizing');
    expect(
      visibleNavItems('STUDENT', [{ ...memberships[0], role: 'PRESIDENT' }]).map(({ label }) => label),
    ).toContain('Organizing');
  });

  it('hides Organizing when memberships are loading or contain only members', () => {
    const labels = (items: ReturnType<typeof visibleNavItems>) => items.map(({ label }) => label);
    expect(labels(visibleNavItems('STUDENT'))).not.toContain('Organizing');
    expect(labels(visibleNavItems('STUDENT', [{ ...memberships[0], role: 'MEMBER' }]))).not.toContain(
      'Organizing',
    );
    expect(labels(visibleNavItems('STUDENT', []))).not.toContain('Organizing');
  });
});
