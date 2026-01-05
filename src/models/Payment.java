// src/models/Payment.java
package models;

import java.sql.Timestamp;

public class Payment {
    private Integer paymentId;
    private Integer bookingId;
    private Double amountDue;
    private Double amountPaid;
    private String remarks;
    private Integer paymentStatus;
    private String paidBy;
    private Integer userId;
    private Timestamp paymentDate;
    
    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PARTIAL = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_REFUNDED = 3;
    
    // Related objects
    private Booking booking;
    private User user;
    
    // Constructors
    public Payment() {}
    
    public Payment(Integer bookingId, Double amountDue) {
        this.bookingId = bookingId;
        this.amountDue = amountDue;
        this.paymentStatus = STATUS_PENDING;
        this.paymentDate = new Timestamp(System.currentTimeMillis());
    }
    
    // Getters and Setters
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
    
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    
    public Double getAmountDue() { return amountDue; }
    public void setAmountDue(Double amountDue) { this.amountDue = amountDue; }
    
    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaidBy() { return paidBy; }
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
    
    // Related object getters/setters
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    // Helper methods
    public Double getBalance() {
        return amountDue - (amountPaid != null ? amountPaid : 0.0);
    }
    
    public boolean isPaidInFull() {
        return getBalance() <= 0;
    }
    
    public String getStatusText() {
        switch (paymentStatus) {
            case STATUS_PENDING: return "Pending";
            case STATUS_PARTIAL: return "Partial";
            case STATUS_PAID: return "Paid";
            case STATUS_REFUNDED: return "Refunded";
            default: return "Unknown";
        }
    }
}