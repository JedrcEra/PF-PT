import java.time.LocalDate;

/** ABSTRACTION: declares what every plan must provide, not how. */
public abstract class Membership {
    public static final String[] TYPES = {"Day Pass", "Monthly", "Premium"};

    private String membershipId;
    private String playerId;
    private LocalDate startDate;

    public Membership(String membershipId, String playerId, LocalDate startDate) {
        this.membershipId = membershipId;
        this.playerId = playerId;
        this.startDate = startDate;
    }

    public String getMembershipId() { return membershipId; }
    public String getPlayerId() { return playerId; }
    public LocalDate getStartDate() { return startDate; }

    public abstract String getTypeName();
    public abstract double getFee();
    public abstract double getDiscountRate();   // e.g. 0.15 = 15% off
    public abstract int getValidityDays();

    public LocalDate getExpiryDate() { return startDate.plusDays(getValidityDays() - 1); }
    public boolean isActive() { return !LocalDate.now().isAfter(getExpiryDate()); }

    public String toRecord() { return String.join("|", membershipId, getTypeName(), playerId, startDate.toString()); }

    @Override public String toString() {
        return String.format("%s - PHP %.0f - %.0f%% off", getTypeName(), getFee(), getDiscountRate() * 100);
    }

    public static Membership create(String type, String id, String playerId, LocalDate start) {
        switch (type) {
            case "Day Pass": return new DayPass(id, playerId, start);
            case "Monthly":  return new MonthlyMembership(id, playerId, start);
            case "Premium":  return new PremiumMembership(id, playerId, start);
            default: throw new IllegalArgumentException("Unknown membership type: " + type);
        }
    }

    public static Membership fromRecord(String line) {
        String[] p = line.split("\\|", -1);
        return create(p[1], p[0], p[2], LocalDate.parse(p[3]));
    }
}
