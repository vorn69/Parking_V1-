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
        setTitle("Customer Parking Portal - ParkFlow");
        setSize(1280, 800);
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

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Main Content Area
        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBackground(new Color(240, 242, 245)); // Light background

        // Initialize dashboard view - will be loaded properly after user data

        add(mainPanel, BorderLayout.CENTER);
    }

    // --- SIDEBAR ---

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80), 0, getHeight(),
                        new Color(34, 47, 62));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Brand
        JLabel brandLabel = new JLabel("ParkFlow");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setIcon(new TextIcon("🚗", new Font("Segoe UI Emoji", Font.PLAIN, 28), Color.WHITE));
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // User Profile Card (Compact)
        JPanel userCard = createSidebarUserCard();

        // Menu
        JPanel menuPanel = createMenuPanel();

        // Footer
        JButton logoutBtn = createStyledButton("Logout", new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> logout());
        logoutBtn.setMaximumSize(new Dimension(240, 45));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brandLabel);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(userCard);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(menuPanel);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JPanel createSidebarUserCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomeLabel = new JLabel("Loading...");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);

        emailLabel = new JLabel("...");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(new Color(189, 195, 199));

        JLabel ownerStatus = new JLabel("Checking status...");
        ownerStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        ownerStatus.setForeground(new Color(241, 196, 15));

        card.add(welcomeLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(ownerStatus);

        card.putClientProperty("ownerStatus", ownerStatus);
        return card;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addMenuButton(panel, "Dashboard", "📊", e -> showDashboard());
        addMenuButton(panel, "My Vehicles", "🚗", e -> showMyVehicles());
        addMenuButton(panel, "Bookings", "📅", e -> showBookings());
        addMenuButton(panel, "Payments", "💰", e -> showPayments());
        addMenuButton(panel, "Profile", "👤", e -> showProfile());

        return panel;
    }

    private void addMenuButton(JPanel panel, String text, String icon, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(new Color(236, 240, 241));
        btn.setBackground(new Color(255, 255, 255, 10)); // Transparent white
        btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 45));
        btn.setIcon(new TextIcon(icon, new Font("Segoe UI Emoji", Font.PLAIN, 16), Color.WHITE));
        btn.setIconTextGap(15);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(255, 255, 255, 30));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });

        btn.addActionListener(action);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(5));
    }

    // --- DASHBOARD CONTENT ---

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Welcome back, " + (currentUser != null ? currentUser.getUsername() : "User"));
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(44, 62, 80));

        JButton quickBookBtn = createStyledButton("New Booking +", new Color(46, 204, 113));
        quickBookBtn.addActionListener(e -> showBookings()); // Redirect to bookings for now

        header.add(title, BorderLayout.WEST);
        header.add(quickBookBtn, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Content Grid
        JPanel content = new JPanel(new GridLayout(1, 2, 30, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 0, 0, 0));

        // 1. Recent Bookings Card
        JPanel bookingsCard = createCard("Recent Bookings");
        loadBookingsIntoCard(bookingsCard);

        // 2. Quick Slots Card
        JPanel slotsCard = createCard("Available Spots");
        loadSlotsIntoCard(slotsCard);

        content.add(bookingsCard);
        content.add(slotsCard);

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCard(String titleStr) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(44, 62, 80));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        card.add(title, BorderLayout.NORTH);
        return card;
    }

    // --- HELPERS ---

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2d.setColor(baseColor.darker());
                else if (getModel().isRollover())
                    g2d.setColor(baseColor.brighter());
                else
                    g2d.setColor(baseColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));
        return btn;
    }

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
            g2d.drawString(text, x, y + getIconHeight() - 5);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return 30;
        }

        @Override
        public int getIconHeight() {
            return 30;
        }
    }

    // --- DATA LOADING ---

    private void loadUserData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    currentUser = userDAO.findById(userId);
                    if (currentUser != null) {
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
                    showDashboard(); // Show dashboard after data load
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
        if (welcomeLabel != null)
            welcomeLabel.setText(currentUser.getUsername());
        if (emailLabel != null)
            emailLabel.setText(currentUser.getEmail());
        updateOwnerStatusLabel(getContentPane());
    }

    private void updateOwnerStatusLabel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                Object prop = ((JComponent) comp).getClientProperty("ownerStatus");
                if (prop instanceof JLabel) {
                    JLabel label = (JLabel) prop;
                    if (ownerId != null) {
                        label.setText("Verified Vehicle Owner");
                        label.setForeground(new Color(46, 204, 113));
                    } else {
                        label.setText("Guest (No Vehicles)");
                        label.setForeground(new Color(241, 196, 15));
                    }
                    return;
                }
                updateOwnerStatusLabel((Container) comp);
            }
        }
    }

    private void loadBookingsIntoCard(JPanel card) {
        String[] columns = { "Vehicle", "Slot", "Status" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(245, 247, 250));
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);

        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                if (ownerId == null)
                    return new ArrayList<>();
                return bookingDAO.findByCustomerId(ownerId);
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    if (bookings != null) {
                        for (Booking b : bookings) {
                            String status = b.getBookingStatus() == 1 ? "Approved"
                                    : (b.getBookingStatus() == 2 ? "Rejected" : "Pending");
                            model.addRow(new Object[] {
                                    "ID: " + b.getVehicleId(),
                                    "Slot " + b.getSlotId(),
                                    status
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadSlotsIntoCard(JPanel card) {
        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 10));
        grid.setOpaque(false);

        new SwingWorker<List<ParkingSlot>, Void>() {
            @Override
            protected List<ParkingSlot> doInBackground() throws Exception {
                return slotDAO.findAvailableSlots();
            }

            @Override
            protected void done() {
                try {
                    List<ParkingSlot> slots = get();
                    grid.removeAll();
                    if (slots != null) {
                        int count = 0;
                        for (ParkingSlot slot : slots) {
                            if (count++ >= 9)
                                break;
                            JButton btn = new JButton("#" + slot.getParkingSlotNumber());
                            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                            btn.setForeground(new Color(44, 62, 80));
                            btn.setBackground(new Color(236, 240, 241));
                            btn.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
                            btn.setFocusPainted(false);
                            grid.add(btn);
                        }
                    }
                    grid.revalidate();
                    grid.repaint();
                } catch (Exception e) {
                }
            }
        }.execute();

        card.add(grid, BorderLayout.CENTER);
    }

    // --- NAVIGATION & PUBLIC METHODS ---

    public void showDashboard() {
        currentView = "dashboard";
        mainPanel.removeAll();
        mainPanel.add(createDashboardPanel());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Legacy support alias
    public void showDashboardView() {
        showDashboard();
    }

    public void showMyVehicles() {
        currentView = "vehicles";
        mainPanel.removeAll();
        // Pass 'this' as the dashboard reference
        JPanel p = new MyVehiclesPanel(userId, ownerId, this);
        mainPanel.add(p);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showBookings() {
        currentView = "bookings";
        mainPanel.removeAll();
        // Pass 'this' if constructor allows, otherwise check current implementation
        JPanel p = new MyBookingsPanel(userId, ownerId);
        mainPanel.add(p);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showPayments() {
        currentView = "payments";
        mainPanel.removeAll();
        // Check if CustomerPaymentPanel exists in package, assuming yes
        try {
            JPanel p = new CustomerPaymentPanel(userId, ownerId);
            mainPanel.add(p);
        } catch (Exception e) {
            JPanel p = new JPanel();
            p.add(new JLabel("Payment module loading..."));
            mainPanel.add(p);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showProfile() {
        currentView = "profile";
        mainPanel.removeAll();

        UserProfilePanel profilePanel = new UserProfilePanel(userId);
        mainPanel.add(profilePanel);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showRegistrationForm() {
        currentView = "registration";
        mainPanel.removeAll();
        VehicleOwnerRegistrationPanel registrationPanel = new VehicleOwnerRegistrationPanel(userId, this);
        mainPanel.add(registrationPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showHelp() {
        currentView = "help";
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.add(new JLabel("Help & Support Coming Soon"));

        mainPanel.removeAll();
        mainPanel.add(p);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void onRegistrationSuccess(Integer newOwnerId) {
        this.ownerId = newOwnerId;
        try {
            this.vehicleOwner = ownerDAO.findByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateUserInfo();
        JOptionPane.showMessageDialog(this,
                "✅ Registration successful!\nVehicle Owner ID: " + newOwnerId,
                "Success", JOptionPane.INFORMATION_MESSAGE);
        showDashboard();
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}