package com.pharmacy.dispensing.medicine.service;

import com.pharmacy.dispensing.audit.service.AuditEventService;
import com.pharmacy.dispensing.common.exception.ResourceNotFoundException;
import com.pharmacy.dispensing.medicine.dto.MedicineRequest;
import com.pharmacy.dispensing.medicine.dto.MedicineResponse;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import com.pharmacy.dispensing.medicine.entity.Supplier;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import com.pharmacy.dispensing.medicine.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;
    private final AuditEventService auditEventService;

    public MedicineService(MedicineRepository medicineRepository,
                           SupplierRepository supplierRepository,
                           AuditEventService auditEventService) {
        this.medicineRepository = medicineRepository;
        this.supplierRepository = supplierRepository;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        Medicine medicine = new Medicine();
        applyRequest(request, medicine);
        Medicine saved = medicineRepository.save(medicine);

        auditEventService.logEvent(
                "MEDICINE_CREATED",
                currentUsername(),
                "Medicine catalog entry created: " + saved.getName(),
                "medicineId=" + saved.getId(),
                null
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<MedicineResponse> findAll(String search, MedicineCategory category, Pageable pageable) {
        return medicineRepository.searchAndFilterActive(search, category, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MedicineResponse findById(Long id) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return mapToResponse(medicine);
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        applyRequest(request, medicine);
        Medicine saved = medicineRepository.save(medicine);

        auditEventService.logEvent(
                "MEDICINE_UPDATED",
                currentUsername(),
                "Medicine catalog entry updated: " + saved.getName(),
                "medicineId=" + saved.getId(),
                null
        );

        return mapToResponse(saved);
    }

    /**
     * Soft-delete: sets {@code active = false}. The medicine record is retained
     * for historical dispensation traceability.
     */
    @Transactional
    public void deactivate(Long id) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        medicine.setActive(false);
        medicineRepository.save(medicine);

        auditEventService.logEvent(
                "MEDICINE_DEACTIVATED",
                currentUsername(),
                "Medicine soft-deleted from catalog: " + medicine.getName(),
                "medicineId=" + id,
                null
        );
    }

    // ---- Mapping helpers ----

    /**
     * Applies request fields onto the entity. Resolves Supplier FK when supplierId is provided.
     */
    private void applyRequest(MedicineRequest request, Medicine medicine) {
        medicine.setName(request.getName());
        medicine.setGenericName(request.getGenericName());
        medicine.setCategory(request.getCategory());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setStrength(request.getStrength());
        medicine.setUnitOfMeasure(request.getUnitOfMeasure());
        medicine.setDescription(request.getDescription());
        medicine.setRequiresPrescription(
                request.getRequiresPrescription() != null ? request.getRequiresPrescription() : false);
        medicine.setReorderLevel(
                request.getReorderLevel() != null ? request.getReorderLevel() : 0);

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found with id: " + request.getSupplierId()));
            medicine.setSupplier(supplier);
        } else {
            medicine.setSupplier(null);
        }
    }

    public MedicineResponse mapToResponse(Medicine medicine) {
        Long supplierId = medicine.getSupplier() != null ? medicine.getSupplier().getId() : null;
        String supplierName = medicine.getSupplier() != null ? medicine.getSupplier().getName() : null;

        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getCategory(),
                medicine.getDosageForm(),
                medicine.getStrength(),
                medicine.getUnitOfMeasure(),
                medicine.getDescription(),
                medicine.getRequiresPrescription(),
                medicine.getReorderLevel(),
                medicine.getActive(),
                medicine.getCreatedAt(),
                medicine.getUpdatedAt(),
                supplierId,
                supplierName
        );
    }

    // ---- Security helper ----

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}
