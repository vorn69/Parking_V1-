package ui;

import dao.BookingDAO;
import dao.PaymentDAO;
import models.Booking;
import models.Payment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PaymentPanel extends JPanel {

    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> bookingCombo;
    private JTextField refNoField, methodField, paidField, remarksField;
    private PaymentDAO paymentDAO;
    private BookingDAO bookingDAO;

    public PaymentPanel() {
        paymentDAO = new PaymentDAO();
        bookingDAO = new BookingDAO();
        setLayout(new BorderLayout());

        createTopPanel();
        createTable();
        loadPayments();
        loadBookingCombo();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        refNoField = new JTextField(10);
        methodField = new JTextField(10);
        paidField = new JTextField(7);
        remarksField = new JTextField(15);
        bookingCombo = new JComboBox<>();

        JButton addBtn = new JButton("Add Payment");
        addBtn.addActionListener(e -> addPayment());

        topPanel.add(new JLabel("Ref No:"));
        topPanel.add(refNoField);
        topPanel.add(new JLabel("Booking:"));
        topPanel.add(bookingCombo);
        topPanel.add(new JLabel("Method:"));
        topPanel.add(methodField);
        topPanel.add(new JLabel("Paid:"));
        topPanel.add(paidField);
        topPanel.add(new JLabel("Remarks:"));
        topPanel.add(remarksField);
        topPanel.add(addBtn);

        add(topPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"Ref No", "Booking Ref", "User ID", "Paid Amount", "Method", "Date", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0);
        paymentTable = new JTable(tableModel);
        add(new JScrollPane(paymentTable), BorderLayout.CENTER);
    }

    private void loadPayments() {
        tableModel.setRowCount(0);
        try {
            List<Payment> payments = paymentDAO.findAll(); // Use findAll()
            for (Payment p : payments) {
                tableModel.addRow(new Object[]{
                        p.getRefNo(),
                        p.getBookingRef(),
                        p.getUserId(),
                        p.getPaidAmount(),
                        p.getMethod(),
                        p.getPaymentDate(),
                        p.getRemarks()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBookingCombo() {
        bookingCombo.removeAllItems();
        try {
            for (Booking b : bookingDAO.findAll()) { // Use findAll()
                bookingCombo.addItem(b.getBookingRef());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addPayment() {
        String refNo = refNoField.getText();
        String bookingRef = (String) bookingCombo.getSelectedItem();
        String method = methodField.getText();
        String paidStr = paidField.getText();
        String remarks = remarksField.getText();

        if (refNo.isEmpty() || bookingRef == null || method.isEmpty() || paidStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields");
            return;
        }

        double paid;
        try {
            paid = Double.parseDouble(paidStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid paid amount");
            return;
        }

        try {
            Booking b = bookingDAO.findAll().stream()
                    .filter(x -> x.getBookingRef().equals(bookingRef))
                    .findFirst().orElse(null);

            if (b == null) {
                JOptionPane.showMessageDialog(this, "Booking not found");
                return;
            }

            Payment p = new Payment();
            p.setRefNo(refNo);
            p.setBookingRef(bookingRef);
            p.setUserId(b.getUserId());
            p.setPaidAmount(paid);
            p.setMethod(method);
            p.setPaymentDate(new Date());
            p.setRemarks(remarks);

            if (paymentDAO.insertPayment(p)) {
                JOptionPane.showMessageDialog(this, "Payment added successfully");
                loadPayments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add payment");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
