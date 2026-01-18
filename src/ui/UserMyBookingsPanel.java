package ui;

import dao.BookingDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Booking;

public class UserMyBookingsPanel extends JPanel {

    private int userId;
    private BookingDAO bookingDAO = new BookingDAO();
    private JTable table;
    private DefaultTableModel model;
    private List<Booking> bookingList;

    public UserMyBookingsPanel(int userId) {
        this.userId = userId;
        initComponents();
        loadMyBookings();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel title = new JLabel("MY BOOKINGS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.WEST);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton viewDetailsBtn = new JButton("📋 View Details");
        JButton cancelBtn = new JButton("❌ Cancel Booking");
        
        refreshBtn.addActionListener(e -> loadMyBookings());
        viewDetailsBtn.addActionListener(e -> viewBookingDetails());
        cancelBtn.addActionListener(e -> cancelBooking());
        
        btnPanel.add(refreshBtn);
        btnPanel.add(viewDetailsBtn);
        btnPanel.add(cancelBtn);
        
        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(
            new String[]{"Booking ID", "Reference", "Slot", "Duration", "Amount", "Status", "Booking Time"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> Integer.class; // Booking ID
                    case 2 -> Integer.class; // Slot
                    case 4 -> Double.class; // Amount
                    default -> String.class;
                };
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        return new JScrollPane(table);
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel countLabel = new JLabel();
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(countLabel);
        
        // Update count when data loads
        SwingUtilities.invokeLater(() -> updateFooter(countLabel));
        
        return panel;
    }

    private void loadMyBookings() {
        try {
            bookingList = bookingDAO.findByUserId(userId);
            model.setRowCount(0);

            for (Booking b : bookingList) {
                model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getBookingRef(),
                    b.getSlotId(),
                    b.getDurationOfBooking(),
                    b.getTotalAmount(),
                    getStatusWithIcon(b.getBookingStatus()),
                    b.getFormattedBookingTime()
                });
            }

            updateFooter();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private String getStatusWithIcon(int status) {
        String icon = switch (status) {
            case Booking.STATUS_PENDING -> "⏳ ";
            case Booking.STATUS_APPROVED -> "✅ ";
            case Booking.STATUS_REJECTED -> "❌ ";
            case Booking.STATUS_COMPLETED -> "✔️ ";
            case Booking.STATUS_CANCELLED -> "🚫 ";
            default -> "❓ ";
        };
        // return icon + Booking.getStatusText(status);
        return icon + switch (status) {
            case Booking.STATUS_PENDING -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            case Booking.STATUS_COMPLETED -> "COMPLETED";
            case Booking.STATUS_CANCELLED -> "CANCELLED";   
            default -> "UNKNOWN";
        };
    }

    private void updateFooter() {
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel footer = (JPanel) comp;
                for (Component c : footer.getComponents()) {
                    if (c instanceof JLabel) {
                        updateFooter((JLabel) c);
                        break;
                    }
                }
            }
        }
    }

    private void updateFooter(JLabel label) {
        if (bookingList == null) return;
        
        long pending = bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING)
                .count();
        
        long approved = bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED)
                .count();
        
        label.setText(String.format(
            "Total Bookings: %d | ⏳ Pending: %d | ✅ Approved: %d",
            bookingList.size(), pending, approved
        ));
    }

    private void viewBookingDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("No Selection", "Please select a booking first");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Booking booking = bookingList.get(modelRow);
        
        String details = String.format("""
            <html>
            <h3>Booking Details</h3>
            <table style='border-collapse: collapse;'>
            <tr><td><b>Booking ID:</b></td><td>%d</td></tr>
            <tr><td><b>Reference:</b></td><td>%s</td></tr>
            <tr><td><b>Customer ID:</b></td><td>%d</td></tr>
            <tr><td><b>Vehicle ID:</b></td><td>%d</td></tr>
            <tr><td><b>Slot ID:</b></td><td>%d</td></tr>
            <tr><td><b>Duration:</b></td><td>%s</td></tr>
            <tr><td><b>Status:</b></td><td>%s</td></tr>
            <tr><td><b>Booking Time:</b></td><td>%s</td></tr>
            <tr><td><b>Remarks:</b></td><td>%s</td></tr>
            <tr><td><b>Total Amount:</b></td><td>%s</td></tr>
            </table>
            </html>
            """,
            booking.getBookingId(),
            booking.getBookingRef(),
            booking.getCustomerId(),
            booking.getVehicleId(),
            booking.getSlotId(),
            booking.getDurationOfBooking(),
            booking.getStatusText(),
            booking.getFormattedBookingTime(),
            booking.getRemarks() != null ? booking.getRemarks() : "None",
            booking.getFormattedAmount()
        );

        JOptionPane.showMessageDialog(this, details, "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelBooking() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("No Selection", "Please select a booking first");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Booking booking = bookingList.get(modelRow);
        
        // Check if booking can be cancelled
        if (!booking.canBeCancelled()) {
            showWarning("Cannot Cancel", 
                "This booking cannot be cancelled. Only pending or approved bookings can be cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format("Cancel booking #%s?\nSlot: %d, Status: %s",
                booking.getBookingRef(), booking.getSlotId(), booking.getStatusText()),
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Update booking status to cancelled
                bookingDAO.updateBookingStatus(booking.getBookingId(), Booking.STATUS_CANCELLED);
                
                // If booking was approved, also free the slot
                if (booking.isApproved()) {
                    // You might need to add a method to free the slot
                    // This depends on your ParkingSlotDAO implementation
                }
                
                showSuccess("Success", "Booking cancelled successfully!");
                loadMyBookings();
            } catch (SQLException e) {
                showError("Database Error", "Failed to cancel booking: " + e.getMessage());
            }
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}