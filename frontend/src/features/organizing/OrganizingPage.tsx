import { ComingSoon } from '../../components/ComingSoon';

// TODO(W5): event organization (organizer tasks, attendance check-in, announcements).
// Also gate the "Organizing" navigation item by organization role (see AppLayout NAV_ITEMS).
export function OrganizingPage() {
  return (
    <ComingSoon
      title="Organizing"
      workflow="W5"
      description="Organizer tasks, attendance check-in and announcements."
    />
  );
}
