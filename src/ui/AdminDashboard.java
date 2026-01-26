package ui;

import dao.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.Booking;

public class AdminDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Use JLabels directly instead of complex components
    private JLabel lblBookings, lblVehicles, lblOwners, lblSlots;
    private JTable table;

    private BookingDAO bookingDAO;
    private VehicleDAO vehicleDAO;
    private VehicleOwnerDAO ownerDAO;
    private ParkingSlotDAO slotDAO;

    // COLORS
    private final Color SIDEBAR = new Color(32, 34, 45);
    private final Color HEADER = new Color(45, 49, 66);
    private final Color SIDEBAR_BTN_CLR = new Color(33, 150, 243);
    private final Color LOGOUT_CLR = new Color(220, 53, 69);
    private final Color BG = new Color(245, 246, 250);

    public AdminDashboard() {
        setTitle("Parking Management System - Admin");

        // Set to full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        bookingDAO = new BookingDAO();
        vehicleDAO = new VehicleDAO();
        ownerDAO = new VehicleOwnerDAO();
        slotDAO = new ParkingSlotDAO();

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(createHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createDashboardPage(), "DASHBOARD");
        contentPanel.add(new BookingPanel(), "BOOKINGS");
        contentPanel.add(new PaymentPanel(), "PAYMENTS");
        contentPanel.add(new VehicleOwnerPanel(), "OWNERS");
        contentPanel.add(new ParkingSlotPanel(), "SLOTS");
        contentPanel.add(new UserManagementPanel(), "USERS");

        add(contentPanel, BorderLayout.CENTER);

        refreshDashboard();
        cardLayout.show(contentPanel, "DASHBOARD");
        setVisible(true);
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(SIDEBAR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Make sidebar width proportional to screen
        int sidebarWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.18);
        panel.setPreferredSize(new Dimension(sidebarWidth, getHeight()));

        JLabel title = new JLabel("ADMIN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);

        panel.add(Box.createVerticalStrut(10));
        panel.add(menuBtn("Dashboard", "DASHBOARD", SIDEBAR_BTN_CLR, "📊"));
        panel.add(menuBtn("Bookings", "BOOKINGS", SIDEBAR_BTN_CLR, "📋"));
        panel.add(menuBtn("Payments", "PAYMENTS", SIDEBAR_BTN_CLR, "💰"));
        panel.add(menuBtn("Owners", "OWNERS", SIDEBAR_BTN_CLR, "👤"));
        panel.add(menuBtn("Parking Slots", "SLOTS", SIDEBAR_BTN_CLR, "🅿️"));
        panel.add(menuBtn("User Management", "USERS", SIDEBAR_BTN_CLR, "👥"));

        // Add some vertical space before logout button
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(20));
        panel.add(menuBtn("Logout", null, LOGOUT_CLR, "🚪"));
        panel.add(Box.createVerticalStrut(20));

        return panel;
    }

    private JButton menuBtn(String text, String page, Color color, String icon) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setIcon(new TextIcon(icon, new Font("Segoe UI Emoji", Font.PLAIN, 16), Color.WHITE));
        btn.setIconTextGap(15);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Simpler border
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 255, 255, 50), 1),
                new EmptyBorder(10, 20, 10, 20)));

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addActionListener(e -> {
            if ("Logout".equals(text)) {
                dispose();
                new LoginFrame().setVisible(true);
            } else {
                cardLayout.show(contentPanel, page);
                if ("DASHBOARD".equals(page))
                    refreshDashboard();
            }
        });

        return btn;
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER);

        // Make header height proportional to screen
        int headerHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 0.08);
        panel.setPreferredSize(new Dimension(getWidth(), headerHeight));

        JLabel title = new JLabel("Dashboard Overview");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        // Add welcome message and time on the right side
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setBackground(HEADER);
        rightPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome, Admin");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Update time every second - Use javax.swing.Timer specifically
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("hh:mm:ss a").format(new Date()));
        });
        timer.start();

        rightPanel.add(welcomeLabel);
        rightPanel.add(new JLabel("|"));
        rightPanel.add(timeLabel);

        panel.add(title, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    // ================= DASHBOARD =================
    private JPanel createDashboardPanel() {
        return createDashboardPage(); // Rename or fix call
    }

    private JPanel createDashboardPage() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Stats Panel
        JPanel stats = new JPanel(new GridLayout(1, 4, 20, 20));
        stats.setOpaque(false);

        lblBookings = createSimpleCard(new Color(33, 150, 243), "📋", "Bookings", "0");
        lblVehicles = createSimpleCard(new Color(0, 200, 83), "🚗", "Vehicles", "0");
        lblOwners = createSimpleCard(new Color(255, 193, 7), "👤", "Owners", "0");
        lblSlots = createSimpleCard(new Color(244, 67, 54), "🅿️", "Slots", "0");

        stats.add(lblBookings);
        stats.add(lblVehicles);
        stats.add(lblOwners);
        stats.add(lblSlots);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Recent Bookings",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)));
        tablePanel.setBackground(Color.WHITE);

        table = new JTable();
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Custom table header
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(new Color(52, 73, 94));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scroll, BorderLayout.CENTER);

        panel.add(stats, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createSimpleCard(Color color, String icon, String title, String value) {
        // Create a simple JLabel with HTML formatting
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(25, 10, 25, 10)));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // Set initial text
        updateCardText(label, title, value, icon);

        return label;
    }

    private void updateCardText(JLabel card, String title, String value, String icon) {
        card.setText("<html><div style='text-align:center;'>" +
                "<span style='font-size:24px;'>" + icon + "</span><br/>" +
                "<span style='font-size:24px; font-weight:bold;'>" + value + "</span><br/>" +
                "<span style='font-size:14px;'>" + title + "</span></div></html>");
    }

    // ================= DATA =================
    private void refreshDashboard() {
        // Load Stats Worker
        new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("Bookings", bookingDAO.countBookings());
                stats.put("Vehicles", vehicleDAO.countVehicles());
                stats.put("Owners", ownerDAO.countOwners());
                stats.put("Slots", slotDAO.countSlots());
                return stats;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> stats = get();

                    // Update cards with simple method calls
                    updateCardText(lblBookings, "Bookings", String.valueOf(stats.get("Bookings")), "📋");
                    updateCardText(lblVehicles, "Vehicles", String.valueOf(stats.get("Vehicles")), "🚗");
                    updateCardText(lblOwners, "Owners", String.valueOf(stats.get("Owners")), "👤");
                    updateCardText(lblSlots, "Slots", String.valueOf(stats.get("Slots")), "🅿️");

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading stats: " + e.getMessage());
                }
            }
        }.execute();

        // Load Table Worker
        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingDAO.findPendingBookings();
            }

            @Override
            protected void done() {
                try {
                    List<Booking> list = get();
                    DefaultTableModel model = new DefaultTableModel(
                            new String[] { "Booking ID", "ID", "Slot ID", "Vehicle ID", "Status" }, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false; // Make table non-editable
                        }
                    };

                    for (Booking b : list) {
                        model.addRow(new Object[] {
                                b.getBookingId(),
                                b.getCustomerId(),
                                b.getSlotId(),
                                b.getVehicleId(),
                                getStatusWithIcon(b.getBookingStatus())
                        });
                    }
                    table.setModel(model);

                    // Adjust column widths
                    table.getColumnModel().getColumn(0).setPreferredWidth(80);
                    table.getColumnModel().getColumn(1).setPreferredWidth(100);
                    table.getColumnModel().getColumn(2).setPreferredWidth(80);
                    table.getColumnModel().getColumn(3).setPreferredWidth(100);
                    table.getColumnModel().getColumn(4).setPreferredWidth(100);

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading bookings: " + e.getMessage());
                }
            }
        }.execute();
    }

    private String getStatusWithIcon(int status) {
        switch (status) {
            case Booking.STATUS_PENDING:
                return "⏳ PENDING";
            case Booking.STATUS_APPROVED:
                return "✅ APPROVED";
            case Booking.STATUS_REJECTED:
                return "❌ REJECTED";
            default:
                return "❓ UNKNOWN";
        }
    }

    // Helper Class for Text Icon
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

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }
}