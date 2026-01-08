package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Booking;

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
        b.setVehicleId(rs.getInt("vehicle_id"));
        b.setDurationOfBooking(rs.getString("duration_of_booking"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setBookingStatus(rs.getInt("booking_status"));
        b.setRemarks(rs.getString("remarks"));
        b.setUserId((Integer) rs.getObject("user_id"));

        // FIXED: Use Timestamp directly
        b.setBookingTime(rs.getTimestamp("booking_time"));
        b.setExpectedArrival(rs.getTimestamp("expected_arrival"));
        b.setActualArrival(rs.getTimestamp("actual_arrival"));
        b.setDepartureTime(rs.getTimestamp("departure_time"));

        b.setTotalHours((Double) rs.getObject("total_hours"));
        b.setTotalAmount((Double) rs.getObject("total_amount"));

        b.setBookingRef(rs.getString("booking_ref"));

        return b;
    }

    /* ================= CREATE ================= */
    public Integer create(Booking b) throws SQLException {
        String sql = """
            INSERT INTO inet_vehicleparking.tbl_booking
            (customer_id, vehicle_id, duration_of_booking, slot_id,
            booking_status, remarks, user_id, booking_time,
            expected_arrival, actual_arrival, departure_time,
            total_hours, total_amount, booking_ref)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, b.getCustomerId(), Types.INTEGER);
            ps.setInt(2, b.getVehicleId());
            ps.setString(3, b.getDurationOfBooking());
            ps.setInt(4, b.getSlotId());
            ps.setInt(5, b.getBookingStatus() != null ? b.getBookingStatus() : Booking.STATUS_PENDING);
            ps.setString(6, b.getRemarks());
            ps.setObject(7, b.getUserId(), Types.INTEGER);

            // Convert java.util.Date to Timestamp for PreparedStatement
            ps.setTimestamp(8, b.getBookingTime() != null ? new Timestamp(b.getBookingTime().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(9, b.getExpectedArrival() != null ? new Timestamp(b.getExpectedArrival().getTime()) : null);
            ps.setTimestamp(10, b.getActualArrival() != null ? new Timestamp(b.getActualArrival().getTime()) : null);
            ps.setTimestamp(11, b.getDepartureTime() != null ? new Timestamp(b.getDepartureTime().getTime()) : null);

            ps.setObject(12, b.getTotalHours(), Types.DOUBLE);
            ps.setObject(13, b.getTotalAmount(), Types.DOUBLE);
            ps.setString(14, b.getBookingRef());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : null;
        }
    }

    /* ================= READ ================= */
    public Booking findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE booking_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapResultSetToEntity(rs) : null;
        }
    }

    public List<Booking> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY booking_time DESC";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    /* ================= UPDATE ================= */
    public boolean update(Booking b) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_booking SET
            customer_id=?, vehicle_id=?, duration_of_booking=?,
            slot_id=?, booking_status=?, remarks=?, user_id=?,
            booking_time=?, expected_arrival=?, actual_arrival=?,
            departure_time=?, total_hours=?, total_amount=?, booking_ref=?
            WHERE booking_id=?
        """;

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, b.getCustomerId(), Types.INTEGER);
            ps.setInt(2, b.getVehicleId());
            ps.setString(3, b.getDurationOfBooking());
            ps.setInt(4, b.getSlotId());
            ps.setInt(5, b.getBookingStatus());
            ps.setString(6, b.getRemarks());
            ps.setObject(7, b.getUserId(), Types.INTEGER);

            ps.setTimestamp(8, b.getBookingTime() != null ? new Timestamp(b.getBookingTime().getTime()) : null);
            ps.setTimestamp(9, b.getExpectedArrival() != null ? new Timestamp(b.getExpectedArrival().getTime()) : null);
            ps.setTimestamp(10, b.getActualArrival() != null ? new Timestamp(b.getActualArrival().getTime()) : null);
            ps.setTimestamp(11, b.getDepartureTime() != null ? new Timestamp(b.getDepartureTime().getTime()) : null);

            ps.setObject(12, b.getTotalHours(), Types.DOUBLE);
            ps.setObject(13, b.getTotalAmount(), Types.DOUBLE);
            ps.setString(14, b.getBookingRef());
            ps.setInt(15, b.getBookingId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int bookingId, int status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET booking_status=? WHERE booking_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean checkIn(int bookingId) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_booking
            SET booking_status=?, actual_arrival=CURRENT_TIMESTAMP
            WHERE booking_id=?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Booking.STATUS_CHECKED_IN);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean checkOut(int bookingId, Double hours, Double amount) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_booking
            SET booking_status=?, departure_time=CURRENT_TIMESTAMP,
                total_hours=?, total_amount=?
            WHERE booking_id=?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Booking.STATUS_CHECKED_OUT);
            ps.setObject(2, hours, Types.DOUBLE);
            ps.setObject(3, amount, Types.DOUBLE);
            ps.setInt(4, bookingId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE booking_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
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

    public boolean isSlotBooked(int slotId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM inet_vehicleparking.tbl_booking
            WHERE slot_id=? AND booking_status IN (1,2)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
