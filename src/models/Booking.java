package models;

import java.sql.Timestamp;

public class Booking {
        // Booking status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_CHECKED_IN = 2;
    public static final int STATUS_CHECKED_OUT = 3;
    public static final int STATUS_REJECTED = 4;


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

    // Relations (optional)
    private User customer;
    private Vehicle vehicle;
    private ParkingSlot parkingSlot;

    private String bookingRef; // For payment reference

    public Booking() {}

    // Getters & Setters
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public Integer getSlotId() { return slotId; }
    public void setSlotId(Integer slotId) { this.slotId = slotId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(Integer bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getDurationOfBooking() { return durationOfBooking; }
    public void setDurationOfBooking(String durationOfBooking) { this.durationOfBooking = durationOfBooking; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

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

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public ParkingSlot getParkingSlot() { return parkingSlot; }
    public void setParkingSlot(ParkingSlot parkingSlot) { this.parkingSlot = parkingSlot; }

    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
}
