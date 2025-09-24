package com.company.erp.payment.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.payment.entity.Payment;
import com.company.erp.payment.entity.PaymentBatch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SaudiBankFileGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SaudiBankFileGenerator.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Generate Excel file in Saudi bank format
     */
    public byte[] generateBankFile(PaymentBatch batch, String bankName) {
        logger.info("Generating bank file for batch: {} with {} payments",
                batch.getBatchNumber(), batch.getPaymentCount());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = createMainSheet(workbook, bankName);
            populateHeaders(sheet, bankName);
            populatePaymentData(sheet, batch.getPayments());
            addSummarySection(sheet, batch);

            return convertWorkbookToBytes(workbook);

        } catch (IOException e) {
            logger.error("Error generating bank file for batch: {}", batch.getBatchNumber(), e);
            throw new BusinessException("BANK_FILE_GENERATION_ERROR",
                    "Failed to generate bank file: " + e.getMessage());
        }
    }

    private Sheet createMainSheet(Workbook workbook, String bankName) {
        String sheetName = getSanitizedBankName(bankName) + "_Payments";
        Sheet sheet = workbook.createSheet(sheetName);

        // Set default column widths
        sheet.setColumnWidth(0, 4000);  // Bank
        sheet.setColumnWidth(1, 5000);  // Account Number
        sheet.setColumnWidth(2, 3000);  // Amount
        sheet.setColumnWidth(3, 8000);  // Comments
        sheet.setColumnWidth(4, 6000);  // Employee Name
        sheet.setColumnWidth(5, 4000);  // National ID/Iqama
        sheet.setColumnWidth(6, 10000); // Beneficiary Address

        return sheet;
    }

    private void populateHeaders(Sheet sheet, String bankName) {
        // Create title row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Bulk Payment File - " + bankName);
        titleCell.setCellStyle(createHeaderStyle(sheet.getWorkbook()));

        // Merge title across columns
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        // Create info rows
        Row dateRow = sheet.createRow(1);
        dateRow.createCell(0).setCellValue("Generated Date:");
        dateRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // Create headers row
        Row headerRow = sheet.createRow(3);
        String[] headers = getHeadersForBank(bankName);

        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private String[] getHeadersForBank(String bankName) {
        // Standard Saudi bank format - common across major banks
        return new String[]{
                "Bank",
                "Account Number",
                "Amount",
                "Comments",
                "Employee Name",
                "National ID/Iqama ID",
                "Beneficiary Address"
        };
    }

    private void populatePaymentData(Sheet sheet, List<Payment> payments) {
        int rowNum = 4; // Start after headers

        CellStyle amountStyle = createAmountStyle(sheet.getWorkbook());
        CellStyle textStyle = createTextStyle(sheet.getWorkbook());

        for (Payment payment : payments) {
            Row row = sheet.createRow(rowNum++);
            populatePaymentRow(row, payment, amountStyle, textStyle);
        }
    }

    private void populatePaymentRow(Row row, Payment payment, CellStyle amountStyle, CellStyle textStyle) {
        // Validate payment data
        validatePaymentData(payment);

        int colNum = 0;

        // Bank
        Cell bankCell = row.createCell(colNum++);
        bankCell.setCellValue(payment.getBankName());
        bankCell.setCellStyle(textStyle);

        // Account Number
        Cell accountCell = row.createCell(colNum++);
        accountCell.setCellValue(payment.getAccountNumber());
        accountCell.setCellStyle(textStyle);

        // Amount (formatted for Saudi banks)
        Cell amountCell = row.createCell(colNum++);
        amountCell.setCellValue(payment.getAmount().doubleValue());
        amountCell.setCellStyle(amountStyle);

        // Comments (Quotation description)
        Cell commentsCell = row.createCell(colNum++);
        String comments = generatePaymentComments(payment);
        commentsCell.setCellValue(comments);
        commentsCell.setCellStyle(textStyle);

        // Employee Name
        Cell nameCell = row.createCell(colNum++);
        nameCell.setCellValue(payment.getPayee().getFullName());
        nameCell.setCellStyle(textStyle);

        // National ID/Iqama ID
        Cell idCell = row.createCell(colNum++);
        String nationalId = payment.getPayee().getNationalId() != null ?
                payment.getPayee().getNationalId() : payment.getPayee().getIqamaId();
        idCell.setCellValue(nationalId != null ? nationalId : "");
        idCell.setCellStyle(textStyle);

        // Beneficiary Address
        Cell addressCell = row.createCell(colNum++);
        addressCell.setCellValue(payment.getBeneficiaryAddress() != null ?
                payment.getBeneficiaryAddress() : "");
        addressCell.setCellStyle(textStyle);
    }

    private void addSummarySection(Sheet sheet, PaymentBatch batch) {
        int lastRowNum = sheet.getLastRowNum();
        int summaryStartRow = lastRowNum + 3;

        CellStyle summaryStyle = createSummaryStyle(sheet.getWorkbook());

        // Summary title
        Row summaryTitleRow = sheet.createRow(summaryStartRow);
        Cell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("PAYMENT SUMMARY");
        summaryTitleCell.setCellStyle(summaryStyle);

        // Total payments count
        Row countRow = sheet.createRow(summaryStartRow + 1);
        countRow.createCell(0).setCellValue("Total Payments:");
        countRow.createCell(1).setCellValue(batch.getPaymentCount());

        // Total amount
        Row amountRow = sheet.createRow(summaryStartRow + 2);
        amountRow.createCell(0).setCellValue("Total Amount:");
        Cell totalAmountCell = amountRow.createCell(1);
        totalAmountCell.setCellValue(batch.getTotalAmount().doubleValue() + " " + batch.getCurrency());

        // Batch info
        Row batchRow = sheet.createRow(summaryStartRow + 3);
        batchRow.createCell(0).setCellValue("Batch Number:");
        batchRow.createCell(1).setCellValue(batch.getBatchNumber());

        Row bankRow = sheet.createRow(summaryStartRow + 4);
        bankRow.createCell(0).setCellValue("Bank:");
        bankRow.createCell(1).setCellValue(batch.getBankName());
    }

    private void validatePaymentData(Payment payment) {
        if (!payment.hasValidBankDetails()) {
            throw new BusinessException("INVALID_BANK_DETAILS",
                    "Payment " + payment.getId() + " has incomplete bank details");
        }

        if (payment.getPayee() == null) {
            throw new BusinessException("INVALID_PAYMENT_DATA",
                    "Payment " + payment.getId() + " has no payee information");
        }

        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_PAYMENT_AMOUNT",
                    "Payment " + payment.getId() + " has invalid amount");
        }
    }

    private String generatePaymentComments(Payment payment) {
        StringBuilder comments = new StringBuilder();

        if (payment.getQuotation() != null) {
            comments.append("Project: ").append(payment.getQuotation().getProject().getName());

            if (payment.getQuotation().getDescription() != null &&
                    !payment.getQuotation().getDescription().trim().isEmpty()) {
                comments.append(" - ").append(payment.getQuotation().getDescription());
            }
        }

        // Limit comments to 200 characters for bank compatibility
        String result = comments.toString();
        if (result.length() > 200) {
            result = result.substring(0, 197) + "...";
        }

        return result.isEmpty() ? "Project Payment" : result;
    }

    private String getSanitizedBankName(String bankName) {
        return bankName.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private byte[] convertWorkbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Style creation methods
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createAmountStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    /**
     * Generate file name for bank file
     */
    public String generateFileName(PaymentBatch batch) {
        String sanitizedBankName = getSanitizedBankName(batch.getBankName());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_Payments_%s_%s.xlsx",
                sanitizedBankName, batch.getBatchNumber(), timestamp);
    }

    /**
     * Validate IBAN format for Saudi banks
     */
    public boolean isValidSaudiIBAN(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return false;
        }

        // Remove spaces and convert to uppercase
        String cleanIban = iban.replaceAll("\\s", "").toUpperCase();

        // Saudi IBAN format: SA followed by 22 digits
        if (!cleanIban.matches("^SA\\d{22}$")) {
            return false;
        }

        // Additional IBAN checksum validation could be added here
        return true;
    }

    /**
     * Format amount for Saudi banks (2 decimal places, no currency symbol)
     */
    public String formatAmountForBank(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount.doubleValue());
    }
}