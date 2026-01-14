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

    public UserMyBookingsPanel(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        model = new DefaultTableModel(
            new String[]{"Booking ID", "Slot ID", "Duration", "Status"},
            0
        );

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadMyBookings();
    }

    private void loadMyBookings() {
        try {
            model.setRowCount(0);
            List<Booking> list = bookingDAO.findByUserId(userId);
            for (Booking b : list) {
                model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getSlotId(),
                    b.getDurationOfBooking(),
                    statusText(b.getBookingStatus())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings");
        }
    }

    private String statusText(int s) {
        return switch (s) {
            case Booking.STATUS_PENDING -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            default -> "UNKNOWN";
        };
    }
}
