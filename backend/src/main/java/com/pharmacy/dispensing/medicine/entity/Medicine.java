package com.pharmacy.dispensing.medicine.entity;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;

/**
 * Master catalog entry for a pharmaceutical medicine product.
 * <p>
 * Linked to {@link Supplier} via FK. Category and dosage form are persisted
 * as strings (enum name) so they are readable directly in the database.
 * <p>
 * {@code @Audited} ensures Hibernate Envers tracks all field-level changes
 * for regulatory compliance.
 */
@Entity
@Table(name = "medicines")
@Audited
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "generic_name", nullable = false, length = 200)
    private String genericName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MedicineCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "dosage_form", nullable = false, length = 50)
    private DosageForm dosageForm;

    /** e.g. "500mg", "5mg/5ml" */
    @Column(nullable = false, length = 50)
    private String strength;

    /** e.g. "tablet", "ml", "capsule" */
    @Column(name = "unit_of_measure", nullable = false, length = 30)
    private String unitOfMeasure;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * When true, {@link com.pharmacy.dispensing.dispensing.service.DispensingValidationService}
     * enforces that a non-blank {@code prescriptionReference} is supplied at dispense time.
     */
    @Column(name = "requires_prescription", nullable = false)
    private Boolean requiresPrescription = Boolean.FALSE;

    /** Quantity threshold below which a low-stock alert is raised. */
    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 0;

    /**
     * Preferred supplier for this medicine (nullable — some medicines may not
     * have a preferred supplier assigned yet).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @NotAudited   // supplier relationship changes tracked via supplier entity itself
    private Supplier supplier;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Medicine() {}

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public MedicineCategory getCategory() { return category; }
    public void setCategory(MedicineCategory category) { this.category = category; }

    public DosageForm getDosageForm() { return dosageForm; }
    public void setDosageForm(DosageForm dosageForm) { this.dosageForm = dosageForm; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(Boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
