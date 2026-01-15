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
    private PaymentDAO paymentDAO = new PaymentDAO();

    private JTable table;
    private DefaultTableModel model;

    public UserMyPaymentsPanel(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Table model
        model = new DefaultTableModel(
            new String[]{"Payment ID", "Booking ID", "Due", "Paid", "Status"},
            0
        );

        table = new JTable(model);
        table.setRowHeight(28);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadPayments(); // ✅ correct method
    }

    // ================= LOAD DATA =================
    private void loadPayments() {
        try {
            model.setRowCount(0);
            List<Payment> list = paymentDAO.findByUserId(userId);

            for (Payment p : list) {
                model.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getBookingId(),
                    p.getDueAmount(),
                    p.getPaidAmount(),
                    p.getPaymentStatus() == Payment.STATUS_PAID ? "PAID" : "UNPAID"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= BOTTOM BUTTON =================
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton payBtn = new JButton("PAY NOW");
        payBtn.setBackground(new Color(76, 175, 80));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFocusPainted(false);

        payBtn.addActionListener(e -> paySelected());

        panel.add(payBtn);
        return panel;
    }

    // ================= PAY LOGIC =================
    private void paySelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a payment first");
            return;
        }

        int paymentId = (int) model.getValueAt(row, 0);
        double due = (double) model.getValueAt(row, 2);
        String status = model.getValueAt(row, 4).toString();

        if ("PAID".equals(status)) {
            JOptionPane.showMessageDialog(this, "Already paid");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter payment amount:", due);
        if (input == null) return;

        try {
            double amount = Double.parseDouble(input);
            paymentDAO.payNow(paymentId, amount, "USER");
            JOptionPane.showMessageDialog(this, "Payment successful!");
            loadPayments(); // 🔁 reload from DB
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
