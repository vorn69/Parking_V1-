package ui;

import dao.ParkingSlotDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.ParkingSlot;

public class ParkingSlotPanel extends JPanel {

    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color TABLE_HEADER = new Color(60, 64, 72);
    private final Color BOTTOM_BG = Color.BLACK;
    private final Color BOTTOM_TEXT = Color.WHITE;

    private DefaultTableModel slotTableModel;
    private JTable slotTable;
    private JLabel infoLabel;
    private JLabel timestampLabel;

    private ParkingSlotDAO slotDAO;
    private List<ParkingSlot> slotList;

    private final String[] statusOptions = {"Available", "Occupied", "Reserved", "Maintenance"};
    private JComboBox<String> statusFilter;

    public ParkingSlotPanel() {
        slotDAO = new ParkingSlotDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        createHeaderPanel();
        createTablePanel();
        createInfoPanel();
        loadSlots();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);

        JLabel titleLabel = new JLabel("PARKING SLOT MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(MAIN_BG);

        JButton addButton = new JButton("+ ADD SLOT");
        addButton.addActionListener(e -> addSlot());
        buttonPanel.add(addButton);

        JButton refreshButton = new JButton("⟳ REFRESH");
        refreshButton.addActionListener(e -> loadSlots());
        buttonPanel.add(refreshButton);

        // Status Filter
        statusFilter = new JComboBox<>();
        statusFilter.addItem("All Statuses");
        for (String status : statusOptions) statusFilter.addItem(status);
        statusFilter.addActionListener(e -> filterByStatus());
        buttonPanel.add(new JLabel("Filter:"));
        buttonPanel.add(statusFilter);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        String[] columns = {"ID", "Slot Number", "Status"};
        slotTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // Only Status editable
            }
        };

        slotTable = new JTable(slotTableModel);
        slotTable.setRowHeight(35);
        slotTable.getTableHeader().setBackground(TABLE_HEADER);
        slotTable.getTableHeader().setForeground(Color.WHITE);

        // Center renderer for ID and Slot Number
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        slotTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        slotTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Status column editor
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        slotTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));

        // Listen for status changes
        slotTable.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 2) {
                int row = e.getFirstRow();
                Integer id = (Integer) slotTable.getValueAt(row, 0); // Get slot ID from table
                ParkingSlot slot = slotList.stream()
                        .filter(s -> s.getParkingSlotId().equals(id))
                        .findFirst().orElse(null);

                if (slot != null) {
                    String newStatus = (String) slotTable.getValueAt(row, 2);
                    slot.setStatusFromText(newStatus);
                    try {
                        slotDAO.updateStatus(slot); // Update DB
                        updateInfoPanel();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Failed to update status!");
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(slotTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BOTTOM_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(BOTTOM_TEXT);
        infoPanel.add(infoLabel, BorderLayout.WEST);

        timestampLabel = new JLabel();
        timestampLabel.setForeground(new Color(180, 180, 180));
        infoPanel.add(timestampLabel, BorderLayout.EAST);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadSlots() {
        try {
            slotList = slotDAO.findAll();
            updateTable(slotList);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load slots!");
        }
    }

    private void updateTable(List<ParkingSlot> list) {
        slotTableModel.setRowCount(0);
        for (ParkingSlot slot : list) {
            slotTableModel.addRow(new Object[]{
                    slot.getParkingSlotId(),
                    slot.getSlotNumber(),
                    slot.getStatusText()
            });
        }
        updateInfoPanel();
    }

    private void updateInfoPanel() {
        int total = slotList.size();
        long available = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE).count();
        long occupied = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED).count();

        infoLabel.setText("Total Slots: " + total + " | Available: " + available + " | Occupied: " + occupied);
        timestampLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void addSlot() {
        JTextField numberField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Slot Number:"));
        panel.add(numberField);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Parking Slot",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingSlotNumber(Integer.parseInt(numberField.getText()));
                slot.setStatusFromText((String) statusBox.getSelectedItem());
                slotDAO.create(slot);
                loadSlots();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to add slot!");
            }
        }
    }

    private void filterByStatus() {
        String selected = (String) statusFilter.getSelectedItem();
        if (selected.equals("All Statuses")) {
            updateTable(slotList);
        } else {
            List<ParkingSlot> filtered = slotList.stream()
                    .filter(s -> s.getStatusText().equals(selected))
                    .collect(Collectors.toList());
            updateTable(filtered);
        }
    }

    // Optional: Edit status via dialog
    public void editSelectedSlot() {
        int row = slotTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot to edit!");
            return;
        }

        Integer id = (Integer) slotTable.getValueAt(row, 0);
        ParkingSlot slot = slotList.stream().filter(s -> s.getParkingSlotId().equals(id)).findFirst().orElse(null);
        if (slot == null) return;

        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Select new status for slot " + slot.getSlotNumber(),
                "Edit Slot",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statusOptions,
                slot.getStatusText());

        if (newStatus != null) {
            slot.setStatusFromText(newStatus);
            try {
                slotDAO.updateStatus(slot);
                loadSlots();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update slot!");
            }
        }
    }
}
