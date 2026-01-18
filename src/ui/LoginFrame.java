package ui;

import dao.UserDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import models.User;

public class LoginFrame extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JCheckBox chkRemember;
    private JButton btnLogin;
    private UserDAO userDAO;
    private JPanel mainPanel;

    public LoginFrame() {
        setTitle("Login - Parking Management System");
        setSize(450, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add Enter key listener for login
        txtPassword.addActionListener(e -> login());
        
        // Add focus listeners for better UX
        addFocusListeners();
        
        // Set focus to email field
        SwingUtilities.invokeLater(() -> txtEmail.requestFocusInWindow());
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Title
        JLabel title = new JLabel("Enter Your Email & Password");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(33, 37, 41));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtitle
        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(108, 117, 125));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Icon (optional)
        JLabel iconLabel = new JLabel("🔐");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(title, BorderLayout.CENTER);
        panel.add(subtitle, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Email Field
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEmail.setForeground(new Color(73, 80, 87));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(5));

        txtEmail = new JTextField();
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtEmail.putClientProperty("JTextField.placeholderText", "example@gmail.com");
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(20));

        // Password Field
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPassword.setForeground(new Color(73, 80, 87));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(5));

        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtPassword.putClientProperty("JTextField.placeholderText", "Enter your password");
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(20));

        // Remember me and Forgot Password
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        chkRemember = new JCheckBox("Keep me logged in");
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkRemember.setBackground(Color.WHITE);
        chkRemember.setFocusPainted(false);
        chkRemember.setSelected(false);

        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnForgot.setForeground(new Color(0, 123, 255));
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.setFocusPainted(false);
        btnForgot.addActionListener(e -> showForgotPasswordDialog());

        optionsPanel.add(chkRemember, BorderLayout.WEST);
        optionsPanel.add(btnForgot, BorderLayout.EAST);
        panel.add(optionsPanel);
        panel.add(Box.createVerticalStrut(30));

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setPreferredSize(new Dimension(200, 45));
        btnLogin.setMaximumSize(new Dimension(400, 45));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(0, 123, 255));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 105, 217));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 123, 255));
            }
        });
        
        // Pressed effect
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 98, 204));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 123, 255));
            }
        });

        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Divider
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(233, 236, 239));
        panel.add(separator, BorderLayout.NORTH);

        // Footer text
        JLabel footerLabel = new JLabel("© 2024 Parking Management System");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(108, 117, 125));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        panel.add(footerLabel, BorderLayout.CENTER);

        return panel;
    }

    private void addFocusListeners() {
        // Email field focus
        txtEmail.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtEmail.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txtEmail.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });

        // Password field focus
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                txtPassword.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                    BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                txtPassword.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "Forgot Password", true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        JLabel message = new JLabel("<html><div style='text-align: center;'>"
                + "Enter your email address and we'll send you a password reset link."
                + "</div></html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        message.setHorizontalAlignment(SwingConstants.CENTER);
        
        JTextField emailField = new JTextField();
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JButton sendButton = new JButton("Send Reset Link");
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> {
            // TODO: Implement password reset functionality
            JOptionPane.showMessageDialog(dialog, 
                "Password reset link would be sent to: " + emailField.getText(),
                "Reset Link Sent", 
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);
        
        panel.add(message, BorderLayout.NORTH);
        panel.add(emailField, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void login() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validation
        if (email.isEmpty()) {
            highlightField(txtEmail, "Please enter your email address");
            return;
        }

        if (password.isEmpty()) {
            highlightField(txtPassword, "Please enter your password");
            return;
        }

        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            highlightField(txtEmail, "Please enter a valid email address");
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
                    // Convert email to lowercase for consistency
                    String username = email.toLowerCase();
                    
                    // Try to authenticate user
                    return userDAO.login(username, password);
                } catch (SQLException ex) {
                    errorMessage = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnLogin.setText("Login");
                btnLogin.setEnabled(true);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                try {
                    User user = get();

                    if (user == null) {
                        if (errorMessage != null) {
                            showError("Login Error", "Database error: " + errorMessage);
                        } else {
                            showError("Login Failed", "Invalid email or password.");
                        }
                        return;
                    }

                    // Login successful - open appropriate dashboard
                    openDashboard(user);

                } catch (Exception ex) {
                    showError("Unexpected Error", "An unexpected error occurred: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void highlightField(JComponent field, String message) {
        // Shake animation
        Timer timer = new Timer(20, null);
        final int[] x = {0};
        final int originalX = field.getX();
        
        timer.addActionListener(new ActionListener() {
            private int count = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < 10) {
                    int offset = (count % 2 == 0) ? 5 : -5;
                    field.setLocation(originalX + offset, field.getY());
                    count++;
                } else {
                    field.setLocation(originalX, field.getY());
                    timer.stop();
                }
            }
        });
        timer.start();

        // Show error message
        JOptionPane.showMessageDialog(this, message, "Validation Error", 
            JOptionPane.WARNING_MESSAGE);
        
        // Set error border
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
            BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        
        field.requestFocus();
    }

    private void openDashboard(User user) {
        try {
            int groupId = user.getUserGroupId() != null ? user.getUserGroupId() : 0;

            if (groupId == 1) {
                // Admin dashboard
                SwingUtilities.invokeLater(() -> {
                    new AdminDashboard().setVisible(true);
                    dispose();
                });
            } else {
                // Customer dashboard
                int ownerId = userDAO.findOwnerIdByUserId(user.getUserId());
                SwingUtilities.invokeLater(() -> {
                    new CustomerDashboard(user.getUserId(), ownerId).setVisible(true);
                    dispose();
                });
            }
        } catch (SQLException ex) {
            showError("Dashboard Error", "Failed to load dashboard: " + ex.getMessage());
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        // Set Look and Feel
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