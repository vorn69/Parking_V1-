package models;

import java.util.Objects;

public class ParkingSlot {
    private Integer parkingSlotId;
    private Integer parkingSlotNumber;
    private Integer parkingSlotStatus;
    private Integer userId;
    private String slotType;
    private String zone;
    private User user;

    // Status constants
    public static final int STATUS_AVAILABLE = 0;
    public static final int STATUS_RESERVED = 1;
    public static final int STATUS_OCCUPIED = 2;

    // Constructors
    public ParkingSlot() {
        this.parkingSlotStatus = STATUS_AVAILABLE;
    }

    public ParkingSlot(Integer parkingSlotNumber) {
        this();
        this.parkingSlotNumber = parkingSlotNumber;
    }

    // Copy constructor
    public ParkingSlot(ParkingSlot other) {
        this.parkingSlotId = other.parkingSlotId;
        this.parkingSlotNumber = other.parkingSlotNumber;
        this.parkingSlotStatus = other.parkingSlotStatus;
        this.userId = other.userId;
        this.slotType = other.slotType;
        this.zone = other.zone;
        this.user = other.user;
    }

    // Getters
    public Integer getParkingSlotId() {
        return parkingSlotId;
    }

    public Integer getParkingSlotNumber() {
        return parkingSlotNumber;
    }

    public Integer getParkingSlotStatus() {
        return parkingSlotStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getSlotType() {
        return slotType;
    }

    public String getZone() {
        return zone;
    }

    public User getUser() {
        return user;
    }

    // Setters with validation
    public void setParkingSlotId(Integer parkingSlotId) {
        this.parkingSlotId = parkingSlotId;
    }

    public void setParkingSlotNumber(Integer parkingSlotNumber) {
        if (parkingSlotNumber != null && parkingSlotNumber <= 0) {
            throw new IllegalArgumentException("Slot number must be positive");
        }
        this.parkingSlotNumber = parkingSlotNumber;
    }

    public void setParkingSlotStatus(Integer parkingSlotStatus) {
        if (parkingSlotStatus != null && 
            parkingSlotStatus != STATUS_AVAILABLE &&
            parkingSlotStatus != STATUS_RESERVED &&
            parkingSlotStatus != STATUS_OCCUPIED) {
            throw new IllegalArgumentException("Invalid status code: " + parkingSlotStatus);
        }
        this.parkingSlotStatus = parkingSlotStatus;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Business logic methods
    public boolean isAvailable() {
        return parkingSlotStatus == null || parkingSlotStatus == STATUS_AVAILABLE;
    }

    public boolean isReserved() {
        return parkingSlotStatus != null && parkingSlotStatus == STATUS_RESERVED;
    }

    public boolean isOccupied() {
        return parkingSlotStatus != null && parkingSlotStatus == STATUS_OCCUPIED;
    }

    public String getStatusText() {
        if (parkingSlotStatus == null) {
            return "UNKNOWN";
        }
        return switch (parkingSlotStatus) {
            case STATUS_AVAILABLE -> "AVAILABLE";
            case STATUS_RESERVED -> "RESERVED";
            case STATUS_OCCUPIED -> "OCCUPIED";
            default -> "INVALID (" + parkingSlotStatus + ")";
        };
    }

    // Utility methods
    public void markAsAvailable() {
        this.parkingSlotStatus = STATUS_AVAILABLE;
        this.userId = null;
        this.user = null;
    }

    public void markAsReserved(Integer userId) {
        this.parkingSlotStatus = STATUS_RESERVED;
        this.userId = userId;
    }

    public void markAsOccupied(Integer userId) {
        this.parkingSlotStatus = STATUS_OCCUPIED;
        this.userId = userId;
    }

    // Override methods
    @Override
    public String toString() {
        return String.format("Slot #%d (%s) - %s", 
            parkingSlotNumber != null ? parkingSlotNumber : 0,
            zone != null ? zone : "No Zone",
            getStatusText());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSlot that = (ParkingSlot) o;
        return Objects.equals(parkingSlotId, that.parkingSlotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parkingSlotId);
    }

    // Builder pattern (optional but useful)
    public static class Builder {
        private final ParkingSlot slot = new ParkingSlot();

        public Builder parkingSlotNumber(Integer number) {
            slot.setParkingSlotNumber(number);
            return this;
        }

        public Builder zone(String zone) {
            slot.setZone(zone);
            return this;
        }

        public Builder slotType(String type) {
            slot.setSlotType(type);
            return this;
        }

        public Builder status(Integer status) {
            slot.setParkingSlotStatus(status);
            return this;
        }

        public Builder userId(Integer userId) {
            slot.setUserId(userId);
            return this;
        }

        public ParkingSlot build() {
            return new ParkingSlot(slot);
        }
    }
}