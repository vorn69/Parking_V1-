package ui;

import dao.ParkingSlotDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.ParkingSlot;

public class ParkingSlotPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel infoLabel;
    private JLabel timeLabel;

    private ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private List<ParkingSlot> slots;

    public ParkingSlotPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadSlots();
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("PARKING SLOTS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton addBtn = new JButton("+ ADD SLOT");
        JButton refreshBtn = new JButton("REFRESH");

        addBtn.addActionListener(e -> addSlot());
        refreshBtn.addActionListener(e -> loadSlots());

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    // ================= TABLE =================
    private JScrollPane createTable() {
        tableModel = new DefaultTableModel(
            new String[]{"Slot ID", "Slot Number", "Status"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    int status = slots.get(row).getParkingSlotStatus();
                    if (status == ParkingSlot.STATUS_AVAILABLE)
                        c.setBackground(new Color(198, 239, 206));
                    else if (status == ParkingSlot.STATUS_RESERVED)
                        c.setBackground(new Color(255, 235, 156));
                    else
                        c.setBackground(new Color(255, 199, 206));
                }
                return c;
            }
        };

        table.setRowHeight(32);
        return new JScrollPane(table);
    }

    // ================= FOOTER =================
    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);

        timeLabel = new JLabel();
        timeLabel.setForeground(Color.LIGHT_GRAY);

        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);

        return panel;
    }

    // ================= DATA =================
    private void loadSlots() {
        try {
            slots = slotDAO.findAll();
            tableModel.setRowCount(0);

            for (ParkingSlot s : slots) {
                tableModel.addRow(new Object[]{
                    s.getParkingSlotId(),
                    s.getParkingSlotNumber(),
                    statusText(s.getParkingSlotStatus())
                });
            }

            updateFooter();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load slots");
        }
    }

    private void updateFooter() {
        long available = slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE)
                .count();
        long reserved = slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_RESERVED)
                .count();
        long booked = slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED)
                .count();

        infoLabel.setText("Total: " + slots.size()
                + " | Available: " + available
                + " | Reserved: " + reserved
                + " | Booked: " + booked);

        timeLabel.setText("Updated: " +
                new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private String statusText(int status) {
        return switch (status) {
            case ParkingSlot.STATUS_AVAILABLE -> "AVAILABLE";
            case ParkingSlot.STATUS_RESERVED  -> "RESERVED";
            case ParkingSlot.STATUS_OCCUPIED  -> "OCCUPIED";
            default -> "UNKNOWN";
        };
    }

    // ================= ADD SLOT =================
    private void addSlot() {
        JTextField slotNo = new JTextField();

        int opt = JOptionPane.showConfirmDialog(
            this,
            new Object[]{"Slot Number:", slotNo},
            "Add Parking Slot",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (opt == JOptionPane.OK_OPTION) {
            try {
                ParkingSlot s = new ParkingSlot();
                s.setParkingSlotNumber(Integer.parseInt(slotNo.getText()));
                s.setParkingSlotStatus(ParkingSlot.STATUS_AVAILABLE);
                slotDAO.create(s);
                loadSlots();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid slot number");
            }
        }
    }
}
