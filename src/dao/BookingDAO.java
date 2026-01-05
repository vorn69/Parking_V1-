// src/dao/BookingDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Booking;
import models.ParkingSlot;
import models.User;
import models.Vehicle;
import models.VehicleOwner;

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
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setVehicleId(rs.getInt("vehicle_id"));
        booking.setDurationOfBooking(rs.getString("duration_of_booking"));
        booking.setSlotId(rs.getInt("slot_id"));
        booking.setBookingStatus(rs.getInt("booking_status"));
        booking.setRemarks(rs.getString("remarks"));
        booking.setUserId(rs.getInt("user_id"));
        
        // Handle nullable timestamps
        Timestamp bookingTime = rs.getTimestamp("booking_time");
        Timestamp expectedArrival = rs.getTimestamp("expected_arrival");
        Timestamp actualArrival = rs.getTimestamp("actual_arrival");
        Timestamp departureTime = rs.getTimestamp("departure_time");
        
        if (!rs.wasNull()) {
            booking.setBookingTime(bookingTime);
            booking.setExpectedArrival(expectedArrival);
            booking.setActualArrival(actualArrival);
            booking.setDepartureTime(departureTime);
        }
        
        booking.setTotalHours(rs.getDouble("total_hours"));
        booking.setTotalAmount(rs.getDouble("total_amount"));
        
        return booking;
    }
    
    public Integer create(Booking booking) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (customer_id, vehicle_id, duration_of_booking, slot_id, " +
                    "booking_status, remarks, user_id, booking_time, expected_arrival, " +
                    "actual_arrival, departure_time, total_hours, total_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            if (booking.getCustomerId() != null) {
                pstmt.setInt(1, booking.getCustomerId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            
            pstmt.setInt(2, booking.getVehicleId());
            pstmt.setString(3, booking.getDurationOfBooking());
            pstmt.setInt(4, booking.getSlotId());
            pstmt.setInt(5, booking.getBookingStatus() != null ? booking.getBookingStatus() : Booking.STATUS_PENDING);
            
            pstmt.setString(6, booking.getRemarks());
            
            if (booking.getUserId() != null) {
                pstmt.setInt(7, booking.getUserId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            // Timestamps
            if (booking.getBookingTime() != null) {
                pstmt.setTimestamp(8, booking.getBookingTime());
            } else {
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            }
            
            if (booking.getExpectedArrival() != null) {
                pstmt.setTimestamp(9, booking.getExpectedArrival());
            } else {
                pstmt.setNull(9, Types.TIMESTAMP);
            }
            
            if (booking.getActualArrival() != null) {
                pstmt.setTimestamp(10, booking.getActualArrival());
            } else {
                pstmt.setNull(10, Types.TIMESTAMP);
            }
            
            if (booking.getDepartureTime() != null) {
                pstmt.setTimestamp(11, booking.getDepartureTime());
            } else {
                pstmt.setNull(11, Types.TIMESTAMP);
            }
            
            if (booking.getTotalHours() != null) {
                pstmt.setDouble(12, booking.getTotalHours());
            } else {
                pstmt.setNull(12, Types.DOUBLE);
            }
            
            if (booking.getTotalAmount() != null) {
                pstmt.setDouble(13, booking.getTotalAmount());
            } else {
                pstmt.setNull(13, Types.DOUBLE);
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
    
    public Booking findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE booking_id = ?";
        
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
    
    public Booking findByIdWithDetails(Integer id) throws SQLException {
        String sql = "SELECT b.*, vo.vehicle_owner_name, vo.vehicle_owner_contact, " +
                    "v.vehicle_plate_number, vc.vehicle_category_name, " +
                    "ps.parking_slot_number, u.fullname as user_fullname " +
                    "FROM " + getTableName() + " b " +
                    "LEFT JOIN inet_vehicleparking.tbl_vehicle_owner vo ON b.customer_id = vo.vehicle_owner_id " +
                    "JOIN inet_vehicleparking.tbl_vehicle v ON b.vehicle_id = v.vehicle_id " +
                    "JOIN inet_vehicleparking.tbl_vehicle_category vc ON v.vehicle_category_id = vc.vehicle_category_id " +
                    "JOIN inet_vehicleparking.tbl_parking_slot ps ON b.slot_id = ps.parking_slot_id " +
                    "LEFT JOIN inet_vehicleparking.tbl_user u ON b.user_id = u.user_id " +
                    "WHERE b.booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Booking booking = mapResultSetToEntity(rs);
                
                // Map customer
                if (booking.getCustomerId() != null) {
                    VehicleOwner customer = new VehicleOwner();
                    customer.setVehicleOwnerId(booking.getCustomerId());
                    customer.setVehicleOwnerName(rs.getString("vehicle_owner_name"));
                    customer.setVehicleOwnerContact(rs.getString("vehicle_owner_contact"));
                    booking.setCustomer(customer);
                }
                
                // Map vehicle
                Vehicle vehicle = new Vehicle();
                vehicle.setVehicleId(booking.getVehicleId());
                vehicle.setVehiclePlateNumber(rs.getString("vehicle_plate_number"));
                booking.setVehicle(vehicle);
                
                // Map parking slot
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingSlotId(booking.getSlotId());
                slot.setParkingSlotNumber(rs.getInt("parking_slot_number"));
                booking.setParkingSlot(slot);
                
                // Map user
                if (booking.getUserId() != null) {
                    User user = new User();
                    user.setUserId(booking.getUserId());
                    user.setFullname(rs.getString("user_fullname"));
                    booking.setUser(user);
                }
                
                return booking;
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findByStatus(Integer status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE booking_status = ? ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findByCustomerId(Integer customerId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE customer_id = ? ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findByVehicleId(Integer vehicleId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE vehicle_id = ? ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vehicleId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findBySlotId(Integer slotId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE slot_id = ? ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findActiveBookings() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE booking_status IN (1, 2) ORDER BY booking_time DESC"; // Confirmed or Checked In
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> findTodayBookings() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE DATE(booking_time) = CURRENT_DATE ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<Booking> searchByDateRange(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE DATE(booking_time) BETWEEN ? AND ? " +
                    "ORDER BY booking_time DESC";
        List<Booking> bookings = new ArrayList<>();
        
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
                bookings.add(mapResultSetToEntity(rs));
            }
            return bookings;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(Booking booking) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "customer_id = ?, vehicle_id = ?, duration_of_booking = ?, " +
                    "slot_id = ?, booking_status = ?, remarks = ?, user_id = ?, " +
                    "booking_time = ?, expected_arrival = ?, actual_arrival = ?, " +
                    "departure_time = ?, total_hours = ?, total_amount = ? " +
                    "WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            
            if (booking.getCustomerId() != null) {
                pstmt.setInt(1, booking.getCustomerId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            
            pstmt.setInt(2, booking.getVehicleId());
            pstmt.setString(3, booking.getDurationOfBooking());
            pstmt.setInt(4, booking.getSlotId());
            pstmt.setInt(5, booking.getBookingStatus());
            pstmt.setString(6, booking.getRemarks());
            
            if (booking.getUserId() != null) {
                pstmt.setInt(7, booking.getUserId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            // Timestamps
            if (booking.getBookingTime() != null) {
                pstmt.setTimestamp(8, booking.getBookingTime());
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }
            
            if (booking.getExpectedArrival() != null) {
                pstmt.setTimestamp(9, booking.getExpectedArrival());
            } else {
                pstmt.setNull(9, Types.TIMESTAMP);
            }
            
            if (booking.getActualArrival() != null) {
                pstmt.setTimestamp(10, booking.getActualArrival());
            } else {
                pstmt.setNull(10, Types.TIMESTAMP);
            }
            
            if (booking.getDepartureTime() != null) {
                pstmt.setTimestamp(11, booking.getDepartureTime());
            } else {
                pstmt.setNull(11, Types.TIMESTAMP);
            }
            
            if (booking.getTotalHours() != null) {
                pstmt.setDouble(12, booking.getTotalHours());
            } else {
                pstmt.setNull(12, Types.DOUBLE);
            }
            
            if (booking.getTotalAmount() != null) {
                pstmt.setDouble(13, booking.getTotalAmount());
            } else {
                pstmt.setNull(13, Types.DOUBLE);
            }
            
            pstmt.setInt(14, booking.getBookingId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean updateStatus(Integer bookingId, Integer status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET booking_status = ? WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, bookingId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean checkIn(Integer bookingId) throws SQLException {
        String sql = "UPDATE " + getTableName() + 
                    " SET booking_status = ?, actual_arrival = CURRENT_TIMESTAMP " +
                    "WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Booking.STATUS_CHECKED_IN);
            pstmt.setInt(2, bookingId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean checkOut(Integer bookingId, Double totalHours, Double totalAmount) throws SQLException {
        String sql = "UPDATE " + getTableName() + 
                    " SET booking_status = ?, departure_time = CURRENT_TIMESTAMP, " +
                    "total_hours = ?, total_amount = ? WHERE booking_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Booking.STATUS_CHECKED_OUT);
            
            if (totalHours != null) {
                pstmt.setDouble(2, totalHours);
            } else {
                pstmt.setNull(2, Types.DOUBLE);
            }
            
            if (totalAmount != null) {
                pstmt.setDouble(3, totalAmount);
            } else {
                pstmt.setNull(3, Types.DOUBLE);
            }
            
            pstmt.setInt(4, bookingId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE booking_id = ?";
        
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
    
    public int countBookings() throws SQLException {
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
    
    public int countBookingsByStatus(Integer status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE booking_status = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Double calculateDailyRevenue(Date date) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM " + getTableName() + 
                    " WHERE DATE(departure_time) = ? AND booking_status = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, date);
            pstmt.setInt(2, Booking.STATUS_CHECKED_OUT);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean isSlotBooked(Integer slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + 
                    " WHERE slot_id = ? AND booking_status IN (1, 2)"; // Confirmed or Checked In
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public Booking findActiveBookingBySlot(Integer slotId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE slot_id = ? AND booking_status IN (1, 2) " +
                    "ORDER BY booking_time DESC LIMIT 1";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slotId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
}