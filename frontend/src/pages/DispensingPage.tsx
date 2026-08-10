import { Typography } from '@mui/material';
import EmptyState from '../components/EmptyState';

export default function DispensingPage() {
  return (
    <>
      <Typography variant="h4" sx={{ mb: 3 }}>Dispensing</Typography>
      <EmptyState title="Coming Soon" message="Dispensing workflows will be implemented in Sprint 3C." />
    </>
  );
}
