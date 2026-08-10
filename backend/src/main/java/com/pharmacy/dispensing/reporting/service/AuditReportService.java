package com.pharmacy.dispensing.reporting.service;

import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.reporting.dto.AuditReportRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditReportService {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Transactional(readOnly = true)
    public List<AuditReportRow> getReport(String performedBy, String eventType, LocalDateTime from, LocalDateTime to) {
        Page<AuditEvent> page = auditEventRepository.findFiltered(
                performedBy != null && !performedBy.trim().isEmpty() ? performedBy.trim() : null,
                eventType != null && !eventType.trim().isEmpty() ? eventType.trim() : null,
                from,
                to,
                PageRequest.of(0, 5000)
        );

        return page.getContent().stream()
                .map(a -> new AuditReportRow(
                        a.getId(),
                        a.getEventType(),
                        a.getPerformedBy(),
                        a.getDescription(),
                        a.getMetadata(),
                        a.getIpAddress(),
                        a.getCreatedAt()
                ))
                .toList();
    }
}
