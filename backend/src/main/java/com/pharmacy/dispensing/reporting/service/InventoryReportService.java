package com.pharmacy.dispensing.reporting.service;

import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.reporting.dto.InventoryDashboardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReportService {

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private MedicineBatchRepository batchRepository;

    @Transactional(readOnly = true)
    public InventoryDashboardDto getDashboard() {
        InventoryDashboardDto dto = new InventoryDashboardDto();
        long totalMedicines = inventoryRepository.count();
        long lowStock = inventoryRepository.countLowStock();
        long totalLiveStock = batchRepository.sumTotalLiveStock();
        long activeBatches = batchRepository.countByStatus(com.pharmacy.dispensing.inventory.entity.BatchStatus.ACTIVE);
        long recalledBatches = batchRepository.countByStatus(com.pharmacy.dispensing.inventory.entity.BatchStatus.RECALLED);
        dto.setTotalMedicineCount(totalMedicines);
        dto.setLowStockCount(lowStock);
        dto.setTotalLiveStock(totalLiveStock);
        dto.setActiveBatchCount(activeBatches);
        dto.setRecalledBatchCount(recalledBatches);
        return dto;
    }

    @Transactional(readOnly = true)
    public long getLowStockCount() {
        return inventoryRepository.countLowStock();
    }
}
