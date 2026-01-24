package dao;

import java.sql.*;

public abstract class BaseDAO<T> {
    
    // Get database connection
    protected Connection getConnection() throws SQLException {
        return utils.DatabaseConnection.getConnection();
    }
    
    // Resource closing methods (multiple signatures for flexibility)
    protected void closeResources(ResultSet rs, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    protected void closeResources(PreparedStatement pstmt, ResultSet rs) {
        closeResources(rs, pstmt);
    }
    
    protected void closeResources(PreparedStatement pstmt) {
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    protected void closeResources(ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // For backward compatibility (what your other DAOs are calling)
    protected void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            // DO NOT close connection if using singleton
            // if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Abstract methods that must be implemented
    protected abstract String getTableName();
    protected abstract String getIdColumnName();
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    // Optional CRUD methods (can be overridden by child classes)
    public T findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }
    
    public java.util.List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        java.util.List<T> list = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}   