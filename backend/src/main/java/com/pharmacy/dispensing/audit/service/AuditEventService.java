package com.pharmacy.dispensing.audit.service;

import com.pharmacy.dispensing.audit.dto.AuditEventDto;
import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void logEvent(String eventType, String performedBy, String description, String metadata, String ipAddress) {
        AuditEvent event = new AuditEvent(eventType, performedBy, description, metadata, ipAddress);
        auditEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityOrFailureEvent(String eventType, String performedBy, String description, String metadata, String ipAddress) {
        AuditEvent event = new AuditEvent(eventType, performedBy, description, metadata, ipAddress);
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AuditEventDto> getRecentEvents() {
        return auditEventRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDto> getEvents(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return auditEventRepository.findAll(pageRequest).map(this::mapToDto);
    }

    private AuditEventDto mapToDto(AuditEvent event) {
        return new AuditEventDto(
                event.getId(),
                event.getEventType(),
                event.getPerformedBy(),
                event.getDescription(),
                event.getMetadata(),
                event.getIpAddress(),
                event.getCreatedAt()
        );
    }
}
