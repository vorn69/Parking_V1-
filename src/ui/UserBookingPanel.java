package ui;

import dao.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import models.*;

public class UserBookingPanel extends JPanel {

    private ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private BookingDAO bookingDAO = new BookingDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();

    private JComboBox<ParkingSlot> cbSlots;
    private JComboBox<Vehicle> cbVehicles;
    private JTextField txtDuration;
    private JTextField txtRemarks;

    private int userId;
    private int ownerId;

    public UserBookingPanel(int userId, int ownerId) {
        this.userId = userId;
        this.ownerId = ownerId;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250));

        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        loadSlots();
        loadVehicles();
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Book Parking Slot"));

        cbSlots = new JComboBox<>();
        cbVehicles = new JComboBox<>();
        txtDuration = new JTextField();
        txtRemarks = new JTextField();

        panel.add(new JLabel("Available Slot:"));
        panel.add(cbSlots);
        panel.add(new JLabel("Your Vehicle:"));
        panel.add(cbVehicles);
        panel.add(new JLabel("Duration:"));
        panel.add(txtDuration);
        panel.add(new JLabel("Remarks:"));
        panel.add(txtRemarks);

        return panel;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("BOOK");
        btn.addActionListener(e -> book());
        panel.add(btn);
        return panel;
    }

    private void loadSlots() {
        try {
            cbSlots.removeAllItems();
            for (ParkingSlot s : slotDAO.findAvailableSlots()) {
                cbSlots.addItem(s);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cannot load slots");
        }
    }

    private void loadVehicles() {
        try {
            cbVehicles.removeAllItems();
            for (Vehicle v : vehicleDAO.findByOwnerId(ownerId)) {
                cbVehicles.addItem(v);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cannot load vehicles");
        }
    }

    private void book() {
        ParkingSlot slot = (ParkingSlot) cbSlots.getSelectedItem();
        Vehicle vehicle = (Vehicle) cbVehicles.getSelectedItem();

        if (slot == null || vehicle == null) {
            JOptionPane.showMessageDialog(this, "Select slot and vehicle");
            return;
        }

        try {
            Booking b = new Booking();
            b.setCustomerId(ownerId);
            b.setVehicleId(vehicle.getVehicleId());
            b.setSlotId(slot.getParkingSlotId());
            b.setUserId(userId);
            b.setDurationOfBooking(txtDuration.getText());
            b.setRemarks(txtRemarks.getText());
            b.setUserId(userId);

            bookingDAO.createBookingWithSlotUpdate(b);

            JOptionPane.showMessageDialog(this, "Booking submitted. Waiting for admin approval.");
            loadSlots();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
