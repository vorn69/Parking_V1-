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
        
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        
        loadCustomerVehicles();
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
                    JOptionPane.showMessageDialog(MyBookingsPanel.this,
                        "Error loading vehicles: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("📅 My Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(52, 73, 94));
        header.add(title, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton searchSlotsBtn = new JButton("🔍 Find Available Slots");
        searchSlotsBtn.setBackground(new Color(52, 152, 219));
        searchSlotsBtn.setForeground(Color.WHITE);
        searchSlotsBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchSlotsBtn.setFocusPainted(false);
        searchSlotsBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        searchSlotsBtn.addActionListener(e -> searchAvailableSlots());
        
        JButton newBookingBtn = new JButton("➕ New Booking");
        newBookingBtn.setBackground(new Color(46, 204, 113));
        newBookingBtn.setForeground(Color.WHITE);
        newBookingBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        newBookingBtn.setFocusPainted(false);
        newBookingBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        newBookingBtn.addActionListener(e -> newBooking());
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(241, 196, 15));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
        refreshBtn.addActionListener(e -> loadBookings());
        
        buttonPanel.add(searchSlotsBtn);
        buttonPanel.add(newBookingBtn);
        buttonPanel.add(refreshBtn);
        
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Booking table
        JPanel tablePanel = createBookingTable();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createBookingTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        String[] columns = {"ID", "Vehicle", "Slot", "Duration", "Status", "Payment", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(40);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Set column widths
        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        bookingTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        bookingTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        bookingTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        bookingTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        bookingTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        bookingTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        // Center align
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < bookingTable.getColumnCount(); i++) {
            bookingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Custom renderer for status
        bookingTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        
        // Action listener for action buttons
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
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadBookings() {
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                try {
                    // Using your exact method: findByCustomerId()
                    if (ownerId != null && ownerId > 0) {
                        return bookingDAO.findByCustomerId(ownerId);
                    }
                    return java.util.Collections.emptyList();
                } catch (Exception e) {
                    System.out.println("Error loading bookings: " + e.getMessage());
                    return java.util.Collections.emptyList();
                }
            }
            
            @Override
            protected void done() {
                try {
                    bookingList = get();
                    updateBookingTable();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this,
                        "Error loading bookings: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateBookingTable() {
        tableModel.setRowCount(0);
        
        if (bookingList == null || bookingList.isEmpty()) {
            tableModel.addRow(new Object[]{
                "", "No bookings yet", "", "", "", "", "Click 'New Booking' to create one"
            });
            return;
        }
        
        for (Booking booking : bookingList) {
            // Get vehicle info
            String vehicleInfo = "N/A";
            try {
                Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                if (vehicle != null) {
                    vehicleInfo = vehicle.getVehiclePlateNumber();
                }
            } catch (SQLException e) {
                vehicleInfo = "Error";
            }
            
            // Get slot info
            String slotInfo = "Slot " + booking.getSlotId();
            
            // Get status text
            String statusText = getStatusText(booking.getBookingStatus());
            
            // Get payment info
            String paymentStatus = "❌ Not Paid";
            try {
                Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
                if (payment != null) {
                    int paymentStatusValue = payment.getPaymentStatus();
                    if (paymentStatusValue == 1) {
                        paymentStatus = "✅ Paid";
                    } else if (payment.getPaidAmount() > 0) {
                        paymentStatus = "💰 Partial";
                    }
                }
            } catch (SQLException e) {
                paymentStatus = "❓ Error";
            }
            
            tableModel.addRow(new Object[]{
                booking.getBookingId(),
                vehicleInfo,
                slotInfo,
                booking.getDurationOfBooking(),
                statusText,
                paymentStatus,
                "[Actions]"
            });
        }
    }
    
    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 0: return "⏳ Pending";
            case 1: return "✅ Approved";
            case 2: return "❌ Rejected";
            case 3: return "✅ Completed";
            default: return "❓ Unknown";
        }
    }
    
    private void showBookingMenu(Booking booking, int row) {
        // Create popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem viewItem = new JMenuItem("👁️ View Details");
        JMenuItem paymentItem = new JMenuItem("💰 Make Payment");
        JMenuItem cancelItem = new JMenuItem("❌ Cancel Booking");
        
        viewItem.addActionListener(e -> viewBookingDetails(booking));
        paymentItem.addActionListener(e -> makePayment(booking));
        cancelItem.addActionListener(e -> cancelBooking(booking, row));
        
        // Disable payment if booking is not pending
        if (booking.getBookingStatus() != 0) { // Not pending
            paymentItem.setEnabled(false);
        }
        
        popupMenu.add(viewItem);
        popupMenu.addSeparator();
        popupMenu.add(paymentItem);
        popupMenu.add(cancelItem);
        
        // Show menu at mouse position
        Point p = bookingTable.getMousePosition();
        if (p != null) {
            popupMenu.show(bookingTable, p.x, p.y);
        }
    }
    
    private void searchAvailableSlots() {
        SwingWorker<List<ParkingSlot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ParkingSlot> doInBackground() throws Exception {
                return slotDAO.findAvailableSlots();
            }
            
            @Override
            protected void done() {
                try {
                    List<ParkingSlot> slots = get();
                    showAvailableSlotsDialog(slots);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this,
                        "Error finding slots: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showAvailableSlotsDialog(List<ParkingSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No available parking slots at the moment.",
                "No Slots Available",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                   "Available Parking Slots", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("🅿️ Available Parking Slots (" + slots.size() + ")", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(52, 152, 219));
        
        // Create grid of slots
        JPanel slotsPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        slotsPanel.setBackground(Color.WHITE);
        
        for (ParkingSlot slot : slots) {
            JButton slotBtn = createSlotButton(slot);
            slotsPanel.add(slotBtn);
        }
        
        JScrollPane scrollPane = new JScrollPane(slotsPanel);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        
        content.add(title, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(closeBtn, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }
    
    private JButton createSlotButton(ParkingSlot slot) {
        JButton button = new JButton("🅿️ Slot " + slot.getParkingSlotNumber());
        button.setBackground(new Color(46, 204, 113));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 60));
        button.setToolTipText("Click to book this slot");
        
        button.addActionListener(e -> {
            bookSelectedSlot(slot);
            ((Window) button.getTopLevelAncestor()).dispose();
        });
        
        return button;
    }
    
    private void bookSelectedSlot(ParkingSlot slot) {
        // Check if customer has vehicles
        if (customerVehicles == null || customerVehicles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "You need to register a vehicle first!\nGo to 'My Vehicles' to add one.",
                "No Vehicles",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show vehicle selection dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                   "Book Slot " + slot.getParkingSlotNumber(), true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Book Parking Slot " + slot.getParkingSlotNumber(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        
        formPanel.add(new JLabel("Select Vehicle:"));
        JComboBox<Vehicle> vehicleCombo = new JComboBox<>();
        for (Vehicle vehicle : customerVehicles) {
            vehicleCombo.addItem(vehicle);
        }
        formPanel.add(vehicleCombo);
        
        formPanel.add(new JLabel("Duration:"));
        JComboBox<String> durationCombo = new JComboBox<>(new String[]{
            "1 hour", "2 hours", "3 hours", "4 hours", 
            "5 hours", "6 hours", "1 day", "2 days"
        });
        formPanel.add(durationCombo);
        
        formPanel.add(new JLabel("Estimated Cost:"));
        JLabel costLabel = new JLabel("$5.00");
        costLabel.setForeground(new Color(46, 204, 113));
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(costLabel);
        
        // Update cost when duration changes
        durationCombo.addActionListener(e -> {
            String duration = (String) durationCombo.getSelectedItem();
            costLabel.setText("$" + calculateCost(duration));
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton bookBtn = new JButton("📅 Book Now");
        bookBtn.setBackground(new Color(46, 204, 113));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.addActionListener(e -> {
            Vehicle selectedVehicle = (Vehicle) vehicleCombo.getSelectedItem();
            String duration = (String) durationCombo.getSelectedItem();
            createBooking(selectedVehicle, slot, duration, dialog);
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(bookBtn);
        buttonPanel.add(cancelBtn);
        
        content.add(title, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }
    
    private String calculateCost(String duration) {
        if (duration.contains("hour")) {
            int hours = Integer.parseInt(duration.split(" ")[0]);
            return String.valueOf(hours * 5);
        } else if (duration.contains("day")) {
            int days = Integer.parseInt(duration.split(" ")[0]);
            return String.valueOf(days * 50);
        }
        return "0";
    }
    
    private void createBooking(Vehicle vehicle, ParkingSlot slot, String duration, JDialog dialog) {
        try {
            // Create booking object
            Booking booking = new Booking();
            booking.setCustomerId(ownerId != null ? ownerId : customerId);
            booking.setVehicleId(vehicle.getVehicleId());
            booking.setSlotId(slot.getParkingSlotId());
            booking.setDurationOfBooking(duration);
            booking.setBookingStatus(0); // Pending
            booking.setUserId(customerId);
            booking.setRemarks("Booking from customer portal");
            booking.setBookingTime(new Timestamp(System.currentTimeMillis()));
            
            // Use your exact method: createBookingWithSlotUpdate()
            int bookingId = bookingDAO.createBookingWithSlotUpdate(booking);
            
            if (bookingId > 0) {
                // Show success message
                JOptionPane.showMessageDialog(dialog,
                    "✅ Booking created successfully!\n" +
                    "Booking ID: " + bookingId + "\n" +
                    "Slot: " + slot.getParkingSlotNumber() + "\n" +
                    "Status: Pending (Make payment to confirm)",
                    "Booking Created",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dialog.dispose();
                loadBookings(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to create booking. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Error creating booking: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void newBooking() {
        // Start with searching available slots
        searchAvailableSlots();
    }
    
    private void viewBookingDetails(Booking booking) {
        try {
            Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
            ParkingSlot slot = slotDAO.findById(booking.getSlotId());
            Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
            
            String details = "📋 Booking Details\n" +
                           "────────────────\n" +
                           "ID: " + booking.getBookingId() + "\n" +
                           "Vehicle: " + (vehicle != null ? vehicle.getVehiclePlateNumber() : "N/A") + "\n" +
                           "Slot: " + (slot != null ? "Slot " + slot.getParkingSlotNumber() : "N/A") + "\n" +
                           "Duration: " + booking.getDurationOfBooking() + "\n" +
                           "Status: " + getStatusText(booking.getBookingStatus()) + "\n";
            
            if (payment != null) {
                details += "Amount Due: $" + String.format("%.2f", payment.getDueAmount()) + "\n" +
                          "Amount Paid: $" + String.format("%.2f", payment.getPaidAmount()) + "\n" +
                          "Payment Status: " + (payment.getPaymentStatus() == 1 ? "✅ Paid" : 
                                               payment.getPaidAmount() > 0 ? "💰 Partial" : "❌ Pending");
            } else {
                details += "Payment: N/A";
            }
            
            // Add booking time if available
            if (booking.getBookingTime() != null) {
                details += "\nBooking Time: " + booking.getBookingTime().toString();
            }
            
            JOptionPane.showMessageDialog(this, details, "Booking Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void makePayment(Booking booking) {
        try {
            Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
            
            if (payment == null) {
                JOptionPane.showMessageDialog(this, "Payment record not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amountDue = payment.getDueAmount();
            double amountPaid = payment.getPaidAmount();
            double remaining = amountDue - amountPaid;
            
            if (remaining <= 0) {
                JOptionPane.showMessageDialog(this, "Payment already completed", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String amountStr = JOptionPane.showInputDialog(this,
                "Make Payment\n" +
                "────────────\n" +
                "Booking ID: " + booking.getBookingId() + "\n" +
                "Amount Due: $" + String.format("%.2f", amountDue) + "\n" +
                "Amount Paid: $" + String.format("%.2f", amountPaid) + "\n" +
                "Remaining: $" + String.format("%.2f", remaining) + "\n\n" +
                "Enter payment amount:",
                "Payment",
                JOptionPane.QUESTION_MESSAGE);
            
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                try {
                    double paymentAmount = Double.parseDouble(amountStr);
                    
                    if (paymentAmount <= 0) {
                        JOptionPane.showMessageDialog(this, "Amount must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (paymentAmount > remaining) {
                        paymentAmount = remaining; // Don't overpay
                    }
                    
                    // Update payment
                    double newPaid = amountPaid + paymentAmount;
                    payment.setPaidAmount(newPaid);
                    
                    if (newPaid >= amountDue) {
                        payment.setPaymentStatus(1); // Fully paid
                    } else {
                        payment.setPaymentStatus(2); // Partial payment
                    }
                    
                    paymentDAO.update(payment);
                    
                    JOptionPane.showMessageDialog(this,
                        "✅ Payment processed!\n" +
                        "Amount: $" + String.format("%.2f", paymentAmount) + "\n" +
                        "Total Paid: $" + String.format("%.2f", newPaid) + "\n" +
                        "Remaining: $" + String.format("%.2f", amountDue - newPaid),
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadBookings(); // Refresh
                    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error processing payment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelBooking(Booking booking, int row) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this booking?\n\n" +
            "Booking #" + booking.getBookingId() + "\n" +
            "Note: Partial payments may not be refunded.",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Use your exact method: delete()
                boolean success = bookingDAO.delete(booking.getBookingId());
                
                if (success) {
                    // Update table
                    tableModel.setValueAt("❌ Cancelled", row, 4);
                    JOptionPane.showMessageDialog(this,
                        "Booking cancelled successfully.",
                        "Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    loadBookings(); // Refresh
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to cancel booking.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error cancelling booking: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Custom table cell renderer for status
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String status = (String) value;
                if (status.contains("Pending")) {
                    c.setForeground(new Color(241, 196, 15));
                } else if (status.contains("Approved") || status.contains("Completed")) {
                    c.setForeground(new Color(46, 204, 113));
                } else if (status.contains("Rejected") || status.contains("Cancelled")) {
                    c.setForeground(new Color(231, 76, 60));
                }
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(CENTER);
            }
            return c;
        }
    }
}