// src/models/Booking.java
package models;

import java.sql.Timestamp;

public class Booking {
    private Integer bookingId;
    private Integer customerId;
    private Integer vehicleId;
    private String durationOfBooking;
    private Integer slotId;
    private Integer bookingStatus;
    private String remarks;
    private Integer userId;
    private Timestamp bookingTime;
    private Timestamp expectedArrival;
    private Timestamp actualArrival;
    private Timestamp departureTime;
    private Double totalHours;
    private Double totalAmount;
    
    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_CHECKED_IN = 2;
    public static final int STATUS_CHECKED_OUT = 3;
    public static final int STATUS_CANCELLED = 4;
    
    // Related objects (for joined queries)
    private VehicleOwner customer;
    private Vehicle vehicle;
    private ParkingSlot parkingSlot;
    private User user;
    
    // Constructors
    public Booking() {}
    
    public Booking(Integer customerId, Integer vehicleId, Integer slotId) {
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.bookingStatus = STATUS_PENDING;
        this.bookingTime = new Timestamp(System.currentTimeMillis());
    }
    
    // Getters and Setters
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    
    public String getDurationOfBooking() { return durationOfBooking; }
    public void setDurationOfBooking(String durationOfBooking) { this.durationOfBooking = durationOfBooking; }
    
    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }
    
    public Integer getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(Integer bookingStatus) { this.bookingStatus = bookingStatus; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public Timestamp getBookingTime() { return bookingTime; }
    public void setBookingTime(Timestamp bookingTime) { this.bookingTime = bookingTime; }
    
    public Timestamp getExpectedArrival() { return expectedArrival; }
    public void setExpectedArrival(Timestamp expectedArrival) { this.expectedArrival = expectedArrival; }
    
    public Timestamp getActualArrival() { return actualArrival; }
    public void setActualArrival(Timestamp actualArrival) { this.actualArrival = actualArrival; }
    
    public Timestamp getDepartureTime() { return departureTime; }
    public void setDepartureTime(Timestamp departureTime) { this.departureTime = departureTime; }
    
    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    // Related object getters/setters
    public VehicleOwner getCustomer() { return customer; }
    public void setCustomer(VehicleOwner customer) { this.customer = customer; }
    
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    
    public ParkingSlot getParkingSlot() { return parkingSlot; }
    public void setParkingSlot(ParkingSlot parkingSlot) { this.parkingSlot = parkingSlot; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    // Helper methods
    public String getStatusText() {
        switch (bookingStatus) {
            case STATUS_PENDING: return "Pending";
            case STATUS_CONFIRMED: return "Confirmed";
            case STATUS_CHECKED_IN: return "Checked In";
            case STATUS_CHECKED_OUT: return "Checked Out";
            case STATUS_CANCELLED: return "Cancelled";
            default: return "Unknown";
        }
    }
    
    public boolean isActive() {
        return bookingStatus == STATUS_CONFIRMED || bookingStatus == STATUS_CHECKED_IN;
    }
}