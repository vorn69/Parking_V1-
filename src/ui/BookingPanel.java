package ui;

import dao.BookingDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Booking;

public class BookingPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAO();
    private List<Booking> bookingList;

    private JTable table;
    private DefaultTableModel model;

    private JLabel infoLabel;
    private JLabel timeLabel;

    // TEMP: replace later with real logged-in admin id
    private final int ADMIN_USER_ID = 1;

    public BookingPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadData();
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("BOOKING MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton approveBtn = createButton("APPROVE", new Color(76, 175, 80));
        JButton rejectBtn  = createButton("REJECT", new Color(244, 67, 54));
        JButton refreshBtn = createButton("REFRESH", new Color(33, 150, 243));

        approveBtn.addActionListener(e -> approveBooking());
        rejectBtn.addActionListener(e -> rejectBooking());
        refreshBtn.addActionListener(e -> loadData());

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    // ================= TABLE =================
    private JScrollPane createTable() {
        model = new DefaultTableModel(
            new String[]{"Booking ID", "Customer ID", "Slot ID", "Duration", "Status"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        return new JScrollPane(table);
    }

    // ================= FOOTER =================
    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);

        timeLabel = new JLabel();
        timeLabel.setForeground(Color.LIGHT_GRAY);

        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);
        return panel;
    }

    // ================= LOAD DATA =================
    private void loadData() {
        try {
            bookingList = bookingDAO.findPendingBookings();
            model.setRowCount(0);

            for (Booking b : bookingList) {
                model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getCustomerId(),
                    b.getSlotId(),
                    b.getDurationOfBooking(),
                    statusText(b.getBookingStatus())
                });
            }

            updateFooter();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFooter() {
        long pending = bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING)
                .count();

        infoLabel.setText("Total: " + bookingList.size() + " | Pending: " + pending);
        timeLabel.setText("Updated: " +
                new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private String statusText(int status) {
        return switch (status) {
            case Booking.STATUS_PENDING  -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            default -> "UNKNOWN";
        };
    }

    // ================= ACTIONS =================
    private void approveBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking first");
            return;
        }

        Booking b = bookingList.get(row);
        if (b.getBookingStatus() != Booking.STATUS_PENDING) {
            JOptionPane.showMessageDialog(this, "Only pending bookings can be approved");
            return;
        }

        try {
            bookingDAO.approveBooking(
                b.getBookingId(),
                ADMIN_USER_ID
            );
            loadData();
            JOptionPane.showMessageDialog(this, "Booking approved & payment created");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking first");
            return;
        }

        Booking b = bookingList.get(row);
        if (b.getBookingStatus() != Booking.STATUS_PENDING) {
            JOptionPane.showMessageDialog(this, "Only pending bookings can be rejected");
            return;
        }

        try {
            bookingDAO.rejectBooking(b.getBookingId(), b.getSlotId());
            loadData();
            JOptionPane.showMessageDialog(this, "Booking rejected & slot freed");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
