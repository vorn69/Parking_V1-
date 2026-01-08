package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import dao.VehicleOwnerDAO;
import models.VehicleOwner;

public class VehicleOwnerPanel extends JPanel {

    private JTable ownerTable;
    private VehicleOwnerDAO ownerDAO;
    private List<VehicleOwner> ownerList;
    private JTextField searchField;
    private JButton searchButton;

    public VehicleOwnerPanel() {
        ownerDAO = new VehicleOwnerDAO();
        ownerList = new ArrayList<>();

        setLayout(new BorderLayout());

        // Top panel for search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // Table
        ownerTable = new JTable();
        add(new JScrollPane(ownerTable), BorderLayout.CENTER);

        // Load data
        loadOwners();

        // Search action
        searchButton.addActionListener(e -> filterOwners(searchField.getText()));
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
        Object[][] data = new Object[list.size()][5];

        for (int i = 0; i < list.size(); i++) {
            VehicleOwner o = list.get(i);
            data[i][0] = o.getVehicleOwnerId();
            data[i][1] = o.getVehicleOwnerName();
            data[i][2] = o.getVehicleOwnerContact();
            data[i][3] = o.getVehicleOwnerEmail();
            data[i][4] = o.getStatus() == 1 ? "Active" : "Inactive";
        }

        ownerTable.setModel(new DefaultTableModel(
                data,
                new String[]{"ID", "Name", "Contact", "Email", "Status"}
        ));
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

    // Test panel independently
    public static void main(String[] args) {
        JFrame frame = new JFrame("Vehicle Owners");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(null);
        frame.add(new VehicleOwnerPanel());
        frame.setVisible(true);
    }
}
