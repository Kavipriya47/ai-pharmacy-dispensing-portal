package com.pharmacy.dispensing.inventory.dto;

/**
 * Summary of current stock position for a single medicine.
 * Returned by {@code GET /api/v1/inventory/stock-summary}.
 */
public class StockSummaryResponse {

    private Long medicineId;
    private String medicineName;
    private String genericName;
    private Integer totalQuantity;
    private Integer reorderLevel;

    /** True when totalQuantity <= reorderLevel. */
    private Boolean lowStock;

    /** Number of ACTIVE batches available for dispensing. */
    private Long activeBatchCount;

    public StockSummaryResponse() {}

    public StockSummaryResponse(Long medicineId, String medicineName, String genericName,
                                Integer totalQuantity, Integer reorderLevel,
                                Boolean lowStock, Long activeBatchCount) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.totalQuantity = totalQuantity;
        this.reorderLevel = reorderLevel;
        this.lowStock = lowStock;
        this.activeBatchCount = activeBatchCount;
    }

    // ---- Getters & Setters ----

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Boolean getLowStock() { return lowStock; }
    public void setLowStock(Boolean lowStock) { this.lowStock = lowStock; }

    public Long getActiveBatchCount() { return activeBatchCount; }
    public void setActiveBatchCount(Long activeBatchCount) { this.activeBatchCount = activeBatchCount; }
}
