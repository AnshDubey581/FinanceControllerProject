import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class ReconciliationService {

    // Load CSV into List<String[]>
    public List<String[]> loadCsv(File file) throws Exception {
        if (file == null || !file.exists()) return Collections.emptyList();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return Collections.emptyList();
            // Remove header if it looks like header
            String[] header = rows.get(0);
            if (header.length > 0 && header[0].toLowerCase().contains("transaction")) {
                rows.remove(0);
            }
            List<String[]> cleaned = new ArrayList<>();
            for (String[] r : rows) {
                String[] c = new String[r.length];
                for (int i = 0; i < r.length; i++) c[i] = r[i] == null ? "" : r[i].trim();
                cleaned.add(c);
            }
            return cleaned;
        }
    }

    // Export List<ExceptionRecord> directly to Excel
    public void exportExceptionsToExcelFromRecords(List<ExceptionRecord> records, File outFile) throws Exception {
        if (records == null) records = Collections.emptyList();
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(outFile)) {
            Sheet sheet = workbook.createSheet("Exceptions");
            Row header = sheet.createRow(0);
            String[] headers = {"TransactionID","SettlementID","Amount","Date","MerchantID","Reason"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                CellStyle hs = workbook.createCellStyle();
                Font hf = workbook.createFont();
                hf.setBold(true);
                hs.setFont(hf);
                c.setCellStyle(hs);
            }

            CellStyle missingStyle = workbook.createCellStyle();
            missingStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            missingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle mismatchStyle = workbook.createCellStyle();
            mismatchStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            mismatchStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int r = 1;
            for (ExceptionRecord rec : records) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(rec.getTransactionId());
                row.createCell(1).setCellValue(rec.getSettlementId());
                row.createCell(2).setCellValue(rec.getAmount());
                row.createCell(3).setCellValue(rec.getDate());
                row.createCell(4).setCellValue(rec.getMerchantId());
                Cell reasonCell = row.createCell(5);
                reasonCell.setCellValue(rec.getReason() == null || rec.getReason().isEmpty() ? rec.getStatus() : rec.getReason());
                if ("Missing Settlement".equals(rec.getReason()) || "Missing Settlement".equals(rec.getStatus())) reasonCell.setCellStyle(missingStyle);
                else if ("Amount Mismatch".equals(rec.getReason()) || "Amount Mismatch".equals(rec.getStatus())) reasonCell.setCellStyle(mismatchStyle);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(fos);
        }
    }

    // Optional helper: convert List<String[]> to List<ExceptionRecord>
    // Assumes row layout: txnId, amount, date, merchantId, reason or txnId, amount, date, merchantId, reason,settleId
    public List<ExceptionRecord> rowsToRecords(List<String[]> rows) {
        List<ExceptionRecord> out = new ArrayList<>();
        if (rows == null) return out;
        for (String[] r : rows) {
            String txn = safeGet(r, 0);
            String amt = safeGet(r, 1);
            String date = safeGet(r, 2);
            String merchant = safeGet(r, 3);
            String reason = safeGet(r, 4);
            String settleId = safeGet(r, 5); // optional
            out.add(new ExceptionRecord(txn, settleId.isEmpty() ? "-" : settleId, amt, date, merchant, reason));
        }
        return out;
    }

    private String safeGet(String[] arr, int idx) {
        return (arr != null && arr.length > idx && arr[idx] != null) ? arr[idx].trim() : "";
    }
}
