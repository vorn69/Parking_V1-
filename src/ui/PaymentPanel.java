package ui;

import dao.PaymentDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.Payment;

public class PaymentPanel extends JPanel {

    private PaymentDAO paymentDAO = new PaymentDAO();
    private JTable table;
    private DefaultTableModel model;
    private List<Payment> paymentList;
    
    private JLabel infoLabel;
    private JLabel timeLabel;

    public PaymentPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadPayments();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("PAYMENT MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton refreshBtn = createHeaderButton("🔄 REFRESH", new Color(33, 150, 243));
        JButton markPaidBtn = createHeaderButton("✅ MARK AS PAID", new Color(76, 175, 80));
        JButton invoiceBtn = createHeaderButton("📄 INVOICE", new Color(255, 152, 0));
        JButton addPaymentBtn = createHeaderButton("➕ ADD PAYMENT", new Color(156, 39, 176));

        refreshBtn.addActionListener(e -> loadPayments());
        markPaidBtn.addActionListener(e -> markAsPaid());
        invoiceBtn.addActionListener(e -> generateInvoice());
        addPaymentBtn.addActionListener(e -> addPayment());

        btnPanel.add(refreshBtn);
        btnPanel.add(markPaidBtn);
        btnPanel.add(invoiceBtn);
        btnPanel.add(addPaymentBtn);

        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton createHeaderButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(
            new String[]{"Payment ID", "Booking ID", "Amount Due", "Amount Paid", "Balance", "Status", "User ID", "Created"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    
                    String text = value.toString();
                    if (text.contains("✅")) {
                        label.setBackground(new Color(212, 237, 218));
                        label.setForeground(new Color(21, 87, 36));
                    } else if (text.contains("❌")) {
                        label.setBackground(new Color(248, 215, 218));
                        label.setForeground(new Color(114, 28, 36));
                    } else if (text.contains("⚠️")) {
                        label.setBackground(new Color(255, 243, 205));
                        label.setForeground(new Color(133, 100, 4));
                    }
                    label.setOpaque(true);
                }
                return c;
            }
        });
        
        return new JScrollPane(table);
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(33, 33, 33));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);

        timeLabel = new JLabel();
        timeLabel.setForeground(Color.LIGHT_GRAY);

        panel.add(infoLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);
        return panel;
    }

    private void loadPayments() {
        try {
            paymentList = paymentDAO.findAll();
            model.setRowCount(0);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            DecimalFormat df = new DecimalFormat("#,##0.00");
            
            for (Payment p : paymentList) {
                double balance = p.getDueAmount() - p.getPaidAmount();
                model.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getBookingId(),
                    "$" + df.format(p.getDueAmount()),
                    "$" + df.format(p.getPaidAmount()),
                    "$" + df.format(balance),
                    getPaymentStatusText(p.getPaymentStatus()),
                    p.getUserId(),
                    p.getPaymentDate() != null ? sdf.format(p.getPaymentDate()) : "N/A"
                });
            }

            updateFooter();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load payments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getPaymentStatusText(int status) {
        return switch (status) {
            case 0 -> "❌ UNPAID";
            case 1 -> "✅ PAID";
            case 2 -> "⚠️ PARTIAL";
            default -> "❓ UNKNOWN";
        };
    }

    private void updateFooter() {
        double totalDue = 0;
        double totalPaid = 0;
        int unpaidCount = 0;
        int paidCount = 0;
        
        if (paymentList != null) {
            for (Payment p : paymentList) {
                totalDue += p.getDueAmount();
                totalPaid += p.getPaidAmount();
                if (p.getPaymentStatus() == 0) {
                    unpaidCount++;
                } else if (p.getPaymentStatus() == 1) {
                    paidCount++;
                }
            }
        }
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        infoLabel.setText(String.format(
            "Total: %d | Paid: %d | Unpaid: %d | Due: $%s | Paid: $%s",
            paymentList != null ? paymentList.size() : 0,
            paidCount, unpaidCount, df.format(totalDue), df.format(totalPaid)
        ));
        
        timeLabel.setText("Updated: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void markAsPaid() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment first", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Payment payment = paymentList.get(selectedRow);
        
        if (payment.getPaymentStatus() == 1) {
            JOptionPane.showMessageDialog(this, "This payment is already marked as paid", "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show payment dialog
        JTextField amountField = new JTextField(String.valueOf(payment.getDueAmount()));
        JTextField paidByField = new JTextField("Admin");
        
        Object[] form = {
            "Payment ID: " + payment.getPaymentId(),
            "Booking ID: " + payment.getBookingId(),
            "Amount Due: $" + payment.getDueAmount(),
            "Amount to Pay:", amountField,
            "Paid By:", paidByField
        };

        int option = JOptionPane.showConfirmDialog(
            this, form, "Mark as Paid", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String paidBy = paidByField.getText().trim();
                
                if (paidBy.isEmpty()) {
                    paidBy = "Admin";
                }
                
                // Update payment in database
                updatePayment(payment.getPaymentId(), amount, paidBy);
                
                JOptionPane.showMessageDialog(this, "Payment marked as paid successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPayments();
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to update payment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePayment(int paymentId, double amount, String paidBy) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_payment
            SET amount_paid = ?, 
                payment_status = CASE 
                    WHEN ? >= amount_due THEN 1 
                    WHEN ? > 0 THEN 2 
                    ELSE 0 
                END,
                paid_by = ?
            WHERE payment_id = ?
        """;
        
        paymentDAO.executeUpdate(sql, amount, amount, amount, paidBy, paymentId);
    }

    private void generateInvoice() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment to generate invoice", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Payment payment = paymentList.get(selectedRow);
        
        // Create invoice dialog
        JDialog invoiceDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Invoice", true);
        invoiceDialog.setLayout(new BorderLayout());
        invoiceDialog.setSize(500, 700);
        invoiceDialog.setLocationRelativeTo(this);
        
        // Invoice content
        JPanel invoicePanel = new JPanel(new BorderLayout(10, 10));
        invoicePanel.setBackground(Color.WHITE);
        invoicePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("INVOICE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(59, 89, 152));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel invoiceNoLabel = new JLabel("INVOICE #" + payment.getPaymentId());
        invoiceNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        invoiceNoLabel.setForeground(Color.DARK_GRAY);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(invoiceNoLabel, BorderLayout.SOUTH);
        
        // Company Info
        JPanel companyPanel = new JPanel(new BorderLayout());
        companyPanel.setBackground(Color.WHITE);
        companyPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel companyLabel = new JLabel("<html><b>Parking Management System</b><br>" +
                                         "123 Parking Street<br>" +
                                         "City, State 12345<br>" +
                                         "Phone: (123) 456-7890<br>" +
                                         "Email: info@parking.com</html>");
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        JLabel dateLabel = new JLabel("<html><b>Invoice Date:</b><br>" + sdf.format(new Date()) + "</html>");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        companyPanel.add(companyLabel, BorderLayout.WEST);
        companyPanel.add(dateLabel, BorderLayout.EAST);
        
        // Bill To section
        JPanel billToPanel = new JPanel(new BorderLayout());
        billToPanel.setBackground(new Color(248, 249, 250));
        billToPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel billToLabel = new JLabel("<html><b>Bill To:</b><br>" +
                                        "User ID: " + payment.getUserId() + "<br>" +
                                        "Booking ID: " + payment.getBookingId() + "<br>" +
                                        "Payment Status: " + getPaymentStatusText(payment.getPaymentStatus()) + "</html>");
        billToLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        billToPanel.add(billToLabel, BorderLayout.WEST);
        
        // Payment Details Table
        String[] columns = {"Description", "Amount"};
        Object[][] data = {
            {"Parking Booking #" + payment.getBookingId(), ""},
            {"Amount Due", "$" + String.format("%.2f", payment.getDueAmount())},
            {"Amount Paid", "$" + String.format("%.2f", payment.getPaidAmount())},
            {"Balance Due", "$" + String.format("%.2f", payment.getDueAmount() - payment.getPaidAmount())}
        };
        
        JTable invoiceTable = new JTable(data, columns);
        invoiceTable.setRowHeight(30);
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        invoiceTable.setEnabled(false);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.getTableHeader().setBackground(new Color(248, 249, 250));
        
        JScrollPane tableScroll = new JScrollPane(invoiceTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Total Section
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(new Color(248, 249, 250));
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel totalLabel = new JLabel("<html><b>TOTAL DUE:</b></html>");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        double balance = payment.getDueAmount() - payment.getPaidAmount();
        JLabel amountLabel = new JLabel("$" + String.format("%.2f", balance));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amountLabel.setForeground(new Color(220, 53, 69));
        amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        totalPanel.add(totalLabel, BorderLayout.WEST);
        totalPanel.add(amountLabel, BorderLayout.EAST);
        
        // Footer Notes
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel notesLabel = new JLabel("<html><b>Notes:</b><br>" +
                                       "• Payment due within 30 days<br>" +
                                       "• Late payments subject to 5% fee<br>" +
                                       "• Please include invoice number with payment<br>" +
                                       "• Contact us for any questions</html>");
        notesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        notesLabel.setForeground(Color.GRAY);
        
        JLabel thankYouLabel = new JLabel("Thank you for your business!");
        thankYouLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        thankYouLabel.setForeground(new Color(59, 89, 152));
        thankYouLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        footerPanel.add(notesLabel, BorderLayout.WEST);
        footerPanel.add(thankYouLabel, BorderLayout.SOUTH);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton printBtn = new JButton("🖨️ Print Invoice");
        JButton saveBtn = new JButton("💾 Save as PDF");
        JButton closeBtn = new JButton("✖ Close");
        
        printBtn.setBackground(new Color(59, 89, 152));
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        
        saveBtn.setBackground(new Color(76, 175, 80));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        
        closeBtn.setBackground(new Color(220, 53, 69));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(invoiceDialog, 
                "Print functionality would be implemented here", 
                "Print Invoice", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(invoiceDialog, 
                "PDF export functionality would be implemented here", 
                "Save as PDF", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        closeBtn.addActionListener(e -> invoiceDialog.dispose());
        
        buttonPanel.add(printBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(closeBtn);
        
        // Assemble invoice
        invoicePanel.add(headerPanel, BorderLayout.NORTH);
        invoicePanel.add(companyPanel, BorderLayout.CENTER);
        invoicePanel.add(billToPanel, BorderLayout.PAGE_START);
        invoicePanel.add(tableScroll, BorderLayout.CENTER);
        invoicePanel.add(totalPanel, BorderLayout.PAGE_END);
        invoicePanel.add(footerPanel, BorderLayout.SOUTH);
        
        invoiceDialog.add(invoicePanel, BorderLayout.CENTER);
        invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        invoiceDialog.setVisible(true);
    }

    private void addPayment() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Payment", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField bookingIdField = new JTextField();
        JTextField userIdField = new JTextField();
        JTextField amountDueField = new JTextField();
        JTextField amountPaidField = new JTextField("0");
        
        formPanel.add(new JLabel("Booking ID*:"));
        formPanel.add(bookingIdField);
        formPanel.add(new JLabel("User ID*:"));
        formPanel.add(userIdField);
        formPanel.add(new JLabel("Amount Due*:"));
        formPanel.add(amountDueField);
        formPanel.add(new JLabel("Amount Paid:"));
        formPanel.add(amountPaidField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn = new JButton("Save Payment");
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            saveNewPayment(bookingIdField, userIdField, amountDueField, amountPaidField, dialog);
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        
        dialog.add(new JLabel("   Add New Payment", SwingConstants.CENTER), BorderLayout.NORTH);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveNewPayment(JTextField bookingIdField, JTextField userIdField,
                               JTextField amountDueField, JTextField amountPaidField,
                               JDialog dialog) {
        try {
            int bookingId = Integer.parseInt(bookingIdField.getText());
            int userId = Integer.parseInt(userIdField.getText());
            double amountDue = Double.parseDouble(amountDueField.getText());
            double amountPaid = Double.parseDouble(amountPaidField.getText());
            
            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            payment.setUserId(userId);
            payment.setDueAmount(amountDue);
            payment.setPaidAmount(amountPaid);
            payment.setPaymentStatus(amountPaid >= amountDue ? 1 : (amountPaid > 0 ? 2 : 0));
            
            paymentDAO.create(payment);
            
            JOptionPane.showMessageDialog(this, "Payment added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            loadPayments();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to save payment: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}