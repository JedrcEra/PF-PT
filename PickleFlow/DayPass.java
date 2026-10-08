import java.time.LocalDate;

public class DayPass extends Membership {
    public DayPass(String id, String playerId, LocalDate start) { super(id, playerId, start); }
    @Override public String getTypeName() { return "Day Pass"; }
    @Override public double getFee() { return 100; }
    @Override public double getDiscountRate() { return 0.05; }
    @Override public int getValidityDays() { return 1; }
}
