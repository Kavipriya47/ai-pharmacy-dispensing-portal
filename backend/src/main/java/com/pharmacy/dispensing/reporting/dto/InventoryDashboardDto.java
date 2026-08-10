package com.pharmacy.dispensing.reporting.dto;

public class InventoryDashboardDto {
    private long totalMedicineCount;
    private long lowStockCount;
    private long totalLiveStock;
    private long activeBatchCount;
    private long recalledBatchCount;

    public long getTotalMedicineCount() {
        return totalMedicineCount;
    }

    public void setTotalMedicineCount(long totalMedicineCount) {
        this.totalMedicineCount = totalMedicineCount;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getTotalLiveStock() {
        return totalLiveStock;
    }

    public void setTotalLiveStock(long totalLiveStock) {
        this.totalLiveStock = totalLiveStock;
    }

    public long getActiveBatchCount() {
        return activeBatchCount;
    }

    public void setActiveBatchCount(long activeBatchCount) {
        this.activeBatchCount = activeBatchCount;
    }

    public long getRecalledBatchCount() {
        return recalledBatchCount;
    }

    public void setRecalledBatchCount(long recalledBatchCount) {
        this.recalledBatchCount = recalledBatchCount;
    }
}
