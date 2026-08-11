package com.pharmacy.dispensing.dispensing.controller;

import com.pharmacy.dispensing.dispensing.dto.DispensationResponse;
import com.pharmacy.dispensing.dispensing.dto.DispenseRequest;
import com.pharmacy.dispensing.dispensing.service.DispensingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dispensing")
public class DispensingController {

    private final DispensingService dispensingService;

    public DispensingController(DispensingService dispensingService) {
        this.dispensingService = dispensingService;
    }

    /**
     * POST /api/v1/dispensing
     * Dispense medication to a patient.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<DispensationResponse> dispense(@Valid @RequestBody DispenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dispensingService.dispense(request));
    }

    /**
     * GET /api/v1/dispensing
     * List all dispensation records.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'AUDITOR')")
    public ResponseEntity<Page<DispensationResponse>> findAll(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(required = false) Long medicineId,
            @RequestParam(required = false) com.pharmacy.dispensing.dispensing.entity.DispensationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("dispensedAt").descending());
        return ResponseEntity.ok(dispensingService.findAll(startDate, endDate, medicineId, status, pageable));
    }

    /**
     * GET /api/v1/dispensing/{id}
     * Get a specific dispensation record.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'AUDITOR')")
    public ResponseEntity<DispensationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(dispensingService.findById(id));
    }

    /**
     * GET /api/v1/dispensing/patient/{patientIdentifier}
     * Get dispensation history for a specific patient.
     */
    @GetMapping("/patient/{patientIdentifier}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'AUDITOR')")
    public ResponseEntity<Page<DispensationResponse>> findByPatient(
            @PathVariable String patientIdentifier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("dispensedAt").descending());
        return ResponseEntity.ok(dispensingService.findByPatient(patientIdentifier, pageable));
    }
}
