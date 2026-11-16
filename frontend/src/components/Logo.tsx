import { Box, Typography } from '@mui/material';

type LogoProps = {
  variant?: 'light' | 'onPrimary';
  size?: number;
  showWordmark?: boolean;
};

export function Logo({ variant = 'light', size = 32, showWordmark = true }: LogoProps) {
  const color = variant === 'onPrimary' ? '#ffffff' : '#3949ab';
  const textColor = variant === 'onPrimary' ? '#ffffff' : '#212121';

  return (
    <Box
      sx={{ display: 'inline-flex', alignItems: 'center', gap: 1 }}
      role="img"
      aria-label="UBB Events"
    >
      <svg width={size} height={size} viewBox="0 0 32 32" fill="none" aria-hidden="true">
        <rect x="4" y="7" width="24" height="21" rx="4" stroke={color} strokeWidth="2.5" />
        <path d="M10 4V10M22 4V10M4 13H28" stroke={color} strokeWidth="2.5" strokeLinecap="round" />
        <path
          d="M10 20L14 24L22 16"
          stroke={color}
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>

      {showWordmark && (
        <Typography
          component="span"
          sx={{
            color: textColor,
            fontWeight: 700,
            fontSize: '1.1rem',
            letterSpacing: '-0.02em',
          }}
        >
          UBB Events
        </Typography>
      )}
    </Box>
  );
}
