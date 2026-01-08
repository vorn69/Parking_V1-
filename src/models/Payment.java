package models;

import java.util.Date;

public class Payment {

    private String refNo;
    private String bookingRef;
    private Integer userId;
    private double dueAmount;
    private double paidAmount;
    private String method;
    private Date paymentDate;
    private String remarks;

    // Full constructor
    public Payment(String refNo, String bookingRef, Integer userId, double dueAmount,
                   double paidAmount, String method, Date paymentDate, String remarks) {
        this.refNo = refNo;
        this.bookingRef = bookingRef;
        this.userId = userId;
        this.dueAmount = dueAmount;
        this.paidAmount = paidAmount;
        this.method = method;
        this.paymentDate = paymentDate;
        this.remarks = remarks;
    }

    // No-arg constructor (needed by PaymentPanel)
    public Payment() {}

    // GETTERS & SETTERS
    public String getRefNo() { return refNo; }
    public void setRefNo(String refNo) { this.refNo = refNo; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
