import { Box, Typography, Button } from '@mui/material';
import { Block } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

export default function ForbiddenPage() {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        gap: 2,
        textAlign: 'center',
      }}
    >
      <Block sx={{ fontSize: 80, color: 'error.main', opacity: 0.7 }} />
      <Typography variant="h4" sx={{ fontWeight: 700 }}>
        403 — Forbidden
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 400 }}>
        You do not have the required permissions to access this page.
      </Typography>
      <Button variant="contained" onClick={() => navigate('/')} sx={{ mt: 2 }}>
        Go to Dashboard
      </Button>
    </Box>
  );
}
