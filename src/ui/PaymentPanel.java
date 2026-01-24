package ui;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.Booking;
import models.Payment; // ADD THIS IMPORT
import models.User; // ADD THIS IMPORT

public class PaymentPanel extends JPanel {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();

    private List<Payment> paymentList;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statsLabel;
    private JLabel timeLabel;

    // Colors
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color PRIMARY = new Color(59, 89, 152);
    private final Color SUCCESS = new Color(46, 204, 113);
    private final Color WARNING = new Color(241, 196, 15);
    private final Color DANGER = new Color(231, 76, 60);
    private final Color INFO = new Color(52, 152, 219);
    private final Color TEXT_PRIMARY = new Color(52, 73, 94);
    private final Color TEXT_SECONDARY = new Color(149, 165, 166);

    // Filters
    private JComboBox<String> statusFilter;
    private JComboBox<String> typeFilter;
    private JTextField searchField;
    private JSpinner dateFromSpinner;
    private JSpinner dateToSpinner;

    public PaymentPanel() {
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        loadData();
        startClock();
    }

    private void initComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ================= HEADER PANEL =================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 25, 20, 25)));

        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(CARD_BG);

        JLabel icon = new JLabel("💰");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));

        JLabel title = new JLabel("Payment Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        titlePanel.add(icon);
        titlePanel.add(title);
        header.add(titlePanel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);

        buttonPanel.add(createStyledButton("➕ NEW PAYMENT", SUCCESS, "Create new payment"));
        buttonPanel.add(createStyledButton("📊 EXPORT", INFO, "Export to Excel"));
        buttonPanel.add(createStyledButton("🖨️ PRINT", WARNING, "Print report"));
        buttonPanel.add(createRefreshButton());

        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createStyledButton(String text, Color bgColor, String tooltip) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgColor.darker(), 1),
                new EmptyBorder(10, 15, 10, 15)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        if (text.contains("NEW PAYMENT")) {
            button.addActionListener(e -> createNewPayment());
        } else if (text.contains("EXPORT")) {
            button.addActionListener(e -> exportPayments());
        } else if (text.contains("PRINT")) {
            button.addActionListener(e -> printReport());
        }

        return button;
    }

    private JButton createRefreshButton() {
        JButton button = new JButton("🔄");
        button.setBackground(new Color(236, 240, 241));
        button.setForeground(TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setToolTipText("Refresh Data");
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(8, 12, 8, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            button.setText("⏳");
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    loadData();
                    return null;
                }

                @Override
                protected void done() {
                    button.setText("🔄");
                }
            };
            worker.execute();
        });

        return button;
    }

    // ================= CONTENT PANEL =================
    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(BG_COLOR);
        content.setBorder(new EmptyBorder(0, 15, 15, 15));

        content.add(createFilterPanel(), BorderLayout.NORTH);
        content.add(createTablePanel(), BorderLayout.CENTER);
        content.add(createStatsPanel(), BorderLayout.SOUTH);

        return content;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));

        // Status filter
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(TEXT_PRIMARY);
        filterPanel.add(statusLabel);

        statusFilter = new JComboBox<>(new String[] { "All", "PENDING", "PAID", "PARTIAL" });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.addActionListener(e -> filterPayments());
        filterPanel.add(statusFilter);

        // Type filter (payment method)
        filterPanel.add(Box.createHorizontalStrut(15));
        JLabel typeLabel = new JLabel("Payment Method:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        typeLabel.setForeground(TEXT_PRIMARY);
        filterPanel.add(typeLabel);

        typeFilter = new JComboBox<>(new String[] { "All", "Cash", "Card", "Bank Transfer", "Mobile" });
        typeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeFilter.addActionListener(e -> filterPayments());
        filterPanel.add(typeFilter);

        // Date range
        filterPanel.add(Box.createHorizontalStrut(15));
        JLabel dateLabel = new JLabel("Date Range:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(TEXT_PRIMARY);
        filterPanel.add(dateLabel);

        dateFromSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(dateFromSpinner, "yyyy-MM-dd");
        dateFromSpinner.setEditor(fromEditor);
        dateFromSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateFromSpinner.setValue(new Date());
        dateFromSpinner.addChangeListener(e -> filterPayments());
        filterPanel.add(dateFromSpinner);

        JLabel toLabel = new JLabel("to");
        toLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toLabel.setForeground(TEXT_SECONDARY);
        filterPanel.add(toLabel);

        dateToSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(dateToSpinner, "yyyy-MM-dd");
        dateToSpinner.setEditor(toEditor);
        dateToSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateToSpinner.setValue(new Date());
        dateToSpinner.addChangeListener(e -> filterPayments());
        filterPanel.add(dateToSpinner);

        // Search field
        filterPanel.add(Box.createHorizontalStrut(15));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT_PRIMARY);
        filterPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, Booking, Customer...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterPayments();
            }
        });
        filterPanel.add(searchField);

        // Clear filters button
        JButton clearButton = new JButton("Clear Filters");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearButton.setBackground(new Color(236, 240, 241));
        clearButton.setForeground(TEXT_PRIMARY);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearFilters());
        filterPanel.add(clearButton);

        return filterPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(0, 0, 0, 0)));

        // Table model
        model = new DefaultTableModel(
                new String[] { "ID", "Booking", "Customer", "Amount Due", "Amount Paid",
                        "Balance", "Method", "Status", "Date", "Actions" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 9; // Only actions column is editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 9 ? JPanel.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80); // Booking
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Customer
        table.getColumnModel().getColumn(3).setPreferredWidth(90); // Amount Due
        table.getColumnModel().getColumn(4).setPreferredWidth(90); // Amount Paid
        table.getColumnModel().getColumn(5).setPreferredWidth(80); // Balance
        table.getColumnModel().getColumn(6).setPreferredWidth(80); // Method
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(9).setPreferredWidth(150); // Actions

        // Custom header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        header.setReorderingAllowed(false);

        // Custom cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setBorder(new EmptyBorder(0, 10, 0, 10));

                    // Color coding for amounts
                    if (column == 3 || column == 4 || column == 5) { // Amount columns
                        if (value instanceof String) {
                            String val = (String) value;
                            if (val.startsWith("$")) {
                                try {
                                    double amount = Double.parseDouble(val.substring(1));
                                    if (amount > 0) {
                                        label.setForeground(column == 5 ? DANGER : TEXT_PRIMARY);
                                    } else {
                                        label.setForeground(TEXT_SECONDARY);
                                    }
                                } catch (NumberFormatException e) {
                                    label.setForeground(TEXT_PRIMARY);
                                }
                            }
                        }
                    }

                    // Status column styling
                    if (column == 7) {
                        String status = value != null ? value.toString() : "";
                        if (status.contains("PENDING")) {
                            label.setForeground(WARNING);
                        } else if (status.contains("PAID") && !status.contains("UNPAID")) {
                            label.setForeground(SUCCESS);
                        } else if (status.contains("PARTIAL")) {
                            label.setForeground(new Color(155, 89, 182));
                        } else if (status.contains("DUE") || status.contains("UNPAID")) {
                            label.setForeground(INFO);
                        }
                    }

                    // Row coloring
                    if (isSelected) {
                        label.setBackground(new Color(236, 240, 241));
                    } else {
                        label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    }
                }

                return c;
            }
        });

        // Action column renderer and editor
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        statsPanel.setBackground(CARD_BG);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 20, 15, 20)));

        // Stats will be updated after data load
        updateStatsPanel(statsPanel);

        return statsPanel;
    }

    private void updateStatsPanel(JPanel statsPanel) {
        statsPanel.removeAll();

        try {
            double totalRevenue = paymentList.stream()
                    .filter(p -> p.isPaid())
                    .mapToDouble(p -> p.getPaidAmount() != null ? p.getPaidAmount() : 0)
                    .sum();

            double pendingAmount = paymentList.stream()
                    .filter(p -> p.isPending())
                    .mapToDouble(p -> p.getDueAmount() != null ? p.getDueAmount() : 0)
                    .sum();

            double partialAmount = paymentList.stream()
                    .filter(p -> p.isPartial())
                    .mapToDouble(p -> p.getBalance())
                    .sum();

            long paidCount = paymentList.stream()
                    .filter(p -> p.isPaid())
                    .count();

            long pendingCount = paymentList.stream()
                    .filter(p -> p.isPending())
                    .count();

            // Add stat cards
            statsPanel.add(createStatCard("💰 Total Revenue",
                    String.format("$%.2f", totalRevenue), SUCCESS, "Collected amount"));

            statsPanel.add(createStatCard("⏳ Pending",
                    String.format("$%.2f", pendingAmount), WARNING, pendingCount + " payments"));

            statsPanel.add(createStatCard("📊 Partial",
                    String.format("$%.2f", partialAmount), new Color(155, 89, 182), "Unpaid balance"));

            statsPanel.add(createStatCard("✅ Completed",
                    String.valueOf(paidCount), new Color(46, 204, 113), "Successful payments"));

            statsPanel.add(createStatCard("📋 Total",
                    String.valueOf(paymentList.size()), INFO, "All payments"));

        } catch (Exception e) {
            statsPanel.add(new JLabel("Error loading statistics"));
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel createStatCard(String title, String value, Color color, String desc) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(240, 240, 240), 1),
                new EmptyBorder(10, 15, 10, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(TEXT_SECONDARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    // ================= FOOTER PANEL =================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(52, 73, 94));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));

        statsLabel = new JLabel();
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        timeLabel = new JLabel();
        timeLabel.setForeground(new Color(189, 195, 199));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        footer.add(statsLabel, BorderLayout.WEST);
        footer.add(timeLabel, BorderLayout.EAST);

        return footer;
    }

    // private void startClock() {
    // Timer timer = new Timer(1000, e -> {
    // SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a");
    // timeLabel.setText("🕒 " + sdf.format(new Date()));
    // });
    // timer.start();
    // }
    private void startClock() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a");
                    timeLabel.setText("🕒 " + sdf.format(new Date()));
                });
            }
        }, 0, 1000);
    }

    // ================= DATA LOADING =================
    private void loadData() {
        try {
            paymentList = paymentDAO.findAll();
            enrichPaymentData();
            updateTable();
            updateFooter();

            // Update stats panel
            JPanel parent = (JPanel) getComponent(1); // Content panel
            JPanel statsPanel = (JPanel) ((BorderLayout) parent.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            updateStatsPanel(statsPanel);

        } catch (SQLException e) {
            showError("Database Error", "Failed to load payments: " + e.getMessage());
        }
    }

    private void enrichPaymentData() {
        // Enrich payment data with user and booking information
        for (Payment payment : paymentList) {
            try {
                // Get booking information
                if (payment.getBookingId() != null) {
                    Booking booking = bookingDAO.findById(payment.getBookingId());
                    if (booking != null) {
                        payment.setBookingRef("BK-" + booking.getBookingId());
                    }
                }

                // Get user information
                if (payment.getUserId() != null) {
                    User user = userDAO.findById(payment.getUserId());
                    if (user != null) {
                        payment.setUserName(user.getUsername());
                        // Try to get full name - if method doesn't exist, use username
                        try {
                            // Try to get full name using reflection
                            java.lang.reflect.Method method = user.getClass().getMethod("getFullName");
                            String fullName = (String) method.invoke(user);
                            if (fullName != null && !fullName.trim().isEmpty()) {
                                payment.setFullName(fullName);
                            } else {
                                payment.setFullName(user.getUsername());
                            }
                        } catch (Exception e) {
                            // If getFullName doesn't exist, use username
                            payment.setFullName(user.getUsername());
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error enriching payment data: " + e.getMessage());
            }
        }
    }

    private void updateTable() {
        model.setRowCount(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Payment payment : paymentList) {
            try {
                // Get display values
                String bookingInfo = payment.getBookingRef() != null ? payment.getBookingRef()
                        : "BK-" + payment.getBookingId();

                String customerInfo = payment.getFullName() != null ? payment.getFullName()
                        : (payment.getUserName() != null ? payment.getUserName() : "Customer");

                // Calculate balance
                double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
                double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
                double balance = payment.getBalance();

                // Format date
                String paymentDate = "";
                if (payment.getPaymentDate() != null) {
                    paymentDate = dateFormat.format(payment.getPaymentDate());
                }

                // Get payment method (from paid_by field)
                String paymentMethod = payment.getPaidBy() != null ? payment.getPaidBy() : "Cash";

                model.addRow(new Object[] {
                        payment.getPaymentId(),
                        bookingInfo,
                        customerInfo,
                        String.format("$%.2f", dueAmount),
                        String.format("$%.2f", paidAmount),
                        String.format("$%.2f", balance),
                        paymentMethod,
                        payment.getStatusText(),
                        paymentDate,
                        createActionButtons(payment)
                });

            } catch (Exception e) {
                System.err.println("Error loading payment details: " + e.getMessage());
            }
        }
    }

    private void updateFooter() {
        long total = paymentList != null ? paymentList.size() : 0;
        long pending = paymentList != null ? paymentList.stream()
                .filter(p -> p.isPending()).count() : 0;

        statsLabel.setText("<html>💰 <b>" + total + "</b> payments | ⏳ <b>" + pending + "</b> pending</html>");
    }

    private void filterPayments() {
        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedType = (String) typeFilter.getSelectedItem();
        Date fromDate = (Date) dateFromSpinner.getValue();
        Date toDate = (Date) dateToSpinner.getValue();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (paymentList == null)
                    return null;

                List<Payment> filtered = paymentList.stream()
                        .filter(p -> {
                            // Status filter
                            if (!selectedStatus.equals("All")) {
                                if (!p.getStatusText().equalsIgnoreCase(selectedStatus))
                                    return false;
                            }

                            // Type filter (payment method)
                            if (!selectedType.equals("All")) {
                                String method = p.getPaidBy() != null ? p.getPaidBy() : "Cash";
                                if (!method.equalsIgnoreCase(selectedType))
                                    return false;
                            }

                            // Date range filter
                            if (p.getPaymentDate() != null && fromDate != null && toDate != null) {
                                if (p.getPaymentDate().before(fromDate) ||
                                        p.getPaymentDate().after(toDate)) {
                                    return false;
                                }
                            }

                            // Search filter
                            if (!searchText.isEmpty()) {
                                boolean matches = String.valueOf(p.getPaymentId()).contains(searchText) ||
                                        (p.getBookingRef() != null &&
                                                p.getBookingRef().toLowerCase().contains(searchText))
                                        ||
                                        (p.getFullName() != null &&
                                                p.getFullName().toLowerCase().contains(searchText))
                                        ||
                                        (p.getUserName() != null &&
                                                p.getUserName().toLowerCase().contains(searchText))
                                        ||
                                        p.getStatusText().toLowerCase().contains(searchText) ||
                                        (p.getPaidBy() != null &&
                                                p.getPaidBy().toLowerCase().contains(searchText));
                                if (!matches)
                                    return false;
                            }

                            return true;
                        })
                        .toList();

                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    for (Payment payment : filtered) {
                        try {
                            double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
                            double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
                            double balance = payment.getBalance();

                            String paymentDate = "";
                            if (payment.getPaymentDate() != null) {
                                paymentDate = dateFormat.format(payment.getPaymentDate());
                            }

                            String bookingInfo = payment.getBookingRef() != null ? payment.getBookingRef()
                                    : "BK-" + payment.getBookingId();

                            String customerInfo = payment.getFullName() != null ? payment.getFullName()
                                    : (payment.getUserName() != null ? payment.getUserName() : "Customer");

                            String paymentMethod = payment.getPaidBy() != null ? payment.getPaidBy() : "Cash";

                            model.addRow(new Object[] {
                                    payment.getPaymentId(),
                                    bookingInfo,
                                    customerInfo,
                                    String.format("$%.2f", dueAmount),
                                    String.format("$%.2f", paidAmount),
                                    String.format("$%.2f", balance),
                                    paymentMethod,
                                    payment.getStatusText(),
                                    paymentDate,
                                    createActionButtons(payment)
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                return null;
            }
        };
        worker.execute();
    }

    private void clearFilters() {
        statusFilter.setSelectedIndex(0);
        typeFilter.setSelectedIndex(0);
        searchField.setText("");
        dateFromSpinner.setValue(new Date());
        dateToSpinner.setValue(new Date());
        loadData();
    }

    private JPanel createActionButtons(Payment payment) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);

        // View button
        JButton viewBtn = new JButton("👁");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewBtn.setToolTipText("View Details");
        viewBtn.setBackground(INFO);
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
        viewBtn.addActionListener(e -> viewPaymentDetails(payment));

        // Edit button
        JButton editBtn = new JButton("✏️");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editBtn.setToolTipText("Edit Payment");
        editBtn.setBackground(WARNING);
        editBtn.setForeground(Color.WHITE);
        editBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
        editBtn.addActionListener(e -> editPayment(payment));

        // Mark as paid button (for pending/partial/approved payments)
        if (payment.getPaymentStatus() == Payment.STATUS_PENDING_APPROVAL ||
                payment.getPaymentStatus() == Payment.STATUS_APPROVED_UNPAID ||
                payment.getPaymentStatus() == Payment.STATUS_PARTIAL) {
            JButton payBtn = new JButton("💰");
            payBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            payBtn.setToolTipText("Receive Payment");
            payBtn.setBackground(SUCCESS);
            payBtn.setForeground(Color.WHITE);
            payBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
            payBtn.addActionListener(e -> receivePayment(payment));
            panel.add(payBtn);
        }

        // Delete button
        JButton deleteBtn = new JButton("🗑️");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteBtn.setToolTipText("Delete Payment");
        deleteBtn.setBackground(DANGER);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
        deleteBtn.addActionListener(e -> deletePayment(payment));

        panel.add(viewBtn);
        panel.add(editBtn);
        panel.add(deleteBtn);

        return panel;
    }

    // ================= ACTION METHODS =================
    private void createNewPayment() {
        // Show dialog to create new payment
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Create New Payment", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Create New Payment", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        // Form fields
        formPanel.add(new JLabel("Booking ID:"));
        JTextField bookingIdField = new JTextField();
        formPanel.add(bookingIdField);

        formPanel.add(new JLabel("Customer ID:"));
        JTextField userIdField = new JTextField();
        formPanel.add(userIdField);

        formPanel.add(new JLabel("Amount Due:"));
        JTextField amountDueField = new JTextField();
        formPanel.add(amountDueField);

        formPanel.add(new JLabel("Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(new String[] { "Cash", "Card", "Bank Transfer", "Mobile" });
        formPanel.add(methodCombo);

        formPanel.add(new JLabel("Remarks:"));
        JTextField remarksField = new JTextField();
        formPanel.add(remarksField);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("💾 Save");
        saveButton.setBackground(SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            try {
                // Create new payment
                Payment payment = new Payment();
                payment.setBookingId(Integer.parseInt(bookingIdField.getText()));
                payment.setUserId(Integer.parseInt(userIdField.getText()));
                payment.setDueAmount(Double.parseDouble(amountDueField.getText()));
                payment.setPaidBy((String) methodCombo.getSelectedItem());
                payment.setRemarks(remarksField.getText());
                payment.setPaymentStatus(Payment.STATUS_PENDING);

                // Save to database
                Integer newId = paymentDAO.create(payment);
                if (newId != null) {
                    dialog.dispose();
                    showSuccess("Success", "Payment created successfully! ID: " + newId);
                    loadData();
                } else {
                    showError("Error", "Failed to create payment");
                }
            } catch (NumberFormatException ex) {
                showError("Input Error", "Please enter valid numbers");
            } catch (SQLException ex) {
                showError("Database Error", ex.getMessage());
            }
        });

        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setBackground(DANGER);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        content.add(title, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private void viewPaymentDetails(Payment payment) {
        StringBuilder details = new StringBuilder();
        details.append("<html><div style='width:400px;'>");
        details.append("<h3>Payment #").append(payment.getPaymentId()).append("</h3>");
        details.append("<table border='0' cellpadding='5'>");

        details.append("<tr><td><b>Status:</b></td><td>").append(payment.getStatusText()).append("</td></tr>");
        details.append("<tr><td><b>Booking ID:</b></td><td>").append(payment.getBookingId()).append("</td></tr>");
        details.append("<tr><td><b>Amount Due:</b></td><td>$").append(payment.getDueAmount()).append("</td></tr>");
        details.append("<tr><td><b>Amount Paid:</b></td><td>$").append(payment.getPaidAmount()).append("</td></tr>");
        details.append("<tr><td><b>Balance:</b></td><td>$").append(String.format("%.2f", payment.getBalance()))
                .append("</td></tr>");
        details.append("<tr><td><b>Method:</b></td><td>")
                .append(payment.getPaidBy() != null ? payment.getPaidBy() : "Cash").append("</td></tr>");

        if (payment.getPaymentDate() != null) {
            details.append("<tr><td><b>Date:</b></td><td>")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(payment.getPaymentDate()))
                    .append("</td></tr>");
        }

        if (payment.getPaidBy() != null) {
            details.append("<tr><td><b>Paid By:</b></td><td>").append(payment.getPaidBy()).append("</td></tr>");
        }

        if (payment.getRemarks() != null) {
            details.append("<tr><td><b>Remarks:</b></td><td>").append(payment.getRemarks()).append("</td></tr>");
        }

        if (payment.getFullName() != null) {
            details.append("<tr><td><b>Customer:</b></td><td>").append(payment.getFullName()).append("</td></tr>");
        }

        details.append("</table></div></html>");

        showInfo("Payment Details", details.toString());
    }

    private void editPayment(Payment payment) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Payment #" + payment.getPaymentId(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Edit Payment", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        // Form fields
        formPanel.add(new JLabel("Amount Due:"));
        JTextField amountDueField = new JTextField(String.valueOf(payment.getDueAmount()));
        formPanel.add(amountDueField);

        formPanel.add(new JLabel("Amount Paid:"));
        JTextField amountPaidField = new JTextField(String.valueOf(payment.getPaidAmount()));
        formPanel.add(amountPaidField);

        formPanel.add(new JLabel("Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(new String[] { "Cash", "Card", "Bank Transfer", "Mobile" });
        methodCombo.setSelectedItem(payment.getPaidBy() != null ? payment.getPaidBy() : "Cash");
        formPanel.add(methodCombo);

        formPanel.add(new JLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[] { "PENDING", "PAID", "PARTIAL" });
        statusCombo.setSelectedItem(payment.getStatusText());
        formPanel.add(statusCombo);

        formPanel.add(new JLabel("Remarks:"));
        JTextField remarksField = new JTextField(payment.getRemarks() != null ? payment.getRemarks() : "");
        formPanel.add(remarksField);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("💾 Save Changes");
        saveButton.setBackground(SUCCESS);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            try {
                // Update payment
                payment.setDueAmount(Double.parseDouble(amountDueField.getText()));
                payment.setPaidAmount(Double.parseDouble(amountPaidField.getText()));
                payment.setPaidBy((String) methodCombo.getSelectedItem());
                payment.setRemarks(remarksField.getText());

                // Set status
                String status = (String) statusCombo.getSelectedItem();
                if (status.equals("PAID")) {
                    payment.setPaymentStatus(Payment.STATUS_PAID);
                } else if (status.equals("PARTIAL")) {
                    payment.setPaymentStatus(Payment.STATUS_PARTIAL);
                } else {
                    payment.setPaymentStatus(Payment.STATUS_PENDING);
                }

                // Save to database
                boolean success = paymentDAO.update(payment);
                if (success) {
                    dialog.dispose();
                    showSuccess("Success", "Payment updated successfully!");
                    loadData();
                } else {
                    showError("Error", "Failed to update payment");
                }
            } catch (NumberFormatException ex) {
                showError("Input Error", "Please enter valid numbers");
            } catch (SQLException ex) {
                showError("Database Error", ex.getMessage());
            }
        });

        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setBackground(DANGER);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        content.add(title, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private void receivePayment(Payment payment) {
        double dueAmount = payment.getDueAmount() != null ? payment.getDueAmount() : 0;
        double paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
        double balance = payment.getBalance();

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Amount Due:"));
        panel.add(new JLabel("$" + String.format("%.2f", dueAmount)));

        panel.add(new JLabel("Already Paid:"));
        panel.add(new JLabel("$" + String.format("%.2f", paidAmount)));

        panel.add(new JLabel("Balance:"));
        panel.add(new JLabel("$" + String.format("%.2f", balance)));

        panel.add(new JLabel("Amount to Receive:"));
        JTextField amountField = new JTextField(String.format("%.2f", balance));
        panel.add(amountField);

        panel.add(new JLabel("Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(new String[] { "Cash", "Card", "Bank Transfer", "Mobile" });
        panel.add(methodCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Receive Payment #" + payment.getPaymentId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double receivedAmount = Double.parseDouble(amountField.getText());
                String method = (String) methodCombo.getSelectedItem();

                if (receivedAmount <= 0) {
                    showError("Error", "Amount must be greater than 0");
                    return;
                }

                // Update payment
                double newPaidAmount = paidAmount + receivedAmount;
                payment.setPaidAmount(newPaidAmount);
                payment.setPaidBy(method);
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
                    showSuccess("Success", "Payment received successfully!");
                    loadData();
                } else {
                    showError("Error", "Failed to save payment");
                }
            } catch (NumberFormatException e) {
                showError("Input Error", "Please enter a valid amount");
            } catch (SQLException e) {
                showError("Database Error", e.getMessage());
            }
        }
    }

    private void deletePayment(Payment payment) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Payment #" + payment.getPaymentId() + "?\n" +
                        "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        paymentDAO.delete(payment.getPaymentId());
                        SwingUtilities.invokeLater(() -> {
                            showSuccess("Deleted", "Payment deleted successfully!");
                            loadData();
                        });
                    } catch (SQLException e) {
                        SwingUtilities.invokeLater(() -> showError("Delete Error", e.getMessage()));
                    }
                    return null;
                }
            };
            worker.execute();
        }
    }

    private void exportPayments() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Payments to CSV");
        fileChooser.setSelectedFile(new File("payments_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                exportToCSV(file);
                showSuccess("Export Successful",
                        "Payments exported to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Write header
            writer.println("Payment ID,Booking ID,Customer,Amount Due,Amount Paid,Balance,Method,Status,Date,Remarks");

            // Write data
            for (Payment payment : paymentList) {
                String customer = payment.getFullName() != null ? payment.getFullName()
                        : (payment.getUserName() != null ? payment.getUserName() : "Unknown");

                writer.println(String.format("%d,%d,\"%s\",%.2f,%.2f,%.2f,\"%s\",\"%s\",\"%s\",\"%s\"",
                        payment.getPaymentId(),
                        payment.getBookingId(),
                        customer,
                        payment.getDueAmount(),
                        payment.getPaidAmount(),
                        payment.getBalance(),
                        payment.getPaidBy() != null ? payment.getPaidBy() : "Cash",
                        payment.getStatusText(),
                        payment.getPaymentDate() != null
                                ? new SimpleDateFormat("yyyy-MM-dd").format(payment.getPaymentDate())
                                : "",
                        payment.getRemarks() != null ? payment.getRemarks() : ""));
            }
        }
    }

    private void printReport() {
        showInfo("Print Report", "Print functionality will be implemented soon!");
    }

    // ================= HELPER METHODS =================
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // ================= TABLE CELL CLASSES =================
    class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return (Component) value;
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;

        @Override
        public Object getCellEditorValue() {
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            panel = (JPanel) value;
            return panel;
        }
    }
}