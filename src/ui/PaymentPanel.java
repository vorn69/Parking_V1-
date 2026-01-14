package ui;

import dao.PaymentDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Payment;

public class PaymentPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private PaymentDAO paymentDAO = new PaymentDAO();

    private final Color BG = new Color(245, 247, 250);
    private final Color CARD = Color.WHITE;
    private final Color BLUE = new Color(33, 150, 243);
    private final Color GREEN = new Color(76, 175, 80);

    public PaymentPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);

        loadPayments();
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("PAYMENTS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton addBtn = new JButton("+ ADD PAYMENT");
        addBtn.setBackground(GREEN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> addPayment());

        panel.add(title, BorderLayout.WEST);
        panel.add(addBtn, BorderLayout.EAST);
        return panel;
    }

    // ================= TABLE =================
    private JScrollPane createTable() {
        model = new DefaultTableModel(
            new String[]{"Payment ID", "Booking ID", "User ID", "Due", "Paid", "Status", "Paid By", "Remarks"},
            0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(30);

        return new JScrollPane(table);
    }

    // ================= LOAD =================
    private void loadPayments() {
        model.setRowCount(0);
        try {
            List<Payment> list = paymentDAO.findAll();
            for (Payment p : list) {
                model.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getBookingId(),
                    p.getUserId(),
                    p.getDueAmount(),
                    p.getPaidAmount(),
                    paymentStatusText(p.getPaymentStatus()),
                    p.getPaidBy(),
                    p.getRemarks()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String paymentStatusText(int status) {
        return status == Payment.STATUS_PAID ? "PAID" : "PENDING";
    }

    // ================= ADD PAYMENT =================
    private void addPayment() {
        JTextField bookingId = new JTextField();
        JTextField userId = new JTextField();
        JTextField due = new JTextField();
        JTextField paid = new JTextField();
        JTextField paidBy = new JTextField();
        JTextField remarks = new JTextField();

        Object[] fields = {
            "Booking ID:", bookingId,
            "User ID:", userId,
            "Amount Due:", due,
            "Amount Paid:", paid,
            "Paid By:", paidBy,
            "Remarks:", remarks
        };

        int opt = JOptionPane.showConfirmDialog(
            this, fields, "Add Payment", JOptionPane.OK_CANCEL_OPTION
        );

        if (opt == JOptionPane.OK_OPTION) {
            try {
                Payment p = new Payment();
                p.setBookingId(Integer.parseInt(bookingId.getText()));
                p.setUserId(Integer.parseInt(userId.getText()));
                p.setDueAmount(Double.parseDouble(due.getText()));
                p.setPaidAmount(Double.parseDouble(paid.getText()));
                p.setPaymentStatus(Payment.STATUS_PAID);
                p.setPaidBy(paidBy.getText());
                p.setRemarks(remarks.getText());

                paymentDAO.create(p);
                loadPayments();

                JOptionPane.showMessageDialog(this, "Payment saved successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
