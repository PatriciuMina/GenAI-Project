import { describe, expect, it } from 'vitest';
import { getErrorMessage } from './errorMessages';

describe('getErrorMessage', () => {
  it.each([
    ['USER_NOT_FOUND', 'No account was found with that email.'],
    ['ALREADY_MEMBER', 'This user is already a member of the organization.'],
    ['CANNOT_REMOVE_PRESIDENT', 'The organization president cannot be removed.'],
    ['ORGANIZATION_NAME_TAKEN', 'An organization with this name already exists.'],
    ['EVENT_NOT_EDITABLE', 'This event can no longer be edited.'],
    ['CONCURRENT_UPDATE', 'This event was changed elsewhere. Reload and try again.'],
    ['INVALID_STATUS_TRANSITION', 'This event cannot be moved to that status.'],
    ['REGISTRATION_CLOSED', 'Registration is closed for this event.'],
    ['EVENT_FULL', 'Sorry, this event is full.'],
    ['ALREADY_REGISTERED', 'You are already registered for this event.'],
    ['NOT_REGISTERED', 'You do not have an active registration for this event.'],
    ['CANCEL_NOT_ALLOWED', 'This registration can no longer be cancelled.'],
    ['CHECK_IN_NOT_ALLOWED', 'This registration cannot be checked in.'],
    ['ALREADY_CHECKED_IN', 'This participant has already checked in.'],
    ['ANNOUNCEMENT_NOT_ALLOWED', 'Announcements are not allowed for this event.'],
    ['ASSIGNEE_NOT_IN_ORGANIZATION', 'Choose a task assignee from this organization.'],
  ])('maps %s to a friendly message', (code, message) => {
    expect(getErrorMessage(code, 'backend message')).toBe(message);
  });

  it('uses the backend message for unknown codes', () => {
    expect(getErrorMessage('UNRECOGNIZED_CODE', 'Backend detail')).toBe('Backend detail');
  });
});
