package com.pharmacy.dispensing.reporting.controller;

import com.pharmacy.dispensing.reporting.dto.DispensingStatsDto;
import com.pharmacy.dispensing.reporting.dto.InventoryDashboardDto;
import com.pharmacy.dispensing.reporting.dto.RecallHistoryDto;
import com.pharmacy.dispensing.reporting.dto.AuditReportRow;
import com.pharmacy.dispensing.reporting.service.DispensingReportService;
import com.pharmacy.dispensing.reporting.service.InventoryReportService;
import com.pharmacy.dispensing.reporting.service.RecallReportService;
import com.pharmacy.dispensing.reporting.service.AuditReportService;
import com.pharmacy.dispensing.reporting.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    @Autowired
    private DispensingReportService dispensingReportService;
    @Autowired
    private InventoryReportService inventoryReportService;
    @Autowired
    private RecallReportService recallReportService;
    @Autowired
    private AuditReportService auditReportService;
    @Autowired
    private ExportService exportService;

    // --- Dispensing ---
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN','AUDITOR')")
    @GetMapping("/dispensing/summary")
    public DispensingStatsDto dispensingSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.atTime(23, 59, 59);
        return dispensingReportService.getSummary(fromTs, toTs);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/dispensing/by-medicine")
    public List<Object[]> dispensingByMedicine(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return dispensingReportService.byMedicine(from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/dispensing/by-pharmacist")
    public List<Object[]> dispensingByPharmacist(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return dispensingReportService.byPharmacist(from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/dispensing/export/excel")
    public ResponseEntity<byte[]> exportDispensingExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] data = exportService.exportDispensingExcel(from.atStartOfDay(), to.atTime(23, 59, 59));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dispensing_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/dispensing/export/pdf")
    public ResponseEntity<byte[]> exportDispensingPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] data = exportService.exportDispensingPdf(from.atStartOfDay(), to.atTime(23, 59, 59));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dispensing_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    // --- Inventory ---
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN','AUDITOR')")
    @GetMapping("/inventory/dashboard")
    public InventoryDashboardDto inventoryDashboard() {
        return inventoryReportService.getDashboard();
    }

    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    @GetMapping("/inventory/low-stock")
    public long lowStockCount() {
        return inventoryReportService.getLowStockCount();
    }

    // --- Recall ---
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/recalls")
    public List<RecallHistoryDto> recallHistory() {
        return recallReportService.getRecallHistory();
    }

    // --- Audit ---
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/audit")
    public List<AuditReportRow> auditReport(
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String eventType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return auditReportService.getReport(performedBy, eventType, from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    @GetMapping("/audit/export/excel")
    public ResponseEntity<byte[]> exportAuditExcel(
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String eventType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] data = exportService.exportAuditExcel(performedBy, eventType, from.atStartOfDay(), to.atTime(23, 59, 59));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
