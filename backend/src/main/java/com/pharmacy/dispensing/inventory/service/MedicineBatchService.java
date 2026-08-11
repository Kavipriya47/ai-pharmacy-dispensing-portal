package com.pharmacy.dispensing.inventory.service;

import com.pharmacy.dispensing.audit.service.AuditEventService;
import com.pharmacy.dispensing.common.exception.ResourceNotFoundException;
import com.pharmacy.dispensing.inventory.dto.*;
import com.pharmacy.dispensing.inventory.entity.*;
import com.pharmacy.dispensing.inventory.repository.*;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business service for managing medicine batches and inventory.
 * <p>
 * Responsibilities:
 * <ol>
 *   <li>Receive new stock (create batch + update inventory + record InventoryTransaction)</li>
 *   <li>FEFO batch selection for dispensing</li>
 *   <li>Batch status updates (QUARANTINE, RECALL, etc.) with audit trail</li>
 *   <li>Stock summary aggregation</li>
 * </ol>
 */
@Service
public class MedicineBatchService {

    private final MedicineBatchRepository batchRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final MedicineRepository medicineRepository;
    private final AuditEventService auditEventService;

    public MedicineBatchService(MedicineBatchRepository batchRepository,
                                InventoryRepository inventoryRepository,
                                InventoryTransactionRepository transactionRepository,
                                MedicineRepository medicineRepository,
                                AuditEventService auditEventService) {
        this.batchRepository = batchRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.medicineRepository = medicineRepository;
        this.auditEventService = auditEventService;
    }

    // -------------------------------------------------------
    // Receive Stock (add new batch)
    // -------------------------------------------------------

    /**
     * Records the receipt of a new stock batch for a medicine.
     * <ol>
     *   <li>Resolves or creates the {@link Inventory} record for the medicine.</li>
     *   <li>Creates a new {@link MedicineBatch} with status ACTIVE.</li>
     *   <li>Updates {@link Inventory#getTotalQuantity()} atomically.</li>
     *   <li>Records a {@link TransactionType#RECEIVE} ledger entry.</li>
     *   <li>Emits a {@code BATCH_ADDED} audit event.</li>
     * </ol>
     */
    @Transactional
    public BatchResponse receiveStock(BatchRequest request) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active medicine not found with id: " + request.getMedicineId()));

        // Get or create Inventory record for this medicine
        Inventory inventory = inventoryRepository.findByMedicineId(medicine.getId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setMedicine(medicine);
                    inv.setTotalQuantity(0);
                    return inventoryRepository.save(inv);
                });

        // Create the batch
        MedicineBatch batch = new MedicineBatch();
        batch.setInventory(inventory);
        batch.setBatchNumber(request.getBatchNumber());
        batch.setManufacturer(request.getManufacturer());
        batch.setManufacturingDate(request.getManufacturingDate());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setQuantityReceived(request.getQuantityReceived());
        batch.setQuantityRemaining(request.getQuantityReceived());
        batch.setUnitCost(request.getUnitCost());
        batch.setStatus(BatchStatus.ACTIVE);
        MedicineBatch savedBatch = batchRepository.save(batch);

        // Update aggregate inventory quantity
        inventory.setTotalQuantity(inventory.getTotalQuantity() + request.getQuantityReceived());
        inventoryRepository.save(inventory);

        // Record RECEIVE transaction
        recordTransaction(savedBatch, TransactionType.RECEIVE, request.getQuantityReceived(),
                request.getNotes(), null);

        // Emit audit event
        auditEventService.logEvent(
                "BATCH_ADDED",
                currentUsername(),
                "Batch added: " + request.getBatchNumber() + " for medicine " + medicine.getName(),
                "batchId=" + savedBatch.getId() + ", qty=" + request.getQuantityReceived(),
                null
        );

        return mapToResponse(savedBatch);
    }

    // -------------------------------------------------------
    // Read operations
    // -------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<BatchResponse> findAll(String search, BatchStatus status, Long medicineId, Pageable pageable) {
        return batchRepository.searchAndFilter(search, status, medicineId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BatchResponse findById(Long id) {
        return batchRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findByInventory(Long inventoryId, Pageable pageable) {
        return batchRepository.findByInventoryId(inventoryId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findExpiringSoon(int days, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(days);
        return batchRepository.findExpiringSoon(today, cutoff, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findExpired(Pageable pageable) {
        return batchRepository.findByStatusIn(List.of(BatchStatus.EXPIRED), pageable).map(this::mapToResponse);
    }

    // -------------------------------------------------------
    // FEFO batch selection (for DispensingService)
    // -------------------------------------------------------

    /**
     * Selects the FEFO (First-Expiry-First-Out) batch for a medicine.
     * Returns the batch with the earliest expiry date that has sufficient stock.
     *
     * @param medicineId        target medicine
     * @param requiredQuantity  units needed
     * @return the best FEFO batch, or empty if no valid batch exists
     */
    @Transactional(readOnly = true)
    public Optional<MedicineBatch> selectFefoBatch(Long medicineId, int requiredQuantity) {
        return batchRepository.findFefoBatches(medicineId, LocalDate.now())
                .stream()
                .filter(b -> b.getQuantityRemaining() >= requiredQuantity)
                .findFirst();
    }

    /**
     * Finds a specific batch by ID, validating it belongs to the given medicine.
     * Used for manual FEFO override (audit-logged by caller).
     */
    @Transactional(readOnly = true)
    public MedicineBatch findBatchForMedicine(Long batchId, Long medicineId) {
        MedicineBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        if (!batch.getInventory().getMedicine().getId().equals(medicineId)) {
            throw new IllegalArgumentException("Batch " + batchId + " does not belong to medicine " + medicineId);
        }
        return batch;
    }

    // -------------------------------------------------------
    // Status update (QUARANTINE, RECALL, etc.)
    // -------------------------------------------------------

    /**
     * Updates a batch's status. Adjusts inventory total when stock becomes unavailable.
     * Emits appropriate audit events.
     */
    @Transactional
    public BatchResponse updateStatus(Long batchId, BatchStatusUpdateRequest request) {
        MedicineBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));

        BatchStatus oldStatus = batch.getStatus();
        BatchStatus newStatus = request.getStatus();

        // If moving from ACTIVE to a non-dispensable status, subtract from inventory total
        if (oldStatus == BatchStatus.ACTIVE &&
                (newStatus == BatchStatus.QUARANTINED || newStatus == BatchStatus.RECALLED)) {
            Inventory inventory = batch.getInventory();
            inventory.setTotalQuantity(
                    Math.max(0, inventory.getTotalQuantity() - batch.getQuantityRemaining()));
            inventoryRepository.save(inventory);
        }

        batch.setStatus(newStatus);
        batchRepository.save(batch);

        // Determine audit event type
        String eventType = switch (newStatus) {
            case QUARANTINED -> "BATCH_QUARANTINED";
            case RECALLED    -> "BATCH_RECALLED";
            case EXPIRED     -> "BATCH_EXPIRED";
            default          -> "BATCH_STATUS_CHANGED";
        };

        // Record transaction if stock is being removed
        if (newStatus == BatchStatus.QUARANTINED || newStatus == BatchStatus.RECALLED) {
            TransactionType txnType = newStatus == BatchStatus.RECALLED
                    ? TransactionType.RECALL : TransactionType.DISPOSE;
            recordTransaction(batch, txnType, batch.getQuantityRemaining(),
                    request.getReason(), null);
        }

        auditEventService.logEvent(
                eventType,
                currentUsername(),
                "Batch " + batch.getBatchNumber() + " status changed: " + oldStatus + " → " + newStatus,
                "batchId=" + batchId + ", reason=" + request.getReason(),
                null
        );

        return mapToResponse(batch);
    }

    // -------------------------------------------------------
    // Stock summary
    // -------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<StockSummaryResponse> getStockSummary(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(inv -> {
            Medicine med = inv.getMedicine();
            long activeBatchCount = inv.getBatches().stream()
                    .filter(b -> b.getStatus() == BatchStatus.ACTIVE)
                    .count();
            boolean lowStock = inv.getTotalQuantity() <= med.getReorderLevel();
            return new StockSummaryResponse(
                    med.getId(), med.getName(), med.getGenericName(),
                    inv.getTotalQuantity(), med.getReorderLevel(),
                    lowStock, activeBatchCount
            );
        });
    }

    // -------------------------------------------------------
    // Internal helpers (also used by DispensingService)
    // -------------------------------------------------------

    /**
     * Records an inventory ledger entry. Called after each stock movement.
     *
     * @param batch       the affected batch
     * @param type        movement type
     * @param quantity    units moved (always positive)
     * @param notes       optional reason / notes
     * @param referenceId optional cross-table reference (e.g. dispensation ID)
     */
    public void recordTransaction(MedicineBatch batch, TransactionType type,
                                   int quantity, String notes, String referenceId) {
        InventoryTransaction txn = new InventoryTransaction();
        txn.setBatch(batch);
        txn.setTransactionType(type);
        txn.setQuantity(quantity);
        txn.setPerformedBy(currentUsername());
        txn.setNotes(notes);
        txn.setReferenceId(referenceId);
        transactionRepository.save(txn);
    }

    // -------------------------------------------------------
    // Mapping
    // -------------------------------------------------------

    public BatchResponse mapToResponse(MedicineBatch batch) {
        Medicine med = batch.getInventory().getMedicine();
        return new BatchResponse(
                batch.getId(),
                batch.getInventory().getId(),
                med.getId(),
                med.getName(),
                batch.getBatchNumber(),
                batch.getManufacturer(),
                batch.getManufacturingDate(),
                batch.getExpiryDate(),
                batch.getQuantityReceived(),
                batch.getQuantityRemaining(),
                batch.getUnitCost(),
                batch.getStatus(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }

    // -------------------------------------------------------
    // Security helper
    // -------------------------------------------------------

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}
