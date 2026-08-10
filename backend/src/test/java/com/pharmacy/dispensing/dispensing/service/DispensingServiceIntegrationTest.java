package com.pharmacy.dispensing.dispensing.service;

import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.dispensing.dto.DispenseRequest;
import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class DispensingServiceIntegrationTest {

    @Autowired
    private DispensingService dispensingService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MedicineBatchRepository batchRepository;

    @Autowired
    private DispensationRepository dispensationRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private Medicine testMedicine;
    private Medicine rxMedicine;
    private Inventory testInventory;
    private Inventory rxInventory;

    @BeforeEach
    void setUp() {
        dispensationRepository.deleteAll();
        batchRepository.deleteAll();
        inventoryRepository.deleteAll();
        medicineRepository.deleteAll();
        auditEventRepository.deleteAll();

        // Non-RX medicine
        testMedicine = new Medicine();
        testMedicine.setName("Test Med");
        testMedicine.setGenericName("Generic Test");
        testMedicine.setCategory(MedicineCategory.ANALGESIC);
        testMedicine.setDosageForm(DosageForm.TABLET);
        testMedicine.setStrength("500mg");
        testMedicine.setUnitOfMeasure("tablet");
        testMedicine.setRequiresPrescription(false);
        testMedicine.setActive(true);
        testMedicine = medicineRepository.save(testMedicine);

        testInventory = new Inventory();
        testInventory.setMedicine(testMedicine);
        testInventory.setTotalQuantity(0);
        testInventory = inventoryRepository.save(testInventory);

        // RX medicine
        rxMedicine = new Medicine();
        rxMedicine.setName("RX Med");
        rxMedicine.setGenericName("Generic RX");
        rxMedicine.setCategory(MedicineCategory.ANTIBIOTIC);
        rxMedicine.setDosageForm(DosageForm.CAPSULE);
        rxMedicine.setStrength("250mg");
        rxMedicine.setUnitOfMeasure("capsule");
        rxMedicine.setRequiresPrescription(true);
        rxMedicine.setActive(true);
        rxMedicine = medicineRepository.save(rxMedicine);

        rxInventory = new Inventory();
        rxInventory.setMedicine(rxMedicine);
        rxInventory.setTotalQuantity(0);
        rxInventory = inventoryRepository.save(rxInventory);
    }

    private MedicineBatch createBatch(Inventory inv, int qty, LocalDate expiry, BatchStatus status) {
        Inventory currentInv = inventoryRepository.findById(inv.getId()).orElseThrow();
        MedicineBatch batch = new MedicineBatch();
        batch.setInventory(currentInv);
        batch.setBatchNumber("BATCH-" + System.nanoTime());
        batch.setQuantityReceived(qty);
        batch.setQuantityRemaining(qty);
        batch.setExpiryDate(expiry);
        batch.setStatus(status);
        batch.setUnitCost(new BigDecimal("10.00"));
        batch = batchRepository.save(batch);

        if (status == BatchStatus.ACTIVE) {
            currentInv.setTotalQuantity(currentInv.getTotalQuantity() + qty);
            inventoryRepository.save(currentInv);
        }
        return batch;
    }

    @Test
    @WithMockUser(username = "pharmacist_jane")
    void testValidDispensingAndFEFOSelection() {
        // Create two active batches. Batch 1 expires sooner.
        MedicineBatch batch1 = createBatch(testInventory, 50, LocalDate.now().plusDays(10), BatchStatus.ACTIVE);
        MedicineBatch batch2 = createBatch(testInventory, 100, LocalDate.now().plusDays(30), BatchStatus.ACTIVE);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(20);

        var response = dispensingService.dispense(request);

        // Assertions
        assertThat(response.getBatchId()).isEqualTo(batch1.getId()); // FEFO selected batch 1
        assertThat(response.getQuantityDispensed()).isEqualTo(20);

        // Verify deductions
        MedicineBatch updatedBatch1 = batchRepository.findById(batch1.getId()).orElseThrow();
        assertThat(updatedBatch1.getQuantityRemaining()).isEqualTo(30);

        Inventory updatedInv = inventoryRepository.findById(testInventory.getId()).orElseThrow();
        assertThat(updatedInv.getTotalQuantity()).isEqualTo(130); // Started with 150

        // Verify Audit Event
        List<AuditEvent> audits = auditEventRepository.findAll();
        assertThat(audits).anyMatch(a -> a.getEventType().equals("MEDICATION_DISPENSED")
                && a.getPerformedBy().equals("pharmacist_jane"));
    }

    @Test
    @WithMockUser
    void testNegativeOrZeroQuantity() {
        createBatch(testInventory, 50, LocalDate.now().plusDays(10), BatchStatus.ACTIVE);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(0);

        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly positive");
    }

    @Test
    @WithMockUser
    void testInsufficientStock() {
        createBatch(testInventory, 5, LocalDate.now().plusDays(10), BatchStatus.ACTIVE);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10); // requests 10, only 5 available

        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sufficient stock");

        // Verify Failure Audit
        List<AuditEvent> audits = auditEventRepository.findAll();
        assertThat(audits).anyMatch(a -> a.getEventType().equals("DISPENSE_FAILED"));
    }

    @Test
    @WithMockUser
    void testExpiredBatchSelection() {
        // System should never pick an expired batch automatically
        createBatch(testInventory, 50, LocalDate.now().minusDays(1), BatchStatus.ACTIVE); // Expired but not marked yet

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10);

        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No ACTIVE batches with sufficient stock available");
    }

    @Test
    @WithMockUser
    void testManualOverrideRecalledBatch() {
        MedicineBatch recalledBatch = createBatch(testInventory, 50, LocalDate.now().plusDays(30), BatchStatus.RECALLED);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setBatchId(recalledBatch.getId()); // Pharmacist maliciously/accidentally forces a RECALLED batch
        request.setOverrideReason("I need this now");
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10);

        // Even with override, safety rules cannot be bypassed
        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CRITICAL SAFETY VIOLATION: Cannot dispense from a RECALLED batch");
    }

    @Test
    @WithMockUser
    void testManualOverrideQuarantinedBatch() {
        MedicineBatch quarantinedBatch = createBatch(testInventory, 50, LocalDate.now().plusDays(30), BatchStatus.QUARANTINED);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setBatchId(quarantinedBatch.getId());
        request.setOverrideReason("Just checking");
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10);

        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SAFETY VIOLATION: Cannot dispense from a QUARANTINED batch");
    }

    @Test
    @WithMockUser
    void testManualOverrideWithoutReason() {
        MedicineBatch batch = createBatch(testInventory, 50, LocalDate.now().plusDays(30), BatchStatus.ACTIVE);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setBatchId(batch.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10);
        // NO override reason provided

        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FEFO override requires a reason");
    }

    @Test
    @WithMockUser
    void testValidManualOverride() {
        MedicineBatch batch1 = createBatch(testInventory, 50, LocalDate.now().plusDays(10), BatchStatus.ACTIVE);
        MedicineBatch batch2 = createBatch(testInventory, 100, LocalDate.now().plusDays(30), BatchStatus.ACTIVE);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setBatchId(batch2.getId()); // Selecting the newer batch explicitly
        request.setOverrideReason("Batch 1 box is damaged");
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10);

        var response = dispensingService.dispense(request);
        assertThat(response.getBatchId()).isEqualTo(batch2.getId());

        // Verify FEFO_OVERRIDE audit event
        List<AuditEvent> audits = auditEventRepository.findAll();
        assertThat(audits).anyMatch(a -> a.getEventType().equals("FEFO_OVERRIDE") && a.getMetadata().contains("Batch 1 box is damaged"));
    }

    @Test
    @WithMockUser
    void testPrescriptionRequiredWithoutRef() {
        createBatch(rxInventory, 50, LocalDate.now().plusDays(30), BatchStatus.ACTIVE);

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(rxMedicine.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(10);
        // NO prescription reference

        assertThatThrownBy(() -> dispensingService.dispense(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PRESCRIPTION_REQUIRED");
    }

    @Test
    @WithMockUser
    void testConcurrentDispensingOptimisticLocking() throws InterruptedException {
        // We have 10 stock
        createBatch(testInventory, 10, LocalDate.now().plusDays(30), BatchStatus.ACTIVE);

        // Two concurrent requests
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger lockFailures = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // wait for all threads to start at the exact same time
                    DispenseRequest request = new DispenseRequest();
                    request.setMedicineId(testMedicine.getId());
                    request.setPatientIdentifier("PT-CONCURRENT");
                    request.setQuantity(8);
                    
                    dispensingService.dispense(request);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    lockFailures.incrementAndGet();
                } catch (Exception e) {
                    // Ignore other exceptions for this test, we care about locking
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown(); // release threads
        done.await(5, TimeUnit.SECONDS);

        // One should succeed, one should fail (because 8+8 > 10, or version conflicts)
        assertThat(successCount.get()).isEqualTo(1);
        
        // Either Optimistic Locking prevented it, or the second one saw quantity=2 and threw Insufficient stock.
        // The key is that we NEVER dispensed 16 when we only had 10.
        Inventory currentInv = inventoryRepository.findById(testInventory.getId()).orElseThrow();
        assertThat(currentInv.getTotalQuantity()).isEqualTo(2); // 10 - 8
    }
}
