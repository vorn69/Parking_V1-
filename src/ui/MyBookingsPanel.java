package ui;

import dao.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.*;

public class MyBookingsPanel extends JPanel {

    private Integer customerId;
    private Integer ownerId;
    private BookingDAO bookingDAO;
    private VehicleDAO vehicleDAO;
    private PaymentDAO paymentDAO;
    private ParkingSlotDAO slotDAO;

    private List<Booking> bookingList;
    private List<Vehicle> customerVehicles;

    private JTable bookingTable;
    private DefaultTableModel tableModel;

    public MyBookingsPanel(Integer customerId, Integer ownerId) {
        this.customerId = customerId;
        this.ownerId = ownerId;
        this.bookingDAO = new BookingDAO();
        this.vehicleDAO = new VehicleDAO();
        this.paymentDAO = new PaymentDAO();
        this.slotDAO = new ParkingSlotDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 242, 245)); // Modern light background
        setBorder(new EmptyBorder(30, 30, 30, 30));

        loadCustomerVehicles();

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadBookings();
    }

    private void loadCustomerVehicles() {
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                if (ownerId != null && ownerId > 0) {
                    return vehicleDAO.findByVehicleOwnerId(ownerId);
                }
                return java.util.Collections.emptyList();
            }

            @Override
            protected void done() {
                try {
                    customerVehicles = get();
                } catch (Exception e) {
                    // Fail silently or log
                }
            }
        };
        worker.execute();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("My Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));

        JLabel subtitle = new JLabel("View and manage your parking reservations");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(127, 140, 141));

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton searchSlotsBtn = createStyledButton("Find Slots 🔍", new Color(52, 152, 219));
        searchSlotsBtn.addActionListener(e -> searchAvailableSlots());

        JButton newBookingBtn = createStyledButton("New Booking +", new Color(46, 204, 113));
        newBookingBtn.addActionListener(e -> newBooking());

        JButton refreshBtn = createStyledButton("Refresh 🔄", new Color(149, 165, 166));
        refreshBtn.addActionListener(e -> loadBookings());

        buttonPanel.add(searchSlotsBtn);
        buttonPanel.add(newBookingBtn);
        buttonPanel.add(refreshBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Modern Card styling
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 0, 0, 0),
                new LineBorder(new Color(230, 230, 230), 1, true)));

        String[] columns = { "ID", "Vehicle", "Slot", "Duration", "Status", "Payment", "Actions" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Actions handled via mouse click
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(50);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bookingTable.setShowVerticalLines(false);
        bookingTable.setIntercellSpacing(new Dimension(0, 0));
        bookingTable.setFillsViewportHeight(true);
        bookingTable.setSelectionBackground(new Color(232, 240, 254));
        bookingTable.setSelectionForeground(Color.BLACK);

        // Header styling
        JTableHeader header = bookingTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(44, 62, 80));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        // Custom Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Apply styling to all cells
        bookingTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected)
                    c.setBackground(Color.WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 10));

                // Center specific columns
                if (column == 0 || column == 2 || column == 3) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }

                return c;
            }
        });

        // Status Column Renderer
        bookingTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        // Action listener
        bookingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = bookingTable.rowAtPoint(e.getPoint());
                int col = bookingTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6 && bookingList != null && row < bookingList.size()) {
                    Booking booking = bookingList.get(row);
                    showBookingMenu(booking, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));
        return btn;
    }

    private void loadBookings() {
        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                try {
                    if (ownerId != null && ownerId > 0) {
                        return bookingDAO.findByCustomerId(ownerId);
                    }
                    return java.util.Collections.emptyList();
                } catch (Exception e) {
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    bookingList = get();
                    updateBookingTable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateBookingTable() {
        tableModel.setRowCount(0);
        if (bookingList == null || bookingList.isEmpty()) {
            return;
        }

        for (Booking booking : bookingList) {
            String vehicleInfo = "Unknown";
            try {
                Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                if (vehicle != null)
                    vehicleInfo = vehicle.getVehiclePlateNumber();
            } catch (SQLException e) {
            }

            String slotInfo = "Slot " + booking.getSlotId();
            String statusText = getStatusText(booking.getBookingStatus());

            String paymentStatus = "Unpaid";
            try {
                Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
                if (payment != null) {
                    if (payment.getPaymentStatus() == 1)
                        paymentStatus = "Paid";
                    else if (payment.getPaidAmount() > 0)
                        paymentStatus = "Partial";
                }
            } catch (SQLException e) {
            }

            tableModel.addRow(new Object[] {
                    booking.getBookingId(),
                    vehicleInfo,
                    slotInfo,
                    booking.getDurationOfBooking(),
                    statusText,
                    paymentStatus,
                    "••• Options" // Simpler action text
            });
        }
    }

    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 0:
                return "Pending";
            case 1:
                return "Approved";
            case 2:
                return "Rejected";
            case 3:
                return "Completed";
            default:
                return "Unknown";
        }
    }

    // Helper Class for Status Pill Styling
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            String status = (String) value;

            label.setText(status);
            label.setHorizontalAlignment(CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            if (status.contains("Pending") || status.contains("Unpaid")) {
                label.setForeground(new Color(243, 156, 18));
            } else if (status.contains("Approved") || status.contains("Paid") || status.contains("Completed")) {
                label.setForeground(new Color(39, 174, 96));
            } else if (status.contains("Rejected") || status.contains("Cancelled")) {
                label.setForeground(new Color(192, 57, 43));
            } else {
                label.setForeground(Color.GRAY);
            }

            if (!isSelected)
                label.setBackground(Color.WHITE);

            return label;
        }
    }

    // --- Actions (Keep existing logic roughly same but simplified dialogs) ---

    private void showBookingMenu(Booking booking, int row) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JMenuItem paymentItem = new JMenuItem("Make Payment");
        paymentItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JMenuItem cancelItem = new JMenuItem("Cancel Booking");
        cancelItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelItem.setForeground(new Color(192, 57, 43));

        viewItem.addActionListener(e -> viewBookingDetails(booking));
        paymentItem.addActionListener(e -> makePayment(booking));
        cancelItem.addActionListener(e -> cancelBooking(booking, row));

        if (booking.getBookingStatus() != 0)
            paymentItem.setEnabled(false);

        popupMenu.add(viewItem);
        popupMenu.addSeparator();
        popupMenu.add(paymentItem);
        popupMenu.add(cancelItem);

        // Show under mouse
        Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, bookingTable);
        popupMenu.show(bookingTable, p.x, p.y);
    }

    private void searchAvailableSlots() {
        new SwingWorker<List<ParkingSlot>, Void>() {
            @Override
            protected List<ParkingSlot> doInBackground() throws Exception {
                return slotDAO.findAvailableSlots();
            }

            @Override
            protected void done() {
                try {
                    showAvailableSlotsDialog(get());
                } catch (Exception e) {
                }
            }
        }.execute();
    }

    private void showAvailableSlotsDialog(List<ParkingSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No slots available.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Available Slots", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel grid = new JPanel(new GridLayout(0, 4, 10, 10));
        grid.setBackground(Color.WHITE);
        grid.setBorder(new EmptyBorder(20, 20, 20, 20));

        for (ParkingSlot slot : slots) {
            JButton btn = new JButton("#" + slot.getParkingSlotNumber());
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(44, 62, 80)); // Dark blue-grey text
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));

            // Add a border to make it look like a slot
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(52, 152, 219), 2),
                    new EmptyBorder(5, 5, 5, 5)));

            btn.setPreferredSize(new Dimension(80, 60));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                bookSelectedSlot(slot);
                dialog.dispose();
            });
            grid.add(btn);
        }

        content.add(new JScrollPane(grid), BorderLayout.CENTER);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void bookSelectedSlot(ParkingSlot slot) {
        if (customerVehicles == null || customerVehicles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add a vehicle first!");
            return;
        }

        // Simplified booking dialog
        Vehicle v = (Vehicle) JOptionPane.showInputDialog(this, "Select Vehicle:",
                "Booking Slot " + slot.getParkingSlotNumber(),
                JOptionPane.QUESTION_MESSAGE, null, customerVehicles.toArray(), customerVehicles.get(0));

        if (v != null) {
            String[] durations = { "1 hour", "2 hours", "4 hours", "1 day" };
            String d = (String) JOptionPane.showInputDialog(this, "Select Duration:",
                    "Booking", JOptionPane.QUESTION_MESSAGE, null, durations, durations[0]);

            if (d != null) {
                // Create booking logic...
                createBooking(v, slot, d);
            }
        }
    }

    private void createBooking(Vehicle vehicle, ParkingSlot slot, String duration) {
        try {
            Booking b = new Booking();
            b.setCustomerId(ownerId != null ? ownerId : customerId);
            b.setVehicleId(vehicle.getVehicleId());
            b.setSlotId(slot.getParkingSlotId());
            b.setDurationOfBooking(duration);
            b.setBookingStatus(0);
            b.setUserId(customerId);
            b.setBookingTime(new Timestamp(System.currentTimeMillis()));

            if (bookingDAO.createBookingWithSlotUpdate(b) > 0) {
                JOptionPane.showMessageDialog(this, "Booking Created!");
                loadBookings();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newBooking() {
        searchAvailableSlots();
    }

    // Minimal stub for viewing details - improved readability
    private void viewBookingDetails(Booking b) {
        String msg = "Booking ID: " + b.getBookingId() + "\n" +
                "Status: " + getStatusText(b.getBookingStatus()) + "\n" +
                "Duration: " + b.getDurationOfBooking();
        JOptionPane.showMessageDialog(this, msg);
    }

    // Minimal stub for payment
    private void makePayment(Booking b) {
        String amount = JOptionPane.showInputDialog(this, "Enter Payment Amount:");
        if (amount != null) {
            try {
                Payment p = paymentDAO.findByBookingId(b.getBookingId());
                if (p != null) {
                    p.setPaidAmount(p.getPaidAmount() + Double.parseDouble(amount));
                    if (p.getPaidAmount() >= p.getDueAmount())
                        p.setPaymentStatus(1);
                    paymentDAO.update(p);
                    JOptionPane.showMessageDialog(this, "Payment Successful!");
                    loadBookings();
                } else {
                    JOptionPane.showMessageDialog(this, "No payment record found");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelBooking(Booking b, int row) {
        if (JOptionPane.showConfirmDialog(this, "Cancel booking?") == JOptionPane.YES_OPTION) {
            try {
                bookingDAO.delete(b.getBookingId());
                loadBookings();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}