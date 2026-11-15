import { createBrowserRouter, Navigate } from 'react-router';
import { AppLayout } from '../components/AppLayout';
import { NotFoundPage } from '../components/NotFoundPage';
import { AdminPage } from '../features/admin/AdminPage';
import { LoginPage } from '../features/auth/LoginPage';
import { RegisterPage } from '../features/auth/RegisterPage';
import { EventDetailsPage } from '../features/events/EventDetailsPage';
import { EventsPage } from '../features/events/EventsPage';
import { OrganizationDetailsPage } from '../features/organizations/OrganizationDetailsPage';
import { OrganizationsListPage } from '../features/organizations/OrganizationsListPage';
import { OrganizingPage } from '../features/organizing/OrganizingPage';
import { EventFormPage } from '../features/organizing/EventFormPage';
import { MyRegistrationsPage } from '../features/registrations/MyRegistrationsPage';
import { paths } from './paths';
import { ProtectedRoute } from './ProtectedRoute';

export const router = createBrowserRouter([
  // Public pages
  { path: paths.login, element: <LoginPage /> },
  { path: paths.register, element: <RegisterPage /> },

  // Everything below needs a logged-in user and uses the app layout (top bar + navigation).
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true, element: <Navigate to={paths.organizations} replace /> },
          { path: paths.organizations, element: <OrganizationsListPage /> },
          { path: '/organizations/:id', element: <OrganizationDetailsPage /> },
          { path: paths.events, element: <EventsPage /> },
          { path: '/events/:id', element: <EventDetailsPage /> },
          { path: paths.myRegistrations, element: <MyRegistrationsPage /> },
          { path: paths.organizing, element: <OrganizingPage /> },
          { path: paths.organizingNewEvent, element: <EventFormPage /> },
          { path: '/organizing/events/:id/edit', element: <EventFormPage /> },
          {
            path: paths.admin,
            element: (
              <ProtectedRoute roles={['ADMIN']}>
                <AdminPage />
              </ProtectedRoute>
            ),
          },
          { path: '*', element: <NotFoundPage /> },
        ],
      },
    ],
  },
]);
