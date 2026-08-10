import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Tooltip,
  Divider,
} from '@mui/material';
import {
  AutoAwesome,
  TrendingUp,
  TrendingDown,
  TrendingFlat,
  Warning,
  CheckCircle,
  LocalShipping,
  Psychology,
  InfoOutlined,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getAiInsightsSummary, AiInsightsSummaryDto } from '../api/aiApi';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorAlert from '../components/ErrorAlert';

export default function AiInsightsPage() {
  const { data, isLoading, error } = useQuery<AiInsightsSummaryDto>({
    queryKey: ['aiInsightsSummary'],
    queryFn: getAiInsightsSummary,
    refetchInterval: 30000,
  });

  if (isLoading) return <LoadingSpinner message="Loading AI Machine Learning insights..." />;
  if (error) return <ErrorAlert error={error} title="Failed to load AI Insights" />;
  if (!data) return null;

  return (
    <Box sx={{ pb: 6 }}>
      {/* Header Banner */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
        <Psychology sx={{ fontSize: 36, color: 'primary.main' }} />
        <Box>
          <Typography variant="h4" fontWeight={700}>
            AI Insights
          </Typography>
          <Typography variant="body2" color="text.secondary">
            AI-Assisted Pharmacy Decision Support
          </Typography>
        </Box>
      </Box>

      {/* Advisory Banner */}
      <Alert severity="info" icon={<InfoOutlined />} sx={{ mb: 3 }}>
        <strong>Architecture Notice:</strong> This AI module serves as an advisory decision-support layer. The deterministic ADCE Safety Core engine continues to make the final safety decisions (FEFO, expiry checks, prescriptions) before any dispensing occurs.
      </Alert>

      {/* Top Stat Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card sx={{ bgcolor: 'background.paper', borderLeft: '5px solid #1976d2' }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary" fontWeight={600}>
                AI Engine
              </Typography>
              <Typography variant="h4" fontWeight={700} color="primary.main" sx={{ my: 1 }}>
                {data.aiEngineStatus}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Predictive Services Status
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, sm: 4 }}>
          <Card sx={{ bgcolor: 'background.paper', borderLeft: '5px solid #d32f2f' }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary" fontWeight={600}>
                Units at Risk
              </Typography>
              <Typography variant="h4" fontWeight={700} color="error.main" sx={{ my: 1 }}>
                {data.totalUnitsAtRisk}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Stock exceeding predicted velocity
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, sm: 4 }}>
          <Card sx={{ bgcolor: 'background.paper', borderLeft: '5px solid #ed6c02' }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary" fontWeight={600}>
                Recommended Orders
              </Typography>
              <Typography variant="h4" fontWeight={700} color="warning.main" sx={{ my: 1 }}>
                {data.totalRecommendedOrders}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Total units to procure
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* MODEL TRANSPARENCY */}
      <Card sx={{ mb: 4, bgcolor: '#f4f6f8' }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" fontWeight={700} sx={{ mb: 2 }}>
            🔬 MODEL TRANSPARENCY
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <Typography variant="body2"><strong>Algorithm:</strong> Scikit-Learn Linear Regression with bounded extrapolation</Typography>
              <Typography variant="body2"><strong>Historical window:</strong> 14 days (minimum 3 required)</Typography>
              <Typography variant="body2"><strong>Forecast horizon:</strong> 30 days</Typography>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <Typography variant="body2"><strong>R² Score (Avg):</strong> {data.demandForecasts.length > 0 ? (data.demandForecasts.reduce((sum, f) => sum + (f.r2Score || 0), 0) / data.demandForecasts.length).toFixed(2) : '0.00'}</Typography>
              <Typography variant="body2">
                <strong>Forecast status:</strong> {data.aiEngineStatus === 'UP' ? 'MODEL_AVAILABLE' : 'DEGRADED / INSUFFICIENT_DATA'}
              </Typography>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION 1: ML Demand Forecasting */}
      <Card sx={{ mb: 4 }}>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <Typography variant="h6" fontWeight={700}>
              🔮 DEMAND FORECAST
            </Typography>
          </Box>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead sx={{ bgcolor: '#f8fafc' }}>
                <TableRow>
                  <TableCell fontWeight={700}>Medicine</TableCell>
                  <TableCell align="right">Avg/day</TableCell>
                  <TableCell align="right">30-Day Forecast</TableCell>
                  <TableCell align="center">Trend</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.demandForecasts.map((f) => (
                  <TableRow key={f.medicineId} hover>
                    <TableCell fontWeight={600}>{f.medicineName}</TableCell>
                    <TableCell align="right">{f.dailyDemandAverage} units</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      {f.forecasted30DayDemand} units
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        icon={
                          f.trend === 'INCREASING' ? (
                            <TrendingUp fontSize="small" />
                          ) : f.trend === 'DECREASING' ? (
                            <TrendingDown fontSize="small" />
                          ) : (
                            <TrendingFlat fontSize="small" />
                          )
                        }
                        label={f.trend}
                        color={
                          f.trend === 'INCREASING'
                            ? 'success'
                            : f.trend === 'DECREASING'
                            ? 'warning'
                            : 'default'
                        }
                        variant="outlined"
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* SECTION 2: Expiry-Waste Risk Assessment */}
      <Card sx={{ mb: 4 }}>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <Typography variant="h6" fontWeight={700}>
              ⚠️ EXPIRY-WASTE RISK
            </Typography>
          </Box>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead sx={{ bgcolor: '#f8fafc' }}>
                <TableRow>
                  <TableCell>Batch</TableCell>
                  <TableCell>Medicine</TableCell>
                  <TableCell align="right">Units Risk</TableCell>
                  <TableCell align="center">Severity</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.expiryWasteRisks.map((r) => (
                  <TableRow key={r.batchId} hover>
                    <TableCell fontWeight={600}>{r.batchNumber}</TableCell>
                    <TableCell>{r.medicineName}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700, color: r.unitsAtRisk > 0 ? 'error.main' : 'text.primary' }}>
                      {r.unitsAtRisk}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={r.riskLevel}
                        color={r.riskLevel === 'HIGH' ? 'error' : r.riskLevel === 'MEDIUM' ? 'warning' : 'success'}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* SECTION 3: Smart Procurement Recommendations */}
      <Card>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <Typography variant="h6" fontWeight={700}>
              📦 SMART PROCUREMENT
            </Typography>
          </Box>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead sx={{ bgcolor: '#f8fafc' }}>
                <TableRow>
                  <TableCell>Medicine</TableCell>
                  <TableCell align="right">Stock</TableCell>
                  <TableCell align="right">Demand</TableCell>
                  <TableCell align="right">Order Qty</TableCell>
                  <TableCell align="center">Urgency</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.procurementRecommendations.map((p) => (
                  <TableRow key={p.medicineId} hover>
                    <TableCell fontWeight={600}>{p.medicineName}</TableCell>
                    <TableCell align="right">{p.currentUsableStock}</TableCell>
                    <TableCell align="right">{p.projected30DayDemand}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700, color: p.recommendedOrderQuantity > 0 ? 'warning.dark' : 'success.main' }}>
                      {p.recommendedOrderQuantity} units
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={p.urgency}
                        color={p.urgency === 'CRITICAL' ? 'error' : p.urgency === 'WARNING' ? 'warning' : 'success'}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  );
}
