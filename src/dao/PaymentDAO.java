package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Payment;

public class PaymentDAO extends BaseDAO<Payment> {

    // Payment Status Constants (should match your Payment model)
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_PARTIAL = 2;

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
            p.setUserId((Integer) userId);
        }
        
        p.setDueAmount(rs.getDouble("amount_due"));
        p.setPaidAmount(rs.getDouble("amount_paid"));
        p.setPaymentStatus(rs.getInt("payment_status"));
        p.setPaidBy(rs.getString("paid_by"));
        p.setRemarks(rs.getString("remarks"));
        
        // Handle payment_date if it exists
        try {
            Date paymentDate = rs.getDate("payment_date");
            if (paymentDate != null) {
                p.setPaymentDate(paymentDate);
            }
        } catch (SQLException e) {
            // Column might not exist, ignore
        }
        
        // Handle payment_method if it exists
        try {
            String method = rs.getString("payment_method");
            if (method != null) {
                // You might need to add a setPaymentMethod() in Payment model
            }
        } catch (SQLException e) {
            // Column might not exist, ignore
        }
        
        return p;
    }

    public List<Payment> findByUserId(int userId) throws SQLException {
        List<Payment> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM inet_vehicleparking.tbl_payment
            WHERE user_id = ?
            ORDER BY payment_id DESC
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

    public void createPendingPayment(int bookingId, int userId) throws SQLException {
        String sql = "INSERT INTO inet_vehicleparking.tbl_payment "
                   + "(booking_id, payment_status, user_id) "
                   + "VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setInt(2, STATUS_PENDING);  // Use constant
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

    // Find unpaid payments
    public List<Payment> findUnpaid() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM inet_vehicleparking.tbl_payment " +
                    "WHERE payment_status IN (0, 2) " +  // Pending or Partial
                    "ORDER BY payment_id DESC";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    // Count total payments
    public int countPayments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_payment";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Sum total revenue
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM inet_vehicleparking.tbl_payment";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // CREATE payment with all fields
    public void create(Payment p) throws SQLException {
        String sql = """
            INSERT INTO inet_vehicleparking.tbl_payment
            (booking_id, user_id, amount_due, amount_paid,
             payment_status, paid_by, remarks, payment_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
            
            // Handle payment date
            if (p.getPaymentDate() != null) {
                ps.setDate(8, new java.sql.Date(p.getPaymentDate().getTime()));
            } else {
                ps.setDate(8, null);
            }

            ps.executeUpdate();
        }
    }

    // UPDATE payment
    public void update(Payment p) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_payment
            SET booking_id = ?, 
                user_id = ?,
                amount_due = ?,
                amount_paid = ?,
                payment_status = ?,
                paid_by = ?,
                remarks = ?,
                payment_date = ?
            WHERE payment_id = ?
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
            
            if (p.getPaymentDate() != null) {
                ps.setDate(8, new java.sql.Date(p.getPaymentDate().getTime()));
            } else {
                ps.setDate(8, null);
            }
            
            ps.setInt(9, p.getPaymentId());

            ps.executeUpdate();
        }
    }

    // MARK AS PAID - Simple version
    public void markAsPaid(int paymentId, double amount, String paidBy) throws SQLException {
        String sql = """
            UPDATE inet_vehicleparking.tbl_payment
            SET amount_paid = ?,
                payment_status = ?,
                paid_by = ?,
                payment_date = CURRENT_TIMESTAMP
            WHERE payment_id = ?
        """;
        
        // Determine status based on amount
        // First get the current due amount
        double dueAmount = getDueAmount(paymentId);
        int newStatus = amount >= dueAmount ? STATUS_PAID : STATUS_PARTIAL;
        
        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, newStatus);
            ps.setString(3, paidBy);
            ps.setInt(4, paymentId);
            ps.executeUpdate();
        }
    }
    
    // Helper method to get due amount
    private double getDueAmount(int paymentId) throws SQLException {
        String sql = "SELECT amount_due FROM inet_vehicleparking.tbl_payment WHERE payment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // Also add this helper method
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }
    
    // Find payment by booking ID
    public Payment findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM inet_vehicleparking.tbl_payment WHERE booking_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        }
        return null;
    }
    
    // Delete payment
    public void delete(int paymentId) throws SQLException {
        String sql = "DELETE FROM inet_vehicleparking.tbl_payment WHERE payment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ps.executeUpdate();
        }
    }
}   