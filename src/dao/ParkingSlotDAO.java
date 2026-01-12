package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Booking;
import models.ParkingSlot;
import models.Payment;

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

    // ================= CRUD =================
    public List<ParkingSlot> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY parking_slot_number ASC";
        List<ParkingSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToEntity(rs));
        }
        return list;
    }

    public ParkingSlot findById(int id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE parking_slot_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public boolean updateStatus(ParkingSlot slot) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET parking_slot_status=?, user_id=? WHERE parking_slot_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot.getParkingSlotStatus());
            if (slot.getUserId() != null)
                ps.setInt(2, slot.getUserId());
            else
                ps.setNull(2, Types.INTEGER);
            ps.setInt(3, slot.getParkingSlotId());
            return ps.executeUpdate() > 0;
        }
    }

public void create(Payment payment) throws SQLException {

    String sql = "INSERT INTO inet_vehicleparking.tbl_payment "
               + "(booking_id, amount_due, amount_paid, payment_status, paid_by, user_id, remarks) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, payment.getBookingId());
        ps.setDouble(2, payment.getDueAmount());
        ps.setDouble(3, payment.getPaidAmount());
        ps.setInt(4, payment.getPaymentStatus());
        ps.setString(5, payment.getPaidBy());
        ps.setObject(6, payment.getUserId(), Types.INTEGER);
        ps.setString(7, payment.getRemarks());

        ps.executeUpdate();
    }
}

public int createBookingWithSlotUpdate(Booking booking) throws SQLException {
    int bookingId = 0;
    Connection conn = getConnection();
    try {
        conn.setAutoCommit(false);

        // 1️⃣ Insert booking
        String sqlBooking = "INSERT INTO inet_vehicleparking.tbl_booking "
                + "(customer_id, vehicle_id, slot_id, booking_status, duration_of_booking, remarks, booking_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getVehicleId());
            ps.setInt(3, booking.getSlotId());
            ps.setInt(4, Booking.STATUS_PENDING);
            ps.setString(5, booking.getDurationOfBooking());
            ps.setString(6, booking.getRemarks());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            bookingId = rs.next() ? rs.getInt(1) : 0;
        }

        // 2️⃣ Update slot to RESERVED
        String sqlSlot = "UPDATE inet_vehicleparking.tbl_parking_slot SET parking_slot_status=?, user_id=? WHERE parking_slot_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlSlot)) {
            ps.setInt(1, ParkingSlot.STATUS_RESERVED);
            ps.setInt(2, booking.getCustomerId());
            ps.setInt(3, booking.getSlotId());
            ps.executeUpdate();
        }

        conn.commit();
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
        conn.close();
    }
    return bookingId;
}


public void create(ParkingSlot slot) throws SQLException {
    String sql = "INSERT INTO inet_vehicleparking.tbl_parking_slot "
               + "(parking_slot_number, parking_slot_status) VALUES (?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, slot.getParkingSlotNumber());
        ps.setInt(2, slot.getParkingSlotStatus());
        ps.executeUpdate();
    }
}

    // Inside ParkingSlotDAO
public int countSlots() throws SQLException {
    String sql = "SELECT COUNT(*) FROM " + getTableName();
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
    }
}

}
