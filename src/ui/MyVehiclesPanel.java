package ui;

import dao.VehicleCategoryDAO;
import dao.VehicleDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.Vehicle;
import models.VehicleCategory;

public class MyVehiclesPanel extends JPanel {

    private Integer customerId;
    private Integer ownerId;
    private VehicleDAO vehicleDAO;
    private VehicleCategoryDAO categoryDAO;
    private List<Vehicle> vehicleList;
    private List<VehicleCategory> categories;
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private Map<Integer, String> categoryMap = new HashMap<>();
    private CustomerDashboard dashboard;

    public MyVehiclesPanel(Integer customerId, Integer ownerId, CustomerDashboard dashboard) {
        this.customerId = customerId;
        this.ownerId = ownerId;
        this.dashboard = dashboard;
        this.vehicleDAO = new VehicleDAO();
        this.categoryDAO = new VehicleCategoryDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 242, 245)); // Modern light background
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // First, check if ownerId is valid
        if (ownerId == null || ownerId <= 0) {
            showNoOwnerMessage();
            return;
        }

        loadCategories();

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Content (Table)
        add(createTablePanel(), BorderLayout.CENTER);

        loadVehicles();
    }

    private void showNoOwnerMessage() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Registration Required");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("You need to register as a Vehicle Owner to verify vehicles.");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLabel.setForeground(new Color(127, 140, 141));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerBtn = createStyledButton("Register Now", new Color(46, 204, 113));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> {
            if (dashboard != null) {
                dashboard.showRegistrationForm();
            }
        });

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(msgLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(registerBtn);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void loadCategories() {
        try {
            categories = categoryDAO.findAll();
            if (categories != null) {
                categoryMap.clear();
                for (VehicleCategory category : categories) {
                    categoryMap.put(category.getVehicleCategoryId(),
                            category.getVehicleCategoryName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("My Vehicles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));

        JLabel subtitle = new JLabel("Manage your registered vehicles");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(127, 140, 141));

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton refreshBtn = createStyledButton("Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadVehicles());

        JButton addBtn = createStyledButton("Add Vehicle +", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addVehicle());

        actionPanel.add(refreshBtn);
        actionPanel.add(addBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Modern Card styling
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 0, 0, 0),
                new LineBorder(new Color(230, 230, 230), 1, true)));

        String[] columns = { "ID", "Plate Number", "Description", "Category", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vehicleTable = new JTable(tableModel);
        vehicleTable.setRowHeight(50); // Taller rows
        vehicleTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vehicleTable.setShowVerticalLines(false);
        vehicleTable.setIntercellSpacing(new Dimension(0, 0));
        vehicleTable.setFillsViewportHeight(true);
        vehicleTable.setSelectionBackground(new Color(232, 240, 254));
        vehicleTable.setSelectionForeground(Color.BLACK);

        // Header styling
        JTableHeader header = vehicleTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(44, 62, 80));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        // Custom Renderers
        vehicleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(Color.WHITE);
                }
                setBorder(new EmptyBorder(0, 15, 0, 15)); // Padding
                return c;
            }
        });

        // Center alignment for specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        vehicleTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Status Column Renderer (Pill style)
        vehicleTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setText(value.toString().replace("✅ ", "")); // Remove old icon if exists
                label.setForeground(new Color(39, 174, 96));
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(CENTER);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        return btn;
    }

    private void loadVehicles() {
        new SwingWorker<List<Vehicle>, Void>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                try {
                    return vehicleDAO.findByVehicleOwnerId(ownerId);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    vehicleList = get();
                    updateTable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateTable() {
        tableModel.setRowCount(0);

        if (vehicleList == null || vehicleList.isEmpty()) {
            return;
        }

        for (Vehicle vehicle : vehicleList) {
            String categoryName = categoryMap.getOrDefault(vehicle.getVehicleCategoryId(), "Car");
            tableModel.addRow(new Object[] {
                    vehicle.getVehicleId(),
                    vehicle.getVehiclePlateNumber(),
                    vehicle.getVehicleDescription() != null ? vehicle.getVehicleDescription() : "-",
                    categoryName,
                    "Active"
            });
        }
    }

    private void addVehicle() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Vehicle", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Title
        JLabel title = new JLabel("Register Vehicle");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));
        content.add(title, gbc);

        gbc.gridy++;
        content.add(new JLabel(" "), gbc); // Spacer

        // Fields
        gbc.gridy++;
        content.add(createLabel("License Plate Number *"), gbc);

        gbc.gridy++;
        JTextField plateField = createStyledField();
        content.add(plateField, gbc);

        gbc.gridy++;
        content.add(createLabel("Description / Model"), gbc);

        gbc.gridy++;
        JTextField descField = createStyledField();
        content.add(descField, gbc);

        gbc.gridy++;
        content.add(createLabel("Category"), gbc);

        gbc.gridy++;
        String[] cats = { "Car", "Motorcycle", "Truck" }; // Fallback if DB load fails
        if (categories != null && !categories.isEmpty()) {
            cats = categories.stream().map(VehicleCategory::getVehicleCategoryName).toArray(String[]::new);
        }
        JComboBox<String> catCombo = new JComboBox<>(cats);
        catCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        catCombo.setBackground(Color.WHITE);
        content.add(catCombo, gbc);

        // Buttons
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);

        JButton saveBtn = createStyledButton("Save Vehicle", new Color(46, 204, 113));
        saveBtn.addActionListener(e -> {
            String plate = plateField.getText().trim().toUpperCase();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Plate number is required");
                return;
            }

            try {
                Vehicle v = new Vehicle();
                v.setVehiclePlateNumber(plate);
                v.setVehicleDescription(descField.getText().trim());
                v.setVehicleOwnerId(ownerId);
                // Map selected string back to ID (simplified)
                v.setVehicleCategoryId(1);
                if (categories != null) {
                    for (VehicleCategory vc : categories) {
                        if (vc.getVehicleCategoryName().equals(catCombo.getSelectedItem())) {
                            v.setVehicleCategoryId(vc.getVehicleCategoryId());
                            break;
                        }
                    }
                }

                vehicleDAO.create(v);
                JOptionPane.showMessageDialog(dialog, "Vehicle registered successfully!");
                dialog.dispose();
                loadVehicles();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        content.add(saveBtn, gbc);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(100, 100, 100));
        return l;
    }

    private JTextField createStyledField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(200, 40));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        return f;
    }
}