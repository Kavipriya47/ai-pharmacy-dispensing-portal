import { Typography } from '@mui/material';
import EmptyState from '../components/EmptyState';

export default function AuditPage() {
  return (
    <>
      <Typography variant="h4" sx={{ mb: 3 }}>Audit Trail</Typography>
      <EmptyState title="Coming Soon" message="Audit trail viewing will be implemented in Sprint 3D." />
    </>
  );
}
