package ui;

import dao.VehicleOwnerDAO;
import models.VehicleOwner;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.sql.SQLException;

public class VehicleOwnerRegistrationPanel extends JPanel {
    
    private Integer userId;
    private CustomerDashboard dashboard;
    
    public VehicleOwnerRegistrationPanel(Integer userId, CustomerDashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;
        
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        add(createHeader(), BorderLayout.NORTH);
        add(createRegistrationForm(), BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel title = new JLabel("📝 Register as Vehicle Owner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(52, 73, 94));
        
        JLabel subtitle = new JLabel("Complete this form to register vehicles and book parking");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(127, 140, 141));
        
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createRegistrationForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel formTitle = new JLabel("Vehicle Owner Information", SwingConstants.CENTER);
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        formTitle.setForeground(new Color(52, 152, 219));
        formPanel.add(formTitle, gbc);
        
        gbc.gridwidth = 1;
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel nameLabel = new JLabel("Full Name *:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        JTextField nameField = new JTextField(25);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(nameField, gbc);
        
        // Contact Number
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel contactLabel = new JLabel("Contact Number *:");
        contactLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(contactLabel, gbc);
        
        gbc.gridx = 1;
        JTextField contactField = new JTextField(25);
        contactField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contactField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(contactField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel emailLabel = new JLabel("Email Address *:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        JTextField emailField = new JTextField(25);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(emailField, gbc);
        
        // User ID (read-only)
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel userIdLabelText = new JLabel("Your User ID:");
        userIdLabelText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(userIdLabelText, gbc);
        
        gbc.gridx = 1;
        JLabel userIdLabel = new JLabel(String.valueOf(userId));
        userIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userIdLabel.setForeground(new Color(52, 152, 219));
        userIdLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        userIdLabel.setBackground(new Color(245, 245, 245));
        userIdLabel.setOpaque(true);
        formPanel.add(userIdLabel, gbc);
        
        // Terms
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JCheckBox termsCheck = new JCheckBox("I agree to the terms and conditions");
        termsCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        termsCheck.setBackground(Color.WHITE);
        formPanel.add(termsCheck, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton registerBtn = new JButton("✅ Complete Registration");
        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(39, 174, 96), 1),
            new EmptyBorder(12, 25, 12, 25)
        ));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerOwner(nameField, contactField, emailField, termsCheck);
            }
        });
        
        JButton cancelBtn = new JButton("⬅️ Back to Dashboard");
        cancelBtn.setBackground(new Color(52, 152, 219));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
            new EmptyBorder(12, 25, 12, 25)
        ));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if dashboard has showDashboardView method
                if (dashboard != null) {
                    dashboard.showDashboardView();
                }
            }
        });
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        formPanel.add(buttonPanel, gbc);
        
        return formPanel;
    }
    
    private void registerOwner(JTextField nameField, JTextField contactField, 
                              JTextField emailField, JCheckBox termsCheck) {
        
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        
        // Validation
        if (name.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all required fields!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!termsCheck.isSelected()) {
            JOptionPane.showMessageDialog(this,
                "Please agree to the terms and conditions!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Phone validation
        if (!contact.matches("\\+?[0-9\\s-]{10,15}")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid contact number!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Check if already registered
            VehicleOwnerDAO dao = new VehicleOwnerDAO();
            VehicleOwner existing = dao.findByUserId(userId);
            if (existing != null) {
                JOptionPane.showMessageDialog(this,
                    "You already have a vehicle owner account!\n" +
                    "Owner ID: " + existing.getVehicleOwnerId(),
                    "Already Registered", JOptionPane.INFORMATION_MESSAGE);
                if (dashboard != null) {
                    dashboard.onRegistrationSuccess(existing.getVehicleOwnerId());
                }
                return;
            }
            
            // Create new vehicle owner
            VehicleOwner newOwner = new VehicleOwner();
            newOwner.setVehicleOwnerName(name);
            newOwner.setVehicleOwnerContact(contact);
            newOwner.setVehicleOwnerEmail(email);
            newOwner.setStatus(1); // Active
            newOwner.setUserId(userId);
            
            // Save to database
            Integer ownerId = dao.create(newOwner);
            
            if (ownerId != null && ownerId > 0) {
                // Show success
                JOptionPane.showMessageDialog(this,
                    "✅ Registration successful!\n\n" +
                    "Your vehicle owner ID: " + ownerId + "\n" +
                    "You can now:\n" +
                    "• Register vehicles\n" +
                    "• Book parking slots\n" +
                    "• Make payments",
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                
                // Notify dashboard
                if (dashboard != null) {
                    dashboard.onRegistrationSuccess(ownerId);
                }
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