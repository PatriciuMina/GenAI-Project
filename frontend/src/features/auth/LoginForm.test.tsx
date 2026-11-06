import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { apiError } from '../../test/utils';
import { LoginForm } from './LoginForm';

describe('LoginForm', () => {
  it('submits the trimmed email and the password', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.type(screen.getByLabelText(/email/i), '  ana@ubb.ro ');
    await userEvent.type(screen.getByLabelText(/password/i), 'password');
    await userEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(onSubmit).toHaveBeenCalledWith({ email: 'ana@ubb.ro', password: 'password' });
  });

  it('shows required-field messages and does not submit an empty form', async () => {
    const onSubmit = vi.fn();
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(screen.getByText('Email is required')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('shows the backend message on 401 "Invalid credentials"', async () => {
    const onSubmit = vi
      .fn()
      .mockRejectedValue(apiError(401, 'UNAUTHORIZED', 'Invalid credentials'));
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.type(screen.getByLabelText(/email/i), 'ana@ubb.ro');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrong-password');
    await userEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid credentials');
    expect(screen.getByRole('button', { name: /log in/i })).toBeEnabled();
  });

  it('shows backend validation messages next to the matching field', async () => {
    const onSubmit = vi
      .fn()
      .mockRejectedValue(
        apiError(400, 'VALIDATION_FAILED', 'Request validation failed', [
          { field: 'email', message: 'must be a well-formed email address' },
        ]),
      );
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.type(screen.getByLabelText(/email/i), 'not-an-email');
    await userEvent.type(screen.getByLabelText(/password/i), 'password');
    await userEvent.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByText('must be a well-formed email address')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveTextContent('Please correct the highlighted fields.');
  });
});
