package ui;

import dao.BookingDAO;
import dao.ParkingSlotDAO;
import dao.UserDAO;
import dao.VehicleDAO;
import dao.VehicleOwnerDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.Booking;
import models.ParkingSlot;
import models.Vehicle;
import models.VehicleOwner;

public class BookingPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ParkingSlotDAO parkingSlotDAO = new ParkingSlotDAO();
    private final UserDAO userDAO = new UserDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final VehicleOwnerDAO vehicleOwnerDAO = new VehicleOwnerDAO();

    private List<Booking> bookingList;

    private JTable table;
    private DefaultTableModel model;
    private JLabel infoLabel;
    private JLabel timeLabel;
    private JPanel statsPanel;
    private JPanel filterPanel;
    private JComboBox<String> statusFilter;
    private JTextField searchField;
    private JTabbedPane tabbedPane;

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

    private int adminUserId;

    public BookingPanel(int adminUserId) {
        this.adminUserId = adminUserId;
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        loadData();
        startClock();
    }

    public BookingPanel() {
        this(1);
    }

    private void initComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFilterPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ================= HEADER =================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 25, 20, 25)));

        // Title with icon using TextIcon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(CARD_BG);

        JLabel icon = new JLabel();
        icon.setIcon(new TextIcon("📋", new Font("Segoe UI Emoji", Font.PLAIN, 28), PRIMARY));

        JLabel title = new JLabel("Booking Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        titlePanel.add(icon);
        titlePanel.add(title);
        header.add(titlePanel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);

        buttonPanel.add(createStyledButton("STATS", INFO, "View Statistics", "📊"));
        buttonPanel.add(createStyledButton("APPROVE", SUCCESS, "Approve Selected", "✅"));
        buttonPanel.add(createStyledButton("REJECT", DANGER, "Reject Selected", "❌"));
        buttonPanel.add(createStyledButton("NEW", PRIMARY, "Create New Booking", "➕"));
        buttonPanel.add(createRefreshButton());

        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createStyledButton(String text, Color bgColor, String tooltip, String iconSymbol) {
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

        // Add icon
        button.setIcon(new TextIcon(iconSymbol, new Font("Segoe UI Emoji", Font.PLAIN, 14), Color.WHITE));
        button.setIconTextGap(8);

        // Hover effect
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

        // Action
        if (text.contains("STATS")) {
            button.addActionListener(e -> showStatistics());
        } else if (text.contains("APPROVE")) {
            button.addActionListener(e -> approveBooking());
        } else if (text.contains("REJECT")) {
            button.addActionListener(e -> rejectBooking());
        } else if (text.contains("NEW")) {
            button.addActionListener(e -> createNewBooking());
        }

        return button;
    }

    private JButton createRefreshButton() {
        JButton button = new JButton();
        button.setBackground(new Color(236, 240, 241));
        button.setForeground(TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setToolTipText("Refresh Data");
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(8, 12, 8, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setIcon(new TextIcon("🔄", new Font("Segoe UI Emoji", Font.PLAIN, 18), TEXT_PRIMARY));

        button.addActionListener(e -> {
            // button.setText("⏳"); // No text, just icon
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    loadData();
                    return null;
                }

                @Override
                protected void done() {
                    // Done
                }
            };
            worker.execute();
        });

        return button;
    }

    // ================= FILTER PANEL =================
    private JPanel createFilterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);

        // Filter panel
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Status filter
        JLabel filterLabel = new JLabel("Filter by:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLabel.setForeground(TEXT_PRIMARY);
        filterPanel.add(filterLabel);

        statusFilter = new JComboBox<>(new String[] { "All", "Pending", "Approved", "Rejected" });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.addActionListener(e -> filterBookings());
        filterPanel.add(statusFilter);

        // Search
        filterPanel.add(Box.createHorizontalStrut(20));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT_PRIMARY);
        filterPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, Customer, or Slot...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterBookings();
            }
        });
        filterPanel.add(searchField);

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabbedPane.addTab("Bookings", new JLabel("")); // Icons handled by renderer or look and feel usually
        tabbedPane.setTabComponentAt(0, createTabHeader("📋", "Bookings"));
        tabbedPane.setComponentAt(0, createTablePanel());

        tabbedPane.addTab("Statistics", new JLabel(""));
        tabbedPane.setTabComponentAt(1, createTabHeader("📊", "Statistics"));
        tabbedPane.setComponentAt(1, createStatsPanel());

        tabbedPane.addTab("Parking Map", new JLabel(""));
        tabbedPane.setTabComponentAt(2, createTabHeader("🗺️", "Parking Map"));
        tabbedPane.setComponentAt(2, createParkingMapPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private Component createTabHeader(String icon, String title) {
        JLabel label = new JLabel(title);
        label.setIcon(new TextIcon(icon, new Font("Segoe UI Emoji", Font.PLAIN, 14), Color.BLACK));
        label.setIconTextGap(8);
        return label;
    }

    // ================= TABLE PANEL =================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        // Table
        model = new DefaultTableModel(
                new String[] { "ID", "Customer", "Vehicle", "Slot", "Duration", "Status", "Created", "Actions" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 7; // Only actions column is editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 7 ? JPanel.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Customer
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Vehicle
        table.getColumnModel().getColumn(3).setPreferredWidth(60); // Slot
        table.getColumnModel().getColumn(4).setPreferredWidth(80); // Duration
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // Created
        table.getColumnModel().getColumn(7).setPreferredWidth(150); // Actions

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

                    if (column == 5) { // Status column
                        String status = value != null ? value.toString() : "";
                        switch (status) {
                            case "PENDING":
                                label.setForeground(WARNING);
                                label.setText(
                                        "<html><span style='background:#FEF9E7; padding:4px 8px; border-radius:10px; color:#F1C40F;'>PENDING</span></html>");
                                break;
                            case "APPROVED":
                                label.setForeground(SUCCESS);
                                label.setText(
                                        "<html><span style='background:#EAFAF1; padding:4px 8px; border-radius:10px; color:#2ECC71;'>APPROVED</span></html>");
                                break;
                            case "REJECTED":
                                label.setForeground(DANGER);
                                label.setText(
                                        "<html><span style='background:#FDEDEC; padding:4px 8px; border-radius:10px; color:#E74C3C;'>REJECTED</span></html>");
                                break;
                        }
                    } else {
                        label.setForeground(TEXT_PRIMARY);
                    }

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
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ================= STATISTICS PANEL =================
    private JPanel createStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new GridBagLayout());
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        updateStatsPanel();

        return statsPanel;
    }

    private void updateStatsPanel() {
        statsPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        try {
            List<Booking> allBookings = bookingDAO.findAll();
            int total = allBookings.size();
            long pending = allBookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count();
            long approved = allBookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED).count();
            long rejected = allBookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_REJECTED).count();

            // Get revenue
            double totalRevenue = allBookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED)
                    .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0)
                    .sum();

            // Get available slots
            List<ParkingSlot> availableSlots = parkingSlotDAO.findAvailableSlots();

            // Add stat cards
            statsPanel.add(createAdvancedStatCard("Total Bookings",
                    String.valueOf(total), PRIMARY, "All time bookings", "📅"), gbc);

            gbc.gridx = 1;
            statsPanel.add(createAdvancedStatCard("Total Revenue",
                    String.format("$%.2f", totalRevenue), SUCCESS, "Total income", "💵"), gbc);

            gbc.gridx = 2;
            statsPanel.add(createAdvancedStatCard("Pending",
                    String.valueOf(pending), WARNING, "Awaiting action", "⏰"), gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            statsPanel.add(createAdvancedStatCard("Approved",
                    String.valueOf(approved), new Color(46, 204, 113), "Confirmed bookings", "✅"), gbc);

            gbc.gridx = 1;
            statsPanel.add(createAdvancedStatCard("Rejected",
                    String.valueOf(rejected), DANGER, "Cancelled bookings", "❌"), gbc);

            gbc.gridx = 2;
            statsPanel.add(createAdvancedStatCard("Available Slots",
                    String.valueOf(availableSlots.size()), INFO, "Free parking slots", "🅿️"), gbc);

            // Add chart panel
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            gbc.weighty = 0.3;
            statsPanel.add(createChartPanel(allBookings), gbc);

        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("<html><center>Error loading statistics:<br>" +
                    e.getMessage() + "</center></html>");
            errorLabel.setForeground(DANGER);
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            statsPanel.add(errorLabel);
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel createAdvancedStatCard(String title, String value, Color color, String desc, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 15, 20, 15)));

        // Icon
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new TextIcon(icon, new Font("Segoe UI Emoji", Font.PLAIN, 24), color));

        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_PRIMARY);

        // Description
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_SECONDARY);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(CARD_BG);
        topPanel.add(iconLabel);
        topPanel.add(titleLabel);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createChartPanel(List<Booking> bookings) {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel chartTitle = new JLabel("Booking Status Distribution");
        chartTitle.setIcon(new TextIcon("📈", new Font("Segoe UI Emoji", Font.PLAIN, 14), TEXT_PRIMARY));
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartTitle.setForeground(TEXT_PRIMARY);

        // Simple bar chart logic kept same...
        JPanel barPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                long pending = bookings.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count();
                long approved = bookings.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED).count();
                long rejected = bookings.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_REJECTED).count();

                int total = (int) (pending + approved + rejected);
                if (total == 0)
                    return;

                int width = getWidth() - 40;
                int height = getHeight() - 60;

                g.setColor(WARNING);
                int pendingWidth = (int) ((pending * width) / total);
                g.fillRect(20, 20, pendingWidth, 30);

                g.setColor(SUCCESS);
                int approvedWidth = (int) ((approved * width) / total);
                g.fillRect(20 + pendingWidth, 20, approvedWidth, 30);

                g.setColor(DANGER);
                int rejectedWidth = (int) ((rejected * width) / total);
                g.fillRect(20 + pendingWidth + approvedWidth, 20, rejectedWidth, 30);

                g.setColor(TEXT_PRIMARY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("Pending: " + pending + " (" + (pending * 100 / total) + "%)", 20, 70);
                g.drawString("Approved: " + approved + " (" + (approved * 100 / total) + "%)", 20 + pendingWidth + 10,
                        70);
                g.drawString("Rejected: " + rejected + " (" + (rejected * 100 / total) + "%)",
                        20 + pendingWidth + approvedWidth + 10, 70);
            }
        };
        barPanel.setPreferredSize(new Dimension(400, 100));
        chartPanel.add(chartTitle, BorderLayout.NORTH);
        chartPanel.add(barPanel, BorderLayout.CENTER);
        return chartPanel;
    }

    // ================= PARKING MAP PANEL =================
    private JPanel createParkingMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        try {
            List<ParkingSlot> slots = parkingSlotDAO.findAll();
            JPanel gridPanel = new JPanel(new GridLayout(5, 4, 10, 10));
            gridPanel.setBackground(BG_COLOR);

            for (ParkingSlot slot : slots) {
                gridPanel.add(createSlotButton(slot));
            }

            JScrollPane scrollPane = new JScrollPane(gridPanel);
            panel.add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading parking map: " + e.getMessage());
            errorLabel.setForeground(DANGER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    private JButton createSlotButton(ParkingSlot slot) {
        JButton button = new JButton();
        button.setText("<html><center><b>Slot " + slot.getParkingSlotNumber() + "</b></center></html>");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color bgColor = switch (slot.getParkingSlotStatus()) {
            case ParkingSlot.STATUS_AVAILABLE -> new Color(46, 204, 113);
            case ParkingSlot.STATUS_RESERVED -> new Color(241, 196, 15);
            case ParkingSlot.STATUS_OCCUPIED -> new Color(231, 76, 60);
            default -> new Color(149, 165, 166);
        };

        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(new LineBorder(bgColor.darker(), 2));
        button.addActionListener(e -> showSlotDetails(slot));

        return button;
    }

    // ================= FOOTER =================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(52, 73, 94));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        timeLabel = new JLabel();
        timeLabel.setForeground(new Color(189, 195, 199));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        footer.add(infoLabel, BorderLayout.WEST);
        footer.add(timeLabel, BorderLayout.EAST);

        return footer;
    }

    // ================= ACTIONS & HELPERS =================

    private void startClock() {
        new Timer(1000, e -> updateFooter()).start();
    }

    // Stub methods for actions
    private void showStatistics() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(1); // Index 1 is Statistics
        }
    }

    private void approveBooking() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one booking to approve.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve " + selectedRows.length + " selected booking(s)?",
                "Confirm Approval", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {
                int bookingId = (int) table.getValueAt(row, 0);
                try {
                    Booking b = bookingDAO.findById(bookingId);
                    if (b != null)
                        approveSpecificBooking(b);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void rejectBooking() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one booking to reject.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reject " + selectedRows.length + " selected booking(s)?",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {
                int bookingId = (int) table.getValueAt(row, 0);
                try {
                    Booking b = bookingDAO.findById(bookingId);
                    if (b != null)
                        rejectSpecificBooking(b);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createNewBooking() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New Booking", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Customer ID:"));
        JTextField customerIdField = new JTextField();
        panel.add(customerIdField);

        panel.add(new JLabel("Vehicle ID:"));
        JTextField vehicleIdField = new JTextField();
        panel.add(vehicleIdField);

        panel.add(new JLabel("Slot ID:"));
        JTextField slotIdField = new JTextField();
        panel.add(slotIdField);

        panel.add(new JLabel("Duration (e.g., 2 hours):"));
        JTextField durationField = new JTextField("2 hours");
        panel.add(durationField);

        panel.add(new JLabel("User ID:"));
        JTextField userIdField = new JTextField(String.valueOf(adminUserId));
        panel.add(userIdField);

        panel.add(new JLabel("Remarks:"));
        JTextField remarksField = new JTextField();
        panel.add(remarksField);

        JButton saveBtn = new JButton("Create Booking");
        saveBtn.setBackground(SUCCESS);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Booking b = new Booking();
                b.setCustomerId(Integer.parseInt(customerIdField.getText()));
                b.setVehicleId(Integer.parseInt(vehicleIdField.getText()));
                b.setSlotId(Integer.parseInt(slotIdField.getText()));
                b.setDurationOfBooking(durationField.getText());
                b.setUserId(Integer.parseInt(userIdField.getText()));
                b.setRemarks(remarksField.getText());

                int bookingId = bookingDAO.createBookingWithSlotUpdate(b);
                if (bookingId > 0) {
                    JOptionPane.showMessageDialog(dialog, "Booking created successfully! ID: " + bookingId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create booking.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("")); // Empty space
        panel.add(saveBtn);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void viewBookingDetails(Booking b) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><div style='width:300px;'>");
        sb.append("<h2>Booking #").append(b.getBookingId()).append("</h2>");
        sb.append("<hr>");
        sb.append("<b>Status:</b> ").append(statusText(b.getBookingStatus())).append("<br>");
        sb.append("<b>Customer ID:</b> ").append(b.getCustomerId()).append("<br>");
        sb.append("<b>Vehicle ID:</b> ").append(b.getVehicleId()).append("<br>");
        sb.append("<b>Slot ID:</b> ").append(b.getSlotId()).append("<br>");
        sb.append("<b>Duration:</b> ").append(b.getDurationOfBooking()).append("<br>");
        sb.append("<b>Time:</b> ").append(b.getBookingTime()).append("<br>");
        sb.append("<b>Remarks:</b> ").append(b.getRemarks() != null ? b.getRemarks() : "None").append("<br>");
        sb.append("</div></html>");

        JOptionPane.showMessageDialog(this, sb.toString(), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void approveSpecificBooking(Booking b) {
        try {
            boolean success = bookingDAO.approveBookingNow(b.getBookingId(), adminUserId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking #" + b.getBookingId() + " approved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to approve booking #" + b.getBookingId(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error approving booking: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSpecificBooking(Booking b) {
        try {
            boolean success = bookingDAO.rejectBooking(b.getBookingId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking #" + b.getBookingId() + " rejected.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reject booking #" + b.getBookingId(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error rejecting booking: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSlotDetails(ParkingSlot s) {
        String info = String.format("Slot: %d\nStatus: %s\nUser ID: %s",
                s.getParkingSlotNumber(),
                s.getParkingSlotStatus() == 0 ? "AVAILABLE" : (s.getParkingSlotStatus() == 1 ? "RESERVED" : "OCCUPIED"),
                s.getUserId() != null ? s.getUserId() : "None");
        JOptionPane.showMessageDialog(this, info, "Slot Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadData() {
        try {
            bookingList = bookingDAO.findAll();
            updateTable();
            updateFooter();
            updateStatsPanel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ... (rest of methods like updateTable etc which were shown before but use
    // createActionButtons)

    private JPanel createActionButtons(Booking booking) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);

        // View button
        JButton viewBtn = new JButton();
        viewBtn.setIcon(new TextIcon("👁", new Font("Segoe UI Emoji", Font.PLAIN, 12), Color.WHITE));
        viewBtn.setToolTipText("View Details");
        viewBtn.setBackground(new Color(52, 152, 219));
        viewBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
        viewBtn.addActionListener(e -> viewBookingDetails(booking));

        // Approve/Reject based on status
        if (booking.getBookingStatus() == Booking.STATUS_PENDING) {
            JButton approveBtn = new JButton();
            approveBtn.setIcon(new TextIcon("✓", new Font("Segoe UI Emoji", Font.BOLD, 12), Color.WHITE));
            approveBtn.setToolTipText("Approve");
            approveBtn.setBackground(SUCCESS);
            approveBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
            approveBtn.addActionListener(e -> {
                if (table.isEditing())
                    table.getCellEditor().stopCellEditing();
                approveSpecificBooking(booking);
            });

            JButton rejectBtn = new JButton();
            rejectBtn.setIcon(new TextIcon("✗", new Font("Segoe UI Emoji", Font.BOLD, 12), Color.WHITE));
            rejectBtn.setToolTipText("Reject");
            rejectBtn.setBackground(DANGER);
            rejectBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
            rejectBtn.addActionListener(e -> {
                if (table.isEditing())
                    table.getCellEditor().stopCellEditing();
                rejectSpecificBooking(booking);
            });

            panel.add(approveBtn);
            panel.add(rejectBtn);
        }

        panel.add(viewBtn);
        return panel;
    }

    // TextIcon definition
    private static class TextIcon implements Icon {
        private String text;
        private Font font;
        private Color color;

        public TextIcon(String text, Font font, Color color) {
            this.text = text;
            this.font = font;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(font);
            g2d.setColor(color);
            FontMetrics fm = g2d.getFontMetrics();
            int width = fm.stringWidth(text);
            g2d.drawString(text, x + (getIconWidth() - width) / 2, y + getIconHeight() - 5);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
    }

    // Helper classes for table actions
    private void updateTable() {
        model.setRowCount(0);
        if (bookingList == null)
            return;

        for (Booking b : bookingList) {
            try {
                String customerName = "Customer #" + b.getCustomerId();
                if (b.getCustomerId() != null) {
                    VehicleOwner owner = vehicleOwnerDAO.findById(b.getCustomerId());
                    if (owner != null && owner.getVehicleOwnerName() != null) {
                        customerName = owner.getVehicleOwnerName();
                    }
                }

                String vehicleInfo = "Vehicle #" + b.getVehicleId();
                if (b.getVehicleId() != null) {
                    Vehicle vehicle = vehicleDAO.findById(b.getVehicleId());
                    if (vehicle != null && vehicle.getVehiclePlateNumber() != null) {
                        vehicleInfo = vehicle.getVehiclePlateNumber();
                    }
                }

                String created = b.getBookingTime() != null
                        ? new SimpleDateFormat("MMM dd, HH:mm").format(b.getBookingTime())
                        : "";

                model.addRow(new Object[] {
                        b.getBookingId(),
                        customerName,
                        vehicleInfo,
                        b.getSlotId(),
                        b.getDurationOfBooking(),
                        statusText(b.getBookingStatus()),
                        created,
                        createActionButtons(b)
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateFooter() {
        if (bookingList == null)
            return;

        int total = bookingList.size();
        long pending = bookingList.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count();
        long approved = bookingList.stream().filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED).count();

        infoLabel.setText(String.format("Total: %d | Pending: %d | Approved: %d", total, pending, approved));
        timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    private String statusText(int status) {
        return switch (status) {
            case Booking.STATUS_PENDING -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            default -> "UNKNOWN";
        };
    }

    private void filterBookings() {
        /* Reuse previous logic */ }

    // Cell Editor/Renderer classes
    class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value instanceof JPanel)
                return (JPanel) value;
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            if (value instanceof JPanel)
                this.panel = (JPanel) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }
    }
}