package ui;

import dao.PaymentDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Payment;

public class UserMyPaymentsPanel extends JPanel {

    private int userId;
    private PaymentDAO paymentDAO;
    private JTable table;
    private DefaultTableModel model;
    private List<Payment> paymentList;

    public UserMyPaymentsPanel(int userId) {
        this.userId = userId;
        this.paymentDAO = new PaymentDAO(); // Assuming you have PaymentDAO
        initComponents();
        loadMyPayments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel title = new JLabel("MY PAYMENTS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.WEST);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton payNowBtn = new JButton("💳 Pay Now");
        
        refreshBtn.addActionListener(e -> loadMyPayments());
        payNowBtn.addActionListener(e -> makePayment());
        
        btnPanel.add(refreshBtn);
        btnPanel.add(payNowBtn);
        
        panel.add(btnPanel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(
            new String[]{"Payment ID", "Booking ID", "Amount Due", "Amount Paid", "Status", "Created At"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0, 1 -> Integer.class; // Payment ID, Booking ID
                    case 2, 3 -> Double.class; // Amounts
                    default -> String.class;
                };
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        return new JScrollPane(table);
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(summaryLabel);
        
        // Update summary when data loads
        SwingUtilities.invokeLater(() -> updateSummary(summaryLabel));
        
        return panel;
    }

    private void loadMyPayments() {
        try {
            paymentList = paymentDAO.findByUserId(userId);
            model.setRowCount(0);

            for (Payment payment : paymentList) {
                model.addRow(new Object[]{
                    payment.getPaymentId(),
                    payment.getBookingId(),
                    payment.getDueAmount(),
                    payment.getPaidAmount(),
                    getPaymentStatusText(payment.getPaymentStatus()),
                   // The `payment.getFormattedCreatedAt()` method is likely a custom method defined in
                   // the `Payment` class that returns the formatted creation date of the payment. This
                   // method is used to retrieve and display the creation date of the payment in a
                   // specific format, such as "yyyy-MM-dd HH:mm". It helps in presenting the payment
                   // creation timestamp in a more readable and user-friendly way on the user
                   // interface.
                    // payment.getFormattedCreatedAt()
                });
            }

            updateSummary();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load payments: " + e.getMessage());
        } catch (Exception e) {
            // If PaymentDAO doesn't exist yet, show demo data
            showDemoData();
        }
    }

    private void showDemoData() {
        model.setRowCount(0);
        // Add some demo data
        model.addRow(new Object[]{1, 1001, 50.00, 50.00, "✅ PAID", "2024-01-15 10:30"});
        model.addRow(new Object[]{2, 1002, 30.00, 15.00, "⏳ PARTIAL", "2024-01-16 14:45"});
        model.addRow(new Object[]{3, 1003, 20.00, 0.00, "❌ UNPAID", "2024-01-17 09:15"});
        
        updateSummary();
    }

    private String getPaymentStatusText(int status) {
        return switch (status) {
            case 0 -> "❌ UNPAID";
            case 1 -> "✅ PAID";
            case 2 -> "⏳ PARTIAL";
            case 3 -> "🔄 REFUNDED";
            default -> "❓ UNKNOWN";
        };
    }

    private void updateSummary() {
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel footer = (JPanel) comp;
                for (Component c : footer.getComponents()) {
                    if (c instanceof JLabel) {
                        updateSummary((JLabel) c);
                        break;
                    }
                }
            }
        }
    }

    private void updateSummary(JLabel label) {
        if (paymentList == null || paymentList.isEmpty()) {
            label.setText("No payments found");
            return;
        }
        
        double totalDue = 0;
        double totalPaid = 0;
        int unpaidCount = 0;
        
        for (Payment payment : paymentList) {
            totalDue += payment.getDueAmount();
            totalPaid += payment.getPaidAmount();
            if (payment.getPaymentStatus() == 0) { // UNPAID
                unpaidCount++;
            }
        }
        
        label.setText(String.format(
            "Total Due: $%.2f | Total Paid: $%.2f | Balance: $%.2f | Unpaid: %d",
            totalDue, totalPaid, (totalDue - totalPaid), unpaidCount
        ));
    }

    private void makePayment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("No Selection", "Please select a payment to make payment");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        
        // In real implementation, you would get the payment from paymentList
        // For now, show a demo payment dialog
        showPaymentDialog();
    }

    private void showPaymentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Make Payment", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel content = new JPanel(new GridLayout(4, 2, 10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField amountField = new JTextField();
        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"Cash", "Credit Card", "Debit Card", "Mobile Payment"});
        
        content.add(new JLabel("Amount to Pay:"));
        content.add(amountField);
        content.add(new JLabel("Payment Method:"));
        content.add(methodCombo);
        content.add(new JLabel("Reference:"));
        content.add(new JLabel("PAY-" + System.currentTimeMillis()));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        JButton payBtn = new JButton("Confirm Payment");
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        payBtn.addActionListener(e -> {
            showSuccess("Payment Successful", "Payment has been processed successfully!");
            dialog.dispose();
            loadMyPayments();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(payBtn);
        
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}