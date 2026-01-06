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
        slot.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        slot.setSlotType(rs.getString("slot_type"));
        slot.setZone(rs.getString("zone"));
        return slot;
    }

    /* ================= CREATE ================= */

    public Integer create(ParkingSlot slot) throws SQLException {
        String sql = "INSERT INTO " + getTableName() +
                " (parking_slot_number, parking_slot_status, user_id, slot_type, zone) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, slot.getParkingSlotNumber());
            pstmt.setInt(2, slot.getParkingSlotStatus() != null
                    ? slot.getParkingSlotStatus()
                    : ParkingSlot.STATUS_AVAILABLE);

            if (slot.getUserId() != null) {
                pstmt.setInt(3, slot.getUserId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setString(4, slot.getSlotType());
            pstmt.setString(5, slot.getZone());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /* ================= READ ================= */

    public ParkingSlot findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public ParkingSlot findBySlotNumber(Integer slotNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public List<ParkingSlot> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY parking_slot_number";
        List<ParkingSlot> slots = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                slots.add(mapResultSetToEntity(rs));
            }
        }
        return slots;
    }

    public List<ParkingSlot> findAvailableSlots() throws SQLException {
        return findByStatus(ParkingSlot.STATUS_AVAILABLE);
    }

    public List<ParkingSlot> findByStatus(Integer status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() +
                " WHERE parking_slot_status = ? ORDER BY parking_slot_number";

        List<ParkingSlot> slots = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(mapResultSetToEntity(rs));
                }
            }
        }
        return slots;
    }

    /* ================= UPDATE ================= */

    public boolean update(ParkingSlot slot) throws SQLException {
        String sql = "UPDATE " + getTableName() +
                " SET parking_slot_number=?, parking_slot_status=?, user_id=?, slot_type=?, zone=? " +
                "WHERE parking_slot_id=?";

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

    public boolean updateStatus(Integer slotId, Integer status) throws SQLException {
        String sql = "UPDATE " + getTableName() +
                " SET parking_slot_status=? WHERE parking_slot_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, slotId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /* ================= DELETE ================= */

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE parking_slot_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /* ================= COUNT ================= */

    public int countSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countAvailableSlots() throws SQLException {
        return countByStatus(ParkingSlot.STATUS_AVAILABLE);
    }

    public int countOccupiedSlots() throws SQLException {
        return countByStatus(ParkingSlot.STATUS_OCCUPIED);
    }

    private int countByStatus(int status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE parking_slot_status=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ================= VALIDATION ================= */

    public boolean existsBySlotNumber(Integer slotNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() +
                " WHERE parking_slot_number=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, slotNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
