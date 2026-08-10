package com.pharmacy.dispensing.reporting.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private DispensationRepository dispensationRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Transactional(readOnly = true)
    public byte[] exportDispensingExcel(LocalDateTime from, LocalDateTime to) {
        Page<DispensationRecord> page = dispensationRepository.findByDispensedAtBetween(from, to, PageRequest.of(0, 10000));
        List<DispensationRecord> records = page.getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Dispensing Report");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Patient Identifier", "Medicine Name", "Batch Number", "Quantity Dispensed", "Dispensed By", "Status", "FEFO Override", "Dispensed At"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (DispensationRecord record : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getPatientIdentifier());
                row.createCell(2).setCellValue(record.getMedicine() != null ? record.getMedicine().getName() : "");
                row.createCell(3).setCellValue(record.getBatch() != null ? record.getBatch().getBatchNumber() : "");
                row.createCell(4).setCellValue(record.getQuantityDispensed());
                row.createCell(5).setCellValue(record.getDispensedBy());
                row.createCell(6).setCellValue(record.getStatus() != null ? record.getStatus().toString() : "");
                row.createCell(7).setCellValue(record.getFefoOverride() != null && record.getFefoOverride() ? "Yes" : "No");
                row.createCell(8).setCellValue(record.getDispensedAt() != null ? record.getDispensedAt().format(formatter) : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate dispensing Excel report", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportDispensingPdf(LocalDateTime from, LocalDateTime to) {
        Page<DispensationRecord> page = dispensationRepository.findByDispensedAtBetween(from, to, PageRequest.of(0, 10000));
        List<DispensationRecord> records = page.getContent();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Pharmacy Dispensing Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add Date Range info
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Paragraph subtitle = new Paragraph("Period: " + from.format(dateFormatter) + " to " + to.format(dateFormatter), subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(10);
            document.add(subtitle);

            // Create Table with 9 columns
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 2f, 2f, 1f, 2f, 1.5f, 1.5f, 2.5f});

            // Header
            String[] headers = {"ID", "Patient ID", "Medicine", "Batch", "Qty", "Disp. By", "Status", "FEFO Over.", "Date"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Rows
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (DispensationRecord r : records) {
                table.addCell(new Phrase(String.valueOf(r.getId()), cellFont));
                table.addCell(new Phrase(r.getPatientIdentifier(), cellFont));
                table.addCell(new Phrase(r.getMedicine() != null ? r.getMedicine().getName() : "", cellFont));
                table.addCell(new Phrase(r.getBatch() != null ? r.getBatch().getBatchNumber() : "", cellFont));
                table.addCell(new Phrase(String.valueOf(r.getQuantityDispensed()), cellFont));
                table.addCell(new Phrase(r.getDispensedBy(), cellFont));
                table.addCell(new Phrase(r.getStatus() != null ? r.getStatus().toString() : "", cellFont));
                table.addCell(new Phrase(r.getFefoOverride() != null && r.getFefoOverride() ? "Yes" : "No", cellFont));
                table.addCell(new Phrase(r.getDispensedAt() != null ? r.getDispensedAt().format(dateFormatter) : "", cellFont));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate dispensing PDF report", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportAuditExcel(String performedBy, String eventType, LocalDateTime from, LocalDateTime to) {
        Page<AuditEvent> page = auditEventRepository.findFiltered(
                performedBy != null && !performedBy.trim().isEmpty() ? performedBy.trim() : null,
                eventType != null && !eventType.trim().isEmpty() ? eventType.trim() : null,
                from,
                to,
                PageRequest.of(0, 10000)
        );
        List<AuditEvent> events = page.getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Audit Report");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Event Type", "Performed By", "Description", "Metadata", "IP Address", "Created At"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AuditEvent event : events) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(event.getId());
                row.createCell(1).setCellValue(event.getEventType());
                row.createCell(2).setCellValue(event.getPerformedBy());
                row.createCell(3).setCellValue(event.getDescription());
                row.createCell(4).setCellValue(event.getMetadata());
                row.createCell(5).setCellValue(event.getIpAddress());
                row.createCell(6).setCellValue(event.getCreatedAt() != null ? event.getCreatedAt().format(formatter) : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate audit Excel report", e);
        }
    }
}
