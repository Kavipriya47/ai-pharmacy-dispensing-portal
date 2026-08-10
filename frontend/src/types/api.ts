// Spring Data Page wrapper
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// RFC 7807 ProblemDetail
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  timestamp?: string;
  errors?: Record<string, string>;
}

// Auth
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: UserDto;
}

export interface UserDto {
  id: number;
  username: string;
  email: string;
  fullName: string;
  active: boolean;
  roles: string[];
}

// Notifications
export type NotificationType = 'LOW_STOCK' | 'EXPIRY_WARNING' | 'RECALL' | 'SYSTEM';
export type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL';

export interface NotificationDto {
  id: number;
  type: NotificationType;
  severity: NotificationSeverity;
  title: string;
  message: string;
  read: boolean;
  recipient: string;
  relatedEntityType: string;
  relatedEntityId: string;
  createdAt: string;
}

// Inventory Dashboard (used by Dashboard page skeleton)
export interface InventoryDashboardDto {
  totalMedicines: number;
  activeBatches: number;
  totalStockOnHand: number;
  lowStockCount: number;
  nearExpiryBatches: number;
  expiredBatches: number;
  recalledBatches: number;
}

// Backend roles - exact values from DB
export const ROLES = {
  ADMIN: 'ROLE_ADMIN',
  PHARMACIST: 'ROLE_PHARMACIST',
  AUDITOR: 'ROLE_AUDITOR',
} as const;

export type RoleName = typeof ROLES[keyof typeof ROLES];

// -----------------------------------------------------------------------------
// SPRINT 3A: Medicine, Supplier, Inventory Models
// -----------------------------------------------------------------------------

export type MedicineCategory =
  | 'ANALGESIC' | 'ANTIBIOTIC' | 'ANTIFUNGAL' | 'ANTIVIRAL' | 'ANTIDIABETIC'
  | 'ANTIHYPERTENSIVE' | 'CARDIOVASCULAR' | 'GASTROINTESTINAL' | 'RESPIRATORY'
  | 'NEUROLOGICAL' | 'PSYCHIATRIC' | 'HORMONAL' | 'ELECTROLYTE'
  | 'VITAMIN_SUPPLEMENT' | 'DERMATOLOGICAL' | 'OPHTHALMIC' | 'VACCINE'
  | 'ANTIPARASITIC' | 'ANTICOAGULANT' | 'OTHER';

export type DosageForm =
  | 'TABLET' | 'CAPSULE' | 'SYRUP' | 'SUSPENSION' | 'SOLUTION' | 'INJECTION'
  | 'INFUSION' | 'CREAM' | 'OINTMENT' | 'GEL' | 'DROPS' | 'INHALER' | 'PATCH'
  | 'SUPPOSITORY' | 'SACHET' | 'POWDER' | 'LOTION' | 'SPRAY' | 'OTHER';

export interface MedicineRequest {
  name: string;
  genericName: string;
  category: MedicineCategory;
  dosageForm: DosageForm;
  strength: string;
  unitOfMeasure: string;
  description?: string;
  requiresPrescription?: boolean;
  reorderLevel: number;
  supplierId?: number | null;
}

export interface MedicineResponse {
  id: number;
  name: string;
  genericName: string;
  category: MedicineCategory;
  dosageForm: DosageForm;
  strength: string;
  unitOfMeasure: string;
  description: string;
  requiresPrescription: boolean;
  reorderLevel: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  supplierId: number | null;
  supplierName: string | null;
}

export interface SupplierRequest {
  name: string;
  contactPerson?: string;
  email?: string;
  phone?: string;
  address?: string;
}

export interface SupplierResponse {
  id: number;
  name: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  active: boolean;
  createdAt: string;
}

export type BatchStatus = 'ACTIVE' | 'EXPIRED' | 'QUARANTINED' | 'DEPLETED' | 'RECALLED';

export interface BatchRequest {
  medicineId: number;
  batchNumber: string;
  manufacturer?: string;
  manufacturingDate?: string;
  expiryDate: string;
  quantityReceived: number;
  unitCost?: number;
  notes?: string;
}

export interface BatchResponse {
  id: number;
  inventoryId: number;
  medicineId: number;
  medicineName: string;
  batchNumber: string;
  manufacturer: string;
  manufacturingDate: string;
  expiryDate: string;
  quantityReceived: number;
  quantityRemaining: number;
  unitCost: number;
  status: BatchStatus;
  createdAt: string;
  updatedAt: string;
}

export interface StockSummaryResponse {
  medicineId: number;
  medicineName: string;
  genericName: string;
  totalQuantity: number;
  reorderLevel: number;
  lowStock: boolean;
  activeBatchCount: number;
}

// -----------------------------------------------------------------------------
// SPRINT 3C: Dispensing
// Source: DispensationStatus.java, DispenseRequest.java, DispensationResponse.java
// -----------------------------------------------------------------------------

/** Exactly 2 values — DispensationStatus.java */
export type DispensationStatus = 'COMPLETED' | 'CANCELLED';

/** POST /api/v1/dispensing — DispenseRequest.java */
export interface DispenseRequest {
  /** @NotNull */
  medicineId: number;
  /** Optional. If provided, FEFO is overridden. overrideReason becomes required. */
  batchId?: number | null;
  /** @NotBlank, max 100 */
  patientIdentifier: string;
  /** max 200. Required when medicine.requiresPrescription === true */
  prescriptionReference?: string;
  /** @NotNull, @Min(1) */
  quantity: number;
  /** Required when batchId is provided */
  overrideReason?: string;
  /** Optional free-text */
  notes?: string;
}

/** Response for GET /api/v1/dispensing and POST /api/v1/dispensing — DispensationResponse.java
 *  Note: `notes` is stored in DispensationRecord but is NOT included in this DTO.
 */
export interface DispensationResponse {
  id: number;
  medicineId: number;
  medicineName: string;
  batchId: number;
  batchNumber: string;
  patientIdentifier: string;
  prescriptionReference: string | null;
  quantityDispensed: number;
  dispensedBy: string;
  status: DispensationStatus;
  fefoOverride: boolean;
  overrideReason: string | null;
  dispensedAt: string; // LocalDateTime serialized as ISO string by Jackson
}

// -----------------------------------------------------------------------------
// SPRINT 3D: Reporting & Audit Trail
// Source: DispensingStatsDto.java, RecallHistoryDto.java, AuditReportRow.java, AuditEventDto.java
// -----------------------------------------------------------------------------

export interface DispensingStatsDto {
  completedCount: number;
  cancelledCount: number;
  failedCount: number;
  totalQuantityDispensed: number;
  semanticsNote?: string;
}

export interface RecallHistoryDto {
  batchNumber: string;
  medicineName: string;
  recallDate: string;
  recalledBy: string;
  reason: string;
  affectedDispensationCount: number;
}

export interface AuditReportRow {
  id: number;
  eventType: string;
  performedBy: string;
  description: string;
  metadata: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface AuditEventDto {
  id: number;
  eventType: string;
  performedBy: string;
  description: string;
  metadata: string | null;
  ipAddress: string | null;
  createdAt: string;
}


