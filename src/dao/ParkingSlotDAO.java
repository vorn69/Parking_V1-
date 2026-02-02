package dao;

import java.sql.*;
import java.util.*;
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
        
        // Handle null user_id safely
        Object userIdObj = rs.getObject("user_id");
        if (userIdObj != null) {
            slot.setUserId(((Number) userIdObj).intValue());
        } else {
            slot.setUserId(null);
        }
        
        slot.setSlotType(rs.getString("slot_type"));
        slot.setZone(rs.getString("zone"));
        return slot;
    }

    // ================= CRUD METHODS =================
    
    @Override
    public List<ParkingSlot> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY parking_slot_number";
        List<ParkingSlot> list = new ArrayList<>();

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    // CREATE method
    public Integer create(ParkingSlot slot) throws SQLException {
        String sql = """
            INSERT INTO inet_vehicleparking.tbl_parking_slot
            (parking_slot_number, parking_slot_status, user_id, slot_type, zone)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, slot.getParkingSlotNumber());
            pstmt.setInt(2, slot.getParkingSlotStatus());
            
            if (slot.getUserId() != null) {
                pstmt.setInt(3, slot.getUserId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, slot.getSlotType());
            pstmt.setString(5, slot.getZone());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    // UPDATE method
    public boolean update(ParkingSlot slot) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_parking_slot
            SET parking_slot_number = ?, parking_slot_status = ?, 
                user_id = ?, slot_type = ?, zone = ?
            WHERE parking_slot_id = ?
        """;

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slot.getParkingSlotNumber());
            pstmt.setInt(2, slot.getParkingSlotStatus());
            
            if (slot.getUserId() != null) {
                pstmt.setInt(3, slot.getUserId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, slot.getSlotType());
            pstmt.setString(5, slot.getZone());
            pstmt.setInt(6, slot.getParkingSlotId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE method (inherited from BaseDAO works fine)

    // ================= CUSTOM METHODS =================
    public int countSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean updateStatus(int slotId, int status, Integer userId) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_parking_slot
            SET parking_slot_status = ?, user_id = ?
            WHERE parking_slot_id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, status);
            
            if (userId != null) {
                pstmt.setInt(2, userId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            pstmt.setInt(3, slotId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<ParkingSlot> findAvailableSlots() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE parking_slot_status = ? ORDER BY parking_slot_number";
        
        List<ParkingSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ParkingSlot.STATUS_AVAILABLE);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }

    public boolean reserveSlot(int slotId, int userId) throws SQLException {
        String sql = "UPDATE " + getTableName() + 
                    " SET parking_slot_status = ?, user_id = ? " +
                    "WHERE parking_slot_id = ? AND parking_slot_status = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ParkingSlot.STATUS_RESERVED);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, slotId);
            pstmt.setInt(4, ParkingSlot.STATUS_AVAILABLE);

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean releaseSlot(int slotId) throws SQLException {
        String sql = "UPDATE " + getTableName() + 
                    " SET parking_slot_status = ?, user_id = NULL " +
                    "WHERE parking_slot_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ParkingSlot.STATUS_AVAILABLE);
            pstmt.setInt(2, slotId);

            return pstmt.executeUpdate() > 0;
        }
    }

    public ParkingSlot findBySlotNumber(int slotNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_number = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public int countByStatus(int status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE parking_slot_status = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    
    // Find by user ID
    public List<ParkingSlot> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ? ORDER BY parking_slot_number";
        List<ParkingSlot> list = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }
}