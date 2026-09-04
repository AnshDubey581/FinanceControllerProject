import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            File tx = new File("transactions.csv");
            File st = new File("settlements.csv");

            if (!tx.exists() || !st.exists()) {
                System.out.println("transactions.csv or settlements.csv not found in project root.");
                return;
            }

            ReconciliationEngine engine = new ReconciliationEngine();
            List<ExceptionRecord> exceptions = engine.run(tx, st);

            System.out.println("Total Transactions: " + engine.getTotalTransactions());
            System.out.println("Total Settlements: " + engine.getTotalSettlements());
            System.out.println("Exceptions found: " + exceptions.size());

            if (!exceptions.isEmpty()) {
                ReconciliationService svc = new ReconciliationService();
                File out = new File("Exception_Report.xlsx");
                svc.exportExceptionsToExcelFromRecords(exceptions, out);
                System.out.println("Exported Exception_Report.xlsx");
            } else {
                System.out.println("No exceptions to export.");
            }
        } catch (Exception e) {
            System.err.println("Error running reconciliation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
