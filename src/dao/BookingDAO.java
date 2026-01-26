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

        // Added missing fields to match Booking model
        b.setExpectedArrival(rs.getTimestamp("expected_arrival"));
        b.setActualArrival(rs.getTimestamp("actual_arrival"));
        b.setDepartureTime(rs.getTimestamp("departure_time"));
        b.setTotalHours((Double) rs.getObject("total_hours"));
        b.setTotalAmount((Double) rs.getObject("total_amount"));
        b.setBookingRef(rs.getString("booking_ref"));

        return b;
    }

    // ================= FIND METHODS =================

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapResultSetToEntity(rs) : null;
        }
    }

    public List<Booking> findByUserId(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ? ORDER BY booking_time DESC";

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

    public List<Booking> findByCustomerId(int customerId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE customer_id = ? ORDER BY booking_time DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    public List<Booking> findByStatus(int status) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE booking_status = ? ORDER BY booking_time DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY booking_time DESC";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    // ================= PENDING BOOKINGS =================
    public List<Booking> findPendingBookings() throws SQLException {
        return findByStatus(Booking.STATUS_PENDING);
    }

    // ================= CREATE BOOKING =================
    public int createBookingWithSlotUpdate(Booking booking) throws SQLException {
        Connection conn = null;
        PreparedStatement psBooking = null;
        PreparedStatement psSlot = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1️⃣ Generate booking reference
            String bookingRef = generateBookingRef();

            // 2️⃣ Insert booking (PENDING)
            String sqlBooking = """
                        INSERT INTO inet_vehicleparking.tbl_booking
                        (customer_id, vehicle_id, slot_id, booking_status,
                        duration_of_booking, remarks, booking_time, user_id, booking_ref)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            int bookingId = 0;
            psBooking = conn.prepareStatement(sqlBooking, Statement.RETURN_GENERATED_KEYS);
            psBooking.setInt(1, booking.getCustomerId());
            psBooking.setInt(2, booking.getVehicleId());
            psBooking.setInt(3, booking.getSlotId());
            psBooking.setInt(4, Booking.STATUS_PENDING);
            psBooking.setString(5, booking.getDurationOfBooking());
            psBooking.setString(6, booking.getRemarks());
            psBooking.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            psBooking.setInt(8, booking.getUserId());
            psBooking.setString(9, bookingRef);

            psBooking.executeUpdate();

            rs = psBooking.getGeneratedKeys();
            if (rs.next()) {
                bookingId = rs.getInt(1);
            }

            // 3️⃣ Reserve slot
            String sqlSlot = """
                        UPDATE inet_vehicleparking.tbl_parking_slot
                        SET parking_slot_status = ?, user_id = ?
                        WHERE parking_slot_id = ?
                    """;
            psSlot = conn.prepareStatement(sqlSlot);
            psSlot.setInt(1, ParkingSlot.STATUS_RESERVED);
            psSlot.setInt(2, booking.getUserId());
            psSlot.setInt(3, booking.getSlotId());
            psSlot.executeUpdate();

            conn.commit();

            conn.commit();
            return bookingId;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            // Close resources but NOT the connection
            if (rs != null)
                rs.close();
            if (psBooking != null)
                psBooking.close();
            if (psSlot != null)
                psSlot.close();

            // Reset auto-commit but DO NOT close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkTableStructure() throws SQLException {
        String sql = """
                    SELECT column_name, data_type, is_nullable
                    FROM information_schema.columns
                    WHERE table_name = 'tbl_booking'
                    AND table_schema = 'inet_vehicleparking'
                    ORDER BY ordinal_position
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            System.out.println("=== tbl_booking COLUMNS ===");
            while (rs.next()) {
                System.out.printf("%-20s %-15s %s%n",
                        rs.getString("column_name"),
                        rs.getString("data_type"),
                        rs.getString("is_nullable"));
            }
            System.out.println("==========================");
        }
    }

    // ================= APPROVE BOOKING =================
    public boolean approveBookingNow(int bookingId, int adminUserId) throws SQLException {
        Connection conn = null;
        PreparedStatement psBooking = null;
        PreparedStatement psSlot = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            System.out.println("🚀 DEBUG: Starting approval for booking #" + bookingId);

            // 1. GET BOOKING DETAILS
            String getSql = "SELECT slot_id, booking_status, user_id, duration_of_booking FROM " +
                    getTableName() + " WHERE booking_id = ?";
            psBooking = conn.prepareStatement(getSql);
            psBooking.setInt(1, bookingId);
            rs = psBooking.executeQuery();

            if (!rs.next()) {
                throw new SQLException("❌ Booking #" + bookingId + " not found!");
            }

            int slotId = rs.getInt("slot_id");
            int currentStatus = rs.getInt("booking_status");
            int userId = rs.getInt("user_id");
            String duration = rs.getString("duration_of_booking");

            System.out.println("📋 DEBUG: Booking details - Slot: " + slotId +
                    ", Status: " + currentStatus);

            // 2. VALIDATE BOOKING STATUS
            if (currentStatus == Booking.STATUS_APPROVED) {
                throw new SQLException("⚠️ Booking #" + bookingId + " is already APPROVED!");
            }

            if (currentStatus != Booking.STATUS_PENDING) {
                throw new SQLException("❌ Booking #" + bookingId + " is not PENDING. Current status: " +
                        (currentStatus == 0 ? "PENDING" : currentStatus == 1 ? "APPROVED" : "REJECTED"));
            }

            // 3. CHECK SLOT STATUS
            String checkSlotSql = "SELECT parking_slot_status FROM inet_vehicleparking.tbl_parking_slot " +
                    "WHERE parking_slot_id = ?";
            try (PreparedStatement psCheckSlot = conn.prepareStatement(checkSlotSql)) {
                psCheckSlot.setInt(1, slotId);
                try (ResultSet rsSlot = psCheckSlot.executeQuery()) {
                    if (!rsSlot.next()) {
                        throw new SQLException("❌ Slot #" + slotId + " not found!");
                    }

                    int slotStatus = rsSlot.getInt("parking_slot_status");
                    System.out.println("🅿️ DEBUG: Slot #" + slotId + " current status: " + slotStatus);

                    // Check if slot is already occupied
                    if (slotStatus == ParkingSlot.STATUS_OCCUPIED) {
                        throw new SQLException("❌ Slot #" + slotId + " is already OCCUPIED!");
                    }
                }
            }

            // 4. UPDATE BOOKING STATUS - SIMPLIFIED (no approval timestamp)
            System.out.println("📝 DEBUG: Updating booking status to APPROVED...");
            String updateBookingSql = "UPDATE " + getTableName() +
                    " SET booking_status = ? " +
                    "WHERE booking_id = ?";

            psBooking = conn.prepareStatement(updateBookingSql);
            psBooking.setInt(1, Booking.STATUS_APPROVED);
            psBooking.setInt(2, bookingId);

            int bookingRows = psBooking.executeUpdate();
            System.out.println("✅ DEBUG: Booking rows updated: " + bookingRows);

            if (bookingRows == 0) {
                throw new SQLException("❌ Failed to update booking status!");
            }

            // 5. UPDATE SLOT STATUS
            System.out.println("🔄 DEBUG: Updating slot #" + slotId + " to OCCUPIED...");

            // Use simple update to avoid transaction issues with Postgres
            String updateSlotSql = "UPDATE inet_vehicleparking.tbl_parking_slot " +
                    "SET parking_slot_status = ? " +
                    "WHERE parking_slot_id = ?";

            psSlot = conn.prepareStatement(updateSlotSql);
            psSlot.setInt(1, ParkingSlot.STATUS_OCCUPIED);
            psSlot.setInt(2, slotId);
            int slotRows = psSlot.executeUpdate();
            System.out.println("✅ DEBUG: Slot rows updated: " + slotRows);

            if (slotRows == 0) {
                throw new SQLException("❌ Failed to update slot status!");
            }

            // 6. CREATE PAYMENT RECORD
            System.out.println("💰 DEBUG: Creating payment record...");
            double amount = calculateAmountFromDuration(duration);

            // Status 1 = APPROVED_UNPAID (Booking Approved, Payment Required)
            String paymentSql = """
                        INSERT INTO inet_vehicleparking.tbl_payment
                        (booking_id, user_id, amount_due, amount_paid, payment_status, remarks, payment_date)
                        VALUES (?, ?, ?, 0.0, 1, 'Payment pending for approved booking', NULL)
                    """;

            try (PreparedStatement psPayment = conn.prepareStatement(paymentSql)) {
                psPayment.setInt(1, bookingId);
                psPayment.setInt(2, userId);
                psPayment.setDouble(3, amount);
                psPayment.executeUpdate();
                System.out.println("✅ DEBUG: Payment record created for $" + amount);
            }

            // 6. COMMIT TRANSACTION
            conn.commit();
            System.out.println("🎉 DEBUG: Transaction SUCCESSFUL! Booking #" + bookingId + " approved.");
            return true;

        } catch (SQLException e) {
            System.err.println("💥 DEBUG: Transaction FAILED!");
            System.err.println("Error: " + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 DEBUG: Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    System.err.println("⚠️ DEBUG: Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;

        } finally {
            // Clean up resources
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (psBooking != null)
                try {
                    psBooking.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (psSlot != null)
                try {
                    psSlot.close();
                } catch (SQLException e) {
                    /* ignore */ }

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("⚠️ DEBUG: Failed to reset auto-commit: " + e.getMessage());
                }
            }
        }
    }

    // Helper methods to add to your class
    private String getStatusText(int status) {
        return switch (status) {
            case Booking.STATUS_PENDING -> "PENDING";
            case Booking.STATUS_APPROVED -> "APPROVED";
            case Booking.STATUS_REJECTED -> "REJECTED";
            default -> "UNKNOWN (" + status + ")";
        };
    }

    private String getSlotStatusText(int status) {
        return switch (status) {
            case ParkingSlot.STATUS_AVAILABLE -> "AVAILABLE";
            case ParkingSlot.STATUS_RESERVED -> "RESERVED";
            case ParkingSlot.STATUS_OCCUPIED -> "OCCUPIED";
            default -> "UNKNOWN (" + status + ")";
        };
    }

    // ================= REJECT BOOKING =================
    public boolean rejectBooking(int bookingId) throws SQLException {
        Connection conn = null;
        PreparedStatement psBooking = null;
        PreparedStatement psSlot = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1️⃣ Get booking to find slotId
            String getSql = "SELECT slot_id FROM " + getTableName() + " WHERE booking_id = ?";
            psBooking = conn.prepareStatement(getSql);
            psBooking.setInt(1, bookingId);
            rs = psBooking.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Booking not found: " + bookingId);
            }

            int slotId = rs.getInt("slot_id");

            // 2️⃣ Reject booking
            String rejectSql = "UPDATE inet_vehicleparking.tbl_booking SET booking_status=? WHERE booking_id=?";
            psBooking = conn.prepareStatement(rejectSql);
            psBooking.setInt(1, Booking.STATUS_REJECTED);
            psBooking.setInt(2, bookingId);
            psBooking.executeUpdate();

            // 3️⃣ Free slot and clear user_id
            String slotSql = "UPDATE inet_vehicleparking.tbl_parking_slot SET parking_slot_status=?, user_id=NULL WHERE parking_slot_id=?";
            psSlot = conn.prepareStatement(slotSql);
            psSlot.setInt(1, ParkingSlot.STATUS_AVAILABLE);
            psSlot.setInt(2, slotId);
            psSlot.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            // Close resources but NOT the connection
            if (rs != null)
                rs.close();
            if (psBooking != null)
                psBooking.close();
            if (psSlot != null)
                psSlot.close();

            // Reset auto-commit but DO NOT close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ================= UPDATE METHODS =================
    public boolean updateBookingStatus(int bookingId, int status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET booking_status = ? WHERE booking_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateArrivalTime(int bookingId, Timestamp arrivalTime) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET actual_arrival = ? WHERE booking_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, arrivalTime);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateDepartureTime(int bookingId, Timestamp departureTime, double totalHours, double totalAmount)
            throws SQLException {
        String sql = "UPDATE " + getTableName()
                + " SET departure_time = ?, total_hours = ?, total_amount = ? WHERE booking_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, departureTime);
            ps.setDouble(2, totalHours);
            ps.setDouble(3, totalAmount);
            ps.setInt(4, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    /* ================= UPDATE ================= */
    public boolean update(Booking booking) throws SQLException {
        String sql = "UPDATE " + getTableName() +
                " SET customer_id = ?, vehicle_id = ?, slot_id = ?, " +
                "duration_of_booking = ?, booking_status = ?, " +
                "booking_time = ?, actual_end_time = ?, " +
                "user_id = ?, remarks = ? " +
                "WHERE booking_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getVehicleId());
            pstmt.setInt(3, booking.getSlotId());
            pstmt.setString(4, booking.getDurationOfBooking());
            pstmt.setInt(5, booking.getBookingStatus());

            if (booking.getBookingTime() != null) {
                pstmt.setTimestamp(6, booking.getBookingTime());
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            if (booking.getActualEndTime() != null) {
                pstmt.setTimestamp(7, booking.getActualEndTime());
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setInt(8, booking.getUserId());
            pstmt.setString(9, booking.getRemarks());
            pstmt.setInt(10, booking.getBookingId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // ================= HELPER METHODS =================
    private String generateBookingRef() {
        // Generate a unique booking reference like "BK-20241215-12345"
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("BK-%tY%tm%td-%04d",
                timestamp, timestamp, timestamp, random);
    }

    private double calculateAmountFromDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 5.00; // Default minimum charge
        }

        try {
            // Parse duration string like "2 hours", "30 minutes", "1 day"
            String lowerDuration = duration.toLowerCase();

            if (lowerDuration.contains("hour")) {
                String[] parts = duration.split(" ");
                double hours = Double.parseDouble(parts[0]);
                return hours * 5.00; // $5 per hour
            } else if (lowerDuration.contains("day")) {
                String[] parts = duration.split(" ");
                double days = Double.parseDouble(parts[0]);
                return days * 50.00; // $50 per day
            } else if (lowerDuration.contains("minute")) {
                String[] parts = duration.split(" ");
                double minutes = Double.parseDouble(parts[0]);
                double hours = minutes / 60.0;
                return Math.max(hours * 5.00, 2.00); // Minimum $2
            }
        } catch (Exception e) {
            // If parsing fails, use default rate
        }

        return 5.00; // Default rate
    }

    // ================= COUNT METHODS =================
    public int countBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countBookingsByStatus(int status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE booking_status = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countBookingsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ================= DELETE METHOD =================
    public boolean delete(int bookingId) throws SQLException {
        Connection conn = null;
        PreparedStatement psGet = null;
        PreparedStatement psSlot = null;
        PreparedStatement psDelete = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1️⃣ Get booking details
            String getSql = "SELECT slot_id, booking_status FROM " + getTableName() + " WHERE booking_id = ?";
            psGet = conn.prepareStatement(getSql);
            psGet.setInt(1, bookingId);
            rs = psGet.executeQuery();

            if (!rs.next()) {
                return false;
            }

            int slotId = rs.getInt("slot_id");
            int bookingStatus = rs.getInt("booking_status");

            // 2️⃣ Free the slot if it's reserved/occupied by this booking
            if (bookingStatus == Booking.STATUS_PENDING || bookingStatus == Booking.STATUS_APPROVED) {
                String slotSql = "UPDATE inet_vehicleparking.tbl_parking_slot SET parking_slot_status=?, user_id=NULL WHERE parking_slot_id=?";
                psSlot = conn.prepareStatement(slotSql);
                psSlot.setInt(1, ParkingSlot.STATUS_AVAILABLE);
                psSlot.setInt(2, slotId);
                psSlot.executeUpdate();
            }

            // 3️⃣ Delete the booking
            String deleteSql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
            psDelete = conn.prepareStatement(deleteSql);
            psDelete.setInt(1, bookingId);
            int rows = psDelete.executeUpdate();

            conn.commit();
            return rows > 0;

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            // Close resources but NOT the connection
            if (rs != null)
                rs.close();
            if (psGet != null)
                psGet.close();
            if (psSlot != null)
                psSlot.close();
            if (psDelete != null)
                psDelete.close();

            // Reset auto-commit but DO NOT close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}