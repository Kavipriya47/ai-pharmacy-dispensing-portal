import { Box, Typography } from '@mui/material';
import InboxIcon from '@mui/icons-material/Inbox';

interface EmptyStateProps {
  title?: string;
  message?: string;
}

export default function EmptyState({
  title = 'No data',
  message = 'There are no items to display.',
}: EmptyStateProps) {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        py: 8,
        gap: 1,
        color: 'text.secondary',
      }}
    >
      <InboxIcon sx={{ fontSize: 64, opacity: 0.3 }} />
      <Typography variant="h6">{title}</Typography>
      <Typography variant="body2">{message}</Typography>
    </Box>
  );
}
