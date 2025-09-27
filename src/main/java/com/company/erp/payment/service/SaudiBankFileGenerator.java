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
import java.util.ArrayList;
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

        // Set optimized column widths for Saudi bank format
        sheet.setColumnWidth(0, 4000);  // Bank
        sheet.setColumnWidth(1, 6000);  // Account Number (IBAN can be long)
        sheet.setColumnWidth(2, 3000);  // Amount
        sheet.setColumnWidth(3, 10000); // Comments
        sheet.setColumnWidth(4, 6000);  // Employee Name
        sheet.setColumnWidth(5, 4000);  // National ID/Iqama
        sheet.setColumnWidth(6, 12000); // Beneficiary Address

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

        // Column A: Bank
        Cell bankCell = row.createCell(colNum++);
        bankCell.setCellValue(payment.getBankName() != null ? payment.getBankName() : "");
        bankCell.setCellStyle(textStyle);

        // Column B: Account Number (IBAN preferred, fallback to account number)
        Cell accountCell = row.createCell(colNum++);
        String accountNumber = payment.getIban() != null ? 
                              payment.getIban() : 
                              payment.getAccountNumber();
        accountCell.setCellValue(accountNumber != null ? accountNumber : "");
        accountCell.setCellStyle(textStyle);

        // Column C: Amount
        Cell amountCell = row.createCell(colNum++);
        amountCell.setCellValue(payment.getAmount().doubleValue());
        amountCell.setCellStyle(amountStyle);

        // Column D: Comments
        Cell commentsCell = row.createCell(colNum++);
        String comments = generatePaymentComments(payment);
        commentsCell.setCellValue(comments != null ? comments : "");
        commentsCell.setCellStyle(textStyle);

        // Column E: Employee Name
        Cell nameCell = row.createCell(colNum++);
        String employeeName = payment.getPayee().getFullName();
        nameCell.setCellValue(employeeName != null ? employeeName : "");
        nameCell.setCellStyle(textStyle);

        // Column F: National ID/Iqama ID
        Cell idCell = row.createCell(colNum++);
        String nationalId = payment.getPayee().getNationalId() != null ?
                           payment.getPayee().getNationalId() : 
                           payment.getPayee().getIqamaId();
        idCell.setCellValue(nationalId != null ? nationalId : "");
        idCell.setCellStyle(textStyle);

        // Column G: Beneficiary Address
        Cell addressCell = row.createCell(colNum++);
        String address = payment.getBeneficiaryAddress() != null ?
                        payment.getBeneficiaryAddress() : 
                        (payment.getPayee().getBankDetails() != null ? 
                         payment.getPayee().getBankDetails().getBeneficiaryAddress() : "");
        addressCell.setCellValue(address != null ? address : "");
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
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        if (payment.getPayee() == null) {
            errors.add("Payment must have a payee");
        } else {
            if (payment.getPayee().getFullName() == null || payment.getPayee().getFullName().trim().isEmpty()) {
                errors.add("Employee name is required");
            }
            
            if (payment.getIban() == null && payment.getAccountNumber() == null) {
                errors.add("Account number or IBAN is required");
            }
            
            if (payment.getPayee().getNationalId() == null && payment.getPayee().getIqamaId() == null) {
                errors.add("National ID or Iqama ID is required");
            }
        }
        
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Valid payment amount is required");
        }
        
        if (payment.getBankName() == null || payment.getBankName().trim().isEmpty()) {
            errors.add("Bank name is required");
        }
        
        if (!errors.isEmpty()) {
            throw new BusinessException("INVALID_PAYMENT_DATA", 
                    "Payment validation failed: " + String.join(", ", errors));
        }
    }

    private String generatePaymentComments(Payment payment) {
        // Create meaningful comments for the bank
        StringBuilder comments = new StringBuilder();
        
        if (payment.getComments() != null && !payment.getComments().trim().isEmpty()) {
            comments.append(payment.getComments());
        } else if (payment.getQuotation() != null) {
            comments.append("Payment for quotation: ").append(payment.getQuotation().getDescription());
        } else {
            comments.append("Salary/Payment to ").append(payment.getPayee().getFullName());
        }
        
        // Add period/month if available
        if (payment.getQuotation() != null && payment.getQuotation().getProject() != null) {
            comments.append(" - ").append(payment.getQuotation().getProject().getName());
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

    /**
     * Create a sample Excel file for testing purposes
     */
    public byte[] createSampleExcelFile() {
        logger.info("Creating sample Excel file for testing");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = createMainSheet(workbook, "Sample Bank");
            populateHeaders(sheet, "Sample Bank");
            
            // Add sample data rows
            int rowNum = 4;
            CellStyle amountStyle = createAmountStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            // Sample row 1
            Row sampleRow1 = sheet.createRow(rowNum++);
            sampleRow1.createCell(0).setCellValue("Al Rajhi Bank");
            sampleRow1.createCell(1).setCellValue("SA442000000123456789012345");
            sampleRow1.createCell(2).setCellValue(5000.00);
            sampleRow1.createCell(3).setCellValue("Salary for September 2025");
            sampleRow1.createCell(4).setCellValue("Ahmed Mohammed Al-Rashid");
            sampleRow1.createCell(5).setCellValue("1234567890");
            sampleRow1.createCell(6).setCellValue("Riyadh, Saudi Arabia");

            // Sample row 2
            Row sampleRow2 = sheet.createRow(rowNum++);
            sampleRow2.createCell(0).setCellValue("Al Rajhi Bank");
            sampleRow2.createCell(1).setCellValue("SA442000000123456789012346");
            sampleRow2.createCell(2).setCellValue(3500.00);
            sampleRow2.createCell(3).setCellValue("Project allowance");
            sampleRow2.createCell(4).setCellValue("Sarah Abdullah");
            sampleRow2.createCell(5).setCellValue("9876543210");
            sampleRow2.createCell(6).setCellValue("Jeddah, Saudi Arabia");

            // Apply styles to sample data
            for (int i = 0; i < 2; i++) {
                Row row = sheet.getRow(4 + i);
                for (int j = 0; j < 7; j++) {
                    Cell cell = row.getCell(j);
                    if (j == 2) { // Amount column
                        cell.setCellStyle(amountStyle);
                    } else {
                        cell.setCellStyle(textStyle);
                    }
                }
            }

            return convertWorkbookToBytes(workbook);

        } catch (IOException e) {
            logger.error("Error creating sample Excel file", e);
            throw new BusinessException("SAMPLE_FILE_GENERATION_ERROR",
                    "Failed to create sample file: " + e.getMessage());
        }
    }
}