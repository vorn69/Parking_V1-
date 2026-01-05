// src/dao/PaymentDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Booking;
import models.Payment;
import models.User;

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
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setAmountDue(rs.getDouble("amount_due"));
        payment.setAmountPaid(rs.getDouble("amount_paid"));
        payment.setRemarks(rs.getString("remarks"));
        payment.setPaymentStatus(rs.getInt("payment_status"));
        payment.setPaidBy(rs.getString("paid_by"));
        payment.setUserId(rs.getInt("user_id"));
        
        // Handle nullable timestamp
        Timestamp paymentDate = rs.getTimestamp("payment_date");
        if (!rs.wasNull()) {
            payment.setPaymentDate(paymentDate);
        }
        
        return payment;
    }
    
    public Integer create(Payment payment) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (booking_id, amount_due, amount_paid, remarks, payment_status, " +
                    "paid_by, user_id, payment_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDouble(2, payment.getAmountDue());
            
            if (payment.getAmountPaid() != null) {
                pstmt.setDouble(3, payment.getAmountPaid());
            } else {
                pstmt.setDouble(3, 0.0);
            }
            
            pstmt.setString(4, payment.getRemarks());
            pstmt.setInt(5, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : Payment.STATUS_PENDING);
            pstmt.setString(6, payment.getPaidBy());
            
            if (payment.getUserId() != null) {
                pstmt.setInt(7, payment.getUserId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            if (payment.getPaymentDate() != null) {
                pstmt.setTimestamp(8, payment.getPaymentDate());
            } else {
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            }
            
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Payment findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE payment_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Payment findByIdWithDetails(Integer id) throws SQLException {
        String sql = "SELECT p.*, b.total_amount, vo.vehicle_owner_name, " +
                    "v.vehicle_plate_number, u.fullname as user_fullname " +
                    "FROM " + getTableName() + " p " +
                    "JOIN inet_vehicleparking.tbl_booking b ON p.booking_id = b.booking_id " +
                    "JOIN inet_vehicleparking.tbl_vehicle v ON b.vehicle_id = v.vehicle_id " +
                    "JOIN inet_vehicleparking.tbl_vehicle_owner vo ON v.vehicle_owner_id = vo.vehicle_owner_id " +
                    "LEFT JOIN inet_vehicleparking.tbl_user u ON p.user_id = u.user_id " +
                    "WHERE p.payment_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Payment payment = mapResultSetToEntity(rs);
                
                // Map booking
                Booking booking = new Booking();
                booking.setBookingId(payment.getBookingId());
                booking.setTotalAmount(rs.getDouble("total_amount"));
                payment.setBooking(booking);
                
                // Map user
                if (payment.getUserId() != null) {
                    User user = new User();
                    user.setUserId(payment.getUserId());
                    user.setFullname(rs.getString("user_fullname"));
                    payment.setUser(user);
                }
                
                return payment;
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Payment> findByBookingId(Integer bookingId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE booking_id = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Payment> findByStatus(Integer status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE payment_status = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Payment> findByPaidBy(String paidBy) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE paid_by = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, paidBy);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Payment> findTodayPayments() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE DATE(payment_date) = CURRENT_DATE ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Payment> searchByDateRange(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE DATE(payment_date) BETWEEN ? AND ? " +
                    "ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(Payment payment) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "booking_id = ?, amount_due = ?, amount_paid = ?, remarks = ?, " +
                    "payment_status = ?, paid_by = ?, user_id = ?, payment_date = ? " +
                    "WHERE payment_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDouble(2, payment.getAmountDue());
            
            if (payment.getAmountPaid() != null) {
                pstmt.setDouble(3, payment.getAmountPaid());
            } else {
                pstmt.setDouble(3, 0.0);
            }
            
            pstmt.setString(4, payment.getRemarks());
            pstmt.setInt(5, payment.getPaymentStatus());
            pstmt.setString(6, payment.getPaidBy());
            
            if (payment.getUserId() != null) {
                pstmt.setInt(7, payment.getUserId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            if (payment.getPaymentDate() != null) {
                pstmt.setTimestamp(8, payment.getPaymentDate());
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }
            
            pstmt.setInt(9, payment.getPaymentId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean updatePaymentStatus(Integer paymentId, Integer status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET payment_status = ? WHERE payment_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, paymentId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean makePayment(Integer paymentId, Double amount, String paidBy) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "amount_paid = amount_paid + ?, paid_by = ?, payment_date = CURRENT_TIMESTAMP, " +
                    "payment_status = CASE WHEN (amount_paid + ?) >= amount_due THEN ? ELSE ? END " +
                    "WHERE payment_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, paidBy);
            pstmt.setDouble(3, amount);
            pstmt.setInt(4, Payment.STATUS_PAID); // Fully paid
            pstmt.setInt(5, Payment.STATUS_PARTIAL); // Partial payment
            pstmt.setInt(6, paymentId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE payment_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public int countPayments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Double calculateTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(amount_paid) FROM " + getTableName() + 
                    " WHERE payment_status IN (2, 3)"; // Paid or Refunded
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Double calculateDailyRevenue(Date date) throws SQLException {
        String sql = "SELECT SUM(amount_paid) FROM " + getTableName() + 
                    " WHERE DATE(payment_date) = ? AND payment_status IN (2, 3)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, date);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Double calculateOutstandingByBooking(Integer bookingId) throws SQLException {
        String sql = "SELECT (amount_due - COALESCE(SUM(amount_paid), 0)) " +
                    "FROM " + getTableName() + 
                    " WHERE booking_id = ? " +
                    "GROUP BY amount_due";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean isBookingFullyPaid(Integer bookingId) throws SQLException {
        String sql = "SELECT (amount_due - COALESCE(SUM(amount_paid), 0)) <= 0 " +
                    "FROM " + getTableName() + 
                    " WHERE booking_id = ? " +
                    "GROUP BY amount_due";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
}