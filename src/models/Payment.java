package models;

import java.util.Date;
import java.util.Objects;

public class Payment {
    private Integer paymentId;
    private Integer bookingId;
    private Integer userId;
    private Double dueAmount;
    private Double paidAmount;
    private Integer paymentStatus; // 0 = pending, 1 = paid, 2 = partial
    private String paidBy;
    private String remarks;
    private Date paymentDate;

    // Optional fields for display (not in database)
    private String bookingRef;
    private String userName;
    private String fullName;
    private String paymentMethod;

    // Status constants - MATCHING PAYMENTDAO
    public static final int STATUS_PENDING_APPROVAL = 0;
    public static final int STATUS_APPROVED_UNPAID = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_PARTIAL = 3;
    public static final int STATUS_CANCELLED = 4;

    // Legacy support (to avoid Breaking Changes imediately if used elsewhere)
    public static final int STATUS_PENDING = STATUS_PENDING_APPROVAL;

    // Constructors
    public Payment() {
        this.paymentStatus = STATUS_PENDING;
        this.dueAmount = 0.0;
        this.paidAmount = 0.0;
    }

    public Payment(Integer bookingId, Integer userId, Double dueAmount) {
        this();
        this.bookingId = bookingId;
        this.userId = userId;
        this.dueAmount = dueAmount != null ? dueAmount : 0.0;
    }

    // Getters and Setters with validation
    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        if (bookingId != null && bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID must be positive");
        }
        this.bookingId = bookingId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Double getDueAmount() {
        return dueAmount;
    }

    public void setDueAmount(Double dueAmount) {
        if (dueAmount != null && dueAmount < 0) {
            throw new IllegalArgumentException("Due amount cannot be negative");
        }
        this.dueAmount = dueAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        if (paidAmount != null && paidAmount < 0) {
            throw new IllegalArgumentException("Paid amount cannot be negative");
        }
        this.paidAmount = paidAmount;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        if (paymentStatus != null &&
                paymentStatus != STATUS_PENDING_APPROVAL &&
                paymentStatus != STATUS_APPROVED_UNPAID &&
                paymentStatus != STATUS_PAID &&
                paymentStatus != STATUS_PARTIAL &&
                paymentStatus != STATUS_CANCELLED) {
            throw new IllegalArgumentException("Invalid payment status: " + paymentStatus);
        }
        this.paymentStatus = paymentStatus;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    // Optional display fields (not in database)
    public String getBookingRef() {
        return bookingRef;
    }

    public void setBookingRef(String bookingRef) {
        this.bookingRef = bookingRef;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Business logic methods
    public boolean isPaid() {
        return paymentStatus != null && paymentStatus == STATUS_PAID;
    }

    public boolean isPending() {
        return paymentStatus == null || paymentStatus == STATUS_PENDING;
    }

    public boolean isPartial() {
        return paymentStatus != null && paymentStatus == STATUS_PARTIAL;
    }

    // In your Payment.java model class, add:
    private Integer vehicleId; // Add this field

    // Add getter and setter:
    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getBalance() {
        if (dueAmount == null || paidAmount == null) {
            return 0.0;
        }
        return dueAmount - paidAmount;
    }

    public boolean hasBalance() {
        return getBalance() > 0;
    }

    public void markAsPaid(Double amount, String paidBy) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        this.paidAmount = (this.paidAmount != null ? this.paidAmount : 0) + amount;
        this.paidBy = paidBy;
        this.paymentDate = new Date();

        if (dueAmount != null && this.paidAmount >= dueAmount) {
            this.paymentStatus = STATUS_PAID;
        } else {
            this.paymentStatus = STATUS_PARTIAL;
        }
    }

    public String getStatusText() {
        if (paymentStatus == null) {
            return "UNKNOWN";
        }

        return switch (paymentStatus) {
            case STATUS_PENDING_APPROVAL -> "PENDING APPROVAL";
            case STATUS_APPROVED_UNPAID -> "APPROVED (UNPAID)";
            case STATUS_PAID -> "PAID";
            case STATUS_PARTIAL -> "PARTIAL";
            case STATUS_CANCELLED -> "CANCELLED";
            default -> "INVALID (" + paymentStatus + ")";
        };
    }

    // Utility methods
    @Override
    public String toString() {
        return String.format("Payment #%d - Booking: %d - Amount: $%.2f/$%.2f - %s",
                paymentId != null ? paymentId : 0,
                bookingId != null ? bookingId : 0,
                paidAmount != null ? paidAmount : 0.0,
                dueAmount != null ? dueAmount : 0.0,
                getStatusText());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    // Builder pattern
    public static class Builder {
        private final Payment payment = new Payment();

        public Builder bookingId(Integer bookingId) {
            payment.setBookingId(bookingId);
            return this;
        }

        public Builder userId(Integer userId) {
            payment.setUserId(userId);
            return this;
        }

        public Builder dueAmount(Double dueAmount) {
            payment.setDueAmount(dueAmount);
            return this;
        }

        public Builder paidAmount(Double paidAmount) {
            payment.setPaidAmount(paidAmount);
            return this;
        }

        public Builder status(Integer status) {
            payment.setPaymentStatus(status);
            return this;
        }

        public Builder paidBy(String paidBy) {
            payment.setPaidBy(paidBy);
            return this;
        }

        public Builder remarks(String remarks) {
            payment.setRemarks(remarks);
            return this;
        }

        public Payment build() {
            return payment;
        }

    }
}