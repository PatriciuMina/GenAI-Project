import { Box, Typography } from '@mui/material';

/** PLACEHOLDER logo: a teammate will design the real one (also public/logo-placeholder.svg). */
export function Logo({ color = 'primary.main' }: { color?: string }) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }} aria-label="UBB Events">
      <Box
        component="span"
        sx={{
          width: 32,
          height: 32,
          borderRadius: 1,
          bgcolor: color === 'primary.main' ? 'primary.main' : 'common.white',
          color: color === 'primary.main' ? 'primary.contrastText' : 'primary.main',
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 700,
          fontSize: 14,
        }}
        aria-hidden
      >
        UE
      </Box>
      <Typography component="span" variant="h6" sx={{ color, fontWeight: 700 }}>
        UBB Events
      </Typography>
    </Box>
  );
}
