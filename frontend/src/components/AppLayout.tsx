import { useState } from 'react';
import {
  AppBar,
  Box,
  Button,
  Container,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import { NavLink, Outlet, useNavigate } from 'react-router';
import type { GlobalRole, Membership } from '../api/types';
import { useMyMemberships } from '../features/organizations/queries';
import { useAuth } from '../features/auth/useAuth';
import { paths } from '../routes/paths';
import { Logo } from './Logo';

interface NavItem {
  label: string;
  to: string;
  /** Global roles that see this item; omitted = every logged-in user. */
  roles?: GlobalRole[];
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Organizations', to: paths.organizations },
  { label: 'Events', to: paths.events },
  { label: 'My registrations', to: paths.myRegistrations },
  { label: 'Organizing', to: paths.organizing },
  { label: 'Admin', to: paths.admin, roles: ['ADMIN'] },
];

// eslint-disable-next-line react-refresh/only-export-components -- exported for reuse/tests
export function visibleNavItems(role: GlobalRole, memberships?: Membership[]): NavItem[] {
  const canOrganize = memberships?.some(
    ({ role: orgRole }) => orgRole === 'ORGANIZER' || orgRole === 'PRESIDENT',
  );
  return NAV_ITEMS.filter(
    (item) =>
      (!item.roles || item.roles.includes(role)) &&
      (item.label !== 'Organizing' || canOrganize === true),
  );
}

export function AppLayout() {
  const { user, logout } = useAuth();
  const { data: memberships } = useMyMemberships();
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);

  if (user === null) return null; // ProtectedRoute guarantees a user; keeps TypeScript happy
  const items = visibleNavItems(user.role, memberships);

  const handleLogout = () => {
    logout();
    navigate(paths.login, { replace: true });
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky">
        <Toolbar sx={{ gap: 1 }}>
          <IconButton
            color="inherit"
            edge="start"
            aria-label="Open navigation"
            onClick={() => setDrawerOpen(true)}
            sx={{ display: { md: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Box sx={{ mr: 2 }}>
            <Logo color="common.white" />
          </Box>
          <Box component="nav" sx={{ display: { xs: 'none', md: 'flex' }, gap: 0.5, flexGrow: 1 }}>
            {items.map((item) => (
              <Button
                key={item.to}
                component={NavLink}
                to={item.to}
                color="inherit"
                sx={{ '&.active': { bgcolor: 'rgba(255,255,255,0.16)' } }}
              >
                {item.label}
              </Button>
            ))}
          </Box>
          <Box sx={{ flexGrow: { xs: 1, md: 0 } }} />
          <Typography
            variant="body2"
            sx={{ display: { xs: 'none', sm: 'block' }, mr: 1 }}
            title={user.email}
          >
            {user.fullName}
            {user.role === 'ADMIN' && ' (admin)'}
          </Typography>
          <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
            Log out
          </Button>
        </Toolbar>
      </AppBar>

      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)}>
        <Box sx={{ width: 260 }} role="presentation" onClick={() => setDrawerOpen(false)}>
          <Box sx={{ p: 2 }}>
            <Logo />
          </Box>
          <Divider />
          <List component="nav">
            {items.map((item) => (
              <ListItemButton key={item.to} component={NavLink} to={item.to}>
                <ListItemText primary={item.label} />
              </ListItemButton>
            ))}
          </List>
        </Box>
      </Drawer>

      <Container component="main" maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
