import java.time.LocalDate;

public class Payment {
    public static final String PENDING = "Pending", CONFIRMED = "Confirmed", REFUNDED = "Refunded";
    private String paymentId;
    private String playerId;
    private String reference;   // booking ID or membership ID
    private double amount;
    private String status;
    private LocalDate date;

    public Payment(String paymentId, String playerId, String reference, double amount, String status, LocalDate date) {
        this.paymentId = paymentId;
        this.playerId = playerId;
        this.reference = reference;
        setAmount(amount);
        setStatus(status);
        this.date = date;
    }

    public String getPaymentId() { return paymentId; }
    public String getPlayerId() { return playerId; }
    public String getReference() { return reference; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDate getDate() { return date; }

    public void setAmount(double a) {
        if (a < 0) throw new IllegalArgumentException("Amount cannot be negative.");
        this.amount = a;
    }
    public void setStatus(String s) {
        if (!PENDING.equals(s) && !CONFIRMED.equals(s) && !REFUNDED.equals(s))
            throw new IllegalArgumentException("Invalid payment status: " + s);
        this.status = s;
    }

    public String receipt(String playerName) {
        return "===== PICKLEFLOW RECEIPT =====\n"
             + "Payment ID : " + paymentId + "\n"
             + "Date       : " + date + "\n"
             + "Player     : " + playerName + " (" + playerId + ")\n"
             + "For        : " + reference + "\n"
             + "Amount     : PHP " + String.format("%.2f", amount) + "\n"
             + "Status     : " + status + "\n"
             + "==============================";
    }

    public String toRecord() {
        return String.join("|", paymentId, playerId, reference, String.valueOf(amount), status, date.toString());
    }
    public static Payment fromRecord(String line) {
        String[] p = line.split("\\|", -1);
        return new Payment(p[0], p[1], p[2], Double.parseDouble(p[3]), p[4], LocalDate.parse(p[5]));
    }
}
