import { useState } from 'react';
import {
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TablePagination, TextField, Button, Chip,
  Grid, Card, CircularProgress, Alert, IconButton,
} from '@mui/material';
import {
  FilterList as FilterIcon,
  FileDownloadOutlined as ExportIcon,
  HistoryOutlined as RecentIcon,
  Search as SearchIcon,
  Clear as ClearIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getRecentEvents, getAuditEvents } from '../api/auditApi';
import { getAuditReport, exportAuditExcel } from '../api/reportApi';
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

function EventTypeChip({ type }: { type: string }) {
  let color: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' = 'default';
  if (type.includes('FAIL') || type.includes('ERROR') || type.includes('RECALL')) {
    color = 'error';
  } else if (type.includes('OVERRIDE') || type.includes('WARN')) {
    color = 'warning';
  } else if (type.includes('DISPENSE') || type.includes('CREATE')) {
    color = 'success';
  } else if (type.includes('LOGIN') || type.includes('AUTH')) {
    color = 'info';
  }

  return <Chip size="small" label={type} color={color} variant="outlined" sx={{ fontWeight: 600, fontSize: '0.72rem' }} />;
}

export default function AuditPage() {
  const defaults = getDefaultDates();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  // Filter state for report/export
  const [fromDate, setFromDate] = useState(defaults.from);
  const [toDate, setToDate] = useState(defaults.to);
  const [performedByFilter, setPerformedByFilter] = useState('');
  const [eventTypeFilter, setEventTypeFilter] = useState('');

  // Active filter params for the filtered report query
  const [activeFilterParams, setActiveFilterParams] = useState<{
    from: string;
    to: string;
    performedBy?: string;
    eventType?: string;
  } | null>(null);

  const [exportingExcel, setExportingExcel] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);

  // --- Queries ---
  // 1. Recent Events widget
  const { data: recentEvents, isLoading: recentLoading } = useQuery({
    queryKey: ['auditEventsRecent'],
    queryFn: getRecentEvents,
    staleTime: 30_000,
  });

  // 2. Paginated Audit Events (Default view)
  const { data: auditEventsPage, isLoading: eventsLoading } = useQuery({
    queryKey: ['auditEvents', page, size],
    queryFn: () => getAuditEvents(page, size),
    staleTime: 30_000,
  });

  // 3. Filtered Audit Report (Triggered on search)
  const { data: filteredReportRows, isLoading: reportLoading } = useQuery({
    queryKey: ['auditReportFiltered', activeFilterParams],
    queryFn: () => getAuditReport(activeFilterParams!),
    enabled: activeFilterParams !== null,
    staleTime: 30_000,
  });

  const handleApplyFilter = () => {
    setActiveFilterParams({
      from: fromDate,
      to: toDate,
      performedBy: performedByFilter.trim() || undefined,
      eventType: eventTypeFilter.trim() || undefined,
    });
  };

  const handleClearFilter = () => {
    setActiveFilterParams(null);
    setPerformedByFilter('');
    setEventTypeFilter('');
  };

  const handleExportExcel = async () => {
    try {
      setExportingExcel(true);
      setExportError(null);
      await exportAuditExcel({
        from: fromDate,
        to: toDate,
        performedBy: performedByFilter.trim() || undefined,
        eventType: eventTypeFilter.trim() || undefined,
      });
    } catch (err: any) {
      setExportError(err?.message || 'Failed to export audit report Excel file.');
    } finally {
      setExportingExcel(false);
    }
  };

  return (
    <>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 600, mb: 0.5 }}>
          System Audit Trail
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Security events, operational logs, overrides, and regulatory compliance records.
        </Typography>
      </Box>

      {exportError && (
        <Alert severity="error" onClose={() => setExportError(null)} sx={{ mb: 3 }}>
          {exportError}
        </Alert>
      )}

      {/* Widget: Recent Events Monitoring */}
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3, bgcolor: 'grey.50' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <RecentIcon color="primary" />
          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
            Recent Audit Stream
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
            (Latest security & system events across all sessions)
          </Typography>
        </Box>
        {recentLoading ? (
          <CircularProgress size={24} />
        ) : recentEvents && recentEvents.length > 0 ? (
          <Grid container spacing={1.5}>
            {recentEvents.slice(0, 4).map(evt => (
              <Grid size={{ xs: 12, sm: 6, md: 3 }} key={evt.id}>
                <Card variant="outlined" sx={{ bgcolor: 'background.paper', p: 1.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <EventTypeChip type={evt.eventType} />
                    <Typography variant="caption" color="text.secondary">
                      {new Date(evt.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ fontWeight: 500 }} noWrap>
                    {evt.description}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                    By: {evt.performedBy}
                  </Typography>
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : (
          <Typography variant="body2" color="text.secondary">No recent events recorded.</Typography>
        )}
      </Paper>

      {/* Filter Toolbar */}
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <FilterIcon fontSize="small" color="action" />
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            Audit Report Filters & Export
          </Typography>
        </Box>
        <Grid container spacing={2} sx={{ alignItems: 'center' }}>
          <Grid size={{ xs: 12, sm: 6, md: 2.5 }}>
            <TextField
              fullWidth
              label="From Date"
              type="date"
              size="small"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 2.5 }}>
            <TextField
              fullWidth
              label="To Date"
              type="date"
              size="small"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 2.5 }}>
            <TextField
              fullWidth
              label="Performed By"
              placeholder="e.g. pharmacist"
              size="small"
              value={performedByFilter}
              onChange={(e) => setPerformedByFilter(e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 2.5 }}>
            <TextField
              fullWidth
              label="Event Type"
              placeholder="e.g. FEFO_OVERRIDE"
              size="small"
              value={eventTypeFilter}
              onChange={(e) => setEventTypeFilter(e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }} sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant="contained"
              size="medium"
              startIcon={<SearchIcon />}
              onClick={handleApplyFilter}
              fullWidth
            >
              Filter
            </Button>
            {activeFilterParams && (
              <IconButton onClick={handleClearFilter} color="secondary" title="Clear Filters">
                <ClearIcon />
              </IconButton>
            )}
          </Grid>
        </Grid>
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="outlined"
            color="success"
            startIcon={exportingExcel ? <CircularProgress size={16} color="inherit" /> : <ExportIcon />}
            onClick={handleExportExcel}
            disabled={exportingExcel}
          >
            Export Filtered Excel (.xlsx)
          </Button>
        </Box>
      </Paper>

      {/* Audit Log Table */}
      {activeFilterParams ? (
        // Filtered View (`AuditReportRow`)
        <Paper variant="outlined">
          <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              Filtered Audit Report ({filteredReportRows?.length ?? 0} events)
            </Typography>
            <Chip size="small" label={`Range: ${fromDate} to ${toDate}`} color="info" />
          </Box>
          {reportLoading ? (
            <LoadingSpinner message="Filtering audit events..." />
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell>Timestamp</TableCell>
                    <TableCell>Event Type</TableCell>
                    <TableCell>Performed By</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>IP Address</TableCell>
                    <TableCell>Metadata</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {!filteredReportRows || filteredReportRows.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                        No audit events match the specified filter criteria.
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredReportRows.map(row => (
                      <TableRow key={row.id} hover>
                        <TableCell sx={{ whiteSpace: 'nowrap', fontSize: '0.78rem' }}>
                          {new Date(row.createdAt).toLocaleString()}
                        </TableCell>
                        <TableCell><EventTypeChip type={row.eventType} /></TableCell>
                        <TableCell sx={{ fontWeight: 500 }}>{row.performedBy}</TableCell>
                        <TableCell>{row.description}</TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.78rem' }}>{row.ipAddress || '—'}</TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.78rem', color: 'text.secondary' }}>
                          {row.metadata || '—'}
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
        // Paginated Default View (`AuditEventDto`)
        <Paper variant="outlined">
          <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              All System Audit Logs
            </Typography>
          </Box>
          {eventsLoading ? (
            <LoadingSpinner message="Loading audit logs..." />
          ) : (
            <>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow sx={{ bgcolor: 'grey.50' }}>
                      <TableCell>Timestamp</TableCell>
                      <TableCell>Event Type</TableCell>
                      <TableCell>Performed By</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>IP Address</TableCell>
                      <TableCell>Metadata</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {!auditEventsPage?.content || auditEventsPage.content.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                          No audit events recorded in system.
                        </TableCell>
                      </TableRow>
                    ) : (
                      auditEventsPage.content.map(evt => (
                        <TableRow key={evt.id} hover>
                          <TableCell sx={{ whiteSpace: 'nowrap', fontSize: '0.78rem' }}>
                            {new Date(evt.createdAt).toLocaleString()}
                          </TableCell>
                          <TableCell><EventTypeChip type={evt.eventType} /></TableCell>
                          <TableCell sx={{ fontWeight: 500 }}>{evt.performedBy}</TableCell>
                          <TableCell>{evt.description}</TableCell>
                          <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.78rem' }}>{evt.ipAddress || '—'}</TableCell>
                          <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.78rem', color: 'text.secondary' }}>
                            {evt.metadata || '—'}
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
              <TablePagination
                component="div"
                count={auditEventsPage?.totalElements || 0}
                page={page}
                onPageChange={(_, newPage) => setPage(newPage)}
                rowsPerPage={size}
                onRowsPerPageChange={(event) => {
                  setSize(parseInt(event.target.value, 10));
                  setPage(0);
                }}
                rowsPerPageOptions={[10, 20, 50]}
              />
            </>
          )}
        </Paper>
      )}
    </>
  );
}
