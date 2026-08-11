package com.pharmacy.dispensing.medicine.controller;

import com.pharmacy.dispensing.medicine.dto.MedicineRequest;
import com.pharmacy.dispensing.medicine.dto.MedicineResponse;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import com.pharmacy.dispensing.medicine.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for the Medicine catalog.
 * <p>
 * Reading medicines is open to any authenticated user.
 * Creating, updating, and soft-deleting require PHARMACIST or ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<MedicineResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MedicineCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return ResponseEntity.ok(medicineService.findAll(search, category, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<MedicineResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    /**
     * Soft-delete: sets the medicine as inactive.
     * The record is retained for historical dispensation traceability.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        medicineService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
