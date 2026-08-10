package com.pharmacy.dispensing.medicine.dto;

import com.pharmacy.dispensing.medicine.entity.DosageForm;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;

import java.time.LocalDateTime;

/**
 * Read-only response view of a Medicine catalog entry.
 * Embeds a minimal supplier summary to avoid deep nesting.
 */
public class MedicineResponse {

    private Long id;
    private String name;
    private String genericName;
    private MedicineCategory category;
    private DosageForm dosageForm;
    private String strength;
    private String unitOfMeasure;
    private String description;
    private Boolean requiresPrescription;
    private Integer reorderLevel;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Supplier summary (flat, not nested object)
    private Long supplierId;
    private String supplierName;

    public MedicineResponse() {}

    public MedicineResponse(Long id, String name, String genericName, MedicineCategory category,
                            DosageForm dosageForm, String strength, String unitOfMeasure,
                            String description, Boolean requiresPrescription, Integer reorderLevel,
                            Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
                            Long supplierId, String supplierName) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.category = category;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.unitOfMeasure = unitOfMeasure;
        this.description = description;
        this.requiresPrescription = requiresPrescription;
        this.reorderLevel = reorderLevel;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }

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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}
