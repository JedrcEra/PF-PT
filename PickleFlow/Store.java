import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/** Holds the records in memory and writes them back through FileManager after every change. */
public class Store {
    private final FileManager fm = new FileManager("data");
    public final List<Player> players;
    public final List<Staff> staff;
    public final List<Court> courts;
    public final List<Booking> bookings;
    public final List<Membership> memberships;
    public final List<Payment> payments;

    public Store() {
        players = fm.loadPlayers(); staff = fm.loadStaff(); courts = fm.loadCourts();
        bookings = fm.loadBookings(); memberships = fm.loadMemberships(); payments = fm.loadPayments();
        if (staff.isEmpty()) { staff.add(new Staff("S001", "Administrator", "09000000000", "admin123")); saveStaff(); }
        if (courts.isEmpty()) {
            courts.add(new Court("C001", "Court 1", 300, Court.AVAILABLE));
            courts.add(new Court("C002", "Court 2", 300, Court.AVAILABLE));
            courts.add(new Court("C003", "Court 3", 350, Court.AVAILABLE));
            saveCourts();
        }
    }

    public void savePlayers() { fm.savePlayers(players); }
    public void saveStaff() { fm.saveStaff(staff); }
    public void saveCourts() { fm.saveCourts(courts); }
    public void saveBookings() { fm.saveBookings(bookings); }
    public void saveMemberships() { fm.saveMemberships(memberships); }
    public void savePayments() { fm.savePayments(payments); }

    private <T> String nextId(String prefix, List<T> list, Function<T, String> idOf) {
        int max = 0;
        for (T t : list) {
            try { max = Math.max(max, Integer.parseInt(idOf.apply(t).substring(prefix.length()))); }
            catch (RuntimeException ignored) { }
        }
        return String.format("%s%03d", prefix, max + 1);
    }
    public String nextPlayerId() { return nextId("P", players, Player::getPersonId); }
    public String nextCourtId() { return nextId("C", courts, Court::getCourtId); }
    public String nextBookingId() { return nextId("B", bookings, Booking::getBookingId); }
    public String nextMembershipId() { return nextId("M", memberships, Membership::getMembershipId); }
    public String nextPaymentId() { return nextId("PAY", payments, Payment::getPaymentId); }

    /** Returns a Player or Staff (POLYMORPHISM: callers just use Person). */
    public Person authenticate(String id, String pw) {
        for (Person p : players) if (p.login(id, pw)) return p;
        for (Person p : staff) if (p.login(id, pw)) return p;
        return null;
    }

    public Player findPlayer(String id) { for (Player p : players) if (p.getPersonId().equals(id)) return p; return null; }
    public String playerName(String id) { Player p = findPlayer(id); return p == null ? "(deleted)" : p.getFullName(); }
    public Court findCourt(String id) { for (Court c : courts) if (c.getCourtId().equals(id)) return c; return null; }
    public Booking findBooking(String id) { for (Booking b : bookings) if (b.getBookingId().equals(id)) return b; return null; }
    public Payment findPayment(String id) { for (Payment p : payments) if (p.getPaymentId().equals(id)) return p; return null; }
    public Payment paymentFor(String reference) { for (Payment p : payments) if (p.getReference().equals(reference)) return p; return null; }

    /** The player's current membership, or null. Returned as the abstract type. */
    public Membership activeMembership(String playerId) {
        for (Membership m : memberships) if (m.getPlayerId().equals(playerId) && m.isActive()) return m;
        return null;
    }
    public Membership anyMembership(String playerId) {
        for (Membership m : memberships) if (m.getPlayerId().equals(playerId)) return m;
        return null;
    }

    public boolean hasConflict(String courtId, LocalDate d, int start, int hrs, String ignoreBookingId) {
        for (Booking b : bookings)
            if (!b.getBookingId().equals(ignoreBookingId) && b.conflictsWith(courtId, d, start, hrs)) return true;
        return false;
    }

    /** Cancels a booking and settles its payment (refund if paid, remove if still pending). */
    public void cancelBooking(Booking b) {
        b.cancel();
        Payment p = paymentFor(b.getBookingId());
        if (p != null) {
            if (Payment.CONFIRMED.equals(p.getStatus())) p.setStatus(Payment.REFUNDED);
            else if (Payment.PENDING.equals(p.getStatus())) payments.remove(p);
        }
        saveBookings(); savePayments();
    }

    public void deletePlayer(String id) {
        for (Booking b : new ArrayList<>(bookings)) if (b.getPlayerId().equals(id) && b.isActive()) cancelBooking(b);
        memberships.removeIf(m -> m.getPlayerId().equals(id));
        players.removeIf(p -> p.getPersonId().equals(id));
        savePlayers(); saveMemberships();
    }
}
