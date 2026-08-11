package com.pharmacy.dispensing.dispensing.repository;

import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.entity.DispensationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DispensationRepository extends JpaRepository<DispensationRecord, Long> {

    Page<DispensationRecord> findByPatientIdentifier(String patientIdentifier, Pageable pageable);

    Page<DispensationRecord> findByDispensedBy(String dispensedBy, Pageable pageable);

    Page<DispensationRecord> findByMedicineId(Long medicineId, Pageable pageable);

    Page<DispensationRecord> findByBatchId(Long batchId, Pageable pageable);

    long countByBatchId(Long batchId);

    // --- Reporting queries ---

    long countByStatus(DispensationStatus status);

    @Query("SELECT COUNT(d) FROM DispensationRecord d WHERE d.status = :status AND d.dispensedAt >= :from AND d.dispensedAt <= :to")
    long countByStatusAndDateRange(@Param("status") DispensationStatus status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(d.quantityDispensed), 0) FROM DispensationRecord d WHERE d.status = 'COMPLETED' AND d.dispensedAt >= :from AND d.dispensedAt <= :to")
    long sumQuantityDispensedInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT d.medicine.name, COUNT(d), SUM(d.quantityDispensed) FROM DispensationRecord d WHERE d.status = 'COMPLETED' AND d.dispensedAt >= :from AND d.dispensedAt <= :to GROUP BY d.medicine.name ORDER BY COUNT(d) DESC")
    List<Object[]> countByMedicineInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT d.dispensedBy, COUNT(d) FROM DispensationRecord d WHERE d.status = 'COMPLETED' AND d.dispensedAt >= :from AND d.dispensedAt <= :to GROUP BY d.dispensedBy ORDER BY COUNT(d) DESC")
    List<Object[]> countByPharmacistInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    Page<DispensationRecord> findByDispensedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("""
            SELECT d FROM DispensationRecord d
            WHERE (:startDate IS NULL OR d.dispensedAt >= :startDate)
              AND (:endDate IS NULL OR d.dispensedAt <= :endDate)
              AND (:medicineId IS NULL OR d.medicine.id = :medicineId)
              AND (:status IS NULL OR d.status = :status)
            """)
    Page<DispensationRecord> searchAndFilter(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("medicineId") Long medicineId,
                                             @Param("status") DispensationStatus status,
                                             Pageable pageable);
}
