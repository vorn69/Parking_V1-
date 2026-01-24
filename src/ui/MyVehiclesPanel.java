package ui;

import dao.VehicleCategoryDAO;
import dao.VehicleDAO;
import java.awt.*;
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
    
    public MyVehiclesPanel(Integer customerId, Integer ownerId) {
        this.customerId = customerId;
        this.ownerId = ownerId;
        this.vehicleDAO = new VehicleDAO();
        this.categoryDAO = new VehicleCategoryDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // First, check if ownerId is valid
        if (ownerId == null || ownerId <= 0) {
            showNoOwnerMessage();
            return;
        }
        
        loadCategories();
        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
        
        loadVehicles();
    }
    
    private void showNoOwnerMessage() {
        removeAll();
        setLayout(new BorderLayout());
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        JLabel iconLabel = new JLabel("⚠️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        iconLabel.setForeground(new Color(241, 196, 15));
        
        JLabel titleLabel = new JLabel("No Vehicle Owner Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel messageLabel = new JLabel(
            "<html><div style='text-align: center;'>" +
            "You don't have a vehicle owner account yet.<br>" +
            "Please contact the administrator to set up your account.<br><br>" +
            "Customer ID: " + customerId + "<br>" +
            "Owner ID: " + (ownerId != null ? ownerId : "Not assigned") +
            "</div></html>", 
            SwingConstants.CENTER
        );
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(127, 140, 141));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.add(iconLabel);
        content.add(Box.createVerticalStrut(20));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(messageLabel);
        
        messagePanel.add(content, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.CENTER);
        
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
            JOptionPane.showMessageDialog(this,
                "Error loading vehicle categories: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("🚗 My Vehicles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(52, 73, 94));
        header.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Owner ID: " + ownerId);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(127, 140, 141));
        header.add(subtitle, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        String[] columns = {"ID", "Plate Number", "Description", "Category", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        vehicleTable = new JTable(tableModel);
        vehicleTable.setRowHeight(40);
        vehicleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < vehicleTable.getColumnCount(); i++) {
            vehicleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Set column widths
        vehicleTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        vehicleTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        vehicleTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        vehicleTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        vehicleTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(new Color(245, 247, 250));
        
        JButton addButton = new JButton("➕ Add Vehicle");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setFocusPainted(false);
        addButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addVehicle());
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadVehicles());
        
        panel.add(addButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void loadVehicles() {
        try {
            vehicleList = vehicleDAO.findByVehicleOwnerId(ownerId);
            updateTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading vehicles: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        
        if (vehicleList == null || vehicleList.isEmpty()) {
            tableModel.addRow(new Object[]{
                "", "No vehicles found", 
                "Click 'Add Vehicle' to register your first vehicle", 
                "", ""
            });
            return;
        }
        
        for (Vehicle vehicle : vehicleList) {
            String categoryName = categoryMap.getOrDefault(vehicle.getVehicleCategoryId(), "N/A");
            
            tableModel.addRow(new Object[]{
                vehicle.getVehicleId(),
                vehicle.getVehiclePlateNumber(),
                vehicle.getVehicleDescription() != null ? 
                    shortenText(vehicle.getVehicleDescription(), 30) : 
                    "No description",
                categoryName,
                "✅ Active"
            });
        }
    }
    
    private String shortenText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private void addVehicle() {
        // Simple dialog for adding vehicle
        JTextField plateField = new JTextField(20);
        JTextField descField = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("License Plate:"));
        panel.add(plateField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Owner ID:"));
        panel.add(new JLabel(ownerId.toString()));
        
        int result = JOptionPane.showConfirmDialog(this, panel,
            "Add New Vehicle", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String plateNumber = plateField.getText().trim().toUpperCase();
            String description = descField.getText().trim();
            
            if (plateNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "License plate is required!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // Create vehicle object
                Vehicle vehicle = new Vehicle();
                vehicle.setVehiclePlateNumber(plateNumber);
                vehicle.setVehicleDescription(description.isEmpty() ? null : description);
                vehicle.setVehicleOwnerId(ownerId);
                vehicle.setVehicleCategoryId(1); // Default to Car category
                
                // Save to database
                Integer vehicleId = vehicleDAO.create(vehicle);
                
                if (vehicleId != null) {
                    JOptionPane.showMessageDialog(this,
                        "Vehicle added successfully!\nID: " + vehicleId,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadVehicles(); // Refresh the list
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving vehicle: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}