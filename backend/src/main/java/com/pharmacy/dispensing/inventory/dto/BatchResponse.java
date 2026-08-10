package com.pharmacy.dispensing.inventory.dto;

import com.pharmacy.dispensing.inventory.entity.BatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO returned from batch read operations.
 */
public class BatchResponse {

    private Long id;
    private Long inventoryId;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private String manufacturer;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Integer quantityReceived;
    private Integer quantityRemaining;
    private BigDecimal unitCost;
    private BatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BatchResponse() {}

    public BatchResponse(Long id, Long inventoryId, Long medicineId, String medicineName,
                         String batchNumber, String manufacturer, LocalDate manufacturingDate,
                         LocalDate expiryDate, Integer quantityReceived, Integer quantityRemaining,
                         BigDecimal unitCost, BatchStatus status,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.manufacturer = manufacturer;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.quantityReceived = quantityReceived;
        this.quantityRemaining = quantityRemaining;
        this.unitCost = unitCost;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

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

    public Integer getQuantityRemaining() { return quantityRemaining; }
    public void setQuantityRemaining(Integer quantityRemaining) { this.quantityRemaining = quantityRemaining; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
