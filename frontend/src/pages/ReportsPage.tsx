import React, { useState } from 'react';
import {
  Typography, Box, Tabs, Tab, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Card, CardContent, Grid,
  TextField, Tooltip, IconButton, Alert, CircularProgress, Chip,
} from '@mui/material';
import {
  AssessmentOutlined as AssessmentIcon,
  Inventory2Outlined as InventoryIcon,
  FileDownloadOutlined as ExportIcon,
  PictureAsPdfOutlined as PdfIcon,
  CheckCircleOutlined as CheckIcon,
  CancelOutlined as CancelIcon,
  ErrorOutlined as ErrorIcon,
  LocalShippingOutlined as QtyIcon,
  HelpOutlined as HelpIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import {
  getDispensingSummary,
  getDispensingByMedicine,
  getDispensingByPharmacist,
  getInventoryDashboard,
  getRecallHistory,
  exportDispensingExcel,
  exportDispensingPdf,
} from '../api/reportApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';
import LoadingSpinner from '../components/LoadingSpinner';

function getDefaultDates() {
  const today = new Date();
  const thirtyDaysAgo = new Date();
  thirtyDaysAgo.setDate(today.getDate() - 30);
  return {
    from: thirtyDaysAgo.toISOString().split('T')[0],
    to: today.toISOString().split('T')[0],
  };
}

interface StatCardProps {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  color: string;
  subtitle?: string;
  tooltip?: string;
}

function StatCard({ title, value, icon, color, subtitle, tooltip }: StatCardProps) {
  return (
    <Card variant="outlined">
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 2.5 }}>
        <Box
          sx={{
            width: 48,
            height: 48,
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
        <Box sx={{ flexGrow: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Typography variant="body2" color="text.secondary">{title}</Typography>
            {tooltip && (
              <Tooltip title={tooltip} arrow>
                <HelpIcon sx={{ fontSize: 14, color: 'text.secondary', cursor: 'pointer' }} />
              </Tooltip>
            )}
          </Box>
          <Typography variant="h5" sx={{ fontWeight: 700, mt: 0.5 }}>
            {value}
          </Typography>
          {subtitle && (
            <Typography variant="caption" color="text.secondary">{subtitle}</Typography>
          )}
        </Box>
      </CardContent>
    </Card>
  );
}

export default function ReportsPage() {
  const { user } = useAuth();
  const canViewDetailed = user?.roles?.some(r => r === ROLES.ADMIN || r === ROLES.AUDITOR);

  const defaults = getDefaultDates();
  const [fromDate, setFromDate] = useState(defaults.from);
  const [toDate, setToDate] = useState(defaults.to);
  const [activeTab, setActiveTab] = useState(0);

  const [exportingExcel, setExportingExcel] = useState(false);
  const [exportingPdf, setExportingPdf] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);

  // --- Queries ---
  const {
    data: summary,
    isLoading: summaryLoading,
    refetch: refetchSummary,
  } = useQuery({
    queryKey: ['dispensingSummary', fromDate, toDate],
    queryFn: () => getDispensingSummary(fromDate, toDate),
    staleTime: 60_000,
  });

  const { data: byMedicine, isLoading: byMedLoading } = useQuery({
    queryKey: ['dispensingByMedicine', fromDate, toDate],
    queryFn: () => getDispensingByMedicine(fromDate, toDate),
    enabled: canViewDetailed && activeTab === 0,
    staleTime: 60_000,
  });

  const { data: byPharmacist, isLoading: byPharmLoading } = useQuery({
    queryKey: ['dispensingByPharmacist', fromDate, toDate],
    queryFn: () => getDispensingByPharmacist(fromDate, toDate),
    enabled: canViewDetailed && activeTab === 0,
    staleTime: 60_000,
  });

  const { data: invDashboard, isLoading: invLoading } = useQuery({
    queryKey: ['inventoryDashboardReport'],
    queryFn: getInventoryDashboard,
    enabled: activeTab === 1,
    staleTime: 60_000,
  });

  const { data: recallHistory, isLoading: recallLoading } = useQuery({
    queryKey: ['recallHistory'],
    queryFn: getRecallHistory,
    enabled: canViewDetailed && activeTab === 1,
    staleTime: 60_000,
  });

  // --- Exports ---
  const handleExportExcel = async () => {
    try {
      setExportingExcel(true);
      setExportError(null);
      await exportDispensingExcel(fromDate, toDate);
    } catch (err: any) {
      setExportError(err?.message || 'Failed to export Excel report.');
    } finally {
      setExportingExcel(false);
    }
  };

  const handleExportPdf = async () => {
    try {
      setExportingPdf(true);
      setExportError(null);
      await exportDispensingPdf(fromDate, toDate);
    } catch (err: any) {
      setExportError(err?.message || 'Failed to export PDF report.');
    } finally {
      setExportingPdf(false);
    }
  };

  return (
    <>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 600, mb: 0.5 }}>
            Analytics & Reports
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Operational dispensing metrics, inventory positioning, and compliance audit exports.
          </Typography>
        </Box>

        {/* Global Date Range Controls */}
        <Paper variant="outlined" sx={{ p: 1.5, display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <TextField
            label="From"
            type="date"
            size="small"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="To"
            type="date"
            size="small"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <Tooltip title="Refresh metrics for date range">
            <IconButton onClick={() => refetchSummary()} size="small" color="primary">
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Paper>
      </Box>

      {exportError && (
        <Alert severity="error" onClose={() => setExportError(null)} sx={{ mb: 3 }}>
          {exportError}
        </Alert>
      )}

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab icon={<AssessmentIcon />} iconPosition="start" label="Dispensing Reports" />
          <Tab icon={<InventoryIcon />} iconPosition="start" label="Inventory & Recalls" />
        </Tabs>
      </Box>

      {/* ==================== TAB 0: DISPENSING ==================== */}
      {activeTab === 0 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* Summary Stat Cards */}
          {summaryLoading ? (
            <LoadingSpinner message="Loading dispensing summary..." />
          ) : summary ? (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <StatCard
                  title="Completed"
                  value={summary.completedCount}
                  icon={<CheckIcon />}
                  color="#2e7d32"
                  tooltip="DispensationRecords with status = COMPLETED"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <StatCard
                  title="Cancelled"
                  value={summary.cancelledCount}
                  icon={<CancelIcon />}
                  color="#757575"
                  tooltip="DispensationRecords with status = CANCELLED"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <StatCard
                  title="Failed Attempts"
                  value={summary.failedCount}
                  icon={<ErrorIcon />}
                  color="#d32f2f"
                  tooltip={summary.semanticsNote || "Audit events of type DISPENSE_FAILED (no DispensationRecord)"}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <StatCard
                  title="Units Dispensed"
                  value={summary.totalQuantityDispensed}
                  icon={<QtyIcon />}
                  color="#0d7c66"
                  subtitle="Total quantity dispensed in range"
                />
              </Grid>
            </Grid>
          ) : null}

          {/* Export Bar for ADMIN / AUDITOR */}
          {canViewDetailed && (
            <Paper variant="outlined" sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box>
                <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Export Dispensing Report</Typography>
                <Typography variant="caption" color="text.secondary">
                  Download full dispensation audit report for period {fromDate} to {toDate}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 1.5 }}>
                <Button
                  variant="outlined"
                  color="success"
                  startIcon={exportingExcel ? <CircularProgress size={16} color="inherit" /> : <ExportIcon />}
                  onClick={handleExportExcel}
                  disabled={exportingExcel}
                >
                  Export Excel (.xlsx)
                </Button>
                <Button
                  variant="outlined"
                  color="error"
                  startIcon={exportingPdf ? <CircularProgress size={16} color="inherit" /> : <PdfIcon />}
                  onClick={handleExportPdf}
                  disabled={exportingPdf}
                >
                  Export PDF (.pdf)
                </Button>
              </Box>
            </Paper>
          )}

          {/* Detailed Breakdowns for ADMIN / AUDITOR */}
          {canViewDetailed ? (
            <Grid container spacing={3}>
              {/* By Medicine */}
              <Grid size={{ xs: 12, md: 7 }}>
                <Paper variant="outlined" sx={{ p: 2.5 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                    Dispensing by Medicine
                  </Typography>
                  {byMedLoading ? (
                    <LoadingSpinner message="Loading medicine breakdown..." />
                  ) : (
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow sx={{ bgcolor: 'grey.50' }}>
                            <TableCell>Medicine Name</TableCell>
                            <TableCell align="right">Dispense Count</TableCell>
                            <TableCell align="right">Total Units</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {!byMedicine || byMedicine.length === 0 ? (
                            <TableRow>
                              <TableCell colSpan={3} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                                No dispensing activity in selected date range.
                              </TableCell>
                            </TableRow>
                          ) : (
                            byMedicine.map(([medName, count, totalQty], idx) => (
                              <TableRow key={idx} hover>
                                <TableCell sx={{ fontWeight: 500 }}>{medName}</TableCell>
                                <TableCell align="right">{count}</TableCell>
                                <TableCell align="right">{totalQty}</TableCell>
                              </TableRow>
                            ))
                          )}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  )}
                </Paper>
              </Grid>

              {/* By Pharmacist */}
              <Grid size={{ xs: 12, md: 5 }}>
                <Paper variant="outlined" sx={{ p: 2.5 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                    Dispensing by Pharmacist
                  </Typography>
                  {byPharmLoading ? (
                    <LoadingSpinner message="Loading pharmacist breakdown..." />
                  ) : (
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow sx={{ bgcolor: 'grey.50' }}>
                            <TableCell>Pharmacist</TableCell>
                            <TableCell align="right">Dispense Count</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {!byPharmacist || byPharmacist.length === 0 ? (
                            <TableRow>
                              <TableCell colSpan={2} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                                No activity recorded.
                              </TableCell>
                            </TableRow>
                          ) : (
                            byPharmacist.map(([pharmName, count], idx) => (
                              <TableRow key={idx} hover>
                                <TableCell sx={{ fontWeight: 500 }}>{pharmName}</TableCell>
                                <TableCell align="right">{count}</TableCell>
                              </TableRow>
                            ))
                          )}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  )}
                </Paper>
              </Grid>
            </Grid>
          ) : (
            <Alert severity="info">
              Detailed medicine/pharmacist breakdowns and report exports are restricted to Administrators and Auditors.
            </Alert>
          )}
        </Box>
      )}

      {/* ==================== TAB 1: INVENTORY & RECALLS ==================== */}
      {activeTab === 1 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* Inventory Dashboard Summary Cards */}
          {invLoading ? (
            <LoadingSpinner message="Loading inventory dashboard..." />
          ) : invDashboard ? (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <StatCard title="Total Medicines" value={invDashboard.totalMedicines} icon={<AssessmentIcon />} color="#0d7c66" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <StatCard title="Active Batches" value={invDashboard.activeBatches} icon={<InventoryIcon />} color="#3a86a8" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <StatCard title="Total Stock Units" value={invDashboard.totalStockOnHand} icon={<QtyIcon />} color="#2e7d32" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <StatCard title="Low Stock Items" value={invDashboard.lowStockCount} icon={<ErrorIcon />} color="#ed6c02" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <StatCard title="Near Expiry Batches" value={invDashboard.nearExpiryBatches} icon={<ErrorIcon />} color="#d32f2f" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 4 }}>
                <StatCard title="Recalled Batches" value={invDashboard.recalledBatches} icon={<ErrorIcon />} color="#9c27b0" />
              </Grid>
            </Grid>
          ) : null}

          {/* Recall History Table */}
          {canViewDetailed ? (
            <Paper variant="outlined" sx={{ p: 2.5 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                Recall History Log
              </Typography>
              {recallLoading ? (
                <LoadingSpinner message="Loading recall history..." />
              ) : (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.50' }}>
                        <TableCell>Batch Number</TableCell>
                        <TableCell>Medicine Name</TableCell>
                        <TableCell>Recall Date</TableCell>
                        <TableCell>Recalled By</TableCell>
                        <TableCell>Reason</TableCell>
                        <TableCell align="right">Affected Dispensing Records</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {!recallHistory || recallHistory.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={6} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                            No batch recalls recorded.
                          </TableCell>
                        </TableRow>
                      ) : (
                        recallHistory.map((rec, idx) => (
                          <TableRow key={idx} hover>
                            <TableCell sx={{ fontFamily: 'monospace', fontWeight: 500 }}>{rec.batchNumber}</TableCell>
                            <TableCell>{rec.medicineName}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>
                              {rec.recallDate ? new Date(rec.recallDate).toLocaleString() : 'N/A'}
                            </TableCell>
                            <TableCell>{rec.recalledBy || 'N/A'}</TableCell>
                            <TableCell>{rec.reason || 'N/A'}</TableCell>
                            <TableCell align="right">
                              <Chip size="small" label={rec.affectedDispensationCount} color={rec.affectedDispensationCount > 0 ? 'error' : 'default'} />
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>
          ) : (
            <Alert severity="info">
              Detailed recall history log is restricted to Administrators and Auditors.
            </Alert>
          )}
        </Box>
      )}
    </>
  );
}
