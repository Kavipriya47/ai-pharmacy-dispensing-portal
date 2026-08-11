import apiClient from './axiosClient';
import type { Page, BatchRequest, BatchResponse, StockSummaryResponse } from '../types/api';

export async function getStockSummary(params?: {
  page?: number;
  size?: number;
  search?: string;
  lowStockOnly?: boolean;
}): Promise<Page<StockSummaryResponse>> {
  const response = await apiClient.get<Page<StockSummaryResponse>>('/api/v1/inventory/stock-summary', { params });
  return response.data;
}

export async function getBatches(params?: {
  page?: number;
  size?: number;
  search?: string;
  medicineId?: number;
  status?: string;
}): Promise<Page<BatchResponse>> {
  const response = await apiClient.get<Page<BatchResponse>>('/api/v1/inventory/batches', { params });
  return response.data;
}

export async function receiveBatch(data: BatchRequest): Promise<BatchResponse> {
  const response = await apiClient.post<BatchResponse>('/api/v1/inventory/batches', data);
  return response.data;
}

export async function recallBatch(batchNumber: string, reason: string): Promise<void> {
  await apiClient.post(`/api/v1/inventory/batches/${batchNumber}/recall`, reason, {
    headers: { 'Content-Type': 'text/plain' }
  });
}

export async function getAffectedPatients(batchNumber: string): Promise<string[]> {
  const response = await apiClient.get<string[]>(`/api/v1/inventory/batches/${batchNumber}/affected-patients`);
  return response.data;
}
