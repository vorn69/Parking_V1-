package ui;

import dao.BookingDAO;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.Booking;
import models.ParkingSlot;
import models.User;
import models.Vehicle;

public class BookingPanel extends JPanel {

    private final Color MAIN_BG = new Color(245, 246, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_BLACK = Color.BLACK;
    private final Color ACCENT_GREEN = new Color(52, 199, 89);
    private final Color ACCENT_BLUE = new Color(0, 122, 255);
    private final Color ACCENT_PURPLE = new Color(175, 82, 222);
    private final Color ACCENT_YELLOW = new Color(255, 204, 0);
    private final Color ACCENT_RED = new Color(255, 59, 48);
    private final Color TABLE_HEADER = new Color(60, 64, 72);
    private final Color BOTTOM_BG = Color.BLACK;
    private final Color BOTTOM_TEXT = Color.WHITE;

    private JTable bookingTable;
    private DefaultTableModel bookingTableModel;
    private BookingDAO bookingDAO;
    private List<Booking> bookingList;

    private JLabel infoLabel;
    private JLabel timestampLabel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    public BookingPanel() {
        bookingDAO = new BookingDAO();
        setLayout(new BorderLayout(15, 15));
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createHeaderPanel();
        createTablePanel();
        createBottomPanel();

        loadBookings();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);

        JLabel titleLabel = new JLabel("BOOKING MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_BLACK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(MAIN_BG);

        JButton addButton = createActionButton("+ ADD BOOKING", ACCENT_GREEN);
        addButton.addActionListener(e -> showAddBookingDialog());
        JButton refreshButton = createActionButton("⟳ REFRESH", ACCENT_BLUE);
        refreshButton.addActionListener(e -> loadBookings());
        JButton exportButton = createActionButton("📥 EXPORT", ACCENT_PURPLE);
        exportButton.addActionListener(e -> exportBookingData());

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(brighter(color)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
        return btn;
    }

    private Color brighter(Color color) {
        return new Color(Math.min(255, color.getRed() + 30),
                        Math.min(255, color.getGreen() + 30),
                        Math.min(255, color.getBlue() + 30));
    }

    private void createTablePanel() {
        bookingTableModel = new DefaultTableModel(
            new String[]{"Customer", "Vehicle", "Parking Slot", "Date", "Status"}, 0
        ) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(28);
        bookingTable.getTableHeader().setBackground(TABLE_HEADER);
        bookingTable.getTableHeader().setForeground(Color.WHITE);
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < bookingTable.getColumnCount(); i++) {
            bookingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BOTTOM_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(BOTTOM_TEXT);
        bottomPanel.add(infoLabel, BorderLayout.WEST);

        timestampLabel = new JLabel();
        timestampLabel.setForeground(new Color(200, 200, 200));
        bottomPanel.add(timestampLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadBookings() {
        try {
            bookingList = bookingDAO.findAll();
            bookingTableModel.setRowCount(0);

            for (Booking b : bookingList) {
                User customer = b.getCustomer();
                Vehicle vehicle = b.getVehicle();
                ParkingSlot slot = b.getParkingSlot();

                bookingTableModel.addRow(new Object[]{
                    customer != null ? customer.getFullname() : b.getCustomerId(),
                    vehicle != null ? vehicle.getType() : b.getVehicleId(),
                    slot != null ? slot.getSlotNumber() : b.getSlotId(),
                    b.getBookingTime(),
                    b.getBookingStatus()
                });
            }
            updateBottomInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBottomInfo() {
        long total = bookingList.size();
        long active = bookingList.stream().filter(b -> "Active".equals(b.getBookingStatus())).count();
        long completed = bookingList.stream().filter(b -> "Completed".equals(b.getBookingStatus())).count();
        long cancelled = bookingList.stream().filter(b -> "Cancelled".equals(b.getBookingStatus())).count();

        infoLabel.setText("Total: " + total + " | Active: " + active + " | Completed: " + completed + " | Cancelled: " + cancelled);
        timestampLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void showAddBookingDialog() {
        JOptionPane.showMessageDialog(this, "Add booking dialog placeholder.");
    }

    private void exportBookingData() {
        JOptionPane.showMessageDialog(this, "Export booking data placeholder.");
    }
}
