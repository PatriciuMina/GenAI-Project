import type { ReactNode } from 'react';
import { Link as RouterLink } from 'react-router';
import { Box, Button, Paper, Stack, Typography } from '@mui/material';
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined';

export type EmptyStateAction = { label: string } & ({ to: string } | { onClick: () => void });

interface EmptyStateProps {
  message: string;
  icon?: ReactNode;
  action?: EmptyStateAction;
  /** Less padding, for a panel or table inside a page. */
  compact?: boolean;
  /** Without its own border, e.g. inside a table cell or a card that already has one. */
  plain?: boolean;
}

export function EmptyState({
  message,
  icon,
  action,
  compact = false,
  plain = false,
}: EmptyStateProps) {
  const content = (
    <Stack
      spacing={1.5}
      sx={{ alignItems: 'center', textAlign: 'center', py: compact ? 2 : 5, px: 2 }}
    >
      <Box
        sx={{ color: 'text.disabled', display: 'flex', '& svg': { fontSize: compact ? 32 : 48 } }}
      >
        {icon ?? <InboxOutlinedIcon />}
      </Box>
      <Typography color="text.secondary">{message}</Typography>
      {action &&
        ('to' in action ? (
          <Button component={RouterLink} to={action.to} variant="outlined" size="small">
            {action.label}
          </Button>
        ) : (
          <Button onClick={action.onClick} variant="outlined" size="small">
            {action.label}
          </Button>
        ))}
    </Stack>
  );
  return plain ? content : <Paper variant="outlined">{content}</Paper>;
}
