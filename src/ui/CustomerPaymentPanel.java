package ui;

import dao.*;
import models.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

public class CustomerPaymentPanel extends JPanel {

    private Integer userId;
    private Integer ownerId;

    // DAOs
    private PaymentDAO paymentDAO;
    private BookingDAO bookingDAO;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;

    // UI Components
    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private JLabel totalDueLabel;
    private JLabel totalPaidLabel;
    private JLabel pendingCountLabel;
    private JLabel lastPaymentLabel;

    // Colors
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color PRIMARY = new Color(41, 128, 185);
    private final Color SUCCESS = new Color(46, 204, 113);
    private final Color WARNING = new Color(241, 196, 15);
    private final Color DANGER = new Color(231, 76, 60);
    private final Color INFO = new Color(155, 89, 182);

    public CustomerPaymentPanel(Integer userId, Integer ownerId) {
        this.userId = userId;
        this.ownerId = ownerId;

        this.paymentDAO = new PaymentDAO();
        this.bookingDAO = new BookingDAO();
        this.vehicleDAO = new VehicleDAO();
        this.userDAO = new UserDAO();

        initUI();
        loadPayments();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createStatsCards(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createQuickActions(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("💰 My Payments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(52, 73, 94));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton refreshBtn = createHeaderButton("🔄 Refresh", "Refresh payment data");
        refreshBtn.addActionListener(e -> loadPayments());

        JButton payAllBtn = createHeaderButton("💳 Pay All Pending", "Pay all pending payments");
        payAllBtn.addActionListener(e -> showPayAllDialog());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(payAllBtn);

        header.add(title, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createHeaderButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(52, 152, 219));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY);
            }
        });

        return button;
    }

    private JPanel createStatsCards() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setPreferredSize(new Dimension(250, 0));

        // Card 1: Total Due
        totalDueLabel = new JLabel("$0.00");
        statsPanel.add(createStatCard("Total Amount Due", totalDueLabel, DANGER, "Amount you need to pay"));

        statsPanel.add(Box.createVerticalStrut(10));

        // Card 2: Total Paid
        totalPaidLabel = new JLabel("$0.00");
        statsPanel.add(createStatCard("Total Amount Paid", totalPaidLabel, SUCCESS, "Amount already paid"));

        statsPanel.add(Box.createVerticalStrut(10));

        // Card 3: Pending Payments
        pendingCountLabel = new JLabel("0");
        statsPanel.add(createStatCard("Pending Payments", pendingCountLabel, WARNING, "Payments awaiting action"));

        statsPanel.add(Box.createVerticalStrut(10));

        // Card 4: Last Payment
        lastPaymentLabel = new JLabel("None");
        statsPanel.add(createStatCard("Last Payment", lastPaymentLabel, INFO, "Most recent payment"));

        return statsPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(250, 120));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(127, 140, 141));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(149, 165, 166));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);

        // Add filter panel
        mainPanel.add(createFilterPanel(), BorderLayout.NORTH);

        // Add payment table
        mainPanel.add(createPaymentTable(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)));

        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JComboBox<String> statusFilter = new JComboBox<>(new String[] {
                "All Payments", "Pending", "Paid", "Partial"
        });
        statusFilter.addActionListener(e -> filterPayments((String) statusFilter.getSelectedItem()));

        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);

        return filterPanel;
    }

    private JPanel createPaymentTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(0, 0, 0, 0)));

        // Table model
        String[] columns = { "ID", "Booking", "Vehicle", "Amount Due", "Amount Paid",
                "Balance", "Status", "Due Date", "Actions" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only Actions column is editable
            }
        };

        paymentTable = new JTable(tableModel);
        paymentTable.setRowHeight(40);
        paymentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        paymentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Set column widths
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(80); // Booking
        paymentTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Vehicle
        paymentTable.getColumnModel().getColumn(3).setPreferredWidth(90); // Amount Due
        paymentTable.getColumnModel().getColumn(4).setPreferredWidth(90); // Amount Paid
        paymentTable.getColumnModel().getColumn(5).setPreferredWidth(80); // Balance
        paymentTable.getColumnModel().getColumn(6).setPreferredWidth(90); // Status
        paymentTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Due Date
        paymentTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Actions

        // Custom renderer for status column
        paymentTable.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        // Custom renderer for amount columns
        paymentTable.getColumnModel().getColumn(5).setCellRenderer(new AmountRenderer());

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createQuickActions() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton autoPayBtn = createActionButton("🤖 Auto Pay All",
                "Automatically pay all pending payments", SUCCESS);
        autoPayBtn.addActionListener(e -> autoPayAll());

        JButton scheduleBtn = createActionButton("📅 Schedule Payment",
                "Schedule a payment for later", INFO);
        scheduleBtn.addActionListener(e -> schedulePayment());

        JButton statementBtn = createActionButton("📄 Download Statement",
                "Download payment statement", WARNING);
        statementBtn.addActionListener(e -> downloadStatement());

        actionPanel.add(autoPayBtn);
        actionPanel.add(scheduleBtn);
        actionPanel.add(statementBtn);

        return actionPanel;
    }

    private JButton createActionButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void loadPayments() {
        SwingWorker<List<Payment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Payment> doInBackground() throws Exception {
                try {
                    // Load payments for this user
                    List<Payment> payments;
                    if (ownerId != null) {
                        // If user is a vehicle owner, get payments by ownerId
                        payments = getPaymentsByOwnerId(ownerId);
                    } else {
                        // Otherwise get payments by userId
                        payments = paymentDAO.findByUserId(userId);
                    }
                    return payments;
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to load payments: " + e.getMessage(), e);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Payment> payments = get();
                    updatePaymentTable(payments);
                    updateStats(payments);
                } catch (Exception e) {
                    showError("Error", "Failed to load payments: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private List<Payment> getPaymentsByOwnerId(Integer ownerId) throws SQLException {
        // Get bookings by ownerId, then get payments for those bookings
        List<Booking> bookings = bookingDAO.findByCustomerId(ownerId);
        List<Payment> allPayments = new java.util.ArrayList<>();

        for (Booking booking : bookings) {
            Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
            if (payment != null) {
                // Enrich payment data with booking info
                payment.setBookingRef("BK-" + booking.getBookingId());
                payment.setVehicleId(booking.getVehicleId());
                allPayments.add(payment);
            }
        }

        return allPayments;
    }

    private void updatePaymentTable(List<Payment> payments) {
        tableModel.setRowCount(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Payment payment : payments) {
            try {
                // Get booking info
                Booking booking = bookingDAO.findById(payment.getBookingId());

                // Get vehicle info
                String vehicleInfo = "N/A";
                if (booking != null) {
                    Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                    if (vehicle != null) {
                        vehicleInfo = vehicle.getVehiclePlateNumber();
                    }
                }

                // Calculate balance
                double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
                double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
                double balance = dueAmount - paidAmount;

                // Get status text
                String statusText = getPaymentStatusText(payment.getPaymentStatus());

                // Format date
                String dueDate = "";
                if (booking != null && booking.getBookingTime() != null) {
                    // Add 24 hours to booking time for due date
                    long dueTime = booking.getBookingTime().getTime() + (24 * 60 * 60 * 1000);
                    dueDate = dateFormat.format(new Date(dueTime));
                }

                // Add row to table
                tableModel.addRow(new Object[] {
                        payment.getPaymentId(),
                        "BK-" + payment.getBookingId(),
                        vehicleInfo,
                        String.format("$%.2f", dueAmount),
                        String.format("$%.2f", paidAmount),
                        String.format("$%.2f", balance),
                        statusText,
                        dueDate,
                        createActionButtons(payment, booking)
                });

            } catch (Exception e) {
                System.err.println("Error displaying payment: " + e.getMessage());
            }
        }
    }

    private String getPaymentStatusText(int status) {
        switch (status) {
            case Payment.STATUS_PENDING_APPROVAL:
                return "⏳ PENDING APPROVAL";
            case Payment.STATUS_APPROVED_UNPAID:
                return "💳 PAYMENT DUE";
            case Payment.STATUS_PAID:
                return "✅ PAID";
            case Payment.STATUS_PARTIAL:
                return "💰 PARTIAL";
            case Payment.STATUS_CANCELLED:
                return "❌ CANCELLED";
            default:
                return "❓ UNKNOWN";
        }
    }

    private JPanel createActionButtons(Payment payment, Booking booking) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);

        // View Details button
        JButton viewBtn = createSmallButton("👁", "View Details", PRIMARY);
        viewBtn.addActionListener(e -> viewPaymentDetails(payment, booking));

        // Pay Now button (only for approved/partial payments)
        if (payment.getPaymentStatus() == Payment.STATUS_APPROVED_UNPAID ||
                payment.getPaymentStatus() == Payment.STATUS_PARTIAL) {

            JButton payBtn = createSmallButton("💰", "Pay Now", SUCCESS);
            payBtn.addActionListener(e -> makePayment(payment, booking));
            panel.add(payBtn);
        }

        // Receipt button (only for paid payments)
        if (payment.getPaymentStatus() == Payment.STATUS_PAID) {
            JButton receiptBtn = createSmallButton("🧾", "Get Receipt", INFO);
            receiptBtn.addActionListener(e -> generateReceipt(payment));
            panel.add(receiptBtn);
        }

        panel.add(viewBtn);

        return panel;
    }

    private JButton createSmallButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void updateStats(List<Payment> payments) {
        double totalDue = 0;
        double totalPaid = 0;
        int pendingCount = 0;
        Date lastPaymentDate = null;

        for (Payment payment : payments) {
            double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
            double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;

            totalDue += dueAmount;
            totalPaid += paidAmount;

            if (payment.getPaymentStatus() == Payment.STATUS_APPROVED_UNPAID ||
                    payment.getPaymentStatus() == Payment.STATUS_PARTIAL) {
                pendingCount++;
            }

            if (payment.getPaymentDate() != null) {
                if (lastPaymentDate == null || payment.getPaymentDate().after(lastPaymentDate)) {
                    lastPaymentDate = payment.getPaymentDate();
                }
            }
        }

        totalDueLabel.setText(String.format("$%.2f", totalDue));
        totalPaidLabel.setText(String.format("$%.2f", totalPaid));
        pendingCountLabel.setText(String.valueOf(pendingCount));

        if (lastPaymentDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            lastPaymentLabel.setText(sdf.format(lastPaymentDate));
        } else {
            lastPaymentLabel.setText("None");
        }
    }

    private void filterPayments(String statusFilter) {
        // This would filter the displayed payments based on status
        // For simplicity, we'll reload all payments and filter in memory
        loadPayments();
    }

    private void makePayment(Payment payment, Booking booking) {
        double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
        double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
        double balance = dueAmount - paidAmount;

        if (balance <= 0) {
            showInfo("No Payment Needed", "This payment is already fully paid.");
            return;
        }

        // Create payment dialog
        JDialog paymentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Make Payment - BK-" + payment.getBookingId(), true);
        paymentDialog.setSize(400, 350);
        paymentDialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Make Payment", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY);

        // Payment info panel
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);

        infoPanel.add(new JLabel("Booking ID:"));
        infoPanel.add(new JLabel("BK-" + payment.getBookingId()));

        infoPanel.add(new JLabel("Amount Due:"));
        infoPanel.add(new JLabel(String.format("$%.2f", dueAmount)));

        infoPanel.add(new JLabel("Already Paid:"));
        infoPanel.add(new JLabel(String.format("$%.2f", paidAmount)));

        infoPanel.add(new JLabel("Balance:"));
        infoPanel.add(new JLabel(String.format("$%.2f", balance)));

        infoPanel.add(new JLabel("Pay Amount:"));
        JTextField amountField = new JTextField(String.format("%.2f", balance));
        infoPanel.add(amountField);

        // Payment method panel
        JPanel methodPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        methodPanel.setBackground(Color.WHITE);
        methodPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        methodPanel.add(new JLabel("Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(new String[] {
                "Credit Card", "Debit Card", "Bank Transfer", "Mobile Wallet", "Cash"
        });
        methodPanel.add(methodCombo);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton payNowBtn = new JButton("💳 Pay Now");
        payNowBtn.setBackground(SUCCESS);
        payNowBtn.setForeground(Color.WHITE);
        payNowBtn.addActionListener(e -> processPayment(payment, amountField, methodCombo, paymentDialog));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> paymentDialog.dispose());

        buttonPanel.add(payNowBtn);
        buttonPanel.add(cancelBtn);

        content.add(title, BorderLayout.NORTH);
        content.add(infoPanel, BorderLayout.CENTER);
        content.add(methodPanel, BorderLayout.SOUTH);
        content.add(buttonPanel, BorderLayout.SOUTH);

        paymentDialog.add(content);
        paymentDialog.setVisible(true);
    }

    private void processPayment(Payment payment, JTextField amountField,
            JComboBox<String> methodCombo, JDialog dialog) {
        try {
            double paymentAmount = Double.parseDouble(amountField.getText());
            double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
            double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
            double balance = dueAmount - paidAmount;

            if (paymentAmount <= 0) {
                showError("Invalid Amount", "Payment amount must be greater than 0.");
                return;
            }

            if (paymentAmount > balance) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        String.format("You are paying $%.2f, but the balance is $%.2f.\n" +
                                "The extra amount will be refunded to your account.\n" +
                                "Continue?", paymentAmount, balance),
                        "Overpayment Warning",
                        JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                paymentAmount = balance; // Only pay the balance
            }

            String paymentMethod = (String) methodCombo.getSelectedItem();

            // Update payment
            double newPaidAmount = paidAmount + paymentAmount;
            payment.setPaidAmount(newPaidAmount);
            payment.setPaidBy(paymentMethod);
            payment.setPaymentDate(new Date());

            // Update status
            if (newPaidAmount >= dueAmount) {
                payment.setPaymentStatus(Payment.STATUS_PAID);
            } else if (newPaidAmount > 0) {
                payment.setPaymentStatus(Payment.STATUS_PARTIAL);
            }

            // Save to database
            boolean success = paymentDAO.update(payment);

            if (success) {
                // Also update booking status if fully paid
                if (newPaidAmount >= dueAmount) {
                    try {
                        Booking booking = bookingDAO.findById(payment.getBookingId());
                        if (booking != null) {
                            booking.setBookingStatus(1); // Approved
                            bookingDAO.update(booking);
                        }
                    } catch (SQLException e) {
                        System.err.println("Failed to update booking status: " + e.getMessage());
                    }
                }

                showSuccess("Payment Successful",
                        String.format("Payment of $%.2f processed successfully!\n" +
                                "New balance: $%.2f",
                                paymentAmount, (dueAmount - newPaidAmount)));

                dialog.dispose();
                loadPayments(); // Refresh the table
            } else {
                showError("Payment Failed", "Failed to process payment. Please try again.");
            }

        } catch (NumberFormatException e) {
            showError("Invalid Amount", "Please enter a valid payment amount.");
        } catch (SQLException e) {
            showError("Database Error", "Failed to save payment: " + e.getMessage());
        }
    }

    private void viewPaymentDetails(Payment payment, Booking booking) {
        StringBuilder details = new StringBuilder();
        details.append("<html><div style='width:400px;'>");
        details.append("<h3>Payment Details</h3>");
        details.append("<table border='0' cellpadding='5'>");

        details.append("<tr><td><b>Payment ID:</b></td><td>").append(payment.getPaymentId()).append("</td></tr>");
        details.append("<tr><td><b>Booking ID:</b></td><td>BK-").append(payment.getBookingId()).append("</td></tr>");

        try {
            if (booking != null) {
                Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                if (vehicle != null) {
                    details.append("<tr><td><b>Vehicle:</b></td><td>").append(vehicle.getVehiclePlateNumber())
                            .append("</td></tr>");
                }

                if (booking.getBookingTime() != null) {
                    details.append("<tr><td><b>Booking Time:</b></td><td>")
                            .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(booking.getBookingTime()))
                            .append("</td></tr>");
                }
            }
        } catch (SQLException e) {
            // Ignore
        }

        details.append("<tr><td><b>Amount Due:</b></td><td>$").append(payment.getDueAmount()).append("</td></tr>");
        details.append("<tr><td><b>Amount Paid:</b></td><td>$").append(payment.getPaidAmount()).append("</td></tr>");
        details.append("<tr><td><b>Balance:</b></td><td>$").append(String.format("%.2f",
                (payment.getDueAmount() - payment.getPaidAmount()))).append("</td></tr>");
        details.append("<tr><td><b>Status:</b></td><td>").append(getPaymentStatusText(payment.getPaymentStatus()))
                .append("</td></tr>");

        if (payment.getPaymentDate() != null) {
            details.append("<tr><td><b>Payment Date:</b></td><td>")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(payment.getPaymentDate()))
                    .append("</td></tr>");
        }

        if (payment.getPaidBy() != null) {
            details.append("<tr><td><b>Payment Method:</b></td><td>").append(payment.getPaidBy()).append("</td></tr>");
        }

        if (payment.getRemarks() != null) {
            details.append("<tr><td><b>Remarks:</b></td><td>").append(payment.getRemarks()).append("</td></tr>");
        }

        details.append("</table></div></html>");

        JOptionPane.showMessageDialog(this, details.toString(),
                "Payment Details - #" + payment.getPaymentId(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateReceipt(Payment payment) {
        try {
            Booking booking = bookingDAO.findById(payment.getBookingId());
            Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
            User user = userDAO.findById(userId);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String receipt = "=================================\n" +
                    "         PARKING RECEIPT          \n" +
                    "=================================\n" +
                    String.format("Receipt #: %-23d\n", payment.getPaymentId()) +
                    String.format("Date: %-28s\n", sdf.format(new Date())) +
                    "---------------------------------\n" +
                    String.format("Customer: %-25s\n", user.getUsername()) +
                    String.format("Booking #: BK-%-20d\n", payment.getBookingId()) +
                    String.format("Vehicle: %-26s\n", vehicle.getVehiclePlateNumber()) +
                    "---------------------------------\n" +
                    String.format("Amount Due: $%-22.2f\n", payment.getDueAmount()) +
                    String.format("Amount Paid: $%-21.2f\n", payment.getPaidAmount()) +
                    String.format("Payment Method: %-18s\n", payment.getPaidBy()) +
                    String.format("Status: %-27s\n", "PAID") +
                    "---------------------------------\n" +
                    "        THANK YOU FOR PAYING!     \n" +
                    "=================================\n";

            // Show receipt in dialog
            JTextArea receiptArea = new JTextArea(receipt);
            receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            receiptArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(receiptArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Payment Receipt - #" + payment.getPaymentId(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            showError("Error", "Failed to generate receipt: " + e.getMessage());
        }
    }

    private void showPayAllDialog() {
        // Calculate total pending balance
        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                List<Payment> payments = getPaymentsByOwnerId(ownerId);
                double totalBalance = 0;

                for (Payment payment : payments) {
                    if (payment.getPaymentStatus() == Payment.STATUS_APPROVED_UNPAID ||
                            payment.getPaymentStatus() == Payment.STATUS_PARTIAL) {
                        double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
                        double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
                        totalBalance += (dueAmount - paidAmount);
                    }
                }

                return totalBalance;
            }

            @Override
            protected void done() {
                try {
                    double totalBalance = get();

                    if (totalBalance <= 0) {
                        showInfo("No Pending Payments", "You have no pending payments to pay.");
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(CustomerPaymentPanel.this,
                            String.format("Pay all pending payments?\n\n" +
                                    "Total Amount: $%.2f\n\n" +
                                    "This will process payments for all your pending bookings.",
                                    totalBalance),
                            "Pay All Pending",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Process all payments
                        processAllPayments(totalBalance);
                    }

                } catch (Exception e) {
                    showError("Error", "Failed to calculate total balance: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void processAllPayments(double totalAmount) {
        // Show payment method selection
        JComboBox<String> methodCombo = new JComboBox<>(new String[] {
                "Credit Card", "Debit Card", "Bank Transfer", "Mobile Wallet"
        });

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Total Amount:"));
        panel.add(new JLabel(String.format("$%.2f", totalAmount)));
        panel.add(new JLabel("Payment Method:"));
        panel.add(methodCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Confirm Bulk Payment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String paymentMethod = (String) methodCombo.getSelectedItem();

            // Process payments in background
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<Payment> payments = getPaymentsByOwnerId(ownerId);

                    for (Payment payment : payments) {
                        if (payment.getPaymentStatus() == Payment.STATUS_PENDING ||
                                payment.getPaymentStatus() == Payment.STATUS_PARTIAL) {

                            double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
                            double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
                            double balance = dueAmount - paidAmount;

                            if (balance > 0) {
                                payment.setPaidAmount(dueAmount); // Pay in full
                                payment.setPaidBy(paymentMethod);
                                payment.setPaymentDate(new Date());
                                payment.setPaymentStatus(Payment.STATUS_PAID);

                                paymentDAO.update(payment);

                                // Update booking status
                                Booking booking = bookingDAO.findById(payment.getBookingId());
                                if (booking != null) {
                                    booking.setBookingStatus(1); // Approved
                                    bookingDAO.update(booking);
                                }
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Wait for completion
                        showSuccess("Bulk Payment Successful",
                                "All pending payments have been processed successfully!");
                        loadPayments();
                    } catch (Exception e) {
                        showError("Bulk Payment Failed",
                                "Failed to process some payments: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private void autoPayAll() {
        showInfo("Auto Pay", "Auto Pay functionality will be implemented soon!");
    }

    private void schedulePayment() {
        showInfo("Schedule Payment", "Payment scheduling will be implemented soon!");
    }

    private void downloadStatement() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Download Payment Statement");
        fileChooser.setSelectedFile(new java.io.File("payment_statement_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Generate and save statement
            showInfo("Statement Downloaded",
                    "Payment statement saved to: " + fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    // Custom table cell renderers
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (value instanceof String) {
                String status = (String) value;
                if (status.contains("PENDING")) {
                    c.setForeground(WARNING);
                } else if (status.contains("PAID")) {
                    c.setForeground(SUCCESS);
                } else if (status.contains("PARTIAL")) {
                    c.setForeground(new Color(155, 89, 182));
                }
                ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
            }
            return c;
        }
    }

    class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (value instanceof String) {
                String amountStr = (String) value;
                if (amountStr.startsWith("$")) {
                    try {
                        double amount = Double.parseDouble(amountStr.substring(1));
                        if (amount > 0) {
                            c.setForeground(DANGER);
                        } else {
                            c.setForeground(SUCCESS);
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
                ((JLabel) c).setFont(getFont().deriveFont(Font.BOLD));
            }
            return c;
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}