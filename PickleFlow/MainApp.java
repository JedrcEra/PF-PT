import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginFrame(new Store()).setVisible(true));
    }
}
