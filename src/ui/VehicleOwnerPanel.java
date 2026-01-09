package ui;

import dao.VehicleOwnerDAO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.VehicleOwner;

public class VehicleOwnerPanel extends JPanel {

    private JTable ownerTable;
    private VehicleOwnerDAO ownerDAO;
    private List<VehicleOwner> ownerList;
    private JTextField searchField;
    private JButton searchButton, addButton, editButton, deleteButton, refreshButton;

    public VehicleOwnerPanel() {
        ownerDAO = new VehicleOwnerDAO();
        ownerList = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.decode("#F5F5F5"));

        initTopPanel();
        initTable();
        initBottomButtons();

        loadOwners();
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#C0C0C0")),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        searchButton = new JButton("Search");
        styleButton(searchButton, "#1976D2");

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        searchButton.addActionListener(e -> filterOwners(searchField.getText()));

        add(topPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        ownerTable = new JTable();
        ownerTable.setRowHeight(28);
        ownerTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ownerTable.setFillsViewportHeight(true);
        ownerTable.setSelectionBackground(Color.decode("#BBDEFB"));
        ownerTable.setSelectionForeground(Color.BLACK);
        ownerTable.setShowGrid(true);
        ownerTable.setGridColor(Color.decode("#E0E0E0"));

        // Header style
        ownerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        ownerTable.getTableHeader().setBackground(Color.decode("#1976D2"));
        ownerTable.getTableHeader().setForeground(Color.WHITE);
        ownerTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(ownerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initBottomButtons() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        styleButton(addButton, "#4CAF50");
        styleButton(editButton, "#FFC107");
        styleButton(deleteButton, "#F44336");
        styleButton(refreshButton, "#607D8B");

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Example actions
        refreshButton.addActionListener(e -> loadOwners());
    }

    private void styleButton(JButton button, String colorHex) {
        button.setBackground(Color.decode(colorHex));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void loadOwners() {
        try {
            ownerList = ownerDAO.findAll();
            updateTable(ownerList);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading vehicle owners: " + e.getMessage());
        }
    }

    private void updateTable(List<VehicleOwner> list) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Name", "Contact", "Email", "Status"}, 0
        );

        for (VehicleOwner o : list) {
            model.addRow(new Object[]{
                    o.getVehicleOwnerId(),
                    o.getVehicleOwnerName(),
                    o.getVehicleOwnerContact(),
                    o.getVehicleOwnerEmail(),
                    o.getStatus() == 1 ? "Active" : "Inactive"
            });
        }

        ownerTable.setModel(model);

        // Center align ID and Status
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        ownerTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        ownerTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        ownerTable.getColumnModel().getColumn(0).setMaxWidth(60);
        ownerTable.getColumnModel().getColumn(4).setMaxWidth(80);
    }

    private void filterOwners(String query) {
        if (query == null || query.isEmpty()) {
            updateTable(ownerList);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<VehicleOwner> filtered = new ArrayList<>();

        for (VehicleOwner o : ownerList) {
            if (o.getVehicleOwnerName().toLowerCase().contains(lowerQuery) ||
                o.getVehicleOwnerContact().toLowerCase().contains(lowerQuery) ||
                o.getVehicleOwnerEmail().toLowerCase().contains(lowerQuery)) {
                filtered.add(o);
            }
        }

        updateTable(filtered);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Vehicle Owners");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 450);
            frame.setLocationRelativeTo(null);
            frame.add(new VehicleOwnerPanel());
            frame.setVisible(true);
        });
    }
}
