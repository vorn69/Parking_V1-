package ui;

import dao.BookingDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import models.Booking;

public class BookingPanel extends JPanel {

    private final BookingDAO bookingDAO = new BookingDAO();
    private List<Booking> bookingList;

    private JTable table;
    private DefaultTableModel model;
    private JLabel infoLabel;
    private JLabel timeLabel;
    private JPanel statsPanel;
    
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

    // Admin user ID
    private int adminUserId;

    public BookingPanel(int adminUserId) {
        this.adminUserId = adminUserId;
        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 15));
        initComponents();
        loadData();
        startClock();
    }

    public BookingPanel() {
        this(1);
    }

    private void initComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createStatsPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);
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
        
        buttonPanel.add(createStyledButton("APPROVE", SUCCESS, "✓"));
        buttonPanel.add(createStyledButton("REJECT", DANGER, "✗"));
        buttonPanel.add(createRefreshButton());
        
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }

    private JButton createStyledButton(String text, Color bgColor, String icon) {
        JButton button = new JButton("<html><span style='font-size:12px'>" + icon + " " + text + "</span></html>");
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bgColor.darker(), 1),
            new EmptyBorder(10, 20, 10, 20)
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
        if (text.equals("APPROVE")) {
            button.addActionListener(e -> approveBooking());
        } else if (text.equals("REJECT")) {
            button.addActionListener(e -> rejectBooking());
        }
        
        return button;
    }

    private JButton createRefreshButton() {
        JButton button = new JButton("🔄 Refresh");
        button.setBackground(new Color(236, 240, 241));
        button.setForeground(TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            button.setText("🔄 Refreshing...");
            Timer timer = new Timer(500, evt -> {
                loadData();
                button.setText("🔄 Refresh");
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        return button;
    }

    // ================= STATS PANEL =================
    private JPanel createStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(new EmptyBorder(0, 15, 0, 15));
        statsPanel.setPreferredSize(new Dimension(250, 0));
        
        updateStatsPanel();
        
        return statsPanel;
    }

    private void updateStatsPanel() {
        statsPanel.removeAll();
        
        int total = bookingList != null ? bookingList.size() : 0;
        long pending = bookingList != null ? bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count() : 0;
        long approved = bookingList != null ? bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_APPROVED).count() : 0;
        long rejected = bookingList != null ? bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_REJECTED).count() : 0;
        
        statsPanel.add(createStatCard("📊 Total Bookings", String.valueOf(total), PRIMARY, "All bookings"));
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(createStatCard("⏳ Pending", String.valueOf(pending), WARNING, "Awaiting action"));
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(createStatCard("✅ Approved", String.valueOf(approved), SUCCESS, "Confirmed bookings"));
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(createStatCard("❌ Rejected", String.valueOf(rejected), DANGER, "Cancelled bookings"));
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel createStatCard(String title, String value, Color color, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(20, 15, 20, 15)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        
        // Description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        
        return card;
    }

    // ================= TABLE PANEL =================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        
        // Table header
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(CARD_BG);
        tableHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel tableTitle = new JLabel("Booking Details");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableHeader.add(tableTitle, BorderLayout.WEST);
        
        panel.add(tableHeader, BorderLayout.NORTH);
        
        // Table - FIXED: Removed Date column since Booking model doesn't have getBookingDate()
        model = new DefaultTableModel(
            new String[]{"ID", "Customer", "Slot", "Duration", "Status"},
            0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        // Custom header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
                    label.setBorder(new EmptyBorder(0, 15, 0, 15));
                    
                    if (column == 4) { // Status column
                        String status = value.toString();
                        switch (status) {
                            case "PENDING":
                                label.setForeground(WARNING);
                                label.setText("<html><span style='background:#FEF9E7; padding:5px 10px; border-radius:12px;'>⏳ " + status + "</span></html>");
                                break;
                            case "APPROVED":
                                label.setForeground(SUCCESS);
                                label.setText("<html><span style='background:#EAFAF1; padding:5px 10px; border-radius:12px;'>✅ " + status + "</span></html>");
                                break;
                            case "REJECTED":
                                label.setForeground(DANGER);
                                label.setText("<html><span style='background:#FDEDEC; padding:5px 10px; border-radius:12px;'>❌ " + status + "</span></html>");
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
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // ================= FOOTER =================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(52, 73, 94));
        footer.setBorder(new EmptyBorder(12, 20, 12, 20));
        
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
            model.setRowCount(0);
            
            for (Booking b : bookingList) {
                model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getCustomerId(),
                    b.getSlotId(),
                    b.getDurationOfBooking() + " hours",
                    statusText(b.getBookingStatus())
                    // Removed b.getBookingDate() - method doesn't exist
                });
            }
            
            updateFooter();
            updateStatsPanel();
            
            // Animation effect
            animateRefresh();
            
        } catch (SQLException e) {
            showMessage("Database Error", "Failed to load bookings: " + e.getMessage(), 
                       "error", DANGER);
        }
    }

    private void animateRefresh() {
        Timer timer = new Timer(50, null);
        final int[] opacity = {0};
        
        timer.addActionListener(e -> {
            opacity[0] += 10;
            if (opacity[0] > 100) {
                timer.stop();
                return;
            }
            
            float alpha = opacity[0] / 100.0f;
            for (int i = 0; i < table.getRowCount(); i++) {
                table.setRowHeight(i, (int)(45 * alpha));
            }
        });
        
        timer.start();
    }

    private void updateFooter() {
        long pending = bookingList != null ? bookingList.stream()
                .filter(b -> b.getBookingStatus() == Booking.STATUS_PENDING).count() : 0;
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a");
        infoLabel.setText("<html>📊 <b>" + (bookingList != null ? bookingList.size() : 0) + 
                         "</b> bookings | ⏳ <b>" + pending + "</b> pending</html>");
        timeLabel.setText("🕒 " + sdf.format(new Date()));
    }

    private String statusText(int status) {
        return switch (status) {
            case Booking.STATUS_PENDING -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            default -> "UNKNOWN";
        };
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            timeLabel.setText("🕒 " + sdf.format(new Date()));
        });
        timer.start();
    }

    // ================= ACTIONS =================
    private void approveBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showMessage("Select Booking", "Please select a booking to approve", 
                       "warning", WARNING);
            return;
        }

        Booking b = bookingList.get(row);
        if (b.getBookingStatus() != Booking.STATUS_PENDING) {
            showMessage("Invalid Action", "Only pending bookings can be approved", 
                       "warning", WARNING);
            return;
        }

        int confirm = showCustomConfirmDialog(
            "Approve Booking #" + b.getBookingId(),
            "<html><div style='width:300px;'>"
            + "<p>Approve this booking?</p>"
            + "<p><b>Customer:</b> " + b.getCustomerId() + "</p>"
            + "<p><b>Slot:</b> " + b.getSlotId() + "</p>"
            + "<p><b>Duration:</b> " + b.getDurationOfBooking() + " hours</p>"
            + "</div></html>",
            SUCCESS
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingDAO.approveBookingNow(b.getBookingId(), adminUserId);
                loadData();
                showMessage("Success", "Booking #" + b.getBookingId() + " approved successfully!", 
                           "success", SUCCESS);
            } catch (SQLException e) {
                showMessage("Error", "Failed to approve: " + e.getMessage(), 
                           "error", DANGER);
            }
        }
    }

    private void rejectBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showMessage("Select Booking", "Please select a booking to reject", 
                       "warning", WARNING);
            return;
        }

        Booking b = bookingList.get(row);
        if (b.getBookingStatus() != Booking.STATUS_PENDING) {
            showMessage("Invalid Action", "Only pending bookings can be rejected", 
                       "warning", WARNING);
            return;
        }

        int confirm = showCustomConfirmDialog(
            "Reject Booking #" + b.getBookingId(),
            "<html><div style='width:300px;'>"
            + "<p>Reject this booking?</p>"
            + "<p>Slot " + b.getSlotId() + " will be freed.</p>"
            + "<p style='color:#E74C3C; font-weight:bold;'>This action cannot be undone.</p>"
            + "</div></html>",
            DANGER
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookingDAO.rejectBooking(b.getBookingId());
                loadData();
                showMessage("Success", "Booking #" + b.getBookingId() + " rejected successfully!", 
                           "success", SUCCESS);
            } catch (SQLException e) {
                showMessage("Error", "Failed to reject: " + e.getMessage(), 
                           "error", DANGER);
            }
        }
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
}