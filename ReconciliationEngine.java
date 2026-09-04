import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;

public class ReconciliationEngine {

    private int totalTxn;
    private int totalSettle;

    public int getTotalTransactions() { return totalTxn; }
    public int getTotalSettlements() { return totalSettle; }

    /**
     * Run reconciliation between transactions and settlements files.
     * Assumes:
     *  - transactions CSV columns: txnId(0), amount(1), date(2), merchantId(3)
     *  - settlements CSV columns: settlementId(0), transactionId(1), amount(2), date(3)
     */
    public List<ExceptionRecord> run(File txnFile, File settleFile) throws Exception {
        if (txnFile == null || settleFile == null) {
            throw new IllegalArgumentException("Transaction or settlement file is null");
        }

        List<String[]> txns = readCsv(txnFile);
        List<String[]> settles = readCsv(settleFile);

        totalTxn = txns.size();
        totalSettle = settles.size();

        // Settlement map keyed by transaction_id (column 1)
        Map<String, String[]> settleMap = new HashMap<>();
        for (String[] s : settles) {
            String txnKey = safeGet(s, 1); // transaction_id
            if (!txnKey.isEmpty()) settleMap.put(txnKey, s);
        }

        List<ExceptionRecord> exceptions = new ArrayList<>();
        for (String[] t : txns) {
            String txnId = safeGet(t, 0);
            String txnAmount = safeGet(t, 1);
            String txnDate = safeGet(t, 2);
            String txnMerchant = safeGet(t, 3);

            if (txnId.isEmpty()) continue;

            if (!settleMap.containsKey(txnId)) {
                exceptions.add(new ExceptionRecord(txnId, "-", txnAmount, txnDate, txnMerchant, "Missing Settlement"));
            } else {
                String[] s = settleMap.get(txnId);
                String settleId = safeGet(s, 0);     // settlement_id
                String settleAmount = safeGet(s, 2); // settlement amount

                if (!amountsEqual(txnAmount, settleAmount)) {
                    exceptions.add(new ExceptionRecord(txnId, settleId, txnAmount, txnDate, txnMerchant, "Amount Mismatch"));
                }
            }
        }
        return exceptions;
    }

    private List<String[]> readCsv(File file) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return Collections.emptyList();

            // Normalize and detect header row robustly (handles transactions.csv and settlements.csv)
            String[] header = rows.get(0);
            String headerLine = String.join(",", header).toLowerCase();

            // Common header indicators
            if (headerLine.contains("transaction") || headerLine.contains("transaction_id")
                    || headerLine.contains("settlement") || headerLine.contains("settlement_id")) {
                rows.remove(0);
            }

            // Trim cells and skip fully-empty rows
            List<String[]> cleaned = new ArrayList<>();
            for (String[] r : rows) {
                if (r == null) continue;
                boolean allEmpty = true;
                String[] c = new String[r.length];
                for (int i = 0; i < r.length; i++) {
                    c[i] = r[i] == null ? "" : r[i].trim();
                    if (!c[i].isEmpty()) allEmpty = false;
                }
                if (!allEmpty) cleaned.add(c);
            }
            return cleaned;
        }
    }

    private String safeGet(String[] arr, int idx) {
        return (arr != null && arr.length > idx && arr[idx] != null) ? arr[idx].trim() : "";
    }

    private boolean amountsEqual(String a, String b) {
        try {
            BigDecimal ba = new BigDecimal(a.isEmpty() ? "0" : a);
            BigDecimal bb = new BigDecimal(b.isEmpty() ? "0" : b);
            return ba.compareTo(bb) == 0;
        } catch (Exception ex) {
            return a.equals(b);
        }
    }
}
