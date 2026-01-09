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

    private JTextField searchField;
    private JComboBox<String> statusFilter;
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

    // ---------- Header Panel ----------
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);

        JLabel titleLabel = new JLabel("BOOKING MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(MAIN_BG);

        JButton refreshButton = createActionButton("⟳ REFRESH", new Color(0, 122, 255));
        refreshButton.addActionListener(e -> refreshTable());
        buttonPanel.add(refreshButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    // ---------- Table Panel ----------
    private void createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_BG);

        String[] columns = {"Booking ID", "Customer ID", "Vehicle Plate", "Duration", "Slot", "Status", "Remarks"};
        bookingTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingTable = new JTable(bookingTableModel);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bookingTable.setRowHeight(35);
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookingTable.getTableHeader().setBackground(TABLE_HEADER);
        bookingTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    // ---------- Info Panel ----------
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

    private void updateInfoPanel() {
        if (bookingData == null) return;

        int checkedIn = (int) bookingData.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_CHECKED_IN).count();
        int checkedOut = (int) bookingData.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_CHECKED_OUT).count();
        int pending = (int) bookingData.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count();

        infoLabel.setText("📊 Total: " + bookingData.size() +
                " | 🟢 Checked In: " + checkedIn +
                " | 🔵 Checked Out: " + checkedOut +
                " | 🟡 Pending: " + pending);

        timestampLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void refreshTable() {
        try {
            bookingData = bookingDAO.findAll();
            bookingTableModel.setRowCount(0);

            for (Booking b : bookingData) {
                // FIX: Use userId and vehiclePlateNumber directly
                String vehiclePlate = b.getVehicle() != null ? b.getVehicle().getVehiclePlateNumber() : "-";

                bookingTableModel.addRow(new Object[]{
                        b.getBookingId(),
                        b.getUserId() != null ? b.getUserId() : "-",
                        vehiclePlate,
                        b.getDurationOfBooking(),
                        b.getParkingSlot() != null ? b.getParkingSlot().getSlotNumber() : "-",
                        b.getBookingStatus(),
                        b.getRemarks()
                });
            }
            updateInfoPanel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
