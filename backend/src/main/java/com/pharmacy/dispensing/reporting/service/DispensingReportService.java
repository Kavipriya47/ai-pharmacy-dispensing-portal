package com.pharmacy.dispensing.reporting.service;

import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import com.pharmacy.dispensing.reporting.dto.DispensingStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DispensingReportService {

    @Autowired
    private DispensationRepository dispensationRepository;

    @Autowired
    private com.pharmacy.dispensing.audit.repository.AuditEventRepository auditEventRepository;

    @Transactional(readOnly = true)
    public DispensingStatsDto getSummary(LocalDateTime from, LocalDateTime to) {
        DispensingStatsDto dto = new DispensingStatsDto();
        long completed = dispensationRepository.countByStatusAndDateRange(com.pharmacy.dispensing.dispensing.entity.DispensationStatus.COMPLETED, from, to);
        long cancelled = dispensationRepository.countByStatusAndDateRange(com.pharmacy.dispensing.dispensing.entity.DispensationStatus.CANCELLED, from, to);
        long totalQty = dispensationRepository.sumQuantityDispensedInRange(from, to);
        long failed = auditEventRepository.countDispenseFailuresInRange(from, to);
        dto.setCompletedCount(completed);
        dto.setCancelledCount(cancelled);
        dto.setTotalQuantityDispensed(totalQty);
        dto.setFailedCount(failed);
        return dto;
    }

    @Transactional(readOnly = true)
    public java.util.List<Object[]> byMedicine(LocalDateTime from, LocalDateTime to) {
        return dispensationRepository.countByMedicineInRange(from, to);
    }

    @Transactional(readOnly = true)
    public java.util.List<Object[]> byPharmacist(LocalDateTime from, LocalDateTime to) {
        return dispensationRepository.countByPharmacistInRange(from, to);
    }
}
