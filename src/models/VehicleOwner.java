// src/models/VehicleOwner.java
package models;

import java.sql.Timestamp;

public class VehicleOwner {
    private Integer vehicleOwnerId;
    private String vehicleOwnerName;
    private byte[] avatar;
    private String vehicleOwnerContact;
    private String vehicleOwnerEmail;
    private String ownerUsername;
    private String ownerPassword;
    private Integer status;
    private Integer userId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    public VehicleOwner() {}
    
    public VehicleOwner(String vehicleOwnerName, String contact, String email) {
        this.vehicleOwnerName = vehicleOwnerName;
        this.vehicleOwnerContact = contact;
        this.vehicleOwnerEmail = email;
    }
    
    // Getters and Setters
    public Integer getVehicleOwnerId() { return vehicleOwnerId; }
    public void setVehicleOwnerId(Integer vehicleOwnerId) { this.vehicleOwnerId = vehicleOwnerId; }
    
    public String getVehicleOwnerName() { return vehicleOwnerName; }
    public void setVehicleOwnerName(String vehicleOwnerName) { this.vehicleOwnerName = vehicleOwnerName; }
    
    public byte[] getAvatar() { return avatar; }
    public void setAvatar(byte[] avatar) { this.avatar = avatar; }
    
    public String getVehicleOwnerContact() { return vehicleOwnerContact; }
    public void setVehicleOwnerContact(String vehicleOwnerContact) { this.vehicleOwnerContact = vehicleOwnerContact; }
    
    public String getVehicleOwnerEmail() { return vehicleOwnerEmail; }
    public void setVehicleOwnerEmail(String vehicleOwnerEmail) { this.vehicleOwnerEmail = vehicleOwnerEmail; }
    
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    
    public String getOwnerPassword() { return ownerPassword; }
    public void setOwnerPassword(String ownerPassword) { this.ownerPassword = ownerPassword; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}