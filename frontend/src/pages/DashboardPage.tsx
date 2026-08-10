import { Box, Card, CardContent, Typography, Grid } from '@mui/material';
import {
  MedicalServices,
  Inventory2,
  LocalShipping,
  Warning,
  ReportProblem,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import apiClient from '../api/axiosClient';
import type { InventoryDashboardDto } from '../types/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';

function StatCard({
  title,
  value,
  icon,
  color = 'primary.main',
}: {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  color?: string;
}) {
  return (
    <Card>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 3 }}>
        <Box
          sx={{
            width: 52,
            height: 52,
            borderRadius: 2,
            bgcolor: `${color}15`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: color,
          }}
        >
          {icon}
        </Box>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            {value}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {title}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const { data, isLoading, error } = useQuery<InventoryDashboardDto>({
    queryKey: ['inventoryDashboard'],
    queryFn: async () => {
      const res = await apiClient.get<InventoryDashboardDto>('/api/v1/reports/inventory/dashboard');
      return res.data;
    },
  });

  if (isLoading) return <LoadingSpinner message="Loading dashboard..." />;
  if (error) return <ErrorAlert error={error} title="Failed to load dashboard" />;
  if (!data) return null;

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>
        Dashboard
      </Typography>
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Total Medicines"
            value={data.totalMedicineCount}
            icon={<MedicalServices />}
            color="#0d7c66"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Active Batches"
            value={data.activeBatchCount}
            icon={<Inventory2 />}
            color="#3a86a8"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Total Stock"
            value={data.totalLiveStock}
            icon={<LocalShipping />}
            color="#2e7d32"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Low Stock"
            value={data.lowStockCount}
            icon={<Warning />}
            color="#ed6c02"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard
            title="Recalled Batches"
            value={data.recalledBatchCount}
            icon={<ReportProblem />}
            color="#9c27b0"
          />
        </Grid>
      </Grid>
    </Box>
  );
}
