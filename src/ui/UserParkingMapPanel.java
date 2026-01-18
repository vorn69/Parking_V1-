package ui;

import dao.BookingDAO;
import dao.ParkingSlotDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import models.Booking;
import models.ParkingSlot;

public class UserParkingMapPanel extends JPanel {

    private int userId;
    private ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    public UserParkingMapPanel(int userId) {
        this.userId = userId;
        initComponents();
        loadSlots();
    }

    public UserParkingMapPanel(int userId, int ownerId) {
        this(userId);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 242, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createSlotGrid(), BorderLayout.CENTER);
        add(createLegend(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel title = new JLabel("PARKING SLOTS MAP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.WEST);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSlots());
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(refreshBtn, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createSlotGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(2, 10, 15, 15));
        gridPanel.setBackground(new Color(240, 242, 245));
        return gridPanel;
    }

    private JPanel createLegend() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        legendPanel.setOpaque(false);
        
        legendPanel.add(createLegendItem("Available", new Color(76, 175, 80)));
        legendPanel.add(createLegendItem("Reserved", new Color(255, 193, 7)));
        legendPanel.add(createLegendItem("Occupied", new Color(239, 83, 80)));
        
        return legendPanel;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        
        JLabel colorLabel = new JLabel("■");
        colorLabel.setForeground(color);
        colorLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panel.add(colorLabel);
        panel.add(textLabel);
        return panel;
    }

    private void loadSlots() {
        try {
            JPanel gridPanel = (JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
            gridPanel.removeAll();
            
            List<ParkingSlot> slots = slotDAO.findAll();

            for (ParkingSlot slot : slots) {
                gridPanel.add(createSlotButton(slot));
            }

            gridPanel.revalidate();
            gridPanel.repaint();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading slots", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createSlotButton(ParkingSlot slot) {
        JButton btn = new JButton("P" + slot.getParkingSlotNumber());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(80, 80));
        
        // Set color based on status
        if (slot.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE) {
            btn.setBackground(new Color(76, 175, 80));
            btn.setForeground(Color.WHITE);
            btn.addActionListener(e -> bookSlot(slot));
        } else if (slot.getParkingSlotStatus() == ParkingSlot.STATUS_RESERVED) {
            btn.setBackground(new Color(255, 193, 7));
            btn.setForeground(Color.BLACK);
            btn.setEnabled(false);
        } else if (slot.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED) {
            btn.setBackground(new Color(239, 83, 80));
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
        }
        
        return btn;
    }

    // SIMPLE BOOKING METHOD - HARDCODED VALUES
    private void bookSlot(ParkingSlot slot) {
        try {
            // HARDCODED VALUES FROM YOUR DATABASE
            int customerId = 18;     // vehicle_owner_id 18 (belongs to user 12)
            int vehicleId = 22;      // Vehicle ID 22 (ABC-1234)
            
            // Ask for duration
            String duration = JOptionPane.showInputDialog(
                this, 
                "Enter duration (e.g., '1 Hour', '2 Hours'):", 
                "Book Slot P" + slot.getParkingSlotNumber(),
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (duration == null || duration.trim().isEmpty()) {
                return; // User cancelled
            }
            
            // Show confirmation
            String message = String.format(
                "Confirm Booking:\n" +
                "Slot: P%d\n" +
                "Duration: %s\n" +
                "Vehicle: ABC-1234\n" +
                "\nClick OK to confirm.",
                slot.getParkingSlotNumber(), duration
            );
            
            int confirm = JOptionPane.showConfirmDialog(
                this, message, "Confirm", JOptionPane.OK_CANCEL_OPTION
            );
            
            if (confirm != JOptionPane.OK_OPTION) {
                return;
            }
            
            // Create booking object
            Booking booking = new Booking();
            booking.setCustomerId(customerId);
            booking.setVehicleId(vehicleId);
            booking.setSlotId(slot.getParkingSlotId());
            booking.setUserId(userId);
            booking.setDurationOfBooking(duration);
            booking.setRemarks("Booking from user");
            
            // Save to database
            int bookingId = bookingDAO.createBookingWithSlotUpdate(booking);
            
            // Show success
            JOptionPane.showMessageDialog(
                this,
                "Booking created successfully!\nBooking ID: " + bookingId,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Refresh display
            loadSlots();
            
        } catch (SQLException e) {
            // Show detailed error
            String errorMsg = "Booking failed!\n\n";
            errorMsg += "Error: " + e.getMessage() + "\n\n";
            errorMsg += "Database details:\n";
            errorMsg += "- User ID: " + userId + "\n";
            errorMsg += "- Customer ID (vehicle_owner_id): 18\n";
            errorMsg += "- Vehicle ID: 22\n";
            errorMsg += "- Slot ID: " + slot.getParkingSlotId() + "\n";
            
            JOptionPane.showMessageDialog(
                this, errorMsg, "Booking Error", JOptionPane.ERROR_MESSAGE
            );
            
            e.printStackTrace(); // For debugging
        }
    }
}