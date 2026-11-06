import { Box, Paper, Typography } from '@mui/material';
import ConstructionIcon from '@mui/icons-material/Construction';

interface ComingSoonProps {
  title: string;
  /** Workflow id from CLAUDE.md, e.g. "W2". */
  workflow?: string;
  description?: string;
}

/** Placeholder for pages that are not implemented yet. */
export function ComingSoon({ title, workflow, description }: ComingSoonProps) {
  return (
    <Box>
      <Typography component="h1" variant="h4" sx={{ mb: 3 }}>
        {title}
      </Typography>
      <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
        <ConstructionIcon color="action" sx={{ fontSize: 48, mb: 1 }} />
        <Typography variant="h6">Coming soon</Typography>
        {description && <Typography color="text.secondary">{description}</Typography>}
        {workflow && (
          <Typography variant="caption" color="text.secondary">
            Workflow {workflow}
          </Typography>
        )}
      </Paper>
    </Box>
  );
}
