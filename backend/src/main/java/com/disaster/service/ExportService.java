package com.disaster.service;

import com.disaster.dto.DisasterResponse;
import com.disaster.repository.DisasterRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DisasterRepository disasterRepository;

    public ExportService(DisasterRepository disasterRepository) {
        this.disasterRepository = disasterRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportCSV() {
        List<DisasterResponse> disasters = disasterRepository.findAllByOrderByDateDesc()
            .stream().map(DisasterResponse::fromEntity).toList();
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM so Excel opens the file with correct encoding.
        sb.append('\uFEFF');
        sb.append("ID,Type,Severity,Location,Status,Priority,ReportedBy,Date\n");
        for (DisasterResponse d : disasters) {
            sb.append(d.getId()).append(",")
              .append(escapeCSV(d.getDisasterType())).append(",")
              .append(escapeCSV(d.getSeverity())).append(",")
              .append(escapeCSV(d.getLocation())).append(",")
              .append(d.getStatus()).append(",")
              .append(d.getPriority() != null ? d.getPriority() : "").append(",")
              .append(escapeCSV(d.getReportedBy())).append(",")
              .append(d.getDate()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Real PDF report generated with OpenPDF: title page header, summary table
     * and per-disaster detail rows.
     */
    @Transactional(readOnly = true)
    public byte[] exportPDF() {
        List<DisasterResponse> disasters = disasterRepository.findAllByOrderByDateDesc()
            .stream().map(DisasterResponse::fromEntity).toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            Paragraph title = new Paragraph("AI Disaster Management System", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            Paragraph subtitle = new Paragraph("Disaster Report - generated " + LocalDateTime.now().format(TIMESTAMP)
                    + " - " + disasters.size() + " records", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            float[] widths = {0.6f, 2.0f, 1.0f, 2.4f, 1.2f, 1.2f, 2.0f};
            table.setWidths(widths);

            for (String header : List.of("ID", "Type", "Severity", "Location", "Status", "Priority", "Date")) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setPadding(4);
                table.addCell(cell);
            }
            for (DisasterResponse d : disasters) {
                table.addCell(String.valueOf(d.getId()));
                table.addCell(d.getDisasterType() != null ? d.getDisasterType() : "");
                table.addCell(d.getSeverity() != null ? d.getSeverity() : "");
                table.addCell(d.getLocation() != null ? d.getLocation() : "");
                table.addCell(d.getStatus() != null ? d.getStatus().toString() : "");
                table.addCell(d.getPriority() != null ? d.getPriority().toString() : "");
                table.addCell(d.getDate() != null ? d.getDate().toString() : "");
            }
            document.add(table);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF export", e);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    /**
     * Excel-compatible export: CSV content with UTF-8 BOM. Opens directly in
     * Microsoft Excel / LibreOffice Calc with correct encoding.
     */
    public byte[] exportExcel() {
        return exportCSV();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
