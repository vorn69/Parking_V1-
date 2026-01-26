package ui;

import dao.*;
import models.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Arrays;

public class UserProfilePanel extends JPanel {

    private Integer userId;
    private UserDAO userDAO;
    private User currentUser;

    // UI Components
    private JTextField usernameField;
    private JTextField fullnameField;
    private JTextField emailField;
    private JTextField contactField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel avatarLabel;

    // Colors
    private final Color BG_COLOR = new Color(240, 242, 245);
    private final Color PRIMARY = new Color(52, 152, 219);
    private final Color DANGER = new Color(231, 76, 60);
    private final Color SUCCESS = new Color(46, 204, 113);

    public UserProfilePanel(Integer userId) {
        this.userId = userId;
        this.userDAO = new UserDAO();

        initUI();
        loadUserData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content - Scrollable
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 20, 0);

        // Profile Info Section
        contentPanel.add(createProfileSection(), gbc);

        gbc.gridy++;
        // Password Change Section
        contentPanel.add(createPasswordSection(), gbc);

        // Push everything to top
        gbc.gridy++;
        gbc.weighty = 1.0;
        contentPanel.add(Box.createVerticalGlue(), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Profile Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));

        JLabel subtitle = new JLabel("Manage your personal information and security");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(127, 140, 141));

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        header.add(titlePanel, BorderLayout.WEST);

        return header;
    }

    private JPanel createProfileSection() {
        JPanel panel = createCard("Personal Information");
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Avatar (Placeholder)
        avatarLabel = new JLabel("👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        avatarLabel.setPreferredSize(new Dimension(100, 100));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(avatarLabel, gbc);

        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;

        // Fields
        usernameField = createReadOnlyField("Username");
        fullnameField = createTextField("Full Name");
        emailField = createTextField("Email Address");
        contactField = createTextField("Phone Number");

        addFieldToGrid(panel, "Username", usernameField, gbc, 0);
        addFieldToGrid(panel, "Full Name", fullnameField, gbc, 1);
        addFieldToGrid(panel, "Email", emailField, gbc, 2);
        addFieldToGrid(panel, "Phone", contactField, gbc, 3);

        return panel;
    }

    private JPanel createPasswordSection() {
        JPanel panel = createCard("Security");
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        currentPasswordField = createPasswordField();
        newPasswordField = createPasswordField();
        confirmPasswordField = createPasswordField();

        addFieldToGrid(panel, "Current Password", currentPasswordField, gbc, 0);
        addFieldToGrid(panel, "New Password", newPasswordField, gbc, 1);
        addFieldToGrid(panel, "Confirm New Password", confirmPasswordField, gbc, 2);

        return panel;
    }

    private void addFieldToGrid(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 1;
        gbc.gridy = row * 2;
        gbc.insets = new Insets(5, 15, 0, 15);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(100, 100, 100));
        panel.add(label, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(0, 15, 15, 15);
        panel.add(field, gbc);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton saveBtn = createStyledButton("Save Changes", SUCCESS);
        saveBtn.setPreferredSize(new Dimension(150, 40));
        saveBtn.addActionListener(e -> saveChanges());

        footer.add(saveBtn);
        return footer;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(new EmptyBorder(0, 5, 15, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JTextField createReadOnlyField(String placeholder) {
        JTextField field = createTextField(placeholder);
        field.setEditable(false);
        field.setBackground(new Color(245, 245, 245));
        field.setForeground(Color.GRAY);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadUserData() {
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.findById(userId);
            }

            @Override
            protected void done() {
                try {
                    currentUser = get();
                    if (currentUser != null) {
                        usernameField.setText(currentUser.getUsername());
                        fullnameField.setText(currentUser.getFullname());
                        emailField.setText(currentUser.getEmail());
                        contactField.setText(currentUser.getContact());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void saveChanges() {
        if (currentUser == null)
            return;

        // 1. Update Profile Info
        currentUser.setFullname(fullnameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setContact(contactField.getText().trim());

        // Basic validation
        if (currentUser.getFullname().isEmpty() || currentUser.getEmail().isEmpty()) {
            showError("Missing Information", "Full Name and Email are required.");
            return;
        }

        // 2. Check Password Change
        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        boolean passwordChanged = false;

        if (!newPass.isEmpty()) {
            if (currentPass.isEmpty()) {
                showError("Security Check", "Please enter current password to set a new one.");
                return;
            }
            if (!currentPass.equals(currentUser.getPassword())) {
                showError("Security Check", "Current password is incorrect.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                showError("Password Mismatch", "New password and confirmation do not match.");
                return;
            }
            // Update password
            currentUser.setPassword(newPass);
            passwordChanged = true;
        }

        // 3. Save to Database
        try {
            boolean profileUpdated = userDAO.update(currentUser);
            if (passwordChanged) {
                userDAO.changePassword(userId, newPass);
            }

            if (profileUpdated) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                // Clear password fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
            } else {
                showError("Update Failed", "Could not update profile. Please try again.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Error saving changes: " + e.getMessage());
        }
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }
}
