public class ExceptionRecord {
    private final String transactionId;
    private final String settlementId;
    private final String amount;
    private final String date;
    private final String merchantId;
    private final String reason; // human readable reason e.g., "Missing Settlement" or "Amount Mismatch"

    public ExceptionRecord(String transactionId, String settlementId, String amount, String date, String merchantId, String reason) {
        this.transactionId = transactionId == null ? "" : transactionId;
        this.settlementId = settlementId == null ? "" : settlementId;
        this.amount = amount == null ? "" : amount;
        this.date = date == null ? "" : date;
        this.merchantId = merchantId == null ? "" : merchantId;
        this.reason = reason == null ? "" : reason;
    }

    public String getTransactionId() { return transactionId; }
    public String getSettlementId() { return settlementId; }
    public String getAmount() { return amount; }
    public String getDate() { return date; }
    public String getMerchantId() { return merchantId; }
    public String getReason() { return reason; }

    // Backward compatibility: some code expects getStatus()
    public String getStatus() { return reason; }
}
