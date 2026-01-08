package ui;

import dao.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Booking;

public class AdminDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Dashboard components
    private JLabel lblBookings, lblVehicles, lblOwners, lblSlots;
    private JTable table;

    // DAO
    private BookingDAO bookingDAO;
    private VehicleDAO vehicleDAO;
    private VehicleOwnerDAO ownerDAO;
    private ParkingSlotDAO slotDAO;

    // COLORS
    private final Color SIDEBAR = new Color(32, 34, 45);
    private final Color HEADER = new Color(45, 49, 66);
    private final Color CARD1 = new Color(66, 135, 245);
    private final Color CARD2 = new Color(76, 175, 80);
    private final Color CARD3 = new Color(255, 152, 0);
    private final Color CARD4 = new Color(233, 30, 99);
    private final Color BG = new Color(245, 246, 250);

    public AdminDashboard() {
        setTitle("Parking Management System - Admin");
        setSize(1200, 700);
        setLocationRelativeTo(null);
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
        // contentPanel.add(new VehiclePanel(), "VEHICLES");
        contentPanel.add(new VehicleOwnerPanel(), "OWNERS");

        add(contentPanel, BorderLayout.CENTER);

        loadStats();
        loadTable();

        cardLayout.show(contentPanel, "DASHBOARD");
        setVisible(true);
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(220, getHeight()));
        panel.setBackground(SIDEBAR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ADMIN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        panel.add(title);

        panel.add(menuBtn("Dashboard", "DASHBOARD"));
        panel.add(menuBtn("Bookings", "BOOKINGS"));
        panel.add(menuBtn("Vehicles", "VEHICLES"));
        panel.add(menuBtn("Owners", "OWNERS"));
        panel.add(menuBtn("Logout", null));

        return panel;
    }

    private JButton menuBtn(String text, String page) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setBackground(SIDEBAR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if ("Logout".equals(text)) {
                dispose();
                new LoginFrame().setVisible(true);
            } else {
                cardLayout.show(contentPanel, page);
            }
        });

        return btn;
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER);
        panel.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel title = new JLabel("Dashboard Overview");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    // ================= DASHBOARD PAGE =================
    private JPanel createDashboardPage() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 15));
        stats.setOpaque(false);

        lblBookings = createCard("Bookings", CARD1);
        lblVehicles = createCard("Vehicles", CARD2);
        lblOwners = createCard("Owners", CARD3);
        lblSlots = createCard("Slots", CARD4);

        stats.add(lblBookings);
        stats.add(lblVehicles);
        stats.add(lblOwners);
        stats.add(lblSlots);

        table = new JTable();
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Recent Bookings"));

        panel.add(stats, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createCard(String title, Color color) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        return label;
    }

    // ================= DATA =================
    private void loadStats() {
        try {
            lblBookings.setText(cardText("Bookings", bookingDAO.countBookings()));
            lblVehicles.setText(cardText("Vehicles", vehicleDAO.countVehicles()));
            lblOwners.setText(cardText("Owners", ownerDAO.countOwners()));
            lblSlots.setText(cardText("Slots", slotDAO.countSlots()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTable() {
        try {
            List<Booking> list = bookingDAO.findAll();
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Customer", "Slot", "Status"}, 0
            );

            for (Booking b : list) {
                model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getCustomer() != null ? b.getCustomer().getFullname() : "-",
                    b.getParkingSlot() != null ? b.getParkingSlot().getSlotNumber() : "-",
                    b.getBookingStatus()
                });
            }
            table.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String cardText(String title, int value) {
        return "<html><center><h3>" + title +
               "</h3><h1>" + value + "</h1></center></html>";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
