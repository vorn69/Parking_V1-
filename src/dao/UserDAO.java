// src/dao/UserDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.User;
import models.UserGroup;

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
        user.setAvatar(rs.getBytes("avatar"));
        user.setFullname(rs.getString("fullname"));
        user.setContact(rs.getString("contact"));
        user.setEmail(rs.getString("email"));
        user.setUserGroupId(rs.getInt("user_group_id"));
        user.setStatus(rs.getInt("status"));
        
        // Handle nullable timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (!rs.wasNull()) {
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
        }
        
        return user;
    }
    
    public Integer create(User user) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + 
                    " (username, password, avatar, fullname, contact, email, " +
                    "user_group_id, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            
            if (user.getAvatar() != null) {
                pstmt.setBytes(3, user.getAvatar());
            } else {
                pstmt.setNull(3, Types.BINARY);
            }
            
            pstmt.setString(4, user.getFullname());
            pstmt.setString(5, user.getContact());
            pstmt.setString(6, user.getEmail());
            
            if (user.getUserGroupId() != null) {
                pstmt.setInt(7, user.getUserGroupId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            pstmt.setInt(8, user.getStatus() != null ? user.getStatus() : 1);
            
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
    
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ?";
        
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
    
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public User findByUsernameWithGroup(String username) throws SQLException {
        String sql = "SELECT u.*, g.* FROM " + getTableName() + " u " +
                    "LEFT JOIN inet_vehicleparking.tbl_user_group g ON u.user_group_id = g.user_group_id " +
                    "WHERE u.username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToEntity(rs);
                
                // Map user group
                if (rs.getInt("g.user_group_id") > 0) {
                    UserGroup group = new UserGroup();
                    group.setUserGroupId(rs.getInt("g.user_group_id"));
                    group.setGroupName(rs.getString("g.group_name"));
                    group.setDescription(rs.getString("g.description"));
                    group.setAllowAdd(rs.getInt("g.allow_add"));
                    group.setAllowEdit(rs.getInt("g.allow_edit"));
                    group.setAllowDelete(rs.getInt("g.allow_delete"));
                    group.setAllowPrint(rs.getInt("g.allow_print"));
                    group.setAllowImport(rs.getInt("g.allow_import"));
                    group.setAllowExport(rs.getInt("g.allow_export"));
                    user.setUserGroup(group);
                }
                
                return user;
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY fullname";
        List<User> users = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
            return users;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<User> findByStatus(Integer status) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE status = ? ORDER BY fullname";
        List<User> users = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
            return users;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                    "username = ?, password = ?, avatar = ?, fullname = ?, " +
                    "contact = ?, email = ?, user_group_id = ?, status = ?, " +
                    "updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            
            if (user.getAvatar() != null) {
                pstmt.setBytes(3, user.getAvatar());
            } else {
                pstmt.setNull(3, Types.BINARY);
            }
            
            pstmt.setString(4, user.getFullname());
            pstmt.setString(5, user.getContact());
            pstmt.setString(6, user.getEmail());
            
            if (user.getUserGroupId() != null) {
                pstmt.setInt(7, user.getUserGroupId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            pstmt.setInt(8, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setInt(9, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean updatePassword(Integer userId, String newPassword) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE user_id = ?";
        
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
    
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + 
                    " WHERE username = ? AND password = ? AND status = 1";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public int countUsers() throws SQLException {
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
}   