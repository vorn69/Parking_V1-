// src/models/Vehicle.java
package models;

public class Vehicle {
    private Integer vehicleId;
    private Integer vehicleCategoryId;
    private String vehiclePlateNumber;
    private String vehicleDescription;
    private byte[] vehicleImage;
    private Integer vehicleOwnerId;
    private VehicleCategory vehicleCategory; // For joined queries
    private VehicleOwner vehicleOwner; // For joined queries
    
    // Constructors
    public Vehicle() {}
    
    public Vehicle(String vehiclePlateNumber, Integer vehicleCategoryId, Integer vehicleOwnerId) {
        this.vehiclePlateNumber = vehiclePlateNumber;
        this.vehicleCategoryId = vehicleCategoryId;
        this.vehicleOwnerId = vehicleOwnerId;
    }
    
    // Getters and Setters
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    
    public Integer getVehicleCategoryId() { return vehicleCategoryId; }
    public void setVehicleCategoryId(Integer vehicleCategoryId) { this.vehicleCategoryId = vehicleCategoryId; }
    
    public String getVehiclePlateNumber() { return vehiclePlateNumber; }
    public void setVehiclePlateNumber(String vehiclePlateNumber) { this.vehiclePlateNumber = vehiclePlateNumber; }
    
    public String getVehicleDescription() { return vehicleDescription; }
    public void setVehicleDescription(String vehicleDescription) { this.vehicleDescription = vehicleDescription; }
    
    public byte[] getVehicleImage() { return vehicleImage; }
    public void setVehicleImage(byte[] vehicleImage) { this.vehicleImage = vehicleImage; }
    
    public Integer getVehicleOwnerId() { return vehicleOwnerId; }
    public void setVehicleOwnerId(Integer vehicleOwnerId) { this.vehicleOwnerId = vehicleOwnerId; }
    
    public VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(VehicleCategory vehicleCategory) { this.vehicleCategory = vehicleCategory; }
    
    public VehicleOwner getVehicleOwner() { return vehicleOwner; }
    public void setVehicleOwner(VehicleOwner vehicleOwner) { this.vehicleOwner = vehicleOwner; }
}