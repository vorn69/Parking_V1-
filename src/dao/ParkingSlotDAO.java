// src/dao/ParkingSlotDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.ParkingSlot;

public class ParkingSlotDAO extends BaseDAO<ParkingSlot> {
    
    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_parking_slot";
    }
    
    @Override
    protected String getIdColumnName() {
        return "parking_slot_id";
    }
    
    @Override
    protected ParkingSlot mapResultSetToEntity(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setParkingSlotId(rs.getInt("parking_slot_id"));
        slot.setParkingSlotNumber(rs.getInt("parking_slot_number"));
        slot.setParkingSlotStatus(rs.getInt("parking_slot_status"));
        slot.setUserId(rs.getInt("user_id"));
        slot.setSlotType(rs.getString("slot_type"));
        slot.setZone(rs.getString("zone"));
        return slot;
    }
    
    public Integer create(ParkingSlot slot) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (parking_slot_number, parking_slot_status, user_id, slot_type, zone) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, slot.getParkingSlotNumber());
            pstmt.setInt(2, slot.getParkingSlotStatus() != null ? slot.getParkingSlotStatus() : 0);
            
            if (slot.getUserId() != null) {
                pstmt.setInt(3, slot.getUserId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, slot.getSlotType());
            pstmt.setString(5, slot.getZone());
            
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
    
    public ParkingSlot findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_id = ?";
        
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
    
    public ParkingSlot findBySlotNumber(Integer slotNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_number = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotNumber);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<ParkingSlot> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY parking_slot_number";
        List<ParkingSlot> slots = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(mapResultSetToEntity(rs));
            }
            return slots;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<ParkingSlot> findByStatus(Integer status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_status = ? ORDER BY parking_slot_number";
        List<ParkingSlot> slots = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(mapResultSetToEntity(rs));
            }
            return slots;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<ParkingSlot> findAvailableSlots() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE parking_slot_status = 0 ORDER BY parking_slot_number";
        List<ParkingSlot> slots = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(mapResultSetToEntity(rs));
            }
            return slots;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<ParkingSlot> findBySlotType(String slotType) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE slot_type = ? ORDER BY parking_slot_number";
        List<ParkingSlot> slots = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, slotType);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(mapResultSetToEntity(rs));
            }
            return slots;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<ParkingSlot> findByZone(String zone) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE zone = ? ORDER BY parking_slot_number";
        List<ParkingSlot> slots = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, zone);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(mapResultSetToEntity(rs));
            }
            return slots;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(ParkingSlot slot) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "parking_slot_number = ?, parking_slot_status = ?, user_id = ?, " +
                    "slot_type = ?, zone = ? WHERE parking_slot_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slot.getParkingSlotNumber());
            pstmt.setInt(2, slot.getParkingSlotStatus() != null ? slot.getParkingSlotStatus() : 0);
            
            if (slot.getUserId() != null) {
                pstmt.setInt(3, slot.getUserId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, slot.getSlotType());
            pstmt.setString(5, slot.getZone());
            pstmt.setInt(6, slot.getParkingSlotId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean updateStatus(Integer slotId, Integer status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET parking_slot_status = ? WHERE parking_slot_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, slotId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE parking_slot_id = ?";
        
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
    
    public boolean existsBySlotNumber(Integer slotNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE parking_slot_number = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotNumber);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public int countSlots() throws SQLException {
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
    
    public int countAvailableSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE parking_slot_status = 0";
        
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
    
    public int countOccupiedSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE parking_slot_status = 1";
        
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
    
    public boolean isSlotAvailable(Integer slotId) throws SQLException {
        String sql = "SELECT parking_slot_status FROM " + getTableName() + " WHERE parking_slot_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("parking_slot_status") == ParkingSlot.STATUS_AVAILABLE;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean isSlotOccupied(Integer slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_booking " +
                    "WHERE slot_id = ? AND booking_status IN (1, 2)"; // Confirmed or Checked In
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
}