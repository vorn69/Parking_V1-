package ui;

import dao.ParkingSlotDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;
import models.ParkingSlot;

public class ParkingSlotPanel extends JPanel {

    private java.util.List<ParkingSlot> slotList;
    private JTable slotTable;
    private ParkingSlotDAO slotDAO;

    public ParkingSlotPanel() {
        slotDAO = new ParkingSlotDAO();
        slotList = new ArrayList<>();
        setLayout(new BorderLayout());

        // Table
        slotTable = new JTable();
        add(new JScrollPane(slotTable), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add Slot");
        JButton editBtn = new JButton("Edit Slot");
        JButton deleteBtn = new JButton("Delete Slot");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadSlots();

        // Button actions
        addBtn.addActionListener(e -> addSlot());
        editBtn.addActionListener(e -> editSlot());
        deleteBtn.addActionListener(e -> deleteSlot());
    }

    private void loadSlots() {
        try {
            slotList = slotDAO.findAll();
            Object[][] data = new Object[slotList.size()][3];
            for (int i = 0; i < slotList.size(); i++) {
                ParkingSlot slot = slotList.get(i);
                data[i][0] = slot.getParkingSlotId();
                data[i][1] = slot.getSlotNumber();
                data[i][2] = slot.getStatusText();
            }

            slotTable.setModel(new javax.swing.table.DefaultTableModel(
                    data,
                    new String[]{"ID", "Number", "Status"}
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Only Status column editable
                    return column == 2;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addSlot() {
        try {
            String numberStr = JOptionPane.showInputDialog(this, "Enter slot number:");
            if (numberStr == null || numberStr.isEmpty()) return;

            int number = Integer.parseInt(numberStr);
            ParkingSlot slot = new ParkingSlot();
            slot.setParkingSlotNumber(number);
            slot.setParkingSlotStatus(ParkingSlot.STATUS_AVAILABLE);

            slotDAO.create(slot);
            loadSlots();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add slot!");
        }
    }

    private void editSlot() {
        int selectedRow = slotTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot to edit.");
            return;
        }

        ParkingSlot slot = slotList.get(selectedRow);
        String[] statuses = {"Available", "Occupied", "Reserved", "Maintenance"};
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select status for slot " + slot.getSlotNumber(),
                "Edit Slot Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                slot.getStatusText()
        );

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

    private void deleteSlot() {
        int selectedRow = slotTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a slot to delete.");
            return;
        }

        ParkingSlot slot = slotList.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete slot " + slot.getSlotNumber() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

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
