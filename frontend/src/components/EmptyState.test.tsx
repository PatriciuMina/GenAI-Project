import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { EmptyState } from './EmptyState';

describe('EmptyState', () => {
  it('shows the message without an action', () => {
    render(<EmptyState message="No tasks yet" />);

    expect(screen.getByText('No tasks yet')).toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });

  it('renders a link action', () => {
    render(
      <MemoryRouter>
        <EmptyState message="Nothing here" action={{ label: 'Browse events', to: '/events' }} />
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: 'Browse events' })).toHaveAttribute('href', '/events');
  });

  it('renders a button action and calls it', async () => {
    const onClick = vi.fn();
    const user = userEvent.setup({ delay: null });
    render(
      <EmptyState
        message="No events match your filters"
        action={{ label: 'Clear filters', onClick }}
      />,
    );

    await user.click(screen.getByRole('button', { name: 'Clear filters' }));

    expect(onClick).toHaveBeenCalledOnce();
  });
});
