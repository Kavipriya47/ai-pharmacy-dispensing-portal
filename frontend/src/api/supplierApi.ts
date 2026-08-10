import apiClient from './axiosClient';
import type { SupplierRequest, SupplierResponse } from '../types/api';

export async function getSuppliers(): Promise<SupplierResponse[]> {
  const response = await apiClient.get<SupplierResponse[]>('/api/v1/suppliers');
  return response.data;
}

export async function getSupplier(id: number): Promise<SupplierResponse> {
  const response = await apiClient.get<SupplierResponse>(`/api/v1/suppliers/${id}`);
  return response.data;
}

export async function createSupplier(data: SupplierRequest): Promise<SupplierResponse> {
  const response = await apiClient.post<SupplierResponse>('/api/v1/suppliers', data);
  return response.data;
}

export async function updateSupplier(id: number, data: SupplierRequest): Promise<SupplierResponse> {
  const response = await apiClient.put<SupplierResponse>(`/api/v1/suppliers/${id}`, data);
  return response.data;
}

export async function deactivateSupplier(id: number): Promise<void> {
  await apiClient.delete(`/api/v1/suppliers/${id}`);
}
