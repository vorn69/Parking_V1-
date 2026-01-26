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
import java.util.ArrayList;

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
    private final Color BG_COLOR = new Color(240, 242, 245);
    private final Color PRIMARY = new Color(52, 152, 219);
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
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        contentPanel.add(createStatsCards(), BorderLayout.WEST);
        contentPanel.add(createMainPanel(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
        add(createQuickActions(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("My Payments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));

        JLabel subtitle = new JLabel("Track and manage your parking payments");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(127, 140, 141));

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton refreshBtn = createStyledButton("Refresh 🔄", new Color(149, 165, 166));
        refreshBtn.addActionListener(e -> loadPayments());

        JButton payAllBtn = createStyledButton("Pay All Pending 💳", PRIMARY);
        payAllBtn.addActionListener(e -> showPayAllDialog());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(payAllBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }

    private JPanel createStatsCards() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(280, 0));

        // Card 1: Total Due
        totalDueLabel = new JLabel("$0.00");
        statsPanel.add(createStatCard("Total Amount Due", totalDueLabel, DANGER, "Amount you need to pay"));
        statsPanel.add(Box.createVerticalStrut(15));

        // Card 2: Total Paid
        totalPaidLabel = new JLabel("$0.00");
        statsPanel.add(createStatCard("Total Amount Paid", totalPaidLabel, SUCCESS, "Amount already paid"));
        statsPanel.add(Box.createVerticalStrut(15));

        // Card 3: Pending Payments
        pendingCountLabel = new JLabel("0");
        statsPanel.add(createStatCard("Pending Payments", pendingCountLabel, WARNING, "Payments awaiting action"));
        statsPanel.add(Box.createVerticalStrut(15));

        // Card 4: Last Payment
        lastPaymentLabel = new JLabel("None");
        statsPanel.add(createStatCard("Last Payment", lastPaymentLabel, INFO, "Most recent payment"));

        return statsPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);

        // Shadow effect via border
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        card.setMaximumSize(new Dimension(280, 140));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(127, 140, 141));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(149, 165, 166));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        // Add filter panel
        mainPanel.add(createFilterPanel(), BorderLayout.NORTH);

        // Add payment table
        mainPanel.add(createPaymentTable(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(5, 10, 5, 10)));

        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterLabel.setForeground(new Color(44, 62, 80));

        JComboBox<String> statusFilter = new JComboBox<>(new String[] {
                "All Payments", "Pending", "Paid", "Partial"
        });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> loadPayments()); // Simplified reloading

        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);

        return filterPanel;
    }

    private JPanel createPaymentTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 0, 0, 0),
                new LineBorder(new Color(230, 230, 230), 1, true)));

        // Table model
        String[] columns = { "ID", "Booking", "Vehicle", "Due", "Paid", "Balance", "Status", "Actions" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTable = new JTable(tableModel);
        paymentTable.setRowHeight(50);
        paymentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        paymentTable.setShowVerticalLines(false);
        paymentTable.setIntercellSpacing(new Dimension(0, 0));
        paymentTable.setFillsViewportHeight(true);
        paymentTable.setSelectionBackground(new Color(232, 240, 254));
        paymentTable.setSelectionForeground(Color.BLACK);

        // Header styling
        JTableHeader header = paymentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(44, 62, 80));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        // Set column widths
        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        paymentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        paymentTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        paymentTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        paymentTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        // Style cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        paymentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected)
                    c.setBackground(Color.WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Custom renderer for status column
        paymentTable.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        // Action listener
        paymentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = paymentTable.rowAtPoint(e.getPoint());
                int col = paymentTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 7) {
                    int paymentId = (Integer) paymentTable.getValueAt(row, 0);
                    handleActionClick(paymentId, row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createQuickActions() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Placeholder buttons for future features
        JButton exportBtn = createStyledButton("Export PDF 📄", new Color(155, 89, 182));
        actionPanel.add(exportBtn);

        return actionPanel;
    }

    private void loadPayments() {
        SwingWorker<List<Payment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Payment> doInBackground() throws Exception {
                try {
                    if (ownerId != null) {
                        return getPaymentsByOwnerId(ownerId);
                    } else {
                        return paymentDAO.findByUserId(userId);
                    }
                } catch (SQLException e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Payment> payments = get();
                    updatePaymentTable(payments);
                    updateStats(payments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private List<Payment> getPaymentsByOwnerId(Integer ownerId) throws SQLException {
        List<Booking> bookings = bookingDAO.findByCustomerId(ownerId);
        List<Payment> allPayments = new ArrayList<>();

        for (Booking booking : bookings) {
            Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
            if (payment != null) {
                payment.setBookingRef("BK-" + booking.getBookingId());
                payment.setVehicleId(booking.getVehicleId());
                allPayments.add(payment);
            }
        }
        return allPayments;
    }

    private void updatePaymentTable(List<Payment> payments) {
        tableModel.setRowCount(0);

        for (Payment payment : payments) {
            try {
                Booking booking = bookingDAO.findById(payment.getBookingId());
                String vehicleInfo = "Unknown";

                if (booking != null) {
                    Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                    if (vehicle != null)
                        vehicleInfo = vehicle.getVehiclePlateNumber();
                }

                double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
                double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
                double balance = dueAmount - paidAmount;

                String statusText = getPaymentStatusText(payment.getPaymentStatus());

                tableModel.addRow(new Object[] {
                        payment.getPaymentId(),
                        "BK-" + payment.getBookingId(),
                        vehicleInfo,
                        String.format("$%.2f", dueAmount),
                        String.format("$%.2f", paidAmount),
                        String.format("$%.2f", balance),
                        statusText,
                        "••• Options"
                });

            } catch (Exception e) {
            }
        }
    }

    // Status Renderer class
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

            if (status.contains("PENDING") || status.contains("DUE")) {
                label.setForeground(new Color(231, 76, 60)); // Red/Orange
            } else if (status.contains("PAID")) {
                label.setForeground(new Color(46, 204, 113)); // Green
            } else if (status.contains("PARTIAL")) {
                label.setForeground(new Color(241, 196, 15)); // Yellow
            } else {
                label.setForeground(Color.GRAY);
            }

            if (!isSelected)
                label.setBackground(Color.WHITE);
            return label;
        }
    }

    private String getPaymentStatusText(int status) {
        switch (status) {
            case Payment.STATUS_PENDING_APPROVAL:
                return "PENDING";
            case Payment.STATUS_APPROVED_UNPAID:
                return "PAYMENT DUE";
            case Payment.STATUS_PAID:
                return "PAID";
            case Payment.STATUS_PARTIAL:
                return "PARTIAL";
            case Payment.STATUS_CANCELLED:
                return "CANCELLED";
            default:
                return "UNKNOWN";
        }
    }

    private void handleActionClick(int paymentId, int row) {
        try {
            Payment p = paymentDAO.findById(paymentId);
            if (p == null)
                return;

            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem payItem = new JMenuItem("💰 Make Payment");
            payItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            payItem.addActionListener(e -> showPaymentDialog(p));

            JMenuItem detailsItem = new JMenuItem("📄 View Details");
            detailsItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            detailsItem.addActionListener(e -> viewPaymentDetails(p));

            if (p.getPaymentStatus() == Payment.STATUS_PAID)
                payItem.setEnabled(false);

            popupMenu.add(payItem);
            popupMenu.add(detailsItem);

            // Get the bounds of the cell to show the popup menu correctly
            Rectangle cellRect = paymentTable.getCellRect(row, 7, true);
            popupMenu.show(paymentTable, cellRect.x + cellRect.width / 2, cellRect.y + cellRect.height / 2);

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void showPaymentDialog(Payment payment) {
        double currentBalance = payment.getDueAmount() - payment.getPaidAmount();
        if (currentBalance <= 0) {
            showInfo("No Payment Needed", "This payment is already fully paid.");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter amount to pay:",
                String.format("%.2f", currentBalance));

        if (input != null) {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    showError("Invalid Amount", "Payment amount must be greater than 0.");
                    return;
                }
                if (amount > currentBalance) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            String.format("You are paying $%.2f, but the balance is $%.2f.\n" +
                                    "The extra amount will be refunded to your account.\n" +
                                    "Continue?", amount, currentBalance),
                            "Overpayment Warning",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                    amount = currentBalance;
                }

                payment.setPaidAmount(payment.getPaidAmount() + amount);
                if (payment.getPaidAmount() >= payment.getDueAmount()) {
                    payment.setPaymentStatus(Payment.STATUS_PAID);
                } else {
                    payment.setPaymentStatus(Payment.STATUS_PARTIAL);
                }
                payment.setPaymentDate(new Date());
                payment.setPaidBy("User Input");
                paymentDAO.update(payment);

                if (payment.getPaymentStatus() == Payment.STATUS_PAID) {
                    Booking booking = bookingDAO.findById(payment.getBookingId());
                    if (booking != null) {
                        booking.setBookingStatus(1); // Approved
                        bookingDAO.update(booking);
                    }
                }

                showSuccess("Payment Successful", String.format("Payment of $%.2f processed successfully!", amount));
                loadPayments();
            } catch (NumberFormatException e) {
                showError("Invalid Amount", "Please enter a valid number for the amount.");
            } catch (SQLException e) {
                showError("Database Error", "Failed to process payment: " + e.getMessage());
            }
        }
    }

    private void viewPaymentDetails(Payment payment) {
        StringBuilder details = new StringBuilder();
        details.append("<html><div style='width:400px;'>");
        details.append("<h3>Payment Details</h3>");
        details.append("<table border='0' cellpadding='5'>");

        details.append("<tr><td><b>Payment ID:</b></td><td>").append(payment.getPaymentId()).append("</td></tr>");
        details.append("<tr><td><b>Booking ID:</b></td><td>BK-").append(payment.getBookingId()).append("</td></tr>");

        try {
            Booking booking = bookingDAO.findById(payment.getBookingId());
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

    private void showPayAllDialog() {
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
                    if (totalBalance > 0) {
                        int confirm = JOptionPane.showConfirmDialog(CustomerPaymentPanel.this,
                                String.format("Pay all pending balances ($%.2f)?", totalBalance),
                                "Pay All Pending",
                                JOptionPane.YES_NO_OPTION);

                        if (confirm == JOptionPane.YES_OPTION) {
                            JOptionPane.showMessageDialog(CustomerPaymentPanel.this,
                                    "Processing payment...",
                                    "Processing", JOptionPane.INFORMATION_MESSAGE);

                            // Simulation: update all to paid
                            List<Payment> payments = getPaymentsByOwnerId(ownerId);
                            for (Payment p : payments) {
                                if (p.getPaymentStatus() == Payment.STATUS_APPROVED_UNPAID
                                        || p.getPaymentStatus() == Payment.STATUS_PARTIAL) {
                                    p.setPaidAmount(p.getDueAmount());
                                    p.setPaymentStatus(Payment.STATUS_PAID);
                                    p.setPaymentDate(new Date());
                                    p.setPaidBy("Auto-Pay");
                                    paymentDAO.update(p);
                                }
                            }
                            loadPayments();
                            JOptionPane.showMessageDialog(CustomerPaymentPanel.this, "All payments processed!");
                        }
                    } else {
                        showInfo("No Pending Payments", "You have no pending payments at this time.");
                    }
                } catch (Exception e) {
                    showError("Error", "Failed to calculate total balance: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showInfo(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
}