package com.pharmacy.dispensing.dispensing.dto;

import com.pharmacy.dispensing.dispensing.entity.DispensationStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for a dispensation record.
 */
public class DispensationResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private Long batchId;
    private String batchNumber;
    private String patientIdentifier;
    private String prescriptionReference;
    private Integer quantityDispensed;
    private String dispensedBy;
    private DispensationStatus status;
    private Boolean fefoOverride;
    private String overrideReason;
    private LocalDateTime dispensedAt;

    public DispensationResponse() {}

    public DispensationResponse(Long id, Long medicineId, String medicineName, Long batchId,
                                String batchNumber, String patientIdentifier, String prescriptionReference,
                                Integer quantityDispensed, String dispensedBy, DispensationStatus status,
                                Boolean fefoOverride, String overrideReason, LocalDateTime dispensedAt) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.patientIdentifier = patientIdentifier;
        this.prescriptionReference = prescriptionReference;
        this.quantityDispensed = quantityDispensed;
        this.dispensedBy = dispensedBy;
        this.status = status;
        this.fefoOverride = fefoOverride;
        this.overrideReason = overrideReason;
        this.dispensedAt = dispensedAt;
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getPatientIdentifier() { return patientIdentifier; }
    public void setPatientIdentifier(String patientIdentifier) { this.patientIdentifier = patientIdentifier; }

    public String getPrescriptionReference() { return prescriptionReference; }
    public void setPrescriptionReference(String prescriptionReference) { this.prescriptionReference = prescriptionReference; }

    public Integer getQuantityDispensed() { return quantityDispensed; }
    public void setQuantityDispensed(Integer quantityDispensed) { this.quantityDispensed = quantityDispensed; }

    public String getDispensedBy() { return dispensedBy; }
    public void setDispensedBy(String dispensedBy) { this.dispensedBy = dispensedBy; }

    public DispensationStatus getStatus() { return status; }
    public void setStatus(DispensationStatus status) { this.status = status; }

    public Boolean getFefoOverride() { return fefoOverride; }
    public void setFefoOverride(Boolean fefoOverride) { this.fefoOverride = fefoOverride; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public LocalDateTime getDispensedAt() { return dispensedAt; }
    public void setDispensedAt(LocalDateTime dispensedAt) { this.dispensedAt = dispensedAt; }
}
