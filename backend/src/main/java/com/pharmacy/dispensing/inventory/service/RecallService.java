package com.pharmacy.dispensing.inventory.service;

import com.pharmacy.dispensing.audit.service.AuditEventService;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.Inventory;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.entity.TransactionType;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.notification.entity.NotificationSeverity;
import com.pharmacy.dispensing.notification.entity.NotificationType;
import com.pharmacy.dispensing.notification.service.NotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecallService {

    private final MedicineBatchRepository batchRepository;
    private final InventoryRepository inventoryRepository;
    private final MedicineBatchService batchService;
    private final NotificationService notificationService;
    private final AuditEventService auditEventService;

    public RecallService(MedicineBatchRepository batchRepository,
                         InventoryRepository inventoryRepository,
                         MedicineBatchService batchService,
                         NotificationService notificationService,
                         AuditEventService auditEventService) {
        this.batchRepository = batchRepository;
        this.inventoryRepository = inventoryRepository;
        this.batchService = batchService;
        this.notificationService = notificationService;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public MedicineBatch initiateRecall(String batchNumber, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Recall requires a documented reason.");
        }

        MedicineBatch batch = batchRepository.findByBatchNumber(batchNumber)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchNumber));

        if (batch.getStatus() == BatchStatus.RECALLED) {
            throw new IllegalArgumentException("Batch is already recalled.");
        }

        // Deduct from inventory since it can no longer be dispensed
        Inventory inventory = batch.getInventory();
        int remainingQty = batch.getQuantityRemaining();
        
        if (batch.getStatus() == BatchStatus.ACTIVE) {
            inventory.setTotalQuantity(inventory.getTotalQuantity() - remainingQty);
            inventoryRepository.save(inventory);
            
            // Record ledger transaction for the deduction
            batchService.recordTransaction(batch, TransactionType.RECALL, remainingQty, "Recall Initiation: " + reason, null);
        }

        batch.setStatus(BatchStatus.RECALLED);
        batch = batchRepository.save(batch);

        // Generate Audit Event
        auditEventService.logEvent(
                "RECALL_INITIATED",
                currentUsername(),
                "Batch " + batchNumber + " was officially recalled. Reason: " + reason,
                "batchId=" + batch.getId() + ", quantityLost=" + remainingQty,
                null
        );

        // Broadcast global critical notification
        notificationService.createNotification(
                NotificationType.BATCH_RECALLED,
                NotificationSeverity.CRITICAL,
                "CRITICAL RECALL: " + batch.getBatchNumber(),
                "Batch " + batch.getBatchNumber() + " (" + inventory.getMedicine().getName() + ") has been officially recalled. Reason: " + reason + ". Dispensing is blocked immediately.",
                null, // null recipient = global broadcast
                "MedicineBatch",
                String.valueOf(batch.getId())
        );

        return batch;
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}
