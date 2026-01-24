package models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Booking {
    // Booking status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CANCELLED = 4;
    
    // Payment status constants
    public static final int PAYMENT_UNPAID = 0;
    public static final int PAYMENT_PAID = 1;
    public static final int PAYMENT_PARTIAL = 2;
    public static final int PAYMENT_REFUNDED = 3;
    
    // Duration constants
    public static final String DURATION_30MIN = "30 Minutes";
    public static final String DURATION_1H = "1 Hour";
    public static final String DURATION_2H = "2 Hours";
    public static final String DURATION_3H = "3 Hours";
    public static final String DURATION_4H = "4 Hours";
    public static final String DURATION_5H = "5 Hours";
    public static final String DURATION_6H = "6 Hours";
    public static final String DURATION_7H = "7 Hours";
    public static final String DURATION_8H = "8 Hours";
    public static final String DURATION_DAY = "1 Day";
    public static final String DURATION_2DAYS = "2 Days";
    public static final String DURATION_WEEK = "1 Week";
    
    // Rate constants (per hour)
    public static final double RATE_CAR = 5.00;
    public static final double RATE_MOTORCYCLE = 3.00;
    public static final double RATE_TRUCK = 8.00;
    public static final double RATE_VAN = 6.00;
    
    private Integer bookingId;
    private Integer customerId;
    private Integer vehicleId;
    private Integer slotId;
    private Integer userId;
    private Integer bookingStatus;
    private String durationOfBooking;
    private String remarks;
    private Timestamp bookingTime;
    private Timestamp expectedArrival;
    private Timestamp actualArrival;
    private Timestamp departureTime;
    private Double totalHours;
    private Double totalAmount;
    private Integer approvedBy;
    private String bookingRef;
// In Booking.java, add these fields:
private Timestamp actualEndTime;
private Timestamp createdAt;
private Timestamp updatedAt;

// Add getters and setters:
public Timestamp getActualEndTime() {
    return actualEndTime;
}

public void setActualEndTime(Timestamp actualEndTime) {
    this.actualEndTime = actualEndTime;
}

public Timestamp getCreatedAt() {
    return createdAt;
}

public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
}

public Timestamp getUpdatedAt() {
    return updatedAt;
}

public void setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
}
    // Relations (optional - for joined queries)
    private User customer;
    private Vehicle vehicle;
    private ParkingSlot parkingSlot;
    private Payment payment;

    // Default constructor
    public Booking() {
        this.bookingStatus = STATUS_PENDING;
        this.bookingTime = new Timestamp(System.currentTimeMillis());
        this.bookingRef = generateBookingRef();
    }

    // Constructor for creating new booking
    public Booking(Integer customerId, Integer vehicleId, Integer slotId, 
                   Integer userId, String duration, String remarks) {
        this();
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.userId = userId;
        this.durationOfBooking = duration;
        this.remarks = remarks;
    }

    // ================= GETTERS & SETTERS =================
    
    public Integer getBookingId() { 
        return bookingId; 
    }
    
    public void setBookingId(Integer bookingId) { 
        this.bookingId = bookingId; 
    }

    public Integer getCustomerId() { 
        return customerId; 
    }
    
    public void setCustomerId(Integer customerId) { 
        this.customerId = customerId; 
    }

    public Integer getVehicleId() { 
        return vehicleId; 
    }
    
    public void setVehicleId(Integer vehicleId) { 
        this.vehicleId = vehicleId; 
    }

    public Integer getSlotId() { 
        return slotId; 
    }
    
    public void setSlotId(Integer slotId) { 
        this.slotId = slotId; 
    }

    public Integer getUserId() { 
        return userId; 
    }
    
    public void setUserId(Integer userId) { 
        this.userId = userId; 
    }

    public Integer getBookingStatus() { 
        return bookingStatus; 
    }
    
    public void setBookingStatus(Integer bookingStatus) { 
        this.bookingStatus = bookingStatus; 
    }

    public String getDurationOfBooking() { 
        return durationOfBooking; 
    }
    
    public void setDurationOfBooking(String durationOfBooking) { 
        this.durationOfBooking = durationOfBooking; 
    }

    public String getRemarks() { 
        return remarks; 
    }
    
    public void setRemarks(String remarks) { 
        this.remarks = remarks; 
    }

    public Timestamp getBookingTime() { 
        return bookingTime; 
    }
    
    public void setBookingTime(Timestamp bookingTime) { 
        this.bookingTime = bookingTime; 
    }

    public Timestamp getExpectedArrival() { 
        return expectedArrival; 
    }
    
    public void setExpectedArrival(Timestamp expectedArrival) { 
        this.expectedArrival = expectedArrival; 
    }

    public Timestamp getActualArrival() { 
        return actualArrival; 
    }
    
    public void setActualArrival(Timestamp actualArrival) { 
        this.actualArrival = actualArrival; 
    }

    public Timestamp getDepartureTime() { 
        return departureTime; 
    }
    
    public void setDepartureTime(Timestamp departureTime) { 
        this.departureTime = departureTime; 
    }

    public Double getTotalHours() { 
        return totalHours; 
    }
    
    public void setTotalHours(Double totalHours) { 
        this.totalHours = totalHours; 
    }

    public Double getTotalAmount() { 
        return totalAmount; 
    }
    
    public void setTotalAmount(Double totalAmount) { 
        this.totalAmount = totalAmount; 
    }

    public Integer getApprovedBy() { 
        return approvedBy; 
    }
    
    public void setApprovedBy(Integer approvedBy) { 
        this.approvedBy = approvedBy; 
    }

    public String getBookingRef() { 
        return bookingRef; 
    }
    
    public void setBookingRef(String bookingRef) { 
        this.bookingRef = bookingRef; 
    }

    // ================= RELATIONSHIP GETTERS & SETTERS =================
    
    public User getCustomer() { 
        return customer; 
    }
    
    public void setCustomer(User customer) { 
        this.customer = customer; 
    }

    public Vehicle getVehicle() { 
        return vehicle; 
    }
    
    public void setVehicle(Vehicle vehicle) { 
        this.vehicle = vehicle; 
    }

    public ParkingSlot getParkingSlot() { 
        return parkingSlot; 
    }
    
    public void setParkingSlot(ParkingSlot parkingSlot) { 
        this.parkingSlot = parkingSlot; 
    }

    public Payment getPayment() { 
        return payment; 
    }
    
    public void setPayment(Payment payment) { 
        this.payment = payment; 
    }

    // ================= HELPER METHODS =================
    
    public String getStatusText() {
        return switch (this.bookingStatus) {
            case STATUS_PENDING -> "PENDING";
            case STATUS_APPROVED -> "APPROVED";
            case STATUS_REJECTED -> "REJECTED";
            case STATUS_COMPLETED -> "COMPLETED";
            case STATUS_CANCELLED -> "CANCELLED";
            default -> "UNKNOWN";
        };
    }
    
    public String getStatusText(int status) {
        return switch (status) {
            case STATUS_PENDING -> "PENDING";
            case STATUS_APPROVED -> "APPROVED";
            case STATUS_REJECTED -> "REJECTED";
            case STATUS_COMPLETED -> "COMPLETED";
            case STATUS_CANCELLED -> "CANCELLED";
            default -> "UNKNOWN";
        };
    }
    
    public boolean isPending() {
        return STATUS_PENDING == this.bookingStatus;
    }
    
    public boolean isApproved() {
        return STATUS_APPROVED == this.bookingStatus;
    }
    
    public boolean isRejected() {
        return STATUS_REJECTED == this.bookingStatus;
    }
    
    public boolean isCompleted() {
        return STATUS_COMPLETED == this.bookingStatus;
    }
    
    public boolean isCancelled() {
        return STATUS_CANCELLED == this.bookingStatus;
    }
    
    public boolean canBeApproved() {
        return isPending();
    }
    
    public boolean canBeRejected() {
        return isPending();
    }
    
    public boolean canBeCancelled() {
        return isPending() || isApproved();
    }
    
    public String getFormattedBookingTime() {
        if (bookingTime == null) return "N/A";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bookingTime);
    }
    
    public String getFormattedBookingDate() {
        if (bookingTime == null) return "N/A";
        return new SimpleDateFormat("EEE, MMM dd, yyyy").format(bookingTime);
    }
    
    public String getFormattedBookingTimeOnly() {
        if (bookingTime == null) return "N/A";
        return new SimpleDateFormat("HH:mm:ss").format(bookingTime);
    }
    
    public String getFormattedArrivalTime() {
        if (actualArrival == null) return "Not Arrived";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(actualArrival);
    }
    
    public String getFormattedDepartureTime() {
        if (departureTime == null) return "Not Departed";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(departureTime);
    }
    
    public String getFormattedAmount() {
        if (totalAmount == null) return "$0.00";
        return String.format("$%.2f", totalAmount);
    }
    
    // Calculate actual hours parked
    public double calculateActualHours() {
        if (actualArrival != null && departureTime != null) {
            long diffMs = departureTime.getTime() - actualArrival.getTime();
            return diffMs / (1000.0 * 60 * 60); // Convert ms to hours
        }
        return 0.0;
    }
    
    // Calculate estimated amount based on duration
    public double calculateEstimatedAmount(String vehicleType) {
        if (durationOfBooking == null) return 0.0;
        
        double rate = getRateForVehicleType(vehicleType);
        double hours = parseDurationToHours(durationOfBooking);
        
        return hours * rate;
    }
    
    private double getRateForVehicleType(String vehicleType) {
        if (vehicleType == null) return RATE_CAR;
        
        return switch (vehicleType.toLowerCase()) {
            case "motorcycle", "bike" -> RATE_MOTORCYCLE;
            case "truck", "lorry" -> RATE_TRUCK;
            case "van", "minivan" -> RATE_VAN;
            default -> RATE_CAR;
        };
    }
    
    private double parseDurationToHours(String duration) {
        if (duration == null) return 0.0;
        
        try {
            String lowerDuration = duration.toLowerCase();
            
            if (lowerDuration.contains("minute")) {
                String[] parts = duration.split(" ");
                double minutes = Double.parseDouble(parts[0]);
                return minutes / 60.0;
            } else if (lowerDuration.contains("hour")) {
                String[] parts = duration.split(" ");
                return Double.parseDouble(parts[0]);
            } else if (lowerDuration.contains("day")) {
                String[] parts = duration.split(" ");
                double days = Double.parseDouble(parts[0]);
                return days * 24;
            } else if (lowerDuration.contains("week")) {
                String[] parts = duration.split(" ");
                double weeks = Double.parseDouble(parts[0]);
                return weeks * 24 * 7;
            }
        } catch (Exception e) {
            // If parsing fails
        }
        
        return 0.0;
    }
    
    // Generate booking reference
    private String generateBookingRef() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int) (Math.random() * 1000);
        return "BK-" + timestamp + "-" + String.format("%03d", random);
    }
    
    // Check if booking is active (approved but not completed)
    public boolean isActive() {
        return isApproved() && actualArrival != null && departureTime == null;
    }
    
    // Check if booking is overdue (should have departed)
    public boolean isOverdue() {
        if (expectedArrival == null || durationOfBooking == null) {
            return false;
        }
        
        long expectedDurationMs = (long) (parseDurationToHours(durationOfBooking) * 60 * 60 * 1000);
        long expectedDepartureTime = expectedArrival.getTime() + expectedDurationMs;
        
        return System.currentTimeMillis() > expectedDepartureTime;
    }
    
    // Get duration in minutes for easy comparison
    public int getDurationInMinutes() {
        return (int) (parseDurationToHours(durationOfBooking) * 60);
    }
    
    @Override
    public String toString() {
        return String.format("Booking #%s [%s] - Slot: %s, Status: %s", 
            bookingRef != null ? bookingRef : bookingId, 
            getFormattedBookingTime(),
            slotId != null ? slotId : "N/A",
            getStatusText()
        );
    }
    
    // For UI display in comboboxes/tables
    public String getDisplayText() {
        return String.format("%s - Slot %s (%s)", 
            bookingRef != null ? bookingRef : "BK-" + bookingId,
            slotId != null ? slotId : "N/A",
            getStatusText()
        );
    }
    
    // Get all duration options for dropdown
    public static String[] getDurationOptions() {
        return new String[] {
            DURATION_30MIN,
            DURATION_1H,
            DURATION_2H,
            DURATION_3H,
            DURATION_4H,
            DURATION_5H,
            DURATION_6H,
            DURATION_7H,
            DURATION_8H,
            DURATION_DAY,
            DURATION_2DAYS,
            DURATION_WEEK
        };
    }
    
    // Get status options
    public static String[] getStatusOptions() {
        return new String[] {
            "PENDING",
            "APPROVED",
            "REJECTED",
            "COMPLETED",
            "CANCELLED"
        };
    }
    
    // Convert status text to status code
    public static int getStatusCode(String statusText) {
        return switch (statusText.toUpperCase()) {
            case "PENDING" -> STATUS_PENDING;
            case "APPROVED" -> STATUS_APPROVED;
            case "REJECTED" -> STATUS_REJECTED;
            case "COMPLETED" -> STATUS_COMPLETED;
            case "CANCELLED" -> STATUS_CANCELLED;
            default -> STATUS_PENDING;
        };
    }
}