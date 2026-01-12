package models;

public class ParkingSlot {
    private Integer parkingSlotId;
    private Integer parkingSlotNumber;
    private Integer parkingSlotStatus;
    private Integer userId;
    private String slotType;
    private String zone;
    private User user; // Optional, for joined queries

    // Status constants
    public static final int STATUS_AVAILABLE = 0;
    public static final int STATUS_OCCUPIED = 1;
    public static final int STATUS_RESERVED = 2;
    public static final int STATUS_MAINTENANCE = 3;

    // Constructors
    public ParkingSlot() {}

    public ParkingSlot(Integer parkingSlotNumber) {
        this.parkingSlotNumber = parkingSlotNumber;
        this.parkingSlotStatus = STATUS_AVAILABLE;
    }

    // Getters & Setters
    public Integer getParkingSlotId() { return parkingSlotId; }
    public void setParkingSlotId(Integer parkingSlotId) { this.parkingSlotId = parkingSlotId; }

    public Integer getParkingSlotNumber() { return parkingSlotNumber; }
    public void setParkingSlotNumber(Integer parkingSlotNumber) { this.parkingSlotNumber = parkingSlotNumber; }

    public Integer getParkingSlotStatus() { return parkingSlotStatus; }
    public void setParkingSlotStatus(Integer parkingSlotStatus) { this.parkingSlotStatus = parkingSlotStatus; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    // Inside ParkingSlot.java
    public Integer getSlotNumber() {
        return parkingSlotNumber; // already done
    }

    // Helper methods
    public boolean isAvailable() { return parkingSlotStatus == STATUS_AVAILABLE; }

    public String getStatusText() {
        return switch (parkingSlotStatus) {
            case STATUS_AVAILABLE -> "Available";
            case STATUS_OCCUPIED -> "Occupied";
            case STATUS_RESERVED -> "Reserved";
            case STATUS_MAINTENANCE -> "Maintenance";
            default -> "Unknown";
        };
    }

    public void setStatusFromText(String text) {
        this.parkingSlotStatus = switch (text) {
            case "Available" -> STATUS_AVAILABLE;
            case "Occupied" -> STATUS_OCCUPIED;
            case "Reserved" -> STATUS_RESERVED;
            case "Maintenance" -> STATUS_MAINTENANCE;
            default -> STATUS_AVAILABLE;
        };
    }
}
