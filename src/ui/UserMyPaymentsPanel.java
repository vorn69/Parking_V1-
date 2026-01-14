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

        model = new DefaultTableModel(
            new String[]{"Payment ID", "Booking ID", "Due", "Paid", "Status"},
            0
        );

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadMyPayments();
    }

    private void loadMyPayments() {
        try {
            model.setRowCount(0);
            List<Payment> list = paymentDAO.findByUserId(userId);
            for (Payment p : list) {
                model.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getBookingId(),
                    p.getDueAmount(),
                    p.getPaidAmount(),
                    p.getPaymentStatus() == Payment.STATUS_PAID ? "PAID" : "PENDING"

                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load payments");
        }
    }
}
