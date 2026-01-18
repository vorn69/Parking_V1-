package ui;

import dao.ParkingSlotDAO;
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
import models.ParkingSlot;

public class ParkingSlotPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel infoLabel;
    private JLabel timeLabel;
    private JPanel statsPanel;

    private ParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private List<ParkingSlot> slots;

    // Modern Color Scheme
    private final Color BG_COLOR = new Color(248, 250, 252);
    private final Color CARD_BG = Color.WHITE;
    private final Color AVAILABLE_COLOR = new Color(34, 197, 94);
    private final Color RESERVED_COLOR = new Color(245, 158, 11);
    private final Color OCCUPIED_COLOR = new Color(239, 68, 68);
    private final Color ADD_BUTTON_COLOR = new Color(59, 130, 246);
    private final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private final Color BORDER_COLOR = new Color(226, 232, 240);
    private final Color HOVER_COLOR = new Color(241, 245, 249);

    public ParkingSlotPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        initComponents();
        loadSlots();
        startClock();
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
            new LineBorder(BORDER_COLOR, 2),
            new EmptyBorder(20, 25, 20, 25)
        ));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(CARD_BG);
        
        JLabel icon = new JLabel("🅿️");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        
        JLabel title = new JLabel("Parking Slots Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        
        titlePanel.add(icon);
        titlePanel.add(title);
        header.add(titlePanel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(CARD_BG);
        
        buttonPanel.add(createStyledButton("➕ Add Slot", ADD_BUTTON_COLOR));
        buttonPanel.add(createRefreshButton());
        
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bgColor.darker(), 1),
            new EmptyBorder(12, 25, 12, 25)
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
        if (text.contains("Add Slot")) {
            button.addActionListener(e -> showAddSlotDialog());
        }
        
        return button;
    }

    private JButton createRefreshButton() {
        JButton button = new JButton("🔄 Refresh");
        button.setBackground(new Color(241, 245, 249));
        button.setForeground(TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(12, 25, 12, 25)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            button.setText("🔄 Refreshing...");
            Timer timer = new Timer(500, evt -> {
                loadSlots();
                button.setText("🔄 Refresh");
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        return button;
    }

    // ================= STATISTICS PANEL =================
    private JPanel createStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setBorder(new EmptyBorder(0, 0, 0, 20));
        statsPanel.setPreferredSize(new Dimension(280, 0));
        
        updateStatsPanel();
        
        return statsPanel;
    }

    private void updateStatsPanel() {
        statsPanel.removeAll();
        
        int total = slots != null ? slots.size() : 0;
        long available = slots != null ? slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE).count() : 0;
        long reserved = slots != null ? slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_RESERVED).count() : 0;
        long occupied = slots != null ? slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED).count() : 0;
        
        statsPanel.add(createStatCard("Total Slots", "🅿️", String.valueOf(total), 
            new Color(59, 130, 246), "All parking slots"));
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(createStatCard("Available", "✅", String.valueOf(available), 
            AVAILABLE_COLOR, "Ready for booking"));
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(createStatCard("Reserved", "⏳", String.valueOf(reserved), 
            RESERVED_COLOR, "Booked but not occupied"));
        statsPanel.add(Box.createVerticalStrut(15));
        statsPanel.add(createStatCard("Occupied", "🚗", String.valueOf(occupied), 
            OCCUPIED_COLOR, "Currently in use"));
        
        // Add occupancy chart
        statsPanel.add(Box.createVerticalStrut(25));
        statsPanel.add(createOccupancyChart(available, reserved, occupied, total));
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel createStatCard(String title, String icon, String value, Color color, String description) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Title and icon
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_BG);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.EAST);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        // Description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        
        return card;
    }

    private JPanel createOccupancyChart(long available, long reserved, long occupied, int total) {
        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(CARD_BG);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel title = new JLabel("Occupancy Chart");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Chart visualization
        JPanel chartPanel = new JPanel(new GridLayout(1, total > 0 ? total : 1, 3, 0));
        chartPanel.setBackground(CARD_BG);
        chartPanel.setPreferredSize(new Dimension(0, 80));
        
        // Add colored blocks for each slot
        for (int i = 0; i < total; i++) {
            JPanel block = new JPanel();
            block.setBorder(new LineBorder(CARD_BG, 1));
            
            if (i < occupied) {
                block.setBackground(OCCUPIED_COLOR);
                block.setToolTipText("Occupied");
            } else if (i < occupied + reserved) {
                block.setBackground(RESERVED_COLOR);
                block.setToolTipText("Reserved");
            } else if (i < occupied + reserved + available) {
                block.setBackground(AVAILABLE_COLOR);
                block.setToolTipText("Available");
            }
            
            chartPanel.add(block);
        }
        
        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        legendPanel.setBackground(CARD_BG);
        legendPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        legendPanel.add(createLegendItem("Available", AVAILABLE_COLOR));
        legendPanel.add(createLegendItem("Reserved", RESERVED_COLOR));
        legendPanel.add(createLegendItem("Occupied", OCCUPIED_COLOR));
        
        chartCard.add(title, BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);
        chartCard.add(legendPanel, BorderLayout.SOUTH);
        
        return chartCard;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel legendItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        legendItem.setBackground(CARD_BG);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        
        legendItem.add(colorBox);
        legendItem.add(label);
        
        return legendItem;
    }

    // ================= TABLE PANEL =================
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        
        // Table header
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(CARD_BG);
        tableHeader.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel tableTitle = new JLabel("Slot Details");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(TEXT_PRIMARY);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(CARD_BG);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 15, 8, 15)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search slots...");
        
        JButton searchBtn = new JButton("🔍");
        searchBtn.setBackground(new Color(241, 245, 249));
        searchBtn.setForeground(TEXT_PRIMARY);
        searchBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 15, 8, 15)
        ));
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(searchPanel, BorderLayout.EAST);
        
        // Table
        tableModel = new DefaultTableModel(
            new String[]{"Slot ID", "Slot Number", "Status", "Actions"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only actions column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? JPanel.class : Object.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        // Custom header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(new LineBorder(BORDER_COLOR, 1));
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
                    label.setBorder(new EmptyBorder(0, 20, 0, 20));
                    
                    if (column == 2) { // Status column
                        String status = value.toString();
                        switch (status) {
                            case "AVAILABLE":
                                label.setForeground(AVAILABLE_COLOR);
                                label.setText("<html><span style='background:#DCFCE7; padding:8px 16px; border-radius:20px; font-weight:bold;'>✅ " + status + "</span></html>");
                                break;
                            case "RESERVED":
                                label.setForeground(RESERVED_COLOR);
                                label.setText("<html><span style='background:#FEF9C3; padding:8px 16px; border-radius:20px; font-weight:bold;'>⏳ " + status + "</span></html>");
                                break;
                            case "OCCUPIED":
                                label.setForeground(OCCUPIED_COLOR);
                                label.setText("<html><span style='background:#FEE2E2; padding:8px 16px; border-radius:20px; font-weight:bold;'>🚗 " + status + "</span></html>");
                                break;
                        }
                    } else if (column == 3) {
                        return (Component) value; // Return the action buttons panel
                    } else {
                        label.setForeground(TEXT_PRIMARY);
                    }
                    
                    if (isSelected) {
                        label.setBackground(HOVER_COLOR);
                    } else {
                        label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    }
                }
                
                return c;
            }
        });
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(tableHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createActionButtons(ParkingSlot slot) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton editBtn = new JButton("✏️ Edit");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editBtn.setBackground(new Color(59, 130, 246));
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JButton deleteBtn = new JButton("🗑️ Delete");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteBtn.setBackground(new Color(239, 68, 68));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        editBtn.addActionListener(e -> showEditDialog(slot));
        deleteBtn.addActionListener(e -> showDeleteDialog(slot));
        
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        
        return buttonPanel;
    }

    // ================= FOOTER =================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(30, 41, 59));
        footer.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        timeLabel = new JLabel();
        timeLabel.setForeground(new Color(200, 200, 200));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        footer.add(infoLabel, BorderLayout.WEST);
        footer.add(timeLabel, BorderLayout.EAST);
        
        return footer;
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            timeLabel.setText("🕒 " + sdf.format(new Date()));
        });
        timer.start();
    }

    // ================= DATA LOADING =================
    private void loadSlots() {
        try {
            slots = slotDAO.findAll();
            tableModel.setRowCount(0);
            
            for (ParkingSlot s : slots) {
                tableModel.addRow(new Object[]{
                    s.getParkingSlotId(),
                    "Slot #" + s.getParkingSlotNumber(),
                    statusText(s.getParkingSlotStatus()),
                    createActionButtons(s)
                });
            }
            
            updateFooter();
            updateStatsPanel();
            
        } catch (SQLException e) {
            showMessage("Database Error", "Failed to load parking slots: " + e.getMessage(), 
                       "error", OCCUPIED_COLOR);
        }
    }

    private void updateFooter() {
        if (slots == null) return;
        
        long available = slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_AVAILABLE)
                .count();
        long reserved = slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_RESERVED)
                .count();
        long occupied = slots.stream()
                .filter(s -> s.getParkingSlotStatus() == ParkingSlot.STATUS_OCCUPIED)
                .count();
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a");
        infoLabel.setText("<html>📊 <b>" + slots.size() + "</b> total slots | "
                         + "✅ <b>" + available + "</b> available | "
                         + "⏳ <b>" + reserved + "</b> reserved | "
                         + "🚗 <b>" + occupied + "</b> occupied</html>");
        timeLabel.setText("🕒 " + sdf.format(new Date()));
    }

    private String statusText(int status) {
        return switch (status) {
            case ParkingSlot.STATUS_AVAILABLE -> "AVAILABLE";
            case ParkingSlot.STATUS_RESERVED -> "RESERVED";
            case ParkingSlot.STATUS_OCCUPIED -> "OCCUPIED";
            default -> "UNKNOWN";
        };
    }

    // ================= DIALOGS - FIXED =================
    private void showAddSlotDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Parking Slot");
        dialog.setModal(true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);
        
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(CARD_BG);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Title
        JLabel title = new JLabel("<html><center><h2 style='margin:0;'>➕ Add New Slot</h2>"
                                + "<p style='color:#64748B; margin-top:10px;'>Create a new parking slot</p></center></html>",
                                SwingConstants.CENTER);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel slotNoLabel = new JLabel("Slot Number:");
        slotNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(slotNoLabel, gbc);
        
        JTextField slotNoField = new JTextField(15);
        slotNoField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        slotNoField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(slotNoField, gbc);
        
        JLabel statusLabel = new JLabel("Initial Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(statusLabel, gbc);
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "RESERVED", "OCCUPIED"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setBackground(CARD_BG);
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(statusCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(TEXT_PRIMARY);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 30, 10, 30)
        ));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveBtn = new JButton("Save Slot");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(ADD_BUTTON_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ADD_BUTTON_COLOR.darker(), 1),
            new EmptyBorder(10, 30, 10, 30)
        ));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            try {
                String slotNumberText = slotNoField.getText().trim();
                if (slotNumberText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Please enter a slot number", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int slotNumber = Integer.parseInt(slotNumberText);
                if (slotNumber <= 0) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Slot number must be positive", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingSlotNumber(slotNumber);
                slot.setParkingSlotStatus(getStatusFromText((String) statusCombo.getSelectedItem()));
                slotDAO.create(slot);
                loadSlots();
                dialog.dispose();
                showMessage("Success", "Parking slot added successfully!", "success", ADD_BUTTON_COLOR);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a valid number for slot number", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                showMessage("Database Error", "Failed to add slot: " + ex.getMessage(), 
                           "error", OCCUPIED_COLOR);
            } catch (Exception ex) {
                showMessage("Error", "An unexpected error occurred: " + ex.getMessage(), 
                           "error", OCCUPIED_COLOR);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        content.add(title, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content);
        
        // Set focus to text field
        SwingUtilities.invokeLater(() -> slotNoField.requestFocusInWindow());
        
        dialog.setVisible(true);
    }

    private void showEditDialog(ParkingSlot slot) {
        // Create edit dialog similar to add dialog but with existing values
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Parking Slot");
        dialog.setModal(true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);
        
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(CARD_BG);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Title
        JLabel title = new JLabel("<html><center><h2 style='margin:0;'>✏️ Edit Slot #" + slot.getParkingSlotNumber() + "</h2>"
                                + "<p style='color:#64748B; margin-top:10px;'>Modify parking slot details</p></center></html>",
                                SwingConstants.CENTER);
        
        // Form with existing values
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Slot Number (disabled for editing)
        JLabel slotNoLabel = new JLabel("Slot Number:");
        slotNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(slotNoLabel, gbc);
        
        JTextField slotNoField = new JTextField(String.valueOf(slot.getParkingSlotNumber()));
        slotNoField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        slotNoField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        slotNoField.setEnabled(false); // Cannot change slot number
        slotNoField.setBackground(new Color(248, 249, 250));
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(slotNoField, gbc);
        
        // Status
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(statusLabel, gbc);
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "RESERVED", "OCCUPIED"});
        statusCombo.setSelectedItem(statusText(slot.getParkingSlotStatus()));
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setBackground(CARD_BG);
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        form.add(statusCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(TEXT_PRIMARY);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 30, 10, 30)
        ));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(new Color(16, 185, 129));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(16, 185, 129).darker(), 1),
            new EmptyBorder(10, 30, 10, 30)
        ));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            try {
                slot.setParkingSlotStatus(getStatusFromText((String) statusCombo.getSelectedItem()));
                slotDAO.update(slot);
                loadSlots();
                dialog.dispose();
                showMessage("Success", "Parking slot updated successfully!", "success", ADD_BUTTON_COLOR);
            } catch (SQLException ex) {
                showMessage("Database Error", "Failed to update slot: " + ex.getMessage(), 
                           "error", OCCUPIED_COLOR);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        content.add(title, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showDeleteDialog(ParkingSlot slot) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><div style='width:300px;'>"
            + "<p>Are you sure you want to delete this slot?</p>"
            + "<p><b>Slot:</b> #" + slot.getParkingSlotNumber() + "</p>"
            + "<p><b>Status:</b> " + statusText(slot.getParkingSlotStatus()) + "</p>"
            + "<p style='color:#EF4444; font-weight:bold;'>This action cannot be undone!</p>"
            + "</div></html>",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                slotDAO.delete(slot.getParkingSlotId());
                loadSlots();
                showMessage("Success", "Parking slot deleted successfully!", 
                           "success", ADD_BUTTON_COLOR);
            } catch (SQLException e) {
                showMessage("Error", "Failed to delete slot: " + e.getMessage(), 
                           "error", OCCUPIED_COLOR);
            }
        }
    }

    private int getStatusFromText(String status) {
        return switch (status) {
            case "AVAILABLE" -> ParkingSlot.STATUS_AVAILABLE;
            case "RESERVED" -> ParkingSlot.STATUS_RESERVED;
            case "OCCUPIED" -> ParkingSlot.STATUS_OCCUPIED;
            default -> ParkingSlot.STATUS_AVAILABLE;
        };
    }

    private void showMessage(String title, String message, String type, Color color) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);
        
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        String icon = switch (type) {
            case "success" -> "✅";
            case "error" -> "❌";
            case "warning" -> "⚠️";
            default -> "ℹ️";
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
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setFocusPainted(false);
        okButton.setBorder(new EmptyBorder(10, 30, 10, 30));
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