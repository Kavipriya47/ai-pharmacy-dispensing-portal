package com.pharmacy.dispensing.dispensing.service;

import com.pharmacy.dispensing.audit.service.AuditEventService;
import com.pharmacy.dispensing.common.exception.ResourceNotFoundException;
import com.pharmacy.dispensing.dispensing.dto.DispensationResponse;
import com.pharmacy.dispensing.dispensing.dto.DispenseRequest;
import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.entity.DispensationStatus;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import com.pharmacy.dispensing.inventory.entity.Inventory;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.entity.TransactionType;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.inventory.service.MedicineBatchService;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import com.pharmacy.dispensing.notification.entity.NotificationSeverity;
import com.pharmacy.dispensing.notification.entity.NotificationType;
import com.pharmacy.dispensing.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the dispensing process.
 * Ties together Medicine, Inventory, Batch selection, Validation, and Auditing.
 */
@Service
public class DispensingService {

    private final DispensationRepository dispensationRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final InventoryRepository inventoryRepository;
    private final MedicineBatchService batchService;
    private final DispensingValidationService validationService;
    private final AuditEventService auditEventService;
    private final NotificationService notificationService;

    public DispensingService(DispensationRepository dispensationRepository,
                             MedicineRepository medicineRepository,
                             MedicineBatchRepository batchRepository,
                             InventoryRepository inventoryRepository,
                             MedicineBatchService batchService,
                             DispensingValidationService validationService,
                             AuditEventService auditEventService,
                             NotificationService notificationService) {
        this.dispensationRepository = dispensationRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.inventoryRepository = inventoryRepository;
        this.batchService = batchService;
        this.validationService = validationService;
        this.auditEventService = auditEventService;
        this.notificationService = notificationService;
    }

    /**
     * Executes the dispense workflow.
     */
    @Transactional
    public DispensationResponse dispense(DispenseRequest request) {
        try {
            return doDispense(request);
        } catch (Exception e) {
            // Log dispensing failures (Rule violations, out of stock, etc) in an isolated transaction
            auditEventService.logSecurityOrFailureEvent(
                    "DISPENSE_FAILED",
                    currentUsername(),
                    "Dispense failed for medicineId=" + request.getMedicineId() + ": " + e.getMessage(),
                    "medicineId=" + request.getMedicineId() + ", patient=" + request.getPatientIdentifier(),
                    null
            );
            throw e;
        }
    }

    private DispensationResponse doDispense(DispenseRequest request) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Active medicine not found: " + request.getMedicineId()));

        MedicineBatch selectedBatch;
        boolean isOverride = false;

        // FEFO selection or manual override
        if (request.getBatchId() != null) {
            selectedBatch = batchService.findBatchForMedicine(request.getBatchId(), medicine.getId());
            isOverride = true;
            if (request.getOverrideReason() == null || request.getOverrideReason().isBlank()) {
                throw new IllegalArgumentException("FEFO override requires a reason");
            }
        } else {
            selectedBatch = batchService.selectFefoBatch(medicine.getId(), request.getQuantity())
                    .orElseThrow(() -> new IllegalArgumentException("No ACTIVE batches with sufficient stock available."));
        }

        // Validate all ADCE rules
        validationService.validate(request, medicine, selectedBatch);

        // Deduct quantities
        selectedBatch.setQuantityRemaining(selectedBatch.getQuantityRemaining() - request.getQuantity());
        batchRepository.save(selectedBatch);

        Inventory inventory = selectedBatch.getInventory();
        inventory.setTotalQuantity(inventory.getTotalQuantity() - request.getQuantity());
        inventoryRepository.save(inventory);

        // Low stock check
        if (inventory.getTotalQuantity() <= inventory.getReorderLevel()) {
            notificationService.createNotification(
                    NotificationType.LOW_STOCK,
                    NotificationSeverity.WARNING,
                    "Low Stock Alert: " + medicine.getName(),
                    "Remaining stock (" + inventory.getTotalQuantity() + ") has dropped at or below the reorder level (" + inventory.getReorderLevel() + ").",
                    null, // send to all
                    "Inventory",
                    String.valueOf(inventory.getId())
            );
        }

        // Record dispensation
        DispensationRecord record = new DispensationRecord();
        record.setMedicine(medicine);
        record.setBatch(selectedBatch);
        record.setPatientIdentifier(request.getPatientIdentifier());
        record.setPrescriptionReference(request.getPrescriptionReference());
        record.setQuantityDispensed(request.getQuantity());
        record.setDispensedBy(currentUsername());
        record.setStatus(DispensationStatus.COMPLETED);
        record.setFefoOverride(isOverride);
        record.setOverrideReason(request.getOverrideReason());
        record.setNotes(request.getNotes());
        DispensationRecord savedRecord = dispensationRepository.save(record);

        // Record inventory ledger transaction
        batchService.recordTransaction(selectedBatch, TransactionType.DISPENSE, request.getQuantity(),
                "Dispensed to " + request.getPatientIdentifier(), String.valueOf(savedRecord.getId()));

        // Audit Events
        if (isOverride) {
            auditEventService.logEvent(
                    "FEFO_OVERRIDE",
                    currentUsername(),
                    "Pharmacist manually bypassed FEFO for batch " + selectedBatch.getBatchNumber(),
                    "batchId=" + selectedBatch.getId() + ", reason=" + request.getOverrideReason(),
                    null
            );
        }

        auditEventService.logEvent(
                "MEDICATION_DISPENSED",
                currentUsername(),
                "Dispensed " + request.getQuantity() + "x " + medicine.getName() + " to patient " + request.getPatientIdentifier(),
                "dispensationId=" + savedRecord.getId(),
                null
        );

        return mapToResponse(savedRecord);
    }

    @Transactional(readOnly = true)
    public Page<DispensationResponse> findAll(java.time.LocalDateTime startDate,
                                              java.time.LocalDateTime endDate,
                                              Long medicineId,
                                              DispensationStatus status,
                                              Pageable pageable) {
        return dispensationRepository.searchAndFilter(startDate, endDate, medicineId, status, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DispensationResponse findById(Long id) {
        return dispensationRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Dispensation record not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<DispensationResponse> findByPatient(String patientIdentifier, Pageable pageable) {
        return dispensationRepository.findByPatientIdentifier(patientIdentifier, pageable).map(this::mapToResponse);
    }

    private DispensationResponse mapToResponse(DispensationRecord r) {
        return new DispensationResponse(
                r.getId(),
                r.getMedicine().getId(),
                r.getMedicine().getName(),
                r.getBatch().getId(),
                r.getBatch().getBatchNumber(),
                r.getPatientIdentifier(),
                r.getPrescriptionReference(),
                r.getQuantityDispensed(),
                r.getDispensedBy(),
                r.getStatus(),
                r.getFefoOverride(),
                r.getOverrideReason(),
                r.getDispensedAt()
        );
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}
