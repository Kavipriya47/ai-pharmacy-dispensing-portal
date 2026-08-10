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
