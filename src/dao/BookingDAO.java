package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Booking;
import models.ParkingSlot;

public class BookingDAO extends BaseDAO<Booking> {

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_booking";
    }

    @Override
    protected String getIdColumnName() {
        return "booking_id";
    }

    @Override
    protected Booking mapResultSetToEntity(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setCustomerId((Integer) rs.getObject("customer_id"));
        b.setVehicleId((Integer) rs.getObject("vehicle_id"));
        b.setSlotId((Integer) rs.getObject("slot_id"));
        b.setUserId((Integer) rs.getObject("user_id"));
        b.setBookingStatus(rs.getInt("booking_status"));
        b.setDurationOfBooking(rs.getString("duration_of_booking"));
        b.setRemarks(rs.getString("remarks"));
        b.setBookingTime(rs.getTimestamp("booking_time"));
        b.setExpectedArrival(rs.getTimestamp("expected_arrival"));
        b.setActualArrival(rs.getTimestamp("actual_arrival"));
        b.setDepartureTime(rs.getTimestamp("departure_time"));
        b.setTotalHours((Double) rs.getObject("total_hours"));
        b.setTotalAmount((Double) rs.getObject("total_amount"));
        return b;
    }

    public List<Booking> findAll() throws SQLException {
    List<Booking> list = new ArrayList<>();
    String sql = "SELECT * FROM inet_vehicleparking.tbl_booking ORDER BY booking_time DESC";

    try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(mapResultSetToEntity(rs));
        }
    }
    return list;
}

    // ================= CREATE =================
public int create(Booking b) throws SQLException {

    String sql = "INSERT INTO inet_vehicleparking.tbl_booking "
            + "(customer_id, vehicle_id, slot_id, booking_status, "
            + "duration_of_booking, remarks, booking_time) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, b.getCustomerId());
        ps.setInt(2, b.getVehicleId());
        ps.setInt(3, b.getSlotId());
        ps.setInt(4, Booking.STATUS_PENDING);
        ps.setString(5, b.getDurationOfBooking());
        ps.setString(6, b.getRemarks());
        ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getInt(1) : 0;
    }
}


    // ================= UPDATE =================
    public boolean update(Booking b) throws SQLException {

        String sql = "UPDATE " + getTableName() +
                    " SET booking_status=? WHERE booking_id=?";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, b.getBookingStatus());
            ps.setInt(2, b.getBookingId());

            boolean updated = ps.executeUpdate() > 0;

            // ✅ Free slot when checkout
            if (updated && b.getBookingStatus() == Booking.STATUS_CHECKED_OUT) {
                ParkingSlotDAO slotDAO = new ParkingSlotDAO();
                ParkingSlot slot = slotDAO.findById(b.getSlotId());
                if (slot != null) {
                    slot.setParkingSlotStatus(ParkingSlot.STATUS_AVAILABLE);
                    slotDAO.updateStatus(slot);
                }
            }

            return updated;
        }
    }

    public int countBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
