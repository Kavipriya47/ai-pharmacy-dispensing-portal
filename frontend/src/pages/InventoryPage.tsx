import React, { useState } from 'react';
import {
  Typography, Box, Tabs, Tab, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, TablePagination, Chip, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Select, MenuItem, FormControl, InputLabel, Alert, Tooltip, CircularProgress, List, ListItem, ListItemText
} from '@mui/material';
import {
  Edit as EditIcon,
  AddCircleOutlined as AddCircleOutlineIcon,
  ErrorOutlined as ErrorOutlineIcon,
  Group as GroupIcon,
  Announcement as AnnouncementIcon
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getStockSummary, getBatches, receiveBatch, recallBatch, getAffectedPatients } from '../api/inventoryApi';
import { getMedicines, getMedicine, updateMedicine } from '../api/medicineApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';
import type { BatchRequest, BatchResponse, StockSummaryResponse } from '../types/api';
import LoadingSpinner from '../components/LoadingSpinner';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      {...other}
    >
      {value === index && (
        <Box sx={{ pt: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

export default function InventoryPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole(ROLES.ADMIN);
  const queryClient = useQueryClient();

  const [tabValue, setTabValue] = useState(0);

  // Pagination
  const [stockPage, setStockPage] = useState(0);
  const [stockSize, setStockSize] = useState(10);
  const [batchPage, setBatchPage] = useState(0);
  const [batchSize, setBatchSize] = useState(10);

  // Queries
  const { data: stockSummaryPage, isLoading: isLoadingStock } = useQuery({
    queryKey: ['stockSummary', stockPage, stockSize],
    queryFn: () => getStockSummary({ page: stockPage, size: stockSize }),
  });

  // Batch Filter state
  const [batchSearch, setBatchSearch] = useState('');
  const [batchStatus, setBatchStatus] = useState('ALL');

  const { data: batchesPage, isLoading: isLoadingBatches } = useQuery({
    queryKey: ['batches', batchPage, batchSize, batchSearch, batchStatus],
    queryFn: () => getBatches({
      page: batchPage,
      size: batchSize,
      search: batchSearch || undefined,
      status: batchStatus !== 'ALL' ? batchStatus : undefined
    }),
  });

  const { data: medicinesPage } = useQuery({
    queryKey: ['medicines', 0, 1000], // For the dropdown when receiving a batch
    queryFn: () => getMedicines({ page: 0, size: 1000 }),
  });

  // Reorder Level Modal
  const [openReorderModal, setOpenReorderModal] = useState(false);
  const [editingStock, setEditingStock] = useState<StockSummaryResponse | null>(null);
  const [newReorderLevel, setNewReorderLevel] = useState<number>(0);
  const [isUpdatingReorder, setIsUpdatingReorder] = useState(false);

  // Receive Batch Modal
  const [openBatchModal, setOpenBatchModal] = useState(false);
  const [batchFormData, setBatchFormData] = useState<BatchRequest>({
    medicineId: 0,
    batchNumber: '',
    manufacturer: '',
    manufacturingDate: '',
    expiryDate: '',
    quantityReceived: 1,
    unitCost: 0,
    notes: '',
  });

  // Recall Modal
  const [openRecallModal, setOpenRecallModal] = useState(false);
  const [recallingBatch, setRecallingBatch] = useState<BatchResponse | null>(null);
  const [recallReason, setRecallReason] = useState('');

  // Affected Patients Modal
  const [openPatientsModal, setOpenPatientsModal] = useState(false);
  const [affectedPatients, setAffectedPatients] = useState<string[]>([]);
  const [loadingPatients, setLoadingPatients] = useState(false);

  // Mutations
  const receiveBatchMutation = useMutation({
    mutationFn: receiveBatch,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['batches'] });
      queryClient.invalidateQueries({ queryKey: ['stockSummary'] });
      setOpenBatchModal(false);
    },
    onError: (error: any) => {
      console.error('Receive batch failed:', error);
      alert(error.response?.data?.detail || error.message || 'Failed to receive batch');
    }
  });

  const recallBatchMutation = useMutation({
    mutationFn: ({ batchNumber, reason }: { batchNumber: string, reason: string }) => recallBatch(batchNumber, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['batches'] });
      queryClient.invalidateQueries({ queryKey: ['stockSummary'] });
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
      setOpenRecallModal(false);
    },
    onError: (error: any) => {
      console.error('Recall batch failed:', error);
      alert(error.response?.data?.detail || error.message || 'Failed to recall batch');
    }
  });

  const handleOpenReorderModal = (stock: StockSummaryResponse) => {
    setEditingStock(stock);
    setNewReorderLevel(stock.reorderLevel);
    setOpenReorderModal(true);
  };

  const handleSaveReorderLevel = async () => {
    if (!editingStock) return;
    setIsUpdatingReorder(true);
    try {
      // Fetch full medicine details
      const medicine = await getMedicine(editingStock.medicineId);
      // Update with new reorder level
      const updatedMedicine = {
        name: medicine.name,
        genericName: medicine.genericName,
        category: medicine.category,
        dosageForm: medicine.dosageForm,
        strength: medicine.strength,
        unitOfMeasure: medicine.unitOfMeasure,
        description: medicine.description,
        requiresPrescription: medicine.requiresPrescription,
        reorderLevel: newReorderLevel,
        supplierId: medicine.supplierId,
      };
      await updateMedicine(medicine.id, updatedMedicine);
      queryClient.invalidateQueries({ queryKey: ['stockSummary'] });
      setOpenReorderModal(false);
    } catch (error) {
      console.error('Failed to update reorder level', error);
      alert('Failed to update reorder level. Please check console.');
    } finally {
      setIsUpdatingReorder(false);
    }
  };

  const handleOpenBatchModal = () => {
    setBatchFormData({
      medicineId: medicinesPage?.content[0]?.id || 0,
      batchNumber: '',
      manufacturer: '',
      manufacturingDate: '',
      expiryDate: '',
      quantityReceived: 1,
      unitCost: 0,
      notes: '',
    });
    setOpenBatchModal(true);
  };

  const handleBatchFormChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const name = e.target.name as keyof BatchRequest;
    const value = e.target.value;
    setBatchFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleReceiveBatch = (e: React.FormEvent) => {
    e.preventDefault();
    receiveBatchMutation.mutate(batchFormData);
  };

  const handleOpenRecallModal = (batch: BatchResponse) => {
    setRecallingBatch(batch);
    setRecallReason('');
    setOpenRecallModal(true);
  };

  const handleRecallBatch = (e: React.FormEvent) => {
    e.preventDefault();
    if (recallingBatch) {
      recallBatchMutation.mutate({ batchNumber: recallingBatch.batchNumber, reason: recallReason });
    }
  };

  const handleViewPatients = async (batch: BatchResponse) => {
    setLoadingPatients(true);
    setOpenPatientsModal(true);
    try {
      const patients = await getAffectedPatients(batch.batchNumber);
      setAffectedPatients(patients);
    } catch (error) {
      console.error('Failed to fetch patients', error);
      setAffectedPatients([]);
    } finally {
      setLoadingPatients(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'EXPIRED': return 'error';
      case 'QUARANTINED': return 'warning';
      case 'RECALLED': return 'error';
      case 'DEPLETED': return 'default';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>Inventory</Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
          <Tab label="Stock Summary" />
          <Tab label="Batches" />
        </Tabs>
      </Box>

      {/* STOCK SUMMARY TAB */}
      <TabPanel value={tabValue} index={0}>
        {isLoadingStock ? <LoadingSpinner /> : (
          <Paper>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Medicine</TableCell>
                    <TableCell>Generic Name</TableCell>
                    <TableCell align="right">Total Quantity</TableCell>
                    <TableCell align="right">Reorder Level</TableCell>
                    <TableCell align="right">Active Batches</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {stockSummaryPage?.content.map((stock) => (
                    <TableRow key={stock.medicineId}>
                      <TableCell>{stock.medicineName}</TableCell>
                      <TableCell>{stock.genericName}</TableCell>
                      <TableCell align="right">{stock.totalQuantity}</TableCell>
                      <TableCell align="right">{stock.reorderLevel}</TableCell>
                      <TableCell align="right">{stock.activeBatchCount}</TableCell>
                      <TableCell>
                        <Chip
                          label={stock.lowStock ? 'LOW STOCK' : 'IN STOCK'}
                          color={stock.lowStock ? 'warning' : 'success'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Tooltip title="Edit Reorder Level">
                          <span>
                            <IconButton
                              size="small"
                              disabled={!isAdmin && !hasRole(ROLES.PHARMACIST)}
                              onClick={() => handleOpenReorderModal(stock)}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </span>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                  {(!stockSummaryPage || stockSummaryPage.content.length === 0) && (
                    <TableRow>
                      <TableCell colSpan={7} align="center">No stock summary found.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={stockSummaryPage?.totalElements || 0}
              page={stockPage}
              onPageChange={(_, newPage) => setStockPage(newPage)}
              rowsPerPage={stockSize}
              onRowsPerPageChange={(event) => { setStockSize(parseInt(event.target.value, 10)); setStockPage(0); }}
            />
          </Paper>
        )}
      </TabPanel>

      {/* BATCHES TAB */}
      <TabPanel value={tabValue} index={1}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="Search batches or medicines"
              variant="outlined"
              size="small"
              value={batchSearch}
              onChange={(e) => { setBatchSearch(e.target.value); setBatchPage(0); }}
            />
            <TextField
              select
              label="Status"
              variant="outlined"
              size="small"
              value={batchStatus}
              onChange={(e) => { setBatchStatus(e.target.value); setBatchPage(0); }}
              slotProps={{ select: { native: true } }}
              sx={{ minWidth: 120 }}
            >
              <option value="ALL">All</option>
              <option value="ACTIVE">Active</option>
              <option value="EXPIRED">Expired</option>
              <option value="QUARANTINED">Quarantined</option>
              <option value="RECALLED">Recalled</option>
              <option value="DEPLETED">Depleted</option>
            </TextField>
          </Box>
          <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={handleOpenBatchModal}>
            Receive Batch
          </Button>
        </Box>
        {isLoadingBatches ? <LoadingSpinner /> : (
          <Paper>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Batch No</TableCell>
                    <TableCell>Medicine</TableCell>
                    <TableCell>Expiry Date</TableCell>
                    <TableCell align="right">Received</TableCell>
                    <TableCell align="right">Remaining</TableCell>
                    <TableCell>Status</TableCell>
                    {isAdmin && <TableCell align="right">Actions</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {batchesPage?.content.map((batch) => (
                    <TableRow key={batch.id}>
                      <TableCell>{batch.batchNumber}</TableCell>
                      <TableCell>{batch.medicineName}</TableCell>
                      <TableCell>
                        {batch.expiryDate}
                        {batch.status === 'EXPIRED' && <ErrorOutlineIcon color="error" sx={{ ml: 1, verticalAlign: 'middle', fontSize: 18 }} />}
                      </TableCell>
                      <TableCell align="right">{batch.quantityReceived}</TableCell>
                      <TableCell align="right">{batch.quantityRemaining}</TableCell>
                      <TableCell>
                        <Chip label={batch.status} color={getStatusColor(batch.status) as any} size="small" />
                      </TableCell>
                      {isAdmin && (
                        <TableCell align="right">
                          <Tooltip title="View Affected Patients">
                            <IconButton size="small" color="primary" onClick={() => handleViewPatients(batch)}>
                              <GroupIcon />
                            </IconButton>
                          </Tooltip>
                          {batch.status === 'ACTIVE' && (
                            <Tooltip title="Recall Batch">
                              <IconButton size="small" color="error" onClick={() => handleOpenRecallModal(batch)}>
                                <AnnouncementIcon />
                              </IconButton>
                            </Tooltip>
                          )}
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                  {(!batchesPage || batchesPage.content.length === 0) && (
                    <TableRow>
                      <TableCell colSpan={isAdmin ? 7 : 6} align="center">No batches found.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={batchesPage?.totalElements || 0}
              page={batchPage}
              onPageChange={(_, newPage) => setBatchPage(newPage)}
              rowsPerPage={batchSize}
              onRowsPerPageChange={(event) => { setBatchSize(parseInt(event.target.value, 10)); setBatchPage(0); }}
            />
          </Paper>
        )}
      </TabPanel>

      {/* Edit Reorder Level Modal */}
      <Dialog open={openReorderModal} onClose={() => setOpenReorderModal(false)}>
        <DialogTitle>Edit Reorder Level</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Medicine: {editingStock?.medicineName} ({editingStock?.genericName})
          </Typography>
          <TextField
            label="Reorder Level"
            type="number"
            fullWidth
            value={newReorderLevel}
            onChange={(e) => setNewReorderLevel(parseInt(e.target.value, 10) || 0)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenReorderModal(false)}>Cancel</Button>
          <Button onClick={handleSaveReorderLevel} variant="contained" disabled={isUpdatingReorder}>
            {isUpdatingReorder ? <CircularProgress size={24} /> : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Receive Batch Modal */}
      <Dialog open={openBatchModal} onClose={() => setOpenBatchModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Receive Batch</DialogTitle>
        <form onSubmit={handleReceiveBatch}>
          <DialogContent>
            <FormControl fullWidth margin="dense" required>
              <InputLabel>Medicine</InputLabel>
              <Select
                name="medicineId"
                value={batchFormData.medicineId || ''}
                label="Medicine"
                onChange={handleBatchFormChange as any}
              >
                {medicinesPage?.content.map(med => (
                  <MenuItem key={med.id} value={med.id}>{med.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              margin="dense" label="Batch Number" name="batchNumber" fullWidth required
              value={batchFormData.batchNumber} onChange={handleBatchFormChange}
            />
            <TextField
              margin="dense" label="Manufacturer" name="manufacturer" fullWidth
              value={batchFormData.manufacturer || ''} onChange={handleBatchFormChange}
            />
            <TextField
              margin="dense" label="Manufacturing Date" name="manufacturingDate" type="date" fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
              value={batchFormData.manufacturingDate || ''} onChange={handleBatchFormChange}
            />
            <TextField
              margin="dense" label="Expiry Date" name="expiryDate" type="date" fullWidth required
              slotProps={{ inputLabel: { shrink: true } }}
              value={batchFormData.expiryDate} onChange={handleBatchFormChange}
            />
            <TextField
              margin="dense" label="Quantity Received" name="quantityReceived" type="number" fullWidth required
              value={batchFormData.quantityReceived} onChange={handleBatchFormChange}
            />
            <TextField
              margin="dense" label="Unit Cost" name="unitCost" type="number" fullWidth
              slotProps={{ htmlInput: { step: '0.01' } }}
              value={batchFormData.unitCost || ''} onChange={handleBatchFormChange}
            />
            <TextField
              margin="dense" label="Notes" name="notes" fullWidth multiline rows={2}
              value={batchFormData.notes || ''} onChange={handleBatchFormChange}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenBatchModal(false)}>Cancel</Button>
            <Button type="submit" variant="contained">Receive</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Recall Batch Modal */}
      <Dialog open={openRecallModal} onClose={() => setOpenRecallModal(false)}>
        <DialogTitle>Recall Batch</DialogTitle>
        <form onSubmit={handleRecallBatch}>
          <DialogContent>
            <Alert severity="error" sx={{ mb: 2 }}>
              Are you sure you want to recall batch <strong>{recallingBatch?.batchNumber}</strong> of {recallingBatch?.medicineName}?
              This action cannot be undone.
            </Alert>
            <TextField
              autoFocus margin="dense" label="Reason for recall" fullWidth required multiline rows={3}
              value={recallReason} onChange={(e) => setRecallReason(e.target.value)}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenRecallModal(false)}>Cancel</Button>
            <Button type="submit" color="error" variant="contained">Confirm Recall</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Affected Patients Modal */}
      <Dialog open={openPatientsModal} onClose={() => setOpenPatientsModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Affected Patients</DialogTitle>
        <DialogContent>
          {loadingPatients ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : affectedPatients.length > 0 ? (
            <List>
              {affectedPatients.map((patient, index) => (
                <ListItem key={index}>
                  <ListItemText primary={patient} />
                </ListItem>
              ))}
            </List>
          ) : (
            <Typography sx={{ p: 2, textAlign: 'center' }}>No patients affected by this batch.</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenPatientsModal(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
