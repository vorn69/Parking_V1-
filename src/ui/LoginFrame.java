package ui;

import dao.UserDAO;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private UserDAO userDAO;

    public LoginFrame() {
        setTitle("Login");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        userDAO = new UserDAO();

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        panel.add(new JLabel());
        btnLogin = new JButton("Login");
        panel.add(btnLogin);

        add(panel);

        btnLogin.addActionListener(e -> login());
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try {
            boolean authenticated = userDAO.authenticate(username, password);
            if (authenticated) {
                // Open Admin Dashboard
                SwingUtilities.invokeLater(() -> {
                    AdminDashboard dashboard = new AdminDashboard();
                    dashboard.setVisible(true);
                });
                this.dispose(); // close login window
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
