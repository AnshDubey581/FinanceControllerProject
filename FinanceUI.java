import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.util.List;

public class FinanceUI extends Application {

    private Label summaryLabel = new Label("Summary will appear here...");
    private TableView<ExceptionRecord> exceptionTable = new TableView<>();

    private File transactionsFile;
    private File settlementsFile;
    private List<ExceptionRecord> lastExceptions;

    @Override
    public void start(Stage primaryStage) {
        Button uploadTxnBtn = new Button("Upload Transactions CSV");
        Button uploadSettleBtn = new Button("Upload Settlements CSV");
        Button runBtn = new Button("Run Reconciliation");
        Button exportBtn = new Button("Export to Excel");

        TableColumn<ExceptionRecord, String> txnCol = new TableColumn<>("Transaction ID");
        txnCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));

        TableColumn<ExceptionRecord, String> settleCol = new TableColumn<>("Settlement ID");
        settleCol.setCellValueFactory(new PropertyValueFactory<>("settlementId"));

        TableColumn<ExceptionRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status")); // matches getStatus()

        exceptionTable.getColumns().addAll(txnCol, settleCol, statusCol);

        FileChooser csvChooser = new FileChooser();
        csvChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        uploadTxnBtn.setOnAction(e -> {
            File f = csvChooser.showOpenDialog(primaryStage);
            if (f != null) {
                transactionsFile = f;
                showInfo("Transactions file selected: " + f.getName());
            }
        });

        uploadSettleBtn.setOnAction(e -> {
            File f = csvChooser.showOpenDialog(primaryStage);
            if (f != null) {
                settlementsFile = f;
                showInfo("Settlements file selected: " + f.getName());
            }
        });

        runBtn.setOnAction(e -> {
            if (transactionsFile == null || settlementsFile == null) {
                showAlert("Please upload both Transactions and Settlements CSV files first.");
                return;
            }

            runBtn.setDisable(true);
            uploadTxnBtn.setDisable(true);
            uploadSettleBtn.setDisable(true);
            exportBtn.setDisable(true);
            summaryLabel.setText("Running reconciliation...");

            Task<List<ExceptionRecord>> task = new Task<>() {
                @Override
                protected List<ExceptionRecord> call() throws Exception {
                    ReconciliationEngine engine = new ReconciliationEngine();
                    List<ExceptionRecord> exceptions = engine.run(transactionsFile, settlementsFile);
                    // store totals in engine if needed; we will create a new engine to read totals below (or adapt to return engine)
                    // For simplicity, return exceptions; UI will create a new engine to get totals if needed.
                    return exceptions;
                }
            };

            task.setOnSucceeded(ev -> {
                lastExceptions = task.getValue();
                exceptionTable.getItems().setAll(lastExceptions);

                // To get totals, run a quick engine run to populate totals (or modify engine to return totals with exceptions)
                try {
                    ReconciliationEngine engine = new ReconciliationEngine();
                    engine.run(transactionsFile, settlementsFile); // populates totals inside engine
                    summaryLabel.setText("Transactions: " + engine.getTotalTransactions() +
                                         " | Settlements: " + engine.getTotalSettlements() +
                                         " | Exceptions: " + lastExceptions.size());
                } catch (Exception ex) {
                    summaryLabel.setText("Exceptions: " + lastExceptions.size());
                }

                runBtn.setDisable(false);
                uploadTxnBtn.setDisable(false);
                uploadSettleBtn.setDisable(false);
                exportBtn.setDisable(false);
            });

            task.setOnFailed(ev -> {
                showAlert("Reconciliation failed: " + task.getException().getMessage());
                runBtn.setDisable(false);
                uploadTxnBtn.setDisable(false);
                uploadSettleBtn.setDisable(false);
                exportBtn.setDisable(false);
                summaryLabel.setText("Summary will appear here...");
            });

            new Thread(task).start();
        });

        exportBtn.setOnAction(e -> {
            if (lastExceptions == null || lastExceptions.isEmpty()) {
                showAlert("No exceptions to export. Run reconciliation first.");
                return;
            }
            FileChooser saveChooser = new FileChooser();
            saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            saveChooser.setInitialFileName("Exception_Report.xlsx");
            File out = saveChooser.showSaveDialog(primaryStage);
            if (out != null) {
                Task<Void> exportTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ReconciliationService svc = new ReconciliationService();
                        svc.exportExceptionsToExcelFromRecords(lastExceptions, out);
                        return null;
                    }
                };
                exportTask.setOnSucceeded(ev -> showInfo("Exported to " + out.getAbsolutePath()));
                exportTask.setOnFailed(ev -> showAlert("Export failed: " + exportTask.getException().getMessage()));
                new Thread(exportTask).start();
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(uploadTxnBtn, uploadSettleBtn, runBtn, exportBtn, summaryLabel, exceptionTable);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setTitle("Finance Exception Controller");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
