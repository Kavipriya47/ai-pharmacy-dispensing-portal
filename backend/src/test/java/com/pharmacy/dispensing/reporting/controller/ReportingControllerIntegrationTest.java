package com.pharmacy.dispensing.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.entity.DispensationStatus;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.Inventory;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import com.pharmacy.dispensing.inventory.service.RecallService;
import com.pharmacy.dispensing.medicine.entity.DosageForm;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.entity.MedicineCategory;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReportingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private RecallService recallService;

    private Medicine paracetamol;
    private Medicine amoxicillin;
    private Inventory paracetamolInv;
    private Inventory amoxicillinInv;
    private MedicineBatch paraBatch;
    private MedicineBatch amxBatch;

    @BeforeEach
    void setUp() {
        dispensationRepository.deleteAll();
        batchRepository.deleteAll();
        inventoryRepository.deleteAll();
        medicineRepository.deleteAll();
        auditEventRepository.deleteAll();

        // 1. Seed Medicines
        paracetamol = new Medicine();
        paracetamol.setName("Paracetamol");
        paracetamol.setGenericName("paracetamol-generic");
        paracetamol.setCategory(MedicineCategory.ANALGESIC);
        paracetamol.setDosageForm(DosageForm.TABLET);
        paracetamol.setStrength("500mg");
        paracetamol.setUnitOfMeasure("tablet");
        paracetamol = medicineRepository.save(paracetamol);

        amoxicillin = new Medicine();
        amoxicillin.setName("Amoxicillin");
        amoxicillin.setGenericName("amoxicillin-generic");
        amoxicillin.setCategory(MedicineCategory.ANTIBIOTIC);
        amoxicillin.setDosageForm(DosageForm.CAPSULE);
        amoxicillin.setStrength("250mg");
        amoxicillin.setUnitOfMeasure("capsule");
        amoxicillin = medicineRepository.save(amoxicillin);

        // 2. Seed Inventory
        paracetamolInv = new Inventory();
        paracetamolInv.setMedicine(paracetamol);
        paracetamolInv.setTotalQuantity(100);
        paracetamolInv.setReorderLevel(10);
        paracetamolInv = inventoryRepository.save(paracetamolInv);

        amoxicillinInv = new Inventory();
        amoxicillinInv.setMedicine(amoxicillin);
        amoxicillinInv.setTotalQuantity(5); // Low stock!
        amoxicillinInv.setReorderLevel(10);
        amoxicillinInv = inventoryRepository.save(amoxicillinInv);

        // 3. Seed Batches
        paraBatch = new MedicineBatch();
        paraBatch.setInventory(paracetamolInv);
        paraBatch.setBatchNumber("B-PARA-01");
        paraBatch.setQuantityReceived(100);
        paraBatch.setQuantityRemaining(100);
        paraBatch.setExpiryDate(LocalDate.now().plusMonths(6));
        paraBatch.setStatus(BatchStatus.ACTIVE);
        paraBatch = batchRepository.save(paraBatch);

        amxBatch = new MedicineBatch();
        amxBatch.setInventory(amoxicillinInv);
        amxBatch.setBatchNumber("B-AMX-01");
        amxBatch.setQuantityReceived(10);
        amxBatch.setQuantityRemaining(5);
        amxBatch.setExpiryDate(LocalDate.now().plusMonths(12));
        amxBatch.setStatus(BatchStatus.ACTIVE);
        amxBatch = batchRepository.save(amxBatch);

        // 4. Seed Dispensations
        // Completed Dispensation (Paracetamol)
        DispensationRecord disp1 = new DispensationRecord();
        disp1.setMedicine(paracetamol);
        disp1.setBatch(paraBatch);
        disp1.setPatientIdentifier("PT-001");
        disp1.setQuantityDispensed(5);
        disp1.setDispensedBy("pharmacist1");
        disp1.setStatus(DispensationStatus.COMPLETED);
        disp1.setDispensedAt(LocalDateTime.now());
        dispensationRepository.save(disp1);

        // Completed Dispensation 2 (Paracetamol)
        DispensationRecord disp1b = new DispensationRecord();
        disp1b.setMedicine(paracetamol);
        disp1b.setBatch(paraBatch);
        disp1b.setPatientIdentifier("PT-001b");
        disp1b.setQuantityDispensed(4);
        disp1b.setDispensedBy("pharmacist1");
        disp1b.setStatus(DispensationStatus.COMPLETED);
        disp1b.setDispensedAt(LocalDateTime.now());
        dispensationRepository.save(disp1b);

        // Cancelled Dispensation (Paracetamol)
        DispensationRecord disp2 = new DispensationRecord();
        disp2.setMedicine(paracetamol);
        disp2.setBatch(paraBatch);
        disp2.setPatientIdentifier("PT-002");
        disp2.setQuantityDispensed(2);
        disp2.setDispensedBy("pharmacist1");
        disp2.setStatus(DispensationStatus.CANCELLED);
        disp2.setDispensedAt(LocalDateTime.now());
        dispensationRepository.save(disp2);

        // Completed Dispensation (Amoxicillin)
        DispensationRecord disp3 = new DispensationRecord();
        disp3.setMedicine(amoxicillin);
        disp3.setBatch(amxBatch);
        disp3.setPatientIdentifier("PT-003");
        disp3.setQuantityDispensed(3);
        disp3.setDispensedBy("pharmacist2");
        disp3.setStatus(DispensationStatus.COMPLETED);
        disp3.setDispensedAt(LocalDateTime.now());
        dispensationRepository.save(disp3);

        // 5. Seed Audit Events
        // DISPENSE_FAILED
        AuditEvent failedEvent = new AuditEvent();
        failedEvent.setEventType("DISPENSE_FAILED");
        failedEvent.setPerformedBy("pharmacist1");
        failedEvent.setDescription("Failed dispensing Paracetamol due to authorization error.");
        failedEvent.setCreatedAt(LocalDateTime.now());
        auditEventRepository.save(failedEvent);
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testDispensingSummary() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/dispensing/summary")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedCount").value(3))
                .andExpect(jsonPath("$.cancelledCount").value(1))
                .andExpect(jsonPath("$.failedCount").value(1))
                .andExpect(jsonPath("$.totalQuantityDispensed").value(12)) // 5 + 4 + 3
                .andExpect(jsonPath("$.semanticsNote").exists());
    }

    @Test
    @WithMockUser(username = "pharmacistUser", roles = {"PHARMACIST"})
    void testDispensingByMedicineRoleRestriction() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/dispensing/by-medicine")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testDispensingByMedicine() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/dispensing/by-medicine")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0][0]").value("Paracetamol"))
                .andExpect(jsonPath("$[0][1]").value(2)) // count
                .andExpect(jsonPath("$[0][2]").value(9)); // sum qty
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testDispensingByPharmacist() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/dispensing/by-pharmacist")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testInventoryDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/reports/inventory/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMedicineCount").value(2))
                .andExpect(jsonPath("$.lowStockCount").value(1)) // Amoxicillin has 5 <= 10
                .andExpect(jsonPath("$.totalLiveStock").value(105)) // 100 + 5
                .andExpect(jsonPath("$.activeBatchCount").value(2))
                .andExpect(jsonPath("$.recalledBatchCount").value(0));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testRecallHistoryAndReasonExtraction() throws Exception {
        // Trigger recall
        recallService.initiateRecall("B-PARA-01", "Chemical instability");

        mockMvc.perform(get("/api/v1/reports/recalls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchNumber").value("B-PARA-01"))
                .andExpect(jsonPath("$[0].medicineName").value("Paracetamol"))
                .andExpect(jsonPath("$[0].reason").value("Chemical instability"))
                .andExpect(jsonPath("$[0].recalledBy").value("adminUser"))
                .andExpect(jsonPath("$[0].affectedDispensationCount").value(3)); // Completed + Cancelled
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testAuditReportFiltering() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/audit")
                        .param("eventType", "DISPENSE_FAILED")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("DISPENSE_FAILED"))
                .andExpect(jsonPath("$[0].performedBy").value("pharmacist1"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testDispensingExportExcel() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/dispensing/export/excel")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=dispensing_report.xlsx"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void testDispensingExportPdf() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/reports/dispensing/export/pdf")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=dispensing_report.pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
