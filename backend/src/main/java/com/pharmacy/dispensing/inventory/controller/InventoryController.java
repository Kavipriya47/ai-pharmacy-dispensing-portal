package com.pharmacy.dispensing.inventory.controller;

import com.pharmacy.dispensing.inventory.dto.*;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.service.MedicineBatchService;
import com.pharmacy.dispensing.inventory.service.RecallService;
import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for inventory batch management.
 * <p>
 * Base path: {@code /api/v1/inventory}
 * <ul>
 *   <li>Receiving stock → ADMIN, PHARMACIST</li>
 *   <li>Reading batches → ADMIN, PHARMACIST, AUDITOR</li>
 *   <li>Updating batch status → ADMIN only</li>
 *   <li>Stock summary → all authenticated users</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final MedicineBatchService batchService;
    private final RecallService recallService;
    private final DispensationRepository dispensationRepository;

    public InventoryController(MedicineBatchService batchService,
                               RecallService recallService,
                               DispensationRepository dispensationRepository) {
        this.batchService = batchService;
        this.recallService = recallService;
        this.dispensationRepository = dispensationRepository;
    }

    // -------------------------------------------------------
    // Batch endpoints
    // -------------------------------------------------------

    /**
     * POST /api/v1/inventory/batches
     * Receive a new batch of stock.
     */
    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<BatchResponse> receiveStock(@Valid @RequestBody BatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.receiveStock(request));
    }

    /**
     * GET /api/v1/inventory/batches
     * List all batches (paginated).
     */
    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'AUDITOR')")
    public ResponseEntity<Page<BatchResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.pharmacy.dispensing.inventory.entity.BatchStatus status,
            @RequestParam(required = false) Long medicineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expiryDate") String sortBy) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return ResponseEntity.ok(batchService.findAll(search, status, medicineId, pageable));
    }

    /**
     * GET /api/v1/inventory/batches/{id}
     * Get a specific batch by ID.
     */
    @GetMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'AUDITOR')")
    public ResponseEntity<BatchResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.findById(id));
    }

    @PostMapping("/batches/{batchNumber}/recall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchResponse> initiateRecall(
            @PathVariable String batchNumber,
            @RequestBody java.util.Map<String, String> payload) {
        String reason = payload.get("reason");
        MedicineBatch recalled = recallService.initiateRecall(batchNumber, reason);
        return ResponseEntity.ok(batchService.mapToResponse(recalled));
    }

    @GetMapping("/batches/{batchNumber}/affected-patients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<String>> getAffectedPatients(@PathVariable String batchNumber) {
        // Query the DispensationRecord table to find all patients who received this batch
        java.util.List<String> patients = dispensationRepository.findAll().stream()
                .filter(r -> r.getBatch().getBatchNumber().equals(batchNumber))
                .map(DispensationRecord::getPatientIdentifier)
                .distinct()
                .toList();
        return ResponseEntity.ok(patients);
    }

    /**
     * GET /api/v1/inventory/batches/expiring-soon?days=30
     * List active batches expiring within the next N days.
     */
    @GetMapping("/batches/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Page<BatchResponse>> findExpiringSoon(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        return ResponseEntity.ok(batchService.findExpiringSoon(days, pageable));
    }

    /**
     * GET /api/v1/inventory/batches/expired
     * List all expired batches.
     */
    @GetMapping("/batches/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<Page<BatchResponse>> findExpired(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("expiryDate").descending());
        return ResponseEntity.ok(batchService.findExpired(pageable));
    }

    /**
     * PATCH /api/v1/inventory/batches/{id}/status
     * Update batch status (e.g., QUARANTINED, RECALLED).
     */
    @PatchMapping("/batches/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody BatchStatusUpdateRequest request) {
        return ResponseEntity.ok(batchService.updateStatus(id, request));
    }

    // -------------------------------------------------------
    // Stock summary
    // -------------------------------------------------------

    /**
     * GET /api/v1/inventory/stock-summary
     * Aggregated stock position for all medicines.
     */
    @GetMapping("/stock-summary")
    public ResponseEntity<Page<StockSummaryResponse>> getStockSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(batchService.getStockSummary(pageable));
    }
}
