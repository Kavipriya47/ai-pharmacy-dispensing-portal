# ADR 001: Feature-First Package Organization and Audit Separation

## Status
**Approved** (2026-08-04)

## Context
In building the enterprise *AI-Assisted Pharmacy Medication Dispensing Audit & Expiry Tracking System*, two fundamental architectural choices were evaluated:
1. **Package Layout**: Layer-first (`controller`, `service`, `repository`) vs. Feature-first (`auth`, `medicine`, `inventory`, `dispensing`, `audit`, `ai`).
2. **Audit Engine Architecture**: Co-locating entity history with operational events vs. separating entity revision history (Hibernate Envers) from domain business audit events (`audit_events`).

## Decisions

### 1. Feature-First Package Organization
We adopt **Feature-First** package structure under `com.pharmacy.dispensing.<feature>.*`.
- **Rationale**: Feature-first keeps related business logic, entities, DTOs, repositories, and controllers together within their bounded context. This improves cohesion, simplifies maintenance, and aligns with domain-driven design principles.

### 2. Separation of Audit Concerns
We separate audit storage into two distinct mechanisms:
- **Entity Revision Tracking (Hibernate Envers)**: Automated generation of `*_AUD` tables for low-level entity mutation tracking.
- **Business Audit Events (`audit_events` Table)**: Explicit, immutable logging of high-level business events (e.g., `USER_LOGIN`, `MEDICINE_DISPENSED`, `STOCK_ADDED`, `BATCH_QUARANTINED`).
- **Rationale**: Low-level entity revision state snapshots (Envers) serve data reconstruction needs, while business audit events serve operational audit trails, compliance reporting, and user action tracking.

### 3. Authentication and Session Security
- **Access Tokens**: Short-lived stateless JWTs (15 minutes).
- **Refresh Tokens**: Long-lived database-backed refresh tokens (7 days) stored in `refresh_tokens` table for revocation capability.

## Consequences
- Clean separation between technical entity history and business audit logs.
- Modular code layout allowing features to be refactored or extracted independently.
- Complete traceability of all user and system operations.
