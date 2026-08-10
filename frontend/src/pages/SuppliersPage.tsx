import React, { useState } from 'react';
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
  Chip
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getSuppliers, createSupplier, updateSupplier, deactivateSupplier } from '../api/supplierApi';
import { useAuth } from '../auth/useAuth';
import { ROLES } from '../types/api';
import type { SupplierResponse, SupplierRequest } from '../types/api';
import LoadingSpinner from '../components/LoadingSpinner';

export default function SuppliersPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole(ROLES.ADMIN);
  const queryClient = useQueryClient();

  const { data: suppliers, isLoading } = useQuery({
    queryKey: ['suppliers'],
    queryFn: getSuppliers,
  });

  const [openForm, setOpenForm] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<SupplierResponse | null>(null);
  const [formData, setFormData] = useState<SupplierRequest>({
    name: '',
    contactPerson: '',
    email: '',
    phone: '',
    address: '',
  });

  const createMutation = useMutation({
    mutationFn: createSupplier,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
      handleCloseForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: SupplierRequest }) => updateSupplier(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
      handleCloseForm();
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: deactivateSupplier,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suppliers'] });
    },
  });

  const handleOpenAdd = () => {
    setEditingSupplier(null);
    setFormData({ name: '', contactPerson: '', email: '', phone: '', address: '' });
    setOpenForm(true);
  };

  const handleOpenEdit = (supplier: SupplierResponse) => {
    setEditingSupplier(supplier);
    setFormData({
      name: supplier.name,
      contactPerson: supplier.contactPerson || '',
      email: supplier.email || '',
      phone: supplier.phone || '',
      address: supplier.address || '',
    });
    setOpenForm(true);
  };

  const handleCloseForm = () => {
    setOpenForm(false);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingSupplier) {
      updateMutation.mutate({ id: editingSupplier.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to deactivate this supplier?')) {
      deactivateMutation.mutate(id);
    }
  };

  if (isLoading) return <LoadingSpinner />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Suppliers</Typography>
        {isAdmin && (
          <Button variant="contained" onClick={handleOpenAdd}>
            Add Supplier
          </Button>
        )}
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Contact Person</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Status</TableCell>
              {isAdmin && <TableCell align="right">Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {suppliers?.map((supplier) => (
              <TableRow key={supplier.id}>
                <TableCell>{supplier.name}</TableCell>
                <TableCell>{supplier.contactPerson}</TableCell>
                <TableCell>{supplier.email}</TableCell>
                <TableCell>{supplier.phone}</TableCell>
                <TableCell>
                  <Chip
                    label={supplier.active ? 'Active' : 'Inactive'}
                    color={supplier.active ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                {isAdmin && (
                  <TableCell align="right">
                    <IconButton size="small" onClick={() => handleOpenEdit(supplier)} disabled={!supplier.active}>
                      <EditIcon />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => handleDelete(supplier.id)} disabled={!supplier.active}>
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                )}
              </TableRow>
            ))}
            {(!suppliers || suppliers.length === 0) && (
              <TableRow>
                <TableCell colSpan={isAdmin ? 6 : 5} align="center">
                  No suppliers found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openForm} onClose={handleCloseForm} fullWidth maxWidth="sm">
        <DialogTitle>{editingSupplier ? 'Edit Supplier' : 'Add Supplier'}</DialogTitle>
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
              label="Contact Person"
              name="contactPerson"
              fullWidth
              value={formData.contactPerson}
              onChange={handleChange}
            />
            <TextField
              margin="dense"
              label="Email"
              name="email"
              type="email"
              fullWidth
              value={formData.email}
              onChange={handleChange}
            />
            <TextField
              margin="dense"
              label="Phone"
              name="phone"
              fullWidth
              value={formData.phone}
              onChange={handleChange}
            />
            <TextField
              margin="dense"
              label="Address"
              name="address"
              fullWidth
              multiline
              rows={3}
              value={formData.address}
              onChange={handleChange}
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
