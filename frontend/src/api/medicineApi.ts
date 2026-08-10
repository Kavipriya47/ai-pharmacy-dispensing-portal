import apiClient from './axiosClient';
import type { Page, MedicineRequest, MedicineResponse } from '../types/api';

export async function getMedicines(params?: {
  page?: number;
  size?: number;
  search?: string;
  category?: string;
}): Promise<Page<MedicineResponse>> {
  const response = await apiClient.get<Page<MedicineResponse>>('/api/v1/medicines', { params });
  return response.data;
}

export async function getMedicine(id: number): Promise<MedicineResponse> {
  const response = await apiClient.get<MedicineResponse>(`/api/v1/medicines/${id}`);
  return response.data;
}

export async function createMedicine(data: MedicineRequest): Promise<MedicineResponse> {
  const response = await apiClient.post<MedicineResponse>('/api/v1/medicines', data);
  return response.data;
}

export async function updateMedicine(id: number, data: MedicineRequest): Promise<MedicineResponse> {
  const response = await apiClient.put<MedicineResponse>(`/api/v1/medicines/${id}`, data);
  return response.data;
}

export async function deactivateMedicine(id: number): Promise<void> {
  await apiClient.delete(`/api/v1/medicines/${id}`);
}
