package ui;

import dao.ParkingSlotDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.ParkingSlot;

public class ParkingSlotPanel extends JPanel {

    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_BLACK = Color.BLACK;
    private final Color TABLE_HEADER = new Color(60, 64, 72);
    private final Color BOTTOM_BG = Color.BLACK;
    private final Color BOTTOM_TEXT = Color.WHITE;

    private DefaultTableModel slotTableModel;
    private JTable slotTable;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel infoLabel;
    private JLabel timestampLabel;

    private ParkingSlotDAO slotDAO;
    private List<ParkingSlot> slotList;

    public ParkingSlotPanel() {
        slotDAO = new ParkingSlotDAO();
        slotList = new ArrayList<>();
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
        titleLabel.setForeground(TEXT_BLACK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(MAIN_BG);

        JButton addButton = new JButton("+ ADD SLOT");
        addButton.addActionListener(e -> addSlot());
        buttonPanel.add(addButton);

        JButton refreshButton = new JButton("⟳ REFRESH");
        refreshButton.addActionListener(e -> loadSlots());
        buttonPanel.add(refreshButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        String[] columns = {"ID", "Slot Number", "Status"};
        slotTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        slotTable = new JTable(slotTableModel);
        slotTable.setRowHeight(35);
        slotTable.getTableHeader().setBackground(TABLE_HEADER);
        slotTable.getTableHeader().setForeground(Color.WHITE);

        // Center renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        slotTable.setDefaultRenderer(Object.class, centerRenderer);

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
            slotTableModel.setRowCount(0);

            for (ParkingSlot slot : slotList) {
                slotTableModel.addRow(new Object[]{
                        slot.getParkingSlotId(),
                        slot.getSlotNumber(),
                        slot.getStatusText()
                });
            }

            updateInfoPanel();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load slots!");
        }
    }

    private void updateInfoPanel() {
        int total = slotList.size();
        long available = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE).count();
        long occupied = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED).count();

        infoLabel.setText("Total Slots: " + total + " | Available: " + available + " | Occupied: " + occupied);
        timestampLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void addSlot() {
        String number = JOptionPane.showInputDialog(this, "Enter slot number:");
        if (number == null || number.isEmpty()) return;

        ParkingSlot slot = new ParkingSlot();
        slot.setParkingSlotNumber(Integer.parseInt(number));
        slot.setParkingSlotStatus(ParkingSlot.STATUS_AVAILABLE);

        try {
            slotDAO.create(slot);
            loadSlots();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add slot!");
        }
    }

    public void editSelectedSlot() {
        int row = slotTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot to edit!");
            return;
        }

        ParkingSlot slot = slotList.get(row);
        String[] statuses = {"Available", "Occupied", "Reserved", "Maintenance"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Select new status for slot " + slot.getSlotNumber(),
                "Edit Slot",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                slot.getStatusText());

        // if (newStatus != null) {
        //     slot.setStatusFromText(newStatus);
        //     try {
        //         slotDAO.update(slot);
        //         loadSlots();
        //     } catch (SQLException e) {
        //         e.printStackTrace();
        //         JOptionPane.showMessageDialog(this, "Failed to update slot!");
        //     }
        // }
    }

    public void deleteSelectedSlot() {
        int row = slotTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot to delete!");
            return;
        }

        ParkingSlot slot = slotList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete slot " + slot.getSlotNumber() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                slotDAO.delete(slot.getParkingSlotId());
                loadSlots();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to delete slot!");
            }
        }
    }
}
