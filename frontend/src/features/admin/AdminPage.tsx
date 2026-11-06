import { ComingSoon } from '../../components/ComingSoon';

// TODO(W1): admin tools (e.g. create organizations, assign presidents). Only ADMIN users see
// this page (route guarded with <ProtectedRoute roles={['ADMIN']}> in routes/router.tsx).
export function AdminPage() {
  return <ComingSoon title="Admin" workflow="W1" description="Administration tools." />;
}
