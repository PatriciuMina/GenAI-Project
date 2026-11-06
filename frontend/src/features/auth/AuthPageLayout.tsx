import type { ReactNode } from 'react';
import { Box, Container, Paper, Typography } from '@mui/material';
import { Logo } from '../../components/Logo';

/** Centered card used by the login and register pages. */
export function AuthPageLayout({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Container maxWidth="xs" sx={{ py: { xs: 4, sm: 8 } }}>
      <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
        <Logo />
      </Box>
      <Paper sx={{ p: { xs: 3, sm: 4 } }} variant="outlined">
        <Typography component="h1" variant="h5" sx={{ mb: 3 }}>
          {title}
        </Typography>
        {children}
      </Paper>
    </Container>
  );
}
