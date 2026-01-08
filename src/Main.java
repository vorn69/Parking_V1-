import javax.swing.SwingUtilities;
import models.User;
import ui.AdminDashboard;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Dummy user for testing
            User dummy = new User();
            new AdminDashboard().setVisible(true);
        });
    }
}
