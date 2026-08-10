package com.pharmacy.dispensing.dispensing.entity;

import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Record of medication dispensed to a patient.
 * <p>
 * This is the operational core of the ADCE (Adaptive Dispensing Compliance Engine).
 * Links exactly which medicine from which batch was dispensed to whom.
 */
@Entity
@Table(name = "dispensation_records")
public class DispensationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private MedicineBatch batch;

    @Column(name = "patient_identifier", nullable = false, length = 100)
    private String patientIdentifier;

    @Column(name = "prescription_reference", length = 200)
    private String prescriptionReference;

    @Column(name = "quantity_dispensed", nullable = false)
    private Integer quantityDispensed;

    @Column(name = "dispensed_by", nullable = false, length = 100)
    private String dispensedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DispensationStatus status = DispensationStatus.COMPLETED;

    @Column(name = "fefo_override", nullable = false)
    private Boolean fefoOverride = Boolean.FALSE;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "dispensed_at", updatable = false)
    private LocalDateTime dispensedAt;

    @PrePersist
    protected void onCreate() {
        dispensedAt = LocalDateTime.now();
    }

    public DispensationRecord() {}

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public MedicineBatch getBatch() { return batch; }
    public void setBatch(MedicineBatch batch) { this.batch = batch; }

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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getDispensedAt() { return dispensedAt; }
    public void setDispensedAt(LocalDateTime dispensedAt) { this.dispensedAt = dispensedAt; }
}
