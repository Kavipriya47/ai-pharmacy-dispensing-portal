import apiClient from './axiosClient';
import type { AuditEventDto, Page } from '../types/api';

/**
 * GET /api/v1/audit-events/recent
 * Auth: AUDITOR, ADMIN, PHARMACIST
 */
export function getRecentEvents(): Promise<AuditEventDto[]> {
  return apiClient.get<AuditEventDto[]>('/api/v1/audit-events/recent').then(r => r.data);
}

/**
 * GET /api/v1/audit-events?page=0&size=20
 * Auth: AUDITOR, ADMIN
 */
export function getAuditEvents(page = 0, size = 20): Promise<Page<AuditEventDto>> {
  return apiClient
    .get<Page<AuditEventDto>>('/api/v1/audit-events', { params: { page, size } })
    .then(r => r.data);
}
