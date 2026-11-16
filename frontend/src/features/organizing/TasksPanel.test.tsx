import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import * as organizationApi from '../../api/organizations';
import * as taskApi from '../../api/tasks';
import type { EventTask, Member } from '../../api/types';
import { withQueryClient } from '../../test/utils';
import { TasksPanel } from './TasksPanel';

vi.mock('../../api/organizations');
vi.mock('../../api/tasks');

const members: Member[] = [
  { membershipId: 12, userId: 4, fullName: 'Elena Marin', email: 'elena@example.test', role: 'ORGANIZER', joinedAt: '2030-01-01T00:00:00Z' },
];
const seededTask: EventTask = {
  id: 5, title: 'Prepare slides', description: null, status: 'TODO', dueAt: null,
  assignee: null, createdAt: '2030-01-01T00:00:00Z',
};

function renderPanel() {
  return render(withQueryClient(<TasksPanel event={{ id: 8, organizationId: 3 }} />));
}

describe('TasksPanel', () => {
  it('creates a task with optional due date and assignee and renders the returned list', async () => {
    let currentTasks: EventTask[] = [];
    vi.mocked(organizationApi.getMembers).mockResolvedValue(members);
    vi.mocked(taskApi.getEventTasks).mockImplementation(async () => currentTasks);
    vi.mocked(taskApi.createEventTask).mockImplementation(async () => {
      currentTasks = [seededTask];
      return seededTask;
    });
    renderPanel();

    await userEvent.type(screen.getByRole('textbox', { name: 'Title' }), 'Prepare slides');
    await userEvent.click(screen.getByRole('combobox', { name: 'Assignee' }));
    await userEvent.click(await screen.findByRole('option', { name: 'Elena Marin' }));
    await userEvent.click(screen.getByRole('button', { name: 'Create task' }));

    await waitFor(() => expect(taskApi.createEventTask).toHaveBeenCalledWith(8, {
      title: 'Prepare slides', description: null, dueAt: null, assigneeMembershipId: 12,
    }));
    expect(await screen.findByText('Prepare slides')).toBeInTheDocument();
    expect(screen.queryByText('No tasks yet')).not.toBeInTheDocument();
  });

  it('shows the empty state and updates task status and assignment', async () => {
    vi.mocked(organizationApi.getMembers).mockResolvedValue(members);
    vi.mocked(taskApi.getEventTasks).mockResolvedValue([seededTask]);
    vi.mocked(taskApi.updateEventTask).mockImplementation(async (_eventId, _taskId, body) => ({
      ...seededTask,
      status: body.status ?? seededTask.status,
      assignee: body.assigneeMembershipId
        ? { membershipId: body.assigneeMembershipId, fullName: 'Elena Marin' }
        : body.unassign ? null : seededTask.assignee,
    }));
    renderPanel();
    expect(await screen.findByText('Prepare slides')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('combobox', { name: 'Status for Prepare slides' }));
    await userEvent.click(await screen.findByRole('option', { name: 'In progress' }));
    await waitFor(() => expect(taskApi.updateEventTask).toHaveBeenCalledWith(8, 5, { status: 'IN_PROGRESS' }));
    await userEvent.click(screen.getByRole('combobox', { name: 'Assignee for Prepare slides' }));
    await userEvent.click(await screen.findByRole('option', { name: 'Elena Marin' }));
    await waitFor(() => expect(taskApi.updateEventTask).toHaveBeenCalledWith(8, 5, { assigneeMembershipId: 12 }));
  });

  it('shows the empty state when there are no tasks', async () => {
    vi.mocked(organizationApi.getMembers).mockResolvedValue(members);
    vi.mocked(taskApi.getEventTasks).mockResolvedValue([]);
    renderPanel();
    expect(await screen.findByText('No tasks yet')).toBeInTheDocument();
  });
});
