import apiClient from './axiosClient';
import type {
  DispensingStatsDto,
  InventoryDashboardDto,
  RecallHistoryDto,
  AuditReportRow,
} from '../types/api';

/**
 * Helper to download binary files (Excel / PDF) via Axios blob response.
 */
async function downloadFile(url: string, params: Record<string, any>, defaultFilename: string) {
  const response = await apiClient.get(url, {
    params,
    responseType: 'blob',
  });
  const contentType = (response.headers['content-type'] as string) || 'application/octet-stream';
  const blob = new Blob([response.data], { type: contentType });
  const link = document.createElement('a');
  link.href = window.URL.createObjectURL(blob);
  
  // Extract filename from content-disposition header if available
  const disposition = response.headers['content-disposition'];
  let filename = defaultFilename;
  if (disposition && disposition.includes('filename=')) {
    const match = disposition.match(/filename=["']?([^"';]+)["']?/);
    if (match && match[1]) filename = match[1];
  }
  
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(link.href);
}

// --- Dispensing Reports ---

export function getDispensingSummary(from: string, to: string): Promise<DispensingStatsDto> {
  return apiClient.get<DispensingStatsDto>('/api/v1/reports/dispensing/summary', { params: { from, to } }).then(r => r.data);
}

export function getDispensingByMedicine(from: string, to: string): Promise<[string, number, number][]> {
  return apiClient.get<[string, number, number][]>('/api/v1/reports/dispensing/by-medicine', { params: { from, to } }).then(r => r.data);
}

export function getDispensingByPharmacist(from: string, to: string): Promise<[string, number][]> {
  return apiClient.get<[string, number][]>('/api/v1/reports/dispensing/by-pharmacist', { params: { from, to } }).then(r => r.data);
}

export function exportDispensingExcel(from: string, to: string): Promise<void> {
  return downloadFile('/api/v1/reports/dispensing/export/excel', { from, to }, 'dispensing_report.xlsx');
}

export function exportDispensingPdf(from: string, to: string): Promise<void> {
  return downloadFile('/api/v1/reports/dispensing/export/pdf', { from, to }, 'dispensing_report.pdf');
}

// --- Inventory Reports ---

export function getInventoryDashboard(): Promise<InventoryDashboardDto> {
  return apiClient.get<InventoryDashboardDto>('/api/v1/reports/inventory/dashboard').then(r => r.data);
}

export function getLowStockCount(): Promise<number> {
  return apiClient.get<number>('/api/v1/reports/inventory/low-stock').then(r => r.data);
}

// --- Recall Reports ---

export function getRecallHistory(): Promise<RecallHistoryDto[]> {
  return apiClient.get<RecallHistoryDto[]>('/api/v1/reports/recalls').then(r => r.data);
}

// --- Audit Reports ---

export function getAuditReport(params: {
  from: string;
  to: string;
  performedBy?: string;
  eventType?: string;
}): Promise<AuditReportRow[]> {
  return apiClient.get<AuditReportRow[]>('/api/v1/reports/audit', { params }).then(r => r.data);
}

export function exportAuditExcel(params: {
  from: string;
  to: string;
  performedBy?: string;
  eventType?: string;
}): Promise<void> {
  return downloadFile('/api/v1/reports/audit/export/excel', params, 'audit_report.xlsx');
}
