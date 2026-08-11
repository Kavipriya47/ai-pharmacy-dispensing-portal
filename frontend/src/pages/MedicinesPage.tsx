import React, { useState } from 'react';
import { useDebounce } from '../hooks/useDebounce';
import {
  Typography,
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Chip,
  TablePagination,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Switch,
  FormControlLabel
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMedicines, createMedicine, updateMedicine, deactivateMedicine } from '../api/medicineApi';
import { getSuppliers } from '../api/supplierApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';
import type { MedicineResponse, MedicineRequest } from '../types/api';
import LoadingSpinner from '../components/LoadingSpinner';

const CATEGORIES = [
  'ANALGESIC', 'ANTIBIOTIC', 'ANTIFUNGAL', 'ANTIVIRAL', 'ANTIDIABETIC',
  'ANTIHYPERTENSIVE', 'CARDIOVASCULAR', 'GASTROINTESTINAL', 'RESPIRATORY',
  'NEUROLOGICAL', 'PSYCHIATRIC', 'HORMONAL', 'ELECTROLYTE',
  'VITAMIN_SUPPLEMENT', 'DERMATOLOGICAL', 'OPHTHALMIC', 'VACCINE',
  'ANTIPARASITIC', 'ANTICOAGULANT', 'OTHER'
];

const DOSAGE_FORMS = [
  'TABLET', 'CAPSULE', 'SYRUP', 'SUSPENSION', 'SOLUTION', 'INJECTION',
  'INFUSION', 'CREAM', 'OINTMENT', 'GEL', 'DROPS', 'INHALER', 'PATCH',
  'SUPPOSITORY', 'SACHET', 'POWDER', 'LOTION', 'SPRAY', 'OTHER'
];

export default function MedicinesPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole(ROLES.ADMIN);
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const debouncedSearch = useDebounce(search, 300);

  const { data: medicinesPage, isLoading } = useQuery({
    queryKey: ['medicines', page, size, debouncedSearch, categoryFilter],
    queryFn: () => getMedicines({ page, size, search: debouncedSearch || undefined, category: categoryFilter || undefined }),
  });

  const { data: suppliers } = useQuery({
    queryKey: ['suppliers'],
    queryFn: getSuppliers,
  });

  const [openForm, setOpenForm] = useState(false);
  const [editingMedicine, setEditingMedicine] = useState<MedicineResponse | null>(null);
  const [formData, setFormData] = useState<MedicineRequest>({
    name: '',
    genericName: '',
    category: 'OTHER',
    dosageForm: 'TABLET',
    strength: '',
    unitOfMeasure: '',
    description: '',
    requiresPrescription: false,
    reorderLevel: 10,
    supplierId: null,
  });

  const createMutation = useMutation({
    mutationFn: createMedicine,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
      handleCloseForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: MedicineRequest }) => updateMedicine(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
      queryClient.invalidateQueries({ queryKey: ['batches'] });
      queryClient.invalidateQueries({ queryKey: ['stockSummary'] });
      handleCloseForm();
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateMedicine,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
    },
  });

  const handleOpenAdd = () => {
    setEditingMedicine(null);
    setFormData({
      name: '',
      genericName: '',
      category: 'OTHER',
      dosageForm: 'TABLET',
      strength: '',
      unitOfMeasure: '',
      description: '',
      requiresPrescription: false,
      reorderLevel: 10,
      supplierId: null,
    });
    setOpenForm(true);
  };

  const handleOpenEdit = (medicine: MedicineResponse) => {
    setEditingMedicine(medicine);
    setFormData({
      name: medicine.name,
      genericName: medicine.genericName,
      category: medicine.category,
      dosageForm: medicine.dosageForm,
      strength: medicine.strength,
      unitOfMeasure: medicine.unitOfMeasure,
      description: medicine.description || '',
      requiresPrescription: medicine.requiresPrescription || false,
      reorderLevel: medicine.reorderLevel,
      supplierId: medicine.supplierId || null,
    });
    setOpenForm(true);
  };

  const handleCloseForm = () => {
    setOpenForm(false);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const name = e.target.name as keyof MedicineRequest;
    const value = e.target.value;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.checked }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingMedicine) {
      updateMutation.mutate({ id: editingMedicine.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to deactivate this medicine?')) {
      deactivateMutation.mutate(id);
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Medicines</Typography>
        {isAdmin && (
          <Button variant="contained" onClick={handleOpenAdd}>
            Add Medicine
          </Button>
        )}
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <TextField
          label="Search"
          variant="outlined"
          size="small"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
          sx={{ minWidth: 200 }}
        />
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Category</InputLabel>
          <Select
            value={categoryFilter}
            label="Category"
            onChange={(e) => {
              setCategoryFilter(e.target.value);
              setPage(0);
            }}
          >
            <MenuItem value=""><em>All Categories</em></MenuItem>
            {CATEGORIES.map(cat => (
              <MenuItem key={cat} value={cat}>{cat}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <Paper>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Generic Name</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell>Form</TableCell>
                  <TableCell>Strength</TableCell>
                  <TableCell>Supplier</TableCell>
                  <TableCell>Status</TableCell>
                  {isAdmin && <TableCell align="right">Actions</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {medicinesPage?.content.map((medicine) => (
                  <TableRow key={medicine.id}>
                    <TableCell>{medicine.name}</TableCell>
                    <TableCell>{medicine.genericName}</TableCell>
                    <TableCell>{medicine.category}</TableCell>
                    <TableCell>{medicine.dosageForm}</TableCell>
                    <TableCell>{medicine.strength} {medicine.unitOfMeasure}</TableCell>
                    <TableCell>{medicine.supplierName || '-'}</TableCell>
                    <TableCell>
                      <Chip
                        label={medicine.active ? 'Active' : 'Inactive'}
                        color={medicine.active ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    {isAdmin && (
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => handleOpenEdit(medicine)} disabled={!medicine.active}>
                          <EditIcon />
                        </IconButton>
                        <IconButton size="small" color="error" onClick={() => handleDelete(medicine.id)} disabled={!medicine.active}>
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
                {(!medicinesPage || medicinesPage.content.length === 0) && (
                  <TableRow>
                    <TableCell colSpan={isAdmin ? 8 : 7} align="center">
                      No medicines found.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={medicinesPage?.totalElements || 0}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            rowsPerPage={size}
            onRowsPerPageChange={(event) => {
              setSize(parseInt(event.target.value, 10));
              setPage(0);
            }}
          />
        </Paper>
      )}

      <Dialog open={openForm} onClose={handleCloseForm} fullWidth maxWidth="sm">
        <DialogTitle>{editingMedicine ? 'Edit Medicine' : 'Add Medicine'}</DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <TextField
              margin="dense"
              label="Name"
              name="name"
              fullWidth
              required
              value={formData.name}
              onChange={handleChange}
            />
            <TextField
              margin="dense"
              label="Generic Name"
              name="genericName"
              fullWidth
              required
              value={formData.genericName}
              onChange={handleChange}
            />
            <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  name="category"
                  value={formData.category}
                  label="Category"
                  onChange={handleChange as any}
                  required
                >
                  {CATEGORIES.map(cat => (
                    <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                  ))}
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel>Dosage Form</InputLabel>
                <Select
                  name="dosageForm"
                  value={formData.dosageForm}
                  label="Dosage Form"
                  onChange={handleChange as any}
                  required
                >
                  {DOSAGE_FORMS.map(form => (
                    <MenuItem key={form} value={form}>{form}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
            <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
              <TextField
                margin="dense"
                label="Strength"
                name="strength"
                fullWidth
                required
                value={formData.strength}
                onChange={handleChange}
              />
              <TextField
                margin="dense"
                label="Unit of Measure"
                name="unitOfMeasure"
                fullWidth
                required
                value={formData.unitOfMeasure}
                onChange={handleChange}
              />
            </Box>
            <TextField
              margin="dense"
              label="Description"
              name="description"
              fullWidth
              multiline
              rows={2}
              value={formData.description}
              onChange={handleChange}
            />
            <TextField
              margin="dense"
              label="Reorder Level"
              name="reorderLevel"
              type="number"
              fullWidth
              required
              value={formData.reorderLevel}
              onChange={handleChange}
            />
            <FormControl fullWidth margin="dense">
              <InputLabel>Preferred Supplier</InputLabel>
              <Select
                name="supplierId"
                value={formData.supplierId || ''}
                label="Preferred Supplier"
                onChange={handleChange as any}
              >
                <MenuItem value=""><em>None</em></MenuItem>
                {suppliers?.map(sup => (
                  <MenuItem key={sup.id} value={sup.id}>{sup.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControlLabel
              control={<Switch checked={formData.requiresPrescription} onChange={handleCheckboxChange} name="requiresPrescription" />}
              label="Requires Prescription"
              sx={{ mt: 1 }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseForm}>Cancel</Button>
            <Button type="submit" variant="contained">
              Save
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
