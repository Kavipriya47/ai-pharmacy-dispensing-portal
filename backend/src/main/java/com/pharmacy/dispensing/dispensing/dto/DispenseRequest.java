package com.pharmacy.dispensing.dispensing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload to dispense medication to a patient.
 */
public class DispenseRequest {

    @NotNull(message = "medicineId is required")
    private Long medicineId;

    /**
     * Optional. If provided, overrides FEFO logic.
     * The override reason must be provided if batchId is specified.
     */
    private Long batchId;

    @NotBlank(message = "patientIdentifier is required")
    @Size(max = 100)
    private String patientIdentifier;

    @Size(max = 200)
    private String prescriptionReference;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    private String overrideReason;

    private String notes;

    // ---- Getters & Setters ----

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getPatientIdentifier() { return patientIdentifier; }
    public void setPatientIdentifier(String patientIdentifier) { this.patientIdentifier = patientIdentifier; }

    public String getPrescriptionReference() { return prescriptionReference; }
    public void setPrescriptionReference(String prescriptionReference) { this.prescriptionReference = prescriptionReference; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
