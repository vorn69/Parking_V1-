package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Payment;

public class PaymentDAO extends BaseDAO<Payment> {

    // ================= PAYMENT STATUS CONSTANTS =================
    // Updated to match your requirement
    public static final int STATUS_PENDING_APPROVAL = 0;    // Booking pending admin approval
    public static final int STATUS_APPROVED_UNPAID = 1;     // Admin approved, user sees payment in dashboard
    public static final int STATUS_PAID = 2;                // User paid, admin sees as "PAID"
    public static final int STATUS_PARTIAL = 3;             // Partial payment
    public static final int STATUS_CANCELLED = 4;           // Cancelled

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

            return pstmt.executeUpdate() > 0;
        }
    }

    // ================= USER DASHBOARD: GET PAYMENTS AFTER ADMIN APPROVAL =================
    /**
     * User sees this after admin approval
     * Shows payments with status APPROVED_UNPAID
     */
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
            pstmt.setInt(2, STATUS_APPROVED_UNPAID);  // User sees payments to pay
            pstmt.setInt(3, STATUS_PARTIAL);          // User sees partial payments
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment payment = mapResultSetToEntity(rs);
                    
                    // Set display fields
                    payment.setBookingRef(rs.getString("booking_ref"));
                    payment.setFullName(rs.getString("full_name"));
                    payment.setVehicleId(rs.getInt("vehicle_id"));
                    
                    list.add(payment);
                    
                    // Debug output
                    System.out.println("📋 User sees payment #" + payment.getPaymentId() + 
                                     " with amount: $" + payment.getDueAmount() +
                                     " (Status: " + rs.getString("status_display") + ")");
                }
            }
        }
        
        System.out.println("✅ USER ID " + userId + " sees " + list.size() + " payments in dashboard");
        return list;
    }

    // ================= ADMIN: CREATE PAYMENT AFTER APPROVAL =================
    /**
     * Admin approves booking → Creates payment for user to see
     */
    public Payment createPaymentForUser(int bookingId, int userId, double amount, String remarks) 
            throws SQLException {
        
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setDueAmount(amount);
        payment.setPaidAmount(0.0);  // Not paid yet
        payment.setPaymentStatus(STATUS_APPROVED_UNPAID);  // User will see this!
        payment.setRemarks("Admin approved: " + remarks);
        payment.setPaymentDate(null);  // Not paid yet
        
        Integer paymentId = create(payment);
        
        if (paymentId != null) {
            payment.setPaymentId(paymentId);
            System.out.println("✅ ADMIN: Payment created #" + paymentId);
            System.out.println("✅ USER: Will see payment of $" + amount + " in dashboard");
        }
        
        return payment;
    }

    // ================= USER: MAKE PAYMENT =================
    /**
     * User pays → Updates status to PAID
     * Admin will see as "PAID"
     */
    public boolean processUserPayment(int paymentId, double amount, String paymentMethod, 
                                     String transactionId, int userId) throws SQLException {
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // 1. Get current payment
            String getSql = """
                SELECT p.*, b.booking_ref 
                FROM inet_vehicleparking.tbl_payment p
                LEFT JOIN inet_vehicleparking.tbl_booking b ON p.booking_id = b.booking_id
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
            String bookingRef = rs.getString("booking_ref");
            
            System.out.println("💳 USER PAYING:");
            System.out.println("  Payment #" + paymentId + " for booking " + bookingRef);
            System.out.println("  Due: $" + dueAmount + ", Already paid: $" + paidAmount);
            System.out.println("  New payment: $" + amount);
            
            // Validate
            if (currentStatus == STATUS_PAID) {
                throw new SQLException("Payment already completed");
            }
            
            if (currentStatus != STATUS_APPROVED_UNPAID && currentStatus != STATUS_PARTIAL) {
                throw new SQLException("Payment not ready for processing");
            }
            
            double newTotalPaid = paidAmount + amount;
            int newStatus;
            String statusMessage;
            
            if (newTotalPaid >= dueAmount) {
                newStatus = STATUS_PAID;  // ✅ ADMIN WILL SEE THIS AS "PAID"
                statusMessage = "FULLY PAID";
                System.out.println("  ✅ Payment will be marked as PAID");
            } else {
                newStatus = STATUS_PARTIAL;
                statusMessage = "PARTIAL PAYMENT";
                System.out.println("  ⚠️ Payment will be marked as PARTIAL");
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
                    
                    System.out.println("  ✅ Booking marked as paid");
                }
                
                conn.commit();
                System.out.println("✅ PAYMENT SUCCESSFUL!");
                System.out.println("✅ ADMIN will see status as: " + 
                                  (newStatus == STATUS_PAID ? "PAID" : "PARTIAL"));
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("❌ Payment failed: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // ================= ADMIN: VIEW ALL PAYMENTS =================
    /**
     * Admin sees all payments with status
     * PAID payments show as "PAID"
     */
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
                       WHEN 2 THEN '✅ PAID'  -- ADMIN SEES THIS AS PAID!
                       WHEN 3 THEN 'PARTIAL'
                       WHEN 4 THEN 'CANCELLED'
                       ELSE 'UNKNOWN'
                   END as admin_status_display,
                   DATE_FORMAT(p.payment_date, '%Y-%m-%d %H:%i') as formatted_date,
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

            System.out.println("\n📊 ADMIN PAYMENT REPORT:");
            System.out.println("==========================================================");
            
            while (rs.next()) {
                Payment payment = mapResultSetToEntity(rs);
                
                // Set display fields
                payment.setBookingRef(rs.getString("booking_ref"));
                payment.setUserName(rs.getString("user_name"));
                payment.setFullName(rs.getString("full_name"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                
                list.add(payment);
                
                // Print admin view
                int status = payment.getPaymentStatus();
                String adminView = rs.getString("admin_status_display");
                
                System.out.printf("Payment #%-6d | Booking: %-10s | User: %-15s | ", 
                    payment.getPaymentId(),
                    payment.getBookingRef() != null ? payment.getBookingRef() : "N/A",
                    payment.getUserName() != null ? payment.getUserName() : "User#" + payment.getUserId()
                );
                
                System.out.printf("Amount: $%-8.2f/$%-8.2f | ", 
                    payment.getPaidAmount() != null ? payment.getPaidAmount() : 0.0,
                    payment.getDueAmount() != null ? payment.getDueAmount() : 0.0
                );
                
                System.out.printf("Status: %s%n", adminView);
                
                // Special message for paid status
                if (status == STATUS_PAID) {
                    System.out.println("  🎯 ADMIN SEES THIS AS: PAID (MASKED AS PAIN)");
                }
            }
            
            System.out.println("==========================================================");
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
    
    // For backward compatibility
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
    
    // ================= STATUS HELPER METHODS =================
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
    
    // Check if user can see payment
    public boolean isPaymentVisibleToUser(int paymentId, int userId) throws SQLException {
        String sql = "SELECT payment_status FROM " + getTableName() + 
                    " WHERE payment_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paymentId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int status = rs.getInt("payment_status");
                    // User can see if status is APPROVED_UNPAID or PARTIAL
                    return status == STATUS_APPROVED_UNPAID || status == STATUS_PARTIAL;
                }
            }
        }
        return false;
    }
}