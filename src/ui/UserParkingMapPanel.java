package ui;

import dao.ParkingSlotDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import models.ParkingSlot;

public class UserParkingMapPanel extends JPanel {

    private int userId;
    private int ownerId;
    private ParkingSlotDAO slotDAO = new ParkingSlotDAO();

    public UserParkingMapPanel(int userId, int ownerId) {
        this.userId = userId;
        this.ownerId = ownerId;

        setLayout(new GridLayout(2, 10, 15, 15));
        setBackground(Color.WHITE);

        loadSlots();
    }

    private void loadSlots() {
        removeAll();

        try {
            List<ParkingSlot> slots = slotDAO.findAll();

            for (ParkingSlot slot : slots) {
                add(createSlotButton(slot));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        revalidate();
        repaint();
    }   

    private JButton createSlotButton(ParkingSlot slot) {
        JButton btn = new JButton("P" + slot.getParkingSlotNumber());
        btn.setPreferredSize(new Dimension(70, 120));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);

        // 🎨 COLOR BY STATUS
    switch (slot.getParkingSlotStatus()) {
        case ParkingSlot.STATUS_AVAILABLE ->
            btn.setBackground(new Color(76, 175, 80)); // GREEN

        case ParkingSlot.STATUS_RESERVED ->
            btn.setBackground(new Color(255, 193, 7)); // YELLOW

        case ParkingSlot.STATUS_OCCUPIED ->
            btn.setBackground(new Color(239, 83, 80)); // RED
    }

        // CLICK ONLY IF AVAILABLE
        if (slot.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE) {
            btn.addActionListener(e -> openBookingDialog(slot));
        } else {
            btn.setEnabled(false);
        }

        return btn;
    }

    private void openBookingDialog(ParkingSlot slot) {
        JTextField txtDuration = new JTextField();
        JTextField txtRemarks  = new JTextField();

        Object[] form = {
            "Slot: P" + slot.getParkingSlotNumber(),
            "Duration:", txtDuration,
            "Remarks:", txtRemarks
        };

        int opt = JOptionPane.showConfirmDialog(
                this, form, "Book Parking Slot",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opt == JOptionPane.OK_OPTION) {
            try {
                // reserve slot
                slotDAO.reserveSlot(slot.getParkingSlotId(), userId);

                JOptionPane.showMessageDialog(this, "Booking submitted (PENDING)");
                loadSlots();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
