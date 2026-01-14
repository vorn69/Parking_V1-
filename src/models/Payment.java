package models;

import java.util.Date;

public class Payment {

    private Integer paymentId;
    private Integer bookingId;
    private String refNo;          // e.g., "PAY-1234"
    private String bookingRef;     // booking reference string
    private double dueAmount;
    private double paidAmount;
    private String method;         // payment method
    private Date paymentDate;
        private int paymentStatus;     // 0 = pending, 1 = paid
        private String paidBy;         // customer username
    private Integer userId;
    private String remarks;

    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;


    // Getters & Setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public String getRefNo() { return refNo; }
    public void setRefNo(String refNo) { this.refNo = refNo; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }

    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public int getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(int paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaidBy() { return paidBy; }
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
