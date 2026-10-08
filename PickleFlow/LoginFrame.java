import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private final Store store;
    private final JTextField idField = new JTextField(16);
    private final JPasswordField pwField = new JPasswordField(16);

    public LoginFrame(Store store) {
        super("PickleFlow - Login");
        this.store = store;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("PickleFlow", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel sub = new JLabel("Pickleball Court Booking and Membership System", SwingConstants.CENTER);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; root.add(title, g);
        g.gridy = 1; root.add(sub, g);

        g.gridwidth = 1; g.gridy = 2; root.add(new JLabel("User ID:"), g);
        g.gridx = 1; root.add(idField, g);
        g.gridx = 0; g.gridy = 3; root.add(new JLabel("Password:"), g);
        g.gridx = 1; root.add(pwField, g);

        JButton login = new JButton("Login");
        JButton register = new JButton("Register as Player");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.add(login); buttons.add(register);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; root.add(buttons, g);

        JLabel hint = new JLabel("Default staff login:  S001 / admin123", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        g.gridy = 5; root.add(hint, g);

        login.addActionListener(e -> doLogin());
        pwField.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());

        setContentPane(root);
        getRootPane().setDefaultButton(login);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        Person p = store.authenticate(idField.getText(), new String(pwField.getPassword()));
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Invalid ID or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new DashboardFrame(store, p).setVisible(true);
        dispose();
    }

    private void doRegister() {
        JTextField name = new JTextField(), contact = new JTextField();
        JPasswordField pw = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Full name:")); form.add(name);
        form.add(new JLabel("Contact no.:")); form.add(contact);
        form.add(new JLabel("Password (min 4):")); form.add(pw);
        if (JOptionPane.showConfirmDialog(this, form, "Register", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            Player p = new Player(store.nextPlayerId(), name.getText(), contact.getText(), new String(pw.getPassword()));
            store.players.add(p);
            store.savePlayers();
            idField.setText(p.getPersonId());
            JOptionPane.showMessageDialog(this, "Account created. Your User ID is " + p.getPersonId() + ".");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot register", JOptionPane.ERROR_MESSAGE);
        }
    }
}
