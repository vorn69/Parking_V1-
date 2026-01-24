// src/ui/UserRegistrationFrame.java
package ui;

import dao.UserDAO;
import dao.VehicleOwnerDAO;
import models.User;
import models.VehicleOwner;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;

public class UserRegistrationFrame extends JFrame {
    
    private UserDAO userDAO;
    private VehicleOwnerDAO ownerDAO;
    
    public UserRegistrationFrame() {
        userDAO = new UserDAO();
        ownerDAO = new VehicleOwnerDAO();
        
        initUI();
    }
    
    private void initUI() {
        setTitle("User Registration - Parking System");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);
        add(createRegistrationForm(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(52, 152, 219));
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel title = new JLabel("👤 Create New User Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title);
        
        return header;
    }
    
    private JPanel createRegistrationForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // User Type Selection
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel typeLabel = new JLabel("Select User Type:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(typeLabel, gbc);
        
        gbc.gridy = 1;
        JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{
            "Customer (Can book parking)",
            "Vehicle Owner (Can register vehicles)",
            "Staff (Limited access)",
            "Admin (Full access)"
        });
        userTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(userTypeCombo, gbc);
        
        // Personal Information Section
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel personalLabel = new JLabel("Personal Information");
        personalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        personalLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(personalLabel, gbc);
        
        // Full Name
        gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Full Name *:"), gbc);
        
        gbc.gridx = 1;
        JTextField fullNameField = new JTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Username *:"), gbc);
        
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Email *:"), gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Contact
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Contact Number:"), gbc);
        
        gbc.gridx = 1;
        JTextField contactField = new JTextField(20);
        formPanel.add(contactField, gbc);
        
        // Password Section
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        JLabel passwordLabel = new JLabel("Account Security");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        passwordLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(passwordLabel, gbc);
        
        // Password
        gbc.gridy = 8; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Password *:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("Confirm Password *:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        
        // Vehicle Owner Specific (if selected)
        gbc.gridx = 0; gbc.gridy = 10;
        gbc.gridwidth = 2;
        JPanel vehicleOwnerPanel = createVehicleOwnerPanel();
        formPanel.add(vehicleOwnerPanel, gbc);
        
        // Register Button
        gbc.gridx = 0; gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton registerBtn = new JButton("📝 Register User");
        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setPreferredSize(new Dimension(200, 45));
        
        registerBtn.addActionListener(e -> {
            registerUser(
                userTypeCombo,
                fullNameField,
                usernameField,
                emailField,
                contactField,
                passwordField,
                confirmPasswordField
            );
        });
        
        formPanel.add(registerBtn, gbc);
        
        return formPanel;
    }
    
    private JPanel createVehicleOwnerPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            "Vehicle Owner Information (Optional)"));
        
        panel.add(new JLabel("Owner Contact:"));
        JTextField ownerContactField = new JTextField();
        panel.add(ownerContactField);
        
        panel.add(new JLabel("Owner Email:"));
        JTextField ownerEmailField = new JTextField();
        panel.add(ownerEmailField);
        
        panel.add(new JLabel("Owner Address:"));
        JTextField ownerAddressField = new JTextField();
        panel.add(ownerAddressField);
        
        return panel;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new EmptyBorder(15, 10, 15, 10));
        
        JLabel loginLabel = new JLabel("Already have an account? ");
        JButton loginBtn = new JButton("Login here");
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setForeground(new Color(52, 152, 219));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        loginBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        
        footer.add(loginLabel);
        footer.add(loginBtn);
        
        return footer;
    }
    
    private void registerUser(JComboBox<String> userTypeCombo,
                             JTextField fullNameField,
                             JTextField usernameField,
                             JTextField emailField,
                             JTextField contactField,
                             JPasswordField passwordField,
                             JPasswordField confirmPasswordField) {
        
        // Validation
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String userType = (String) userTypeCombo.getSelectedItem();
        
        // Basic validation
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill all required fields!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "Passwords do not match!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "Password must be at least 6 characters!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Check if username exists
            if (userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(this, 
                    "Username already taken!", 
                    "Registration Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check if email exists
            if (userDAO.emailExists(email)) {
                JOptionPane.showMessageDialog(this, 
                    "Email already registered!", 
                    "Registration Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Determine user group ID based on selection
            int userGroupId = 2; // Default to Customer
            
            if (userType.contains("Vehicle Owner")) {
                userGroupId = 3; // Assuming 3 = Vehicle Owner
            } else if (userType.contains("Staff")) {
                userGroupId = 4; // Assuming 4 = Staff
            } else if (userType.contains("Admin")) {
                userGroupId = 1; // 1 = Admin
            }
            
            // Create user
            User newUser = new User();
            newUser.setFullname(fullName);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setContact(contact);
            newUser.setPassword(password); // Note: Should be hashed in production
            newUser.setUserGroupId(userGroupId);
            newUser.setStatus(1); // Active
            
            // Save user to database
            Integer userId = userDAO.create(newUser);
            
            if (userId != null) {
                // If user is Vehicle Owner, create vehicle owner record
                if (userGroupId == 3) {
                    VehicleOwner owner = new VehicleOwner();
                    owner.setVehicleOwnerName(fullName);
                    owner.setVehicleOwnerContact(contact);
                    owner.setVehicleOwnerEmail(email);
                    owner.setUserId(userId);
                    owner.setStatus(1);
                    
                    ownerDAO.create(owner);
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Registration successful!\nUser ID: " + userId + 
                    "\nUser Type: " + userType, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
                new LoginFrame().setVisible(true);
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Registration failed. Please try again.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}