package ui;

import java.awt.*;
import javax.swing.*;

public class CustomerDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private int userId;

    // Colors
    private final Color SIDEBAR = new Color(32, 34, 45);
    private final Color HEADER  = new Color(45, 49, 66);
    private final Color BTN     = new Color(33, 150, 243);
    private final Color LOGOUT  = new Color(220, 53, 69);

    public CustomerDashboard(int userId, int ownerId) {
        this.userId = userId;
        initComponents();
    }

    // Constructor with just userId
    public CustomerDashboard(int userId) {
        this.userId = userId;
        initComponents();
    }

    private void initComponents() {
        setTitle("Parking Management System - Customer");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add panels
        contentPanel.add(new UserParkingMapPanel(userId), "BOOK");
        contentPanel.add(new UserMyBookingsPanel(userId), "MY_BOOKINGS");
        contentPanel.add(new UserMyPaymentsPanel(userId), "MY_PAYMENTS");

        add(createSidebar(), BorderLayout.WEST);
        add(createHeader(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "BOOK");
        setVisible(true);
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(220, getHeight()));
        panel.setBackground(SIDEBAR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("CUSTOMER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        panel.add(title);

        panel.add(menuBtn("📅 Book Slot", "BOOK"));
        panel.add(menuBtn("📋 My Bookings", "MY_BOOKINGS"));
        panel.add(menuBtn("💰 My Payments", "MY_PAYMENTS"));

        panel.add(Box.createVerticalStrut(30));
        panel.add(menuBtn("🚪 Logout", "LOGOUT"));

        return panel;
    }

    private JButton menuBtn(String text, String page) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(180, 45));
        btn.setBackground(text.contains("Logout") ? LOGOUT : BTN);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        btn.addActionListener(e -> {
            if ("LOGOUT".equals(page)) {
                logout();
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
        panel.setPreferredSize(new Dimension(getWidth(), 60));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 73, 92)));

        JLabel title = new JLabel("Customer Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel("👤 User ID: " + userId);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userPanel.add(userLabel);

        panel.add(title, BorderLayout.WEST);
        panel.add(userPanel, BorderLayout.EAST);
        return panel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Open login frame (you need to implement LoginFrame)
            // new LoginFrame().setVisible(true);
            JOptionPane.showMessageDialog(this, "Logged out successfully!");
        }
    }
}