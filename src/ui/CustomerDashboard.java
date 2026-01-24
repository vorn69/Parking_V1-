package ui;

import dao.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.*;

public class CustomerDashboard extends JFrame {
    private Integer userId;
    private Integer ownerId;
    private User currentUser;
    private VehicleOwner vehicleOwner;

    // UI Components
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JLabel welcomeLabel;
    private JLabel emailLabel;

    // DAOs
    private UserDAO userDAO;
    private VehicleOwnerDAO ownerDAO;
    private VehicleDAO vehicleDAO;
    private BookingDAO bookingDAO;
    private PaymentDAO paymentDAO;
    private ParkingSlotDAO slotDAO;

    // Current view
    private String currentView = "dashboard";

    public CustomerDashboard(Integer userId) {
        this.userId = userId;

        // Initialize DAOs
        userDAO = new UserDAO();
        ownerDAO = new VehicleOwnerDAO();
        vehicleDAO = new VehicleDAO();
        bookingDAO = new BookingDAO();
        paymentDAO = new PaymentDAO();
        slotDAO = new ParkingSlotDAO();

        initComponents();
        loadUserData();
    }

    private void initComponents() {
        setTitle("Customer Parking Portal");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set modern look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create main layout
        setLayout(new BorderLayout());

        // Create sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Create main panel with CardLayout
        mainPanel = new JPanel(new CardLayout());
        cardLayout = (CardLayout) mainPanel.getLayout();

        // Initialize all panels
        JPanel dashboardPanel = createDashboardPanel();
        mainPanel.add(dashboardPanel, "dashboard");

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // App Logo/Title
        JLabel appTitle = new JLabel("🚗 PARKFLOW");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appTitle.setForeground(Color.WHITE);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        appTitle.setBorder(new EmptyBorder(0, 0, 30, 0));

        // User Info Panel
        JPanel userPanel = createUserInfoPanel();

        // Menu Panel
        JPanel menuPanel = createMenuPanel();

        // Logout Button
        JButton logoutBtn = new JButton("🚪 LOGOUT");
        styleMenuButton(logoutBtn);
        logoutBtn.addActionListener(e -> logout());

        sidebar.add(appTitle);
        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(menuPanel);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(52, 152, 219), 1),
                new EmptyBorder(15, 10, 15, 10)));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeLabel = new JLabel("Loading...");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailLabel = new JLabel("loading...");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(new Color(189, 195, 199));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Vehicle owner status
        JLabel ownerStatus = new JLabel("🚗 Vehicle Owner: No");
        ownerStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ownerStatus.setForeground(new Color(241, 196, 15));
        ownerStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(userIcon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(emailLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(ownerStatus);

        // Store reference to update later
        panel.putClientProperty("ownerStatus", ownerStatus);

        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Menu items with icons
        String[][] menuItems = {
                { "📊", "DASHBOARD" },
                { "🚗", "MY VEHICLES" },
                { "📅", "BOOK & VIEW BOOKINGS" },
                { "💰", "PAYMENTS" },
                { "👤", "PROFILE" },
                { "❓", "HELP & SUPPORT" }
        };

        for (String[] menuItem : menuItems) {
            JButton menuBtn = createMenuButton(menuItem[0] + " " + menuItem[1]);
            panel.add(menuBtn);
            panel.add(Box.createVerticalStrut(5));

            // Add action listeners
            if (menuItem[1].equals("DASHBOARD")) {
                menuBtn.addActionListener(e -> showDashboard());
            } else if (menuItem[1].equals("MY VEHICLES")) {
                menuBtn.addActionListener(e -> showMyVehicles());
            } else if (menuItem[1].equals("BOOK & VIEW BOOKINGS")) {
                menuBtn.addActionListener(e -> showBookings());
            } else if (menuItem[1].equals("PAYMENTS")) {
                menuBtn.addActionListener(e -> showPayments());
            } else if (menuItem[1].equals("PROFILE")) {
                menuBtn.addActionListener(e -> showProfile());
            } else if (menuItem[1].equals("HELP & SUPPORT")) {
                menuBtn.addActionListener(e -> showHelp());
            }
        }

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(new Color(236, 240, 241));
        button.setBackground(new Color(52, 73, 94));
        button.setBorder(new EmptyBorder(12, 15, 12, 15));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getBackground().equals(new Color(41, 128, 185))) {
                    button.setBackground(new Color(60, 99, 130));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.getBackground().equals(new Color(41, 128, 185))) {
                    button.setBackground(new Color(52, 73, 94));
                }
            }
        });

        return button;
    }

    private void styleMenuButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(231, 76, 60));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(192, 57, 43));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(231, 76, 60));
            }
        });
    }

    private void loadUserData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    currentUser = userDAO.findById(userId);
                    if (currentUser != null) {
                        // Check if user is registered as vehicle owner
                        vehicleOwner = ownerDAO.findByUserId(userId);
                        ownerId = (vehicleOwner != null) ? vehicleOwner.getVehicleOwnerId() : null;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (currentUser != null) {
                    updateUserInfo();
                    // Load initial data
                    loadInitialData();
                } else {
                    JOptionPane.showMessageDialog(CustomerDashboard.this,
                            "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            }
        };
        worker.execute();
    }

    private void updateUserInfo() {
        welcomeLabel.setText(currentUser.getUsername());
        emailLabel.setText(currentUser.getEmail());

        // Update owner status in sidebar
        Component[] components = ((JPanel) getContentPane().getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Object prop = ((JPanel) comp).getClientProperty("ownerStatus");
                if (prop instanceof JLabel) {
                    JLabel ownerStatus = (JLabel) prop;
                    if (ownerId != null) {
                        ownerStatus.setText("🚗 Vehicle Owner: Yes (ID: " + ownerId + ")");
                        ownerStatus.setForeground(new Color(46, 204, 113));
                    } else {
                        ownerStatus.setText("🚗 Vehicle Owner: Not Registered");
                        ownerStatus.setForeground(new Color(241, 196, 15));
                    }
                }
            }
        }
    }

    private void loadInitialData() {
        showDashboard();
    }

    public void showDashboard() {
        currentView = "dashboard";
        highlightCurrentMenu();

        // Remove existing dashboard panel if any
        for (Component comp : mainPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals("dashboardPanel")) {
                mainPanel.remove(comp);
            }
        }

        JPanel dashboardPanel = createDashboardPanel();
        dashboardPanel.setName("dashboardPanel");
        mainPanel.add(dashboardPanel, "dashboard");
        cardLayout.show(mainPanel, "dashboard");
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        panel.add(createDashboardHeader(), BorderLayout.NORTH);

        // Main content
        panel.add(createDashboardContent(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDashboardHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("🏠 Customer Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(52, 73, 94));

        // Quick actions panel
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quickActions.setOpaque(false);

        JButton newBookingBtn = new JButton("➕ New Booking");
        newBookingBtn.setBackground(new Color(46, 204, 113));
        newBookingBtn.setForeground(Color.WHITE);
        newBookingBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newBookingBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        newBookingBtn.addActionListener(e -> showBookings());

        quickActions.add(newBookingBtn);

        header.add(title, BorderLayout.WEST);
        header.add(quickActions, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboardContent() {
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setOpaque(false);

        // Left: Bookings
        content.add(createBookingsPanel());

        // Right: Available Slots
        content.add(createAvailableSlotsPanel());

        return content;
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("📋 My Recent Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(44, 62, 80));

        // Bookings table
        String[] columns = { "ID", "Vehicle", "Slot", "Duration", "Status", "Payment", "Actions" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column is editable
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Load bookings
        loadBookingsData(model);

        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadBookingsData(DefaultTableModel model) {
        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                try {
                    if (ownerId != null) {
                        return bookingDAO.findByCustomerId(ownerId);
                    }
                    return new ArrayList<>();
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();

                    if (bookings == null || bookings.isEmpty()) {
                        model.addRow(new Object[] {
                                "", "No bookings yet", "", "", "", "", "Click 'New Booking' to create one"
                        });
                    } else {
                        // Show only recent bookings (last 5)
                        int count = Math.min(bookings.size(), 5);
                        for (int i = 0; i < count; i++) {
                            Booking booking = bookings.get(i);

                            // Get vehicle info
                            String vehicleInfo = "N/A";
                            try {
                                Vehicle vehicle = vehicleDAO.findById(booking.getVehicleId());
                                if (vehicle != null) {
                                    vehicleInfo = vehicle.getVehiclePlateNumber();
                                }
                            } catch (Exception e) {
                                vehicleInfo = "Unknown";
                            }

                            // Get payment status
                            String paymentStatus = "❌ Not Paid";
                            try {
                                Payment payment = paymentDAO.findByBookingId(booking.getBookingId());
                                if (payment != null && payment.getPaymentStatus() == Payment.STATUS_PAID) {
                                    paymentStatus = "✅ Paid";
                                }
                            } catch (Exception e) {
                                // Ignore
                            }

                            // Get status text
                            String statusText = getBookingStatusText(booking.getBookingStatus());

                            model.addRow(new Object[] {
                                    booking.getBookingId(),
                                    vehicleInfo,
                                    "Slot " + booking.getSlotId(),
                                    booking.getDurationOfBooking(),
                                    statusText,
                                    paymentStatus,
                                    "View"
                            });
                        }
                    }
                } catch (Exception e) {
                    model.addRow(new Object[] {
                            "", "Error loading bookings", "", "", "", "", "Try again"
                    });
                }
            }
        };
        worker.execute();
    }

    private String getBookingStatusText(int status) {
        switch (status) {
            case 0:
                return "⏳ Pending";
            case 1:
                return "✅ Approved";
            case 2:
                return "❌ Rejected";
            case 3:
                return "✅ Completed";
            default:
                return "❓ Unknown";
        }
    }

    private JPanel createAvailableSlotsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("🅿️ Available Parking Slots");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(44, 62, 80));

        // Slots grid
        JPanel slotsGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        slotsGrid.setBackground(Color.WHITE);

        // Load available slots
        loadAvailableSlots(slotsGrid);

        JScrollPane scrollPane = new JScrollPane(slotsGrid);
        scrollPane.setBorder(null);

        // Warning message if needed
        if (ownerId == null) {
            JLabel warning = new JLabel("<html><div style='text-align:center;padding:10px;color:#e74c3c;'>" +
                    "⚠️ You need to register a vehicle first!<br>" +
                    "Go to 'My Vehicles' to add one.</div></html>");
            warning.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            panel.add(warning, BorderLayout.SOUTH);
        }

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadAvailableSlots(JPanel slotsGrid) {
        SwingWorker<List<ParkingSlot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ParkingSlot> doInBackground() throws Exception {
                try {
                    return slotDAO.findAvailableSlots();
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<ParkingSlot> slots = get();

                    if (slots == null || slots.isEmpty()) {
                        JLabel noSlots = new JLabel("No slots available");
                        noSlots.setHorizontalAlignment(SwingConstants.CENTER);
                        slotsGrid.add(noSlots);
                    } else {
                        for (ParkingSlot slot : slots) {
                            JButton slotBtn = createSlotButton(slot);
                            slotsGrid.add(slotBtn);
                        }
                    }

                    slotsGrid.revalidate();
                    slotsGrid.repaint();
                } catch (Exception e) {
                    // Ignore
                }
            }
        };
        worker.execute();
    }

    private JButton createSlotButton(ParkingSlot slot) {
        JButton button = new JButton("Slot " + slot.getParkingSlotNumber());
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 60));

        button.addActionListener(e -> bookSlot(slot));

        return button;
    }

    private void bookSlot(ParkingSlot slot) {
        if (ownerId == null) {
            int response = JOptionPane.showConfirmDialog(this,
                    "You need to register as a Vehicle Owner first!\n\n" +
                            "Would you like to register now?",
                    "Registration Required",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                showMyVehicles();
            }
            return;
        }

        // Check if user has vehicles
        SwingWorker<List<Vehicle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Vehicle> doInBackground() throws Exception {
                try {
                    return vehicleDAO.findByVehicleOwnerId(ownerId);
                } catch (Exception e) {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Vehicle> vehicles = get();

                    if (vehicles == null || vehicles.isEmpty()) {
                        JOptionPane.showMessageDialog(CustomerDashboard.this,
                                "You need to add a vehicle first!\n" +
                                        "Go to 'My Vehicles' to add one.",
                                "No Vehicles",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Show booking dialog
                    showBookingDialog(slot, vehicles);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerDashboard.this,
                            "Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showBookingDialog(ParkingSlot slot, List<Vehicle> vehicles) {
        JDialog dialog = new JDialog(this, "Book Slot " + slot.getParkingSlotNumber(), true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Book Slot " + slot.getParkingSlotNumber(), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(52, 73, 94));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Vehicle:"));
        JComboBox<String> vehicleCombo = new JComboBox<>();
        for (Vehicle v : vehicles) {
            vehicleCombo.addItem(v.getVehiclePlateNumber());
        }
        formPanel.add(vehicleCombo);

        formPanel.add(new JLabel("Duration:"));
        String[] durations = { "1 hour", "2 hours", "3 hours", "4 hours", "5 hours", "6 hours", "1 day", "2 days" };
        JComboBox<String> durationCombo = new JComboBox<>(durations);
        formPanel.add(durationCombo);

        formPanel.add(new JLabel("Date/Time:"));
        JTextField datetimeField = new JTextField();
        datetimeField.setText(new Timestamp(System.currentTimeMillis()).toString().substring(0, 16));
        formPanel.add(datetimeField);

        formPanel.add(new JLabel("Estimated Cost:"));
        JLabel costLabel = new JLabel("$5.00");
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        costLabel.setForeground(new Color(46, 204, 113));
        formPanel.add(costLabel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton bookBtn = new JButton("📅 Book Now");
        bookBtn.setBackground(new Color(46, 204, 113));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookBtn.addActionListener(e -> {
            createBooking(slot, vehicles.get(vehicleCombo.getSelectedIndex()),
                    (String) durationCombo.getSelectedItem(), dialog);
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

    private void createBooking(ParkingSlot slot, Vehicle vehicle, String duration, JDialog dialog) {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try {
                    Booking booking = new Booking();
                    booking.setCustomerId(ownerId);
                    booking.setVehicleId(vehicle.getVehicleId());
                    booking.setSlotId(slot.getParkingSlotId());
                    booking.setDurationOfBooking(duration);
                    booking.setBookingStatus(0); // Pending
                    booking.setUserId(userId);
                    booking.setRemarks("Booking from customer dashboard");
                    booking.setBookingTime(new Timestamp(System.currentTimeMillis()));

                    return bookingDAO.createBookingWithSlotUpdate(booking);
                } catch (Exception e) {
                    return -1;
                }
            }

            @Override
            protected void done() {
                try {
                    int bookingId = get();

                    if (bookingId > 0) {
                        JOptionPane.showMessageDialog(dialog,
                                "✅ Booking created successfully!\n" +
                                        "Booking ID: " + bookingId + "\n" +
                                        "Make payment to confirm your booking.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                        dialog.dispose();
                        showDashboard(); // Refresh dashboard
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Failed to create booking. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void showMyVehicles() {
        currentView = "vehicles";
        highlightCurrentMenu();

        JPanel vehiclesPanel = new MyVehiclesPanel(userId, ownerId);
        mainPanel.add(vehiclesPanel, "vehicles");
        cardLayout.show(mainPanel, "vehicles");
    }

    public void showBookings() {
        currentView = "bookings";
        highlightCurrentMenu();

        JPanel bookingsPanel = new MyBookingsPanel(userId, ownerId);
        mainPanel.add(bookingsPanel, "bookings");
        cardLayout.show(mainPanel, "bookings");
    }

    public void showPayments() {
        currentView = "payments";
        highlightCurrentMenu();

        // Remove existing payments panel if any
        for (Component comp : mainPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals("customerPaymentsPanel")) {
                mainPanel.remove(comp);
            }
        }

        JPanel paymentsPanel = new CustomerPaymentPanel(userId, ownerId);
        paymentsPanel.setName("customerPaymentsPanel");
        mainPanel.add(paymentsPanel, "payments");
        cardLayout.show(mainPanel, "payments");
    }

    // private JPanel createPaymentsPanel() {
    // JPanel panel = new JPanel(new BorderLayout());
    // panel.setBackground(new Color(245, 247, 250));
    // panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    // JLabel title = new JLabel("💰 Payments");
    // title.setFont(new Font("Segoe UI", Font.BOLD, 24));
    // title.setForeground(new Color(52, 73, 94));

    // panel.add(title, BorderLayout.NORTH);
    // panel.add(new JLabel("Payments functionality will be implemented here.",
    // SwingConstants.CENTER), BorderLayout.CENTER);

    // return panel;
    // }

    public void showProfile() {
        currentView = "profile";
        highlightCurrentMenu();

        JPanel profilePanel = createProfilePanel();
        mainPanel.add(profilePanel, "profile");
        cardLayout.show(mainPanel, "profile");
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("👤 Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(52, 73, 94));

        // Profile content
        JPanel content = new JPanel(new GridLayout(6, 2, 10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        if (currentUser != null) {
            content.add(new JLabel("Username:"));
            content.add(new JLabel(currentUser.getUsername()));

            content.add(new JLabel("Email:"));
            content.add(new JLabel(currentUser.getEmail()));

            content.add(new JLabel("User ID:"));
            content.add(new JLabel(String.valueOf(currentUser.getUserId())));

            content.add(new JLabel("Vehicle Owner:"));
            content.add(new JLabel(ownerId != null ? "Yes (ID: " + ownerId + ")" : "No"));

            if (vehicleOwner != null) {
                content.add(new JLabel("Owner Name:"));
                content.add(new JLabel(vehicleOwner.getVehicleOwnerName()));

                content.add(new JLabel("Contact:"));
                content.add(new JLabel(vehicleOwner.getVehicleOwnerContact()));
            }
        }

        panel.add(title, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    public void showHelp() {
        currentView = "help";
        highlightCurrentMenu();

        JPanel helpPanel = createHelpPanel();
        mainPanel.add(helpPanel, "help");
        cardLayout.show(mainPanel, "help");
    }

    private JPanel createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("❓ Help & Support");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(52, 73, 94));

        JTextArea helpText = new JTextArea();
        helpText.setText("PARKFLOW Customer Support\n\n" +
                "1. How to book a parking slot?\n" +
                "   - Register as Vehicle Owner (My Vehicles)\n" +
                "   - Add at least one vehicle\n" +
                "   - Click on available slot to book\n\n" +
                "2. How to make payment?\n" +
                "   - Go to 'Payments' section\n" +
                "   - Select booking to pay\n" +
                "   - Enter payment amount\n\n" +
                "3. Contact Support:\n" +
                "   Email: support@parkflow.com\n" +
                "   Phone: 1-800-PARKFLOW\n" +
                "   Hours: 24/7");
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helpText.setEditable(false);
        helpText.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(helpText);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void highlightCurrentMenu() {
        // Get sidebar panel
        JPanel sidebar = (JPanel) getContentPane().getComponent(0);

        // Find all menu buttons
        for (Component comp : sidebar.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JButton) {
                        JButton btn = (JButton) subComp;
                        String btnText = btn.getText();

                        // Check which menu this button represents
                        if (currentView.equals("dashboard") && btnText.contains("DASHBOARD")) {
                            btn.setBackground(new Color(41, 128, 185));
                        } else if (currentView.equals("vehicles") && btnText.contains("VEHICLES")) {
                            btn.setBackground(new Color(41, 128, 185));
                        } else if (currentView.equals("bookings") && btnText.contains("BOOK")) {
                            btn.setBackground(new Color(41, 128, 185));
                        } else if (currentView.equals("payments") && btnText.contains("PAYMENTS")) {
                            btn.setBackground(new Color(41, 128, 185));
                        } else if (currentView.equals("profile") && btnText.contains("PROFILE")) {
                            btn.setBackground(new Color(41, 128, 185));
                        } else if (currentView.equals("help") && btnText.contains("HELP")) {
                            btn.setBackground(new Color(41, 128, 185));
                        } else if (!btn.getBackground().equals(new Color(231, 76, 60))) { // Don't change logout button
                            btn.setBackground(new Color(52, 73, 94));
                        }
                    }
                }
            }
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // Public method for VehicleOwnerRegistrationPanel to call
    public void onRegistrationSuccess(Integer newOwnerId) {
        this.ownerId = newOwnerId;
        try {
            this.vehicleOwner = ownerDAO.findByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateUserInfo();

        JOptionPane.showMessageDialog(this,
                "✅ Registration successful!\n\n" +
                        "Vehicle Owner ID: " + newOwnerId + "\n" +
                        "You can now add vehicles and make bookings.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        showDashboard();
    }

    // Required public method for VehicleOwnerRegistrationPanel
    public void showDashboardView() {
        showDashboard();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // For testing
            new CustomerDashboard(1).setVisible(true);
        });
    }
}