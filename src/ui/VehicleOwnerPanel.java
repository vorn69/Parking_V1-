package ui;

import dao.VehicleOwnerDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import models.VehicleOwner;

public class VehicleOwnerPanel extends JPanel {

    private VehicleOwnerDAO ownerDAO = new VehicleOwnerDAO();
    private JTable table;
    private DefaultTableModel model;
    private List<VehicleOwner> ownerList;

    // Colors
    private final Color PRIMARY = new Color(33, 150, 243);
    private final Color SUCCESS = new Color(76, 175, 80);
    private final Color WARNING = new Color(255, 193, 7);
    private final Color DANGER = new Color(244, 67, 54);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color TEXT_PRIMARY = new Color(52, 73, 94);
    private final Color TEXT_SECONDARY = new Color(149, 165, 166);

    public VehicleOwnerPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadOwners();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel icon = new JLabel();
        icon.setIcon(new TextIcon("🚗", new Font("Segoe UI Emoji", Font.PLAIN, 28), TEXT_PRIMARY));

        JLabel title = new JLabel("VEHICLE OWNER MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        titlePanel.add(icon);
        titlePanel.add(title);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = createStyledButton("ADD OWNER", SUCCESS, "Add new vehicle owner", "➕");
        JButton refreshBtn = createStyledButton("REFRESH", PRIMARY, "Refresh data", "🔄");
        JButton exportBtn = createStyledButton("EXPORT", new Color(155, 89, 182), "Export to CSV", "📊");

        addBtn.addActionListener(e -> addOwner());
        refreshBtn.addActionListener(e -> loadOwners());
        exportBtn.addActionListener(e -> exportOwners());

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(exportBtn);

        header.add(btnPanel, BorderLayout.EAST);
        return header;
    }

    private JButton createStyledButton(String text, Color bgColor, String tooltip, String iconSymbol) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgColor.darker(), 1),
                new EmptyBorder(10, 15, 10, 15)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setIcon(new TextIcon(iconSymbol, new Font("Segoe UI Emoji", Font.PLAIN, 14), Color.WHITE));
        btn.setIconTextGap(8);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }

            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1), new EmptyBorder(0, 0, 0, 0)));

        model = new DefaultTableModel(
                new String[] { "Owner ID", "Name", "Contact", "Email", "User ID", "Status", "Actions" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 6 ? JPanel.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Status Renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                String status = value != null ? value.toString() : "";
                if (status.contains("ACTIVE")) {
                    label.setForeground(SUCCESS);
                    label.setText(
                            "<html><span style='background:#EAFAF1; padding:4px 8px; border-radius:10px; color:#2ECC71;'>ACTIVE</span></html>");
                } else if (status.contains("INACTIVE")) {
                    label.setForeground(DANGER);
                    label.setText(
                            "<html><span style='background:#FDEDEC; padding:4px 8px; border-radius:10px; color:#E74C3C;'>INACTIVE</span></html>");
                }
                return label;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel cLabel = new JLabel();
        cLabel.setForeground(Color.WHITE);
        panel.add(cLabel, BorderLayout.WEST);
        JLabel tLabel = new JLabel();
        tLabel.setForeground(new Color(189, 195, 199));
        panel.add(tLabel, BorderLayout.EAST);
        new Timer(1000, e -> updateFooter(cLabel, tLabel)).start();
        return panel;
    }

    private void updateFooter(JLabel cLabel, JLabel tLabel) {
        if (ownerList == null)
            return;
        cLabel.setText("Total Owners: " + ownerList.size() + " | Active: "
                + ownerList.stream().filter(o -> o.getStatus() != null && o.getStatus() == 1).count());
        tLabel.setText(
                "🕒 " + new java.text.SimpleDateFormat("EEE, MMM dd yyyy | hh:mm:ss a").format(new java.util.Date()));
    }

    private void loadOwners() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ownerList = ownerDAO.findAll();
                SwingUtilities.invokeLater(() -> updateTable());
                return null;
            }
        };
        worker.execute();
    }

    private void updateTable() {
        model.setRowCount(0);
        if (ownerList == null)
            return;
        for (VehicleOwner owner : ownerList) {
            model.addRow(new Object[] {
                    owner.getVehicleOwnerId(), owner.getVehicleOwnerName(), owner.getVehicleOwnerContact(),
                    owner.getVehicleOwnerEmail(), owner.getUserId(), owner.getStatus() == 1 ? "ACTIVE" : "INACTIVE",
                    createActionButtons(owner)
            });
        }
    }

    private JPanel createActionButtons(VehicleOwner owner) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);
        JButton vBtn = createSmallBtn("View Details", PRIMARY, "👁");
        vBtn.addActionListener(e -> viewOwnerDetails(owner));
        JButton eBtn = createSmallBtn("Edit Owner", WARNING, "✏️");
        eBtn.addActionListener(e -> editOwner(owner));
        JButton dBtn = createSmallBtn("Delete Owner", DANGER, "🗑️");
        dBtn.addActionListener(e -> deleteOwner(owner));
        panel.add(vBtn);
        panel.add(eBtn);
        panel.add(dBtn);
        return panel;
    }

    private JButton createSmallBtn(String tip, Color color, String sym) {
        JButton btn = new JButton();
        btn.setIcon(new TextIcon(sym, new Font("Segoe UI Emoji", Font.PLAIN, 12), Color.WHITE));
        btn.setToolTipText(tip);
        btn.setBackground(color);
        btn.setBorder(new EmptyBorder(5, 8, 5, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- RESTORED LOGIC FROM ORIGINAL FILE ---
    private void viewOwnerDetails(VehicleOwner owner) {
        StringBuilder sb = new StringBuilder("<html><div style='width:300px; padding:10px;'><h3>Owner Details</h3>");
        sb.append("<b>ID:</b> ").append(owner.getVehicleOwnerId()).append("<br>");
        sb.append("<b>Name:</b> ").append(owner.getVehicleOwnerName()).append("<br>");
        sb.append("<b>Contact:</b> ").append(owner.getVehicleOwnerContact()).append("<br>");
        sb.append("<b>Email:</b> ").append(owner.getVehicleOwnerEmail()).append("<br>");
        sb.append("<b>Username:</b> ").append(owner.getOwnerUsername()).append("<br>");
        sb.append("<b>Status:</b> ").append(owner.getStatus() == 1 ? "Active" : "Inactive").append("</div></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Owner Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addOwner() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Owner", true);
        d.setLayout(new BorderLayout());
        d.setSize(400, 450);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField n = new JTextField(), c = new JTextField(), e = new JTextField(), u = new JTextField();
        JPasswordField ps = new JPasswordField();
        JTextField ui = new JTextField();
        p.add(new JLabel("Name:"));
        p.add(n);
        p.add(new JLabel("Contact*:"));
        p.add(c);
        p.add(new JLabel("Email*:"));
        p.add(e);
        p.add(new JLabel("Username:"));
        p.add(u);
        p.add(new JLabel("Password:"));
        p.add(ps);
        p.add(new JLabel("User ID:"));
        p.add(ui);
        JButton s = new JButton("Save");
        s.addActionListener(evt -> {
            try {
                VehicleOwner o = new VehicleOwner();
                o.setVehicleOwnerName(n.getText());
                o.setVehicleOwnerContact(c.getText());
                o.setVehicleOwnerEmail(e.getText());
                o.setOwnerUsername(u.getText());
                o.setOwnerPassword(new String(ps.getPassword()));
                o.setStatus(1);
                if (!ui.getText().isEmpty())
                    o.setUserId(Integer.parseInt(ui.getText()));
                if (ownerDAO.create(o) != null) {
                    d.dispose();
                    loadOwners();
                }
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        });
        d.add(p, BorderLayout.CENTER);
        d.add(s, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void editOwner(VehicleOwner owner) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Owner", true);
        d.setLayout(new BorderLayout());
        d.setSize(400, 450);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField n = new JTextField(owner.getVehicleOwnerName()), c = new JTextField(owner.getVehicleOwnerContact()),
                e = new JTextField(owner.getVehicleOwnerEmail());
        JTextField u = new JTextField(owner.getOwnerUsername());
        JComboBox<String> st = new JComboBox<>(new String[] { "Active", "Inactive" });
        st.setSelectedItem(owner.getStatus() == 1 ? "Active" : "Inactive");
        p.add(new JLabel("Name:"));
        p.add(n);
        p.add(new JLabel("Contact:"));
        p.add(c);
        p.add(new JLabel("Email:"));
        p.add(e);
        p.add(new JLabel("Username:"));
        p.add(u);
        p.add(new JLabel("Status:"));
        p.add(st);
        JButton s = new JButton("Update");
        s.addActionListener(evt -> {
            try {
                owner.setVehicleOwnerName(n.getText());
                owner.setVehicleOwnerContact(c.getText());
                owner.setVehicleOwnerEmail(e.getText());
                owner.setOwnerUsername(u.getText());
                owner.setStatus("Active".equals(st.getSelectedItem()) ? 1 : 0);
                if (ownerDAO.update(owner)) {
                    d.dispose();
                    loadOwners();
                }
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        });
        d.add(p, BorderLayout.CENTER);
        d.add(s, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void deleteOwner(VehicleOwner owner) {
        if (JOptionPane.showConfirmDialog(this, "Delete " + owner.getVehicleOwnerName() + "?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                ownerDAO.delete(owner.getVehicleOwnerId());
                loadOwners();
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        }
    }

    private void exportOwners() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter w = new java.io.PrintWriter(fc.getSelectedFile())) {
                w.println("ID,Name,Contact,Email,Status");
                for (VehicleOwner o : ownerList)
                    w.println(o.getVehicleOwnerId() + "," + o.getVehicleOwnerName() + "," + o.getVehicleOwnerContact()
                            + "," + o.getVehicleOwnerEmail() + "," + (o.getStatus() == 1 ? "Active" : "Inactive"));
                JOptionPane.showMessageDialog(this, "Exported!");
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        }
    }

    private void showError(String t, String m) {
        JOptionPane.showMessageDialog(this, m, t, JOptionPane.ERROR_MESSAGE);
    }

    // TextIcon class
    private static class TextIcon implements Icon {
        private String text;
        private Font font;
        private Color color;

        public TextIcon(String t, Font f, Color c) {
            text = t;
            font = f;
            color = c;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(font);
            g2d.setColor(color);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(text, x + (getIconWidth() - fm.stringWidth(text)) / 2,
                    y + (getIconHeight() + fm.getAscent()) / 2 - 2);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }
    }

    class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean h, int r, int c) {
            if (v instanceof JPanel)
                return (JPanel) v;
            return super.getTableCellRendererComponent(t, v, s, h, r, c);
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel p;

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            p = (JPanel) v;
            return p;
        }

        @Override
        public Object getCellEditorValue() {
            return p;
        }
    }
}