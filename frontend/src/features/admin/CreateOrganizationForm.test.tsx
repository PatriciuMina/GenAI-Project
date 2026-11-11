import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import type { CreateOrganizationRequest, Organization } from '../../api/types';
import { apiError } from '../../test/utils';
import { CreateOrganizationForm } from './CreateOrganizationForm';

const created: Organization = {
  id: 3,
  name: 'UBB Robotics Club',
  description: 'Robots and coffee',
  contactEmail: 'robotics@ubbcluj.ro',
  createdAt: '2026-11-11T12:00:00Z',
};

type Submit = (request: CreateOrganizationRequest) => Promise<Organization>;

function renderForm(onSubmit: Submit) {
  // delay: null types without a timer between keys, so typing four fields stays fast
  const user = userEvent.setup({ delay: null });
  render(
    <MemoryRouter>
      <CreateOrganizationForm onSubmit={onSubmit} />
    </MemoryRouter>,
  );
  return {
    user,
    name: screen.getByLabelText(/^name/i),
    description: screen.getByLabelText(/description/i),
    contactEmail: screen.getByLabelText(/contact email/i),
    presidentEmail: screen.getByLabelText(/president email/i),
    submit: screen.getByRole('button', { name: /create organization/i }),
  };
}

describe('CreateOrganizationForm', () => {
  it('submits trimmed values, then shows a link to the new organization and clears the form', async () => {
    const onSubmit = vi.fn<Submit>().mockResolvedValue(created);
    const form = renderForm(onSubmit);

    await form.user.type(form.name, '  UBB Robotics Club ');
    await form.user.type(form.description, 'Robots and coffee');
    await form.user.type(form.contactEmail, 'robotics@ubbcluj.ro');
    await form.user.type(form.presidentEmail, ' andrei.toma@stud.ubbcluj.ro ');
    await form.user.click(form.submit);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'UBB Robotics Club',
      description: 'Robots and coffee',
      contactEmail: 'robotics@ubbcluj.ro',
      presidentEmail: 'andrei.toma@stud.ubbcluj.ro',
    });
    const link = await screen.findByRole('link', { name: /open ubb robotics club/i });
    expect(link).toHaveAttribute('href', '/organizations/3');
    expect(form.name).toHaveValue('');
    expect(form.description).toHaveValue('');
    expect(form.contactEmail).toHaveValue('');
    expect(form.presidentEmail).toHaveValue('');
  });

  it('sends empty optional fields as null', async () => {
    const onSubmit = vi
      .fn<Submit>()
      .mockResolvedValue({ ...created, description: null, contactEmail: null });
    const form = renderForm(onSubmit);

    await form.user.type(form.name, 'UBB Robotics Club');
    await form.user.type(form.description, '   ');
    await form.user.type(form.presidentEmail, 'andrei.toma@stud.ubbcluj.ro');
    await form.user.click(form.submit);

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'UBB Robotics Club',
      description: null,
      contactEmail: null,
      presidentEmail: 'andrei.toma@stud.ubbcluj.ro',
    });
  });

  it('shows required-field messages and does not submit an empty form', async () => {
    const onSubmit = vi.fn<Submit>();
    const form = renderForm(onSubmit);

    await form.user.click(form.submit);

    expect(screen.getByText('Name is required')).toBeInTheDocument();
    expect(screen.getByText('President email is required')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('checks the email format on the client', async () => {
    const onSubmit = vi.fn<Submit>();
    const form = renderForm(onSubmit);

    await form.user.type(form.name, 'UBB Robotics Club');
    await form.user.type(form.contactEmail, 'robotics');
    await form.user.type(form.presidentEmail, 'andrei.toma');
    await form.user.click(form.submit);

    expect(screen.getAllByText('Enter a valid email')).toHaveLength(2);
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('shows backend validation messages (400) under the matching fields', async () => {
    const onSubmit = vi.fn<Submit>().mockRejectedValue(
      apiError(400, 'VALIDATION_FAILED', 'Request validation failed', [
        { field: 'name', message: 'size must be between 0 and 150' },
        { field: 'presidentEmail', message: 'must be a well-formed email address' },
      ]),
    );
    const form = renderForm(onSubmit);

    await form.user.type(form.name, 'UBB Robotics Club');
    await form.user.type(form.presidentEmail, 'andrei@stud.ubbcluj.ro');
    await form.user.click(form.submit);

    expect(await screen.findByText('size must be between 0 and 150')).toBeInTheDocument();
    expect(screen.getByText('must be a well-formed email address')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveTextContent('Please correct the highlighted fields.');
  });

  it('shows a 409 name conflict above the form and keeps the values', async () => {
    const onSubmit = vi
      .fn<Submit>()
      .mockRejectedValue(
        apiError(
          409,
          'ORGANIZATION_NAME_TAKEN',
          "An organization named 'ubb theatre club' already exists",
        ),
      );
    const form = renderForm(onSubmit);

    await form.user.type(form.name, 'ubb theatre club');
    await form.user.type(form.presidentEmail, 'andrei.toma@stud.ubbcluj.ro');
    await form.user.click(form.submit);

    expect(await screen.findByRole('alert')).toHaveTextContent(
      "An organization named 'ubb theatre club' already exists",
    );
    expect(form.name).toHaveValue('ubb theatre club');
    expect(form.submit).toBeEnabled();
  });

  it('shows a 404 unknown president above the form', async () => {
    const onSubmit = vi
      .fn<Submit>()
      .mockRejectedValue(
        apiError(404, 'USER_NOT_FOUND', 'No account found with email nobody@stud.ubbcluj.ro'),
      );
    const form = renderForm(onSubmit);

    await form.user.type(form.name, 'UBB Robotics Club');
    await form.user.type(form.presidentEmail, 'nobody@stud.ubbcluj.ro');
    await form.user.click(form.submit);

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No account found with email nobody@stud.ubbcluj.ro',
    );
    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });
});
