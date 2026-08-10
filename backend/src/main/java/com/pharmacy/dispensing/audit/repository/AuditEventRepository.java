package com.pharmacy.dispensing.audit.repository;

import com.pharmacy.dispensing.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Page<AuditEvent> findByEventType(String eventType, Pageable pageable);
    Page<AuditEvent> findByPerformedBy(String performedBy, Pageable pageable);
    List<AuditEvent> findTop50ByOrderByCreatedAtDesc();

    // --- Reporting queries ---

    @Query("SELECT COUNT(a) FROM AuditEvent a WHERE a.eventType = 'DISPENSE_FAILED' AND a.createdAt >= :from AND a.createdAt <= :to")
    long countDispenseFailuresInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a FROM AuditEvent a WHERE " +
           "(:performedBy IS NULL OR a.performedBy = :performedBy) AND " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "a.createdAt >= :from AND a.createdAt <= :to " +
           "ORDER BY a.createdAt DESC")
    Page<AuditEvent> findFiltered(@Param("performedBy") String performedBy,
                                  @Param("eventType") String eventType,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to,
                                  Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.eventType = 'RECALL_INITIATED' ORDER BY a.createdAt DESC")
    List<AuditEvent> findAllRecallInitiatedEvents();
}
