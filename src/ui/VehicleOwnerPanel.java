package ui;

import dao.VehicleOwnerDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.VehicleOwner;

public class VehicleOwnerPanel extends JPanel {

    private VehicleOwnerDAO ownerDAO = new VehicleOwnerDAO();
    private JTable table;
    private DefaultTableModel model;
    private List<VehicleOwner> ownerList;

    public VehicleOwnerPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadOwners();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("VEHICLE OWNER MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton addBtn = createButton("➕ ADD OWNER", new Color(76, 175, 80));
        JButton refreshBtn = createButton("🔄 REFRESH", new Color(33, 150, 243));

        addBtn.addActionListener(e -> addOwner());
        refreshBtn.addActionListener(e -> loadOwners());

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(
            new String[]{"Owner ID", "Name", "Contact", "Email", "User ID", "Status"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        return new JScrollPane(table);
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel countLabel = new JLabel();
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(countLabel);
        
        SwingUtilities.invokeLater(() -> updateFooter(countLabel));
        
        return panel;
    }

    private void loadOwners() {
        try {
            ownerList = ownerDAO.findAll();
            model.setRowCount(0);

            for (VehicleOwner owner : ownerList) {
                model.addRow(new Object[]{
                    owner.getVehicleOwnerId(),
                    owner.getVehicleOwnerName(),
                    owner.getVehicleOwnerContact(),
                    owner.getVehicleOwnerEmail(),
                    owner.getUserId(),
                    getStatusText(owner.getStatus())
                });
            }

            updateFooter();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load vehicle owners: " + e.getMessage());
        }
    }

    private String getStatusText(Integer status) {
        if (status == null) return "UNKNOWN";
        return status == 1 ? "✅ ACTIVE" : "❌ INACTIVE";
    }

    private void updateFooter() {
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel footer = (JPanel) comp;
                for (Component c : footer.getComponents()) {
                    if (c instanceof JLabel) {
                        updateFooter((JLabel) c);
                        break;
                    }
                }
            }
        }
    }

    private void updateFooter(JLabel label) {
        if (ownerList == null) return;
        label.setText("Total Vehicle Owners: " + ownerList.size());
    }

    private void addOwner() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Vehicle Owner", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField userIdField = new JTextField();
        
        formPanel.add(new JLabel("Owner Name*:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact*:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("Email*:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("User ID:"));
        formPanel.add(userIdField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn = new JButton("Save Owner");
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            saveOwner(nameField, contactField, emailField, userIdField, dialog);
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        dialog.add(new JLabel("   Add New Vehicle Owner", SwingConstants.CENTER), BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveOwner(JTextField nameField, JTextField contactField,
                          JTextField emailField, JTextField userIdField, JDialog dialog) {
        try {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();
            
            if (name.isEmpty() || contact.isEmpty() || email.isEmpty()) {
                showWarning("Validation Error", "Please fill all required fields");
                return;
            }
            
            VehicleOwner owner = new VehicleOwner();
            owner.setVehicleOwnerName(name);
            owner.setVehicleOwnerContact(contact);
            owner.setVehicleOwnerEmail(email);
            owner.setStatus(1); // Active
            
            if (!userIdField.getText().trim().isEmpty()) {
                owner.setUserId(Integer.parseInt(userIdField.getText().trim()));
            }
            
            ownerDAO.create(owner);
            
            showSuccess("Success", "Vehicle owner added successfully!");
            dialog.dispose();
            loadOwners();
            
        } catch (NumberFormatException e) {
            showError("Invalid Input", "User ID must be a number");
        } catch (SQLException e) {
            showError("Database Error", "Failed to save owner: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}