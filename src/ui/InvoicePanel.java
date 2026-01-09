package ui;

import java.awt.*;
import javax.swing.*;
import models.Payment;

public class InvoicePanel extends JPanel {

    public InvoicePanel(Payment payment) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Invoice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        // Payment details
        JPanel details = new JPanel(new GridLayout(0, 2, 10, 10));
        details.setBackground(Color.WHITE);

        details.add(new JLabel("Payment Ref:"));
        details.add(new JLabel(payment.getRefNo()));

        details.add(new JLabel("Booking Ref:"));
        details.add(new JLabel(payment.getBookingRef()));

        details.add(new JLabel("User ID:"));
        details.add(new JLabel(String.valueOf(payment.getUserId())));

        details.add(new JLabel("Due Amount:"));
        details.add(new JLabel(String.valueOf(payment.getDueAmount())));

        details.add(new JLabel("Paid Amount:"));
        details.add(new JLabel(String.valueOf(payment.getPaidAmount())));

        details.add(new JLabel("Method:"));
        details.add(new JLabel(payment.getMethod()));

        details.add(new JLabel("Payment Date:"));
        details.add(new JLabel(payment.getPaymentDate().toString()));

        details.add(new JLabel("Remarks:"));
        details.add(new JLabel(payment.getRemarks()));

        add(details, BorderLayout.CENTER);
    }
}
