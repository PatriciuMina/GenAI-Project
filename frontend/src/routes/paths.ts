// All URLs of the app in one place. Use these constants instead of string literals.
export const paths = {
  login: '/login',
  register: '/register',
  organizations: '/organizations',
  organization: (id: number | string) => `/organizations/${id}`,
  events: '/events',
  myRegistrations: '/my-registrations',
  organizing: '/organizing',
  admin: '/admin',
} as const;
