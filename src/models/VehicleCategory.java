// src/models/VehicleCategory.java
package models;

public class VehicleCategory {
    private Integer vehicleCategoryId;
    private String vehicleCategoryName;
    private Integer userId;
    
    // Constructors
    public VehicleCategory() {}
    
    public VehicleCategory(String vehicleCategoryName) {
        this.vehicleCategoryName = vehicleCategoryName;
    }
    
    // Getters and Setters
    public Integer getVehicleCategoryId() { return vehicleCategoryId; }
    public void setVehicleCategoryId(Integer vehicleCategoryId) { this.vehicleCategoryId = vehicleCategoryId; }
    
    public String getVehicleCategoryName() { return vehicleCategoryName; }
    public void setVehicleCategoryName(String vehicleCategoryName) { this.vehicleCategoryName = vehicleCategoryName; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}