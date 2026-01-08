    package models;

    public class ParkingSlot {
        private Integer parkingSlotId;
        private Integer parkingSlotNumber;
        private Integer parkingSlotStatus;
        private Integer userId;
        private String slotType;
        private String zone;
        private User user; // For joined queries

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

        // Getters and Setters
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

        // Helper methods for UI
        public boolean isAvailable() {
            return parkingSlotStatus == STATUS_AVAILABLE;
        }

        public String getStatusText() {
            switch (parkingSlotStatus) {
                case STATUS_AVAILABLE: return "Available";
                case STATUS_OCCUPIED: return "Occupied";
                case STATUS_RESERVED: return "Reserved";
                case STATUS_MAINTENANCE: return "Maintenance";
                default: return "Unknown";
            }
        }

        public Integer getSlotNumber() {
            return parkingSlotNumber;
        }
    }
