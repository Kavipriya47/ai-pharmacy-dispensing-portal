package com.pharmacy.dispensing.inventory.repository;

import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Long> {

    Page<MedicineBatch> findByInventoryId(Long inventoryId, Pageable pageable);

    java.util.Optional<MedicineBatch> findByBatchNumber(String batchNumber);

    Page<MedicineBatch> findByStatus(BatchStatus status, Pageable pageable);

    @Query("""
            SELECT b FROM MedicineBatch b
            WHERE (:status IS NULL OR b.status = :status)
              AND (:medicineId IS NULL OR b.inventory.medicine.id = :medicineId)
              AND (:search IS NULL OR :search = '' 
                   OR LOWER(b.batchNumber) LIKE LOWER(CONCAT('%', :search, '%')) 
                   OR LOWER(b.inventory.medicine.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<MedicineBatch> searchAndFilter(@Param("search") String search, @Param("status") BatchStatus status, @Param("medicineId") Long medicineId, Pageable pageable);

    // --- Reporting queries ---
    long countByStatus(BatchStatus status);

    @Query("SELECT COALESCE(SUM(b.quantityRemaining), 0) FROM MedicineBatch b WHERE b.status = 'ACTIVE'")
    long sumTotalLiveStock();

    @Query("SELECT b FROM MedicineBatch b JOIN FETCH b.inventory i JOIN FETCH i.medicine WHERE b.status = 'RECALLED' ORDER BY b.updatedAt DESC")
    java.util.List<MedicineBatch> findAllRecalledWithMedicine();

    /**
     * FEFO query: returns active, non-expired batches for a medicine ordered by
     * expiry date ascending (earliest-expiring first).
     * Used by {@link com.pharmacy.dispensing.inventory.service.MedicineBatchService#selectFefoBatch}.
     */
    @Query("""
            SELECT b FROM MedicineBatch b
            WHERE b.inventory.medicine.id = :medicineId
              AND b.status = 'ACTIVE'
              AND b.expiryDate > :today
              AND b.quantityRemaining > 0
            ORDER BY b.expiryDate ASC
            """)
    List<MedicineBatch> findFefoBatches(@Param("medicineId") Long medicineId,
                                         @Param("today") LocalDate today);

    /**
     * Batches expiring within the next {@code days} days with status ACTIVE.
     * Used for low-expiry alerts in the dashboard.
     */
    @Query("""
            SELECT b FROM MedicineBatch b
            WHERE b.status = 'ACTIVE'
              AND b.expiryDate BETWEEN :today AND :cutoff
            ORDER BY b.expiryDate ASC
            """)
    Page<MedicineBatch> findExpiringSoon(@Param("today") LocalDate today,
                                          @Param("cutoff") LocalDate cutoff,
                                          Pageable pageable);

    /**
     * All ACTIVE batches whose expiry date is in the past.
     * Used by {@link com.pharmacy.dispensing.inventory.service.ExpiryCheckScheduler}.
     */
    @Query("""
            SELECT b FROM MedicineBatch b
            WHERE b.status = 'ACTIVE'
              AND b.expiryDate < :today
            """)
    List<MedicineBatch> findExpiredActiveBatches(@Param("today") LocalDate today);

    /**
     * All batches that are expired (regardless of when they expired).
     * Used by the auditor dashboard endpoint.
     */
    Page<MedicineBatch> findByStatusIn(List<BatchStatus> statuses, Pageable pageable);
}
