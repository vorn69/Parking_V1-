package ui;

import dao.VehicleOwnerDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List; // ADD THIS IMPORT
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import models.VehicleOwner;

public class VehicleOwnerPanel extends JPanel {

    private VehicleOwnerDAO ownerDAO = new VehicleOwnerDAO();
    private JTable table;
    private DefaultTableModel model;
    private List<VehicleOwner> ownerList;

    // Colors for better UI
    private final Color PRIMARY = new Color(33, 150, 243);
    private final Color SUCCESS = new Color(76, 175, 80);
    private final Color WARNING = new Color(255, 193, 7);
    private final Color DANGER = new Color(244, 67, 54);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public VehicleOwnerPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadOwners();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("🚗 VEHICLE OWNER MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(52, 73, 94));
        header.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = createStyledButton("➕ ADD OWNER", SUCCESS, "Add new vehicle owner");
        JButton refreshBtn = createStyledButton("🔄 REFRESH", PRIMARY, "Refresh data");
        JButton exportBtn = createStyledButton("📊 EXPORT", new Color(155, 89, 182), "Export to CSV");

        addBtn.addActionListener(e -> addOwner());
        refreshBtn.addActionListener(e -> loadOwners());
        exportBtn.addActionListener(e -> exportOwners());

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(exportBtn);

        header.add(btnPanel, BorderLayout.EAST);
        return header;
    }

    private JButton createStyledButton(String text, Color bgColor, String tooltip) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bgColor.darker(), 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(0, 0, 0, 0)
        ));

        model = new DefaultTableModel(
            new String[]{"Owner ID", "Name", "Contact", "Email", "User ID", "Status", "Actions"},
            0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { 
                return c == 6; // Only actions column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 6 ? JPanel.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Contact
        table.getColumnModel().getColumn(3).setPreferredWidth(180);  // Email
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // User ID
        table.getColumnModel().getColumn(5).setPreferredWidth(100);  // Status
        table.getColumnModel().getColumn(6).setPreferredWidth(150);  // Actions
        
        // Custom header
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(52, 73, 94));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        
        // Action column renderer and editor
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel countLabel = new JLabel();
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(countLabel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(new Color(189, 195, 199));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(timeLabel, BorderLayout.EAST);
        
        // Update footer with current data
        SwingUtilities.invokeLater(() -> {
            updateFooter(countLabel, timeLabel);
        });
        
        return panel;
    }

    private void loadOwners() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    ownerList = ownerDAO.findAll();
                    SwingUtilities.invokeLater(() -> {
                        updateTable();
                        updateFooter();
                    });
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Database Error", "Failed to load vehicle owners: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private void updateTable() {
        model.setRowCount(0);

        for (VehicleOwner owner : ownerList) {
            model.addRow(new Object[]{
                owner.getVehicleOwnerId(),
                owner.getVehicleOwnerName() != null ? owner.getVehicleOwnerName() : "N/A",
                owner.getVehicleOwnerContact(),
                owner.getVehicleOwnerEmail(),
                owner.getUserId() != null ? owner.getUserId() : "N/A",
                getStatusWithIcon(owner.getStatus()),
                createActionButtons(owner)
            });
        }
    }

    private String getStatusWithIcon(Integer status) {
        if (status == null) return "❓ UNKNOWN";
        return status == 1 ? "✅ ACTIVE" : "❌ INACTIVE";
    }

    private void updateFooter() {
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel footer = (JPanel) comp;
                for (Component c : footer.getComponents()) {
                    if (c instanceof JLabel) {
                        if (footer.getLayout() instanceof BorderLayout) {
                            JLabel countLabel = (JLabel) footer.getComponent(0);
                            JLabel timeLabel = (JLabel) footer.getComponent(1);
                            updateFooter(countLabel, timeLabel);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateFooter(JLabel countLabel, JLabel timeLabel) {
        if (ownerList == null) return;
        countLabel.setText("Total Vehicle Owners: " + ownerList.size() + " | Active: " + 
                          ownerList.stream().filter(o -> o.getStatus() != null && o.getStatus() == 1).count());
        
        timeLabel.setText("🕒 " + new java.text.SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a")
                         .format(new java.util.Date()));
    }

    private JPanel createActionButtons(VehicleOwner owner) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);
        
        // View button
        JButton viewBtn = createSmallButton("👁", "View Details", PRIMARY);
        viewBtn.addActionListener(e -> viewOwnerDetails(owner));
        
        // Edit button
        JButton editBtn = createSmallButton("✏️", "Edit Owner", WARNING);
        editBtn.addActionListener(e -> editOwner(owner));
        
        // Delete button
        JButton deleteBtn = createSmallButton("🗑️", "Delete Owner", DANGER);
        deleteBtn.addActionListener(e -> deleteOwner(owner));
        
        panel.add(viewBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);
        
        return panel;
    }

    private JButton createSmallButton(String text, String tooltip, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setToolTipText(tooltip);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(5, 8, 5, 8));
        btn.setFocusPainted(false);
        return btn;
    }

    private void addOwner() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "Add New Vehicle Owner", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Add New Vehicle Owner", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        
        // Form fields
        formPanel.add(new JLabel("Owner Name:"));
        JTextField nameField = new JTextField();
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Contact Number*:"));
        JTextField contactField = new JTextField();
        formPanel.add(contactField);
        
        formPanel.add(new JLabel("Email Address*:"));
        JTextField emailField = new JTextField();
        formPanel.add(emailField);
        
        formPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        formPanel.add(usernameField);
        
        formPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        formPanel.add(passwordField);
        
        formPanel.add(new JLabel("User ID:"));
        JTextField userIdField = new JTextField();
        formPanel.add(userIdField);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("💾 Save Owner");
        saveButton.setBackground(SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            saveOwner(nameField, contactField, emailField, usernameField, 
                     passwordField, userIdField, dialog);
        });
        
        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setBackground(DANGER);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        content.add(title, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void saveOwner(JTextField nameField, JTextField contactField,
                          JTextField emailField, JTextField usernameField,
                          JPasswordField passwordField, JTextField userIdField, 
                          JDialog dialog) {
        try {
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();
            
            // Validation
            if (contact.isEmpty()) {
                showWarning("Validation Error", "Contact number is required");
                return;
            }
            
            if (email.isEmpty()) {
                showWarning("Validation Error", "Email address is required");
                return;
            }
            
            // Check for duplicates
            if (ownerDAO.contactExists(contact)) {
                showWarning("Duplicate Contact", "This contact number already exists!");
                return;
            }
            
            if (ownerDAO.emailExists(email)) {
                showWarning("Duplicate Email", "This email address already exists!");
                return;
            }
            
            // Create owner object
            VehicleOwner owner = new VehicleOwner();
            owner.setVehicleOwnerName(nameField.getText().trim());
            owner.setVehicleOwnerContact(contact);
            owner.setVehicleOwnerEmail(email);
            owner.setOwnerUsername(usernameField.getText().trim());
            
            String password = new String(passwordField.getPassword());
            if (!password.isEmpty()) {
                owner.setOwnerPassword(password);
            }
            
            owner.setStatus(1); // Active by default
            
            if (!userIdField.getText().trim().isEmpty()) {
                try {
                    owner.setUserId(Integer.parseInt(userIdField.getText().trim()));
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "User ID must be a valid number");
                    return;
                }
            }
            
            // Save to database
            Integer newId = ownerDAO.create(owner);
            if (newId != null) {
                showSuccess("Success", "Vehicle owner added successfully! ID: " + newId);
                dialog.dispose();
                loadOwners();
            } else {
                showError("Database Error", "Failed to create vehicle owner");
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to save owner: " + e.getMessage());
        }
    }

    private void viewOwnerDetails(VehicleOwner owner) {
        StringBuilder details = new StringBuilder();
        details.append("<html><div style='width:400px;'>");
        details.append("<h3>Vehicle Owner #").append(owner.getVehicleOwnerId()).append("</h3>");
        details.append("<table border='0' cellpadding='5'>");
        
        if (owner.getVehicleOwnerName() != null) {
            details.append("<tr><td><b>Name:</b></td><td>").append(owner.getVehicleOwnerName()).append("</td></tr>");
        }
        
        details.append("<tr><td><b>Contact:</b></td><td>").append(owner.getVehicleOwnerContact()).append("</td></tr>");
        details.append("<tr><td><b>Email:</b></td><td>").append(owner.getVehicleOwnerEmail()).append("</td></tr>");
        
        if (owner.getOwnerUsername() != null) {
            details.append("<tr><td><b>Username:</b></td><td>").append(owner.getOwnerUsername()).append("</td></tr>");
        }
        
        details.append("<tr><td><b>Status:</b></td><td>").append(getStatusWithIcon(owner.getStatus())).append("</td></tr>");
        
        if (owner.getUserId() != null) {
            details.append("<tr><td><b>User ID:</b></td><td>").append(owner.getUserId()).append("</td></tr>");
        }
        
        if (owner.getCreatedAt() != null) {
            details.append("<tr><td><b>Created:</b></td><td>")
                  .append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(owner.getCreatedAt()))
                  .append("</td></tr>");
        }
        
        details.append("</table></div></html>");
        
        showInfo("Owner Details", details.toString());
    }

    private void editOwner(VehicleOwner owner) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "Edit Vehicle Owner #" + owner.getVehicleOwnerId(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Edit Vehicle Owner", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        
        // Form fields with existing values
        formPanel.add(new JLabel("Owner Name:"));
        JTextField nameField = new JTextField(owner.getVehicleOwnerName() != null ? owner.getVehicleOwnerName() : "");
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Contact Number*:"));
        JTextField contactField = new JTextField(owner.getVehicleOwnerContact());
        formPanel.add(contactField);
        
        formPanel.add(new JLabel("Email Address*:"));
        JTextField emailField = new JTextField(owner.getVehicleOwnerEmail());
        formPanel.add(emailField);
        
        formPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(owner.getOwnerUsername() != null ? owner.getOwnerUsername() : "");
        formPanel.add(usernameField);
        
        formPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setText(owner.getOwnerPassword() != null ? owner.getOwnerPassword() : "");
        formPanel.add(passwordField);
        
        formPanel.add(new JLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setSelectedItem(owner.getStatus() != null && owner.getStatus() == 1 ? "Active" : "Inactive");
        formPanel.add(statusCombo);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("💾 Save Changes");
        saveButton.setBackground(SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            updateOwner(owner, nameField, contactField, emailField, 
                       usernameField, passwordField, statusCombo, dialog);
        });
        
        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setBackground(DANGER);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        content.add(title, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void updateOwner(VehicleOwner owner, JTextField nameField, JTextField contactField,
                            JTextField emailField, JTextField usernameField,
                            JPasswordField passwordField, JComboBox<String> statusCombo,
                            JDialog dialog) {
        try {
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();
            
            // Validation
            if (contact.isEmpty()) {
                showWarning("Validation Error", "Contact number is required");
                return;
            }
            
            if (email.isEmpty()) {
                showWarning("Validation Error", "Email address is required");
                return;
            }
            
            // Update owner object
            owner.setVehicleOwnerName(nameField.getText().trim());
            owner.setVehicleOwnerContact(contact);
            owner.setVehicleOwnerEmail(email);
            owner.setOwnerUsername(usernameField.getText().trim());
            
            String password = new String(passwordField.getPassword());
            if (!password.isEmpty()) {
                owner.setOwnerPassword(password);
            }
            
            String status = (String) statusCombo.getSelectedItem();
            owner.setStatus("Active".equals(status) ? 1 : 0);
            
            // Save to database
            boolean success = ownerDAO.update(owner);
            if (success) {
                showSuccess("Success", "Vehicle owner updated successfully!");
                dialog.dispose();
                loadOwners();
            } else {
                showError("Database Error", "Failed to update vehicle owner");
            }
            
        } catch (SQLException e) {
            showError("Database Error", "Failed to update owner: " + e.getMessage());
        }
    }

    private void deleteOwner(VehicleOwner owner) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Vehicle Owner #" + owner.getVehicleOwnerId() + "?\n" +
            "Name: " + (owner.getVehicleOwnerName() != null ? owner.getVehicleOwnerName() : "N/A") + "\n" +
            "This action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        ownerDAO.delete(owner.getVehicleOwnerId());
                        SwingUtilities.invokeLater(() -> {
                            showSuccess("Deleted", "Vehicle owner deleted successfully!");
                            loadOwners();
                        });
                    } catch (SQLException e) {
                        SwingUtilities.invokeLater(() -> 
                            showError("Delete Error", e.getMessage()));
                    }
                    return null;
                }
            };
            worker.execute();
        }
    }

    private void exportOwners() {
        // Simple export functionality
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Vehicle Owners to CSV");
        fileChooser.setSelectedFile(new java.io.File("vehicle_owners_" + 
            new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                exportToCSV(fileChooser.getSelectedFile());
                showSuccess("Export Successful", 
                    "Vehicle owners exported to: " + fileChooser.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }

    private void exportToCSV(java.io.File file) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            writer.println("Owner ID,Name,Contact,Email,User ID,Status,Username");
            
            for (VehicleOwner owner : ownerList) {
                writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\"",
                    owner.getVehicleOwnerId(),
                    owner.getVehicleOwnerName() != null ? owner.getVehicleOwnerName() : "",
                    owner.getVehicleOwnerContact(),
                    owner.getVehicleOwnerEmail(),
                    owner.getUserId() != null ? owner.getUserId() : "",
                    owner.getStatus() != null && owner.getStatus() == 1 ? "Active" : "Inactive",
                    owner.getOwnerUsername() != null ? owner.getOwnerUsername() : ""
                ));
            }
        }
    }

    // ================= HELPER METHODS =================
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // ================= TABLE CELL CLASSES =================
    class ActionCellRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return (Component) value;
        }
    }
    
    class ActionCellEditor extends javax.swing.AbstractCellEditor 
                         implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        
        @Override
        public Object getCellEditorValue() {
            return panel;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            panel = (JPanel) value;
            return panel;
        }
    }
}