package com.bankapp.service;

import com.bankapp.model.Transaction;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Color;

@Service
public class StatementService {

    public byte[] generatePdf(String accountNumber, List<Transaction> transactions, java.time.LocalDate from, java.time.LocalDate to) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Account Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            doc.add(new Paragraph("Account: " + accountNumber));
            doc.add(new Paragraph("Period: " + (from != null ? from : "-") + " to " + (to != null ? to : "-")));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14, 18, 18, 20, 30});

            addHeaderCell(table, "Type");
            addHeaderCell(table, "From");
            addHeaderCell(table, "To");
            addHeaderCell(table, "Amount");
            addHeaderCell(table, "Time");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Transaction t : transactions) {
                table.addCell(t.getType());
                table.addCell(t.getSenderAccount() == null ? "-" : t.getSenderAccount());
                table.addCell(t.getReceiverAccount() == null ? "-" : t.getReceiverAccount());
                table.addCell(t.getAmount().toPlainString());
                table.addCell(fmt.format(t.getTimestamp()));
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate statement", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(230, 230, 230));
        table.addCell(cell);
    }
}
