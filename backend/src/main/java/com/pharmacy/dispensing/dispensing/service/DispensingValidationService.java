package com.pharmacy.dispensing.dispensing.service;

import com.pharmacy.dispensing.common.exception.ResourceNotFoundException;
import com.pharmacy.dispensing.dispensing.dto.DispenseRequest;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Encapsulates the 6 core business rules for medication dispensing.
 * Extracted from the main DispensingService to ensure business logic
 * is independently testable and decoupled from transaction orchestration.
 */
@Service
public class DispensingValidationService {

    /**
     * Validates all ADCE business rules against a selected batch before dispensing.
     *
     * @param request The original dispense request
     * @param medicine The target medicine
     * @param batch The selected batch (either FEFO-driven or manually overridden)
     * @throws IllegalArgumentException if any rule fails
     */
    public void validate(DispenseRequest request, Medicine medicine, MedicineBatch batch) {
        // Rule 1: quantity > 0
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Requested quantity must be strictly positive");
        }

        // Rule 2: Explicitly reject RECALLED or QUARANTINED batches
        if (batch.getStatus() == BatchStatus.RECALLED) {
            throw new IllegalArgumentException("CRITICAL SAFETY VIOLATION: Cannot dispense from a RECALLED batch.");
        }
        if (batch.getStatus() == BatchStatus.QUARANTINED) {
            throw new IllegalArgumentException("SAFETY VIOLATION: Cannot dispense from a QUARANTINED batch.");
        }

        // Rule 3: batch.status == ACTIVE
        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot dispense from a non-ACTIVE batch. Current status: " + batch.getStatus());
        }

        // Rule 4: batch.expiryDate > TODAY
        if (!batch.getExpiryDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot dispense an expired batch (Expired on: " + batch.getExpiryDate() + ")");
        }

        // Rule 5: batch.quantity >= requestedQuantity
        if (batch.getQuantityRemaining() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock in batch " + batch.getBatchNumber()
                    + ". Requested: " + request.getQuantity() + ", Available: " + batch.getQuantityRemaining());
        }

        // Rule 6: Prescription Enforcement
        if (Boolean.TRUE.equals(medicine.getRequiresPrescription())) {
            if (request.getPrescriptionReference() == null || request.getPrescriptionReference().isBlank()) {
                throw new IllegalArgumentException("PRESCRIPTION_REQUIRED: This medication requires a valid prescription reference.");
            }
        }
    }
}
