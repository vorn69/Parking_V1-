package ui;

import dao.UserDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import models.User;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnSignup;
    private UserDAO userDAO;
    private JPanel mainPanel;

    public LoginFrame() {
        setTitle("Login - Parking System");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Create the main content panel with vertical layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Header
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createVerticalStrut(40));

        // Form
        contentPanel.add(createFormPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        // OR Divider
        contentPanel.add(createDividerPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        // Sign Up Button
        contentPanel.add(createSignupPanel());

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Add Enter key listener
        txtPassword.addActionListener(e -> login());
        
        // Set focus to username field
        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App Name
        JLabel appName = new JLabel("Parking Management");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appName.setForeground(new Color(0, 0, 0));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Welcome Text
        JLabel welcomeLabel = new JLabel("Welcome back");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(100, 100, 100));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        panel.add(appName);
        panel.add(welcomeLabel);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username Field
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUsername.setForeground(new Color(0, 0, 0));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblUsername);
        panel.add(Box.createVerticalStrut(8));

        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(300, 45));
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtUsername.setBackground(Color.WHITE);
        txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(txtUsername);
        panel.add(Box.createVerticalStrut(20));

        // Password Field
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPassword.setForeground(new Color(0, 0, 0));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(8));

        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(300, 45));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(30));

        // Login Button
        btnLogin = new JButton("Log in");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setPreferredSize(new Dimension(300, 45));
        btnLogin.setMaximumSize(new Dimension(300, 45));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(0, 100, 200));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setBorderPainted(false);
        
        // Simple hover effect
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 90, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 100, 200));
            }
        });

        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin);

        return panel;
    }

    private JPanel createDividerPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(300, 20));
        
        // Left line
        JSeparator leftLine = new JSeparator();
        leftLine.setPreferredSize(new Dimension(100, 1));
        leftLine.setForeground(new Color(200, 200, 200));
        
        // "or" text
        JLabel orLabel = new JLabel("or");
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orLabel.setForeground(new Color(100, 100, 100));
        
        // Right line
        JSeparator rightLine = new JSeparator();
        rightLine.setPreferredSize(new Dimension(100, 1));
        rightLine.setForeground(new Color(200, 200, 200));
        
        panel.add(leftLine);
        panel.add(orLabel);
        panel.add(rightLine);
        
        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setBackground(Color.WHITE);
        
        // Sign Up Button
        btnSignup = new JButton("Sign up");
        btnSignup.setPreferredSize(new Dimension(300, 45));
        btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSignup.setForeground(new Color(0, 100, 200));
        btnSignup.setBackground(Color.WHITE);
        btnSignup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btnSignup.setFocusPainted(false);
        btnSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Simple hover effect
        btnSignup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSignup.setBackground(new Color(240, 240, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSignup.setBackground(Color.WHITE);
            }
        });
        
        btnSignup.addActionListener(e -> openSignup());
        panel.add(btnSignup);
        
        return panel;
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validation
        if (username.isEmpty()) {
            showError("Please enter username");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter password");
            txtPassword.requestFocus();
            return;
        }

        // Show loading state
        btnLogin.setText("Logging in...");
        btnLogin.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Perform login in background thread
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected User doInBackground() throws Exception {
                try {
                    return userDAO.login(username, password);
                } catch (SQLException ex) {
                    errorMessage = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnLogin.setText("Log in");
                btnLogin.setEnabled(true);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                try {
                    User user = get();

                    if (user == null) {
                        if (errorMessage != null) {
                            JOptionPane.showMessageDialog(LoginFrame.this, 
                                "Database error: " + errorMessage, 
                                "Login Error", 
                                JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this, 
                                "Invalid username or password.", 
                                "Login Failed", 
                                JOptionPane.WARNING_MESSAGE);
                        }
                        return;
                    }

                    // Login successful
                    openDashboard(user);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        "An error occurred: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Error", 
            JOptionPane.WARNING_MESSAGE);
    }

    private void openDashboard(User user) {
        try {
            int groupId = user.getUserGroupId() != null ? user.getUserGroupId() : 0;

            SwingUtilities.invokeLater(() -> {
                if (groupId == 1) {
                    new AdminDashboard().setVisible(true);
                } else {
                    try {
                        int ownerId = userDAO.findOwnerIdByUserId(user.getUserId());
                        new CustomerDashboard(user.getUserId(), ownerId).setVisible(true);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Error loading dashboard: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                dispose(); // Close login window
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load dashboard: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSignup() {
        // TODO: Implement signup functionality
        JOptionPane.showMessageDialog(this,
            "Sign up functionality will be implemented here.",
            "Sign Up",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}