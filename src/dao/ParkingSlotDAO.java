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
        slot.setUserId((Integer) rs.getObject("user_id"));
        slot.setSlotType(rs.getString("slot_type"));
        slot.setZone(rs.getString("zone"));
        return slot;
    }


    // ================= READ =================
public List<ParkingSlot> findAll() throws SQLException {
    List<ParkingSlot> list = new ArrayList<>();
    String sql = "SELECT * FROM inet_vehicleparking.tbl_parking_slot ORDER BY parking_slot_number";

    try (Connection c = getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            ParkingSlot s = new ParkingSlot();
            s.setParkingSlotId(rs.getInt("parking_slot_id"));
            s.setParkingSlotNumber(rs.getInt("parking_slot_number"));
            s.setParkingSlotStatus(rs.getInt("parking_slot_status"));
            s.setUserId((Integer) rs.getObject("user_id"));
            list.add(s);
        }
    }
    return list;
}


    // ================= CREATE =================
    public void create(ParkingSlot slot) throws SQLException {
        String sql = """
            INSERT INTO inet_vehicleparking.tbl_parking_slot
            (parking_slot_number, parking_slot_status)
            VALUES (?, ?)
        """;

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slot.getParkingSlotNumber());
            ps.setInt(2, slot.getParkingSlotStatus());
            ps.executeUpdate();
        }
    }

    // ================= COUNT =================
    public int countSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_parking_slot";
        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // update slot status
    public boolean updateStatus(int slotId, int status, Integer userId) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_parking_slot
            SET parking_slot_status=?, user_id=?
            WHERE parking_slot_id=?
        """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setObject(2, userId, Types.INTEGER);
            ps.setInt(3, slotId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ParkingSlot> findAvailableSlots() throws SQLException {
        List<ParkingSlot> list = new ArrayList<>();

        String sql = """
            SELECT * FROM inet_vehicleparking.tbl_parking_slot
            WHERE parking_slot_status = ?
            ORDER BY parking_slot_number
        """;

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ParkingSlot.STATUS_AVAILABLE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }

        public void reserveSlot(int slotId, int userId) throws SQLException {

        String sql = """
            UPDATE inet_vehicleparking.tbl_parking_slot
            SET parking_slot_status = ?, user_id = ?
            WHERE parking_slot_id = ?
        """;

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ParkingSlot.STATUS_RESERVED);
            ps.setInt(2, userId);
            ps.setInt(3, slotId);

            ps.executeUpdate();
        }
    }

    // public boolean reserveSlot(int slotId, int userId) throws SQLException {
    // String sql = """
    //     UPDATE inet_vehicleparking.tbl_parking_slot
    //     SET parking_slot_status=?, user_id=?
    //     WHERE parking_slot_id=? AND parking_slot_status=?
    // """;

    // try (Connection conn = getConnection();
    //      PreparedStatement ps = conn.prepareStatement(sql)) {

    //     ps.setInt(1, ParkingSlot.STATUS_RESERVED);
    //     ps.setInt(2, userId);
    //     ps.setInt(3, slotId);
    //     ps.setInt(4, ParkingSlot.STATUS_AVAILABLE);

    //     ps.executeUpdate() > 0;
    // }
}




