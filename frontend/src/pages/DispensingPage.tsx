import React, { useState } from 'react';
import {
  Typography, Box, Tabs, Tab, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, TablePagination, Chip,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, Alert,
  Select, MenuItem, FormControl, InputLabel, CircularProgress, Tooltip,
  InputAdornment, IconButton, Autocomplete, FormControlLabel, Checkbox,
  Divider,
} from '@mui/material';
import {
  MedicationOutlined as MedIcon,
  HistoryOutlined as HistoryIcon,
  PersonSearchOutlined as PersonSearchIcon,
  CheckCircleOutlined as CheckIcon,
  WarningAmberOutlined as WarnIcon,
  ErrorOutlined as ErrorIcon,
  Search as SearchIcon,
  Clear as ClearIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMedicines } from '../api/medicineApi';
import { getBatches } from '../api/inventoryApi';
import { dispense, getDispensations, getDispensationsByPatient } from '../api/dispensingApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';
import type {
  MedicineResponse, BatchResponse, DispenseRequest, DispensationResponse,
} from '../types/api';
import LoadingSpinner from '../components/LoadingSpinner';

// ---- Tab panel helper -------------------------------------------------------
interface TabPanelProps { children?: React.ReactNode; index: number; value: number; }
function TabPanel({ children, index, value }: TabPanelProps) {
  return (
    <div hidden={value !== index} role="tabpanel">
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

// ---- Chip helpers -----------------------------------------------------------
function StatusChip({ status }: { status: string }) {
  return (
    <Chip
      size="small"
      label={status}
      color={status === 'COMPLETED' ? 'success' : 'default'}
      icon={status === 'COMPLETED' ? <CheckIcon /> : undefined}
    />
  );
}

function FefoChip({ override }: { override: boolean }) {
  return override
    ? <Chip size="small" label="FEFO Override" color="warning" icon={<WarnIcon />} />
    : <Chip size="small" label="FEFO" color="info" />;
}

// ---- Dispensation history table (shared between History and Patient Lookup) --
interface HistoryTableProps {
  rows: DispensationResponse[];
  totalElements: number;
  page: number;
  size: number;
  onPageChange: (newPage: number) => void;
  onSizeChange: (newSize: number) => void;
  loading: boolean;
}
function HistoryTable({ rows, totalElements, page, size, onPageChange, onSizeChange, loading }: HistoryTableProps) {
  if (loading) return <LoadingSpinner />;
  return (
    <Paper variant="outlined">
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.50' }}>
              <TableCell>Date / Time</TableCell>
              <TableCell>Medicine</TableCell>
              <TableCell>Batch</TableCell>
              <TableCell>Patient</TableCell>
              <TableCell align="right">Qty</TableCell>
              <TableCell>Dispensed By</TableCell>
              <TableCell>FEFO</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No records found
                </TableCell>
              </TableRow>
            ) : rows.map(r => (
              <TableRow key={r.id} hover>
                <TableCell sx={{ whiteSpace: 'nowrap', fontSize: '0.78rem' }}>
                  {new Date(r.dispensedAt).toLocaleString()}
                </TableCell>
                <TableCell>{r.medicineName}</TableCell>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.82rem' }}>{r.batchNumber}</TableCell>
                <TableCell>{r.patientIdentifier}</TableCell>
                <TableCell align="right">{r.quantityDispensed}</TableCell>
                <TableCell>{r.dispensedBy}</TableCell>
                <TableCell><FefoChip override={r.fefoOverride} /></TableCell>
                <TableCell><StatusChip status={r.status} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={totalElements}
        page={page}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        rowsPerPage={size}
        onRowsPerPageChange={(event) => { onSizeChange(parseInt(event.target.value, 10)); onPageChange(0); }}
        rowsPerPageOptions={[10, 20, 50]}
      />
    </Paper>
  );
}

// ---- Main page --------------------------------------------------------------
export default function DispensingPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const canDispense = user?.roles?.some(r => r === ROLES.ADMIN || r === ROLES.PHARMACIST);

  const [activeTab, setActiveTab] = useState(canDispense ? 0 : 1);

  // ---------- Dispense form state -------------------------------------------
  const [selectedMedicine, setSelectedMedicine] = useState<MedicineResponse | null>(null);
  const [useFefo, setUseFefo] = useState(true);           // true = FEFO (default), false = manual override
  const [selectedBatch, setSelectedBatch] = useState<BatchResponse | null>(null);
  const [patientIdentifier, setPatientIdentifier] = useState('');
  const [prescriptionReference, setPrescriptionReference] = useState('');
  const [quantity, setQuantity] = useState<number | ''>('');
  const [overrideReason, setOverrideReason] = useState('');
  const [notes, setNotes] = useState('');
  const [dispenseError, setDispenseError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [successRecord, setSuccessRecord] = useState<DispensationResponse | null>(null);

  // Batch override pagination — load pages on demand
  const [batchPage, setBatchPage] = useState(0);
  const BATCH_PAGE_SIZE = 20;

  // ---------- History tab state ---------------------------------------------
  const [histPage, setHistPage] = useState(0);
  const [histSize, setHistSize] = useState(20);

  // ---------- Patient lookup state ------------------------------------------
  const [patientSearch, setPatientSearch] = useState('');
  const [patientQuery, setPatientQuery] = useState('');
  const [patLookupPage, setPatLookupPage] = useState(0);
  const [patLookupSize, setPatLookupSize] = useState(20);

  // ---------- Queries -------------------------------------------------------
  // Medicine list for autocomplete — reuses cache from MedicinesPage
  const { data: medicinesPage } = useQuery({
    queryKey: ['medicines', 0, 200],
    queryFn: () => getMedicines({ page: 0, size: 200 }),
    staleTime: 60_000,
  });

  // Batch list for manual FEFO override — paginated from the server
  // IMPORTANT: We never claim this represents ALL batches unless the user has paged through all.
  const {
    data: batchesPage,
    isFetching: batchesFetching,
  } = useQuery({
    queryKey: ['batches', batchPage, BATCH_PAGE_SIZE],
    queryFn: () => getBatches({ page: batchPage, size: BATCH_PAGE_SIZE }),
    staleTime: 30_000,
    enabled: !useFefo && selectedMedicine != null, // only fetch when manual mode is active
  });

  // Batches filtered client-side to selected medicine + ACTIVE status
  const eligibleBatches: BatchResponse[] = (batchesPage?.content ?? []).filter(
    b => b.medicineId === selectedMedicine?.id && b.status === 'ACTIVE',
  );
  const hasMoreBatchPages = batchesPage ? !batchesPage.last : false;

  // History query
  const { data: histPage_data, isLoading: histLoading } = useQuery({
    queryKey: ['dispensations', histPage, histSize],
    queryFn: () => getDispensations(histPage, histSize),
    staleTime: 30_000,
    enabled: activeTab === 1,
  });

  // Patient lookup query (only triggered when patientQuery is set)
  const { data: patientData, isLoading: patientLoading } = useQuery({
    queryKey: ['dispensations', 'patient', patientQuery, patLookupPage, patLookupSize],
    queryFn: () => getDispensationsByPatient(patientQuery, patLookupPage, patLookupSize),
    staleTime: 30_000,
    enabled: activeTab === 2 && patientQuery.trim().length > 0,
  });

  // ---------- Dispense mutation ---------------------------------------------
  const dispenseMutation = useMutation({
    mutationFn: (req: DispenseRequest) => dispense(req),
    onSuccess: (record) => {
      setSuccessRecord(record);
      setConfirmOpen(false);
      setDispenseError(null);
      // Invalidate history and inventory caches
      queryClient.invalidateQueries({ queryKey: ['dispensations'] });
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
      queryClient.invalidateQueries({ queryKey: ['stock-summary'] });
      // Reset form
      setSelectedMedicine(null);
      setSelectedBatch(null);
      setUseFefo(true);
      setPatientIdentifier('');
      setPrescriptionReference('');
      setQuantity('');
      setOverrideReason('');
      setNotes('');
    },
    onError: (err: any) => {
      setConfirmOpen(false);
      const detail = err?.response?.data?.detail ?? err?.message ?? 'Dispensing failed.';
      setDispenseError(detail);
    },
  });

  // ---------- Form validation (client-side pre-flight) ----------------------
  const prescriptionRequired = selectedMedicine?.requiresPrescription === true;
  const prescriptionMissing = prescriptionRequired && !prescriptionReference.trim();
  const overrideReasonMissing = !useFefo && !overrideReason.trim();
  const formValid =
    selectedMedicine != null &&
    patientIdentifier.trim().length > 0 &&
    quantity !== '' && quantity >= 1 &&
    !prescriptionMissing &&
    !overrideReasonMissing &&
    (useFefo || selectedBatch != null);

  const handleSubmitDispense = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formValid) return;
    setDispenseError(null);
    setConfirmOpen(true);
  };

  const handleConfirmDispense = () => {
    const req: DispenseRequest = {
      medicineId: selectedMedicine!.id,
      batchId: useFefo ? null : selectedBatch?.id,
      patientIdentifier: patientIdentifier.trim(),
      prescriptionReference: prescriptionReference.trim() || undefined,
      quantity: quantity as number,
      overrideReason: useFefo ? undefined : overrideReason.trim(),
      notes: notes.trim() || undefined,
    };
    dispenseMutation.mutate(req);
  };

  // ---- Error classification for targeted UI hints -------------------------
  const isPrescriptionError = dispenseError?.includes('PRESCRIPTION_REQUIRED');
  const isRecalledError = dispenseError?.includes('RECALLED');
  const isQuarantinedError = dispenseError?.includes('QUARANTINED');
  const isSafetyViolation = isRecalledError || isQuarantinedError;

  // ---------- UI ------------------------------------------------------------
  return (
    <>
      <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>Dispensing</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Dispense medications to patients and view dispensation history.
      </Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          {canDispense && (
            <Tab icon={<MedIcon />} iconPosition="start" label="Dispense" id="tab-dispense" />
          )}
          <Tab icon={<HistoryIcon />} iconPosition="start" label="History" id="tab-history" />
          <Tab icon={<PersonSearchIcon />} iconPosition="start" label="Patient Lookup" id="tab-patient" />
        </Tabs>
      </Box>

      {/* ===================== TAB 0: DISPENSE ============================= */}
      {canDispense && (
        <TabPanel value={activeTab} index={0}>
          {/* Success banner */}
          {successRecord && (
            <Alert
              severity="success"
              icon={<CheckIcon />}
              onClose={() => setSuccessRecord(null)}
              sx={{ mb: 3 }}
            >
              <strong>Dispensed successfully.</strong> Record ID: {successRecord.id} — {successRecord.quantityDispensed}×{' '}
              {successRecord.medicineName} dispensed to patient {successRecord.patientIdentifier} from batch{' '}
              {successRecord.batchNumber}.
            </Alert>
          )}

          {/* Prominent safety/error alert */}
          {dispenseError && (
            <Alert
              severity={isSafetyViolation ? 'error' : 'warning'}
              icon={isSafetyViolation ? <ErrorIcon /> : <WarnIcon />}
              onClose={() => setDispenseError(null)}
              sx={{ mb: 3 }}
            >
              <strong>{isSafetyViolation ? 'Safety Violation' : 'Dispensing Error'}</strong>
              <br />
              {dispenseError}
            </Alert>
          )}

          <Paper variant="outlined" sx={{ p: 3, maxWidth: 640 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>New Dispensation</Typography>
            <form onSubmit={handleSubmitDispense}>
              {/* Medicine selection */}
              <Autocomplete
                options={medicinesPage?.content ?? []}
                getOptionLabel={(m: MedicineResponse) => `${m.name} — ${m.genericName}`}
                value={selectedMedicine}
                onChange={(_, val) => {
                  setSelectedMedicine(val);
                  setSelectedBatch(null);
                  setUseFefo(true);
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Medicine *"
                    placeholder="Search by name or generic name"
                    sx={{ mb: 2 }}
                  />
                )}
                renderOption={(props, option) => (
                  <li {...props} key={option.id}>
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>{option.name}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {option.genericName} · {option.dosageForm} · {option.strength}
                        {option.requiresPrescription && (
                          <Chip label="Rx Required" size="small" color="warning" sx={{ ml: 1, height: 16, fontSize: '0.65rem' }} />
                        )}
                      </Typography>
                    </Box>
                  </li>
                )}
              />

              {/* FEFO mode toggle */}
              {selectedMedicine && (
                <Box sx={{ mb: 2, p: 2, bgcolor: useFefo ? 'info.50' : 'warning.50', borderRadius: 1, border: '1px solid', borderColor: useFefo ? 'info.200' : 'warning.200' }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={useFefo}
                        onChange={(e) => {
                          setUseFefo(e.target.checked);
                          setSelectedBatch(null);
                          setOverrideReason('');
                        }}
                      />
                    }
                    label={<Typography sx={{ fontWeight: 500 }}>Use FEFO — Recommended</Typography>}
                  />
                  {useFefo ? (
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 4 }}>
                      The system will automatically select the earliest-expiring eligible batch with sufficient stock.
                    </Typography>
                  ) : (
                    <Typography variant="body2" color="warning.main" sx={{ ml: 4 }}>
                      Manual batch selection overrides FEFO. An override reason is required and will be audit-logged.
                    </Typography>
                  )}
                </Box>
              )}

              {/* Manual batch selection — pagination-safe */}
              {!useFefo && selectedMedicine && (
                <Box sx={{ mb: 2 }}>
                  <FormControl fullWidth required error={!selectedBatch && !useFefo}>
                    <InputLabel>Select Batch (manual override)</InputLabel>
                    <Select
                      value={selectedBatch?.id ?? ''}
                      label="Select Batch (manual override)"
                      onChange={(e) => {
                        const found = eligibleBatches.find(b => b.id === e.target.value);
                        setSelectedBatch(found ?? null);
                      }}
                    >
                      {eligibleBatches.length === 0 && !batchesFetching && (
                        <MenuItem disabled value="">No ACTIVE batches found on this page</MenuItem>
                      )}
                      {eligibleBatches.map(b => (
                        <MenuItem key={b.id} value={b.id}>
                          {b.batchNumber} — expires {b.expiryDate} — {b.quantityRemaining} remaining
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                  {/* Pagination controls for batch list */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                    <Typography variant="caption" color="text.secondary">
                      Page {batchPage + 1}{batchesPage ? ` of ${batchesPage.totalPages}` : ''} —{' '}
                      showing ACTIVE batches for this medicine only
                    </Typography>
                    <Button
                      size="small"
                      disabled={batchPage === 0 || batchesFetching}
                      onClick={() => setBatchPage(p => Math.max(0, p - 1))}
                    >
                      Prev
                    </Button>
                    <Button
                      size="small"
                      disabled={!hasMoreBatchPages || batchesFetching}
                      onClick={() => setBatchPage(p => p + 1)}
                    >
                      Next
                    </Button>
                    {batchesFetching && <CircularProgress size={14} />}
                    {hasMoreBatchPages && (
                      <Tooltip title="More batches exist on subsequent pages. Use Next to load them.">
                        <Chip size="small" label="More pages available" color="warning" variant="outlined" />
                      </Tooltip>
                    )}
                  </Box>

                  <TextField
                    fullWidth
                    required
                    label="Override Reason *"
                    placeholder="Required when manually selecting a batch (will be audit-logged)"
                    multiline
                    rows={2}
                    value={overrideReason}
                    onChange={(e) => setOverrideReason(e.target.value)}
                    error={overrideReasonMissing}
                    helperText={overrideReasonMissing ? 'An override reason is required for manual batch selection' : ''}
                    sx={{ mt: 2 }}
                  />
                </Box>
              )}

              <Divider sx={{ my: 2 }} />

              {/* Patient identifier */}
              <TextField
                fullWidth
                required
                label="Patient Identifier *"
                value={patientIdentifier}
                onChange={(e) => setPatientIdentifier(e.target.value)}
                slotProps={{ htmlInput: { maxLength: 100 } }}
                sx={{ mb: 2 }}
              />

              {/* Prescription reference */}
              <TextField
                fullWidth
                required={prescriptionRequired}
                label={`Prescription Reference${prescriptionRequired ? ' *' : ''}`}
                value={prescriptionReference}
                onChange={(e) => setPrescriptionReference(e.target.value)}
                slotProps={{
                  htmlInput: { maxLength: 200 },
                  input: prescriptionRequired ? {
                    startAdornment: (
                      <InputAdornment position="start">
                        <WarnIcon color="warning" fontSize="small" />
                      </InputAdornment>
                    ),
                  } : undefined,
                }}
                error={isPrescriptionError || prescriptionMissing}
                helperText={
                  prescriptionMissing
                    ? 'This medicine requires a valid prescription reference'
                    : isPrescriptionError
                    ? dispenseError
                    : prescriptionRequired
                    ? 'Required — this medicine is prescription-only'
                    : 'Optional'
                }
                sx={{ mb: 2 }}
              />

              {/* Quantity */}
              <TextField
                fullWidth
                required
                type="number"
                label="Quantity *"
                value={quantity}
                onChange={(e) => setQuantity(parseInt(e.target.value, 10) || '')}
                slotProps={{ htmlInput: { min: 1, step: 1 } }}
                sx={{ mb: 2 }}
              />

              {/* Notes */}
              <TextField
                fullWidth
                label="Notes (optional)"
                multiline
                rows={2}
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                sx={{ mb: 3 }}
              />

              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={!formValid || dispenseMutation.isPending}
                fullWidth
              >
                {dispenseMutation.isPending ? <CircularProgress size={24} /> : 'Review & Dispense'}
              </Button>
            </form>
          </Paper>
        </TabPanel>
      )}

      {/* ===================== TAB 1: HISTORY =============================== */}
      <TabPanel value={activeTab} index={canDispense ? 1 : 0}>
        <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
          Dispensation History
        </Typography>
        <HistoryTable
          rows={histPage_data?.content ?? []}
          totalElements={histPage_data?.totalElements ?? 0}
          page={histPage}
          size={histSize}
          onPageChange={setHistPage}
          onSizeChange={setHistSize}
          loading={histLoading}
        />
      </TabPanel>

      {/* ===================== TAB 2: PATIENT LOOKUP ======================== */}
      <TabPanel value={activeTab} index={canDispense ? 2 : 1}>
        <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
          Patient Dispensation Lookup
        </Typography>
        <Box sx={{ display: 'flex', gap: 1, mb: 3, maxWidth: 480 }}>
          <TextField
            fullWidth
            label="Patient Identifier"
            value={patientSearch}
            onChange={(e) => setPatientSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && patientSearch.trim()) {
                setPatientQuery(patientSearch.trim());
                setPatLookupPage(0);
              }
            }}
            slotProps={{
              input: {
                endAdornment: patientSearch ? (
                  <InputAdornment position="end">
                    <IconButton size="small" onClick={() => { setPatientSearch(''); setPatientQuery(''); }}>
                      <ClearIcon />
                    </IconButton>
                  </InputAdornment>
                ) : null,
              },
            }}
          />
          <Button
            variant="contained"
            startIcon={<SearchIcon />}
            disabled={!patientSearch.trim()}
            onClick={() => {
              setPatientQuery(patientSearch.trim());
              setPatLookupPage(0);
            }}
          >
            Search
          </Button>
        </Box>

        {patientQuery ? (
          <>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Showing records for patient: <strong>{patientQuery}</strong>
            </Typography>
            <HistoryTable
              rows={patientData?.content ?? []}
              totalElements={patientData?.totalElements ?? 0}
              page={patLookupPage}
              size={patLookupSize}
              onPageChange={setPatLookupPage}
              onSizeChange={setPatLookupSize}
              loading={patientLoading}
            />
          </>
        ) : (
          <Alert severity="info" icon={<PersonSearchIcon />}>
            Enter a patient identifier above and press Search or Enter to view their dispensation history.
          </Alert>
        )}
      </TabPanel>

      {/* ===================== CONFIRMATION DIALOG ========================== */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Confirm Dispensation</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Medicine</Typography>
              <Typography variant="body2" sx={{ fontWeight: 500 }}>{selectedMedicine?.name}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Batch selection</Typography>
              <Typography variant="body2" sx={{ fontWeight: 500 }}>
                {useFefo ? 'FEFO — auto-selected by system' : selectedBatch?.batchNumber}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Patient</Typography>
              <Typography variant="body2" sx={{ fontWeight: 500 }}>{patientIdentifier}</Typography>
            </Box>
            {prescriptionReference && (
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Prescription Ref</Typography>
                <Typography variant="body2" sx={{ fontWeight: 500 }}>{prescriptionReference}</Typography>
              </Box>
            )}
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="body2" color="text.secondary">Quantity</Typography>
              <Typography variant="body2" sx={{ fontWeight: 500 }}>{quantity}</Typography>
            </Box>
            {!useFefo && overrideReason && (
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Override Reason</Typography>
                <Typography variant="body2" sx={{ fontWeight: 500, maxWidth: 300, textAlign: 'right' }}>{overrideReason}</Typography>
              </Box>
            )}
            {notes && (
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2" color="text.secondary">Notes</Typography>
                <Typography variant="body2" sx={{ fontWeight: 500, maxWidth: 300, textAlign: 'right' }}>{notes}</Typography>
              </Box>
            )}
            {!useFefo && (
              <Alert severity="warning" sx={{ mt: 1 }} icon={<WarnIcon />}>
                You are manually overriding FEFO. This action will be audit-logged.
              </Alert>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)} disabled={dispenseMutation.isPending}>
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleConfirmDispense}
            disabled={dispenseMutation.isPending}
            color={!useFefo ? 'warning' : 'primary'}
          >
            {dispenseMutation.isPending ? <CircularProgress size={22} /> : 'Confirm Dispense'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
