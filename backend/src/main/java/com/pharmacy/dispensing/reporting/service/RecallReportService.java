package com.pharmacy.dispensing.reporting.service;

import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import com.pharmacy.dispensing.reporting.dto.RecallHistoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecallReportService {

    @Autowired
    private MedicineBatchRepository batchRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private DispensationRepository dispensationRepository;

    @Transactional(readOnly = true)
    public List<RecallHistoryDto> getRecallHistory() {
        List<MedicineBatch> recalledBatches = batchRepository.findAllRecalledWithMedicine();
        List<AuditEvent> recallEvents = auditEventRepository.findAllRecallInitiatedEvents();

        List<RecallHistoryDto> history = new ArrayList<>();
        for (MedicineBatch batch : recalledBatches) {
            RecallHistoryDto dto = new RecallHistoryDto();
            dto.setBatchNumber(batch.getBatchNumber());
            dto.setMedicineName(batch.getInventory().getMedicine().getName());

            // Default values if no audit event found
            dto.setRecallDate(batch.getUpdatedAt());
            dto.setRecalledBy("SYSTEM");
            dto.setReason("Not documented");

            // Look for matching audit event
            for (AuditEvent event : recallEvents) {
                boolean match = false;
                // Match by metadata containing batchId
                if (event.getMetadata() != null && event.getMetadata().contains("batchId=" + batch.getId())) {
                    match = true;
                }
                // Or match by description containing batch number
                else if (event.getDescription() != null && event.getDescription().contains(batch.getBatchNumber())) {
                    match = true;
                }

                if (match) {
                    dto.setRecallDate(event.getCreatedAt());
                    dto.setRecalledBy(event.getPerformedBy());
                    String desc = event.getDescription();
                    if (desc != null) {
                        int idx = desc.indexOf("Reason: ");
                        if (idx != -1) {
                            dto.setReason(desc.substring(idx + "Reason: ".length()).trim());
                        } else {
                            dto.setReason(desc);
                        }
                    }
                    break;
                }
            }

            // Count completed dispensations for this batch
            long affectedCount = dispensationRepository.countByBatchId(batch.getId());
            dto.setAffectedDispensationCount(affectedCount);

            history.add(dto);
        }

        return history;
    }
}
