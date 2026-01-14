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
        return "payment_id";
    }

    @Override
    protected Payment mapResultSetToEntity(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setBookingId(rs.getInt("booking_id"));
        p.setUserId((Integer) rs.getObject("user_id"));
        p.setDueAmount(rs.getDouble("amount_due"));
        p.setPaidAmount(rs.getDouble("amount_paid"));
        p.setPaymentStatus(rs.getInt("payment_status"));
        p.setPaidBy(rs.getString("paid_by"));
        p.setRemarks(rs.getString("remarks"));
        return p;
    }

    public List<Payment> findByUserId(int userId) throws SQLException {
    List<Payment> list = new ArrayList<>();
    String sql = "SELECT * FROM inet_vehicleparking.tbl_payment WHERE user_id=? ORDER BY payment_id DESC";

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

public void createPendingPayment(int bookingId, int userId) throws SQLException {
    String sql = "INSERT INTO inet_vehicleparking.tbl_payment "
               + "(booking_id, payment_status, user_id) "
               + "VALUES (?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, bookingId);
        ps.setInt(2, Payment.STATUS_PENDING);
        ps.setInt(3, userId);
        ps.executeUpdate();
    }
}


        // ================= READ =================
    public List<Payment> findAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM inet_vehicleparking.tbl_payment ORDER BY payment_id DESC";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    public void create(Payment p) throws SQLException {
        String sql = """
            INSERT INTO inet_vehicleparking.tbl_payment
            (booking_id, user_id, amount_due, amount_paid,
             payment_status, paid_by, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, p.getBookingId());
            ps.setInt(2, p.getUserId());
            ps.setDouble(3, p.getDueAmount());
            ps.setDouble(4, p.getPaidAmount());
            ps.setInt(5, p.getPaymentStatus());
            ps.setString(6, p.getPaidBy());
            ps.setString(7, p.getRemarks());

            ps.executeUpdate();
        }
    }

    public boolean pay(int paymentId, double amount) throws SQLException {
    String sql = """
        UPDATE inet_vehicleparking.tbl_payment
        SET amount_paid=?, payment_status=?
        WHERE payment_id=?
    """;

    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setDouble(1, amount);
        ps.setInt(2, Payment.STATUS_PAID);
        ps.setInt(3, paymentId);

        return ps.executeUpdate() > 0;
    }
}

}
