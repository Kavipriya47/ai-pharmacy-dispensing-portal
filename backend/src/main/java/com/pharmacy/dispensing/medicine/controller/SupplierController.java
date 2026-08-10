package com.pharmacy.dispensing.medicine.controller;

import com.pharmacy.dispensing.medicine.dto.SupplierRequest;
import com.pharmacy.dispensing.medicine.dto.SupplierResponse;
import com.pharmacy.dispensing.medicine.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for managing pharmaceutical suppliers.
 * Only ADMIN role can create, update, or deactivate suppliers.
 */
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<SupplierResponse>> findAll() {
        return ResponseEntity.ok(supplierService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
