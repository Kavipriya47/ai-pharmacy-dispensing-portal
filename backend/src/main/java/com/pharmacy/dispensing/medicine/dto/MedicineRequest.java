package com.pharmacy.dispensing.medicine.dto;

import com.pharmacy.dispensing.medicine.entity.DosageForm;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import jakarta.validation.constraints.*;

/**
 * Request payload for creating or updating a Medicine entry in the catalog.
 */
public class MedicineRequest {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 200, message = "Medicine name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Generic name is required")
    @Size(max = 200, message = "Generic name must not exceed 200 characters")
    private String genericName;

    @NotNull(message = "Category is required")
    private MedicineCategory category;

    @NotNull(message = "Dosage form is required")
    private DosageForm dosageForm;

    @NotBlank(message = "Strength is required (e.g. '500mg', '5mg/5ml')")
    @Size(max = 50)
    private String strength;

    @NotBlank(message = "Unit of measure is required (e.g. 'tablet', 'ml')")
    @Size(max = 30)
    private String unitOfMeasure;

    private String description;

    private Boolean requiresPrescription = Boolean.FALSE;

    @Min(value = 0, message = "Reorder level must be zero or positive")
    private Integer reorderLevel = 0;

    /** Optional — ID of the preferred supplier. */
    private Long supplierId;

    // ---- Getters & Setters ----

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

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
}
