package ui;

import dao.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.*;

public class ParkingSlotPanel extends JPanel {

    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color TABLE_HEADER = new Color(60, 64, 72);
    private final Color BOTTOM_BG = Color.BLACK;
    private final Color BOTTOM_TEXT = Color.WHITE;

    private DefaultTableModel slotTableModel;
    private JTable slotTable;
    private JLabel infoLabel;
    private JLabel timestampLabel;

    private ParkingSlotDAO slotDAO;
    private List<ParkingSlot> slotList;

    private final String[] statusOptions = {"Available", "Occupied", "Reserved", "Maintenance"};
    private JComboBox<String> statusFilter;

    public ParkingSlotPanel() {
        slotDAO = new ParkingSlotDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        createHeaderPanel();
        createTablePanel();
        createInfoPanel();
        loadSlots();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);

        JLabel titleLabel = new JLabel("PARKING SLOT MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(MAIN_BG);

        JButton addButton = new JButton("+ ADD SLOT");
        addButton.addActionListener(e -> addSlot());
        buttonPanel.add(addButton);

        JButton refreshButton = new JButton("⟳ REFRESH");
        refreshButton.addActionListener(e -> loadSlots());
        buttonPanel.add(refreshButton);

        JButton reserveButton = new JButton("Reserve Slot");
        reserveButton.addActionListener(e -> {
            int selectedRow = slotTable.getSelectedRow();
            if (selectedRow >= 0) {
                ParkingSlot slot = slotList.get(selectedRow);
                reserveSlot(slot);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a slot first!");
            }
        });
        buttonPanel.add(reserveButton);

        // Status Filter
        statusFilter = new JComboBox<>();
        statusFilter.addItem("All Statuses");
        for (String status : statusOptions) statusFilter.addItem(status);
        statusFilter.addActionListener(e -> filterByStatus());
        buttonPanel.add(new JLabel("Filter:"));
        buttonPanel.add(statusFilter);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        String[] columns = {"ID", "Slot Number", "Status"};
        slotTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // disable editing directly
            }
        };

        slotTable = new JTable(slotTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                int status = slotList.get(row).getParkingSlotStatus();

                if (!isRowSelected(row)) {
                    switch (status) {
                        case ParkingSlot.STATUS_AVAILABLE -> comp.setBackground(new Color(198, 239, 206)); // green
                        case ParkingSlot.STATUS_OCCUPIED -> comp.setBackground(new Color(255, 199, 206)); // red
                        case ParkingSlot.STATUS_RESERVED -> comp.setBackground(new Color(255, 235, 156)); // yellow
                        case ParkingSlot.STATUS_MAINTENANCE -> comp.setBackground(new Color(191, 191, 191)); // gray
                    }
                }
                return comp;
            }
        };

        slotTable.setRowHeight(35);
        slotTable.getTableHeader().setBackground(TABLE_HEADER);
        slotTable.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        slotTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        slotTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(slotTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BOTTOM_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(BOTTOM_TEXT);
        infoPanel.add(infoLabel, BorderLayout.WEST);

        timestampLabel = new JLabel();
        timestampLabel.setForeground(new Color(180, 180, 180));
        infoPanel.add(timestampLabel, BorderLayout.EAST);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadSlots() {
        try {
            slotList = slotDAO.findAll();
            updateTable(slotList);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load slots!");
        }
    }

    private void updateTable(List<ParkingSlot> list) {
        slotTableModel.setRowCount(0);
        for (ParkingSlot slot : list) {
            slotTableModel.addRow(new Object[]{
                    slot.getParkingSlotId(),
                    slot.getParkingSlotNumber(),
                    slot.getStatusText()
            });
        }
        updateInfoPanel();
    }

    private void updateInfoPanel() {
        int total = slotList.size();
        long available = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE).count();
        long occupied = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED).count();
        long reserved = slotList.stream().filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_RESERVED).count();

        infoLabel.setText("Total Slots: " + total +
                " | Available: " + available +
                " | Occupied: " + occupied +
                " | Reserved: " + reserved);
        timestampLabel.setText("Last updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void addSlot() {
        JTextField numberField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Slot Number:"));
        panel.add(numberField);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Parking Slot",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingSlotNumber(Integer.parseInt(numberField.getText()));
                slot.setStatusFromText((String) statusBox.getSelectedItem());
                slotDAO.create(slot);
                loadSlots();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to add slot!");
            }
        }
    }

    private void filterByStatus() {
        String selected = (String) statusFilter.getSelectedItem();
        if (selected.equals("All Statuses")) {
            updateTable(slotList);
        } else {
            List<ParkingSlot> filtered = slotList.stream()
                    .filter(s -> s.getStatusText().equals(selected))
                    .collect(Collectors.toList());
            updateTable(filtered);
        }
    }

    // ====================== RESERVE SLOT + BOOKING + PAYMENT ======================
    private void reserveSlot(ParkingSlot slot) {
        if (!slot.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Slot is not available!");
            return;
        }

        try {
            // ===== Select Customer =====
            List<VehicleOwner> customers = new VehicleOwnerDAO().findAll();
            String[] customerNames = customers.stream()
                    .map(c -> c.getVehicleOwnerName() + " (" + c.getOwnerUsername() + ")")
                    .toArray(String[]::new);

            String selectedCustomer = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Customer:",
                    "Reserve Slot",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    customerNames,
                    customerNames[0]
            );

            if (selectedCustomer == null) return;

            VehicleOwner customer = customers.get(java.util.Arrays.asList(customerNames).indexOf(selectedCustomer));

            // ===== Select Vehicle =====
            List<Vehicle> vehicles = new VehicleDAO().findByOwnerId(customer.getVehicleOwnerId());
            String[] vehicleNames = vehicles.stream()
                    .map(v -> v.getVehiclePlateNumber() + " - " + v.getVehicleDescription())
                    .toArray(String[]::new);

            String selectedVehicle = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Vehicle:",
                    "Reserve Slot",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    vehicleNames,
                    vehicleNames[0]
            );

            if (selectedVehicle == null) return;

            Vehicle vehicle = vehicles.get(java.util.Arrays.asList(vehicleNames).indexOf(selectedVehicle));

            // ===== Create Booking =====
            Booking booking = new Booking();
            booking.setCustomerId(customer.getVehicleOwnerId());
            booking.setVehicleId(vehicle.getVehicleId());
            booking.setSlotId(slot.getParkingSlotId());
            booking.setBookingStatus(Booking.STATUS_PENDING);
            booking.setBookingTime(new java.sql.Timestamp(System.currentTimeMillis()));

            new BookingDAO().create(booking);

            // ===== Create Payment =====
        Payment payment = new Payment();
        payment.setBookingId(booking.getBookingId());
        payment.setDueAmount(0);   // instead of setAmountDue
        payment.setPaidAmount(0);  // instead of setAmountPaid
        payment.setPaymentStatus(Payment.STATUS_PENDING);
        payment.setPaidBy(customer.getOwnerUsername());

        new PaymentDAO().create(payment);


            // ===== Update Slot =====
            slot.setParkingSlotStatus(ParkingSlot.STATUS_RESERVED);
            slot.setUserId(customer.getUserId());
            slotDAO.updateStatus(slot);

            JOptionPane.showMessageDialog(this, "Slot reserved, booking and payment created successfully!");
            loadSlots();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to reserve slot: " + ex.getMessage());
        }
    }
}
