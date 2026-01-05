// src/dao/VehicleDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Vehicle;
import models.VehicleCategory;
import models.VehicleOwner;

public class VehicleDAO extends BaseDAO<Vehicle> {
    
    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_vehicle";
    }
    
    @Override
    protected String getIdColumnName() {
        return "vehicle_id";
    }
    
    @Override
    protected Vehicle mapResultSetToEntity(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(rs.getInt("vehicle_id"));
        vehicle.setVehicleCategoryId(rs.getInt("vehicle_category_id"));
        vehicle.setVehiclePlateNumber(rs.getString("vehicle_plate_number"));
        vehicle.setVehicleDescription(rs.getString("vehicle_description"));
        vehicle.setVehicleImage(rs.getBytes("vehicle_image"));
        vehicle.setVehicleOwnerId(rs.getInt("vehicle_owner_id"));
        return vehicle;
    }
    
    public Integer create(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (vehicle_category_id, vehicle_plate_number, vehicle_description, " +
                    "vehicle_image, vehicle_owner_id) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, vehicle.getVehicleCategoryId());
            pstmt.setString(2, vehicle.getVehiclePlateNumber());
            pstmt.setString(3, vehicle.getVehicleDescription());
            
            if (vehicle.getVehicleImage() != null) {
                pstmt.setBytes(4, vehicle.getVehicleImage());
            } else {
                pstmt.setNull(4, Types.BINARY);
            }
            
            pstmt.setInt(5, vehicle.getVehicleOwnerId());
            
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Vehicle findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Vehicle findByPlateNumber(String plateNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_plate_number = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Vehicle findByIdWithDetails(Integer id) throws SQLException {
        String sql = "SELECT v.*, vc.vehicle_category_name, vo.vehicle_owner_name " +
                    "FROM " + getTableName() + " v " +
                    "JOIN inet_vehicleparking.tbl_vehicle_category vc ON v.vehicle_category_id = vc.vehicle_category_id " +
                    "JOIN inet_vehicleparking.tbl_vehicle_owner vo ON v.vehicle_owner_id = vo.vehicle_owner_id " +
                    "WHERE v.vehicle_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Vehicle vehicle = mapResultSetToEntity(rs);
                
                // Map category
                VehicleCategory category = new VehicleCategory();
                category.setVehicleCategoryId(rs.getInt("vehicle_category_id"));
                category.setVehicleCategoryName(rs.getString("vehicle_category_name"));
                vehicle.setVehicleCategory(category);
                
                // Map owner
                VehicleOwner owner = new VehicleOwner();
                owner.setVehicleOwnerId(rs.getInt("vehicle_owner_id"));
                owner.setVehicleOwnerName(rs.getString("vehicle_owner_name"));
                vehicle.setVehicleOwner(owner);
                
                return vehicle;
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY vehicle_plate_number";
        List<Vehicle> vehicles = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vehicles.add(mapResultSetToEntity(rs));
            }
            return vehicles;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Vehicle> findByOwnerId(Integer ownerId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE vehicle_owner_id = ? ORDER BY vehicle_plate_number";
        List<Vehicle> vehicles = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ownerId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vehicles.add(mapResultSetToEntity(rs));
            }
            return vehicles;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Vehicle> findByCategoryId(Integer categoryId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE vehicle_category_id = ? ORDER BY vehicle_plate_number";
        List<Vehicle> vehicles = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vehicles.add(mapResultSetToEntity(rs));
            }
            return vehicles;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Vehicle> searchByPlateNumber(String plateNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE vehicle_plate_number ILIKE ? ORDER BY vehicle_plate_number";
        List<Vehicle> vehicles = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + plateNumber + "%");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vehicles.add(mapResultSetToEntity(rs));
            }
            return vehicles;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "vehicle_category_id = ?, vehicle_plate_number = ?, vehicle_description = ?, " +
                    "vehicle_image = ?, vehicle_owner_id = ? WHERE vehicle_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vehicle.getVehicleCategoryId());
            pstmt.setString(2, vehicle.getVehiclePlateNumber());
            pstmt.setString(3, vehicle.getVehicleDescription());
            
            if (vehicle.getVehicleImage() != null) {
                pstmt.setBytes(4, vehicle.getVehicleImage());
            } else {
                pstmt.setNull(4, Types.BINARY);
            }
            
            pstmt.setInt(5, vehicle.getVehicleOwnerId());
            pstmt.setInt(6, vehicle.getVehicleId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE vehicle_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean existsByPlateNumber(String plateNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE vehicle_plate_number = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public int countVehicles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public int countVehiclesByOwner(Integer ownerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE vehicle_owner_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ownerId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean isVehicleAvailableForBooking(Integer vehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_booking " +
                    "WHERE vehicle_id = ? AND booking_status IN (1, 2)"; // Confirmed or Checked In
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vehicleId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
}