import apiClient from './axiosClient';
import type { DispenseRequest, DispensationResponse, Page } from '../types/api';

/**
 * POST /api/v1/dispensing
 * Auth: ADMIN, PHARMACIST
 * Returns 201 Created with the saved DispensationResponse.
 */
export function dispense(data: DispenseRequest): Promise<DispensationResponse> {
  return apiClient.post<DispensationResponse>('/api/v1/dispensing', data).then(r => r.data);
}

/**
 * GET /api/v1/dispensing?page=0&size=20
 * Auth: ADMIN, PHARMACIST, AUDITOR
 * Sorted by dispensedAt descending (hardcoded in DispensingController).
 */
export function getDispensations(params?: {
  page?: number;
  size?: number;
  startDate?: string;
  endDate?: string;
  medicineId?: number;
  status?: string;
}): Promise<Page<DispensationResponse>> {
  return apiClient
    .get<Page<DispensationResponse>>('/api/v1/dispensing', { params })
    .then(r => r.data);
}

/**
 * GET /api/v1/dispensing/{id}
 * Auth: ADMIN, PHARMACIST, AUDITOR
 */
export function getDispensationById(id: number): Promise<DispensationResponse> {
  return apiClient.get<DispensationResponse>(`/api/v1/dispensing/${id}`).then(r => r.data);
}

/**
 * GET /api/v1/dispensing/patient/{patientIdentifier}?page=0&size=20
 * Auth: ADMIN, PHARMACIST, AUDITOR
 * Sorted by dispensedAt descending (hardcoded in DispensingController).
 */
export function getDispensationsByPatient(
  patientIdentifier: string,
  page = 0,
  size = 20,
): Promise<Page<DispensationResponse>> {
  return apiClient
    .get<Page<DispensationResponse>>(
      `/api/v1/dispensing/patient/${encodeURIComponent(patientIdentifier)}`,
      { params: { page, size } },
    )
    .then(r => r.data);
}
