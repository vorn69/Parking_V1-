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
import models.User;
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
            new EmptyBorder(20, 25, 20, 25)
        ));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(CARD_BG);
        
        JLabel icon = new JLabel("📋");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        
        JLabel title = new JLabel("Booking Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        
        titlePanel.add(icon);
        titlePanel.add(title);
        header.add(titlePanel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);
        
        buttonPanel.add(createStyledButton("📊 STATS", INFO, "View Statistics"));
        buttonPanel.add(createStyledButton("✅ APPROVE", SUCCESS, "Approve Selected"));
        buttonPanel.add(createStyledButton("❌ REJECT", DANGER, "Reject Selected"));
        buttonPanel.add(createStyledButton("➕ NEW", PRIMARY, "Create New Booking"));
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
            new EmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
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
        JButton button = new JButton("🔄");
        button.setBackground(new Color(236, 240, 241));
        button.setForeground(TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setToolTipText("Refresh Data");
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
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
        
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
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
        
        // Tabbed pane for table and stats
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        tabbedPane.addTab("📋 Bookings", createTablePanel());
        tabbedPane.addTab("📊 Statistics", createStatsPanel());
        tabbedPane.addTab("🗺️ Parking Map", createParkingMapPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    // ================= TABLE PANEL =================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        
        // Table
        model = new DefaultTableModel(
            new String[]{"ID", "Customer", "Vehicle", "Slot", "Duration", "Status", "Created", "Actions"},
            0
        ) {
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
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Customer
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Vehicle
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // Slot
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Duration
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
                                label.setText("<html><span style='background:#FEF9E7; padding:4px 8px; border-radius:10px;'>⏳ " + status + "</span></html>");
                                break;
                            case "APPROVED":
                                label.setForeground(SUCCESS);
                                label.setText("<html><span style='background:#EAFAF1; padding:4px 8px; border-radius:10px;'>✅ " + status + "</span></html>");
                                break;
                            case "REJECTED":
                                label.setForeground(DANGER);
                                label.setText("<html><span style='background:#FDEDEC; padding:4px 8px; border-radius:10px;'>❌ " + status + "</span></html>");
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
            
            // Get today's bookings
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            long todayBookings = allBookings.stream()
                .filter(b -> b.getBookingTime() != null && 
                           sdf.format(b.getBookingTime()).equals(today))
                .count();
            
            // Get available slots
            List<ParkingSlot> availableSlots = parkingSlotDAO.findAvailableSlots();
            
            // Add stat cards
            statsPanel.add(createAdvancedStatCard("📊 Total Bookings", 
                String.valueOf(total), PRIMARY, "All time bookings", "calendar"), gbc);
            
            gbc.gridx = 1;
            statsPanel.add(createAdvancedStatCard("💰 Total Revenue", 
                String.format("$%.2f", totalRevenue), SUCCESS, "Total income", "dollar"), gbc);
            
            gbc.gridx = 2;
            statsPanel.add(createAdvancedStatCard("⏳ Pending", 
                String.valueOf(pending), WARNING, "Awaiting action", "clock"), gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            statsPanel.add(createAdvancedStatCard("✅ Approved", 
                String.valueOf(approved), new Color(46, 204, 113), "Confirmed bookings", "check"), gbc);
            
            gbc.gridx = 1;
            statsPanel.add(createAdvancedStatCard("❌ Rejected", 
                String.valueOf(rejected), DANGER, "Cancelled bookings", "cancel"), gbc);
            
            gbc.gridx = 2;
            statsPanel.add(createAdvancedStatCard("🅿️ Available Slots", 
                String.valueOf(availableSlots.size()), INFO, "Free parking slots", "parking"), gbc);
            
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
            new EmptyBorder(20, 15, 20, 15)
        ));
        
        // Icon
        JLabel iconLabel = new JLabel(getIconEmoji(icon));
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        
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

    private String getIconEmoji(String icon) {
        return switch (icon) {
            case "calendar" -> "📅";
            case "dollar" -> "💵";
            case "clock" -> "⏰";
            case "check" -> "✅";
            case "cancel" -> "❌";
            case "parking" -> "🅿️";
            default -> "📊";
        };
    }

    private JPanel createChartPanel(List<Booking> bookings) {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel chartTitle = new JLabel("📈 Booking Status Distribution");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartTitle.setForeground(TEXT_PRIMARY);
        
        // Simple bar chart
        JPanel barPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                long pending = bookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count();
                long approved = bookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED).count();
                long rejected = bookings.stream()
                    .filter(b -> b.getBookingStatus() == Booking.STATUS_REJECTED).count();
                
                int total = (int)(pending + approved + rejected);
                if (total == 0) return;
                
                int width = getWidth() - 40;
                int height = getHeight() - 60;
                
                // Draw bars
                g.setColor(WARNING);
                int pendingWidth = (int)((pending * width) / total);
                g.fillRect(20, 20, pendingWidth, 30);
                
                g.setColor(SUCCESS);
                int approvedWidth = (int)((approved * width) / total);
                g.fillRect(20 + pendingWidth, 20, approvedWidth, 30);
                
                g.setColor(DANGER);
                int rejectedWidth = (int)((rejected * width) / total);
                g.fillRect(20 + pendingWidth + approvedWidth, 20, rejectedWidth, 30);
                
                // Draw labels
                g.setColor(TEXT_PRIMARY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("Pending: " + pending + " (" + (pending*100/total) + "%)", 20, 70);
                g.drawString("Approved: " + approved + " (" + (approved*100/total) + "%)", 20 + pendingWidth + 10, 70);
                g.drawString("Rejected: " + rejected + " (" + (rejected*100/total) + "%)", 
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
        
        // Tooltip with details
        String tooltip = "<html>Slot " + slot.getParkingSlotNumber() + 
                        "<br>Status: " + slot.getStatusText() +
                        "<br>Type: " + (slot.getSlotType() != null ? slot.getSlotType() : "Regular") +
                        "<br>Zone: " + (slot.getZone() != null ? slot.getZone() : "General") + "</html>";
        button.setToolTipText(tooltip);
        
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

    // ================= DATA LOADING =================
    private void loadData() {
        try {
            bookingList = bookingDAO.findAll();
            updateTable();
            updateFooter();
            updateStatsPanel();
            
        } catch (SQLException e) {
            showMessage("Database Error", "Failed to load bookings: " + e.getMessage(), 
                       "error", DANGER);
        }
    }

    private void updateTable() {
        model.setRowCount(0);
        
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
                
                String createdDate = "";
                if (b.getBookingTime() != null) {
                    createdDate = new SimpleDateFormat("MMM dd, HH:mm").format(b.getBookingTime());
                }
                
                model.addRow(new Object[]{
                    b.getBookingId(),
                    customerName,
                    vehicleInfo,
                    b.getSlotId(),
                    b.getDurationOfBooking(),
                    statusText(b.getBookingStatus()),
                    createdDate,
                    createActionButtons(b)
                });
                
            } catch (SQLException e) {
                System.err.println("Error loading booking details: " + e.getMessage());
            }
        }
    }

    private void updateFooter() {
        long pending = bookingList != null ? bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count() : 0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a");
        infoLabel.setText("<html>📊 <b>" + (bookingList != null ? bookingList.size() : 0) + 
                         "</b> bookings | ⏳ <b>" + pending + "</b> pending</html>");
        timeLabel.setText("🕒 " + sdf.format(new Date()));
    }

    private void filterBookings() {
        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (bookingList == null) return null;
                
                List<Booking> filtered = bookingList.stream()
                    .filter(b -> {
                        // Status filter
                        if (!selectedStatus.equals("All")) {
                            String status = statusText(b.getBookingStatus());
                            if (!status.equalsIgnoreCase(selectedStatus)) return false;
                        }
                        
                        // Search filter
                        if (!searchText.isEmpty()) {
                            boolean matches = String.valueOf(b.getBookingId()).contains(searchText) ||
                                            String.valueOf(b.getCustomerId()).contains(searchText) ||
                                            String.valueOf(b.getSlotId()).contains(searchText) ||
                                            statusText(b.getBookingStatus()).toLowerCase().contains(searchText);
                            if (!matches) return false;
                        }
                        
                        return true;
                    })
                    .toList();
                
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (Booking b : filtered) {
                        try {
                            String customerName = "Customer #" + b.getCustomerId();
                            String vehicleInfo = "Vehicle #" + b.getVehicleId();
                            String createdDate = b.getBookingTime() != null ? 
                                new SimpleDateFormat("MMM dd, HH:mm").format(b.getBookingTime()) : "";
                            
                            model.addRow(new Object[]{
                                b.getBookingId(),
                                customerName,
                                vehicleInfo,
                                b.getSlotId(),
                                b.getDurationOfBooking(),
                                statusText(b.getBookingStatus()),
                                createdDate,
                                createActionButtons(b)
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

    private String statusText(int status) {
        return switch (status) {
            case Booking.STATUS_PENDING -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            default -> "UNKNOWN";
        };
    }

    private JPanel createActionButtons(Booking booking) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);
        
        // View button
        JButton viewBtn = new JButton("👁");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewBtn.setToolTipText("View Details");
        viewBtn.setBackground(new Color(52, 152, 219));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
        viewBtn.addActionListener(e -> viewBookingDetails(booking));
        
        // Approve/Reject based on status
        if (booking.getBookingStatus() == Booking.STATUS_PENDING) {
            JButton approveBtn = new JButton("✓");
            approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            approveBtn.setToolTipText("Approve");
            approveBtn.setBackground(SUCCESS);
            approveBtn.setForeground(Color.WHITE);
            approveBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
            approveBtn.addActionListener(e -> {
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
                approveSpecificBooking(booking);
            });
            
            JButton rejectBtn = new JButton("✗");
            rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rejectBtn.setToolTipText("Reject");
            rejectBtn.setBackground(DANGER);
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setBorder(new EmptyBorder(5, 8, 5, 8));
            rejectBtn.addActionListener(e -> {
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
                rejectSpecificBooking(booking);
            });
            
            panel.add(approveBtn);
            panel.add(rejectBtn);
        }
        
        panel.add(viewBtn);
        
        return panel;
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            timeLabel.setText("🕒 " + sdf.format(new Date()));
        });
        timer.start();
    }

    // ================= ACTION METHODS =================
    private void showStatistics() {
        // Already shown in tabbed pane
    }

    private void approveBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showMessage("Select Booking", "Please select a booking to approve", 
                       "warning", WARNING);
            return;
        }

        int bookingId = (int) model.getValueAt(row, 0);
        Booking booking = bookingList.stream()
            .filter(b -> b.getBookingId() == bookingId)
            .findFirst()
            .orElse(null);
        
        if (booking != null) {
            approveSpecificBooking(booking);
        }
    }

    private void rejectBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showMessage("Select Booking", "Please select a booking to reject", 
                       "warning", WARNING);
            return;
        }

        int bookingId = (int) model.getValueAt(row, 0);
        Booking booking = bookingList.stream()
            .filter(b -> b.getBookingId() == bookingId)
            .findFirst()
            .orElse(null);
        
        if (booking != null) {
            rejectSpecificBooking(booking);
        }
    }

private void approveSpecificBooking(Booking booking) {
    try {
        // Calculate estimated amount
        double estimatedAmount = calculateEstimatedAmount(booking.getDurationOfBooking());
        
        // Show detailed confirmation
        String message = "<html><div style='width:400px;'>" +
            "<h3>Approve Booking #" + booking.getBookingId() + "</h3>" +
            "<table border='0' cellpadding='5'>" +
            "<tr><td><b>Slot:</b></td><td>#" + booking.getSlotId() + "</td></tr>" +
            "<tr><td><b>Duration:</b></td><td>" + booking.getDurationOfBooking() + "</td></tr>" +
            "<tr><td><b>Estimated Amount:</b></td><td>$" + String.format("%.2f", estimatedAmount) + "</td></tr>" +
            "</table>" +
            "<br><p><b>This will:</b></p>" +
            "<ol>" +
            "<li>✅ Change booking status to APPROVED</li>" +
            "<li>🅿️ Mark slot #" + booking.getSlotId() + " as OCCUPIED</li>" +
            "<li>💰 Create payment record ($" + String.format("%.2f", estimatedAmount) + ")</li>" +
            "</ol>" +
            "<p>Proceed with approval?</p>" +
            "</div></html>";
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            message, 
            "Confirm Approval", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        // Show progress
        JDialog progressDialog = createProgressDialog("Approving booking...");
        
        SwingWorker<Boolean, String> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Starting approval process...");
                Thread.sleep(500);
                
                publish("Validating booking...");
                Thread.sleep(300);
                
                publish("Updating records...");
                boolean result = bookingDAO.approveBookingNow(booking.getBookingId(), adminUserId);
                
                publish("Finalizing...");
                Thread.sleep(300);
                
                return result;
            }
            
            @Override
            protected void process(List<String> messages) {
                if (!messages.isEmpty()) {
                    // Update progress dialog
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    boolean success = get();
                    if (success) {
                        showSuccessDialog(booking, estimatedAmount);
                        loadData();
                    }
                } catch (Exception e) {
                    showErrorDialog(booking, e);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
        
    } catch (Exception e) {
        showErrorDialog(booking, e);
    }
}

private double calculateEstimatedAmount(String duration) {
    // Same logic as in DAO
    if (duration == null || duration.trim().isEmpty()) return 5.00;
    
    try {
        String lower = duration.toLowerCase();
        if (lower.contains("hour")) {
            String[] parts = duration.split(" ");
            double hours = Double.parseDouble(parts[0]);
            return Math.max(hours * 5.00, 2.00);
        } else if (lower.contains("day")) {
            String[] parts = duration.split(" ");
            double days = Double.parseDouble(parts[0]);
            return days * 50.00;
        } else {
            return 5.00;
        }
    } catch (Exception e) {
        return 5.00;
    }
}

private void showErrorDialog(Booking booking, Exception e) {
    // Get the root cause error message
    String errorMsg = e.getMessage();
    Throwable cause = e.getCause();
    while (cause != null) {
        errorMsg = cause.getMessage();
        cause = cause.getCause();
    }
    
    if (errorMsg == null || errorMsg.isEmpty()) {
        errorMsg = "Unknown error occurred";
    }
    
    // Format the error message nicely
    String formattedMessage = "<html><div style='width:500px;'>" +
        "<h3 style='color:red;'>❌ Approval Failed</h3>" +
        "<p><b>Booking #" + booking.getBookingId() + "</b> - Slot #" + booking.getSlotId() + "</p>" +
        "<hr>" +
        "<p><b>Error Details:</b></p>" +
        "<div style='background:#f8f9fa; padding:10px; border-left:4px solid #dc3545; margin:10px 0;'>" +
        errorMsg +
        "</div>" +
        "<hr>" +
        "<p><b>Possible Solutions:</b></p>" +
        "<ul style='margin-top:5px;'>" +
        "<li>Check database connection</li>" +
        "<li>Verify slot is not already occupied</li>" +
        "<li>Check if booking exists in database</li>" +
        "<li>Check payment table structure</li>" +
        "<li>Verify user permissions</li>" +
        "</ul>" +
        "<p style='color:#6c757d; font-size:12px; margin-top:15px;'>" +
        "Full error logged to console." +
        "</p>" +
        "</div></html>";
    
    // Show error dialog
    JOptionPane.showMessageDialog(
        this,
        formattedMessage,
        "Approval Error",
        JOptionPane.ERROR_MESSAGE
    );
    
    // Also print to console for debugging
    System.err.println("=== APPROVAL ERROR DETAILS ===");
    System.err.println("Booking ID: " + booking.getBookingId());
    System.err.println("Error: " + e.getMessage());
    e.printStackTrace();
    System.err.println("==============================");
}


private void showSuccessDialog(Booking booking, double amount) {
    String message = "<html><div style='width:450px; text-align:center;'>" +
        "<h2 style='color:green;'>✅ Approval Successful!</h2>" +
        "<table border='0' cellpadding='5' align='center'>" +
        "<tr><td><b>Booking ID:</b></td><td>#" + booking.getBookingId() + "</td></tr>" +
        "<tr><td><b>Slot:</b></td><td>#" + booking.getSlotId() + " (now OCCUPIED)</td></tr>" +
        "<tr><td><b>Amount Due:</b></td><td>$" + String.format("%.2f", amount) + "</td></tr>" +
        "<tr><td><b>Status:</b></td><td><span style='color:green; font-weight:bold;'>APPROVED</span></td></tr>" +
        "</table>" +
        "<br><p>✅ Payment record created<br>" +
        "✅ Customer can now use the slot<br>" +
        "✅ System updated successfully</p>" +
        "</div></html>";
    
    JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
}

private JDialog createProgressDialog(String message) {
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Processing", true);
    dialog.setLayout(new BorderLayout());
    dialog.setSize(350, 150);
    dialog.setLocationRelativeTo(this);
    
    JPanel content = new JPanel(new BorderLayout(20, 20));
    content.setBorder(new EmptyBorder(30, 30, 30, 30));
    content.setBackground(Color.WHITE);
    
    JLabel label = new JLabel("<html><center>" + message + "<br>Please wait...</center></html>");
    label.setHorizontalAlignment(SwingConstants.CENTER);
    
    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    
    content.add(label, BorderLayout.CENTER);
    content.add(progressBar, BorderLayout.SOUTH);
    dialog.add(content);
    
    return dialog;
}

    private void rejectSpecificBooking(Booking booking) {
        if (booking.getBookingStatus() != Booking.STATUS_PENDING) {
            showMessage("Invalid Action", "Only pending bookings can be rejected", 
                       "warning", WARNING);
            return;
        }

        int confirm = showCustomConfirmDialog(
            "Reject Booking #" + booking.getBookingId(),
            "<html><div style='width:300px;'>"
            + "<p>Reject this booking?</p>"
            + "<p>Slot " + booking.getSlotId() + " will be freed.</p>"
            + "<p style='color:#E74C3C; font-weight:bold;'>This action cannot be undone.</p>"
            + "</div></html>",
            DANGER
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingDAO.rejectBooking(booking.getBookingId());
                loadData();
                showMessage("Success", "Booking #" + booking.getBookingId() + " rejected successfully!", 
                           "success", SUCCESS);
            } catch (SQLException e) {
                showMessage("Error", "Failed to reject: " + e.getMessage(), 
                           "error", DANGER);
            }
        }
    }

    private void createNewBooking() {
        // Show dialog to create new booking
        showMessage("Coming Soon", "New booking feature will be added soon!", 
                   "info", INFO);
    }

    private void viewBookingDetails(Booking booking) {
        try {
            StringBuilder details = new StringBuilder();
            details.append("<html><div style='width:400px;'>");
            details.append("<h3>Booking #").append(booking.getBookingId()).append("</h3>");
            details.append("<table border='0' cellpadding='5'>");
            
            // Basic info
            details.append("<tr><td><b>Status:</b></td><td>").append(statusText(booking.getBookingStatus())).append("</td></tr>");
            details.append("<tr><td><b>Slot:</b></td><td>").append(booking.getSlotId()).append("</td></tr>");
            details.append("<tr><td><b>Duration:</b></td><td>").append(booking.getDurationOfBooking()).append("</td></tr>");
            
            // Customer info
            if (booking.getCustomerId() != null) {
                VehicleOwner owner = vehicleOwnerDAO.findById(booking.getCustomerId());
                if (owner != null) {
                    details.append("<tr><td><b>Customer:</b></td><td>").append(owner.getVehicleOwnerName()).append("</td></tr>");
                    details.append("<tr><td><b>Contact:</b></td><td>").append(owner.getVehicleOwnerContact()).append("</td></tr>");
                }
            }
            
            // Vehicle info
            if (booking.getVehicleId() != null) {
                Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                if (vehicle != null) {
                    details.append("<tr><td><b>Vehicle:</b></td><td>").append(vehicle.getVehiclePlateNumber()).append("</td></tr>");
                }
            }
            
            // Timing
            if (booking.getBookingTime() != null) {
                details.append("<tr><td><b>Booked:</b></td><td>")
                      .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(booking.getBookingTime()))
                      .append("</td></tr>");
            }
            
            // Amount
            if (booking.getTotalAmount() != null) {
                details.append("<tr><td><b>Amount:</b></td><td>$").append(booking.getTotalAmount()).append("</td></tr>");
            }
            
            details.append("</table></div></html>");
            
            showMessage("Booking Details", details.toString(), "info", INFO);
            
        } catch (SQLException e) {
            showMessage("Error", "Failed to load booking details: " + e.getMessage(), 
                       "error", DANGER);
        }
    }

    private void showSlotDetails(ParkingSlot slot) {
        String details = "<html><div style='width:300px;'>" +
                        "<h3>Slot " + slot.getParkingSlotNumber() + "</h3>" +
                        "<p><b>Status:</b> " + slot.getStatusText() + "</p>" +
                        "<p><b>Type:</b> " + (slot.getSlotType() != null ? slot.getSlotType() : "Regular") + "</p>" +
                        "<p><b>Zone:</b> " + (slot.getZone() != null ? slot.getZone() : "General") + "</p>";
        
        if (slot.getUserId() != null) {
            try {
                User user = userDAO.findById(slot.getUserId());
                if (user != null) {
                    details += "<p><b>Assigned to:</b> " + user.getFullname() + "</p>";
                }
            } catch (SQLException e) {
                // Ignore error
            }
        }
        
        details += "</div></html>";
        
        showMessage("Slot Details", details, "info", INFO);
    }

    // ================= CUSTOM DIALOGS =================
    private int showCustomConfirmDialog(String title, String message, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel iconLabel = new JLabel("⚠");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setForeground(color);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(80, 80));
        
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(message);
        textPane.setEditable(false);
        textPane.setBackground(panel.getBackground());
        textPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textPane, BorderLayout.CENTER);
        
        return JOptionPane.showConfirmDialog(this, panel, title, 
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private void showMessage(String title, String message, String type, Color color) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(new EmptyBorder(30, 30, 30, 30));
        content.setBackground(Color.WHITE);
        
        String icon = switch (type) {
            case "success" -> "✅";
            case "error" -> "❌";
            case "warning" -> "⚠";
            default -> "ℹ";
        };
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setForeground(color);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel messageLabel = new JLabel("<html><div style='text-align:center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton okButton = new JButton("OK");
        okButton.setBackground(color);
        okButton.setForeground(Color.WHITE);
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);
        
        content.add(iconLabel, BorderLayout.NORTH);
        content.add(messageLabel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    // ================= ACTION CELL RENDERER/EDITOR =================
    class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return (Component) value;
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel currentPanel;
        
        @Override
        public Object getCellEditorValue() {
            return currentPanel;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentPanel = (JPanel) value;
            return currentPanel;
        }
    }
}