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

    private BookingDAO bookingDAO = new BookingDAO();
    private List<Booking> bookingData;

    private DefaultTableModel bookingTableModel;
    private JTable bookingTable;

    private JLabel infoLabel;
    private JLabel timestampLabel;

    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TABLE_HEADER = new Color(60, 64, 72);
    private final Color BOTTOM_BG = Color.BLACK;
    private final Color BOTTOM_TEXT = Color.WHITE;

    public BookingPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createTablePanel();
        createInfoPanel();
        refreshTable();
    }

    // ================= HEADER =================
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);

        JLabel titleLabel = new JLabel("BOOKING MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(MAIN_BG);

        JButton addBtn = createButton("+ ADD BOOKING", new Color(76, 175, 80));
        addBtn.addActionListener(e -> showAddBookingDialog());

        JButton refreshBtn = createButton("⟳ REFRESH", new Color(33, 150, 243));
        refreshBtn.addActionListener(e -> refreshTable());

        buttonPanel.add(addBtn);
        buttonPanel.add(refreshBtn);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
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
    private void createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);

        bookingTableModel = new DefaultTableModel(
            new String[]{"ID", "Customer ID", "Vehicle ID", "Slot ID", "Duration", "Status", "Remarks"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(30);
        bookingTable.getTableHeader().setBackground(TABLE_HEADER);
        bookingTable.getTableHeader().setForeground(Color.WHITE);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        add(panel, BorderLayout.CENTER);
    }

    // ================= INFO =================
    private void createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BOTTOM_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(BOTTOM_TEXT);

        timestampLabel = new JLabel();
        timestampLabel.setForeground(Color.LIGHT_GRAY);

        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(timestampLabel, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);
    }

    // ================= LOAD DATA =================
    private void refreshTable() {
        try {
            bookingData = bookingDAO.findAll();
            bookingTableModel.setRowCount(0);

            for (Booking b : bookingData) {
                bookingTableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getCustomerId(),
                    b.getVehicleId(),
                    b.getSlotId(),
                    b.getDurationOfBooking(),
                    b.getBookingStatus(),
                    b.getRemarks()
                });
            }

            updateInfoPanel();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateInfoPanel() {
        if (bookingData == null) return;

        long pending = bookingData.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count();
        long in = bookingData.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_CHECKED_IN).count();
        long out = bookingData.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_CHECKED_OUT).count();

        infoLabel.setText("Total: " + bookingData.size()
                + " | Pending: " + pending
                + " | In: " + in
                + " | Out: " + out);

        timestampLabel.setText("Updated: " +
                new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    // ================= ADD BOOKING =================
    private void showAddBookingDialog() {
        JTextField customerId = new JTextField();
        JTextField vehicleId = new JTextField();
        JTextField slotId = new JTextField();
        JTextField duration = new JTextField();
        JTextField remarks = new JTextField();

        Object[] fields = {
            "Customer ID:", customerId,
            "Vehicle ID:", vehicleId,
            "Slot ID:", slotId,
            "Duration:", duration,
            "Remarks:", remarks
        };

        int opt = JOptionPane.showConfirmDialog(
            this, fields, "Add Booking", JOptionPane.OK_CANCEL_OPTION
        );

        if (opt == JOptionPane.OK_OPTION) {
            try {
                Booking b = new Booking();
                b.setCustomerId(Integer.parseInt(customerId.getText()));
                b.setVehicleId(Integer.parseInt(vehicleId.getText()));
                b.setSlotId(Integer.parseInt(slotId.getText()));
                b.setDurationOfBooking(duration.getText());
                b.setRemarks(remarks.getText());
                b.setBookingStatus(Booking.STATUS_PENDING);
                b.setBookingTime(new java.sql.Timestamp(System.currentTimeMillis()));

                bookingDAO.create(b); // ✅ INSERT TO DB
                refreshTable();

                JOptionPane.showMessageDialog(this, "Booking added successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
