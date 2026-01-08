package ui;

import dao.BookingDAO;
import dao.ParkingSlotDAO;
import dao.VehicleOwnerDAO;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class AdminDashboard extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;

    /* DAO Instances */
    private BookingDAO bookingDAO;
    private ParkingSlotDAO slotDAO;
    private VehicleOwnerDAO ownerDAO;

    /* Stats */
    private int totalBookings;
    private int totalSlots;
    private int availableSlots;
    private int occupiedSlots;
    private int totalOwners;

    /* ===== COLORS ===== */
    private final Color SIDEBAR_BG = new Color(45, 50, 60);
    private final Color HEADER_BG  = new Color(35, 40, 50);
    private final Color MAIN_BG    = new Color(245, 247, 250);
    private final Color TEXT_WHITE = Color.WHITE;

    public AdminDashboard() {
        bookingDAO = new BookingDAO();
        slotDAO = new ParkingSlotDAO();
        ownerDAO = new VehicleOwnerDAO();

        fetchStats();
        initializeFrame();
        createSidebar();
        createMainContent();

        setVisible(true);
    }

    /* ================= FETCH DATA ================= */
    private void fetchStats() {
        try {
            totalBookings = bookingDAO.countBookings();
            totalSlots = slotDAO.countSlots();
            availableSlots = slotDAO.countAvailableSlots();
            occupiedSlots = slotDAO.countOccupiedSlots();
            // totalOwners = ownerDAO.countVehicleOwners();
        } catch (SQLException e) {
            totalBookings = totalSlots = availableSlots = occupiedSlots = totalOwners = 0;
            e.printStackTrace();
        }
    }

    /* ================= FRAME ================= */
    private void initializeFrame() {
        setTitle("Parking Management System - Admin Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    /* ================= SIDEBAR ================= */
    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));

        JLabel title = new JLabel("NAVIGATION");
        title.setForeground(TEXT_WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        sidebar.add(title);

        sidebar.add(createMenuButton("Dashboard", "DASHBOARD"));
        sidebar.add(createMenuButton("Booking Management", "BOOKINGS"));
        sidebar.add(createMenuButton("Parking Management", "PARKING"));
        sidebar.add(createMenuButton("Vehicle Owners", "OWNERS"));

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createMenuButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> cardLayout.show(mainPanel, panelName));
        return btn;
    }

    /* ================= MAIN CONTENT ================= */
    private void createMainContent() {

        /* ----- Header ----- */
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel headerTitle = new JLabel("ADMIN DASHBOARD");
        headerTitle.setForeground(TEXT_WHITE);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.add(headerTitle);

        add(header, BorderLayout.NORTH);

        /* ----- Main Panel ----- */
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(MAIN_BG);

        // Dashboard summary
        mainPanel.add(createDashboardPanel(), "DASHBOARD");
        // Booking panel
        mainPanel.add(new BookingPanel(), "BOOKINGS");
        // Parking panel using ParkingSlotPanel (table UI)
        mainPanel.add(new ParkingSlotPanel(), "PARKING");
        // Vehicle owners panel
        mainPanel.add(createOwnersPanel(), "OWNERS");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "DASHBOARD");
    }

    /* ================= DASHBOARD PANELS ================= */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setBackground(MAIN_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        panel.add(createStatCard("Total Bookings", totalBookings));
        panel.add(createStatCard("Total Slots", totalSlots));
        panel.add(createStatCard("Available Slots", availableSlots));
        panel.add(createStatCard("Occupied Slots", occupiedSlots));
        panel.add(createStatCard("Vehicle Owners", totalOwners));

        return panel;
    }

    private JPanel createStatCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel valueLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createOwnersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);
        JLabel label = new JLabel("Vehicle Owners Panel", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    /* ================= MAIN ================= */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
