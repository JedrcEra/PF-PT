import java.time.LocalDate;

public class Booking {
    public static final int OPEN_HOUR = 6, CLOSE_HOUR = 22;   // courts open 6:00 - 22:00
    private String bookingId;
    private String playerId;
    private String courtId;
    private LocalDate date;
    private int startHour;
    private int hours;
    private double total;
    private String status;   // Booked / Cancelled

    public Booking(String bookingId, String playerId, String courtId, LocalDate date,
                   int startHour, int hours, double total, String status) {
        this.bookingId = bookingId;
        this.playerId = playerId;
        this.courtId = courtId;
        setSchedule(date, startHour, hours);
        this.total = total;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getPlayerId() { return playerId; }
    public String getCourtId() { return courtId; }
    public LocalDate getDate() { return date; }
    public int getStartHour() { return startHour; }
    public int getHours() { return hours; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public boolean isActive() { return "Booked".equals(status); }

    /** Validated setter: invalid hours or times can never be stored. */
    public void setSchedule(LocalDate date, int startHour, int hours) {
        if (date == null) throw new IllegalArgumentException("Date is required.");
        if (hours < 1) throw new IllegalArgumentException("Hours must be at least 1.");
        if (startHour < OPEN_HOUR || startHour + hours > CLOSE_HOUR)
            throw new IllegalArgumentException("Bookings must fall between " + OPEN_HOUR + ":00 and " + CLOSE_HOUR + ":00.");
        this.date = date; this.startHour = startHour; this.hours = hours;
    }

    /** POLYMORPHISM: asks the Membership for its discount without checking which plan it is. */
    public double computeTotal(double hourlyRate, Membership m) {
        double discount = (m != null && m.isActive()) ? m.getDiscountRate() : 0;
        this.total = hours * hourlyRate * (1 - discount);
        return total;
    }

    public void setTotal(double t) { this.total = t; }

    public void cancel() {
        if (!isActive()) throw new IllegalStateException("Booking is already cancelled.");
        this.status = "Cancelled";
    }

    public boolean conflictsWith(String courtId, LocalDate date, int start, int hrs) {
        return isActive() && this.courtId.equals(courtId) && this.date.equals(date)
                && start < this.startHour + this.hours && this.startHour < start + hrs;
    }

    public String timeText() { return startHour + ":00 - " + (startHour + hours) + ":00"; }

    public String toRecord() {
        return String.join("|", bookingId, playerId, courtId, date.toString(), String.valueOf(startHour),
                String.valueOf(hours), String.valueOf(total), status);
    }
    public static Booking fromRecord(String line) {
        String[] p = line.split("\\|", -1);
        return new Booking(p[0], p[1], p[2], LocalDate.parse(p[3]), Integer.parseInt(p[4]),
                Integer.parseInt(p[5]), Double.parseDouble(p[6]), p[7]);
    }
}
