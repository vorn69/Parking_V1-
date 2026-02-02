// src/dao/UserDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.User;

public class UserDAO extends BaseDAO<User> {
    
    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_user";
    }
    
        @Override
        protected String getIdColumnName() {
            return "user_id";
        }
    
    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        
        // Handle nullable fields
        user.setFullname(rs.getString("fullname"));
        user.setContact(rs.getString("contact"));
        user.setEmail(rs.getString("email"));
        user.setUserGroupId(rs.getInt("user_group_id"));
        user.setStatus(rs.getInt("status"));
        
        // Handle avatar (BLOB)
        try {
            Blob avatarBlob = rs.getBlob("avatar");
            if (avatarBlob != null) {
                user.setAvatar(avatarBlob.getBytes(1, (int) avatarBlob.length()));
            }
        } catch (SQLException e) {
            // Avatar column might not exist or be null
        }
        
        // Handle timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt);
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt);
        }
        
        return user;
    }
    
    // ==================== AUTHENTICATION METHODS ====================
    
    /**
     * Authenticate user (login)
     */
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE (username = ? OR email = ?) AND password = ? AND status = 1";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }
        }
    }
    
    /**
     * Find vehicle owner ID linked to user
     */
    public Integer findOwnerIdByUserId(Integer userId) throws SQLException {
        String sql = "SELECT vehicle_owner_id FROM inet_vehicleparking.tbl_vehicle_owner WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("vehicle_owner_id") : null;
            }
        }
    }
    
    /**
     * Check if username already exists
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
     * Create new user
     */
    public Integer create(User user) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (username, password, fullname, contact, email, " +
                    "user_group_id, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullname());
            pstmt.setString(4, user.getContact());
            pstmt.setString(5, user.getEmail());
            pstmt.setInt(6, user.getUserGroupId() != null ? user.getUserGroupId() : 2);
            pstmt.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
    
    /**
     * Find user by username or email
     */
    public User findByUsernameOrEmail(String usernameOrEmail) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE username = ? OR email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }
    
    /**
     * Update user profile
     */
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "fullname = ?, contact = ?, email = ?, updated_at = NOW() " +
                    "WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFullname());
            pstmt.setString(2, user.getContact());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Change password
     */
    public boolean changePassword(Integer userId, String newPassword) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET password = ?, updated_at = NOW() " +
                    "WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Activate/Deactivate user
     */
    public boolean setStatus(Integer userId, Integer status) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET status = ?, updated_at = NOW() " +
                    "WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, status);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // ==================== STANDARD CRUD METHODS ====================
    
    @Override
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }
    
    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY fullname";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
        }
        return users;
    }
    
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "username = ?, fullname = ?, contact = ?, email = ?, " +
                    "user_group_id = ?, status = ?, updated_at = NOW() " +
                    "WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getFullname());
            pstmt.setString(3, user.getContact());
            pstmt.setString(4, user.getEmail());
            pstmt.setInt(5, user.getUserGroupId());
            pstmt.setInt(6, user.getStatus());
            pstmt.setInt(7, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        // Instead of deleting, deactivate the user
        return setStatus(id, 0);
    }
    
    // ==================== ADDITIONAL METHODS ====================
    
    /**
     * Find users by user group
     */
    public List<User> findByUserGroupId(Integer userGroupId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE user_group_id = ? ORDER BY fullname";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userGroupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToEntity(rs));
                }
            }
        }
        return users;
    }
    
    /**
     * Find all customers (assuming user_group_id = 2)
     */
    public List<User> findAllCustomers() throws SQLException {
        return findByUserGroupId(2);
    }
    
    /**
     * Find all active users
     */
    public List<User> findActiveUsers() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE status = 1 ORDER BY fullname";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
        }
        return users;
    }
    
    /**
     * Search users by name, username, or email
     */
    public List<User> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE fullname ILIKE ? OR username ILIKE ? OR email ILIKE ? " +
                    "ORDER BY fullname";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchTerm = "%" + keyword + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setString(3, searchTerm);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToEntity(rs));
                }
            }
        }
        return users;
    }
    
    /**
     * Count total users
     */
    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Count users by status
     */
    public int countByStatus(Integer status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE status = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    
}