// package ui;

// import dao.BookingDAO;
// import dao.VehicleOwnerDAO;
// import java.awt.*;
// import java.sql.SQLException;
// import javax.swing.*;

// public class AdminDashboard extends JFrame {

//     private JLabel lblTotalBookings;
//     private JLabel lblActiveBookings;
//     private JLabel lblTotalOwners;

//     private BookingDAO bookingDAO;
//     private VehicleOwnerDAO ownerDAO;

//     public AdminDashboard() {
//         bookingDAO = new BookingDAO();
//         ownerDAO = new VehicleOwnerDAO();

//         setTitle("Admin Dashboard");
//         setSize(400, 250);
//         setLocationRelativeTo(null);
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//         initUI();
//         loadDashboardData();
//     }

//     private void initUI() {
//         JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
//         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

//         lblTotalBookings = new JLabel("Total Bookings: ");
//         lblActiveBookings = new JLabel("Active Bookings: ");
//         lblTotalOwners = new JLabel("Total Vehicle Owners: ");

//         lblTotalBookings.setFont(new Font("Arial", Font.BOLD, 16));
//         lblActiveBookings.setFont(new Font("Arial", Font.BOLD, 16));
//         lblTotalOwners.setFont(new Font("Arial", Font.BOLD, 16));

//         panel.add(lblTotalBookings);
//         panel.add(lblActiveBookings);
//         panel.add(lblTotalOwners);

//         add(panel);
//     }

//     private void loadDashboardData() {
//         try {
//             int totalBookings = bookingDAO.countBookings();
//             int totalOwners = ownerDAO.countOwners();

//             lblTotalBookings.setText("Total Bookings: " + totalBookings);
//             lblActiveBookings.setText("Active Bookings: " + totalBookings);
//             lblTotalOwners.setText("Total Vehicle Owners: " + totalOwners);

//         } catch (SQLException e) {
//             JOptionPane.showMessageDialog(
//                     this,
//                     "Failed to load dashboard data:\n" + e.getMessage(),
//                     "Database Error",
//                     JOptionPane.ERROR_MESSAGE
//             );
//         }
//     }
// }
// src/ui/AdminDashboard.java
package ui;

import dao.UserDAO;
import java.awt.*;
import javax.swing.*;

public class AdminDashboard extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;

    private JPanel sidebar;

    private UserDAO userDAO;

    private int totalVehicleOwners;

    private final Color SIDEBAR_BG = new Color(45, 50, 60);
    private final Color HEADER_BG = new Color(35, 40, 50);
    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color TEXT_BLACK = Color.BLACK;
    private final Color ACCENT_BLUE = new Color(0, 122, 255);

    public AdminDashboard() {
        userDAO = new UserDAO();
        fetchStats();
        initializeFrame();
        createSidebar();
        createMainContent();
        setVisible(true);
    }

    private void fetchStats() {
        try {
            totalVehicleOwners = userDAO.countUsers();
        } catch (Exception e) {
            totalVehicleOwners = 0; // fallback
        }
    }

    private void initializeFrame() {
        setTitle("Parking Management System - Admin Dashboard");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));

        JLabel sidebarTitle = new JLabel("NAVIGATION");
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sidebarTitle.setForeground(TEXT_WHITE);
        sidebarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarTitle.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        sidebar.add(sidebarTitle);

        String[] menuItems = {"Dashboard", "User Management"};
        for (String item : menuItems) {
            sidebar.add(createMenuItem(item));
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createMenuItem(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(TEXT_WHITE);
        button.setBackground(SIDEBAR_BG);
        button.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(250, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addActionListener(e -> JOptionPane.showMessageDialog(this, text + " panel clicked"));

        return button;
    }

    private void createMainContent() {
        JPanel header = new JPanel();
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(getWidth(), 70));
        JLabel title = new JLabel("DASHBOARD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        mainPanel.setBackground(MAIN_BG);

        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setBackground(MAIN_BG);
        dashboardPanel.add(new JLabel("Total Vehicle Owners: " + totalVehicleOwners));

        mainPanel.add(dashboardPanel, "Dashboard");

        add(mainPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
