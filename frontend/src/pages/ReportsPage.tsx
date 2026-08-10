import { Typography } from '@mui/material';
import EmptyState from '../components/EmptyState';

export default function ReportsPage() {
  return (
    <>
      <Typography variant="h4" sx={{ mb: 3 }}>Reports</Typography>
      <EmptyState title="Coming Soon" message="Reporting dashboards will be implemented in Sprint 3D." />
    </>
  );
}
