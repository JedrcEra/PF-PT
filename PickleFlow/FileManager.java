import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;


public class FileManager {
    private final Path dir;

    public FileManager(String folder) {
        this.dir = Paths.get(folder);
        try { Files.createDirectories(dir); } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public static String clean(String v, String label) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(label + " is required.");
        return v.trim().replace("|", "/");
    }

    private List<String> readLines(String file) {
        Path p = dir.resolve(file);
        try {
            if (!Files.exists(p)) return new ArrayList<>();
            return Files.readAllLines(p, StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private void writeLines(String file, List<String> lines) {
        try { Files.write(dir.resolve(file), lines, StandardCharsets.UTF_8); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private <T> List<T> load(String file, Function<String, T> parser) {
        List<T> out = new ArrayList<>();
        for (String line : readLines(file)) if (!line.trim().isEmpty()) out.add(parser.apply(line));
        return out;
    }

    private <T> void save(String file, List<T> items, Function<T, String> toRecord) {
        List<String> lines = new ArrayList<>();
        for (T t : items) lines.add(toRecord.apply(t));
        writeLines(file, lines);
    }

    public List<Player> loadPlayers() { return load("players.txt", Player::fromRecord); }
    public void savePlayers(List<Player> l) { save("players.txt", l, Player::toRecord); }
    public List<Staff> loadStaff() { return load("staff.txt", Staff::fromRecord); }
    public void saveStaff(List<Staff> l) { save("staff.txt", l, Staff::toRecord); }
    public List<Court> loadCourts() { return load("courts.txt", Court::fromRecord); }
    public void saveCourts(List<Court> l) { save("courts.txt", l, Court::toRecord); }
    public List<Booking> loadBookings() { return load("bookings.txt", Booking::fromRecord); }
    public void saveBookings(List<Booking> l) { save("bookings.txt", l, Booking::toRecord); }
    public List<Membership> loadMemberships() { return load("memberships.txt", Membership::fromRecord); }
    public void saveMemberships(List<Membership> l) { save("memberships.txt", l, Membership::toRecord); }
    public List<Payment> loadPayments() { return load("payments.txt", Payment::fromRecord); }
    public void savePayments(List<Payment> l) { save("payments.txt", l, Payment::toRecord); }
}
