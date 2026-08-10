package com.pharmacy.dispensing.inventory.repository;

import com.pharmacy.dispensing.inventory.entity.InventoryTransaction;
import com.pharmacy.dispensing.inventory.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /** All transactions for a given batch (for per-batch audit trail). */
    Page<InventoryTransaction> findByBatchId(Long batchId, Pageable pageable);

    /** All transactions performed by a specific user. */
    Page<InventoryTransaction> findByPerformedBy(String performedBy, Pageable pageable);

    /** All transactions of a specific type (e.g., DISPENSE only). */
    Page<InventoryTransaction> findByTransactionType(TransactionType type, Pageable pageable);
}
