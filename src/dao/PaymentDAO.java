package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.ParkingSlot;
import models.Payment;

public class PaymentDAO extends BaseDAO<Payment> {

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_payment";
    }

    @Override
    protected String getIdColumnName() {
        return "payment_id"; // or real PK column
    }

    @Override
    protected Payment mapResultSetToEntity(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setBookingId(rs.getInt("booking_id"));
        p.setUserId((Integer) rs.getObject("user_id"));
        p.setDueAmount((Double) rs.getObject("due_amount"));
        p.setPaidAmount((Double) rs.getObject("paid_amount"));
        p.setMethod(rs.getString("method"));
        p.setPaymentStatus(rs.getInt("payment_status"));
        p.setPaidBy(rs.getString("paid_by"));
        p.setPaymentDate(rs.getTimestamp("payment_date"));
        p.setRemarks(rs.getString("remarks"));
        return p;
    }

    // ================= CREATE PAYMENT =================
    public void create(Payment p) throws SQLException {

        String sql = "INSERT INTO " + getTableName() +
            " (booking_id, user_id, due_amount, paid_amount, method, payment_status, paid_by, payment_date, remarks) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getBookingId());
            ps.setObject(2, p.getUserId(), Types.INTEGER);
            ps.setDouble(3, p.getDueAmount());
            ps.setDouble(4, p.getPaidAmount());
            ps.setString(5, p.getMethod());
            ps.setInt(6, p.getPaymentStatus());
            ps.setString(7, p.getPaidBy());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.setString(9, p.getRemarks());

            ps.executeUpdate();
        }
    }

    public boolean createPaymentAndOccupySlot(Payment payment) throws SQLException {
    Connection conn = getConnection();
    try {
        conn.setAutoCommit(false);

        // 1️⃣ Insert payment
        String sqlPayment = "INSERT INTO inet_vehicleparking.tbl_payment "
                + "(booking_id, user_id, due_amount, paid_amount, method, payment_status, paid_by, payment_date, remarks) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlPayment)) {
            ps.setInt(1, payment.getBookingId());
            ps.setObject(2, payment.getUserId(), Types.INTEGER);
            ps.setDouble(3, payment.getDueAmount());
            ps.setDouble(4, payment.getPaidAmount());
            ps.setString(5, payment.getMethod());
            ps.setInt(6, Payment.STATUS_SUCCESS); // mark as successful
            ps.setString(7, payment.getPaidBy());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.setString(9, payment.getRemarks());
            ps.executeUpdate();
        }

        // 2️⃣ Update slot to OCCUPIED
        String sqlSlot = "UPDATE inet_vehicleparking.tbl_parking_slot "
                + "SET parking_slot_status=? WHERE parking_slot_id=(SELECT slot_id FROM inet_vehicleparking.tbl_booking WHERE booking_id=?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlSlot)) {
            ps.setInt(1, ParkingSlot.STATUS_OCCUPIED);
            ps.setInt(2, payment.getBookingId());
            ps.executeUpdate();
        }

        conn.commit();
        return true;
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
        conn.close();
    }
}

    // ================= READ =================
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY payment_date DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToEntity(rs));
        }
        return list;
    }

    public Payment findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE booking_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public Payment findByRefNo(String refNo) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE ref_no = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public List<Payment> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ? ORDER BY payment_date DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }

    public boolean insertPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO " + getTableName() +
                     " (booking_id, user_id, due_amount, paid_amount, method, payment_status, paid_by, payment_date, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payment.getBookingId());
            ps.setObject(2, payment.getUserId(), Types.INTEGER);
            ps.setDouble(3, payment.getDueAmount());
            ps.setDouble(4, payment.getPaidAmount());
            ps.setString(5, payment.getMethod());
            ps.setInt(6, payment.getPaymentStatus());
            ps.setString(7, payment.getPaidBy());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.setString(9, payment.getRemarks());

            return ps.executeUpdate() > 0;
        }
    }
}
