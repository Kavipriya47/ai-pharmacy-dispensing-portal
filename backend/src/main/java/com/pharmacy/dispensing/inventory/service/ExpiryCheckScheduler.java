package com.pharmacy.dispensing.inventory.service;

import com.pharmacy.dispensing.audit.service.AuditEventService;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.entity.TransactionType;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.notification.entity.NotificationSeverity;
import com.pharmacy.dispensing.notification.entity.NotificationType;
import com.pharmacy.dispensing.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Nightly scheduler that automatically transitions ACTIVE batches to EXPIRED
 * once their {@code expiryDate} has passed.
 * <p>
 * Runs at 00:05 daily (5 minutes past midnight) to ensure the day rollover
 * has fully completed before processing.
 * <p>
 * For each newly expired batch:
 * <ol>
 *   <li>Status set to {@link BatchStatus#EXPIRED}</li>
 *   <li>{@link com.pharmacy.dispensing.inventory.entity.Inventory#getTotalQuantity()} decremented</li>
 *   <li>A {@link TransactionType#DISPOSE} ledger entry created</li>
 *   <li>A {@code BATCH_EXPIRED} audit event emitted</li>
 * </ol>
 */
@Component
public class ExpiryCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckScheduler.class);

    private final MedicineBatchRepository batchRepository;
    private final InventoryRepository inventoryRepository;
    private final MedicineBatchService batchService;
    private final AuditEventService auditEventService;
    private final NotificationService notificationService;

    public ExpiryCheckScheduler(MedicineBatchRepository batchRepository,
                                InventoryRepository inventoryRepository,
                                MedicineBatchService batchService,
                                AuditEventService auditEventService,
                                NotificationService notificationService) {
        this.batchRepository = batchRepository;
        this.inventoryRepository = inventoryRepository;
        this.batchService = batchService;
        this.auditEventService = auditEventService;
        this.notificationService = notificationService;
    }

    /**
     * Runs at 00:05 every day.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void markExpiredBatches() {
        LocalDate today = LocalDate.now();
        List<MedicineBatch> expiredBatches = batchRepository.findExpiredActiveBatches(today);

        if (expiredBatches.isEmpty()) {
            log.info("[ExpiryCheck] No newly expired batches found for date: {}", today);
            return;
        }

        log.warn("[ExpiryCheck] Found {} batch(es) to expire for date: {}", expiredBatches.size(), today);

        for (MedicineBatch batch : expiredBatches) {
            try {
                markBatchExpired(batch);
            } catch (Exception e) {
                // Log and continue — do not allow one bad batch to block the rest
                log.error("[ExpiryCheck] Failed to expire batch id={}, batchNumber={}: {}",
                        batch.getId(), batch.getBatchNumber(), e.getMessage(), e);
            }
        }

        log.info("[ExpiryCheck] Completed expiry check. {} batch(es) processed.", expiredBatches.size());
    }

    private void markBatchExpired(MedicineBatch batch) {
        int quantityLost = batch.getQuantityRemaining();

        // Transition status
        batch.setStatus(BatchStatus.EXPIRED);
        batchRepository.save(batch);

        // Decrement inventory total
        var inventory = batch.getInventory();
        inventory.setTotalQuantity(Math.max(0, inventory.getTotalQuantity() - quantityLost));
        inventoryRepository.save(inventory);

        // Record DISPOSE transaction (expired stock written off)
        if (quantityLost > 0) {
            batchService.recordTransaction(batch, TransactionType.DISPOSE, quantityLost,
                    "Auto-expired by nightly scheduler on " + LocalDate.now(), null);
        }

        // Emit audit event
        auditEventService.logEvent(
                "BATCH_EXPIRED",
                "SYSTEM",
                "Batch auto-expired: " + batch.getBatchNumber()
                        + " (medicine: " + inventory.getMedicine().getName() + ")",
                "batchId=" + batch.getId() + ", quantityLost=" + quantityLost
                        + ", expiryDate=" + batch.getExpiryDate(),
                null
        );

        // Emit notification
        notificationService.createNotification(
                NotificationType.BATCH_EXPIRED,
                NotificationSeverity.CRITICAL,
                "Batch Expired: " + batch.getBatchNumber(),
                "Batch " + batch.getBatchNumber() + " has passed its expiry date and has been marked as EXPIRED.",
                null,
                "MedicineBatch",
                String.valueOf(batch.getId())
        );

        log.info("[ExpiryCheck] Expired batch id={}, batchNumber={}, medicine={}, quantityLost={}",
                batch.getId(), batch.getBatchNumber(),
                inventory.getMedicine().getName(), quantityLost);
    }

    /**
     * Runs daily at 00:10.
     * Finds batches expiring within the next 30 days and generates notifications.
     */
    @Scheduled(cron = "0 10 0 * * ?")
    @Transactional
    public void generateNearExpiryWarnings() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate thresholdDate = today.plusDays(30);
        java.util.List<MedicineBatch> expiringSoon = batchRepository.findExpiringSoon(today, thresholdDate, org.springframework.data.domain.Pageable.unpaged()).getContent();

        for (MedicineBatch batch : expiringSoon) {
            notificationService.createNotification(
                    com.pharmacy.dispensing.notification.entity.NotificationType.NEAR_EXPIRY,
                    com.pharmacy.dispensing.notification.entity.NotificationSeverity.WARNING,
                    "Batch Expiring Soon: " + batch.getBatchNumber(),
                    "Batch " + batch.getBatchNumber() + " is set to expire on " + batch.getExpiryDate() + ".",
                    null,
                    "MedicineBatch",
                    String.valueOf(batch.getId())
            );
        }
        log.info("[ExpiryCheck] Generated {} near-expiry warnings.", expiringSoon.size());
    }
}
