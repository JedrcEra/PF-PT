import java.time.LocalDate;

public class PremiumMembership extends Membership {
    public PremiumMembership(String id, String playerId, LocalDate start) { super(id, playerId, start); }
    @Override public String getTypeName() { return "Premium"; }
    @Override public double getFee() { return 1500; }
    @Override public double getDiscountRate() { return 0.30; }
    @Override public int getValidityDays() { return 30; }
}
