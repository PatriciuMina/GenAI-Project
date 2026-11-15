// All URLs of the app in one place. Use these constants instead of string literals.
export const paths = {
  login: '/login',
  register: '/register',
  organizations: '/organizations',
  organization: (id: number | string) => `/organizations/${id}`,
  events: '/events',
  myRegistrations: '/my-registrations',
  organizing: '/organizing',
  organizingNewEvent: '/organizing/events/new',
  organizingEditEvent: (id: number | string) => `/organizing/events/${id}/edit`,
  admin: '/admin',
} as const;
