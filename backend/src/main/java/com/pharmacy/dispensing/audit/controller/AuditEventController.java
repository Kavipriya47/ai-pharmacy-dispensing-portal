package com.pharmacy.dispensing.audit.controller;

import com.pharmacy.dispensing.audit.dto.AuditEventDto;
import com.pharmacy.dispensing.audit.service.AuditEventService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<AuditEventDto>> getRecentEvents() {
        return ResponseEntity.ok(auditEventService.getRecentEvents());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Page<AuditEventDto>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditEventService.getEvents(page, size));
    }
}
