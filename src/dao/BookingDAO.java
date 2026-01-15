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
        return b;
    }

    // ================= USER BOOKING =================
    public int createBookingWithSlotUpdate(Booking booking) throws SQLException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            // 1️⃣ Insert booking (PENDING)
            String sqlBooking = """
                INSERT INTO inet_vehicleparking.tbl_booking
                (customer_id, vehicle_id, slot_id, booking_status,
                 duration_of_booking, remarks, booking_time, user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            int bookingId;
            try (PreparedStatement ps = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, booking.getCustomerId());
                ps.setInt(2, booking.getVehicleId());
                ps.setInt(3, booking.getSlotId());
                ps.setInt(4, Booking.STATUS_PENDING); // Status PENDING initially
                ps.setString(5, booking.getDurationOfBooking());
                ps.setString(6, booking.getRemarks());
                ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                ps.setInt(8, booking.getUserId());

                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                bookingId = rs.next() ? rs.getInt(1) : 0;
            }

            // 2️⃣ Reserve slot
            String sqlSlot = """
                UPDATE inet_vehicleparking.tbl_parking_slot
                SET parking_slot_status = ?, user_id = ?
                WHERE parking_slot_id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlSlot)) {
                ps.setInt(1, ParkingSlot.STATUS_RESERVED); // Reserved
                ps.setInt(2, booking.getUserId());
                ps.setInt(3, booking.getSlotId());
                ps.executeUpdate();
            }

            conn.commit();
            return bookingId;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }  

    public List<Booking> findByUserId(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM inet_vehicleparking.tbl_booking
            WHERE user_id = ?
            ORDER BY booking_time DESC
        """;

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }


    // ================= ADMIN VIEW =================
    public List<Booking> findPendingBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM inet_vehicleparking.tbl_booking
            WHERE booking_status = ?
            ORDER BY booking_time DESC
        """;

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Booking.STATUS_PENDING);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }


    // ADMIN APPROVE (CREATE PAYMENT)
public void approveBooking(int bookingId, int adminUserId) throws SQLException {
    Connection conn = getConnection();
    try {
        conn.setAutoCommit(false);

        // 1️⃣ Approve booking
        String approveSql = """
            UPDATE inet_vehicleparking.tbl_booking
            SET booking_status = ?, approved_by = ?
            WHERE booking_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(approveSql)) {
            ps.setInt(1, Booking.STATUS_APPROVED);
            ps.setInt(2, adminUserId);
            ps.setInt(3, bookingId);
            ps.executeUpdate();
        }

        // 2️⃣ Create payment (FOR CUSTOMER)
        String paymentSql = """
            INSERT INTO inet_vehicleparking.tbl_payment
            (booking_id, amount_due, amount_paid, payment_status, user_id)
            SELECT booking_id, 5.00, 0, 0, customer_id
            FROM inet_vehicleparking.tbl_booking
            WHERE booking_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }

        // 3️⃣ Mark slot OCCUPIED
        String slotSql = """
            UPDATE inet_vehicleparking.tbl_parking_slot
            SET parking_slot_status = ?
            WHERE parking_slot_id = (
                SELECT slot_id FROM inet_vehicleparking.tbl_booking
                WHERE booking_id = ?
            )
        """;

        try (PreparedStatement ps = conn.prepareStatement(slotSql)) {
            ps.setInt(1, ParkingSlot.STATUS_OCCUPIED);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }

        conn.commit();
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.close();
    }
}


    public boolean rejectBooking(int bookingId, int slotId) throws SQLException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            // Reject booking
            PreparedStatement ps1 = conn.prepareStatement(
                "UPDATE inet_vehicleparking.tbl_booking SET booking_status=? WHERE booking_id=?"
            );
            ps1.setInt(1, Booking.STATUS_REJECTED); // Set rejected status
            ps1.setInt(2, bookingId);
            ps1.executeUpdate();

            // Free slot
            PreparedStatement ps2 = conn.prepareStatement(
                "UPDATE inet_vehicleparking.tbl_parking_slot SET parking_slot_status=? WHERE parking_slot_id=?"
            );
            ps2.setInt(1, ParkingSlot.STATUS_AVAILABLE); // Set slot to available
            ps2.setInt(2, slotId);
            ps2.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    // ================= COUNT =================
    public int countBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_booking";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
