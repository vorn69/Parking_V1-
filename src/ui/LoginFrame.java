// package ui;

// import dao.UserDAO;
// import models.User;

// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.*;
// import java.sql.SQLException;

// public class LoginFrame extends JFrame {

//     private JTextField txtUsername;
//     private JPasswordField txtPassword;
//     private JButton btnLogin;

//     private UserDAO userDAO;

//     public LoginFrame() {
//         userDAO = new UserDAO();

//         setTitle("Login");
//         setSize(350, 200);
//         setLocationRelativeTo(null);
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//         initUI();
//     }

//     private void initUI() {
//         JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
//         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

//         JLabel lblUsername = new JLabel("Username:");
//         JLabel lblPassword = new JLabel("Password:");

//         txtUsername = new JTextField();
//         txtPassword = new JPasswordField();

//         btnLogin = new JButton("Login");

//         panel.add(lblUsername);
//         panel.add(txtUsername);
//         panel.add(lblPassword);
//         panel.add(txtPassword);
//         panel.add(new JLabel()); // empty cell
//         panel.add(btnLogin);

//         add(panel);

//         // Login action
//         btnLogin.addActionListener(e -> login());
//         txtPassword.addActionListener(e -> login()); // press Enter to login
//     }

//     private void login() {
//         String username = txtUsername.getText().trim();
//         String password = new String(txtPassword.getPassword());

//         if (username.isEmpty() || password.isEmpty()) {
//             JOptionPane.showMessageDialog(this, "Please enter username and password.", "Error", JOptionPane.ERROR_MESSAGE);
//             return;
//         }

//         try {
//             User user = userDAO.checkLogin(username, password);

//             if (user != null) {
//                 JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + user.getFullname(), "Success", JOptionPane.INFORMATION_MESSAGE);
//                 // Open dashboard
//                 AdminDashboard dashboard = new AdminDashboard();
//                 dashboard.setVisible(true);
//                 this.dispose();
//             } else {
//                 JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
//             }

//         } catch (SQLException ex) {
//             JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//             ex.printStackTrace();
//         }
//     }

//     public static void main(String[] args) {
//         SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
//     }
// }
// src/ui/LoginFrame.java
package ui;

import dao.UserDAO;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Parking Management - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(panel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel userLabel = new JLabel("Username:");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(userLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> handleLogin());

        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            boolean success = userDAO.authenticate(username, password);

            if (success) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new AdminDashboard(); // Open dashboard
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
