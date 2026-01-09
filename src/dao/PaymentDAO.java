package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Payment;

public class PaymentDAO extends BaseDAO<Payment> {

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_payment";
    }

    @Override
    protected String getIdColumnName() {
        return "ref_no";
    }

    @Override
    protected Payment mapResultSetToEntity(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setRefNo(rs.getString("ref_no"));
        p.setBookingRef(rs.getString("booking_ref"));
        p.setUserId((Integer) rs.getObject("user_id"));
        p.setDueAmount((Double) rs.getObject("due_amount"));
        p.setPaidAmount((Double) rs.getObject("paid_amount"));
        p.setMethod(rs.getString("method"));
        p.setPaymentDate(rs.getTimestamp("payment_date"));
        p.setRemarks(rs.getString("remarks"));
        return p;
    }

    // ================= GET ALL PAYMENTS =================
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY payment_date DESC";
        List<Payment> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    // ================= INSERT PAYMENT =================
    public boolean insertPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO " + getTableName() +
                "(ref_no, booking_ref, user_id, due_amount, paid_amount, method, payment_date, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getRefNo());
            ps.setString(2, p.getBookingRef());
            ps.setObject(3, p.getUserId(), Types.INTEGER);
            ps.setObject(4, p.getDueAmount(), Types.DOUBLE);
            ps.setObject(5, p.getPaidAmount(), Types.DOUBLE);
            ps.setString(6, p.getMethod());
            ps.setTimestamp(7, p.getPaymentDate() != null ? new Timestamp(p.getPaymentDate().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(8, p.getRemarks());

            return ps.executeUpdate() > 0;
        }
    }

    // ================= FIND PAYMENT BY REF NO =================
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
}
