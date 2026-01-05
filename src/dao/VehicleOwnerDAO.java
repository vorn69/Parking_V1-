// src/dao/VehicleOwnerDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.VehicleOwner;

public class VehicleOwnerDAO extends BaseDAO<VehicleOwner> {
    
    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_vehicle_owner";
    }
    
    @Override
    protected String getIdColumnName() {
        return "vehicle_owner_id";
    }
    
    @Override
    protected VehicleOwner mapResultSetToEntity(ResultSet rs) throws SQLException {
        VehicleOwner owner = new VehicleOwner();
        owner.setVehicleOwnerId(rs.getInt("vehicle_owner_id"));
        owner.setVehicleOwnerName(rs.getString("vehicle_owner_name"));
        owner.setAvatar(rs.getBytes("avatar"));
        owner.setVehicleOwnerContact(rs.getString("vehicle_owner_contact"));
        owner.setVehicleOwnerEmail(rs.getString("vehicle_owner_email"));
        owner.setOwnerUsername(rs.getString("owner_username"));
        owner.setOwnerPassword(rs.getString("owner_password"));
        owner.setStatus(rs.getInt("status"));
        owner.setUserId(rs.getInt("user_id"));
        
        // Handle nullable timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (!rs.wasNull()) {
            owner.setCreatedAt(createdAt);
            owner.setUpdatedAt(updatedAt);
        }
        
        return owner;
    }
    
    public Integer create(VehicleOwner owner) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (vehicle_owner_name, avatar, vehicle_owner_contact, " +
                    "vehicle_owner_email, owner_username, owner_password, status, user_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, owner.getVehicleOwnerName());
            
            if (owner.getAvatar() != null) {
                pstmt.setBytes(2, owner.getAvatar());
            } else {
                pstmt.setNull(2, Types.BINARY);
            }
            
            pstmt.setString(3, owner.getVehicleOwnerContact());
            pstmt.setString(4, owner.getVehicleOwnerEmail());
            pstmt.setString(5, owner.getOwnerUsername());
            pstmt.setString(6, owner.getOwnerPassword());
            pstmt.setInt(7, owner.getStatus() != null ? owner.getStatus() : 1);
            
            if (owner.getUserId() != null) {
                pstmt.setInt(8, owner.getUserId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
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
    
    public VehicleOwner findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_owner_id = ?";
        
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
    
    public VehicleOwner findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE owner_username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<VehicleOwner> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY vehicle_owner_name";
        List<VehicleOwner> owners = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                owners.add(mapResultSetToEntity(rs));
            }
            return owners;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<VehicleOwner> findByStatus(Integer status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE status = ? ORDER BY vehicle_owner_name";
        List<VehicleOwner> owners = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                owners.add(mapResultSetToEntity(rs));
            }
            return owners;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<VehicleOwner> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE vehicle_owner_name ILIKE ? ORDER BY vehicle_owner_name";
        List<VehicleOwner> owners = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                owners.add(mapResultSetToEntity(rs));
            }
            return owners;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(VehicleOwner owner) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "vehicle_owner_name = ?, avatar = ?, vehicle_owner_contact = ?, " +
                    "vehicle_owner_email = ?, owner_username = ?, owner_password = ?, " +
                    "status = ?, user_id = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE vehicle_owner_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, owner.getVehicleOwnerName());
            
            if (owner.getAvatar() != null) {
                pstmt.setBytes(2, owner.getAvatar());
            } else {
                pstmt.setNull(2, Types.BINARY);
            }
            
            pstmt.setString(3, owner.getVehicleOwnerContact());
            pstmt.setString(4, owner.getVehicleOwnerEmail());
            pstmt.setString(5, owner.getOwnerUsername());
            pstmt.setString(6, owner.getOwnerPassword());
            pstmt.setInt(7, owner.getStatus() != null ? owner.getStatus() : 1);
            
            if (owner.getUserId() != null) {
                pstmt.setInt(8, owner.getUserId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            pstmt.setInt(9, owner.getVehicleOwnerId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE vehicle_owner_id = ?";
        
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
    
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE owner_username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + 
                    " WHERE owner_username = ? AND owner_password = ? AND status = 1";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public int countOwners() throws SQLException {
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
    
    public int countActiveOwners() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE status = 1";
        
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
}