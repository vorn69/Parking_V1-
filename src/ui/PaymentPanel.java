package ui;

import dao.PaymentDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Payment;

public class PaymentPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private PaymentDAO paymentDAO;

    // Colors (dashboard style)
    private final Color BG = new Color(245, 247, 250);
    private final Color CARD = Color.WHITE;
    private final Color BLUE = new Color(33, 150, 243);
    private final Color GREEN = new Color(76, 175, 80);
    private final Color ORANGE = new Color(255, 152, 0);

    public PaymentPanel() {
        paymentDAO = new PaymentDAO();
        setLayout(new BorderLayout(20, 20));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        loadPayments();
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JLabel title = new JLabel("Payment Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JButton addBtn = new JButton("+ Add Payment");
        addBtn.setBackground(GREEN);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> showAddDialog());

        panel.add(title, BorderLayout.WEST);
        panel.add(addBtn, BorderLayout.EAST);

        return panel;
    }

    // ================= TABLE CARD =================
    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        model = new DefaultTableModel(
                new String[]{
                        "Ref No", "Booking Ref", "User ID",
                        "Due", "Paid", "Method", "Date", "Remarks"
                }, 0
        );

        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        card.add(scroll, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(CARD);

        // Refresh
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(BLUE);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> loadPayments());
        bottom.add(refreshBtn);

        // Invoice
        JButton invoiceBtn = new JButton("Invoice");
        invoiceBtn.setBackground(ORANGE);
        invoiceBtn.setForeground(Color.WHITE);
        invoiceBtn.addActionListener(e -> showInvoice());
        bottom.add(invoiceBtn);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ================= LOAD DATA =================
    private void loadPayments() {
        model.setRowCount(0);
        try {
            List<Payment> list = paymentDAO.findAll();
            for (Payment p : list) {
                model.addRow(new Object[]{
                        p.getRefNo(),
                        p.getBookingRef(),
                        p.getUserId(),
                        p.getDueAmount(),
                        p.getPaidAmount(),
                        p.getMethod(),
                        p.getPaymentDate(),
                        p.getRemarks()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= ADD PAYMENT =================
    private void showAddDialog() {
        JTextField bookingRef = new JTextField();
        JTextField userId = new JTextField();
        JTextField due = new JTextField();
        JTextField paid = new JTextField();
        JComboBox<String> method = new JComboBox<>(new String[]{"CASH", "CARD", "ABA", "ACLED"});
        JTextField remarks = new JTextField();

        Object[] fields = {
                "Booking Ref:", bookingRef,
                "User ID:", userId,
                "Due Amount:", due,
                "Paid Amount:", paid,
                "Method:", method,
                "Remarks:", remarks
        };

        int opt = JOptionPane.showConfirmDialog(
                this, fields, "Add Payment",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (opt == JOptionPane.OK_OPTION) {
            try {
                Payment p = new Payment();
                p.setRefNo("PAY-" + UUID.randomUUID().toString().substring(0, 8));
                p.setBookingRef(bookingRef.getText());
                p.setUserId(Integer.parseInt(userId.getText()));
                p.setDueAmount(Double.parseDouble(due.getText()));
                p.setPaidAmount(Double.parseDouble(paid.getText()));
                p.setMethod(method.getSelectedItem().toString());
                p.setPaymentDate(new Date());
                p.setRemarks(remarks.getText());

                paymentDAO.insertPayment(p);
                loadPayments();

                JOptionPane.showMessageDialog(this, "Payment added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ================= SHOW INVOICE =================
    private void showInvoice() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String refNo = table.getValueAt(selectedRow, 0).toString(); // Ref No

        try {
            Payment payment = paymentDAO.findByRefNo(refNo); // Fetch payment from DB
            if (payment != null) {
                JFrame invoiceFrame = new JFrame("Invoice - " + payment.getRefNo());
                invoiceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                invoiceFrame.setSize(500, 600);
                invoiceFrame.setLocationRelativeTo(this);
                invoiceFrame.add(new InvoicePanel(payment)); // Pass payment
                invoiceFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Payment not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading invoice: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
