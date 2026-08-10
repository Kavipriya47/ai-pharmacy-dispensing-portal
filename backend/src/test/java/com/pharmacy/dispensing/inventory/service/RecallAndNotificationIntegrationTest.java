package com.pharmacy.dispensing.inventory.service;

import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.dispensing.dto.DispenseRequest;
import com.pharmacy.dispensing.dispensing.service.DispensingService;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.Inventory;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.medicine.entity.DosageForm;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import com.pharmacy.dispensing.notification.entity.Notification;
import com.pharmacy.dispensing.notification.entity.NotificationType;
import com.pharmacy.dispensing.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class RecallAndNotificationIntegrationTest {

    @Autowired private RecallService recallService;
    @Autowired private DispensingService dispensingService;
    @Autowired private MedicineRepository medicineRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private MedicineBatchRepository batchRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AuditEventRepository auditEventRepository;
    @Autowired private com.pharmacy.dispensing.dispensing.repository.DispensationRepository dispensationRepository;

    private Medicine testMedicine;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        dispensationRepository.deleteAll();
        batchRepository.deleteAll();
        inventoryRepository.deleteAll();
        medicineRepository.deleteAll();
        notificationRepository.deleteAll();
        auditEventRepository.deleteAll();

        testMedicine = new Medicine();
        testMedicine.setName("Recall Test Med");
        testMedicine.setGenericName("recall-generic");
        testMedicine.setCategory(MedicineCategory.ANALGESIC);
        testMedicine.setDosageForm(DosageForm.TABLET);
        testMedicine.setStrength("100mg");
        testMedicine.setUnitOfMeasure("tablet");
        testMedicine = medicineRepository.save(testMedicine);

        testInventory = new Inventory();
        testInventory.setMedicine(testMedicine);
        testInventory.setTotalQuantity(0);
        testInventory.setReorderLevel(10);
        testInventory = inventoryRepository.save(testInventory);
    }

    private MedicineBatch createBatch(String batchNumber, int quantity, BatchStatus status) {
        MedicineBatch batch = new MedicineBatch();
        batch.setInventory(testInventory);
        batch.setBatchNumber(batchNumber);
        batch.setQuantityReceived(quantity);
        batch.setQuantityRemaining(quantity);
        batch.setExpiryDate(LocalDate.now().plusMonths(6));
        batch.setStatus(status);

        testInventory.setTotalQuantity(testInventory.getTotalQuantity() + quantity);
        inventoryRepository.save(testInventory);
        return batchRepository.save(batch);
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testRecallDeductsInventoryAndGeneratesNotification() {
        createBatch("B-RECALL-01", 100, BatchStatus.ACTIVE);

        MedicineBatch recalledBatch = recallService.initiateRecall("B-RECALL-01", "Contamination suspected");

        assertThat(recalledBatch.getStatus()).isEqualTo(BatchStatus.RECALLED);

        Inventory updatedInv = inventoryRepository.findById(testInventory.getId()).orElseThrow();
        assertThat(updatedInv.getTotalQuantity()).isEqualTo(0); // 100 deducted

        // Check Notification
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.BATCH_RECALLED);
        assertThat(notifications.get(0).getMessage()).contains("Contamination suspected");

        // Check Audit Event
        List<AuditEvent> audits = auditEventRepository.findAll();
        assertThat(audits).anyMatch(a -> a.getEventType().equals("RECALL_INITIATED")
                && a.getDescription().contains("Contamination suspected"));
    }

    @Test
    @WithMockUser
    void testLowStockNotificationGenerated() {
        createBatch("B-LOWSTOCK-01", 12, BatchStatus.ACTIVE); // reorder level is 10

        DispenseRequest request = new DispenseRequest();
        request.setMedicineId(testMedicine.getId());
        request.setPatientIdentifier("PT-001");
        request.setQuantity(3); // reduces stock from 12 → 9, crosses reorder threshold

        dispensingService.dispense(request);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).anyMatch(n ->
                n.getType() == NotificationType.LOW_STOCK
                        && n.getMessage().contains("dropped at or below the reorder level"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testRecallWithoutReasonIsRejected() {
        createBatch("B-RECALL-02", 50, BatchStatus.ACTIVE);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                recallService.initiateRecall("B-RECALL-02", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("documented reason");
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testDoubleRecallIsRejected() {
        createBatch("B-RECALL-03", 50, BatchStatus.ACTIVE);
        recallService.initiateRecall("B-RECALL-03", "First recall");

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                recallService.initiateRecall("B-RECALL-03", "Second recall"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already recalled");
    }
}
