package com.pharmacy.dispensing.inventory.repository;

import com.pharmacy.dispensing.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Finds the inventory record for a given medicine.
     * Used when adding a new batch to ensure an inventory row exists.
     */
    Optional<Inventory> findByMedicineId(Long medicineId);

    /**
     * Returns true if an inventory record already exists for the medicine.
     * Prevents duplicate inventory rows (enforced at DB layer too via UNIQUE constraint).
     */
    boolean existsByMedicineId(Long medicineId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(i) FROM Inventory i WHERE i.totalQuantity <= i.reorderLevel AND i.totalQuantity > 0")
    long countLowStock();
}
