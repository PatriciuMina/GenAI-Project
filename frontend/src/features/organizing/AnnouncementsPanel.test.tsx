import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as announcementsApi from '../../api/announcements';
import { withQueryClient } from '../../test/utils';
import { AnnouncementsPanel } from './AnnouncementsPanel';

vi.mock('../../api/announcements');

describe('AnnouncementsPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('rejects empty fields on the client', async () => {
    vi.mocked(announcementsApi.getAnnouncements).mockResolvedValue([]);
    render(withQueryClient(<AnnouncementsPanel eventId={2} />));

    await userEvent.click(screen.getByRole('button', { name: 'Post announcement' }));

    expect(screen.getByText('Title is required')).toBeInTheDocument();
    expect(screen.getByText('Body is required')).toBeInTheDocument();
    expect(announcementsApi.postAnnouncement).not.toHaveBeenCalled();
  });

  it('posts, refreshes the list with the new item, and clears the form', async () => {
    const posted = {
      id: 9,
      title: 'Room update',
      body: 'Meet in room 5.',
      authorName: 'Elena Marin',
      createdAt: '2026-11-06T12:00:00Z',
    };
    vi.mocked(announcementsApi.getAnnouncements)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([posted]);
    vi.mocked(announcementsApi.postAnnouncement).mockResolvedValue(posted);
    render(withQueryClient(<AnnouncementsPanel eventId={2} />));

    await userEvent.type(await screen.findByRole('textbox', { name: 'Title' }), 'Room update');
    await userEvent.type(screen.getByRole('textbox', { name: 'Body' }), 'Meet in room 5.');
    await userEvent.click(screen.getByRole('button', { name: 'Post announcement' }));

    expect(await screen.findByRole('heading', { name: 'Room update' })).toBeInTheDocument();
    expect(announcementsApi.postAnnouncement).toHaveBeenCalledWith(2, {
      title: 'Room update',
      body: 'Meet in room 5.',
    });
    await waitFor(() => {
      expect(screen.getByRole('textbox', { name: 'Title' })).toHaveValue('');
      expect(screen.getByRole('textbox', { name: 'Body' })).toHaveValue('');
    });
  });
});
