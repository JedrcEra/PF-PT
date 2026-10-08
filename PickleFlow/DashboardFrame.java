import javax.swing.*;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Main window. Staff and Players share the same code but see different tabs (POLYMORPHISM via Person). */
public class DashboardFrame extends JFrame {
    private final Store store;
    private final Person user;
    private final boolean isStaff;
    private final List<Runnable> refreshers = new ArrayList<>();

    public DashboardFrame(Store store, Person user) {
        super("PickleFlow - " + user.getRole());
        this.store = store;
        this.user = user;
        this.isStaff = user instanceof Staff;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel who = new JLabel("Logged in as " + user.getFullName() + " (" + user.getPersonId() + ", " + user.getRole() + ")");
        who.setFont(who.getFont().deriveFont(Font.BOLD, 14f));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame(store).setVisible(true); });
        top.add(who, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Courts", courtsTab());
        tabs.add(isStaff ? "All Bookings" : "My Bookings", bookingsTab());
        tabs.add(isStaff ? "Memberships" : "My Membership", membershipTab());
        tabs.add(isStaff ? "Payments & Reports" : "My Payments", paymentsTab());
        if (isStaff) tabs.add("Players", playersTab());
        tabs.add("Profile", profileTab());

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        refreshAll();
    }

    // ---------- helpers ----------
    private void refreshAll() { for (Runnable r : refreshers) r.run(); }

    private void act(Runnable r) {
        try { r.run(); }
        catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "PickleFlow", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "PickleFlow", JOptionPane.INFORMATION_MESSAGE); }
    private boolean confirm(String msg) { return JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }

    private void showReceipt(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, area, "Receipt", JOptionPane.PLAIN_MESSAGE);
    }

    private LocalDate parseDate(String s) {
        try { return LocalDate.parse(s.trim()); }
        catch (DateTimeException e) { throw new IllegalArgumentException("Date must be in yyyy-MM-dd format."); }
    }

    private double parseMoney(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a valid number."); }
    }

    private JPanel tabRoot() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JPanel row(Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (Component c : items) p.add(c);
        return p;
    }

    private JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> act(action));
        return b;
    }

    private String freeHours(Court c, LocalDate d) {
        if (!c.isAvailable()) return "-";
        StringBuilder sb = new StringBuilder();
        int h = Booking.OPEN_HOUR;
        while (h < Booking.CLOSE_HOUR) {
            if (store.hasConflict(c.getCourtId(), d, h, 1, null)) { h++; continue; }
            int s = h;
            while (h < Booking.CLOSE_HOUR && !store.hasConflict(c.getCourtId(), d, h, 1, null)) h++;
            if (sb.length() > 0) sb.append(", ");
            sb.append(s).append("-").append(h);
        }
        return sb.length() == 0 ? "Fully booked" : sb.toString();
    }

    // ---------- COURTS (courts.txt) ----------
    private JPanel courtsTab() {
        JPanel root = tabRoot();
        JTextField dateF = new JTextField(LocalDate.now().toString(), 10);
        TablePanel tp = new TablePanel("ID", "Court", "Rate / hour (PHP)", "Status", "Free hours on date");
        Runnable refresh = () -> {
            LocalDate d;
            try { d = parseDate(dateF.getText()); } catch (IllegalArgumentException e) { d = LocalDate.now(); }
            List<Object[]> rows = new ArrayList<>();
            for (Court c : store.courts)
                rows.add(new Object[]{c.getCourtId(), c.getName(), String.format("%.2f", c.getHourlyRate()), c.getStatus(), freeHours(c, d)});
            tp.setRows(rows);
        };
        refreshers.add(refresh);
        root.add(row(new JLabel("Availability date (yyyy-MM-dd):"), dateF, button("Check", () -> { parseDate(dateF.getText()); refresh.run(); })), BorderLayout.NORTH);
        root.add(tp, BorderLayout.CENTER);

        if (isStaff) {
            JPanel bar = row(
                button("Add Court", () -> {
                    JTextField n = new JTextField(), r = new JTextField();
                    JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
                    f.add(new JLabel("Court name:")); f.add(n);
                    f.add(new JLabel("Hourly rate (PHP):")); f.add(r);
                    if (JOptionPane.showConfirmDialog(this, f, "Add court", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                    store.courts.add(new Court(store.nextCourtId(), n.getText(), parseMoney(r.getText()), Court.AVAILABLE));
                    store.saveCourts(); refreshAll();
                }),
                button("Change Rate", () -> {
                    String id = tp.selectedId(); if (id == null) return;
                    Court c = store.findCourt(id);
                    String v = JOptionPane.showInputDialog(this, "New hourly rate for " + c.getName() + ":", c.getHourlyRate());
                    if (v == null) return;
                    c.setHourlyRate(parseMoney(v));
                    store.saveCourts(); refreshAll();
                }),
                button("Toggle Available / Maintenance", () -> {
                    String id = tp.selectedId(); if (id == null) return;
                    Court c = store.findCourt(id);
                    c.setStatus(c.isAvailable() ? Court.MAINTENANCE : Court.AVAILABLE);
                    store.saveCourts(); refreshAll();
                }),
                button("Delete Court", () -> {
                    String id = tp.selectedId(); if (id == null) return;
                    for (Booking b : store.bookings)
                        if (b.getCourtId().equals(id) && b.isActive()) throw new IllegalStateException("This court still has active bookings. Cancel them first.");
                    if (!confirm("Delete court " + id + "?")) return;
                    store.courts.remove(store.findCourt(id));
                    store.saveCourts(); refreshAll();
                }));
            root.add(bar, BorderLayout.SOUTH);
        }
        return root;
    }

    // ---------- BOOKINGS (bookings.txt) ----------
    private JPanel bookingsTab() {
        JPanel root = tabRoot();
        TablePanel tp = new TablePanel("Booking", "Player", "Court", "Date", "Time", "Total (PHP)", "Status");
        refreshers.add(() -> {
            List<Object[]> rows = new ArrayList<>();
            for (Booking b : store.bookings) {
                if (!isStaff && !b.getPlayerId().equals(user.getPersonId())) continue;
                Court c = store.findCourt(b.getCourtId());
                rows.add(new Object[]{b.getBookingId(), store.playerName(b.getPlayerId()), c == null ? b.getCourtId() : c.getName(),
                        b.getDate(), b.timeText(), String.format("%.2f", b.getTotal()), b.getStatus()});
            }
            tp.setRows(rows);
        });
        root.add(tp, BorderLayout.CENTER);

        JButton cancel = button("Cancel Booking", () -> {
            String id = tp.selectedId(); if (id == null) return;
            Booking b = store.findBooking(id);
            if (!confirm("Cancel booking " + id + "?")) return;
            store.cancelBooking(b);
            refreshAll();
        });

        if (isStaff) { root.add(row(cancel), BorderLayout.SOUTH); return root; }

        JComboBox<Court> courtBox = new JComboBox<>();
        refreshers.add(() -> {
            Object sel = courtBox.getSelectedItem();
            courtBox.removeAllItems();
            for (Court c : store.courts) if (c.isAvailable()) courtBox.addItem(c);
            if (sel != null) courtBox.setSelectedItem(sel);
        });
        JTextField dateF = new JTextField(LocalDate.now().toString(), 9);
        JComboBox<Integer> startBox = new JComboBox<>();
        for (int h = Booking.OPEN_HOUR; h < Booking.CLOSE_HOUR; h++) startBox.addItem(h);
        JSpinner hoursSp = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));

        JButton book = button("Book Slot", () -> {
            Court c = (Court) courtBox.getSelectedItem();
            if (c == null) throw new IllegalStateException("No court is available right now.");
            LocalDate d = parseDate(dateF.getText());
            int start = (Integer) startBox.getSelectedItem(), hrs = (Integer) hoursSp.getValue();
            if (d.isBefore(LocalDate.now()) || (d.equals(LocalDate.now()) && start <= java.time.LocalTime.now().getHour()))
                throw new IllegalArgumentException("That time has already passed.");
            Booking b = new Booking(store.nextBookingId(), user.getPersonId(), c.getCourtId(), d, start, hrs, 0, "Booked");
            if (store.hasConflict(c.getCourtId(), d, start, hrs, null)) throw new IllegalStateException("That slot is already booked.");
            b.computeTotal(c.getHourlyRate(), store.activeMembership(user.getPersonId()));
            store.bookings.add(b);
            Payment p = new Payment(store.nextPaymentId(), user.getPersonId(), b.getBookingId(), b.getTotal(), Payment.PENDING, LocalDate.now());
            store.payments.add(p);
            store.saveBookings(); store.savePayments(); refreshAll();
            showReceipt(p.receipt(user.getFullName()) + "\nPay at the counter. Staff will confirm your payment.");
        });

        JButton resched = button("Reschedule Selected", () -> {
            String id = tp.selectedId(); if (id == null) return;
            Booking b = store.findBooking(id);
            if (!b.isActive()) throw new IllegalStateException("Cancelled bookings cannot be rescheduled.");
            Court c = store.findCourt(b.getCourtId());
            LocalDate d = parseDate(dateF.getText());
            int start = (Integer) startBox.getSelectedItem(), hrs = (Integer) hoursSp.getValue();
            Payment p = store.paymentFor(id);
            boolean paid = p != null && Payment.CONFIRMED.equals(p.getStatus());
            if (paid && hrs != b.getHours()) throw new IllegalStateException("A paid booking can only move to a new time, not change its length.");
            if (d.isBefore(LocalDate.now())) throw new IllegalArgumentException("That date has already passed.");
            if (store.hasConflict(b.getCourtId(), d, start, hrs, id)) throw new IllegalStateException("That slot is already booked.");
            b.setSchedule(d, start, hrs);
            if (!paid && c != null) {
                b.computeTotal(c.getHourlyRate(), store.activeMembership(user.getPersonId()));
                if (p != null) p.setAmount(b.getTotal());
            }
            store.saveBookings(); store.savePayments(); refreshAll();
            info("Booking " + id + " moved to " + d + ", " + b.timeText() + ".");
        });

        JPanel form = row(new JLabel("Court:"), courtBox, new JLabel("Date:"), dateF,
                new JLabel("Start:"), startBox, new JLabel("Hours:"), hoursSp, book);
        JPanel south = new JPanel(new GridLayout(0, 1));
        south.add(form);
        south.add(row(resched, cancel, new JLabel("(Reschedule uses the date, start and hours above)")));
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    // ---------- MEMBERSHIPS (memberships.txt) ----------
    private JPanel membershipTab() {
        JPanel root = tabRoot();
        if (isStaff) {
            TablePanel tp = new TablePanel("ID", "Player", "Plan", "Fee (PHP)", "Discount", "Start", "Expires", "Active");
            refreshers.add(() -> {
                List<Object[]> rows = new ArrayList<>();
                for (Membership m : store.memberships)
                    rows.add(new Object[]{m.getMembershipId(), store.playerName(m.getPlayerId()), m.getTypeName(),
                            String.format("%.0f", m.getFee()), String.format("%.0f%%", m.getDiscountRate() * 100),
                            m.getStartDate(), m.getExpiryDate(), m.isActive() ? "Yes" : "Expired"});
                tp.setRows(rows);
            });
            root.add(tp, BorderLayout.CENTER);
            root.add(row(button("Cancel Membership", () -> {
                String id = tp.selectedId(); if (id == null) return;
                if (!confirm("Cancel membership " + id + "?")) return;
                store.memberships.removeIf(m -> m.getMembershipId().equals(id));
                store.saveMemberships(); refreshAll();
            })), BorderLayout.SOUTH);
            return root;
        }

        JLabel current = new JLabel();
        current.setFont(current.getFont().deriveFont(Font.BOLD, 15f));
        refreshers.add(() -> {
            Membership m = store.anyMembership(user.getPersonId());
            current.setText(m == null ? "No membership yet (regular rate)."
                    : "Current plan: " + m.getTypeName() + " | " + String.format("%.0f%%", m.getDiscountRate() * 100)
                      + " off | " + m.getStartDate() + " to " + m.getExpiryDate() + (m.isActive() ? "" : " (EXPIRED)"));
        });

        JComboBox<Membership> plans = new JComboBox<>();
        for (String t : Membership.TYPES) plans.addItem(Membership.create(t, "", "", LocalDate.now()));

        JButton sign = button("Sign Up / Renew / Upgrade", () -> {
            Membership plan = (Membership) plans.getSelectedItem();
            Membership old = store.anyMembership(user.getPersonId());
            String verb = old == null ? "Sign up for" : (old.getTypeName().equals(plan.getTypeName()) ? "Renew" : "Switch to");
            if (!confirm(verb + " " + plan + "?")) return;
            store.memberships.removeIf(m -> m.getPlayerId().equals(user.getPersonId()));
            Membership m = Membership.create(plan.getTypeName(), store.nextMembershipId(), user.getPersonId(), LocalDate.now());
            store.memberships.add(m);
            Payment p = new Payment(store.nextPaymentId(), user.getPersonId(), m.getMembershipId(), m.getFee(), Payment.PENDING, LocalDate.now());
            store.payments.add(p);
            store.saveMemberships(); store.savePayments(); refreshAll();
            showReceipt(p.receipt(user.getFullName()) + "\nPay at the counter to activate your discount record.");
        });
        JButton cancel = button("Cancel Membership", () -> {
            if (store.anyMembership(user.getPersonId()) == null) throw new IllegalStateException("You have no membership.");
            if (!confirm("Cancel your membership?")) return;
            store.memberships.removeIf(m -> m.getPlayerId().equals(user.getPersonId()));
            store.saveMemberships(); refreshAll();
        });

        JPanel box = new JPanel(new GridLayout(0, 1, 6, 10));
        box.add(current);
        box.add(row(new JLabel("Choose plan:"), plans, sign, cancel));
        box.add(new JLabel("Plans: Day Pass = 1 day, Monthly = 30 days, Premium = 30 days with the biggest discount."));
        root.add(box, BorderLayout.NORTH);
        return root;
    }

    // ---------- PAYMENTS (payments.txt) ----------
    private JPanel paymentsTab() {
        JPanel root = tabRoot();
        TablePanel tp = new TablePanel("Payment", "Player", "For", "Amount (PHP)", "Status", "Date");
        refreshers.add(() -> {
            List<Object[]> rows = new ArrayList<>();
            for (Payment p : store.payments) {
                if (!isStaff && !p.getPlayerId().equals(user.getPersonId())) continue;
                rows.add(new Object[]{p.getPaymentId(), store.playerName(p.getPlayerId()), p.getReference(),
                        String.format("%.2f", p.getAmount()), p.getStatus(), p.getDate()});
            }
            tp.setRows(rows);
        });
        root.add(tp, BorderLayout.CENTER);

        JButton receipt = button("Print Receipt", () -> {
            String id = tp.selectedId(); if (id == null) return;
            Payment p = store.findPayment(id);
            showReceipt(p.receipt(store.playerName(p.getPlayerId())));
        });
        if (!isStaff) { root.add(row(receipt), BorderLayout.SOUTH); return root; }

        JButton confirmBtn = button("Confirm Payment", () -> {
            String id = tp.selectedId(); if (id == null) return;
            Payment p = store.findPayment(id);
            if (!Payment.PENDING.equals(p.getStatus())) throw new IllegalStateException("Only pending payments can be confirmed.");
            p.setStatus(Payment.CONFIRMED); store.savePayments(); refreshAll();
        });
        JButton refund = button("Mark Refunded", () -> {
            String id = tp.selectedId(); if (id == null) return;
            Payment p = store.findPayment(id);
            if (!Payment.CONFIRMED.equals(p.getStatus())) throw new IllegalStateException("Only confirmed payments can be refunded.");
            p.setStatus(Payment.REFUNDED); store.savePayments(); refreshAll();
        });
        JButton voidBtn = button("Void Entry", () -> {
            String id = tp.selectedId(); if (id == null) return;
            if (!confirm("Void (delete) payment " + id + "?")) return;
            store.payments.remove(store.findPayment(id)); store.savePayments(); refreshAll();
        });
        JButton report = button("Daily Income Report", () -> {
            String s = JOptionPane.showInputDialog(this, "Report date (yyyy-MM-dd):", LocalDate.now().toString());
            if (s == null) return;
            LocalDate d = parseDate(s);
            double income = 0, refunded = 0; int n = 0, r = 0, pending = 0;
            for (Payment p : store.payments) {
                if (!p.getDate().equals(d)) continue;
                if (Payment.CONFIRMED.equals(p.getStatus())) { income += p.getAmount(); n++; }
                else if (Payment.REFUNDED.equals(p.getStatus())) { refunded += p.getAmount(); r++; }
                else pending++;
            }
            int booked = 0, hours = 0;
            for (Booking b : store.bookings) if (b.isActive() && b.getDate().equals(d)) { booked++; hours += b.getHours(); }
            showReceipt("===== DAILY REPORT: " + d + " =====\n"
                    + "Confirmed payments : " + n + "  (PHP " + String.format("%.2f", income) + ")\n"
                    + "Refunded payments  : " + r + "  (PHP " + String.format("%.2f", refunded) + ")\n"
                    + "Pending payments   : " + pending + "\n"
                    + "Bookings played    : " + booked + "  (" + hours + " court-hours)");
        });
        root.add(row(confirmBtn, refund, voidBtn, receipt, report), BorderLayout.SOUTH);
        return root;
    }

    // ---------- PLAYERS (players.txt, staff view) ----------
    private JPanel playersTab() {
        JPanel root = tabRoot();
        TablePanel tp = new TablePanel("ID", "Name", "Contact", "Membership", "Active bookings");
        refreshers.add(() -> {
            List<Object[]> rows = new ArrayList<>();
            for (Player p : store.players) {
                Membership m = store.activeMembership(p.getPersonId());
                int n = 0;
                for (Booking b : store.bookings) if (b.isActive() && b.getPlayerId().equals(p.getPersonId())) n++;
                rows.add(new Object[]{p.getPersonId(), p.getFullName(), p.getContactNo(), m == null ? "None" : m.getTypeName(), n});
            }
            tp.setRows(rows);
        });
        root.add(tp, BorderLayout.CENTER);
        root.add(row(button("Delete Player Account", () -> {
            String id = tp.selectedId(); if (id == null) return;
            if (!confirm("Delete player " + id + "? Their active bookings will be cancelled.")) return;
            store.deletePlayer(id); refreshAll();
        })), BorderLayout.SOUTH);
        return root;
    }

    // ---------- PROFILE (players.txt / staff.txt) ----------
    private JPanel profileTab() {
        JPanel root = tabRoot();
        JTextField name = new JTextField(user.getFullName(), 20);
        JTextField contact = new JTextField(user.getContactNo(), 20);
        JPasswordField pw = new JPasswordField(user.getPassword(), 20);
        name.setEditable(false);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.add(new JLabel("User ID:")); form.add(new JLabel(user.getPersonId()));
        form.add(new JLabel("Role:")); form.add(new JLabel(user.getRole()));
        form.add(new JLabel("Full name:")); form.add(name);
        form.add(new JLabel("Contact no.:")); form.add(contact);
        form.add(new JLabel("Password:")); form.add(pw);

        JPanel buttons = row(button("Save Changes", () -> {
            user.setContactNo(contact.getText());
            user.setPassword(new String(pw.getPassword()));
            if (isStaff) store.saveStaff(); else store.savePlayers();
            info("Profile updated.");
        }));
        if (!isStaff) buttons.add(button("Delete My Account", () -> {
            if (!confirm("Permanently delete your account? Your active bookings will be cancelled.")) return;
            store.deletePlayer(user.getPersonId());
            dispose();
            new LoginFrame(store).setVisible(true);
        }));

        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.add(form, BorderLayout.NORTH);
        wrap.add(buttons, BorderLayout.CENTER);
        root.add(wrap, BorderLayout.NORTH);
        return root;
    }
}
