package com.expensewise.service;

import com.expensewise.domain.entity.Expense;
import com.expensewise.domain.repository.ExpenseRepository;
import com.expensewise.security.SecurityUserContext;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExpenseRepository expenseRepository;
    private final SecurityUserContext securityUserContext;

    public ExportService(ExpenseRepository expenseRepository, SecurityUserContext securityUserContext) {
        this.expenseRepository = expenseRepository;
        this.securityUserContext = securityUserContext;
    }

    public String exportToCsv(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUserContext.getCurrentUserId();
        List<Expense> expenses = expenseRepository.findAllByUserIdAndDateRange(userId, startDate, endDate);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Date,Category,Amount,Description");

        for (Expense expense : expenses) {
            pw.printf("%s,\"%s\",%s,\"%s\"%n",
                    expense.getExpenseDate().format(DATE_FORMATTER),
                    escapeCsv(expense.getCategory().getName()),
                    expense.getAmount().toPlainString(),
                    escapeCsv(expense.getDescription() != null ? expense.getDescription() : "")
            );
        }

        pw.flush();
        return sw.toString();
    }

    public byte[] exportToPdf(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUserContext.getCurrentUserId();
        List<Expense> expenses = expenseRepository.findAllByUserIdAndDateRange(userId, startDate, endDate);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Expense Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph subtitle = new Paragraph(
                    String.format("Period: %s to %s", startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER)),
                    subtitleFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 3, 2, 4});

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            BaseColor headerBg = new BaseColor(52, 73, 94);

            for (String header : new String[]{"Date", "Category", "Amount", "Description"}) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(8);
                table.addCell(cell);
            }

            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            for (Expense expense : expenses) {
                table.addCell(new Phrase(expense.getExpenseDate().format(DATE_FORMATTER), cellFont));
                table.addCell(new Phrase(expense.getCategory().getName(), cellFont));
                table.addCell(new Phrase("$" + expense.getAmount().toPlainString(), cellFont));
                table.addCell(new Phrase(expense.getDescription() != null ? expense.getDescription() : "", cellFont));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private String escapeCsv(String value) {
        return value.replace("\"", "\"\"");
    }
}
