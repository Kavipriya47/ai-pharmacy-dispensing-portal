package com.pharmacy.dispensing.inventory.service;

import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.Inventory;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.medicine.entity.DosageForm;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ExpiryCheckSchedulerTest {

    @Autowired
    private ExpiryCheckScheduler expiryCheckScheduler;

    @Autowired
    private MedicineBatchRepository batchRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private Inventory inventory;

    @Autowired
    private com.pharmacy.dispensing.dispensing.repository.DispensationRepository dispensationRepository;

    @BeforeEach
    void setUp() {
        dispensationRepository.deleteAll();
        batchRepository.deleteAll();
        inventoryRepository.deleteAll();
        medicineRepository.deleteAll();
        auditEventRepository.deleteAll();

        Medicine med = new Medicine();
        med.setName("Test Med");
        med.setGenericName("Generic");
        med.setCategory(MedicineCategory.ANALGESIC);
        med.setDosageForm(DosageForm.TABLET);
        med.setStrength("500mg");
        med.setUnitOfMeasure("tablet");
        med.setRequiresPrescription(false);
        med.setActive(true);
        med = medicineRepository.save(med);

        inventory = new Inventory();
        inventory.setMedicine(med);
        inventory.setTotalQuantity(0);
        inventory = inventoryRepository.save(inventory);
    }

    @Test
    void testSchedulerIdempotencyAndExpiry() {
        // Create a batch that expired yesterday
        MedicineBatch batch = new MedicineBatch();
        batch.setInventory(inventory);
        batch.setBatchNumber("EXPIRED-1");
        batch.setQuantityReceived(50);
        batch.setQuantityRemaining(50);
        batch.setExpiryDate(LocalDate.now().minusDays(1)); // Expired!
        batch.setStatus(BatchStatus.ACTIVE); // Still marked active
        batch.setUnitCost(new BigDecimal("10.00"));
        batch = batchRepository.save(batch);

        inventory.setTotalQuantity(50);
        inventoryRepository.save(inventory);

        // Run 1
        expiryCheckScheduler.markExpiredBatches();

        MedicineBatch updatedBatch = batchRepository.findById(batch.getId()).orElseThrow();
        assertThat(updatedBatch.getStatus()).isEqualTo(BatchStatus.EXPIRED);

        Inventory updatedInv = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertThat(updatedInv.getTotalQuantity()).isEqualTo(0); // 50 deducted

        long auditCount = auditEventRepository.findAll().stream()
                .filter(a -> a.getEventType().equals("BATCH_EXPIRED"))
                .count();
        assertThat(auditCount).isEqualTo(1);

        // Run 2 - Should be IDEMPOTENT (no changes, no new audit logs)
        expiryCheckScheduler.markExpiredBatches();

        long auditCountAfterSecondRun = auditEventRepository.findAll().stream()
                .filter(a -> a.getEventType().equals("BATCH_EXPIRED"))
                .count();
        assertThat(auditCountAfterSecondRun).isEqualTo(1); // Still 1
    }
}
