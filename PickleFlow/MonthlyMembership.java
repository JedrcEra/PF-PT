import java.time.LocalDate;

public class MonthlyMembership extends Membership {
    public MonthlyMembership(String id, String playerId, LocalDate start) { super(id, playerId, start); }
    @Override public String getTypeName() { return "Monthly"; }
    @Override public double getFee() { return 800; }
    @Override public double getDiscountRate() { return 0.15; }
    @Override public int getValidityDays() { return 30; }
}
