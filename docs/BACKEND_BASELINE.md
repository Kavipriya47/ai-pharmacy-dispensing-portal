# Backend Baseline — AI-Assisted Pharmacy Medication Dispensing System

> **Snapshot date**: 2026-08-10  
> **Backend version**: `1.0.0-SNAPSHOT`  
> **Test suite**: 28 tests — 0 failures — 0 errors — BUILD SUCCESS  
> **Purpose**: This document is the authoritative reference for the backend state before
> React (Sprint 3A) development begins. All frontend API calls must be based on the
> contracts described here. Do **not** change backend business logic from the React layer.

---

## 1. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.3.2 |
| Security | Spring Security + JJWT | 0.12.6 |
| Persistence | Spring Data JPA + Hibernate Envers | (Boot-managed) |
| Database (prod) | MySQL | 8.x |
| Database (test) | H2 (in-memory) | (Boot-managed) |
| Migrations | Flyway | (Boot-managed) |
| API Docs | SpringDoc OpenAPI / Swagger UI | 2.6.0 |
| Excel export | Apache POI (poi-ooxml) | 5.3.0 |
| PDF export | OpenPDF (librepdf) | 2.0.3 |
| Build | Maven | 3.x |

---

## 2. Package Structure

```
com.pharmacy.dispensing
├── PharmacyDispensingApplication.java        ← entry point
├── audit/                                    ← audit event capture & retrieval
│   ├── controller/AuditEventController.java
│   ├── dto/AuditEventDto.java
│   ├── entity/AuditEvent.java
│   ├── repository/AuditEventRepository.java
│   └── service/AuditEventService.java
├── auth/                                     ← authentication & user management
│   ├── controller/AuthController.java
│   ├── dto/{AuthResponse, LoginRequest, RefreshTokenRequest, UserDto}.java
│   ├── entity/{RefreshToken, Role, User}.java
│   ├── repository/{RefreshTokenRepository, RoleRepository, UserRepository}.java
│   └── service/AuthService.java
├── common/                                   ← cross-cutting concerns
│   ├── config/{AuditConfig, OpenApiConfig, SchedulingConfig, SecurityConfig}.java
│   ├── exception/{GlobalExceptionHandler, InvalidCredentialsException,
│   │              ResourceNotFoundException, TokenExpiredException}.java
│   └── security/{CustomUserDetailsService, JwtAuthenticationFilter,
│                 JwtTokenProvider, UserPrincipal}.java
├── dispensing/                               ← dispensing workflow
│   ├── controller/DispensingController.java
│   ├── dto/{DispensationResponse, DispenseRequest}.java
│   ├── entity/{DispensationRecord, DispensationStatus}.java
│   ├── repository/DispensationRepository.java
│   └── service/{DispensingService, DispensingValidationService}.java
├── inventory/                                ← batch & inventory management
│   ├── controller/InventoryController.java
│   ├── dto/{BatchRequest, BatchResponse, BatchStatusUpdateRequest, StockSummaryResponse}.java
│   ├── entity/{BatchStatus, Inventory, InventoryTransaction, MedicineBatch, TransactionType}.java
│   ├── repository/{InventoryRepository, InventoryTransactionRepository, MedicineBatchRepository}.java
│   └── service/{ExpiryCheckScheduler, MedicineBatchService, RecallService}.java
├── medicine/                                 ← medicine catalogue & suppliers
│   ├── controller/{MedicineController, SupplierController}.java
│   ├── dto/{MedicineRequest, MedicineResponse, SupplierRequest, SupplierResponse}.java
│   ├── entity/{DosageForm, Medicine, MedicineCategory, Supplier}.java
│   ├── repository/{MedicineRepository, SupplierRepository}.java
│   └── service/{MedicineService, SupplierService}.java
├── notification/                             ← REST-poll notification system
│   ├── controller/NotificationController.java
│   ├── dto/NotificationDto.java
│   ├── entity/{Notification, NotificationSeverity, NotificationType}.java
│   ├── repository/NotificationRepository.java
│   └── service/NotificationService.java
└── reporting/                                ← read-only reporting & export
    ├── controller/ReportingController.java
    ├── dto/{AuditReportRow, DispensingStatsDto, InventoryDashboardDto, RecallHistoryDto}.java
    └── service/{AuditReportService, DispensingReportService, ExportService,
                InventoryReportService, RecallReportService}.java
```

---

## 3. Database Schema (Flyway Migrations)

### V1 — `initial_schema_and_rbac.sql`
Creates:
- `roles` — id, name (e.g. `ROLE_ADMIN`, `ROLE_PHARMACIST`, `ROLE_AUDITOR`)
- `users` — id, username, password_hash, email, enabled, created_at
- `user_roles` — join table
- Seed data: default roles

### V2 — `envers_revision_info.sql`
Creates:
- `revinfo` — Hibernate Envers revision table (id, timestamp)

### V3 — `medicine_catalog.sql`
Creates:
- `suppliers` — id, name, contact, email, address
- `medicines` — id, name, generic_name, category (enum), dosage_form (enum),
  strength, unit, supplier_id (FK), description, is_active

### V4 — `inventory_batches.sql`
Creates:
- `inventory` — id, medicine_id (FK unique), quantity_on_hand, reorder_level *(added in V6)*,
  last_updated_at
- `inventory_aud` — Envers audit of `inventory`, includes `reorder_level`
- `medicine_batches` — id, batch_number, medicine_id (FK), inventory_id (FK),
  quantity, expiry_date, received_date, cost_price, status (enum: ACTIVE, EXPIRED, RECALLED, DEPLETED)
- `inventory_transactions` — id, inventory_id, transaction_type (enum), quantity,
  batch_id, performed_by, notes, timestamp

### V5 — `dispensing_records.sql`
Creates:
- `dispensation_records` — id, patient_name, patient_id, medicine_id (FK),
  batch_id (FK), quantity, status (enum: COMPLETED, CANCELLED), dispensed_by (username),
  dispensed_at, notes

### V6 — `notifications_reorder.sql`
Creates:
- `notifications` — id, title, message, type (enum), severity (enum),
  reference_id, reference_type, is_read, created_at
- Adds `reorder_level` column to `inventory` and `inventory_aud`

---

## 4. Entities & Key Relationships

```
Supplier ──(1:N)──▶ Medicine ──(1:1)──▶ Inventory
                       │                    │
                       └──(1:N)──▶ MedicineBatch ──(1:N)──▶ DispensationRecord
                                       │
                                  AuditEvent (event-driven, not FK)
```

### `DispensationStatus` enum
- `COMPLETED` — successful dispense; creates a `DispensationRecord`
- `CANCELLED` — cancelled before completion; creates a `DispensationRecord`

> **Note**: `DISPENSE_FAILED` events are captured as `AuditEvent` entries only —
> they do **not** create a `DispensationRecord`. Reports treat failed attempts
> as a separate non-mutually-exclusive category.

### `BatchStatus` enum
- `ACTIVE`, `EXPIRED`, `RECALLED`, `DEPLETED`

### `NotificationType` enum
- `LOW_STOCK`, `EXPIRY_WARNING`, `RECALL`, `SYSTEM`

### `NotificationSeverity` enum
- `INFO`, `WARNING`, `CRITICAL`

---

## 5. Security & RBAC

### JWT Configuration
- Stateless session (no cookies/server state)
- Token type: Bearer JWT (signed HS256 via JJWT 0.12.6)
- Access token: short-lived (configurable in `application.properties`)
- Refresh token: stored in `refresh_tokens` table

### Roles (as stored in DB and JWT authorities)
| Role Name | Capabilities |
|---|---|
| `ROLE_ADMIN` | Full access to all endpoints including sensitive patient data and recalls |
| `ROLE_PHARMACIST` | Dispensing, inventory viewing, notification management, low-stock counts |
| `ROLE_AUDITOR` | Read-only access to audit logs, reports (no patient data, no stock mutation) |

### CORS
- All origins allowed (`*` with `allowedOriginPatterns`)
- Methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- Headers: `Authorization`, `Content-Type`, `X-Requested-With`
- Credentials: allowed

### Public Endpoints (no auth required)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `GET /v3/api-docs/**`, `/swagger-ui/**`
- `GET /actuator/health`
- `OPTIONS /**`

---

## 6. REST API Surface

Base path: `http://localhost:8080/api/v1`

### Authentication — `/auth`
| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/auth/login` | public | Login, returns `accessToken` + `refreshToken` |
| POST | `/auth/register` | public | Register new user |
| POST | `/auth/refresh` | public | Refresh access token |
| POST | `/auth/logout` | authenticated | Invalidate refresh token |
| GET | `/auth/me` | authenticated | Current user profile |
| GET | `/auth/users` | ADMIN | List all users |
| PUT | `/auth/users/{id}/roles` | ADMIN | Update user roles |

### Medicines — `/medicines`
| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/medicines` | ALL | List all medicines (pageable) |
| POST | `/medicines` | ADMIN | Create medicine |
| GET | `/medicines/{id}` | ALL | Get medicine by ID |
| PUT | `/medicines/{id}` | ADMIN | Update medicine |
| DELETE | `/medicines/{id}` | ADMIN | Delete medicine |
| GET | `/medicines/search` | ALL | Search by name/category |

### Suppliers — `/suppliers`
| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/suppliers` | ALL | List all suppliers |
| POST | `/suppliers` | ADMIN | Create supplier |
| GET | `/suppliers/{id}` | ALL | Get supplier |
| PUT | `/suppliers/{id}` | ADMIN | Update supplier |
| DELETE | `/suppliers/{id}` | ADMIN | Delete supplier |

### Inventory & Batches — `/inventory`
| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/inventory` | ALL | List inventory with stock levels |
| GET | `/inventory/{medicineId}/stock` | ALL | Stock summary for a medicine |
| GET | `/inventory/batches` | ALL | List all batches |
| POST | `/inventory/batches` | ADMIN, PHARMACIST | Add new batch |
| GET | `/inventory/batches/{batchNumber}` | ALL | Get batch details |
| PATCH | `/inventory/batches/{batchNumber}/status` | ADMIN | Update batch status |
| POST | `/inventory/batches/{batchNumber}/recall` | ADMIN | Initiate recall |
| GET | `/inventory/batches/{batchNumber}/affected-patients` | **ADMIN only** | Patients from recalled batch |
| PATCH | `/inventory/reorder-level/{medicineId}` | ADMIN | Set reorder level threshold |

### Dispensing — `/dispensing`
| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/dispensing` | PHARMACIST, ADMIN | Dispense medication |
| GET | `/dispensing` | ADMIN, AUDITOR | List all dispense records |
| GET | `/dispensing/{id}` | ADMIN, AUDITOR, PHARMACIST | Get record by ID |
| GET | `/dispensing/patient/{patientId}` | ADMIN, AUDITOR | Records by patient |

### Audit Events — `/audit`
| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/audit/events` | ADMIN, AUDITOR | List audit events (filterable) |
| GET | `/audit/events/{id}` | ADMIN, AUDITOR | Get single event |

### Notifications — `/notifications`
| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/notifications` | PHARMACIST, ADMIN | List notifications |
| GET | `/notifications/unread-count` | PHARMACIST, ADMIN | Unread count (for polling) |
| PATCH | `/notifications/{id}/read` | PHARMACIST, ADMIN | Mark as read |
| PATCH | `/notifications/read-all` | PHARMACIST, ADMIN | Mark all read |
| DELETE | `/notifications/{id}` | ADMIN | Delete notification |

### Reports — `/reports`
| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/reports/dispensing/summary` | PHARMACIST, ADMIN, AUDITOR | Dispensing stats for date range |
| GET | `/reports/dispensing/by-medicine` | ADMIN, AUDITOR | Dispenses grouped by medicine |
| GET | `/reports/dispensing/by-pharmacist` | ADMIN, AUDITOR | Dispenses grouped by pharmacist |
| GET | `/reports/dispensing/export/excel` | ADMIN, AUDITOR | Dispensing report as `.xlsx` |
| GET | `/reports/dispensing/export/pdf` | ADMIN, AUDITOR | Dispensing report as `.pdf` |
| GET | `/reports/inventory/dashboard` | PHARMACIST, ADMIN, AUDITOR | Inventory dashboard stats |
| GET | `/reports/inventory/low-stock` | PHARMACIST, ADMIN | Count of low-stock medicines |
| GET | `/reports/recalls` | ADMIN, AUDITOR | Recall history with audit-extracted reason |
| GET | `/reports/audit` | ADMIN, AUDITOR | Filtered audit report |
| GET | `/reports/audit/export/excel` | ADMIN, AUDITOR | Audit report as `.xlsx` |

> **Date range convention**: All `from` / `to` query parameters are `YYYY-MM-DD` format.
> Both boundaries are **inclusive** (`from` starts at `00:00:00`, `to` ends at `23:59:59`).

---

## 7. Reporting DTOs

### `DispensingStatsDto`
```json
{
  "totalDispensed": 0,
  "completedCount": 0,
  "cancelledCount": 0,
  "failedAttemptCount": 0,
  "totalQuantityDispensed": 0
}
```
> `completedCount`/`cancelledCount` → from `DispensationRecord.status`  
> `failedAttemptCount` → from `AuditEvent` where `eventType = DISPENSE_FAILED`  
> These are **non-mutually exclusive** categories.

### `InventoryDashboardDto`
```json
{
  "totalMedicines": 0,
  "activeBatches": 0,
  "totalStockOnHand": 0,
  "lowStockCount": 0,
  "nearExpiryBatches": 0,
  "expiredBatches": 0,
  "recalledBatches": 0
}
```

### `RecallHistoryDto`
```json
{
  "batchNumber": "",
  "medicineName": "",
  "recallDate": "",
  "recallReason": "",
  "affectedDispensationCount": 0
}
```
> `recallReason` is extracted from the `metadata` JSON field of the `RECALL_INITIATED`
> `AuditEvent`. If absent, defaults to `"Reason not recorded"`.

### `AuditReportRow`
```json
{
  "id": 0,
  "eventType": "",
  "performedBy": "",
  "ipAddress": "",
  "entityType": "",
  "entityId": "",
  "description": "",
  "timestamp": ""
}
```

---

## 8. Audit Logging Architecture

> See [ADR-001](./ADR/001-feature-first-and-audit-separation.md) for the full decision record.

- **Envers auditing**: All `@Entity` classes annotated with `@Audited` write to `*_aud` shadow tables via Hibernate Envers. This captures entity-level create/update/delete changes.
- **Application audit events** (`AuditEvent` table): Business-level events not captured by Envers — e.g., `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `DISPENSE_FAILED`, `RECALL_INITIATED`, `LOW_STOCK_ALERT`. Stored as structured rows with `eventType`, `performedBy`, `ipAddress`, `entityType`, `entityId`, `description`, and `metadata` (JSON string).
- The two mechanisms are **complementary and non-overlapping**.

---

## 9. Operational Features (Sprint 2D)

### Expiry Scheduler
- `ExpiryCheckScheduler` runs on a configurable cron schedule (default: daily)
- Marks batches as `EXPIRED` when `expiryDate < today`
- Creates `EXPIRY_WARNING` notifications for batches expiring within configurable threshold (default: 30 days)
- Creates `CRITICAL` notifications for batches already expired

### Low-Stock Threshold
- Each `Inventory` row has a `reorder_level` (configurable per medicine via `PATCH /inventory/reorder-level/{medicineId}`)
- `NotificationService` checks stock against `reorder_level` after every dispensing transaction
- Emits `LOW_STOCK` / `WARNING` notification when `quantityOnHand ≤ reorderLevel`

### Recall Workflow
- `POST /inventory/batches/{batchNumber}/recall` (ADMIN only)
  1. Sets batch `status → RECALLED`
  2. Logs `RECALL_INITIATED` `AuditEvent` with `metadata.recallReason`
  3. Creates `RECALL` / `CRITICAL` notification
  4. Identifies affected `DispensationRecord` entries linked to the recalled batch

### Notification Polling
- Frontend polls `GET /notifications/unread-count` for badge counts
- REST polling — no WebSockets or SSE

---

## 10. Test Coverage

| Test Class | Tests | Description |
|---|---|---|
| `AuthControllerTest` | 5 | Login, register, refresh, logout, me |
| `JwtTokenProviderTest` | 3 | Token generation, validation, expiry |
| `AuditEventServiceTest` | 4 | Audit event persistence and retrieval |
| `DispensingServiceIntegrationTest` | 7 | Full dispense workflow (H2) |
| `ExpiryCheckSchedulerTest` | 2 | Expiry detection and notification creation |
| `RecallAndNotificationIntegrationTest` | 3 | Recall workflow + notification assertions |
| `ReportingControllerIntegrationTest` | 4 | Reporting endpoints + role authorization |
| **Total** | **28** | **0 failures, 0 errors** |

---

## 11. Configuration Reference (`application.properties` keys)

| Key | Default | Description |
|---|---|---|
| `spring.datasource.url` | — | MySQL JDBC URL |
| `spring.datasource.username` | — | DB username |
| `spring.datasource.password` | — | DB password |
| `app.jwt.secret` | — | JWT signing secret (min 256-bit) |
| `app.jwt.expiration-ms` | `900000` (15 min) | Access token TTL |
| `app.jwt.refresh-expiration-ms` | `604800000` (7 days) | Refresh token TTL |
| `app.expiry.warning-days` | `30` | Days before expiry to warn |
| `app.scheduling.expiry-check-cron` | `0 0 1 * * *` | Cron for expiry scheduler |

---

## 12. OpenAPI / Swagger

- **UI**: `http://localhost:8080/swagger-ui.html`
- **JSON spec**: `http://localhost:8080/v3/api-docs`
- Configured in `OpenApiConfig.java` with API title, version, and JWT bearer auth scheme

---

## 13. Frontend Contract Rules

The following rules apply when building the React frontend:

1. **Do not invent business logic** — all validation, authorization, and state transitions remain in Spring Boot.
2. **Auth flow**: Store `accessToken` in memory (not localStorage); store `refreshToken` in an `httpOnly` cookie or secure storage; implement silent refresh before expiry.
3. **Role-gated UI**: Check JWT claims to conditionally show/hide elements, but always rely on the backend for enforcement.
4. **Date parameters**: Always send `YYYY-MM-DD` for report date ranges.
5. **Notification badge**: Poll `GET /notifications/unread-count` on a reasonable interval (e.g., 60 seconds). Do not use WebSockets or SSE.
6. **Error handling**: All errors return standard Spring `ProblemDetail` / JSON error body. Parse `status`, `message`, and `errors` fields consistently.
7. **No AI / patent features**: Patent-oriented behavioral analysis or risk scoring must **not** be added to the React frontend until separately discussed and approved.

---

*End of Backend Baseline document.*
