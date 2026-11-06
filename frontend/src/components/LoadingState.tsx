import { Box, CircularProgress, Typography } from '@mui/material';

export function LoadingState({ label = 'Loading…' }: { label?: string }) {
  return (
    <Box
      role="status"
      sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, py: 6 }}
    >
      <CircularProgress />
      <Typography color="text.secondary">{label}</Typography>
    </Box>
  );
}
