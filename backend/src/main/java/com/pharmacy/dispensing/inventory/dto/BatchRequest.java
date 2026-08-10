package com.pharmacy.dispensing.inventory.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for adding a new medicine batch (stock received from supplier).
 */
public class BatchRequest {

    @NotNull(message = "medicineId is required")
    private Long medicineId;

    @NotBlank(message = "batchNumber is required")
    @Size(max = 100)
    private String batchNumber;

    @Size(max = 200)
    private String manufacturer;

    private LocalDate manufacturingDate;

    @NotNull(message = "expiryDate is required")
    @Future(message = "expiryDate must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "quantityReceived is required")
    @Min(value = 1, message = "quantityReceived must be at least 1")
    private Integer quantityReceived;

    @DecimalMin(value = "0.0", inclusive = false, message = "unitCost must be positive")
    private BigDecimal unitCost;

    /** Optional additional notes recorded as an InventoryTransaction note. */
    private String notes;

    // ---- Getters & Setters ----

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
