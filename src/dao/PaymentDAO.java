package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Payment;
import utils.TelegramService;

public class PaymentDAO extends BaseDAO<Payment> {

    // ================= PAYMENT STATUS CONSTANTS =================
    // Updated to match your requirement
    public static final int STATUS_PENDING_APPROVAL = 0; // Booking pending admin approval
    public static final int STATUS_APPROVED_UNPAID = 1; // Admin approved, user sees payment in dashboard
    public static final int STATUS_PAID = 2; // User paid, admin sees as "PAID"
    public static final int STATUS_PARTIAL = 3; // Partial payment
    public static final int STATUS_CANCELLED = 4; // Cancelled

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_payment";
    }

    @Override
    protected String getIdColumnName() {
        return "payment_id";
    }

    @Override
    protected Payment mapResultSetToEntity(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setBookingId(rs.getInt("booking_id"));

        // Handle null user_id
        Object userId = rs.getObject("user_id");
        if (userId != null) {
            p.setUserId(((Number) userId).intValue());
        }

        p.setDueAmount(rs.getDouble("amount_due"));
        p.setPaidAmount(rs.getDouble("amount_paid"));
        p.setPaymentStatus(rs.getInt("payment_status"));
        p.setPaidBy(rs.getString("paid_by"));
        p.setRemarks(rs.getString("remarks"));

        // Handle payment_date
        try {
            Date paymentDate = rs.getDate("payment_date");
            if (paymentDate != null) {
                p.setPaymentDate(paymentDate);
            }
        } catch (SQLException e) {
            // Column might not exist
        }

        return p;
    }

    // ================= CRUD METHODS =================
    @Override
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY payment_id DESC";
        List<Payment> list = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    @Override
    public Payment findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    // CREATE method
    public Integer create(Payment payment) throws SQLException {
        String sql = "INSERT INTO " + getTableName() +
                " (booking_id, user_id, amount_due, amount_paid, " +
                "payment_status, paid_by, remarks, payment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getBookingId());

            if (payment.getUserId() != null) {
                pstmt.setInt(2, payment.getUserId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setDouble(3, payment.getDueAmount());
            pstmt.setDouble(4, payment.getPaidAmount());
            pstmt.setInt(5, payment.getPaymentStatus());
            pstmt.setString(6, payment.getPaidBy());
            pstmt.setString(7, payment.getRemarks());

            if (payment.getPaymentDate() != null) {
                pstmt.setDate(8, new java.sql.Date(payment.getPaymentDate().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

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
    public boolean update(Payment payment) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                "booking_id = ?, user_id = ?, amount_due = ?, " +
                "amount_paid = ?, payment_status = ?, paid_by = ?, " +
                "remarks = ?, payment_date = ? WHERE payment_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, payment.getBookingId());

            if (payment.getUserId() != null) {
                pstmt.setInt(2, payment.getUserId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setDouble(3, payment.getDueAmount());
            pstmt.setDouble(4, payment.getPaidAmount());
            pstmt.setInt(5, payment.getPaymentStatus());
            pstmt.setString(6, payment.getPaidBy());
            pstmt.setString(7, payment.getRemarks());

            if (payment.getPaymentDate() != null) {
                pstmt.setDate(8, new java.sql.Date(payment.getPaymentDate().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            pstmt.setInt(9, payment.getPaymentId());

            boolean updated = pstmt.executeUpdate() > 0;
            if (updated && payment.getPaymentStatus() == STATUS_PAID) {
                triggerTelegramNotification(payment.getPaymentId());
            }
            return updated;
        }
    }

    private void triggerTelegramNotification(int paymentId) {
        new Thread(() -> {
            try {
                // Fetch full data with joins for the report
                String sql = """
                            SELECT p.*, b.booking_ref, u.fullname
                            FROM inet_vehicleparking.tbl_payment p
                            LEFT JOIN inet_vehicleparking.tbl_booking b ON p.booking_id = b.booking_id
                            LEFT JOIN inet_vehicleparking.tbl_user u ON p.user_id = u.user_id
                            WHERE p.payment_id = ?
                        """;

                try (Connection conn = getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, paymentId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            Payment p = mapResultSetToEntity(rs);
                            p.setBookingRef(rs.getString("booking_ref"));
                            p.setFullName(rs.getString("fullname"));
                            // Call the notification service
                            TelegramService.sendKhmerPaymentNotification(p);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to trigger Telegram notification: " + e.getMessage());
            }
        }).start();
    }

    // ================= USER DASHBOARD: GET PAYMENTS AFTER ADMIN APPROVAL
    // =================
    public List<Payment> getUserPendingPayments(int userId) throws SQLException {
        String sql = """
                    SELECT p.*,
                           b.booking_ref,
                           vo.vehicle_owner_name as full_name,
                           v.vehicle_plate_number,
                           v.vehicle_id,
                           s.parking_slot_name,
                           b.duration_of_booking,
                           CASE p.payment_status
                               WHEN 1 THEN 'PAYMENT REQUIRED'
                               WHEN 3 THEN 'PARTIAL PAYMENT'
                               ELSE 'UNKNOWN'
                           END as status_display
                    FROM inet_vehicleparking.tbl_payment p
                    LEFT JOIN inet_vehicleparking.tbl_booking b ON p.booking_id = b.booking_id
                    LEFT JOIN inet_vehicleparking.tbl_vehicle_owner vo ON b.customer_id = vo.vehicle_owner_id
                    LEFT JOIN inet_vehicleparking.tbl_vehicle v ON b.vehicle_id = v.vehicle_id
                    LEFT JOIN inet_vehicleparking.tbl_parking_slot s ON b.slot_id = s.parking_slot_id
                    WHERE p.user_id = ?
                    AND p.payment_status IN (?, ?)  -- APPROVED_UNPAID or PARTIAL
                    AND b.booking_status = 1  -- Only approved bookings
                    ORDER BY p.payment_id DESC
                """;

        List<Payment> list = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, STATUS_APPROVED_UNPAID);
            pstmt.setInt(3, STATUS_PARTIAL);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment payment = mapResultSetToEntity(rs);
                    payment.setBookingRef(rs.getString("booking_ref"));
                    payment.setFullName(rs.getString("full_name"));
                    payment.setVehicleId(rs.getInt("vehicle_id"));
                    list.add(payment);
                }
            }
        }
        return list;
    }

    // ================= ADMIN: CREATE PAYMENT AFTER APPROVAL =================
    public Payment createPaymentForUser(int bookingId, int userId, double amount, String remarks)
            throws SQLException {

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setDueAmount(amount);
        payment.setPaidAmount(0.0);
        payment.setPaymentStatus(STATUS_APPROVED_UNPAID);
        payment.setRemarks("Admin approved: " + remarks);
        payment.setPaymentDate(null);

        Integer paymentId = create(payment);

        if (paymentId != null) {
            payment.setPaymentId(paymentId);
        }

        return payment;
    }

    // ================= USER: MAKE PAYMENT =================
    public boolean processUserPayment(int paymentId, double amount, String paymentMethod,
            String transactionId, int userId) throws SQLException {

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Get current payment with user's full name
            String getSql = """
                        SELECT p.*, b.booking_ref, u.fullname
                        FROM inet_vehicleparking.tbl_payment p
                        LEFT JOIN inet_vehicleparking.tbl_booking b ON p.booking_id = b.booking_id
                        LEFT JOIN inet_vehicleparking.tbl_user u ON p.user_id = u.user_id
                        WHERE p.payment_id = ? AND p.user_id = ?
                    """;

            PreparedStatement ps = conn.prepareStatement(getSql);
            ps.setInt(1, paymentId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Payment not found or unauthorized");
            }

            double dueAmount = rs.getDouble("amount_due");
            double paidAmount = rs.getDouble("amount_paid");
            int currentStatus = rs.getInt("payment_status");

            // Validate
            if (currentStatus == STATUS_PAID) {
                throw new SQLException("Payment already completed");
            }

            if (currentStatus != STATUS_APPROVED_UNPAID && currentStatus != STATUS_PARTIAL) {
                throw new SQLException("Payment not ready for processing");
            }

            double newTotalPaid = paidAmount + amount;
            int newStatus;

            if (newTotalPaid >= dueAmount) {
                newStatus = STATUS_PAID;
            } else {
                newStatus = STATUS_PARTIAL;
            }

            rs.close();
            ps.close();

            // 2. Update payment
            String updateSql = """
                        UPDATE inet_vehicleparking.tbl_payment
                        SET amount_paid = ?,
                            payment_status = ?,
                            paid_by = ?,
                            payment_date = NOW(),
                            remarks = CONCAT(remarks, ' | Paid via ', ?, ' - ', ?)
                        WHERE payment_id = ?
                    """;

            ps = conn.prepareStatement(updateSql);
            ps.setDouble(1, newTotalPaid);
            ps.setInt(2, newStatus);
            ps.setString(3, "User ID: " + userId);
            ps.setString(4, paymentMethod);
            ps.setString(5, transactionId);
            ps.setInt(6, paymentId);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                // 3. If fully paid, update booking payment status
                if (newStatus == STATUS_PAID) {
                    String updateBookingSql = """
                                UPDATE inet_vehicleparking.tbl_booking
                                SET payment_status = 1,
                                    paid_at = NOW()
                                WHERE booking_id = (SELECT booking_id FROM tbl_payment WHERE payment_id = ?)
                            """;

                    ps = conn.prepareStatement(updateBookingSql);
                    ps.setInt(1, paymentId);
                    ps.executeUpdate();
                    ps.close();
                }

                conn.commit();

                // 4. Send Telegram Notification handled via triggerTelegramNotification in
                // update()
                // No need to call it here anymore if update() is called or if we commit first.
                // However, processUserPayment uses raw SQL updates, not the update() method.
                // So we should keep a call here or change the updates to call update().
                triggerTelegramNotification(paymentId);

                return true;
            }

            return false;

        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // ================= ADMIN: VIEW ALL PAYMENTS =================
    public List<Payment> getAllPaymentsForAdmin() throws SQLException {
        String sql = """
                    SELECT p.*,
                           b.booking_ref,
                           u.user_name,
                           vo.vehicle_owner_name as full_name,
                           v.vehicle_plate_number,
                           CASE p.payment_status
                               WHEN 0 THEN 'PENDING APPROVAL'
                               WHEN 1 THEN 'APPROVED UNPAID'
                               WHEN 2 THEN '✅ PAID'
                               WHEN 3 THEN 'PARTIAL'
                               WHEN 4 THEN 'CANCELLED'
                               ELSE 'UNKNOWN'
                           END as admin_status_display,
                           p.payment_date,
                           p.payment_method
                    FROM inet_vehicleparking.tbl_payment p
                    LEFT JOIN inet_vehicleparking.tbl_booking b ON p.booking_id = b.booking_id
                    LEFT JOIN inet_vehicleparking.tbl_user u ON p.user_id = u.user_id
                    LEFT JOIN inet_vehicleparking.tbl_vehicle_owner vo ON b.customer_id = vo.vehicle_owner_id
                    LEFT JOIN inet_vehicleparking.tbl_vehicle v ON b.vehicle_id = v.vehicle_id
                    ORDER BY
                        CASE p.payment_status
                            WHEN 1 THEN 1  -- APPROVED_UNPAID first
                            WHEN 3 THEN 2  -- PARTIAL second
                            WHEN 0 THEN 3  -- PENDING third
                            WHEN 2 THEN 4  -- PAID last
                            ELSE 5
                        END,
                        p.payment_id DESC
                """;

        List<Payment> list = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Payment payment = mapResultSetToEntity(rs);
                payment.setBookingRef(rs.getString("booking_ref"));
                payment.setFullName(rs.getString("full_name"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                list.add(payment);
            }
        }
        return list;
    }

    // ================= CUSTOM METHODS =================
    public List<Payment> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ? ORDER BY payment_id DESC";
        List<Payment> list = new ArrayList<>();

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

    public Payment findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE booking_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public void delete(int paymentId) throws SQLException {
        delete(Integer.valueOf(paymentId));
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public String getStatusText(int status) {
        return switch (status) {
            case STATUS_PENDING_APPROVAL -> "PENDING APPROVAL";
            case STATUS_APPROVED_UNPAID -> "APPROVED UNPAID";
            case STATUS_PAID -> "PAID";
            case STATUS_PARTIAL -> "PARTIAL";
            case STATUS_CANCELLED -> "CANCELLED";
            default -> "UNKNOWN (" + status + ")";
        };
    }
}